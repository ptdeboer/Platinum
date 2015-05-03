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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.io.exceptions.FileURISyntaxException;
import nl.esciencecenter.ptk.net.URIFactory;
import nl.esciencecenter.ptk.net.URIUtil;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.ptk.util.logging.PLogger;

/**
 * File System util and resource provoder work with local file paths and URIs.
 * Pathnames are resolved to absolute paths and normalizes to URI style paths.<br>
 * <br>
 * For example: <code>'file:///C:/Users/Bob/'</code>
 */
public class FSUtil implements ResourceProvider, FSPathProvider
{
    private static final PLogger logger = PLogger.getLogger(FSUtil.class);

    public static final String ENCODING_UTF8 = "UTF8";

    public static final String ENCODING_ASCII = "ASCII";

    private static FSUtil instance = null;

    public static FSUtil getDefault()
    {
        if (instance == null)
        {
            instance = new FSUtil();
        }

        return instance;
    }

    public static class FSOptions
    {
        /**
         * Resolve '~' to user home dir.
         */
        public boolean resolveTilde = true;

        /**
         * Whether to follow links.
         * 
         * @see LinkOption.NOFOLLOW_LINKS
         */
        public LinkOption linkOption = null;

    }

    // ========================================================================
    // Class Util methods
    // ========================================================================

    public static int toUnixFileMode(Set<PosixFilePermission> perms)
    {
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

    public static Set<PosixFilePermission> fromUnixFileMode(int mode)
    {
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

    protected URI userHome;

    protected URI workingDir;

    protected URI tmpDir;

    protected FSOptions fsOptions = new FSOptions();

    public FSUtil()
    {
        init();
    }

    private void init()
    {
        FileSystem fs = FileSystems.getDefault();
        
        try
        {
            this.userHome = fs.getPath(GlobalProperties.getGlobalUserHome()).toUri();
            this.workingDir = fs.getPath(GlobalProperties.getGlobalUserDir()).toUri();
            this.tmpDir = fs.getPath(GlobalProperties.getGlobalTempDir()).toUri();
        }
        catch (Throwable e)
        {
            logger.logException(PLogger.FATAL, e, "Initialization Exception:%s\n", e);
        }
    }

    /**
     * Check syntax and decode optional (relative) URL or path to an absolute normalized path. If an exception occurs
     * (syntax error) the path is returned "as is" ! Use resolveURI(path) to resolve to an absolute and normalized URI.
     * @throws IOException 
     */
    public FSPath resolvePath(String path) throws IOException
    {
        return newFSPath(path);
    }

    public FSPath resolve(URI uri) throws IOException
    {
        return newFSPath(uri);
    }

    /**
     * Resolve relative path to absolute URI.
     */
    public URI resolvePathURI(String path) throws FileURISyntaxException
    {
        try
        {
            return URIUtil.resolvePath(workingDir, userHome, fsOptions.resolveTilde, path);
        }
        catch (URISyntaxException e)
        {
            throw new FileURISyntaxException(e.getMessage(), path, e);
        }
    }

    public String[] getSchemes()
    {
        return new String[] {
            "file"
        };
    }

    public boolean existsPath(String path, LinkOption... linkOptions) throws IOException
    {
        return newFSPath(resolvePathURI(path)).exists(linkOptions);
    }

    /**
     * Simple Copy File uses URIs to ensure absolute and normalized Paths.
     */
    public long copyFile(URI source, URI destination) throws IOException
    {
        long num;
        // Create
        try(InputStream finput = createInputStream(newFSPath(source)))
        {
            try (OutputStream foutput = createOutputStream(newFSPath(destination), false))
            {
                // Copy
                num=IOUtil.copyStreams(finput, foutput, false);
            }
        }
        return num;
    }

    /**
     * Checks whether paths exists and is a file. If the filePath contains invalid characters, this method will also
     * return false.
     */
    public boolean existsFile(String filePath, boolean mustBeFileType, LinkOption... linkOptions)
    {
        if (filePath == null)
        {
            return false;
        }

        try
        {
            FSPath file = newFSPath(filePath);
            if (file.exists(linkOptions) == false)
            {
                return false;
            }

            if (mustBeFileType)
            {
                if (file.isFile(linkOptions))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return true;
            }
        }
        catch (FileURISyntaxException e)
        {
            return false;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    /**
     * Checks whether directoryPath paths exists and is a directory. If the directory path contains invalid characters
     * the method will also return false.
     */
    public boolean existsDir(String directoryPath, LinkOption... linkOptions)
    {
        if (directoryPath == null)
        {
            return false;
        }

        try
        {
            FSPath file = newFSPath(directoryPath);
            if (file.exists(linkOptions) == false)
                return false;

            if (file.isDirectory(linkOptions))
                return true;
        }
        catch (FileURISyntaxException e)
        {
            return false;
        }
        catch (IOException e)
        {
            return false;
        }

        return false;
    }

    /**
     * Return new FileSystem Node specified by the URI. Currently only local files are supported.
     * 
     * @param uri
     *            - uri of the file. Uri can be relative here.
     * @return new resolved FSPath
     */
    public FSPath newFSPath(URI uri) throws IOException
    {
        try
        {
            if (uri.isAbsolute() == false)
            {
                uri = URIUtil.resolvePath(this.workingDir, this.userHome, true, uri.getPath());
            }
        }
        catch (URISyntaxException e)
        {
            throw new FileURISyntaxException("Invalid (file)URI:" + uri, uri.getPath(), e);
        }

        FileSystem fs = FileSystems.getDefault();
        if (GlobalProperties.isWindows())
        {
            String dosPath = new URIFactory(uri).getDosPath();
            return new FSPath(this, fs.getPath(dosPath));
        }
        else
        {
            return new FSPath(this, fs.getPath(uri.getPath()));
        }
    }

    /**
     * List directory: returns (URI) normalized paths.
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    public String[] list(String dirPath, LinkOption... linkOptions) throws IOException, FileURISyntaxException
    {
        FSPath file = newFSPath(resolvePathURI(dirPath));

        if (file.exists(linkOptions) == false)
        {
            return null;
        }

        if (file.isDirectory(linkOptions) == false)
        {
            return null;
        }

        String strs[] = file.list();
        if ((strs == null) || (strs.length <= 0))
        {
            return null;
        }

        // sanitize:
        for (int i = 0; i < strs.length; i++)
        {
            strs[i] = resolvePath(dirPath + "/" + strs[i]).getPathname();
        }

        return strs;
    }

    public void deleteFile(String filename) throws IOException, FileURISyntaxException
    {
        FSPath file = newFSPath(filename);
        if (file.exists(LinkOption.NOFOLLOW_LINKS) == false)
            return;
        file.delete();
    }

    /**
     * Open local file and return OutputStream to write to. The default implementation is to create a new File if it
     * doesn't exists or replace an existing file with the new contents if it exists.
     * 
     * @param filename
     *            - relative or absolute file path (resolves to absolute path on local fileystem)
     * @throws IOException
     */
    public OutputStream createOutputStream(String filename) throws IOException
    {
        return createOutputStream(newFSPath(filename), false);
    }

    public FSPath mkdir(String path) throws IOException
    {
        FSPath dir = this.newFSPath(path);
        dir.mkdir();
        return dir;
    }

    public FSPath mkdirs(String path) throws IOException
    {
        FSPath dir = this.newFSPath(path);
        dir.mkdirs();
        return dir;
    }

    public FSPath getLocalTempDir() throws IOException
    {
        return this.newFSPath(this.tmpDir);
    }

    public FSPath getWorkingDir() throws IOException
    {
        return this.newFSPath(this.workingDir);
    }

    public URI getUserHome()
    {
        return userHome;
    }

    public FSPath getUserHomeDir() throws IOException
    {
        return newFSPath(this.userHome);
    }

    public URI getUserHomeURI()
    {
        return userHome;
    }

    public void setWorkingDir(URI newWorkingDir)
    {
        // check for local ?
        this.workingDir = newWorkingDir;
    }

    public URI getWorkingDirVRI()
    {
        return workingDir;
    }

    /**
     * Returns new directory FSPath Object. Path might not exist.
     * 
     * @param dirUri
     *            - location of new Directory
     * @return - new Local Directory object.
     * @throws IOException
     */
    public FSPath newLocalDir(URI dirUri) throws IOException
    {
        FSPath dir = this.newFSPath(dirUri);
        return dir;
    }

    public FSPath newFSPath(String fileUri) throws IOException
    {
        return newFSPath(resolvePathURI(fileUri));
    }

    public void deleteDirectoryContents(URI uri, boolean recursive) throws IOException
    {
        FSPath node = newLocalDir(uri);
        if (node.exists() == false)
            throw new FileNotFoundException("Directory does not exist:" + uri);

        deleteDirectoryContents(node, recursive);
        return;
    }

    public void deleteDirectoryContents(FSPath dirNode, boolean recursive) throws IOException
    {
        FSPath[] nodes = dirNode.listNodes();
        for (FSPath node : nodes)
        {
            if (node.isDirectory() && recursive)
            {
                deleteDirectoryContents(node, recursive);
            }
            node.delete();
        }
    }

    public void delete(FSPath node, boolean recursive) throws IOException
    {
        if ((node.isDirectory()) && (recursive))
        {
            this.deleteDirectoryContents(node, recursive);
        }

        node.delete();
    }

    public boolean isValidPathSyntax(String relPath, StringHolder reasonH)
    {
        if (relPath.matches(".*[!@#$%*()]+"))
        {
            if (reasonH != null)
            {
                reasonH.value = "Path contains invalid characters!";
            }
            return false;
        }

        try
        {
            URI uri = this.resolvePathURI(relPath);
            FSPath node = resolve(uri);
            // should trigger file system check on path.
            boolean exists = node.exists();
            if (reasonH != null)
            {
                reasonH.value = "File path is ok. Exists=" + exists;
            }
            return true;
        }
        catch (IOException ex)
        {
            if (reasonH != null)
            {
                reasonH.value = "Syntax Error:" + ex.getMessage() + ", path=" + relPath;
            }
            return false;
        }
    }

    public boolean hasPosixFS()
    {
        if (GlobalProperties.isWindows())
            return false;

        if (GlobalProperties.isLinux())
            return true;

        if (GlobalProperties.isMac())
            return true;

        return true;
    }

    public boolean isLocalFSUri(URI uri)
    {
        if (uri.getScheme().equalsIgnoreCase("file"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public List<FSPath> listRoots() 
    {
        ArrayList<FSPath> roots=new ArrayList<FSPath>(); 
        
        Iterator<Path> iterator = java.nio.file.FileSystems.getDefault().getRootDirectories().iterator();
        
        while(iterator.hasNext())
        {   
            Path path=iterator.next();
            roots.add(new FSPath(this,path)); 
        }
        return roots; 
    }

    /** 
     * Old code which explicitly checks drive "A:" through "Z:" 
     * @throws IOException 
     */
    public List<FSPath> listWindowsDrives(boolean skipFloppyScan) throws IOException
    {
        ArrayList<FSPath> roots = new ArrayList<FSPath>();

        // Create the A: drive whether it is mounted or not
        if (skipFloppyScan == false)
        {
            String drivestr = "A:\\";
            roots.add(this.newFSPath(drivestr)); 
        }

        // Run through all possible mount points and check
        // for their existence.
        for (char c = 'C'; c <= 'Z'; c++)
        {
            char deviceChars[] = { c, ':', '\\' };
            String deviceName = new String(deviceChars);
            FSPath device=newFSPath(deviceName); 
            
            if (device.exists())
            {
                roots.add(device);
            }
        }
        return roots;
    }

    // ==============
    // IO Methods
    // ==============


    @Override
    public InputStream createInputStream(java.net.URI uri) throws IOException
    {
        return createInputStream(resolve(uri));
    }

    @Override
    public InputStream createInputStream(FSPath node) throws IOException
    {
        return Files.newInputStream(node.path());
    }

    @Override
    public OutputStream createOutputStream(URI uri) throws IOException
    {
        if (isLocalFSUri(uri))
        {
            return createOutputStream(resolve(uri), false);
        }
        else
        {
            // use default URL method !
            return uri.toURL().openConnection().getOutputStream();
        }

    }
    public OutputStream createOutputStream(FSPath node, boolean append) throws IOException
    {
        OpenOption openOptions[];

        if (append)
        {
            openOptions = new OpenOption[4];
            openOptions[0] = StandardOpenOption.WRITE;
            openOptions[1] = StandardOpenOption.CREATE; // create if not exists
            openOptions[2] = StandardOpenOption.TRUNCATE_EXISTING;
            openOptions[3] = StandardOpenOption.APPEND;
        }
        else
        {
            openOptions = new OpenOption[3];
            openOptions[0] = StandardOpenOption.WRITE;
            openOptions[1] = StandardOpenOption.CREATE; // create if not exists
            openOptions[2] = StandardOpenOption.TRUNCATE_EXISTING;
        }

        return Files.newOutputStream(node.path(), openOptions); // OpenOptions..
    }

    @Override
    public RandomReadable createRandomReader(URI uri) throws IOException
    {
        return new FSReader(resolve(uri).path());
    }
    
    @Override
    public RandomReadable createRandomReader(FSPath node) throws IOException
    {
        return new FSReader(node._path);
    }

    @Override
    public RandomWritable createRandomWriter(FSPath node) throws IOException
    {
        return new FSWriter(node._path);
    }
    
    @Override
    public RandomWritable createRandomWriter(URI uri) throws IOException
    {
        return new FSWriter((resolve(uri)._path));
    }

    public ResourceLoader getResourceLoader()
    {
        return new ResourceLoader(this,null); 
    }

    // ============
    // Util methods
    // ============
    
    public String readText(String filename) throws IOException
    {
        URI uri=this.resolvePathURI(filename); 
        return this.getResourceLoader().readText(uri);  
    }

    public void writeText(String filePath, String text) throws IOException
    {
        URI uri=this.resolvePathURI(filePath); 
        this.getResourceLoader().writeTextTo(uri, text); 
    }

    @Override
    public LinkOption[] linkOptions()
    {
        if (this.fsOptions.linkOption==null)
        {
            return null; 
        }
        else
        {
            return new LinkOption[]{fsOptions.linkOption};  
        }
    }

}
