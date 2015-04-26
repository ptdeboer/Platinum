package tests.integration.vfs;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.vbrowser.vrs.VRS;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Test configuration settings.
 *
 * @author P.T. de Boer
 */
public class TestSettings
{
    public static enum TestLocation
    {
        VFS_LOCALFS_LOCATION,
        VFS_LOCAL_TEMPDIR_LOCATION,
    }

    /**
     * Singleton!
     */
    private static TestSettings instance;

    static
    {
        instance = new TestSettings();
    }

    public static TestSettings getDefault()
    {
        return instance;
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

    static
    {

    }

    public static ResourceSystemInfo getServerInfoFor(VRL location, boolean create) throws VrsException
    {
        return VRS.createVRSClient().getResourceSystemInfoFor(location, create);
    }

    // ========================================================================
    // Instance
    // ========================================================================

    private Map<TestLocation, VRL> testLocations = new Hashtable<TestLocation, VRL>();

    private String testUserName;

    private TestSettings()
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
