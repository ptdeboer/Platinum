package nl.esciencecenter.vbrowser.vrs.localfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.esciencecenter.ptk.io.FSUtil;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsIOException;
import nl.esciencecenter.vbrowser.vrs.io.VStreamCreator;
import nl.esciencecenter.vbrowser.vrs.node.VFSPathNode;
import nl.esciencecenter.vbrowser.vrs.node.VFileSystemNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class LocalFileSystem extends VFileSystemNode implements VStreamCreator
{
    private FSUtil fsUtil; 
    
    public LocalFileSystem(VRSContext context) throws VrsException
    {
        super(context,new VRL("file:/"));
        fsUtil=new FSUtil();
    }

    @Override
    protected LocalFSPathNode createVFSNode(VRL vrl) throws VrsException
    {
        
        try
        {
            return new LocalFSPathNode(this,fsUtil.newLocalFSNode(vrl.getPath()));
        }
        catch (IOException e)
        {
            throw new VrsException(e.getMessage(),e);
        }
    }

    @Override
    public InputStream createInputStream(VRL vrl) throws VrsException
    {
        return createVFSNode(vrl).createInputStream();  
    }

    @Override
    public OutputStream createOutputStream(VRL vrl) throws VrsException
    {
        return createVFSNode(vrl).createOutputStream();  
    }
    

}
