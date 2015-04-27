package nl.esciencecenter.vbrowser.vrs.sftp;

import nl.esciencecenter.vbrowser.vrs.sftp.jsch.SftpChannel;
import nl.esciencecenter.vbrowser.vrs.sftp.jsch.SftpConfig;
import nl.esciencecenter.vbrowser.vrs.sftp.jsch.SshSession;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.SftpException;

public class TestSftpExists {

    public static void main(String args[]) {

        try {
            testConnect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void testConnect() throws Exception {

        JSch.setLogger(new DebuggingLogger());

        SftpConfig config = new SftpConfig();

        config.host = "localhost";
        config.port = 22;
        config.user = "sftptest";
        config.passwd = "test1234".toCharArray();

        SshSession session = new SshSession(new JSch(), config, false);
        session.setUserUI(new UserRobot(config.user, config.passwd, null, true));
        session.connect();

        SftpChannel channel = session.createSftpChannel();
        channel.connect();
        
        channel.exists("/tmp/testdir/testfile"); 
        


        try
        {
            channel.exists("/tmp"); 
        }
        catch (SftpException e) {
            e.printStackTrace();
        }

        try
        {
            channel.exists("/tmp/testdir/testfileNOTFOUND"); 
        }
        catch (SftpException e) {
            e.printStackTrace();
        }
    }

}
