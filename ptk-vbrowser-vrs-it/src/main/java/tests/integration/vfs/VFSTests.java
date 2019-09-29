/*
 * Copyright 2006-2010 Virtual Laboratory for e-Science (www.vl-e.nl)
 * Copyright 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package tests.integration.vfs;

import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_FILE_SIZE;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_HOSTNAME;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_LOCATION;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_MIMETYPE;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_NAME;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_PATH;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_PORT;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_RESOURCE_EXISTS;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_RESOURCE_TYPE;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_SCHEME;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.io.IOUtil;
import nl.esciencecenter.ptk.io.RandomReadable;
import nl.esciencecenter.ptk.io.RandomWritable;
import nl.esciencecenter.ptk.net.URIFactory;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VFileSystem;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceAccessDeniedException;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceAlreadyExistsException;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceCreationException;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.io.VReplicatable;
import nl.esciencecenter.vbrowser.vrs.io.VStreamReadable;
import nl.esciencecenter.vbrowser.vrs.io.VStreamWritable;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import tests.integration.vfs.Settings.TestLocation;

/**
 * This is an abstract test class which must be subclassed by a VFS implementation.
 *
 * @author P.T. de Boer
 */
public abstract class VFSTests extends VTestCase {

    private static final String TEST_CONTENTS = ">>> This is a testfile used for the VFS unit tests  <<<\n"
            + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\n" + "0123456789@#$%*()_+\n"
            + "Strange characters:áéíóúâêîôû\n<TODO more...>\nUTF8:<TODO>\n"
            + "\n --- If you read this, you can delete this file ---\n";

    private static int uniquepathnr = 0;

    // ========================================================================
    // Instance
    // ========================================================================

    private VFSPath remoteTestDir = null;

    protected VFSPath localTempDir = null;

    private boolean testRenames = true;

    private boolean doWrites = true;

    private boolean doBigTests = true;

    private VRL localTempDirVrl = Settings.getTestLocation(TestLocation.VFS_LOCAL_TEMPDIR_LOCATION);

    private VRL remoteTestDirVrl = null;

    private Object uniquepathnrMutex = new Object();

    private Object setupMutex = new Object();

    private VRL otherRemoteLocation = null;

    private boolean testEncodedPaths = true;

    private boolean testStrangeChars = true;

    /**
     * Return path with incremental number to make sure each new file did exist before
     */
    public String nextFilename(String prefix) {
        synchronized (uniquepathnrMutex) {
            return prefix + "-" + uniquepathnr++;
        }
    }

    /**
     * Override this method if the local test dir has to have a different location
     */
    public VRL getLocalTempDirVRL() {
        return localTempDirVrl;
    }

    public VRL getRemoteLocation() {
        return remoteTestDirVrl;
    }

    public void setRemoteLocation(VRL remoteLocation) {
        this.remoteTestDirVrl = remoteLocation;
    }

    private String readContentsAsString(VFSPath file) throws IOException, VrsException, URISyntaxException {
        return getVFS().readContentsAsString(file);
    }

    private void writeContents(VFSPath file, String text) throws IOException, VrsException, URISyntaxException {
        this.getVFS().writeContents(file, text);
    }

    private void writeContents(VFSPath file, byte[] bytes) throws IOException, VrsException, URISyntaxException {
        this.getVFS().writeContents(file,bytes); 
    }

    private void streamWrite(VFSPath file, byte[] buffer, int bufOffset, int numBytes) throws IOException, VrsException {
        OutputStream outps = getVFS().createOutputStream(file, false);
        outps.write(buffer, bufOffset, numBytes);
        try {
            outps.close();
        } catch (IOException e) {
            // logger.
        }
        // after a stream write, update file meta data since the length and time has changed.
        file.sync();
    }

    private byte[] readContents(VFSPath file) throws IOException, VrsException {
        return getVFS().readContents(file);
    }

    /**
     * Sets up the tests fixture. (Called before every tests case method.)
     *
     * @throws Exception
     */
    @Before
    public void setUpTestEnv() throws Exception {
        
        debugPrintf("setUp(): Checking remote test location:%s\n", getRemoteLocation());
        checkAuthentication();

        synchronized (setupMutex) {
            // create/get only if VFSPath hasn't been fetched/created before !
            if (getRemoteTestDir() == null) {
                if (getVFS().existsDir(getRemoteLocation())) {
                    setRemoteTestDir(getVFS().openVFSPath(getRemoteLocation()));
                    debugPrintf("setUp(): Using existing remoteDir:%s\n", getRemoteTestDir());
                } else {
                    // create complete path !
                    try {
                        debugPrintf("setUp:Creating new remote test location:%s\n", getRemoteLocation());
                        setRemoteTestDir(getVFS().mkdirs(getRemoteLocation()));
                        messagePrintf("setUp:Created new remote test directory:%s\n", getRemoteTestDir());
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
            }

            if (localTempDir == null) {
                VRL localdir = getLocalTempDirVRL();

                if (getVFS().existsDir(localdir)) {
                    localTempDir = getVFS().openVFSPath(localdir);
                    // localTempDir.delete(true);
                } else {
                    // create complete path !
                    localTempDir = getVFS().mkdirs(localdir);
                    messagePrintf("setUp: created new local test location:%s\n", localTempDir);
                }
            }
        }
    }

    protected boolean existsDir(VFSPath parent, String path) throws VrsException {
        return getVFS().existsDir(parent.resolveVRL(path));
    }

    protected boolean existsFile(VFSPath parent, String path) throws VrsException {
        return getVFS().existsFile(parent.resolveVRL(path));
    }

    protected VFSPath getRemoteFile(String fileName) throws VrsException {
        VFSPath dir = this.getRemoteTestDir();
        VFSPath subPath = dir.resolve(fileName);
        return subPath;
    }

    protected VFSPath getRemoteDir(String dirName) throws VrsException {
        VFSPath dir = this.getRemoteTestDir();
        VFSPath subPath = dir.resolve(dirName);
        return subPath;
    }

    protected VFSPath createRemoteFile(String fileName, boolean ignoreExisting) throws VrsException {
        VFSPath path = getRemoteFile(fileName);
        path.createFile(ignoreExisting);
        return path;
    }

    protected VFSPath createRemoteFile(VRL vrl, boolean ignoreExisting) throws VrsException {
        VFSPath path = getRemoteTestDir().getFileSystem().resolve(vrl);
        path.createFile(ignoreExisting);
        return path;
    }

    protected VFSPath createRemoteDir(String dirName, boolean ignoreExisting) throws VrsException {
        VFSPath subPath = getRemoteDir(dirName);
        subPath.mkdir(ignoreExisting);
        return subPath;
    }

    protected void checkAuthentication() throws Exception {
        ; // check here
    }

    @After
    public void tearDown() {
        //
    }

    /**
     * Whether to test strange characters, like spaces, in paths. SRM doesn't support spaces and strange characters.
     */
    boolean getTestEncodedPaths() {
        return this.testEncodedPaths;
    }

    void setTestEncodedPaths(boolean doEncoding) {
        this.testEncodedPaths = doEncoding;
    }

    boolean getTestStrangeCharsInPaths() {
        return this.testStrangeChars;
    }

    boolean getTestRenames() {
        return testRenames;
    }

    // =============
    // Actual Tests
    // =============

    /**
     * Print some info before starting.
     */
    @Test
    public void testPrintInfo() throws Exception {
        ResourceConfigInfo info = this.getVFS().getVRSContext().getResourceSystemInfoFor(this.getRemoteLocation(), false);

        message(" --- Test Info ---");
        message(" remote test dir         =" + getRemoteTestDir());
        message(" remote test dir exists  =" + getRemoteTestDir().exists());
        message(" - do big tests          =" + this.getTestDoBigTests());
        message(" - do write tests        =" + this.getTestWriteTests());
        message(" - do rename tests       =" + this.getTestRenames());
        message(" - do strange char tests =" + this.getTestStrangeCharsInPaths());
        message(" - do URL en-decoding    =" + this.getTestEncodedPaths());
        message("--- info ---");
        message("" + info);

        if (info != null) {
            message("--- ResourceSystemInfo ---");
            message(" - Host:port           =" + info.getServerHostname() + ":" + info.getServerPort());
            message(" - remote home         =" + info.getServerPath());
            // message(" - info.getUsePassive()=" + info.getUsePassiveMode(false));
        }
    }

    @Test
    public void testFirst() throws Exception {
        setUpTestEnv();

        //
    }

    /**
     * Exist is a basic method used a lot in the unit tests and VRS methods.
     * <p>
     * The exists() methods should return true if resource exists, false if it doesn't exists and only throw an exception when the
     * method couldn't determine whether the resource exists or not.
     *
     */
    @Test
    public void testExists() throws Exception {
        boolean result = existsFile(getRemoteTestDir(), "ThisFileShouldnotexist_1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Assert.assertFalse("Exists(): file should not exist!", result);

        result = existsDir(getRemoteTestDir(), "ThisDirShouldnotexist_1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Assert.assertFalse("Exists(): Dir should not exist!", result);

        result = getRemoteTestDir().exists();
        Assert.assertTrue("Exists(): *** ERROR: Remote Test directory doesn't exists. Tests will fail", result);

        if (this.getTestEncodedPaths()) {
            result = existsFile(getRemoteTestDir(), "This File Should not exist 1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ");
            Assert.assertFalse("Exists(): file should not exist!", result);

            result = existsDir(getRemoteTestDir(), "This Dir Should not exist 1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ");
            Assert.assertFalse("Exists(): Dir should not exist!", result);
        }
    }

    @Test
    public void testRootExists() throws Exception {
        VRL rootPath = null;
        ResourceConfigInfo inf = this.getResourceSystemInfo();
        // Use "/" or explicit rootPath
        // if (inf!=null)
        // rootPath=inf.getRootPath();
        // else
        rootPath = this.getRemoteLocation().replacePath("/");

        VFSPath rootDir = getRemoteTestDir().getFileSystem().resolve(rootPath);
        boolean result = rootDir.exists(); // existsDir(rootPath.getPath());
        Assert.assertTrue("Exists(): root path  '" + rootPath + "' Doesn't exist!", result);
    }

    public ResourceConfigInfo getResourceSystemInfo() throws Exception {
        return this.getVFS().getVRSContext().getResourceSystemInfoFor(this.remoteTestDir.getVRL(), false);
    }

    /**
     * Regression bug: Test whether "/~" resolves to remote (Default) home location and 'exists()'.
     */
    @Test
    public void testTildeExpansion() throws Exception {
        //
        VFSPath dir = getRemoteTestDir();
        VFileSystem fs = dir.getFileSystem();
        //
        VFSPath resolved = fs.resolve("~/someDirectory");
        Assert.assertNotNull("Tilde should be allowed as character in path",resolved);
        //
        for (String tildePath : new String[] { "~","~/" }) {

            VFSPath node = fs.resolve(tildePath);
            VRL homeVrl = getVRSContext().getHomeVRL();

            if (node.getVRL().hasScheme("file")) {
                // check for local file system
                Assert.assertEquals("Resolving of path starting with '" + tildePath + "' should match wich local user home.",
                        homeVrl, node.getVRL());
            }

            Assert.assertTrue("Default HOME reports is does not exist (SRB might do this):'"+tildePath+"' => '" + node + "'", node.exists());
            messagePrintf("Tilde expansion of '/~' => '%s'\n", node.getVRL().getPath());
        }

    }

    /**
     * Should be first test, since other tests methods create and delete files for testing.
     */
    @Test
    public void testCreateAndDeleteFile() throws Exception {
        verbose(1, "Remote testdir=" + getRemoteTestDir());

        // ---
        // Use createFile() method
        // ---

        VFSPath newFile = getRemoteFile(nextFilename("testFile"));
        newFile.createFile(false);
        Assert.assertNotNull("New created file may not be NULL", newFile);
        Assert.assertTrue("Length of newly created file must be 0!:" + getRemoteTestDir(), newFile.fileLength() == 0);
        newFile.delete();
        Assert.assertFalse("After deletion, a file may NOT report it still 'exists'!", newFile.exists());

        // ---
        // Use newFile().createFile() method
        // ---
        newFile = createRemoteFile("testFile1c", false);
        Assert.assertNotNull("New created file may not be NULL", newFile);
        Assert.assertTrue("Length of newly created file must be 0!:" + getRemoteTestDir(), newFile.fileLength() == 0);
        newFile.delete();
        Assert.assertFalse("After deletion, a file may NOT report it still 'exists'!", newFile.exists());
    }

    /**
     * Test FileSystem.resolvePath(...), should behave the same as VFSPath.resolvePath(...)
     *
     * @throws Exception
     */
    @Test
    public void testFSCreateAndDeleteFile() throws Exception {
        verbose(1, "Remote testdir=" + getRemoteTestDir());

        VRL fullpath = getRemoteTestDir().resolveVRL(nextFilename("testFSFile"));
        VFileSystem fs = getRemoteTestDir().getFileSystem();

        // ===
        // use createFile:
        // ===

        VFSPath newFile = createFile(fs, fullpath, true);

        // sftp created 1-length new files !
        Assert.assertNotNull("New created file may not be NULL", newFile);
        Assert.assertTrue("Length of newly created file must be 0!:" + getRemoteTestDir(), newFile.fileLength() == 0);

        newFile.delete();
        Assert.assertFalse("After deletion, a file may NOT report it still 'exists'!", newFile.exists());

        // ===
        // use FileSystem.newFile().createFile()
        // ===
        fullpath = getRemoteTestDir().resolveVRL(nextFilename("testFSFile"));
        newFile = fs.resolve(fullpath);
        newFile.createFile(false);

        // sftp created 1-length new files !
        Assert.assertNotNull("New created file may not be NULL", newFile);
        Assert.assertTrue("Length of newly created file must be 0!:" + getRemoteTestDir(), newFile.fileLength() == 0);

        newFile.delete();
        Assert.assertFalse("After deletion, a file may NOT report it still 'exists'!", newFile.exists());
    }

    private VFSPath createFile(VFileSystem fs, VRL fullpath, boolean ignore) throws Exception {
        VFSPath file = fs.resolve(fullpath);
        file.createFile(ignore);
        return file;
    }

    /**
     * Should be first test, since other tests methods create and delete files for testing.
     *
     * @throws Exception
     */
    @Test
    public void testCreateAndDeleteFullpathFile() throws Exception {
        verbose(1, "Remote testdir=" + getRemoteTestDir());

        VRL filevrl = getRemoteTestDir().getVRL().appendPath(nextFilename("testFileB"));

        // use fullpath:
        VFSPath newFile = createRemoteFile(filevrl.getPath(), false);

        // sftp created 1-length new files !
        Assert.assertNotNull("New created file may not be NULL. Must throw exception", newFile);
        Assert.assertTrue("Length of new created file must be of 0 size:" + getRemoteTestDir(), newFile.fileLength() == 0);
        Assert.assertEquals("File should use complete pathname as new file name", filevrl.getPath(), newFile.getVRL().getPath());

        // cleanup:
        boolean result = newFile.delete();
        Assert.assertTrue("Cleanup after unit test failed. Deletion failed for:" + newFile, result);
        Assert.assertFalse("After deletion, a file may NOT report it still 'exists'!", newFile.exists());

        // Test ./File !
        filevrl = getRemoteTestDir().resolveVRL("./" + nextFilename("testFileC"));

        // use fullpath:
        newFile = createRemoteFile(filevrl.getPath(), false);

        // sftp created 1-length new files !
        Assert.assertNotNull("New created file may not be NULL. Must throw exception", newFile);
        Assert.assertTrue("Length of new created file must be of 0 size:" + getRemoteTestDir(), newFile.fileLength() == 0);
        Assert.assertEquals("File should use complete pathname as new file name", filevrl.getPath(), newFile.getVRL().getPath());

        // cleanup:
        result = newFile.delete();
        Assert.assertTrue("Cleanup after unit test failed. Deletion failed for:" + newFile, result);
        Assert.assertFalse("After deletion, a file may NOT report it still 'exists'!", newFile.exists());

    }

    /**
     * Should be fist tests, since other tests methods create and delete file their own tests files
     *
     * @throws Exception
     */
    @Test
    public void testCreateAndDeleteFileWithSpace() throws Exception {
        if (this.getTestEncodedPaths() == false) {
            message("***Warning: Skipping test:testCreateDeleteFileWithSpaceAndListParentDir");
            return;
        }

        VFSPath newFile = createRemoteFile(nextFilename("test File D"), false);
        newFile.delete();
    }

    /**
     * Regression test for Grid FTP: remote directory list does NOT like spaces !
     *
     * @throws Exception
     */
    @Test
    public void testCreateDeleteFileWithSpaceAndListParentDir() throws Exception {
        if (this.getTestEncodedPaths() == false) {
            message("***Warning: Skipping test:testCreateDeleteFileWithSpaceAndListParentDir");
            return;
        }

        VFSPath newFile = createRemoteFile(nextFilename("test File E"), false);

        verbose(1, "remote filename with space=" + newFile);

        List<? extends VFSPath> names = getRemoteTestDir().list();

        Assert.assertNotNull("Remote directory contents is NULL after creation of file in:" + getRemoteTestDir(), names);
        Assert.assertFalse("Remote directory contents is empty after creation of file in:" + getRemoteTestDir(),
                names.size() <= 0);

        newFile.delete();
    }

    @Test
    public void testCreateAndIgnoreExistingFile() throws Exception {
        String nextFileName = nextFilename("testFileX");
        // create, may not exists:
        VFSPath newFile = createRemoteFile(nextFileName, false);
        // create, may exists:
        newFile = createRemoteFile(nextFileName, true);
        // cleanup
        newFile.delete();
    }

    // =======================================================================
    // Directory Creation Tests
    // =======================================================================

    /**
     * Other tests realy heavily on the creation and deletion of (remote) directories!
     *
     * @throws Exception
     */
    @Test
    public void testCreateAndDeleteSubDir() throws Exception {
        VFSPath dir = this.getRemoteTestDir();
        VRL dirVrl = dir.getVRL();
        String subDir = nextFilename("testDirA");

        // check VRL solve and VFSPath resolve.
        VRL vrlResolvedVrl = dirVrl.resolvePath(subDir);
        VRL pathResolvedVrl = dir.resolveVRL(subDir);
        Assert.assertEquals("Subpath resolved through VRL.resolvePath() must match VRL resolve through VFSPath", vrlResolvedVrl,
                pathResolvedVrl);

        VFSPath resolvedSubDir = dir.resolve(subDir);
        Assert.assertEquals("Actual VRL from VFSPath.resolvePath() must match VRL resolve path VRL", vrlResolvedVrl,
                resolvedSubDir.getVRL());

        // create;
        resolvedSubDir.mkdir(false);
        Assert.assertTrue("Directory should exist after mkdir", resolvedSubDir.exists());
        resolvedSubDir.delete();
        Assert.assertFalse("Directory should not exist after deletion", resolvedSubDir.exists());
    }

    @Test
    public void testCreateAndList3SubFiles() throws Exception {

        VFSPath newDir = createRemoteDir(nextFilename("testDirD_1234"), false);
        Assert.assertTrue("Directory should exist after mkdir", newDir.exists());

        // create subdir:
        VFSPath file1 = newDir.resolve("subFile1");
        file1.createFile(false);
        VFSPath file2 = newDir.resolve("subFile2");
        file2.createFile(false);
        VFSPath file3 = newDir.resolve("subFile3");
        file3.createFile(false);

        VRL dirVrl = newDir.getVRL();
        VFSPath reDir = newDir.getFileSystem().resolve(dirVrl);

        List<? extends VFSPath> nodes = reDir.list();
        // VFSPath[] nodes = newDir.list();

        Assert.assertEquals("Directory must have 3 sub files", 3, nodes.size());

        file1.delete();
        file2.delete();
        file3.delete();
        newDir.delete();
    }

    @Test
    public void testCreateAndList3SubDirs() throws Exception {
        VFSPath newDir = createRemoteDir(nextFilename("testDirC"), false);
        // create subdir:
        VFSPath subDir1 = newDir.resolve("subDir1");
        subDir1.mkdir(false);
        VFSPath subDir2 = newDir.resolve("subDir2");
        subDir2.mkdir(false);
        VFSPath subDir3 = newDir.resolve("subDir3");
        subDir3.mkdir(false);

        newDir.sync();

        List<? extends VFSPath> nodes = newDir.list();

        Assert.assertEquals("Directory must have 3 subdirectories", 3, nodes.size());

        subDir1.delete();
        subDir2.delete();
        subDir3.delete();
        newDir.delete();

    }

    /**
     * Regression for SRM: cannot delete directory which contains directory and a file.
     */
    @Test
    public void testCreateAndDeleteRecursiveDir() throws Exception {

        VFSPath newDir = createRemoteDir(nextFilename("testDirB"), false);
        // create subdir:
        VFSPath subDir1 = newDir.resolve("subDir1");
        subDir1.mkdir(false);
        VFSPath subDir2 = newDir.resolve("subDir2");
        subDir2.mkdir(false);

        VFSPath file1 = newDir.resolve("subFile1");
        file1.createFile(false);
        VFSPath file2 = newDir.resolve("subFile2s");
        file2.createFile(false);

        VFSPath subsubDir = subDir1.resolve("subsubDir1");
        subsubDir.mkdir(false);
        VFSPath subsubFile = subDir1.resolve("subsubsubFile1");
        subsubFile.createFile(false);

        List<? extends VFSPath> list = newDir.list();

        for (VFSPath node : list)
            messagePrintf(" - %s\n", node);

        newDir.delete(true);

        Assert.assertFalse("Directory should not exist after deletion", newDir.exists());
    }

    /**
     * Test whether a directory can take a full (absolute) path as directory name. New since 0.9.2 !
     *
     * @throws Exception
     */
    @Test
    public void testCreateAndDeleteFullDir() throws Exception {
        VRL fullPath = getRemoteTestDir().resolveVRL(nextFilename("testDirD"));

        // get parent dir to check whether directory allow (sub) directories in
        // the create method.
        VFSPath parentDir = getRemoteTestDir().getParent();
        // use absolute path
        VFSPath newDir = parentDir.resolve(fullPath.getPath());
        newDir.mkdir(true);

        Assert.assertEquals("Directory should use complete pathname as new directory name.", fullPath.getPath(), newDir.getVRL()
                .getPath());
        this.messagePrintf("Full path=%s\n", newDir.getVRL().getPath());
        newDir.delete();

        // check ./path !
        fullPath = getRemoteTestDir().resolveVRL("./" + nextFilename("testDirE"));
        newDir = parentDir.resolve(fullPath.getPath());
        // create single sub directory.
        newDir.mkdir(false);
        Assert.assertEquals("Directory should use complete pathname as new directory name.", fullPath.getPath(), newDir.getVRL()
                .getPath());
        // delete parent !
        newDir.delete(true);

        // New Since 0.9.2:
        // create full directory paths in between ! (as default)
        //
        String pathStr = getRemoteTestDir().getVRL().getPath() + URIFactory.URI_SEP_CHAR + "testDirF";
        newDir = getRemoteTestDir().resolve(pathStr);

        Assert.assertEquals("Directory should use complete pathname as new directory name", pathStr, newDir.getVRL().getPath());
        // delete parent of parent !

        newDir.mkdir(false);
        Assert.assertTrue("New VFSPath doesn't exist:" + newDir, newDir.exists());
        Assert.assertTrue("New VFSPath path must be a directory:" + newDir, newDir.isDir());

        newDir.delete(true);
    }

    /**
     * Test VFileSystem.createDir() should behave the same as VFSPath.createDir() ... New since 0.9.2 !
     *
     * @throws Exception
     */
    @Test
    public void testFSCreateAndDeleteDir() throws Exception {
        VRL fullPath = getRemoteTestDir().resolveVRL(nextFilename("testFSDirG"));
        VFileSystem fs = getRemoteTestDir().getFileSystem();

        VFSPath newDir = createDir(fs, fullPath, true);
        Assert.assertEquals("Directory should use complete pathname as new directory name", fullPath.getPath(), newDir.getVRL()
                .getPath());
        newDir.delete();

    }

    private VFSPath createDir(VFileSystem fs, VRL dirpath, boolean ignore) throws Exception {
        VFSPath dir = fs.resolve(dirpath);
        dir.mkdir(ignore);
        return dir;
    }

    // not all implementations can handle spaces
    @Test
    public void testCreateAndDeleteDirWithSpace() throws Exception {
        if (this.getTestEncodedPaths() == false) {
            message("***Warning: Skipping test:testCreateAndDeleteDirWithSpace");
            return;
        }

        VFSPath newDir = createRemoteDir("test Dir F", false);
        Assert.assertTrue("New create directory must exist!", newDir.exists());
        newDir.delete();
        Assert.assertFalse("After deletion, directory must not  exist!", newDir.exists());
    }

    @Test
    public void testCreateAndIgnoreExistingDir() throws Exception {
        String dirname = "testDirG";

        VFSPath newDir = createRemoteDir(dirname, false);
        Assert.assertTrue("New create directory must exist!", newDir.exists());

        try {
            // current default implemenation is to ignore existing directories!
            VFSPath newDir2 = createRemoteDir(dirname, true);
        } catch (Exception e) {
            Assert.fail("Caught Exception: When setting ignoreExisting==true. Method createDir() must ignore the already existing directory");
        }

        newDir.delete();
    }

    @Test
    public void testCreateAndRenameDir() throws Exception {

        // must create
        String newDirName = "renamedTestDirG";

        if (getTestRenames() == true) {
            VFSPath testDir = createRemoteDir("testDirH", false);
            VFSPath parentDir = getRemoteTestDir();

            VRL newVRL = parentDir.resolveVRL(newDirName);
            VFSPath renamedDir = testDir.renameTo(newDirName);

            Assert.assertNotNull("New VFSPath may not be null.", renamedDir);
            Assert.assertEquals("VRL of renamed path must match expected.", newVRL, renamedDir.getVRL());
            Assert.assertTrue("New VFSPath doesn't exist:" + newDirName, renamedDir.exists());
            Assert.assertTrue("New VFSPath path must be a directory:" + newDirName, renamedDir.isDir());
            // re open/refetch.
            VFSPath targetDir = getRemoteTestDir().resolve(newDirName);
            Assert.assertNotNull("After rename, new VFSPath is NULL:" + newDirName, targetDir);
            Assert.assertTrue("New VFSPath doesn't exist:" + newDirName, targetDir.exists());

            // cleanup:
            renamedDir.delete();
        }

    }

    @Test
    public void testCreateAndRenameFile() throws Exception {
        if (getTestRenames() == false) {
            message("Skipping rename test");
            return;
        }

        String newFileName = "newFileName6";

        VFSPath file = createRemoteFile(nextFilename("testFileH2"), false);

        {
            VRL newVRL = file.getVRL().getParent().resolvePath(newFileName);
            VFSPath newPath = file.renameTo(newFileName);
            Assert.assertNotNull("New VFSPath may not be null.", newPath);
            Assert.assertEquals("VRL of renamed path must match expected.", newVRL, newPath.getVRL());
            Assert.assertTrue("New VFSPath doesn't exist:" + newFileName, newPath.exists());
            Assert.assertTrue("New VFSPath path must be a file:" + newFileName, newPath.isFile());
            Assert.assertFalse("Old VFSPath may not exist anymore:" + newFileName, file.exists());
            // fetch
            VFSPath renamedFile = getRemoteTestDir().resolve(newFileName);
            Assert.assertNotNull("new VFSPath is NULL", renamedFile);
            Assert.assertTrue("New VFSPath doesn't exist:" + renamedFile, renamedFile.exists());
            Assert.assertTrue("New VFSPath path must be a file:" + renamedFile, renamedFile.isFile());
            // cleanup:
            renamedFile.delete();
        }

    }

    @Test
    public void testRenameWithSpaces() throws Exception {
        if (getTestRenames() == false) {
            message("Skipping rename test");
            return;
        }

        if (getTestStrangeCharsInPaths() == false) {
            message("Skipping rename with spaces test");
            return;
        }

        VFSPath parentDir = getRemoteTestDir();

        // postfix space

        String orgFileName = nextFilename("spaceRenameFile1");
        String newFileName = orgFileName + " spaces";

        testRename(parentDir, orgFileName, newFileName);

        // prefix space
        orgFileName = nextFilename("spaceRenameFile2");
        newFileName = "pre " + orgFileName + " post";

        // infix space
        testRename(parentDir, orgFileName, newFileName);
        orgFileName = "infixSpaceRenameTest000";
        newFileName = "infixSpace RenameTest000";

        testRename(parentDir, orgFileName, newFileName);

    }

    // ========================================================================
    // Test Rename
    // ========================================================================

    private void testRename(VFSPath parentDir, String orgFileName, String newFileName) throws Exception {
        VFSPath orgFile = parentDir.resolve(orgFileName);
        Assert.assertFalse("Previous file still exists:'" + orgFileName + "'", orgFile.exists());

        orgFile.createFile(false);
        Assert.assertTrue("Failed to creat test file:'" + orgFileName + "'", orgFile.exists());

        message("Rename: '" + orgFileName + "' to '" + newFileName + "'");
        message("Rename:  - orgfile='" + orgFile + "'");

        boolean result = existsFile(getRemoteTestDir(), newFileName);
        Assert.assertFalse("Remote file system claims new file already exists!:'" + newFileName + "'", result);

        VFSPath newPath = orgFile.renameTo(newFileName);
        Assert.assertNotNull("Rename should return new Path!", newPath);
        Assert.assertTrue("Afger rename new Path must exist!", newPath.exists());

        VFSPath targetFile = parentDir.resolve(newFileName);
        Assert.assertTrue("VFSPath.exists(): new File should exist:" + targetFile, targetFile.exists());
        Assert.assertEquals("Returned path after rename must match resolve path!", targetFile.getVRL(), newPath.getVRL());

        result = existsFile(getRemoteTestDir(), newFileName);
        Assert.assertTrue("VFSPath.existsFile(): new File should exist:" + newFileName, result);

        VFSPath renamedFile = getRemoteTestDir().resolve(newFileName);
        message("Rename:  - renamefile='" + renamedFile + "'");

        Assert.assertNotNull("openVFSPath() of renamed file returned NULL", renamedFile);
        Assert.assertTrue("New file must exist after rename!", renamedFile.exists());

        // rename back
        VFSPath rerenamedPath = renamedFile.renameTo(orgFileName);
        Assert.assertNotNull("openVFSPath() of renamed file returned NULL", rerenamedPath);
        Assert.assertEquals("Rename should return true", true, result);
        VFSPath rerenamedFile = getRemoteTestDir().resolve(orgFileName);
        Assert.assertNotNull("openVFSPath() of reerenamed file return NULL", rerenamedFile);
        Assert.assertTrue("Original file must exist again after double rename!", rerenamedFile.exists());

        rerenamedFile.delete();
    }

    // =======================================================================
    // Contents/ReadWrite
    // =======================================================================

    @Test
    public void testSetGetSimpleContentsNewFile() throws Exception {
        if (getTestWriteTests() == false)
            return;

        // test1: small string
        VFileSystem fs = getRemoteTestDir().getFileSystem();
        VFSPath newFile = getRemoteTestDir().resolve("testFile7");

        writeContents(newFile, TEST_CONTENTS);
        long newLen = newFile.fileLength();
        Assert.assertFalse("After setting contents, size may NOT be zero", newLen == 0);

        String str = readContentsAsString(newFile);
        Assert.assertEquals("Contents of file (small string) not the same:" + str, str, TEST_CONTENTS);

        newFile.delete();

        // recreate file:
        newFile = getRemoteTestDir().resolve("testFile7a");
        // test2: big string
        if (getTestDoBigTests() == false)
            return;

        int len = 1024 * 1024 + 1024;

        char chars[] = new char[len];

        for (int i = 0; i < len; i++) {
            chars[i] = (char) ('A' + (i % 26));
        }

        String bigString = new String(chars);

        writeContents(newFile, bigString);
        str = readContentsAsString(newFile);

        if (str.compareTo(bigString) != 0) {
            String infoStr = "strlen=" + bigString.length() + ",newstrlen=" + str.length();

            Assert.fail("Contents of file (big string) not the same, but small string does!.\n" + "info=" + infoStr);
        }

        newFile.delete();
    }

    @Test
    public void testSetGetSimpleContentsExistingFile() throws Exception {
        if (getTestWriteTests() == false)
            return;

        // create existing file first:
        VFSPath newFile = createRemoteFile("testFile7b", false);
        writeContents(newFile, TEST_CONTENTS);

        long newLen = newFile.fileLength();
        Assert.assertFalse("After setting contents, size may NOT be zero", newLen == 0);

        String str = readContentsAsString(newFile);
        Assert.assertEquals("Contents of file (small string) not the same:" + str, str, TEST_CONTENTS);

        newFile.delete();

        // recreate file:
        newFile = createRemoteFile("testFile7c", false);

        // test2: big string
        if (getTestDoBigTests() == false)
            return;

        int len = 1024 * 1024 + 1024;

        char chars[] = new char[len];

        for (int i = 0; i < len; i++) {
            chars[i] = (char) ('A' + (i % 26));
        }

        String bigString = new String(chars);

        writeContents(newFile, bigString);
        str = readContentsAsString(newFile);

        if (str.compareTo(bigString) != 0) {
            String infoStr = "strlen=" + bigString.length() + ",newstrlen=" + str.length();

            Assert.fail("Contents of file (big string) not the same, but small string does!.\n" + "info=" + infoStr);
        }

        newFile.delete();
    }

    public void testCopyMoveToRemote(boolean isMove) throws Exception {
        VFSPath localFile = null;
        VFSPath remoteFile = null;

        // create object, do not create actual file
        localFile = localTempDir.resolve("testLocalFile");
        writeContents(localFile, TEST_CONTENTS);

        if (isMove) {
            remoteFile = getVFS().moveFileToDir(localFile, getRemoteTestDir());
        } else {
            remoteFile = getVFS().copyFileToDir(localFile, getRemoteTestDir());
        }

        Assert.assertNotNull("new remote File is NULL", remoteFile);

        String str = readContentsAsString(remoteFile);
        Assert.assertEquals("Contents of remote file not the same:" + str, str, TEST_CONTENTS);

        // file should be moved: local file may not exist.
        if (isMove == true)
            Assert.assertEquals("local file should not exist:" + localFile, false, localFile.exists());

        if (isMove == false)
            localFile.delete();

        remoteFile.delete();
    }

    @Test
    public void testCopy10MBForthAndBack() throws Exception {
        if (getTestDoBigTests() == false)
            return;

        testCopyForthAndBack(10 * 1024 * 1024, false);
        testCopyForthAndBack(10 * 1024 * 1024, true);

    }

    public void testCopyForthAndBack(int size, boolean isMove) throws Exception {
        VFSPath localFile = null;
        VFSPath remoteFile = null;

        {
            localFile = localTempDir.resolve(nextFilename("test10MBmove"));

            // int len = 10 * 1024 * 1024;

            // create random file: fixed seed for reproducable tests
            Random generator = new Random(13);
            byte buffer[] = new byte[size];
            generator.nextBytes(buffer);
            verbose(1, "streamWriting to localfile:" + localFile);
            streamWrite(localFile, buffer, 0, buffer.length);

            // move to remote (and do same basic asserts).
            long start_time = System.currentTimeMillis();
            verbose(1, "moving localfile to:" + getRemoteTestDir());

            if (isMove) {
                remoteFile = getVFS().moveFileToDir(localFile, getRemoteTestDir());
            } else {
                remoteFile = getVFS().copyFileToDir(localFile, getRemoteTestDir());
            }

            long total_millis = System.currentTimeMillis() - start_time;
            double up_speed = (size / 1024.0) / (total_millis / 1000.0);
            verbose(1, "upload speed=" + ((int) (up_speed * 1000)) / 1000.0 + "KB/s");

            verbose(1, "new remote file=" + remoteFile);

            Assert.assertNotNull("new remote File is NULL", remoteFile);
            Assert.assertTrue("after move to remote testdir, remote file doesn't exist:" + remoteFile, remoteFile.exists());
            if (isMove) {
                Assert.assertFalse("local file reports it still exists, after it has moved", localFile.exists());
            }

            // move back to local with new name (and do same basic asserts).
            start_time = System.currentTimeMillis();

            VFSPath localTargetFile = localTempDir.resolve("test10MBback");

            VFSPath newLocalFile;

            if (isMove) {
                newLocalFile = getVFS().moveFileToFile(remoteFile, localTargetFile);
            } else {
                newLocalFile = getVFS().copyFileToFile(remoteFile, localTargetFile);
            }

            Assert.assertNotNull("new local File is NULL", newLocalFile);
            if (isMove) {
                Assert.assertFalse("remote file reports it still exists, after it has moved", remoteFile.exists());
            }
            total_millis = System.currentTimeMillis() - start_time;

            double down_speed = (size / 1024.0) / (total_millis / 1000.0);
            verbose(1, "download speed=" + ((int) (down_speed * 1000)) / 1000.0 + "KB/s");

            // check contents:

            byte newcontents[] = readContents(newLocalFile);
            int newlen = newcontents.length;
            // check size:
            Assert.assertEquals("size of new contents does not match.", size, newlen);

            // compare contents
            for (int i = 0; i < size; i++) {
                if (buffer[i] != newcontents[i]) {
                    Assert.assertEquals("Contents of file not the same. Byte nr=" + i, buffer[i], newcontents[i]);
                }
            }

            // cleanup
            newLocalFile.delete();

            if (isMove == false) {
                localFile.delete();
                remoteFile.delete();
            }
        }
    }

    // =======================================================================
    // Stream Read/Write
    // =======================================================================

    /**
     * Create local file, move it to remote and streamRead it.
     */
    @Test
    public void testStreamRead() throws Exception {
        if (getTestDoBigTests() == false)
            return;

        VFSPath localFile = null;
        VFSPath remoteFile = null;

        {
            localFile = localTempDir.resolve("test10MBStreamRead");

            int len = 1313 * 1024;

            // fixed seed for reproducable tests
            Random generator = new Random(13);

            byte buffer[] = new byte[len];
            generator.nextBytes(buffer);
            verbose(1, "Creating local file");
            streamWrite(localFile, buffer, 0, buffer.length);

            // move to remote (and do same basic asserts).
            verbose(1, "Moving file to:" + getRemoteTestDir());
            remoteFile = getVFS().moveFileToDir(localFile, getRemoteTestDir());
            Assert.assertNotNull("new remote File is NULL", remoteFile);
            Assert.assertFalse("local file reports it still exists, after it has moved", localFile.exists());

            verbose(1, "Allocing new buffer, len=" + len);
            byte newcontents[] = new byte[len];

            verbose(1, "Getting inputstream of:" + remoteFile);
            InputStream inps = getVFS().createInputStream(remoteFile);

            int nrread = 0;
            int totalread = 0;
            int prevp = 0;

            long read_start_time = System.currentTimeMillis();
            verbose(1, "Starting read loop");
            // read loop:
            while (totalread < len) {
                nrread = inps.read(newcontents, totalread, len - totalread);
                totalread += nrread;
                if (nrread < -1) {
                    verbose(1, "Error nread<0)");
                    break;
                }

                int perc = 100 * totalread / (len + 1);
                if ((perc / 10) > prevp) {
                    verbose(1, "nread=" + perc + "%");
                    prevp = perc / 10;
                }
            }

            long total_read_millis = System.currentTimeMillis() - read_start_time;
            double speed = (len / 1024.0) / (total_read_millis / 1000.0);

            verbose(1, "read speed=" + ((int) (speed * 1000)) / 1000.0 + "KB/s");

            // check size:
            Assert.assertEquals("number of read bytes does not match file length", len, totalread);

            // compare contents
            for (int i = 0; i < len; i++) {
                if (buffer[i] != newcontents[i])
                    Assert.assertEquals("Contents of file not the same. Byte nr=" + i, buffer[i], newcontents[i]);
            }

            deleteLater(remoteFile);
        }
    }

    /**
     * Create remote file, write to it, and move it back to here to check contents.
     */
    @Test
    public void testStreamWrite() throws Exception {
        if (getTestDoBigTests() == false)
            return;

        VFSPath localFile = null;
        VFSPath remoteFile = null;

        {
            remoteFile = createRemoteFile(nextFilename("test10MBstreamWrite"), false);

            int len = 10 * 1024 * 1024;

            // fixed seed for reproducable tests
            Random generator = new Random(13);

            byte buffer[] = new byte[len];
            generator.nextBytes(buffer);

            long read_start_time = System.currentTimeMillis();

            // use streamWrite for now:
            verbose(1, "streadWriting to:" + remoteFile);
            streamWrite(remoteFile, buffer, 0, buffer.length);
            long total_read_millis = System.currentTimeMillis() - read_start_time;
            double speed = (len / 1024.0) / (total_read_millis / 1000.0);
            verbose(1, "write speed=" + ((int) (speed * 1000)) / 1000.0 + "KB/s");

            // move to remote (and do same basic asserts).
            verbose(1, "moving to local dir:" + localTempDir);
            localFile = getVFS().moveFileToDir(remoteFile, localTempDir);
            Assert.assertNotNull("new remote File is NULL", localFile);
            Assert.assertFalse("remote file reports it still exists, after it has moved", remoteFile.exists());

            // get contents of localfile:
            byte newcontents[] = readContents(localFile);
            int newlen = newcontents.length;

            // check size:
            Assert.assertEquals("number of read bytes does not match file length", len, newlen);

            // compare contents
            for (int i = 0; i < len; i++) {
                if (buffer[i] != newcontents[i])
                    Assert.assertEquals("Contents of file not the same. Byte nr=" + i, buffer[i], newcontents[i]);
            }

            localFile.delete();
        }
    }

    /**
     * Regression test for some stream write implementations: When writing to an existing file, the existing file must be
     * truncated and the content of the file must be disregarded. File rewriting and appending must be done by using the
     * VStreamAppendable interface or the RandomAccess interface.
     *
     * @throws Exception
     */
    @Test
    public void testStreamWriteMustTruncateFile() throws Exception {
        VFSPath remoteFile = null;
        remoteFile = createRemoteFile(nextFilename("testStreamWriteMustTruncateFile"), false);

        int originalLength = 10 * 1024; // 10k;

        // fixed seed for reproducable tests!
        Random generator = new Random(45);

        byte buffer[] = new byte[originalLength];
        generator.nextBytes(buffer);
        // use streamWrite for now:
        verbose(1, "streadWriting to:" + remoteFile);
        streamWrite(remoteFile, buffer, 0, buffer.length);
        Assert.assertEquals("Initial remote file size NOT correct", remoteFile.fileLength(), originalLength);

        // reduce size!
        int newLength = 5 * 1024; // new size less then 10k!
        buffer = new byte[newLength];
        generator.nextBytes(buffer);

        // Stream Write:
        {
            OutputStream outps = getVFS().createOutputStream(remoteFile, false);
            outps.write(buffer, 0, buffer.length);
            outps.flush();
            outps.close();
        }

        // imporant: sync with filesystem and update meta-data!
        Assert.assertTrue("sync() must be supported to really test the fileLength change.", remoteFile.sync());

        debugPrintf("testStreamWriteMustTruncateFile(), after write new length=%d\n", remoteFile.fileLength());
        Assert.assertEquals("File length must match new (smaller) size.", newLength, remoteFile.fileLength());
        remoteFile.delete();
    }

    /**
     * Regression test whether file lengths always match the nr of written bytes.
     *
     * @throws Exception
     */
    @Test
    public void testMultiStreamWritesCheckLengths() throws Exception {
        int numTries = 50;
        int maxSize = 1000;

        // Use own random generator but use fixed seed for deterministic
        // testing!
        Random generator = new Random(123123);
        VFSPath remoteFile = null;

        byte buffer[] = new byte[maxSize];
        for (int i = 0; i < numTries; i++) {
            int testSize = generator.nextInt(maxSize);
            remoteFile = createRemoteFile("testStreamWriteTest-" + i, false);

            generator.nextBytes(buffer);

            // use streamWrite for now:
            verbose(1, "streamWriting #" + testSize + " bytes to:" + remoteFile);

            streamWrite(remoteFile, buffer, 0, testSize);

            Assert.assertEquals("LFC File size NOT correct", remoteFile.fileLength(), testSize);

            if (remoteFile instanceof VReplicatable) {
                VRL reps[] = ((VReplicatable) remoteFile).getReplicas();
                if ((reps == null) || (reps.length <= 0)) {
                    Assert.fail("No replicas for Replicatable File!");
                }

                VFSPath rep = remoteFile.getFileSystem().resolve(reps[0]);
                long repLen = rep.fileLength();
                verbose(1, "streamWriting #" + testSize + " replica (@" + rep.getVRL().getHostname() + ") length=" + repLen);
                Assert.assertEquals("LFC Replica File size NOT correct", repLen, testSize);
            }

            remoteFile.delete();
        }
    }

    @Test
    public void testCopyToRemote() throws Exception {
        testCopyMoveToRemote(false);
    }

    @Test
    public void testMoveToRemote() throws Exception {
        testCopyMoveToRemote(true);
    }

    public void testCopyMoveToLocal(boolean isMove) throws Exception {
        VFSPath localFile = null;
        VFSPath remoteFile = null;

        remoteFile = createRemoteFile("testLocalFile", false);
        writeContents(remoteFile, TEST_CONTENTS);

        if (isMove) {
            localFile = getVFS().moveFileToDir(remoteFile, localTempDir);
        } else {
            localFile = getVFS().copyFileToDir(remoteFile, localTempDir);
        }

        Assert.assertNotNull("new remote File is NULL", localFile);

        String str = this.readContentsAsString(localFile);
        Assert.assertEquals("Contents of local file not the same:" + str, str, TEST_CONTENTS);

        // file should be moved: remote file may not exist.
        if (isMove == true)
            Assert.assertEquals("Remote file should not exist after move:" + remoteFile, false, remoteFile.exists());

        if (isMove == false)
            remoteFile.delete();

        localFile.delete();
    }

    @Test
    public void testCopyToLocal() throws Exception {
        testCopyMoveToLocal(false);
    }

    @Test
    public void testMoveToLocal() throws Exception {
        testCopyMoveToLocal(true);
    }

    /**
     * Test readRandomBytes first before testing random reads+writes
     */
    @Test
    public void testRandomReadable() throws Exception {
        VFSPath localFile = this.localTempDir.resolve("readBytesFile1");
        int len = 16;
        byte orgBuffer[] = new byte[len];

        for (int i = 0; i < len; i++)
            orgBuffer[i] = (byte) (i);

        writeContents(localFile, orgBuffer);
        VFSPath rfile = getVFS().moveFileToDir(localFile, getRemoteTestDir());
        if ((rfile instanceof RandomReadable) == false) {
            message("===");
            message("Warning: Skipping readRandomBytes for file:" + rfile);
            message("===");

            return;
        }

        RandomReadable vrfile = (RandomReadable) rfile;

        _testRandomReadable(vrfile, 4, 8, orgBuffer);
        _testRandomReadable(vrfile, 8, 16, orgBuffer);
        _testRandomReadable(vrfile, 0, 1, orgBuffer);
        _testRandomReadable(vrfile, 1, 2, orgBuffer);
        _testRandomReadable(vrfile, 0, 5, orgBuffer);

        rfile.delete();

        //
        // 1MB size:
        //

        localFile = this.localTempDir.resolve("readBytesFile2");
        len = 1024 * 1024;
        orgBuffer = new byte[len];

        for (int i = 0; i < len; i++)
            orgBuffer[i] = (byte) ((13 + len - i) % 256);

        writeContents(localFile, orgBuffer);
        rfile = getVFS().moveFileToDir(localFile, getRemoteTestDir());
        vrfile = (RandomReadable) rfile;

        _testRandomReadable(vrfile, 4, 8, orgBuffer);
        _testRandomReadable(vrfile, 1, 2, orgBuffer);
        _testRandomReadable(vrfile, 0, 1, orgBuffer);
        _testRandomReadable(vrfile, 100001, 200002, orgBuffer);
        _testRandomReadable(vrfile, 200002, 222222, orgBuffer);
        _testRandomReadable(vrfile, 330002, 330256, orgBuffer);
        _testRandomReadable(vrfile, len - 1025, len, orgBuffer);

        rfile.delete();
    }

    private void _testRandomReadable(RandomReadable rfile, int offset, int end, byte orgBuffer[]) throws Exception {
        byte readBuffer[] = new byte[orgBuffer.length];

        int numread = rfile.readBytes(offset, readBuffer, offset, end - offset);

        Assert.assertEquals("Couldn't read requested nr of bytes. numread=" + numread, end - offset, numread);

        for (int i = offset; i < end; i++) {
            if (orgBuffer[i] != readBuffer[i])
                Assert.assertEquals("Contents of file not the same. Byte nr=" + i, orgBuffer[i], readBuffer[i]);
        }
    }

    // @Test
    // public void testVRandomReader() throws Exception
    // {
    // VFSPath remoteFile = createRemoteFile("RandomReaderFile1");
    //
    // // VFSPath localFile = this.localTempDir.createFile("RandomReaderFile");
    //
    // // mandatory ?
    // if ((remoteFile instanceof VRandomReadable)==false)
    // return;
    //
    // StringBuffer contents = new StringBuffer();
    // for (int i = 0; i < 10; i++)
    // {
    // contents.append(i);
    // }
    // writeContents(remoteFile,contents.toString());
    //
    // //RandomReader instance = new RandomReader((VRandomReadable) remoteFile);
    //
    // //-----------Test length
    // Assert.assertEquals(instance.length(), remoteFile.getLength());
    // Assert.assertEquals(instance.length(), contents.length());
    //
    //
    // //-----------Test Seek
    // // read the first byte (0)
    // int start = 0;
    // int end = start+1;
    // instance.seek(start);
    // byte[] signatureBytes = new byte[contents.substring(start,end).getBytes().length];
    // instance.readFully(signatureBytes);
    // Assert.assertEquals(contents.substring(start, end), new String(signatureBytes));
    //
    // //read the last two byte (9)
    // start = instance.length()-2;
    // end = start+1;
    // instance.seek(start);
    // signatureBytes = new byte[contents.substring(start,end).getBytes().length];
    // instance.readFully(signatureBytes);
    // Assert.assertEquals(contents.substring(start, end), new String(signatureBytes));
    //
    //
    // // read byte in the midle(5)
    // start = instance.length()/2;
    // end = start+1;
    // instance.seek(start);
    // signatureBytes = new byte[contents.substring(start,end).getBytes().length];
    // instance.readFully(signatureBytes);
    // Assert.assertEquals(contents.substring(start, end), new String(signatureBytes));
    //
    // //-----------Test readBytes
    // signatureBytes = new byte[1];
    // instance.readBytes(0, signatureBytes, 0, signatureBytes.length);
    // Assert.assertEquals(contents.substring(0, signatureBytes.length), new String(signatureBytes));
    //
    // //-----------Test skip
    // //bring back to the start
    // instance.seek(0);
    // //skip 3 bytes
    // int lenToSkip = 3;
    // int skiped = instance.skipBytes(lenToSkip);
    // signatureBytes = new byte[1];
    // instance.readFully(signatureBytes);
    // Assert.assertEquals(lenToSkip, skiped);
    // Assert.assertEquals(contents.substring(lenToSkip,lenToSkip+1), new String(signatureBytes));
    // }

    /**
     * Writes 1,11,64k+ and 1MB+ number of bytes and reread them using VFSPath.read() and VFSPath.write() Note: read() and write()
     * are high level method which use readBytes() and writeBytes() or streamRead and streamWrite. The implementation chooses the
     * read/write method.
     */
    @Test
    public void testReadWriteBytes() throws Exception {
        if (getTestWriteTests() == false)
            return;

        VFSPath newFile = createRemoteFile("someBytes", false);

        // write single byte:

        byte buffer[] = new byte[] { 13 };
        testReadWriteBytes(newFile, 0, buffer);

        // write array of bytes;

        buffer = new byte[] { 42, 13, 1, 2, 3, 4, 5, 6, 7, 8, 0, 9 };
        testReadWriteBytes(newFile, 0, buffer);

        // >64k block
        int len = 65537;
        buffer = new byte[len];

        for (int i = 0; i < len; i++) {
            buffer[i] = (byte) ((13 + i) % 256);
        }

        testReadWriteBytes(newFile, 0, buffer);

        // >1MB block
        len = 1024 * 1024 + 13;
        buffer = new byte[len];

        for (int i = 0; i < len; i++) {
            buffer[i] = (byte) ((11 + i * 13 + i) % 256);
        }

        testReadWriteBytes(newFile, 0, buffer);
        newFile.delete();
    }

    /**
     * Writes 1,11,64k+ and 1MB+ number of bytes useing writeBytes() and readBytes();
     *
     * @throws Exception
     *
     */
    @Test
    public void testReadWriteRandomBytes() throws Exception {
        if (getTestWriteTests() == false)
            return;

        VFSPath newFile = createRemoteFile(nextFilename("testReadWriteRandomBytes1"), false);
        RandomWritable randomWriter = null;
        RandomReadable randomReader = null;

        if (newFile instanceof RandomWritable) {
            randomWriter = (RandomWritable) newFile;
        } else {
            message("File implementation doesn't support random write methods:" + this);
            return;
        }

        if (newFile instanceof RandomReadable) {
            randomReader = (RandomReadable) newFile;
        } else {
            message("File implementation doesn't support random write methods:" + this);
            return;
        }

        // write small (#12) array of bytes;
        // include NEGATIVE number to test negative to postive
        byte buffer1[] = new byte[] { 127, 42, 13, 1, 2, 3, 4, 5, 6, 7, 8, 0, 9, -1, -10, -126, -127, -128 };
        int nrBytes = buffer1.length;

        randomWriter.writeBytes(4, buffer1, 4, 4);
        randomWriter.writeBytes(8, buffer1, 8, 4);
        randomWriter.writeBytes(0, buffer1, 0, 4);
        // write remainder:
        randomWriter.writeBytes(12, buffer1, 12, nrBytes - 12);

        byte buffer2[] = new byte[nrBytes];

        // use syncReadBytes!
        int numRead = IOUtil.readAll(randomReader, 0, buffer2, 0, nrBytes);
        Assert.assertEquals("Number of actual read bytes is wrong!", nrBytes, numRead);

        for (int i = 0; i < nrBytes; i++) {
            if (buffer1[i] != buffer2[i]) {
                message("Read error. Buffer dump: ");

                for (int j = 0; j < nrBytes; j++)
                    message("Buffer[" + j + "] 1,2 =" + buffer1[j] + "," + buffer2[j]);

                Assert.assertEquals("Contents of file not the same. Byte nr=" + i, buffer1[i], buffer2[i]);
            }
        }

        newFile.delete();
        newFile = createRemoteFile("testReadWriteRandomBytes2", false);

        // use Pseudo random: make test reproducable !

        Random generator = new Random(0);

        int maxlen = 1024 * 1024 + 13;

        buffer1 = new byte[maxlen];
        buffer2 = new byte[maxlen];

        // write between 1-10% per write
        int minwrite = maxlen / 100;
        int maxwrite = maxlen / 10;

        // figure out average nr of writes needed to fill the file
        // (file doesn't need to be fully full)
        int nrWrites = maxlen / ((maxwrite - minwrite) / 2);

        if (nrWrites <= 0) {
            verbose(1, "nrwrites <=0");
        }

        for (int i = 0; i < nrWrites; i++) {
            int partlen = minwrite + generator.nextInt(maxwrite - minwrite);
            int offset = generator.nextInt(maxlen);

            // must not write over end of file:
            if (offset + partlen > maxlen)
                partlen = maxlen - offset;

            // generate random bytes:
            byte part[] = new byte[partlen];
            generator.nextBytes(part);

            // write bytes:
            randomWriter.writeBytes(offset, part, 0, partlen);

            // keep byte in buffer:
            for (int j = 0; j < partlen; j++)
                buffer1[offset + j] = part[j];

            verbose(1, "writeRandom:writes " + i + " of " + nrWrites);

        }

        // reread file contents: Use Sync READ !
        numRead = IOUtil.readAll(randomReader, 0, buffer2, 0, maxlen);
        Assert.assertEquals("Number of actual read bytes is wrong!", maxlen, numRead);

        // check readbuffer;

        for (int i = 0; i < maxlen; i++) {
            if (buffer1[i] != buffer2[i])
                Assert.assertEquals("Contents of file not the same. Byte nr=" + i, buffer1[i], buffer2[i]);
        }

        newFile.delete();
    }

    private void testReadWriteBytes(VFSPath newFile, long offset, byte[] buffer1) throws Exception {
        RandomWritable randomWriter = null;
        RandomReadable randomReader = null;

        if (newFile instanceof RandomWritable) {
            randomWriter = (RandomWritable) newFile;
        } else {
            message("File implementation doesn't support random write methods:" + this);
            return;
        }

        if (newFile instanceof RandomReadable) {
            randomReader = (RandomReadable) newFile;
        } else {
            message("File implementation doesn't support random write methods:" + this);
            return;
        }

        byte buffer2[] = new byte[buffer1.length];

        int numBytes = buffer1.length;

        randomWriter.writeBytes(offset, buffer1, 0, numBytes);
        // newFile.sync(); => writeBytes is specified as synchronous !
        int numRead = randomReader.readBytes(offset, buffer2, 0, numBytes);

        Assert.assertEquals("Actual number of read bytes doesn't match request number of bytes!", numBytes, numRead);

        // check readbuffer;

        for (int i = 0; i < numBytes; i++) {
            if (buffer1[i] != buffer2[i])
                Assert.assertEquals("Contents of file not the same. Byte nr=" + i + " of " + numBytes, buffer1[i], buffer2[i]);
        }
    }

    /**
     * Regression test to test wether a write and a read of a single unsigned byte value > 128 will result in an integer value >
     * 128. SRB had a bug where a single (byte) read of a value > 128 resulted in a negative integer value which is interpreted as
     * a EOF (-1).
     *
     * @throws Exception
     */
    @Test
    public void testStreamReadWriteSingleBytes() throws Exception {
        VFSPath remoteFile = null;

        // negative values should be auto-casted to their positive (usigned)
        // byte equivalents like in a cast
        // 0=0,1=0,127=127 AND -1 = 255, -2=254, etc,until -128=128 !
        // Also: for negative values <-128 the lowest significant byte is used
        // which
        // is a positive byte
        int values[] = { 0, 1, 127, 128, 255, -1, -2, -126, -127, -128, -129, -130 };
        // positive (usigned byte) values of above integers !
        int bytevals[] = { 0, 1, 127, 128, 255, 255, 254, 130, 129, 128, 127, 126 };

        {
            remoteFile = this.createRemoteFile("single_byte", true);
            VStreamReadable reader = (VStreamReadable) remoteFile;
            VStreamWritable writer = (VStreamWritable) remoteFile;

            OutputStream outps = writer.createOutputStream(false);

            for (int value : values) {
                outps.write(value);
            }
            outps.close();

            InputStream inps = reader.createInputStream();

            for (int i = 0; i < bytevals.length; i++) {
                int val = inps.read();

                Assert.assertEquals("single byte read()+write() does not return expected value,", bytevals[i], val);
            }

            inps.close();
        }
    }

    // =======================================================================
    // Directory Tests
    // =======================================================================

    /**
     * Create empty dir, copy it and delete both. This is a regression test for SRM.
     *
     * @throws Exception
     */
    @Test
    public void testCopyEmptyDir() throws Exception {
        VFSPath sourceDir = getRemoteTestDir().resolve(nextFilename("testFSDirEmpty_sourceDir"));
        sourceDir.mkdir(true);
        Assert.assertTrue("Source directory must exists:" + sourceDir, sourceDir.exists());

        String targetSubDir = "testFSDirEmpty_targetDir";

        VFSPath parentDir = sourceDir.getParent();
        VFSPath expectedPath = parentDir.resolve(targetSubDir);

        // test dir copy with optional subdir renaming:
        VFSPath newDir = getVFS().copyDirToDir(sourceDir, parentDir, targetSubDir);
        Assert.assertEquals("New created directory path must match expectedPath", expectedPath.getVRL(), newDir.getVRL());
        Assert.assertTrue("Remote directory must exist:" + newDir, newDir.exists());

        // cleanup:
        sourceDir.delete();
        newDir.delete();
    }

    // not now:
    /*
     * public void testMultiThreadedRead() {
     *
     * }
     */

    @Test
    public void testCopyDirToRemote() throws Exception {
        String subdirName = "testCopyDirToRemoteDir";

        VFSPath localTestDir = localTempDir.resolve(nextFilename(subdirName));
        localTestDir.mkdir(false);

        int numFiles = 10;

        String fileNames[] = new String[numFiles];

        for (int i = 0; i < numFiles; i++) {
            fileNames[i] = "testFile" + i;
            VFSPath file = localTestDir.resolve(fileNames[i]);
            this.writeContents(file, TEST_CONTENTS);
        }

        if (existsDir(getRemoteTestDir(), localTestDir.getVRL().getBasename())) {
            message("*** Warning: removing already existing target directory in:" + getRemoteTestDir());
            // delete should already be test by now:
            getRemoteTestDir().resolve(localTestDir.getVRL().getBasename()).delete(true);
        }

        VFSPath newRemoteDir = getVFS().moveDirToDir(localTestDir, getRemoteTestDir(), subdirName);

        boolean result = existsDir(getRemoteTestDir(), subdirName);
        Assert.assertEquals("New remote directory doesn't exist (I):" + subdirName, true, result);

        result = newRemoteDir.exists();
        Assert.assertEquals("New remote directory doesn't exist (II):" + newRemoteDir, true, result);

        // check contents.
        for (int i = 0; i < numFiles; i++) {
            result = getVFS().existsFile(newRemoteDir.resolveVRL(fileNames[i]));
            Assert.assertEquals("Remote directory doesn't contain file:" + fileNames[i], true, result);
        }

        try {
            newRemoteDir.delete(true);
        } catch (Exception e) {
            message("Warning: after VFSPath.delete(): ignoring exception as not part of unit test :" + e);
        }
    }

    // @Test public void testSetLength() throws Exception
    // {
    // VFSPath file = createRemoteFile("testFileLength", true);
    //
    // Assert.assertEquals("New file must have zero length !", 0, file.getLength());
    //
    // if (file instanceof VResizable)
    // {
    // ((VResizable) file).setLengthToZero();
    // Assert.assertEquals("after setLengthToZero: New file must still have zero length !", 0, file.getLength());
    // }
    // else
    // {
    // message("Skipping (not supported:) VZeroSizable.setLengthToZero()");
    // }
    //
    // if (file instanceof VResizable)
    // {
    // ((VResizable) file).setLength(13);
    // Assert.assertEquals("setLentgg() didn't increaze file size to new length", 13, file.getLength());
    //
    // ((VResizable) file).setLength(3);
    // Assert.assertEquals("setLentgg() didn't dencreaze file size to new length", 3, file.getLength());
    //
    // ((VResizable) file).setLengthToZero();
    // Assert.assertEquals("setLentgg() didn't decreaze file size to zero", 0, file.getLength());
    //
    // int mil = 1024 * 1024;
    // ((VResizable) file).setLength(mil);
    // Assert.assertEquals("setLentgg() didn't increaze file size to new size", mil, file.getLength());
    // }
    // else
    // {
    // message("Skipping (not supported:) VSizeAdjustable.setLength()");
    // }
    // file.delete();
    // }

    private void _testStreamWrite(int targetSize) throws Exception {
        VFSPath file = createRemoteFile("streamWrite", true);
        // write 1MB buffer:
        byte[] buffer = new byte[targetSize];
        streamWrite(file, buffer, 0, buffer.length);

        long size = file.fileLength();
        Assert.assertEquals("testing write > 32k bug: Size of file after streamWrite not correct:" + size, size, targetSize);
        file.delete();
    }

    // ========================================================================
    // VFSAttribute
    // ========================================================================

    /**
     * For VFSPaths there is a minimum set of attributes which must be supported. Also the Attribute value must match teh value
     * returned by getters and setters.
     */
    @Test
    public void testFileAttributes() throws Exception {
        VFSPath newFile = getRemoteFile(nextFilename("testFileAttr"));

        // Non existant file:
        //testVFSPathAttributes(newFile, false, null);

        newFile.createFile(false);
        testVFSPathAttributes(newFile, true, "File");

        newFile.delete();
    }

    private Attribute getFileAttribute(VFSPath file, String name) throws VrsException {
        List<Attribute> attrs = file.getAttributes(new String[] { name });

        if ((attrs == null) || (attrs.size() <= 0)) {
            Assert.fail("Mandatory Attribute not returned:" + name);
        }

        if (attrs.size() > 1) {
            Assert.fail("Returned to many attributes for:" + name);
        }

        Attribute attr = attrs.get(0);
        Assert.assertEquals("Returned Attribute must have same name for:" + name, attr.getName(), name);

        return attr;
    }

    /**
     * Test Attribute interface with matching methods !
     */
    private void testVFSPathAttributes(VFSPath newFile, boolean exists, String expectedResourceType) throws Exception {

        Assert.assertEquals("Both getType() and getAttribute(ATTR_TYPE) must return same value", newFile.getResourceType(),
                getFileAttribute(newFile, ATTR_RESOURCE_TYPE).getStringValue());

        Assert.assertEquals("Both getName() and getAttribute(ATTR_NAME) must return same value", newFile.getName(),
                getFileAttribute(newFile, ATTR_NAME).getStringValue());

        Assert.assertEquals("Both getVRL() and getAttribute('location') must return same value", newFile.getVRL(),
                getFileAttribute(newFile, ATTR_LOCATION).getVRL());

        // for hostname comparisons:
        Assert.assertEquals("Both getHostname() and getAttribute(ATTR_HOSTNAME) must return same value", newFile.getVRL()
                .getHostname(), getFileAttribute(newFile, ATTR_HOSTNAME).getStringValue());

        // for hostname:port comparisons:
        Assert.assertEquals("Both getPort() and getAttribute(ATTR_PORT) must return same value", newFile.getVRL().getPort(),
                getFileAttribute(newFile, ATTR_PORT).getIntValue());

        // for scheme://hostname:port comparisons:
        Assert.assertEquals("Both getScheme() and getAttribute(ATTR_SCHEME) must return same value",
                newFile.getVRL().getScheme(), getFileAttribute(newFile, ATTR_SCHEME).getStringValue());

        // for scheme://hostname:port comparisons:
        Assert.assertEquals("Both getPath() and getAttribute(ATTR_PATH) must return same value", newFile.getVRL().getPath(),
                getFileAttribute(newFile, ATTR_PATH).getStringValue());

        Assert.assertEquals("Both getPath() and getAttribute(ATTR_PATH).VRL().getPath() must return same value", newFile.getVRL()
                .getPath(), getFileAttribute(newFile, ATTR_PATH).getVRL().getPath());

        Assert.assertEquals("Both getMimetype() and getAttribute(ATTR_MIMETYPE) must return same value", newFile.getMimeType(),
                getFileAttribute(newFile, ATTR_MIMETYPE).getStringValue());

        Assert.assertEquals("Both exists() and getAttribute(ATTR_EXISTS) must return same value", newFile.exists(),
                getFileAttribute(newFile, ATTR_RESOURCE_EXISTS).getBooleanValue());

        Assert.assertEquals("File should exists(), doesn't matchfor:" + newFile, exists, newFile.exists());

        if (newFile.exists()) {
            if (newFile.isFile()) {
                // File specified attributes:
                Assert.assertEquals("Both getLength() and getAttribute(ATTR_LENGTH) must return same value",
                        newFile.fileLength(), getFileAttribute(newFile, ATTR_FILE_SIZE).getLongValue());
            }

            Assert.assertEquals("Both getType() and getAttribute(ATTR_TYPE) must return same value", newFile.getResourceType(),
                    getFileAttribute(newFile, ATTR_RESOURCE_TYPE).getStringValue());

            Assert.assertEquals("When a VFSPath exist, the resource types must be equals.", newFile.getResourceType(),
                    expectedResourceType);
        }
    }

    // ========================================================================
    // Explicit Exception Tests for robust programming !
    // ========================================================================

    /*
     * Since the Copy/Move methods highly depend on the create/exists/and delete methods, do extra robustness tests to
     * check correct behaviour in incorrect situations.
     *
     * Scenarios which caused lot of trouble with Gftp and SRB:
     *
     * - Create file when file already exists (and ignoreExisting==false)<br> - Create dir when dir already exists (and
     * ignoreExisting==false) <br> - Create file when directory with same name already exists<br> - Create directory
     * when file with same name already exists<br>
     */

    /**
     * API test for atomic file creation.
     */
    // junit 4: @Test(expected=ResourceAlreadyExistsException.class)
    @Test
    public void testZExceptionCreateFileDoNotIgnoreExisting() throws Exception {
        VFSPath newFile = createRemoteFile("testFile1", false);

        // current implemenation is to ignore existing files
        // except when force=false

        try {
            newFile = createRemoteFile("testFile1", false);
            Assert.fail("Should raise at least an Exception ");
        } catch (Exception e) {
            ; // ok
        }

        newFile.delete();
    }

    @Test
    public void testCreateDirPermissionDenied() throws Exception {
        try {
            VFSPath root = getRemoteTestDir().resolve("/");

            // "/test" is illegal under linux
            if (GlobalProperties.isLinux() == true) {
                VFSPath newDir = root.resolve(nextFilename("testI"));
                newDir.mkdir(false);

                Assert.fail("Should raise Exception:" + ResourceAccessDeniedException.class);
                // ; continue
            }

            if (GlobalProperties.isWindows() == true) {
                // Need read only dir. Do these exists under windows ?
            }

        } catch (ResourceCreationException e) {
            debug("OK: Caught expected ResourceCreationException, but would prefer:" + ResourceAccessDeniedException.class);
        } catch (ResourceAccessDeniedException e) {
            debug("Excellent: Caught expected ResourceAccessDeniedException:" + e);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Caught Exception, but method should raise ResourceCreationException:");
        }
    }

    /**
     * API test for atomic directory creation.
     */
    @Test
    public void testZExceptionCreateDirNotIgnoreExisting() throws Exception {
        VFSPath newDir = createRemoteDir("testDir1", false);
        try {
            newDir = createRemoteDir("testDir1", false);
            Assert.fail("Should raise and Exception. Preferably:" + ResourceCreationException.class);
        }

        catch (Exception e) {
            debug("Caugh expected Exception:" + e);
            // Global.debugPrintStacktrace(e);
        }

        newDir.delete();
    }

    @Test
    public void testZCreateDirectoryWhileFileWithSameNameExists() throws Exception {
        String fdname = "testfiledir2";

        if (existsDir(getRemoteTestDir(), fdname)) {
            // previous test went wrong !!
            verbose(0, "*** Warning: Remote testfile already exists and is a directory");
            VFSPath dir = getRemoteTestDir().resolve(fdname);
            dir.delete(false);
            Assert.assertFalse("Could not remote previous test directory. Please remove it manually:" + dir, dir.exists());
            // fail("Remote testfile is already directory. Please remove previous test directory!");
        }

        VFSPath newfile = createRemoteFile("testfiledir2", false);

        // MUST return false!
        Assert.assertFalse("existsDir() must return FALSE when file with same name already exists!",
                getVFS().existsDir(getRemoteTestDir().resolveVRL("testfiledir2")));

        try {
            VFSPath newDir = createRemoteDir("testfiledir2", false);
            Assert.fail("Create directory out of existing file should raise Exception:");
        }
        // both are allowed:
        catch (ResourceAlreadyExistsException e) {
            debug("Caugh expected Exception:" + e);
            // Global.debugPrintStacktrace(e);
        }

        newfile.delete();
    }

    @Test
    public void testZCreateFileWhileDirectoryWithSameNameExists() throws Exception {
        VFSPath newDir = createRemoteDir("testfiledir3", false);

        // MUST return false!
        Assert.assertFalse("existsFile() must return FALSE when directory with same name already exists!",
                getVFS().existsFile(newDir.getVRL()));
        try {
            VFSPath newfile = createRemoteFile("testfiledir3", false);
            Assert.fail("Create file out of existing directory should raise Exception:");
        }
        // both are allowed:
        catch (ResourceCreationException e) {
            debug("Caugh expected Exception:" + e);
        }

        newDir.delete();

    }

    // junit 4: @Test(expected=ResourceAlreadyExistsException.class)
    @Test
    public void testZExceptionsExistingFile() throws Exception {
        VFSPath newFile = createRemoteFile("testExistingFile1", false);

        // current implemenation is to ignore existing files
        // except when force=false

        try {
            // create and do NOT ignore:
            newFile = createRemoteFile("testExistingFile1", false);
            Assert.fail("createFile(): Should raise at least an ResourceExistsException ");
        } catch (ResourceCreationException e) {
            debug("Caugh expected Exception:" + e);
            // Global.debugPrintStacktrace(e);
        } catch (Exception e) {
            Assert.fail("createFile(): Should raise ResourceExistsException, got:" + e);
        }

        newFile.delete();

        // Check create File while Dir exists:
        VFSPath newDir = createRemoteDir("testExistingDir1", false);

        try {
            // create and do NOT ignore:
            newFile = createRemoteFile("testExistingDir1", false);
            newFile.delete();

            Assert.fail("createFile(): Should raise at least an ResourceExistsException ");
        }
        // also allowed as the intended resource doesn't exists as exactly
        // the same type: existing Directory is not the intended File
        catch (ResourceCreationException e) {
            debug("Caugh expected Exception:" + e);
            // Global.debugPrintStacktrace(e);
        }

        newDir.delete();
    }

    @Test
    public void testZExceptionsExistingDir() throws Exception {
        VFSPath newDir = createRemoteDir("testExistingDir2", false);

        try {
            // create and do NOT ignore:
            newDir = createRemoteDir("testExistingDir2", false);
            newDir.delete();
            Assert.fail("createDir(): Should raise Exception:" + ResourceCreationException.class);
        } catch (ResourceAlreadyExistsException e) {
            debug("Caugh expected Exception:" + e);
        }

        newDir.delete();
        VFSPath newFile = createRemoteFile("testExistingFile2", false);

        try {
            // create Dir and do NOT ignore existing File or Dir:
            newDir = createRemoteDir("testExistingFile2", false);
            newDir.delete();
            Assert.fail("createDir(): Should raise Exception:" + ResourceAlreadyExistsException.class + " or "
                    + ResourceCreationException.class);
        }
        // also allowed as the intended resource doesn't exists as exactly
        // the same type: existing Directory is not the intended File
        catch (ResourceAlreadyExistsException e) {
            debug("Caugh expected Exception:" + e);
            // Global.debugPrintStacktrace(e);
        } catch (ResourceException e) {
            debug("Caugh expected Exception:" + e);
            Assert.fail("createDir(): Although a resource execption is better then any other," + "this unit test expects either:"
                    + ResourceCreationException.class + " or " + ResourceCreationException.class);
            // Global.debugPrintStacktrace(e);
        }

        newFile.delete();
    }

    /**
     * Regression test for SFTP:
     *
     * When writing to a file, last write must be 32k or else the write will not complete...
     *
     * @param targetSize
     * @throws Exception
     */
    @Test
    public void testZRegressionStreamWrite32KBug() throws Exception {
        _testStreamWrite(32000); // this worked
        _testStreamWrite(1024 * 1024); // this didn't work
    }

    /**
     * Another regression. File can have tildes in them for example "file~backup.ext". If not prefix with slash "/~" or if they
     * are not at the beginning of the path, for example "file:~/localDir" they must be kept as is.
     */
    @Test
    public void testZTildeInFileName() throws Exception {

        VFSPath dirPath = createRemoteDir("testZTildeInFileNameDir", false);
        String baseDir = dirPath.getVRL().getPath();
        String subFilename = "prefix~postfix";
        String compositePath = baseDir + "/" + subFilename;

        VFSPath resolvedPath = dirPath.resolve(subFilename);

        VRL baseDirVrl = dirPath.getVRL();
        VRL resolvedVrl = resolvedPath.getVRL();
        VRL expectedVrl = baseDirVrl.resolvePath(subFilename);

        // check VFPath
        Assert.assertEquals("Resolved path with tilde doesn't match expected path:", compositePath, resolvedPath.getVRL()
                .getPath());
        Assert.assertEquals("VRL of resolvePath doesn't match expected:", expectedVrl, resolvedVrl);

        // Check VRLs also
        Assert.assertEquals("VRL doesn't match expected:", expectedVrl, resolvedVrl);
        Assert.assertEquals("VRL path doesn't match expected:", compositePath, resolvedVrl.getPath());

    }

    // ========================================================================
    // Last Test !
    // ========================================================================

    // LAST UNIT TEST: Cleanup test directories
    @Test
    public void testZZZRemoveTestDir() {
        try {
            this.getRemoteTestDir().delete(true);
        } catch (Exception e) {
            message("*** Warning. Deleting remote test directory failed:" + e);
        }

        try {
            this.localTempDir.delete(true);
        } catch (Exception e) {
            message("*** Warning. Deleting local test directory failed:" + e);
        }
    }

    // ========================================================================
    //
    // ========================================================================

    /**
     * For 3rd party transfer !
     */
    protected VRL getOtherRemoteLocation() {
        return this.otherRemoteLocation;
    }

    protected void setTestRenames(boolean doRename) {
        this.testRenames = doRename;
    }

    protected void setTestStrangeChars(boolean testStrange) {
        this.testStrangeChars = testStrange;
    }

    protected void setTestWriteTests(boolean doWrites) {
        this.doWrites = doWrites;
    }

    protected boolean getTestWriteTests() {
        return doWrites;
    }

    protected void setTestDoBigTests(boolean doBigTests) {
        this.doBigTests = doBigTests;
    }

    protected boolean getTestDoBigTests() {
        return doBigTests;
    }

    protected void setRemoteTestDir(VFSPath remoteTestDir) {
        this.remoteTestDir = remoteTestDir;
    }

    protected VFSPath getRemoteTestDir() {
        return remoteTestDir;
    }

}
