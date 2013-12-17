package nl.esciencecenter.vbrowser.vrs.node;

import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public abstract class VResourceSystemNode extends VPathNode implements VResourceSystem
{
    protected VResourceSystemNode(VRL serverVrl)
    {
        super(serverVrl,null);  
        this.resourceSystem=this; 
    }

    @Override
    public VRL getServerVRL()
    {
        return this.getVRL(); 
    }
    
    @Override
    public VRL resolveVRL(String path) throws VrsException
    {
        return this.getServerVRL().resolvePath(path); 
    }
    
 
}
