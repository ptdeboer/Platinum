package nl.esciencecenter.vbrowser.vrs.sftp.jsch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.esciencecenter.ptk.io.FSPath;
import nl.esciencecenter.ptk.io.FSUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * SftpSession manages a JSch Session. This Session can be used to create more channels.
 */
public class SshSession implements AutoCloseable {

    private final static Logger logger = LoggerFactory.getLogger(SshSession.class);

    public static class DummyRobot implements UserInfo, UIKeyboardInteractive {

        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public boolean promptPassphrase(String message) {
            return false;
        }

        @Override
        public boolean promptPassword(String message) {
            return false;
        }

        @Override
        public boolean promptYesNo(String message) {
            return false;
        }

        @Override
        public void showMessage(String message) {
        }

        @Override
        public String[] promptKeyboardInteractive(String destination, String name,
                String instruction, String[] prompt, boolean[] echo) {
            return null;
        }
    }

    public static SshSession createSession(SftpConfig config) throws JSchException {
        return new SshSession(new JSch(), config, false);
    }

    // ===

    private Session session;

    private JSch jsch;

    private SftpConfig config;

    public SshSession(JSch jsch, SftpConfig config, boolean autoConnect) throws JSchException {
        this.jsch = jsch;
        this.config = config;

        initSession();

        if (autoConnect) {
            connect();
        }
    }

    public void connect() throws JSchException {
        session.connect();
    }

    public boolean isConnected() {
        return this.session.isConnected();
    }

    protected void initSession() throws JSchException {
        logger.info("initSession():{}", this);

        try {
            this.session = jsch.getSession(config.user, config.host, config.port);
            this.session.setUserInfo(new DummyRobot());

            addKnownHosts();
            addUserIDFiles();

            if (config.passwd != null) {
                session.setPassword(new String(config.passwd));
            }
            // Legacy options:
            // config.setProperty("StrictHostKeyChecking", "no");
            // if (sshOptions.compression == false) {
            // config.put("compression.s2c", "none");
            // config.put("compression.c2s", "none");
            // } else {
            // config.put("compression.s2c", "zlib,none");
            // config.put("compression.c2s", "zlib,none");
            // }
            this.session.setConfig(config.getProperties());
        } catch (JSchException e) {
            // chain into meaningfull exception:
            throw new JSchException("Couldn't connect to server:" + this, e);
        }
    }

    /**
     * User Robot can be a automated responder or an actual UI interface.
     *
     * @param userRobot
     */
    public void setUserUI(UserInfo userRobot) {
        this.session.setUserInfo(userRobot);
    }

    /**
     * Create actual SFTP Channel. For parallel access, multiple channels can be created.
     *
     * @return
     * @throws JSchException
     * @throws Exception
     */
    public SftpChannel createSftpChannel() throws SftpException, JSchException {
        logger.debug("createSftpChannel() to:{}", this);
        Channel channel = session.openChannel("sftp");
        return new SftpChannel(this, (ChannelSftp) channel);
    }

    public SftpChannelInputStream createSftpInputStream(String remotePath) throws SftpException,
            JSchException {
        logger.debug("createSftpInputStream() to:{}", this);
        SftpChannel newChannel = this.createSftpChannel();
        newChannel.connect();
        InputStream inps = newChannel.get(remotePath);
        return new SftpChannelInputStream(newChannel, inps);
    }

    public SftpChannelOutputStream createSftpOutputStream(String remotePath, boolean append)
            throws SftpException, JSchException {
        logger.debug("createSftpOutputStream() to:{}", this);
        SftpChannel outputChannel = createSftpChannel();
        outputChannel.connect();

        int mode = ChannelSftp.APPEND;
        if (append == false) {
            mode = ChannelSftp.OVERWRITE;
        }

        OutputStream outps = outputChannel.put(remotePath, mode);
        return new SftpChannelOutputStream(outps, outputChannel);
    }

    protected void disconnect() {
        if (this.session.isConnected()) {
            this.session.disconnect();
        }
    }

    public void dispose() {
        disconnect();
    }

    @Override
    public void close() {
        this.disconnect();
    }

    public String getServerString() {
        return String.format("%s@%s:%s", config.user, config.host, config.port);
    }

    public String toString() {
        return "SshSession:[server:'" + getServerString() + "']";
    }

    protected void addUserIDFiles() {
        //
        if (config.userConfigDir == null) {
            logger.debug("addUserIDFiles(): No userConfigDir configured");
            return;
        }
        // Config director is for example" ~/.ssh/
        String configDir = config.userConfigDir;
        FSPath configPath;

        try {
            configPath = FSUtil.getDefault().resolvePath(configDir);
        } catch (IOException e) {
            logger.error(
                    "addUserIDFiles():Failed to read/acces config directory:{} => IOException:{}",
                    configDir, e);
            return;
        }

        String keys[] = config.privateKeys;
        if ((keys == null) || (keys.length <= 0)) {
            logger.info("addUserIDFiles():No private keys");
            return;
        }

        for (String key : keys) {
            try {
                FSPath keyFile = configPath.resolvePath(key);
                if (keyFile.exists()) {
                    logger.info("addUserIDFiles(): adding existing identity:{}\n", keyFile);
                    jsch.addIdentity(keyFile.getPathname());
                } else {
                    logger.info("addUserIDFiles(): ignoring missing identity file:{}\n", keyFile);
                }
            } catch (IOException e) {
                logger.error("Got IOException accessing file:{}/{} => IOException:{}", configPath,
                        key, e);
            } catch (JSchException e) {
                logger.error("Got JSchException adding file:{}/{} => JSchException:{}", configPath,
                        key, e);
            }
        }
    }

    protected void addKnownHosts() {
        //
        if ((config.sshKnowHostFile == null) || (config.sshKnowHostFile == null)) {
            logger.debug("addUserIDFiles(): No userConfigDir or knownHostFile configured");
            return;
        }
        // Config director is for example" ~/.ssh/
        String configDir = config.userConfigDir;
        String knownHostsFile = config.sshKnowHostFile;

        FSPath configPath;
        try {
            if (configDir != null) {
                configPath = FSUtil.getDefault().resolvePath(configDir);
                FSPath hostsFile = configPath.resolvePath(knownHostsFile);
                if (hostsFile.exists()) {
                    jsch.setKnownHosts(hostsFile.getPathname());
                }
            }
        } catch (IOException e) {
            logger.error(
                    "addKnownHosts():Failed to read/acces known hosts file:{}/{} => IOException:{}",
                    configDir, knownHostsFile, e);
            return;
        } catch (JSchException e) {
            logger.error(
                    "addKnownHosts():Failed to add known hosts file:{}/{} => JSchException:{}",
                    configDir, knownHostsFile, e);
            return;
        }
    }

    public ChannelShell createShellChannel() throws JSchException {
        logger.debug("createShellChannel() to:{}", this);
        Channel channel = session.openChannel("shell");
        return (ChannelShell) channel;
    }

}
