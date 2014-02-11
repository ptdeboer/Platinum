package nl.esciencecenter.vbrowser.vrs.util;

import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.VRSContext;

public class VRSUtil extends ResourceLoader
{
    public static ResourceLoader createVRSResourceLoader(VRSContext vrsContext)
    {
        VRSClient vrsClient=new VRSClient(vrsContext);
        VRSResourceProvider prov=new VRSResourceProvider(vrsClient); 
        ResourceLoader loader = new ResourceLoader(prov,null); 
        return loader; 
    }

    
}
