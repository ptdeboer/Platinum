package nl.esciencecenter.vbrowser.vrs.util;

import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.VRSContext;

public class VRSResourceLoader extends ResourceLoader
{
    protected VRSClient vrsClient;  
    
    public VRSResourceLoader(VRSContext vrsContext)
    {
        this.vrsClient=new VRSClient(vrsContext);
        
        init(new VRSResourceProvider(vrsClient),null);
        
    }

    
}
