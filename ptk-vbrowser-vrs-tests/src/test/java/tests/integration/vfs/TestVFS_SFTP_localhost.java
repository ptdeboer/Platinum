package tests.integration.vfs;

import org.junit.Test;

import nl.esciencecenter.ptk.crypt.Secret;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfo;
import nl.esciencecenter.vbrowser.vrs.sftp.SftpFileSystemFactory;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import tests.integration.vfs.TestSettings.TestLocation;

/**
 * Test Local case
 *
 * TestSuite uses testVFS class to tests Local FileSystem implementation.
 *
 * @author P.T. de Boer
 */
public class TestVFS_SFTP_localhost extends TestVFS
{
    static
    {
        initSFTPFS();
    }

    public static void initSFTPFS()
    {
        try
        {
            VRSContext context = VFSTest.getStaticTestContext();
            context.getRegistry().registerFactory(SftpFileSystemFactory.class);
            VRL vrl=TestSettings.getTestLocation(TestLocation.VFS_SFTP_LOCALHOST);


            ResourceSystemInfo info = context.getResourceSystemInfoFor(vrl, true);
            info.setUserInfo("sftptest");
            info.setPassword(new Secret("test1234".toCharArray()));
            info.setAuthSchemeToPassword();

            info.store();

            System.err.printf("New ResourceSystemInfo:%s\n",info);


        }
        catch (InstantiationException | IllegalAccessException | VrsException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public VRL getRemoteLocation()
    {
        return TestSettings.getTestLocation(TestLocation.VFS_SFTP_LOCALHOST);
    }

    
}
