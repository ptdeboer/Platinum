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
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import nl.esciencecenter.ptk.io.exceptions.FileURISyntaxException;
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

    private FSNodeProvider fsHandler = null;

    protected FSNode(FSNodeProvider fsHandler, URI uri)
    {
        this.uri = uri;
        this.fsHandler = fsHandler;
    }

    protected FSNodeProvider getFSHandler()
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
    public FileTime getAccessTime() throws IOException
    {
        BasicFileAttributes attrs = this.getBasicAttributes();
        
        if (attrs==null)
        {
            return null; 
        }
            
        return attrs.lastAccessTime();
    }

    public boolean isHidden()
    {
        // Windows has a different way to hide files.
        // Use default UNIX way to indicate hidden files.
        return this.getBasename().startsWith(".");
    }

    /**
     * Is a unix style soft- or symbolic link
     * @throws IOException 
     */
    public boolean isSymbolicLink() throws IOException
    {
        BasicFileAttributes attrs = this.getBasicAttributes();
        if (attrs==null)
            return false; 
        
        return attrs.isSymbolicLink(); 
    }

    // =======================================================================
    // IO Methods
    // =======================================================================

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

        // Default way to create a file is by writing zero bytes:
        OutputStream outps = this.fsHandler.createOutputStream(this,false);
        outps.write(bytes);
        outps.close();

        return true;
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

    public FileTime getCreationTime() throws IOException
    {
        BasicFileAttributes attrs = this.getBasicAttributes();

        if (attrs == null)
        {
            return null; 
        }
        
        return attrs.creationTime();
    }

    public long getModificationTimeMillies() throws IOException
    {
        FileTime time=getModificationTime();
        if (time==null)
        {
            return -1;
        }
        else
        {
            return time.toMillis();
        }
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

    public long getFileSize() throws IOException
    {
        BasicFileAttributes attrs = this.getBasicAttributes();

        if (attrs == null)
            return 0;

        return attrs.size();
    }

    // =======================================================================
    // InputStream/OutputStream 
    // =======================================================================
    
    public InputStream createInputStream() throws IOException
    {
        return fsHandler.createInputStream(this);
    }
    
    public OutputStream createOutputStream(boolean append) throws IOException
    {
        return fsHandler.createOutputStream(this,append);
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
    public abstract boolean delete(LinkOption... linkOptions) throws IOException;

    // === Directory methods === //

    /** Return contents of directory. */
    public abstract String[] list() throws IOException;

    /** Return contents of directory as FSNode objects . */
    public abstract FSNode[] listNodes() throws IOException;

    /** Create last path element as (sub)directory, parent directory must exist. */
    public abstract boolean mkdir() throws IOException;

    /** Create full directory path. */
    public abstract boolean mkdirs() throws IOException;

    public abstract BasicFileAttributes getBasicAttributes(LinkOption... linkOptions) throws IOException;



}
