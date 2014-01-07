package nl.esciencecenter.ptk.io;

import java.io.IOException;

public abstract class FSHandler
{
    protected FSHandler()
    {
        
    }
    
    public abstract String getScheme(); 
    
    public abstract FSNode newFSNode(java.net.URI uri);

    public abstract RandomReader createRandomReader(FSNode node) throws IOException;

    public abstract RandomWriter createRandomWriter(FSNode node) throws IOException;
    
}
