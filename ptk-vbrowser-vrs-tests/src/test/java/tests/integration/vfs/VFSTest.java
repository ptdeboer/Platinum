package tests.integration.vfs;

import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.registry.Registry;

public class VFSTest
{
    // =========
    // Static
    // =========

    protected static VRSContext staticContext;

    public static VRSContext getStaticTestContext()
    {
        if (staticContext==null)
        {
            staticContext=new VRSContext();
        }
        return staticContext;
    }

    public static Registry getStaticRegistry()
    {
        return getStaticTestContext().getRegistry();
    }

}
