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

package nl.esciencecenter.vbrowser.vrs.localfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.io.local.LocalFSNode;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsAccessDeniedException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.io.VStreamAccessable;
import nl.esciencecenter.vbrowser.vrs.node.FileAttributes;
import nl.esciencecenter.vbrowser.vrs.node.VFSPathNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class LocalFSPathNode extends VFSPathNode implements VStreamAccessable
{
    private static final ClassLogger logger=ClassLogger.getLogger(LocalFSPathNode.class);
    
    private LocalFileSystem localfs;
    
    LocalFSNode fsNode;

    protected LocalFSPathNode(LocalFileSystem fileSystem, LocalFSNode node)
    {
        super(fileSystem,new VRL(node.getURI()));
        this.localfs=fileSystem;
        this.fsNode=node; 
    }

    @Override
    public boolean isRoot()
    {
        return fsNode.isRoot(); 
    }

    @Override
    public boolean isDir(LinkOption... linkOptions)
    {
        return fsNode.isDirectory(linkOptions); 
    }

    @Override
    public boolean isFile(LinkOption... linkOptions)
    {
        return fsNode.isFile(linkOptions);
    }

    @Override
    public boolean exists(LinkOption... linkOptions)
    {
        return fsNode.exists(linkOptions); 
    }
    @Override
    public List<VFSPath> list() throws VrsException
    {
        logger.debugPrintf("list():%s\n",this);
        
        try
        {
            LocalFSNode nodes[];
            nodes = fsNode.listNodes();
            ArrayList<VFSPath> pathNodes=new ArrayList<VFSPath>(); 
            for (LocalFSNode node:nodes)
            {
                logger.debugPrintf(" - adding:%s\n",node);
                pathNodes.add(new LocalFSPathNode(localfs,node));
            }
            return pathNodes;

        }
        catch (java.nio.file.AccessDeniedException e)
        {
            throw new VrsAccessDeniedException(e.getMessage(),e);
        }
        catch (IOException e)
        {
            throw LocalFileSystem.convertException("Failed to list path:"+getVRL(),e);  
        }  
    }

    @Override
    public FileAttributes getFileAttributes(LinkOption... linkOptions) throws VrsException
    {
        try
        {
            return new LocalFileAttributes(fsNode.getBasicAttributes(linkOptions));
        }
        catch (IOException e)
        {
            throw LocalFileSystem.convertException("Failed to get FileAttributes from:"+getVRL(),e);  
        }
    }

    public OutputStream createOutputStream(boolean append) throws VrsException
    {
        try
        {
            return fsNode.createOutputStream(append);
        }
        catch (IOException e)
        {
            throw LocalFileSystem.convertException("Failed to create OutputStream from:"+getVRL(),e);  
        }
    }

    public InputStream createInputStream() throws VrsException
    {
        try
        {
            return fsNode.createInputStream();
        }
        catch (IOException e)
        {
            throw LocalFileSystem.convertException("Failed to create InputStream from:"+getVRL(),e);  
        }
    }

    @Override
    public boolean delete() throws VrsException
    {
        delete(false);
        return true; // delete is applicable. 
    }

    @Override
    public boolean createFile(boolean ignoreExisting) throws VrsException
    {
        try
        {
            if (ignoreExisting && exists() )
                throw new VrsException("File already exists:"+getVRL()); 
            
            fsNode.create();
        }
        catch (IOException e)
        {
            throw LocalFileSystem.convertException("Failed to create file:"+getVRL(),e);  
        } 
        
        return true; 
    }

    @Override
    public boolean mkdir(boolean ignoreExisting) throws VrsException
    {
        try
        {
            fsNode.mkdir();
        }
        catch (IOException e)
        {
            throw LocalFileSystem.convertException("Failed to create directory:"+getVRL(),e);  
        } 
        
        return true; 
    }

    @Override
    public boolean delete(boolean recurse,LinkOption... linkOptions) throws VrsException
    {
        try
        {
            if (fsNode.isDirectory(linkOptions))
            {
                if (recurse)
                {
                    String[] nodes = fsNode.list(); 
                    if ((nodes!=null) && (nodes.length>0))
                    {
                        throw new VrsException("Recursive Delete not yet supported"); 
                    }
                }
            }
        
            fsNode.delete(linkOptions);
        }
        catch (IOException e)
        {
            throw LocalFileSystem.convertException("Couldn't delete:"+getVRL(),e); 
        }
        
        return true; 
    }

    @Override
    public VFSPath renameTo(VFSPath other) throws VrsException
    {
        try
        {
            String newPath=fsNode.renameTo(other.getVRL().getPath());
            return this.resolvePath(newPath);
        }
        catch (IOException e)
        {
            throw LocalFileSystem.convertException("Couldn't rename:"+getVRL(),e); 
        } 
            
    }

    @Override
    public boolean sync()
    {
        return false;
    }

    @Override
    public long getLength(LinkOption... linkOptions) throws VrsException
    {
        try
        {
            return fsNode.getFileSize();
        }
        catch (IOException e)
        {
            throw LocalFileSystem.convertException("Couldn't get file size of:"+getVRL(),e); 
        } 
    }
}
