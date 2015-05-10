package nl.esciencecenter.vbrowser.vrs.sftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.esciencecenter.ptk.exec.ShellChannel;
import nl.esciencecenter.ptk.io.IOUtil;
import nl.esciencecenter.vbrowser.vrs.sftp.jsch.SshSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;

public class SshShellChannel implements ShellChannel {

    static final private Logger logger = LoggerFactory.getLogger(SshShellChannel.class);

    public static class SsshChannelOptions {
        public boolean xforwarding = true;
        public String termType;
    }

    private SshSession session;

    private ChannelShell channel;

    private Object waitForObject = new Object();

    private SsshChannelOptions options;

    private OutputStream stdin;

    private InputStream stdout;

    private String termType;

    protected SshShellChannel(SshSession sshSession, ChannelShell channel) {
        this.session = sshSession;
        this.channel = channel;
    }

    public void connect() throws IOException {
        //
        try {
            // =============================
            // Shell Channel
            // =============================

            if (options != null) {
                if (options.termType != null) {
                    channel.setPtyType(options.termType);
                    this.termType = options.termType;
                }

                if (options.xforwarding) {
                    // actual forwarding settings must be done at session level:
                    // session.setX11Host(options.xhost);
                    // session.setX11Port(options.xport);
                    channel.setXForwarding(true);
                }
            }

            this.stdin = channel.getOutputStream();
            this.stdout = channel.getInputStream();

            channel.connect();
        } catch (IOException | JSchException e) {
            throw new IOException("Could connect to:" + session, e);
        }
    }

    @Override
    public OutputStream getStdin() {
        return this.stdin;
    }

    @Override
    public InputStream getStdout() {
        return this.stdout;
    }

    @Override
    public InputStream getStderr() {
        return null;
    }

    public boolean isConnected() {
        return ((this.channel != null) && (channel.isConnected()));
    }

    @Override
    public void disconnect(boolean waitForTermination) {
        // io streams 
        IOUtil.autoClose(this.stdin);
        IOUtil.autoClose(this.stdout);
        // channels
        if (this.channel != null) {
            this.channel.disconnect();
        }
        this.channel = null;
        // wait
        if (waitForTermination) {
            synchronized (this.waitForObject) {
                this.waitForObject.notifyAll();
            }
        }
    }

    public void setPtySize(int col, int row, int wp, int hp) {
        if (this.isConnected() == false) {
            logger.error("setPtySize(): NOT connected!");
            return;
        }
        this.channel.setPtySize(col, row, wp, hp);
    }

    @Override
    public String getTermType() {
        return this.termType;
    }

    public void setPtyType(String type) {
        if (this.isConnected() == false) {
            logger.error("setPtyType(): NOT connected!");
            return;
        }
        this.termType = type;
        this.channel.setPtyType(type);
    }

    @Override
    public boolean setTermType(String type) {
        this.setPtyType(type);
        return true;
    }

    @Override
    public boolean setTermSize(int col, int row, int wp, int hp) {
        this.setPtySize(col, row, wp, hp);
        return true;
    }

    @Override
    public int[] getTermSize() {
        // must use ctrl sequence
        return null;
    }

    @Override
    public void waitFor() throws InterruptedException {
        boolean wait = true;
        while (wait) {
            try {
                this.waitForObject.wait(30 * 1000);
                if (this.channel.isClosed() == true)
                    wait = false;
            } catch (InterruptedException e) {
                throw e;
            }
        }
    }

    @Override
    public int exitValue() {
        return channel.getExitStatus();
    }

    public void initStartPath(String path) {
        // ssh command:
        String cmd = "cd \"" + path + "\"; clear; echo 'SSHCHannel started...' ; \n";
        try {
            this.stdin.write(cmd.getBytes());
            this.stdin.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCWD(String path) {
        // ssh command:
        String cmd = "cd \"" + path + "\";\n";
        try {
            this.stdin.write(cmd.getBytes());
            this.stdin.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        return "SSHChannel:[session='" + session + "', connnected='" + isConnected() + "']";
    }

}
