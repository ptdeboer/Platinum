/*
 * Copyrighted 2012-2013 Netherlands eScience Center.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import nl.esciencecenter.ptk.net.URIFactory;

/**
 * Abstract FileSystem Node of local or remote Filesystem. Can be File or
 * Directory. Uses URI based location.
 */
public abstract class FSNode
{
    public static final String FILE_TYPE = "File";

    public static final String DIR_TYPE = "Dir";

    public static final String FILE_SCHEME = "file";

    // ===
    //
    // ===

    private URI uri = null;

    private FSHandler fsHandler = null;

    protected FSNode(FSHandler fsHandler, URI uri)
    {
        this.uri = uri;
        this.fsHandler = fsHandler;
    }

    protected FSHandler getFSHandler()
    {
        return fsHandler;
    }

    protected void setURI(URI URI)
    {
        this.uri = URI;
    }

    public URI getURI()
    {
        return uri;
    }

    public URL getURL() throws MalformedURLException
    {
        return uri.toURL();
    }

    public FSNode getNode(String relpath) throws FileURISyntaxException
    {
        return newFile(resolvePath(relpath));
    }

    /**
     * Whether this file points to a local file.
     */
    public boolean isLocal()
    {
        return false;
    }

    /**
     * Returns absolute and normalized URI path as String.
     */
    public String getPathname()
    {
        return uri.getPath();
    }

    /**
     * Returns last part of the path inlcuding extension.
     */
    public String getBasename()
    {
        return URIFactory.basename(uri.getPath());
    }

    public String getBasename(boolean includeExtension)
    {
        String fileName = URIFactory.basename(uri.getPath());

        if (includeExtension)
        {
            return fileName;
        }
        else
        {
            return URIFactory.stripExtension(fileName);
        }
    }

    public String getExtension()
    {
        return URIFactory.extension(uri.getPath());
    }

    public String getDirname()
    {
        return URIFactory.dirname(uri.getPath());
    }

    public String getHostname()
    {
        return uri.getHost();
    }

    public int getPort()
    {
        return uri.getPort();
    }

    public String toString()
    {
        return "(FSNode)" + this.getURI().toString();
    }

    public boolean sync()
    {
        return false;
    }

    /**
     * Returns creation time in millis since EPOCH, if supported. Returns -1
     * otherwise.
     */
    public long getAccessTime() throws IOException
    {
        return -1;
    }

    public boolean isHidden()
    {
        // Windows has a different way to hide files.
        // Use default UNIX way to indicate hidden files.
        return this.getBasename().startsWith(".");
    }

    /**
     * Is a unix style soft- or symbolic link
     */
    public boolean isSymbolicLink()
    {
        return false;
    }

    // =======================================================================
    // IO Methods
    // =======================================================================

    public InputStream createInputStream() throws IOException
    {
        throw new Error("Not supported: createInputStream() of:" + this);
    }

    public OutputStream createOutputStream() throws IOException
    {
        throw new Error("Not supported: createOutputStream() of:" + this);
    }

    public FSNode createDir(String subdir) throws IOException, FileURISyntaxException
    {
        FSNode dir = newFile(resolvePath(subdir));
        dir.mkdir();
        return dir;
    }

    public FSNode createFile(String filepath) throws IOException, FileURISyntaxException
    {
        FSNode file = newFile(resolvePath(filepath));
        file.create();
        return file;
    }

    public String resolvePath(String relPath) throws FileURISyntaxException
    {
        try
        {
            return new URIFactory(uri).resolvePath(relPath);
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
            return new URIFactory(uri).setPath(resolvePath(relPath)).toURI();
        }
        catch (URISyntaxException e)
        {
            throw new FileURISyntaxException(e.getMessage(), relPath, e);
        }
    }

    public boolean create() throws IOException
    {
        byte bytes[] = new byte[0];

        // create file by writing zero bytes:
        OutputStream outps = this.createOutputStream();
        outps.write(bytes);
        outps.close();

        return true;
    }

    /**
     * Returns creation time in millis since EPOCH, if supported. Returns -1
     * otherwise.
     */
    public long getCreationTime() throws IOException
    {
        return -1;
    }

    public boolean isRoot()
    {
        String path = this.getPathname();

        if ("/".equals(path))
        {
            return true;
        }

        return false;
    }

    public long getCreateionTime() throws IOException
    {
        BasicFileAttributes attrs = this.getBasicAttributes();

        if (attrs == null)
            return -1;

        FileTime time = attrs.creationTime();

        if (time == null)
            return -1;

        return time.toMillis();
    }

    public long getModificationTime() throws IOException
    {
        BasicFileAttributes attrs = this.getBasicAttributes();

        if (attrs == null)
            return -1;

        FileTime time = attrs.lastModifiedTime();

        if (time == null)
            return -1;

        return time.toMillis();
    }

    public long getFileSize() throws IOException
    {
        BasicFileAttributes attrs = this.getBasicAttributes();

        if (attrs == null)
            return 0;

        return attrs.size();
    }

    // =======================================================================
    // Abstract Interface
    // =======================================================================

    /**
     * FSNode factory method, optionally resolves path against parent FSNode.
     */
    public abstract FSNode newFile(String path) throws FileURISyntaxException;

    public abstract boolean exists(LinkOption... linkOptions);

    /** Is a regular file. */
    public abstract boolean isFile(LinkOption... linkOptions);

    /** Is a regular directory. */
    public abstract boolean isDirectory(LinkOption... linkOptions);

    /** Logical parent */
    public abstract FSNode getParent();

    /** Delete file or empty directory. */
    public abstract void delete(LinkOption... linkOptions) throws IOException;

    // === Directory methods === //

    /** Return contents of directory. */
    public abstract String[] list() throws IOException;

    /** Return contents of directory as FSNode objects . */
    public abstract FSNode[] listNodes() throws IOException;

    /** Create last path element as (sub)directory, parent directory must exist. */
    public abstract void mkdir() throws IOException;

    /** Create full directory path. */
    public abstract void mkdirs() throws IOException;

    public abstract BasicFileAttributes getBasicAttributes(LinkOption... linkOptions) throws IOException;

}
