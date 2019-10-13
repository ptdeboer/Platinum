/*
 * Copyrighted 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * For details, see the LICENCE.txt file location in the root directory of this
 * distribution or obtain the Apache License at the following location:
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For the full license, see: LICENCE.txt (located in the root folder of this distribution).
 * ---
 */
// source: 

package nl.esciencecenter.ptk.io;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import settings.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Test_FSUtil {
    // =============
    // Static Field
    // =============

    protected static Object testDirMutex = new Object();

    protected static FSPath testDir = null;

    protected static FSUtil getFSUtil() {
        return FSUtil.fsutil();
    }

    protected static FSPath getCreateTestDir() throws IOException {
        synchronized (testDirMutex) {
            if (testDir != null) {
                if (testDir.exists()) {
                    return testDir;
                }
            }

            FSUtil fsUtil = getFSUtil();

            // setup also tests basic methods !
            testDir = fsUtil.newFSPath(Settings.getInstance().getTestSubdir("testfsutil"));

            Assert.assertNotNull("Local test directory is null", testDir);

            if (testDir.exists() == false) {
                getFSUtil().mkdir(testDir);
            }

            Assert.assertTrue("Local test directory MUST exist!", testDir.exists());

            return testDir;
        }
    }

    // ========================================================================
    // PRE
    // ========================================================================

    @BeforeClass
    public static void setup() throws Exception {
        Assert.assertNotNull("Test Dir must be initialized", getCreateTestDir());
    }

    // ========================================================================
    // Actual tests
    // ========================================================================

    public FSPath getTestDir() throws IOException {
        return getCreateTestDir();
    }

    @Test
    public void checkTestDir() throws IOException {
        FSPath dir = getTestDir();
        Assert.assertTrue("Local test directory MUST exist:" + testDir, dir.exists());
        Assert.assertTrue("Local test directory MUST is not directory!", dir.isDirectory());
    }

    public void testCreateDeleteFile(FSPath parent, String fileName) throws Exception {
        FSPath tDir = getTestDir();
        String path = tDir.getPathname();

        FSPath file = tDir.resolve(fileName);
        Assert.assertFalse("Test file already exists:" + file, file.exists());
        file.create();
        Assert.assertTrue("Test file must exist after mkdir():" + file, file.exists());
        Assert.assertTrue("Test file be of file type after create():" + file, file.isFile());
        if (file.isLocal()) {
            java.io.File jfile = new java.io.File(file.getPathname());
            Assert.assertTrue("A local created file must be compatible with an existing (local) java.io.File", jfile.exists());
            Assert.assertTrue("A local file must be a real 'file' type", jfile.isFile());
        }

        file.delete();
        Assert.assertFalse("Test file may not exist after delete():" + file, file.exists());
    }

    @Test
    public void testCreateDeleteDir() throws Exception {
        FSPath tDir = getTestDir();
        String path = tDir.getPathname();
        String subdir = "subdir";
        FSPath subDir = tDir.resolve(subdir);
        Assert.assertFalse("Subdirectory already exists:" + subDir, subDir.exists());
        getFSUtil().mkdirs(subDir);
        Assert.assertTrue("Subdirectory must exist after mkdir():" + subDir, subDir.exists());
        Assert.assertTrue("Subdirectory must be directory after mkdir():" + subDir, subDir.isDirectory());
        if (subDir.isLocal()) {
            java.io.File jfile = new java.io.File(subDir.getPathname());
            Assert.assertTrue("A local created file must be compatible with an existing (local) java.io.File", jfile.exists());
            Assert.assertTrue("A local directory must be a real 'Directory' type", jfile.isDirectory());
        }

        subDir.delete();
        Assert.assertFalse("Subdirectory may not exist after delete():" + subDir, subDir.exists());
    }

    @Test
    public void testCreateDeleteFile() throws Exception {
        testCreateDeleteFile(getTestDir(), "testFile1");
        testCreateDeleteFile(getTestDir(), "test File1");
    }

    public void testCreateReadWriteFile(FSPath parent, String fileName) throws Exception {
        FSPath tDir = getTestDir();
        String path = tDir.getPathname();

        FSPath file = tDir.resolve(fileName);
        Assert.assertFalse("Test file already exists:" + file, file.exists());

        OutputStream outps = getFSUtil().createOutputStream(file, false);

        byte[] buffer =
                {
                        1, 2, 3, 4
                };
        outps.write(buffer, 0, 4);
        outps.close();

        byte[] buffer2 = new byte[4];
        InputStream inps = getFSUtil().createInputStream(file);
        inps.read(buffer2, 0, 4);
        inps.close();

        for (int i = 0; i < buffer.length; i++) {
            Assert.assertEquals("Byte at #" + i + " differs!", buffer[i], buffer2[i]);
        }

        file.delete();

    }

    @Test
    public void testCreateReadWriteFile() throws Exception {
        testCreateReadWriteFile(getTestDir(), "testRWFile1");
        testCreateReadWriteFile(getTestDir(), "test RWFile1");
    }

    // ========================================================================
    // Finalize Test Suite: cleanup test dir!
    // ========================================================================

    @AfterClass
    public static void removeTestDir() throws IOException {
        try {
            FSPath tDir = getCreateTestDir();
            getFSUtil().delete(tDir, true);
            Assert.assertFalse("Test directory must be deleted after testSuite", tDir.exists());
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
