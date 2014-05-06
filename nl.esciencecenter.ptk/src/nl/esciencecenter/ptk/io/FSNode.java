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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.io.exceptions.FileURISyntaxException;
import nl.esciencecenter.ptk.net.URIFactory;

/**
 * Wrapper around nio.file.path interface.
 */
public class FSNode
{
    public static final String DIR_TYPE = "Dir";

    public static final String FILE_SCHEME = "file";

    public static final String FILE_TYPE = "File";

    // =========
    // Instance
    // =========

    // nio !
    protected Path _path;

    protected BasicFileAttributes basicAttrs;

    protected FSNodeProvider fsHandler = null;

    protected PosixFileAttributes posixAttrs;

    protected FSNode(FSNodeProvider fsHandler, Path path)
    {
        this.fsHandler = fsHandler;
        init(path);
    }
    
    private void init(Path path)
    {
        this._path = path;
    }

    protected FSNodeProvider getFSHandler()
    {
        return fsHandler;
    }

    // =====
    //
    // =====

    public FSNode create() throws IOException
    {
        byte bytes[] = new byte[0];

        // Default way to create a file is by writing zero bytes:
        OutputStream outps = this.fsHandler.createOutputStream(this, false);
        outps.write(bytes);
        outps.close();

        return this;
    }

    public FSNode createDir(String subdir) throws IOException, FileURISyntaxException
    {
        FSNode dir = newPath(resolvePath(subdir));
        dir.mkdir();
        return dir;
    }

    public FSNode createFile(String filepath) throws IOException, FileURISyntaxException
    {
        FSNode file = newPath(resolvePath(filepath));
        file.create();
        return file;
    }

    public boolean delete(LinkOption... linkOptions) throws IOException
    {
        Files.delete(_path);
        return true;
    }

    public boolean exists(LinkOption... linkOptions)
    {
        if (linkOptions == null)
        {
            return Files.exists(_path);
        }
        else
        {
            return Files.exists(_path, linkOptions);
        }
    }

    /**
     * Returns creation time in millis since EPOCH, if supported. Returns -1 otherwise.
     */
    public FileTime getAccessTime() throws IOException
    {
        BasicFileAttributes attrs = this.getBasicAttributes();

        if (attrs == null)
        {
            return null;
        }

        return attrs.lastAccessTime();
    }

    /**
     * Returns last part of the path inlcuding extension.
     */
    public String getBasename()
    {
        return getBasename(true);
    }

    public String getBasename(boolean includeExtension)
    {
        String fileName = _path.getFileName().toString();

        if (includeExtension)
        {
            return fileName;
        }
        else
        {
            return URIFactory.stripExtension(fileName);
        }
    }

    public BasicFileAttributes getBasicAttributes(LinkOption... linkOptions) throws IOException
    {
        try
        {
            if (basicAttrs == null)
            {
                basicAttrs = Files.readAttributes(_path, BasicFileAttributes.class, linkOptions);
            }
        }
        catch (IOException e)
        {
            // Auto dereference in the case of a borken link:
            if (isBrokenLink())
            {
                basicAttrs = Files.readAttributes(_path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            }
            else
            {
                throw e;
            }
        }

        return basicAttrs;
    }

    public FileTime getCreationTime() throws IOException
    {
        BasicFileAttributes attrs = this.getBasicAttributes();

        if (attrs == null)
        {
            return null;
        }

        return attrs.creationTime();
    }

    public String getDirname()
    {
        return URIFactory.dirname(_path.toUri().getPath());
    }

    public String getExtension()
    {
        return URIFactory.extension(_path.toUri().getPath());
    }

    public long getFileSize() throws IOException
    {
        BasicFileAttributes attrs = this.getBasicAttributes();

        if (attrs == null)
            return 0;

        return attrs.size();
    }

    public String getGroupName() throws IOException
    {
        PosixFileAttributes attrs;

        if ((attrs = this.getPosixAttributes()) == null)
        {
            return null;
        }

        return attrs.group().getName();
    }

    public String getHostname()
    {
        return _path.toUri().getHost();
    }

    public FileTime getModificationTime() throws IOException
    {
        BasicFileAttributes attrs = this.getBasicAttributes();

        if (attrs == null)
        {
            return null;
        }

        return attrs.lastModifiedTime();
    }

    public long getModificationTimeMillies() throws IOException
    {
        FileTime time = getModificationTime();
        if (time == null)
        {
            return -1;
        }
        else
        {
            return time.toMillis();
        }
    }

    public String getOwnerName() throws IOException
    {
        PosixFileAttributes attrs;

        if ((attrs = this.getPosixAttributes()) == null)
            return null;

        return attrs.owner().getName();
    }

    public FSNode getParent()
    {
        return new FSNode(getFSHandler(), _path.getParent());
    }

    /**
     * @return Return actual nio.file.path
     */
    public Path getPath()
    {
        return _path;
    }

    /**
     * Returns absolute and normalized URI path
     * 
     * @return
     */
    public String getPathname()
    {
        return _path.toUri().getPath();
    }

    public int getPort()
    {
        return _path.toUri().getPort();
    }

    public PosixFileAttributes getPosixAttributes() throws IOException
    {
        try
        {
            if (posixAttrs == null)
            {
                posixAttrs = Files.readAttributes(_path, PosixFileAttributes.class);
            }
        }
        catch (IOException e)
        {
            // auto dereference in the case of a borken link:
            if (isBrokenLink())
            {
                posixAttrs = Files.readAttributes(_path, PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            }
            else
            {
                throw e;
            }
        }
        catch (UnsupportedOperationException e)
        {
            return null;
        }

        return posixAttrs;
    }

    /**
     * Returns symbolic link target or NULL
     */
    public FSNode getSymbolicLinkTarget() throws IOException
    {
        if (this.isSymbolicLink() == false)
            return null;

        Path target = Files.readSymbolicLink(_path);

        return new FSNode(getFSHandler(), target);
    }

    public int getUnixFileMode() throws IOException
    {
        PosixFileAttributes attrs;
        if ((attrs = getPosixAttributes()) == null)
            return 0;

        Set<PosixFilePermission> perms = attrs.permissions();

        return FSUtil.toUnixFileMode(perms);
    }

    public URI getURI()
    {
        return _path.toUri();
    }

    public URL getURL() throws MalformedURLException
    {
        return _path.toUri().toURL();
    }



    public boolean isBrokenLink() throws IOException
    {
        if (isSymbolicLink() == false)
        {
            return false;
        }

        return (getSymbolicLinkTarget().exists() == false);
    }

    public boolean isDirectory(LinkOption... linkOptions)
    {
        if (linkOptions == null)
        {
            return Files.isDirectory(_path);
        }
        else
        {
            return Files.isDirectory(_path, linkOptions);
        }
    }

    public boolean isFile(LinkOption... linkOptions)
    {
        return Files.isRegularFile(_path, linkOptions);
    }

    public boolean isHidden()
    {
        // Windows has a different way to hide files.
        // Use default UNIX way to indicate hidden files.
        return this.getBasename().startsWith(".");
    }

    /**
     * Whether this file points to a local file. Currently only local files are supported.
     */
    public boolean isLocal()
    {
        return true;
    }

//    boolean isPosix()
//    {
//        return false;
//    }

    public boolean isRoot() 
    {
        String path = this.getPathname();

        if ("/".equals(path))
        {
            return true;
        }

        if (isFileSystemRoot())
        {
            return true;
        }

        return false;
    }

    /**
     * Is a unix style soft- or symbolic link.
     * 
     * @throws IOException
     */
    public boolean isSymbolicLink()
    {
        return Files.isSymbolicLink(_path);
    }

    public boolean isFileSystemRoot() 
    {
        List<FSNode> roots = this.fsHandler.listRoots();
        
        for (FSNode root:roots)
        {
            if (root._path.normalize().toString().equals(_path.normalize().toString()))
            {
                return true;
            }
        }
        return false; 
    }

    public String[] list() throws IOException
    {
        DirectoryStream<Path> dirStream = Files.newDirectoryStream(_path);
        try
        {
            Iterator<Path> dirIterator = dirStream.iterator();
            ArrayList<String> list = new ArrayList<String>();

            while (dirIterator.hasNext())
            {
                list.add(dirIterator.next().getFileName().toString());
            }

            return list.toArray(new String[0]);
        }
        finally
        {
            dirStream.close();
        }
    }

    public FSNode[] listNodes() throws IOException
    {
        DirectoryStream<Path> dirStream = Files.newDirectoryStream(_path);
        try
        {
            Iterator<Path> dirIterator = dirStream.iterator();
            ArrayList<FSNode> list = new ArrayList<FSNode>();

            while (dirIterator.hasNext())
            {
                list.add(new FSNode(getFSHandler(), dirIterator.next()));
            }

            return list.toArray(new FSNode[0]);

        }
        finally
        {
            dirStream.close();
        }

    }

    public FSNode mkdir() throws IOException
    {
        Files.createDirectory(_path);
        return this;
    }

    public FSNode mkdirs() throws IOException
    {
        Files.createDirectories(_path);
        return this;
    }

    public FSNode newPath(String path) throws IOException
    {
        URI resolved=this.resolvePathURI(path); 
        FSNode lfile = this.fsHandler.newFSNode(resolved);
        return lfile;
    }

    public FSNode renameTo(String relativeOrAbsolutePath) throws IOException
    {
        FSNode other = this.newPath(relativeOrAbsolutePath);

        Path targetPath = other._path;
        Path actualPath = Files.move(this._path, targetPath);
        // no errrors, assume path is renamed.
        return other;
    }

    public String resolvePath(String relPath) throws FileURISyntaxException
    {
        try
        {
            return new URIFactory(_path.toUri()).resolvePath(relPath);
        }
        catch (URISyntaxException e)
        {
            throw new FileURISyntaxException(e.getMessage(), relPath, e);
        }
    }

    public URI resolvePathURI(String relPath) throws FileURISyntaxException
    {
        try
        {
            return new URIFactory(_path.toUri()).setPath(resolvePath(relPath)).toURI();
        }
        catch (URISyntaxException e)
        {
            throw new FileURISyntaxException(e.getMessage(), relPath, e);
        }
    }

    public void setUnixFileMode(int mode) throws IOException
    {
        Files.setPosixFilePermissions(_path, FSUtil.fromUnixFileMode(mode));
    }

    public boolean sync()
    {
        this.basicAttrs = null;
        this.posixAttrs = null;
        return true;
    }

    // =======================
    // IO Methods
    // =======================

    public InputStream createInputStream() throws IOException
    {
        return fsHandler.createInputStream(this);
    }

    public OutputStream createOutputStream(boolean append) throws IOException
    {
        return fsHandler.createOutputStream(this, append);
    }

    // =======================
    // Misc.
    // =======================

    public java.io.File toJavaFile()
    {
        return _path.toFile();
    }

    public String toString()
    {
        return "(FSNode)" + this.getURI().toString();
    }

}
