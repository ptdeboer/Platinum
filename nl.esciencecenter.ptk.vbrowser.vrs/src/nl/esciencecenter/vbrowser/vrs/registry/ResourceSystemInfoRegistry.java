package nl.esciencecenter.vbrowser.vrs.registry;

import java.util.Hashtable;
import java.util.Map;

import nl.esciencecenter.vbrowser.vrs.VRSContext;

public class ResourceSystemInfoRegistry
{
    /** 
     * Owner Object of this registry.  
     */ 
    private VRSContext vrsContext; 
    
    private Map<String,ResourceSystemInfo> resourceInfos=new Hashtable<String,ResourceSystemInfo>(); 
    
    public ResourceSystemInfoRegistry(VRSContext vrsContext)
    {
        this.vrsContext=vrsContext;
    }
    
    public void putInfo(ResourceSystemInfo info)
    {
        synchronized(resourceInfos)
        {
            // always update ID. 
            resourceInfos.put(info.getID(),info);
        }
    }
    
    public ResourceSystemInfo getInfo(String id)
    {
        synchronized(resourceInfos)
        {
            return resourceInfos.get(id);
        }
    }

}
