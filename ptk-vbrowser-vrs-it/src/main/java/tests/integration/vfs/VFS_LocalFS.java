package tests.integration.vfs;

import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import tests.integration.vfs.Settings.TestLocation;

/**
 * Test Local case
 * TestSuite uses testVFS class to tests Local FileSystem implementation.
 */
public class VFS_LocalFS extends VFSTests {

    static {
        initLocalFS();
    }

    public static void initLocalFS() {

    }

    @Override
    public VRL getRemoteLocation() {
        return Settings.getTestLocation(TestLocation.VFS_LOCALFS_LOCATION);
    }

}
