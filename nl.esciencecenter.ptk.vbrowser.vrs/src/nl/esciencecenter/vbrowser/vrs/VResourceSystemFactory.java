package nl.esciencecenter.vbrowser.vrs;

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public interface VResourceSystemFactory
{
    public String[] getSchemes();

    /**
     * Create unique resource system ID. <br>
     * Equivalent ResourceSystems, for example FileSystems on the same server,
     * should have an equal resource system ID. A file system might return a
     * different ID per mounted file system or local drive.
     * 
     * @param vrl - VRL to deduce resource system from.
     * @return - unique resource system ID for this ID.
     */
    public String createResourceSystemId(VRL vrl);

    public ResourceSystemInfo updateResourceInfo(VRSContext context, ResourceSystemInfo resourceSystemInfo, VRL vrl);

    /**
     * Create new resource System. This system will be cached and re-used  by the VRS if the
     * ResourceSystemID matches.
     */
    public VResourceSystem createResourceSystemFor(VRSContext context, ResourceSystemInfo info, VRL vrl) throws VrsException;

}
