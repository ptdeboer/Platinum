package tests.integration.vfs;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.vbrowser.vrs.VRS;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.Registry;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Test configuration settings.
 *
 * @author P.T. de Boer
 */
public class Settings
{
    public static enum TestLocation
    {
        VFS_LOCALFS_LOCATION,
        VFS_LOCAL_TEMPDIR_LOCATION,
        VFS_SFTP_LOCALHOST
    }

    /**
     * Singleton!
     */
    private static Settings instance;

    static
    {
        instance = new Settings();
    }

    public static Settings getDefault()
    {
        return instance;
    }

    // ====================
    // Static Context
    // ====================

    protected static VRSContext staticContext;

    public static VRSContext getStaticTestContext()
    {
        if (staticContext == null)
        {
            staticContext = new VRSContext();
        }
        return staticContext;
    }

    public static Registry getStaticRegistry()
    {
        return getStaticTestContext().getRegistry();
    }

    // ========================================================================
    // init locations:
    // ========================================================================

    public static VRL getTestLocation(String location)
    {
        return getDefault().testLocations.get(TestLocation.valueOf(location));
    }

    public static VRL getTestLocation(TestLocation location)
    {
        return getDefault().testLocations.get(location);
    }

    // ========================================================================
    //
    // ========================================================================

    public static ResourceConfigInfo getServerInfoFor(VRL location, boolean create) throws VrsException
    {
        return VRS.createVRSClient().getResourceSystemInfoFor(location, create);
    }

    // ========================================================================
    // Instance
    // ========================================================================

    private Map<TestLocation, VRL> testLocations = new Hashtable<TestLocation, VRL>();

    private String testUserName;

    private Settings()
    {
        testUserName = GlobalProperties.getGlobalUserName();
        initLocations();
    }

    private void initLocations()
    {

        testLocations.put(TestLocation.VFS_LOCAL_TEMPDIR_LOCATION,
                new VRL("file", null, "/tmp/" + testUserName + "/localtmpdir"));

        testLocations.put(TestLocation.VFS_LOCALFS_LOCATION,
                new VRL("file", null, "/tmp/" + testUserName + "/testLocalFS"));

        testLocations.put(TestLocation.VFS_SFTP_LOCALHOST,
                new VRL("sftp", null, "localhost", 22, "/tmp/" + "sftptest" + "/testSftpFS"));

    }

    public VRL getLocation(TestLocation location)
    {
        return this.testLocations.get(location);
    }

    public String[] getLocationNames()
    {
        Set<TestLocation> set = this.testLocations.keySet();
        String names[] = new String[set.size()];
        names = set.toArray(names);
        return names;
    }

    public void setUsername(String user)
    {
        this.testUserName = user;
        initLocations();
    }

}
