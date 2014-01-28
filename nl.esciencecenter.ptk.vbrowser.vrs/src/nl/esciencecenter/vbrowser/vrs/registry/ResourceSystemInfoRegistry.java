package nl.esciencecenter.vbrowser.vrs.registry;

import java.util.Hashtable;
import java.util.Map;

import nl.esciencecenter.vbrowser.vrs.VRSContext;

public class ResourceSystemInfoRegistry
{
    /** Owner Object */ 
    private VRSContext vrsContext; 
    
    private Map<String,ResourceSystemInfo> resourceInfos=new Hashtable<String,ResourceSystemInfo>(); 
    
    public ResourceSystemInfoRegistry(VRSContext vrsContext)
    {
        this.vrsContext=vrsContext;
    }
    
    public void putInfo(String id, ResourceSystemInfo info)
    {
        resourceInfos.put(id, info);
    }
    
    public ResourceSystemInfo getInfo(String id)
    {
        return resourceInfos.get(id);
    }

}
