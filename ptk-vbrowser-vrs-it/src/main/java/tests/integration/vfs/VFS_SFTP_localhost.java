package tests.integration.vfs;

import nl.esciencecenter.ptk.crypt.Secret;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.sftp.SftpFileSystemFactory;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import tests.integration.vfs.Settings.TestLocation;

/**
 * Test Local case TestSuite uses testVFS class to tests Local FileSystem implementation.
 */
public class VFS_SFTP_localhost extends VFSTests {

    static {
        initSFTPFS();
    }

    public static void initSFTPFS() {
        //
        try {
            VRSContext context = Settings.getStaticTestContext();
            context.getRegistry().registerFactory(SftpFileSystemFactory.class);
            VRL vrl = Settings.getTestLocation(TestLocation.VFS_SFTP_LOCALHOST);
            //
            ResourceConfigInfo info = context.getResourceSystemInfoFor(vrl, true);
            info.setUserInfo("sftptest");
            info.setPassword(new Secret("test1234".toCharArray()));
            info.setAuthSchemeToPassword();
            info.store();
        } catch (InstantiationException | IllegalAccessException | VrsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public VRL getRemoteLocation() {
        return Settings.getTestLocation(TestLocation.VFS_SFTP_LOCALHOST);
    }

}
