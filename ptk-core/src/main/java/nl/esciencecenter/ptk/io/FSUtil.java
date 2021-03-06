/*
 * Copyright 2012-2014 Netherlands eScience Center.
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

package nl.esciencecenter.ptk.io;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.exceptions.FileURISyntaxException;
import nl.esciencecenter.ptk.net.URIFactory;
import nl.esciencecenter.ptk.util.ContentReader;
import nl.esciencecenter.ptk.util.ContentWriter;
import nl.esciencecenter.ptk.util.ResourceLoader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

/**
 * File System util and resource provider works with local file-paths and URIs. <br>
 * Pathnames are resolved to absolute paths and normalized to URI style paths.<br>
 * <br>
 * For example: <code>'C:Bob'</code> => <code>'file:///C:/Users/Bob/'</code>
 */
@Slf4j
public class FSUtil implements ResourceProvider, FSInterface {

    private static FSUtil instance = null;

    public static FSUtil fsutil() {
        if (instance == null) {
            instance = new FSUtil();
        }

        return instance;
    }

    public static class FSOptions {

        /**
         * Resolve '~' to user home dir.
         */
        public boolean resolveTilde = true;

        /**
         * Whether to follow links.
         *
         * @see LinkOption
         */
        public LinkOption linkOption = null;

        /**
         * Default character encoding;
         */
        public String charEncoding = ResourceLoader.CHARSET_UTF8;
    }

    // ========================================================================
    // Class Util methods
    // ========================================================================

    public static int toUnixFileMode(Set<PosixFilePermission> perms) {
        int mode = 0;

        if (perms.contains(PosixFilePermission.OWNER_READ))
            mode |= 0400;
        if (perms.contains(PosixFilePermission.OWNER_WRITE))
            mode |= 0200;
        if (perms.contains(PosixFilePermission.OWNER_EXECUTE))
            mode |= 0100;
        if (perms.contains(PosixFilePermission.GROUP_READ))
            mode |= 0040;
        if (perms.contains(PosixFilePermission.GROUP_WRITE))
            mode |= 0020;
        if (perms.contains(PosixFilePermission.GROUP_EXECUTE))
            mode |= 0010;
        if (perms.contains(PosixFilePermission.OTHERS_READ))
            mode |= 0004;
        if (perms.contains(PosixFilePermission.OTHERS_WRITE))
            mode |= 0002;
        if (perms.contains(PosixFilePermission.OTHERS_EXECUTE))
            mode |= 0001;

        return mode;
    }

    public static Set<PosixFilePermission> fromUnixFileMode(int mode) {
        Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();

        if ((mode & 0400) > 0)
            perms.add(PosixFilePermission.OWNER_READ);
        if ((mode & 0200) > 0)
            perms.add(PosixFilePermission.OWNER_WRITE);
        if ((mode & 0100) > 0)
            perms.add(PosixFilePermission.OWNER_EXECUTE);

        if ((mode & 0040) > 0)
            perms.add(PosixFilePermission.GROUP_READ);
        if ((mode & 0020) > 0)
            perms.add(PosixFilePermission.GROUP_WRITE);
        if ((mode & 0010) > 0)
            perms.add(PosixFilePermission.GROUP_EXECUTE);

        if ((mode & 0004) > 0)
            perms.add(PosixFilePermission.OTHERS_READ);
        if ((mode & 0002) > 0)
            perms.add(PosixFilePermission.OTHERS_WRITE);
        if ((mode & 0001) > 0)
            perms.add(PosixFilePermission.OTHERS_EXECUTE);

        return perms;
    }

    // ========================================================================
    // Instance
    // ========================================================================

    protected Path userHome;
    protected Path workingDir;
    protected Path tmpDir;
    protected FSOptions fsOptions = new FSOptions();

    protected FSUtil() {
        log.debug("New FSUtil()");
        init();
    }

    private void init() {
        // Resolve against default file system
        FileSystem fs = FileSystems.getDefault();
        this.userHome = fs.getPath(GlobalProperties.getGlobalUserHome()).toAbsolutePath();
        this.workingDir = fs.getPath(GlobalProperties.getGlobalUserDir()).toAbsolutePath();
        this.tmpDir = fs.getPath(GlobalProperties.getGlobalTempDir()).toAbsolutePath();
    }

    @Override
    public LinkOption[] linkOptions() {
        if (this.fsOptions.linkOption == null) {
            return null;
        } else {
            return new LinkOption[]{fsOptions.linkOption};
        }
    }

    // ========================================================================
    // Resolve
    // ========================================================================

    public FSPath resolvePath(String path) throws FileURISyntaxException {
        return resolvePath(workingDir, userHome, fsOptions.resolveTilde, path);
    }

    /**
     * @return new FileSystem Path specified by the URI.
     * @throws FileURISyntaxException
     */
    public FSPath resolvePath(URI uri) throws FileURISyntaxException {
        if (uri.isAbsolute() == false) {
            return resolvePath(this.workingDir, this.userHome, true, uri.getPath());
        }
        return new FSPath(this, Paths.get(uri));
    }

    /**
     * Return new FileSystem Path specified by the URL.
     *
     * @throws FileURISyntaxException
     */
    public FSPath resolvePath(URL url) throws FileURISyntaxException {
        try {
            return new FSPath(this, Paths.get(url.toURI()));
        } catch (URISyntaxException e) {
            throw new FileURISyntaxException("Not a valid URI:" + url, url.toString(), e);
        }
    }

    /**
     * Resolve relative path against current working directory and return absolute URI.<br>
     * Note: URI has an absolute path and uses a forward slash as File separator.
     *
     * @return normalized and absolute URI path.
     */
    public URI resolvePathURI(String path) throws FileURISyntaxException {
        return resolvePath(workingDir, userHome, fsOptions.resolveTilde, path).toURI();
    }

    /**
     * Main resolve method resolve relative path against home and current working directory. Both
     * localized paths, 'C:\Home' are well as normalized URI path are allowed '/C:/Home/'.<br>
     * For a complete URI like "file:///C:/Home" use {@link #resolvePath(URI)}
     */
    public FSPath resolvePath(Path cwd, Path home, boolean resolveTilde, String path) throws FileURISyntaxException {
        //
        String newPath = path;
        if (resolveTilde) {
            newPath = URIFactory.resolveTilde(home.toString(), path);
        }
        // URIFactory introduces URI style paths.
        return new FSPath(this, cwd.resolve(localizePath(newPath, false)));
    }

    /**
     * Localize performs some OS specific patches to the current path string.
     */
    public String localizePath(String path, boolean flipPathSeperator) {
        String newPath = path;
        // patch: allow URI style paths like "/C:/Home/...".
        if (GlobalProperties.isWindows()) {
            boolean uriDosPath = (path.length() > 2) && (path.charAt(0) == '/') && (path.charAt(2) == ':');
            if (uriDosPath) {
                newPath = newPath.substring(1);
            }
        }
        return newPath;
    }

    public FSPath newFSPath(URI uri) throws FileURISyntaxException {
        return this.resolvePath(uri);
    }

    // ========================================================================
    //
    // ========================================================================

    public boolean existsPath(String path, LinkOption... linkOptions) throws IOException {
        return resolvePath(path).exists(linkOptions);
    }

    /**
     * Simple Copy File uses URIs to ensure absolute and normalized Paths.
     */
    public long copyFile(URI source, URI destination) throws IOException {
        long num;
        // Create
        try (InputStream finput = createInputStream(resolvePath(source))) {
            try (OutputStream foutput = createOutputStream(resolvePath(destination), false)) {
                // Copy
                num = IOUtil.copyStreams(finput, foutput, false);
            }
        }
        return num;
    }

    /**
     * Checks whether paths exists and is a file. If the filePath contains invalid characters, this
     * method will also return false.
     */
    public boolean existsFile(String filePath, boolean mustBeFileType, LinkOption... linkOptions) {
        if (filePath == null) {
            return false;
        }

        try {
            FSPath file = newFSPath(filePath);
            if (file.exists(linkOptions) == false) {
                return false;
            }

            if (mustBeFileType) {
                return file.isFile(linkOptions);
            } else {
                return true;
            }
        } catch (FileURISyntaxException e) {
            return false;
        }
    }

    /**
     * Checks whether directoryPath paths exists and is a directory. If the directory path contains
     * invalid characters the method will also return false.
     */
    public boolean existsDir(String directoryPath, LinkOption... linkOptions) {
        //
        if (directoryPath == null) {
            return false;
        }
        //
        try {
            FSPath file = newFSPath(directoryPath);
            if (file.exists(linkOptions) == false)
                return false;
            if (file.isDirectory(linkOptions))
                return true;
        } catch (FileURISyntaxException e) {
            return false;
        }

        return false;
    }

    /**
     * List directory: returns (URI) normalized paths.
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    public List<FSPath> list(String dirPath, LinkOption... linkOptions) throws IOException {
        FSPath file = resolvePath(resolvePathURI(dirPath));

        if (file.exists(linkOptions) == false) {
            return null;
        }
        if (file.isDirectory(linkOptions) == false) {
            return null;
        }

        return file.list();
    }

    public void deleteFile(String filename) throws IOException {
        FSPath file = newFSPath(filename);
        if (file.exists(LinkOption.NOFOLLOW_LINKS) == false)
            return;
        file.delete();
    }

    /**
     * Open local file and return OutputStream to write to. The default implementation is to create
     * a new File if it doesn't exists or replace an existing file with the new contents if it
     * exists.
     *
     * @param filename - relative or absolute file path (resolves to absolute path on local fileystem)
     * @throws IOException
     */
    public OutputStream createOutputStream(String filename) throws IOException {
        return createOutputStream(newFSPath(filename), false);
    }

    public FSPath mkdir(FSPath path) throws IOException {
        Path newPath = Files.createDirectory(path.path());
        return new FSPath(this, newPath);
    }

    public FSPath mkdirs(FSPath path) throws IOException {
        Path newPath = Files.createDirectories(path.path());
        return new FSPath(this, newPath);
    }

    public FSPath mkdirs(String path) throws IOException {
        FSPath dirs = this.newFSPath(path);
        return this.mkdirs(dirs);
    }

    public FSPath getLocalTempDir() throws IOException {
        return new FSPath(this, tmpDir);
    }

    public FSPath getWorkingDir() throws IOException {
        return new FSPath(this, workingDir);
    }

    public URI getUserHome() {
        return userHome.toUri();
    }

    public FSPath getUserHomeDir() throws IOException {
        return new FSPath(this, userHome);
    }

    public URI getUserHomeURI() {
        return userHome.toUri();
    }

    public void setWorkingDir(URI newWorkingDir) throws FileURISyntaxException {
        // check for local ?
        this.workingDir = this.resolvePath(newWorkingDir).path();
    }

    public URI getWorkingDirURI() {
        return workingDir.toUri();
    }

    /**
     * Returns new directory FSPath Object. Path might not exist.
     *
     * @param dirUri - location of new Directory
     * @return - new Local Directory object.
     * @throws IOException
     */
    public FSPath newLocalDir(URI dirUri) throws FileURISyntaxException {
        FSPath dir = this.resolvePath(dirUri);
        return dir;
    }

    public FSPath newFSPath(String fileUri) throws FileURISyntaxException {
        return resolvePath(fileUri);
    }

    public void deleteDirectoryContents(URI uri, boolean recursive) throws IOException {
        FSPath node = newLocalDir(uri);
        if (node.exists() == false)
            throw new FileNotFoundException("Directory does not exist:" + uri);

        deleteDirectoryContents(node, recursive);
        return;
    }

    public void deleteDirectoryContents(FSPath dirNode, boolean recursive) throws IOException {
        FSPath[] nodes = dirNode.listNodes();
        for (FSPath node : nodes) {
            if (node.isDirectory() && recursive) {
                deleteDirectoryContents(node, recursive);
            }
            node.delete();
        }
    }

    public void delete(FSPath node, boolean recursive) throws IOException {
        if ((node.isDirectory()) && (recursive)) {
            this.deleteDirectoryContents(node, recursive);
        }

        node.delete();
    }


    public boolean hasPosixFS() {

        if (GlobalProperties.isWindows())
            return false;

        if (GlobalProperties.isLinux())
            return true;

        if (GlobalProperties.isMac())
            return true;

        return true;
    }

    public boolean isLocalFSUri(URI uri) {
        return uri.getScheme().equalsIgnoreCase("file");
    }

    @Override
    public List<FSPath> listRoots() {
        ArrayList<FSPath> roots = new ArrayList<FSPath>();

        Iterator<Path> iterator = java.nio.file.FileSystems.getDefault().getRootDirectories().iterator();

        while (iterator.hasNext()) {
            Path path = iterator.next();
            roots.add(new FSPath(this, path));
        }
        return roots;
    }

    /**
     * Legacy file-drive check which explicitly checks drive "A:" through "Z:"
     *
     * @throws IOException
     */
    public List<FSPath> listWindowsDrives(boolean skipFloppyScan) throws IOException {
        ArrayList<FSPath> roots = new ArrayList<FSPath>();

        // Create the A: drive whether it is mounted or not
        if (skipFloppyScan == false) {
            String drivestr = "A:\\";
            roots.add(this.newFSPath(drivestr));
        }

        if (skipFloppyScan == false) {
            String drivestr = "B:\\";
            roots.add(this.newFSPath(drivestr));
        }

        // Run through all possible mount points and check
        // for their existence.
        for (char c = 'C'; c <= 'Z'; c++) {
            char[] deviceChars = {c, ':', '\\'};
            String deviceName = new String(deviceChars);
            FSPath device = newFSPath(deviceName);

            if (device.exists()) {
                roots.add(device);
            }
        }
        return roots;
    }

    // ==============
    // IO Methods
    // ==============

    @Override
    public InputStream createInputStream(java.net.URI uri) throws IOException {
        return createInputStream(resolvePath(uri));
    }

    @Override
    public InputStream createInputStream(FSPath node) throws IOException {
        return Files.newInputStream(node.path());
    }

    @Override
    public OutputStream createOutputStream(URI uri) throws IOException {
        if (isLocalFSUri(uri)) {
            return createOutputStream(resolvePath(uri), false);
        } else {
            // use default URL method !
            return uri.toURL().openConnection().getOutputStream();
        }
    }

    public OutputStream createOutputStream(FSPath node, boolean append) throws IOException {

        OpenOption[] openOptions;

        if (append) {
            openOptions = new OpenOption[4];
            openOptions[0] = StandardOpenOption.WRITE;
            openOptions[1] = StandardOpenOption.CREATE; // create if not exists
            openOptions[2] = StandardOpenOption.TRUNCATE_EXISTING;
            openOptions[3] = StandardOpenOption.APPEND;
        } else {
            openOptions = new OpenOption[3];
            openOptions[0] = StandardOpenOption.WRITE;
            openOptions[1] = StandardOpenOption.CREATE; // create if not exists
            openOptions[2] = StandardOpenOption.TRUNCATE_EXISTING;
        }

        return Files.newOutputStream(node.path(), openOptions); // OpenOptions..
    }

    @Override
    public RandomReadable createRandomReader(URI uri) throws IOException {
        return new FSReader(resolvePath(uri).path());
    }

    @Override
    public RandomReadable createRandomReader(FSPath node) throws IOException {
        return new FSReader(node._path);
    }

    @Override
    public RandomWritable createRandomWriter(FSPath node) throws IOException {
        return new FSWriter(node._path);
    }

    @Override
    public RandomWritable createRandomWriter(URI uri) throws IOException {
        return new FSWriter((resolvePath(uri)._path));
    }

    // ============
    // Attributes
    // ============

    public BasicFileAttributes getBasicAttributes(FSPath path, LinkOption... linkOptions) throws IOException {
        try {
            return Files.readAttributes(path.path(), BasicFileAttributes.class, linkOptions);
        } catch (IOException e) {
            // Auto dereference in the case of a borken link:
            if (path.isBrokenLink()) {
                return Files.readAttributes(path.path(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            } else {
                throw e;
            }
        }
    }

    public PosixFileAttributes getPosixAttributes(FSPath path, LinkOption... linkOptions) throws IOException {
        try {
            return Files.readAttributes(path.path(), PosixFileAttributes.class);
        } catch (IOException e) {
            // auto dereference in the case of a borken link:
            if (path.isBrokenLink()) {
                return Files.readAttributes(path.path(), PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            } else {
                throw e;
            }
        } catch (UnsupportedOperationException e) {
            log.warn("No posix attributes:UnsupportedOperationException:{}", e.getMessage());
            return null;
        }
    }

    // ============
    // Util methods
    // ============

    public String getCharEncoding() {
        return fsOptions.charEncoding;
    }

    public String readText(String filePath) throws IOException {
        FSPath path = this.newFSPath(filePath);
        return readText(path);
    }

    public String readText(FSPath path) throws IOException {
        try (InputStream inps = this.createInputStream(path)) {
            return new ContentReader(inps, this.fsOptions.charEncoding, false).readString();
        }
        // autoclose 
    }

    public void writeText(String filePath, String message) throws IOException {
        FSPath path = this.newFSPath(filePath);
        writeText(path, message);
    }

    public void writeText(FSPath filePath, String message) throws IOException {
        try (OutputStream outps = this.createOutputStream(filePath, false)) {
            new ContentWriter(outps, this.fsOptions.charEncoding, false).write(message);
        }
        // autoclose 
    }
}
