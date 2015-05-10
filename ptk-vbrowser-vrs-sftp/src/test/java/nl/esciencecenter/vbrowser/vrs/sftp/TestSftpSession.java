package nl.esciencecenter.vbrowser.vrs.sftp;

import java.util.List;

import nl.esciencecenter.vbrowser.vrs.sftp.jsch.SftpChannel;
import nl.esciencecenter.vbrowser.vrs.sftp.jsch.SftpConfig;
import nl.esciencecenter.vbrowser.vrs.sftp.jsch.SftpEntry;
import nl.esciencecenter.vbrowser.vrs.sftp.jsch.SshSession;

import com.jcraft.jsch.JSch;

public class TestSftpSession {

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
        config.passwd = "********".toCharArray();

        SshSession session = new SshSession(new JSch(), config, false);
        session.setUserUI(new UserRobot(config.user, config.passwd, null, true));
        session.connect();

        SftpChannel channel = session.createSftpChannel();
        channel.connect();
        List<SftpEntry> entries = channel.list(".");

        for (int i = 0; i < entries.size(); i++) {
            System.out.printf(" - %s\n", entries.get(i));
        }
    }

}
