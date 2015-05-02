package tests.integration.vfs;

import tests.integration.vfs.Settings.TestLocation;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Test Local case
 * 
 * TestSuite uses testVFS class to tests Local FileSystem implementation.
 * 
 * @author P.T. de Boer
 */
public class VFS_LocalFS extends VFSTests
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
        return Settings.getTestLocation(TestLocation.VFS_LOCALFS_LOCATION);
    }

}
