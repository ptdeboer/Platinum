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
import java.nio.file.LinkOption;
import java.util.List;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.io.local.LocalFSHandler;
import nl.esciencecenter.ptk.io.local.LocalFSNode;
import nl.esciencecenter.ptk.net.URIFactory;
import nl.esciencecenter.ptk.net.URIUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;

/**
 * Global File System utils and resource loaders. <br>
 * Used to access the local file system.
 */
public class FSUtil implements ResourceProvider
{
    private static final ClassLogger logger=ClassLogger.getLogger(FSUtil.class);
    
    public static final String ENCODING_UTF8 = "UTF8";

    public static final String ENCODING_ASCII = "ASCII";

    private static FSUtil instance = null;

    public static FSUtil getDefault()
    {
        if (instance == null)
            instance = new FSUtil();

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
         * @see LinkOption.NOFOLLOW_LINKS 
         */ 
        public boolean defaultFollowLinks = true;
        
    }

    // ========================================================================
    // Instance
    // ========================================================================

    protected URI userHome;

    protected URI workingDir;

    protected URI tmpDir;

    protected FSOptions fsOptions = new FSOptions();

    private LocalFSHandler localFSHandler;

    public FSUtil()
    {
        init();
    }

    private void init()
    {
        this.localFSHandler = LocalFSHandler.getDefault();

        try
        {
            this.userHome = new java.io.File(GlobalProperties.getGlobalUserHome()).toURI();
            this.workingDir = new java.io.File(GlobalProperties.getGlobalUserHome()).toURI();
            this.tmpDir = new java.io.File(GlobalProperties.getGlobalTempDir()).toURI();
        }
        catch (Throwable e)
        {
            logger.logException(ClassLogger.FATAL, e, "Initialization Exception:%s\n", e);
        }
    }

    /**
     * Check syntax and decode optional (relative) URL or path to an absolute
     * normalized path. If an exception occurs (syntax error) the path is
     * returned "as is" ! Use resolveURI(path) to resolve to an absolute and
     * normalized URI.
     * 
     * @throws FileURISyntaxException
     *             if the path contains invalid characters.
     */
    public String resolvePath(String path) throws FileURISyntaxException
    {
        try
        {
            return resolvePathURI(path).getPath();
        }
        catch (URISyntaxException e)
        {
            throw new FileURISyntaxException(e.getMessage(), path, e);
        }
    }

    /**
     * Resolve relative path to absolute URI.
     */
    public URI resolvePathURI(String path) throws URISyntaxException
    {
        if ((this.fsOptions.resolveTilde) && (path != null) && path.contains("~"))
        {
            String homePath = URIFactory.uripath(userHome.getPath());
            path = path.replace("~", homePath);
        }

        return URIUtil.resolvePathURI(workingDir, path);
    }

    public boolean existsPath(String path, LinkOption... linkOptions) throws IOException
    {
        return newFSNode(resolvePath(path)).exists(linkOptions);
    }

    /**
     * Simple Copy File uses URIs to ensure absolute and normalized Paths.
     */
    public void copyFile(URI source, URI destination) throws IOException
    {
        InputStream finput = newFSNode(source).createInputStream();
        OutputStream foutput = newFSNode(destination).createOutputStream(false);

        IOUtil.copyStreams(finput, foutput, false);

        autoClose(finput); 
        autoClose(foutput); 
        return;
    }

    private boolean autoClose(InputStream finput)
    {
        if (finput==null)
        {
            return false; 
        }
        
        try
        {
            finput.close();
            return true; 
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    private boolean autoClose(OutputStream foutput)
    {
        if (foutput==null)
        {
            return false; 
        }
        
        try
        {
            foutput.close();
            return true; 
        }
        catch (Exception e)
        {
            return false; 
        }
    }

    /**
     * Checks whether paths exists and is a file. If the filePath contains
     * invalid characters, this method will also return false.
     */
    public boolean existsFile(String filePath, boolean mustBeFileType, LinkOption... linkOptions)
    {
        if (filePath == null)
        {
            return false;
        }

        try
        {
            FSNode file = newFSNode(filePath);
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
     * Checks whether directoryPath paths exists and is a directory. If the
     * directory path contains invalid characters the method will also return
     * false.
     */
    public boolean existsDir(String directoryPath, LinkOption... linkOptions)
    {
        if (directoryPath == null)
        {
            return false;
        }

        try
        {
            FSNode file = newFSNode(directoryPath);
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

    public FSNode newFSNode(String path) throws IOException
    {
        try
        {
            return localFSHandler.newFSNode(resolvePathURI(path));
        }
        catch (URISyntaxException e)
        {
            throw new FileURISyntaxException(e.getMessage(), path, e);
        }
    }

    /**
     * Return new FileSystem Node specified by the URI. Currently only local
     * files are supported.
     * 
     * @param uri
     *            - uri of the file.
     * @return Lofical FileSystem Node.
     */
    public FSNode newFSNode(URI uri)
    {
        return new LocalFSNode(localFSHandler, uri);
    }

    /**
     * Return new local file node specified by the path.
     * 
     * @param path
     *            - the relative,logical or absolute path to resolve on the
     *            local file system.
     */
    public LocalFSNode newLocalFSNode(URI localFileURI)
    {
        if (localFileURI.isAbsolute())
        {
            return new LocalFSNode(LocalFSHandler.getDefault(), localFileURI);
        }
        else
        {
            return new LocalFSNode(localFSHandler, workingDir.resolve(localFileURI));
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
        FSNode file;
        try
        {
            file = newFSNode(resolvePathURI(dirPath));
        }
        catch (URISyntaxException e)
        {
            throw new FileURISyntaxException(e.getMessage(), dirPath, e);
        }

        if (file.exists(linkOptions) == false)
            return null;

        if (file.isDirectory(linkOptions) == false)
            return null;

        String strs[] = file.list();
        if ((strs == null) || (strs.length <= 0))
            return null;

        // sanitize:
        for (int i = 0; i < strs.length; i++)
            strs[i] = resolvePath(dirPath + "/" + strs[i]);

        return strs;
    }

    public void deleteFile(String filename) throws IOException, FileURISyntaxException
    {
        FSNode file = newFSNode(filename);
        if (file.exists(LinkOption.NOFOLLOW_LINKS) == false)
            return;
        file.delete();
    }

    /**
     * Open local file and return InputStream to read from.
     * 
     * @param filename
     *            - relative or absolute file path (resolves to absolute path on
     *            local filesystem)
     * @throws URISyntaxException
     * @throws IOException
     */
    public InputStream getInputStream(String filename) throws IOException, FileURISyntaxException
    {
        return newFSNode(filename).createInputStream();
    }

    /**
     * Open local file and return OutputStream to write to. The default
     * implementation is to creat a new File if it doesn't exists or replace an
     * existing file with the new contents if it exists.
     * 
     * @param filename
     *            - relative or absolute file path (resolves to absolute path on
     *            local fileystem)
     * @throws URISyntaxException
     * @throws IOException
     */
    public OutputStream createOutputStream(String filename) throws IOException, FileURISyntaxException
    {
        return newFSNode(filename).createOutputStream(false);
    }

    public RandomReader createRandomReader(FSNode node) throws IOException
    {
        return node.getFSHandler().createRandomReader(node);
    }

    public RandomWriter createRandomWriter(FSNode node) throws IOException
    {
        return node.getFSHandler().createRandomWriter(node);
    }

    /**
     * Read file and return as UTF8 String.
     * 
     * @param filename
     *            - path to resolve and read.
     * @throws URISyntaxException
     */
    public String readText(String filename) throws IOException, FileURISyntaxException
    {
        return readText(filename, ENCODING_UTF8);
    }

    public String readText(String filename, String encoding) throws IOException, FileURISyntaxException
    {
        return readText(filename, encoding, 1024 * 1024);
    }

    /**
     * Read file and return as String. Provide optional encoding (can be null
     * for default). Limit size to maxSize
     * 
     * @param filename
     *            - absolute of relative filepath
     * @param enconding
     *            - optional encoding. Use null for default.
     * @param maxSize
     *            - limit size of number of bytes read (not the String size).
     * @throws FileURISyntaxException
     *             - when the filename contains invalid characters.
     */
    public String readText(String filename, String encoding, int maxSize) throws IOException, FileURISyntaxException
    {
        if (encoding == null)
        {
            encoding = ENCODING_UTF8;
        }
        
        FSNode file = newFSNode(filename);
        int len = (int) file.getFileSize();
        if (len > maxSize)
        {
            len = maxSize;
        }
        
        InputStream finps = file.createInputStream();
        try
        {
            byte buffer[] = new byte[len + 1];
    
            int numRead = IOUtil.syncReadBytes(finps, 0, buffer, 0, len);
            // truncate buffer in the case of a read error:
            buffer[numRead] = 0;
            return new String(buffer, encoding);
        }
        finally
        {
            autoClose(finps);
        }
    }

    public void writeText(String path, String txt) throws IOException, FileURISyntaxException
    {
        writeText(path, txt, ENCODING_UTF8);
    }

    public void writeText(String filename, String txt, String encoding) throws IOException, FileURISyntaxException
    {
        if (encoding == null)
        {
            encoding = ENCODING_UTF8;
        }
        
        FSNode file = newFSNode(filename);

        OutputStream foutps = file.createOutputStream(false);
        int len=0; 
        
        try
        {
            byte bytes[] = txt.getBytes(encoding);
            len = bytes.length;
            foutps.write(bytes);
            // close and flush
            foutps.flush(); 
            foutps.close(); 
            foutps=null; // close successful  
            
            long fileLen = file.getFileSize();
            
            if (len != fileLen)
            {
                logger.warnPrintf("File NOT truncated: After writing %d byte to '%s', file length is:%d!\n", filename, len, fileLen);
            }
        }
        finally
        {
            autoClose(foutps); 
        }


        return;
    }

    public FSNode mkdir(String path) throws IOException, FileURISyntaxException
    {
        FSNode dir = this.newFSNode(path);
        dir.mkdir();
        return dir;
    }

    public FSNode mkdirs(String path) throws IOException, FileURISyntaxException
    {
        FSNode dir = this.newFSNode(path);
        dir.mkdirs();
        return dir;
    }

    public LocalFSNode getLocalTempDir()
    {
        return this.newLocalFSNode(this.tmpDir);
    }

    public LocalFSNode getWorkingDir()
    {
        return this.newLocalFSNode(this.workingDir);
    }

    public URI getUserHome()
    {
        return userHome;
    }

    public FSNode getUserHomeDir()
    {
        return newFSNode(this.userHome);
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
     * Returns new directory FSNode Object. Path might not exist.
     * 
     * @param dirUri
     *            - location of new Directory
     * @return - new Local Directory object.
     */
    public LocalFSNode newLocalDir(URI dirUri)
    {
        LocalFSNode dir = this.newLocalFSNode(dirUri);
        return dir;
    }

    public LocalFSNode newLocalFSNode(String fileUri) throws FileURISyntaxException
    {
        try
        {
            return newLocalFSNode(resolvePathURI(fileUri));
        }
        catch (URISyntaxException e)
        {
            throw new FileURISyntaxException(e.getMessage(), fileUri, e);
        }
    }

    public void deleteDirectoryContents(URI uri, boolean recursive) throws IOException
    {
        LocalFSNode node = newLocalDir(uri);
        if (node.exists() == false)
            throw new FileNotFoundException("Directory does not exist:" + uri);

        deleteDirectoryContents(node, recursive);
        return;
    }

    public void deleteDirectoryContents(FSNode dirNode, boolean recursive) throws IOException
    {
        FSNode[] nodes = dirNode.listNodes();
        for (FSNode node : nodes)
        {
            if (node.isDirectory() && recursive)
            {
                deleteDirectoryContents(node, recursive);
            }
            node.delete();
        }
    }

    public void delete(FSNode node, boolean recursive) throws IOException
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
            FSNode node = newFSNode(uri);
            // should trigger file system check on path.
            boolean exists = node.exists();
            if (reasonH != null)
            {
                reasonH.value = "File path is ok. Exists=" + exists;
            }
            return true;
        }
        catch (URISyntaxException ex)
        {
            if (reasonH != null)
            {
                reasonH.value = "Syntax Error:" + ex.getMessage() + ", input=" + ex.getInput();
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

    @Override
    public OutputStream createOutputStream(URI uri) throws IOException
    {
        if (isLocalFSUri(uri))
        {
            return localFSHandler.newFSNode(uri).createOutputStream(false);
        }
        else
        {
            // use default URL method !
            return uri.toURL().openConnection().getOutputStream();
        }

    }

    @Override
    public InputStream createInputStream(URI uri) throws IOException
    {
        if (isLocalFSUri(uri))
        {
            return localFSHandler.newFSNode(uri).createInputStream();
        }
        else
        {
            // use default URL method !
            return uri.toURL().openConnection().getInputStream();
        }
    }

    private boolean isLocalFSUri(URI uri)
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
    public RandomReader createRandomReader(URI uri) throws IOException
    {
        return localFSHandler.createRandomReader(newFSNode(uri));
    }

    @Override
    public RandomWriter createRandomWriter(URI uri) throws IOException
    {
        return localFSHandler.createRandomWriter(newFSNode(uri));
    }

    public List<FSNode> listRoots()
    {
        return localFSHandler.listLocalRoots();
    }

}
