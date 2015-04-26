package tests.integration.vfs;

import tests.integration.vfs.TestSettings.TestLocation;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Test Local case
 * 
 * TestSuite uses testVFS class to tests Local FileSystem implementation.
 * 
 * @author P.T. de Boer
 */
public class TestVFS_LocalFS extends TestVFS
{
    static
    {
        initLocalFS();
    }

    public static void initLocalFS()
    {

    }

    @Override
    public VRL getRemoteLocation()
    {
        return TestSettings.getTestLocation(TestLocation.VFS_LOCALFS_LOCATION);
    }

}
