package nl.esciencecenter.vbrowser.vrs.localfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.io.FSNode;
import nl.esciencecenter.ptk.io.local.LocalFSNode;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VFileSystem;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsAccessDeniedException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsIOException;
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
    public boolean isDir()
    {
        return fsNode.isDirectory(); 
    }

    @Override
    public boolean isFile()
    {
        return fsNode.isFile();
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
            throw new VrsException(e.getMessage(),e);
        }  
    }

    @Override
    public FileAttributes getFileAttributes() throws VrsException
    {
        try
        {
            return new LocalFileAttributes(fsNode.getBasicAttributes());
        }
        catch (IOException e)
        {
           throw new VrsException(e.getMessage(),e); 
        }
    }

    public OutputStream createOutputStream() throws VrsException
    {
        try
        {
            return fsNode.createOutputStream();
        }
        catch (IOException e)
        {
            throw new VrsIOException(e); 
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
            throw new VrsIOException(e);
        }
    }
    
}
