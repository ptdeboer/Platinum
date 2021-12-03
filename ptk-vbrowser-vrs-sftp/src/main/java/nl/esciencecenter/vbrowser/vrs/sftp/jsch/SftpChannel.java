package nl.esciencecenter.vbrowser.vrs.sftp.jsch;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Managed ChannelSftp.
 */
public class SftpChannel implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(SftpChannel.class);

    private final ChannelSftp channel;

    private final Object channelMutex = new Object();

    private String userHome;

    private final SshSession sshSession;

    public SftpChannel(SshSession session, ChannelSftp channel) {
        this.channel = channel;
        this.sshSession = session;
    }

    public void connect() throws JSchException {

        synchronized (channelMutex) {
            this.channel.connect();
        }

        try {
            this.userHome = this.channel.pwd();
        } catch (SftpException e) {
            logger.warn("Failed to retrieve PWD for:{}", this);
            this.userHome = null;
        }
    }

    public boolean isConnected() {
        synchronized (channelMutex) {
            return ((this.channel != null) && (this.channel.isConnected()));
        }
    }

    @SuppressWarnings("unchecked")
    protected Vector<LsEntry> ls(String remotePath) throws SftpException {
        logger.debug("ls():'{}'", remotePath);
        check();
        synchronized (channelMutex) {
            return (Vector<LsEntry>) channel.ls(remotePath);
        }
    }

    protected void check() throws SftpException {
        if (this.channel == null) {
            throw new SftpException(ChannelSftp.SSH_FX_CONNECTION_LOST,
                    "Channel is not connected: channel is NULL.");
        }
        if (this.channel.isConnected() == false) {
            throw new SftpException(ChannelSftp.SSH_FX_CONNECTION_LOST, "Channel is not connected.");
        }
    }

    /**
     * The user home is the starting directory when the user connects for the firs time.
     *
     * @return user home which was the starting path at the beginnen of a (sftp)sesion.
     */
    public String getUserHome() {
        return this.userHome;
    }

    /**
     * List relative or absolute path. Both are allowed.
     *
     * @param remotePath relative or absolute path.
     * @return List of relative filenames.
     */
    public List<SftpEntry> list(String remotePath) throws SftpException {
        logger.debug("list():'{}'", remotePath);
        check();

        Vector<LsEntry> dirList = this.ls(remotePath);
        List<SftpEntry> entries = new ArrayList<SftpEntry>();

        for (int i = 0; i < dirList.size(); i++) {
            Object entry = dirList.elementAt(i);

            if (entry instanceof com.jcraft.jsch.ChannelSftp.LsEntry) {
                String name = ((com.jcraft.jsch.ChannelSftp.LsEntry) entry).getFilename();
                if (".".equals(name) || ("..".equals(name))) {
                    continue;
                } else {
                    entries.add(new SftpEntry((ChannelSftp.LsEntry) entry));
                }
            } else {
                logger.warn("ls() returned unknown entry:{}", entry.getClass());
            }
        }

        return entries;
    }

    public SftpATTRS statSftpAttrs(String remotePath, boolean resolveLink) throws SftpException {
        logger.debug("statSftpAttrs():resolveLink={},remotePath={}", resolveLink, remotePath);
        check();
        synchronized (channelMutex) {
            SftpATTRS attrs;
            if (resolveLink == false) {
                attrs = channel.stat(remotePath);
            } else {
                attrs = channel.lstat(remotePath);
            }

            logger.debug("statSftpAttrs():remotePath='{}' => stat='{}'", remotePath, attrs);
            return attrs;
        }
    }

    public boolean mkdir(String remotePath) throws SftpException {
        logger.debug("mkdir():'{}'", remotePath);
        check();
        synchronized (channelMutex) {
            this.channel.mkdir(remotePath);
            return true;
        }
    }

    public boolean delete(String remotePath, boolean isDir) throws SftpException {
        logger.debug("delete():({}:)'{}'", isDir ? "DIR" : "FILE", remotePath);
        check();
        synchronized (channelMutex) {
            if (isDir) {
                this.channel.rmdir(remotePath);
            } else {
                this.channel.rm(remotePath);
            }
        }
        return true;
    }

    public void rename(String oldPath, String newPath) throws SftpException {
        logger.debug("rename():'{}' => '{}'", oldPath, newPath);
        check();
        synchronized (channelMutex) {
            this.channel.rename(oldPath, newPath);
        }
    }

    public boolean exists(String remotePath) throws SftpException {
        logger.debug("exists():'{}'", remotePath);
        check();
        try {
            return (this.statSftpAttrs(remotePath, false) != null);
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }
            throw e;
        }
    }

    /**
     * Check whether remote path exists but use 'ls' method instead of 'stat'.
     *
     * @param remotePath
     * @return whether remote path exists using the 'ls' method.
     * @throws SftpException
     */
    public boolean existsLS(String remotePath) throws SftpException {
        check();
        logger.debug("existsLS():remotePath={}", remotePath);

        try {
            Vector<LsEntry> entries = this.ls(remotePath);

            return entries.size() > 0;

        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }
            throw e;
        }
    }

    /**
     * Performs 'get()' but this blocks the current channel until the InputStream is closed. For a
     * private SftpChannel and InputStream use SshSession.createSftpInputStream();
     *
     * @see SshSession#createSftpInputStream(String)
     */
    protected InputStream get(String path) throws SftpException {
        check();
        return channel.get(path);
    }

    /**
     * Performs 'put()' but this blocks the current channel until the OutputStream is closed. For a
     * private SftpChannel and OutputStream use SshSession.createSftpOutputStream();
     *
     * @see SshSession#createSftpOutputStream(String)
     */
    protected OutputStream put(String remotePath, int mode) throws SftpException {
        check();
        return this.channel.put(remotePath, mode);
    }

    // =========
    // LifeCycle 
    // =========

    protected void disconnect() {
        synchronized (channelMutex) {
            if (this.channel.isConnected()) {
                logger.info("disconnect() from:{}", this);
                this.channel.disconnect();
            } else {
                logger.info("disconnect(): already disconnected from:{}", this);
            }
        }
    }

    public void dispose() {
        disconnect();
    }

    public void close() {
        this.disconnect();
    }

    // =========
    // Misc 
    // =========

    public String toString() {
        return String.format("SftpChannel:[sshSession:'" + sshSession + "',isConnected:'"
                + isConnected() + "']");
    }
}
