package nl.esciencecenter.vbrowser.vrs.infors;

import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import org.junit.Assert;
import org.junit.Test;

public class Test_InfoRS {
    public static VRSClient initTestClient() {
        VRSContext vrsCtx = new VRSContext();
        VRSClient vrsClient = new VRSClient(vrsCtx);
        return vrsClient;
    }

    @Test
    public void creatInfoRS() throws Exception {
        VRSClient vrsClient = Test_InfoRS.initTestClient();
        VResourceSystem infoRs = vrsClient.getVResourceSystemFor(new VRL("info:/"));
        Assert.assertNotNull(infoRs);
    }

}
