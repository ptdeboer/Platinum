package nl.esciencecenter.vbrowser.vrs.infors;

import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class InfoRSFactory implements VResourceSystemFactory
{
    public InfoRSFactory() throws VrsException
    {
    }

    @Override
    public String[] getSchemes()
    {
        return new String[]
        { InfoConstants.INFO_SCHEME };
    }

    @Override
    public String createResourceSystemId(VRL vrl)
    {
        // only one local infors per registry;
        return InfoConstants.INFO_SCHEME + ":0";
    }

    @Override
    public VResourceSystem createResourceSystemFor(VRSContext context, ResourceSystemInfo info, VRL vrl) throws VrsException
    {
        if (StringUtil.equals(InfoConstants.INFO_SCHEME, vrl.getScheme()) == false)
        {
            throw new VrsException("Only support 'info' scheme:" + vrl);
        }

        return new InfoRS(context);
    }

    @Override
    public ResourceSystemInfo updateResourceInfo(VRSContext context, ResourceSystemInfo resourceSystemInfo, VRL vrl)
    {
        // Nothing to be updated.
        return resourceSystemInfo;
    }

}
