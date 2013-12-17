package nl.esciencecenter.vbrowser.vrs;

import java.util.Properties;

import nl.esciencecenter.vbrowser.vrs.registry.Registry;

public class VRSContext
{
    protected Registry registry;
    
    protected VRSProperties vrsProperties; 
    
    protected VRSContext(Properties props)
    {
        // default Static Registry ! 
        this.registry=Registry.getInstance(); 
        this.vrsProperties=new VRSProperties(props); 
    }
    
    protected Registry getRegistry()
    {
        return registry;  
    }
    
    public VRSProperties getProperties()
    {
        return this.vrsProperties; 
    }

}
