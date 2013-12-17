package nl.esciencecenter.vbrowser.vrs.node;

import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public abstract class VResourceSystemNode extends VPathNode implements VResourceSystem
{
    protected VRSContext vrsContext=null;
    
    protected VResourceSystemNode(VRSContext context,VRL serverVrl)
    {
        super(null,serverVrl);  
        this.resourceSystem=this; 
        this.vrsContext=context; 
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
    
    protected VRSContext getVRSContext()
    {
        return vrsContext; 
    }
 
}
