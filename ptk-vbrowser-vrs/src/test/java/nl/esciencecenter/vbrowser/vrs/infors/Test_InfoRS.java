package nl.esciencecenter.vbrowser.vrs.infors;

import org.junit.Test;

import junit.framework.Assert;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class Test_InfoRS
{   
    public static VRSClient initTestClient()
    {
        VRSContext vrsCtx=new VRSContext(); 
        VRSClient vrsClient=new VRSClient(vrsCtx);
        return vrsClient;
        
    }
    
    @Test
    public void creatInfoRS() throws Exception
    {
        VRSClient vrsClient=Test_InfoRS.initTestClient(); 
    
        VResourceSystem infoRs = vrsClient.getVResourceSystemFor(new VRL("info:/")); 
        
        Assert.assertNotNull(infoRs);
        
    }
    
        
}
