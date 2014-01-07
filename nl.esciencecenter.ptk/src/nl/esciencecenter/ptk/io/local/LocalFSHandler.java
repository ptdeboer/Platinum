package nl.esciencecenter.ptk.io.local;

import java.io.IOException;
import java.net.URI;

import nl.esciencecenter.ptk.io.FSHandler;
import nl.esciencecenter.ptk.io.FSNode;
import nl.esciencecenter.ptk.io.RandomReader;
import nl.esciencecenter.ptk.io.RandomWriter;

public class LocalFSHandler extends FSHandler
{
    private static Object instanceMutex=new Object(); 
    
    private static LocalFSHandler instance=null;
    
    public static LocalFSHandler getDefault()
    {
        synchronized(instanceMutex)
        {
            if (instance==null)
            {
                instance=new LocalFSHandler(); 
            }
        
            return instance;
        }
    }

    public String getScheme()
    {
        return "file"; 
    }

    @Override
    public FSNode newFSNode(URI uri)
    {
        return new LocalFSNode(this,uri);
    }

    @Override
    public RandomReader createRandomReader(FSNode node) throws IOException
    {
        return new LocalFSReader((LocalFSNode)node); 
    }

    @Override
    public RandomWriter createRandomWriter(FSNode node) throws IOException
    {
        return new LocalFSWriter((LocalFSNode)node); 
    }

}
