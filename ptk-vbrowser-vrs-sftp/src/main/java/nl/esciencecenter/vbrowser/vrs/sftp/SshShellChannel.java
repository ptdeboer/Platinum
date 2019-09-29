package nl.esciencecenter.vbrowser.vrs.sftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.io.IOUtil;
import nl.esciencecenter.vbrowser.vrs.sftp.jsch.SshSession;

import nl.piter.vterm.api.ChannelOptions;
import nl.piter.vterm.api.ShellChannel;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import nl.piter.vterm.api.TermConst;

@Slf4j
public class SshShellChannel implements ShellChannel {

    public static class SshChannelOptions {
        // default!
        public String termType= "xterm"; //"xterm-256color";
        public int num_cols=80;
        public int num_rows=24;
//        public final boolean xforwarding=false;
//        public final String xforwaring_host=null;
//        public final int xforwarding_port=0;

        public SshChannelOptions() {
        }

        public SshChannelOptions(String termType, int numCols, int numRows) {
            this.termType=termType;
            this.num_cols=numCols;
            this.num_rows=numRows;
        }
    }

    private SshSession session;
    private ChannelShell channel;
    private Object waitForObject = new Object();
    private SshChannelOptions options;
    private OutputStream stdin;
    private InputStream stdout;

    public SshShellChannel(SshSession sftpSession, ChannelShell shellChannel, ChannelOptions options) {
        this.session = sftpSession;
        this.channel = shellChannel;
        // TODO: proper options forwarding!
        this.options = new SshChannelOptions(options.getTermType(), options.getDefaultColumns(),options.getDefaultRows());
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
                }

                channel.setPtySize(options.num_cols, options.num_rows, options.num_cols, options.num_rows);

                //                if (options.xforwarding) {
//                    // actual forwarding settings must be done at session level:
//                    // session.setX11Host(options.xhost);
//                    // session.setX11Port(options.xport);
//                    channel.setXForwarding(true);
//                }

            } else {
                channel.setPtyType(TermConst.TERM_XTERM);
                channel.setPtySize(80,24,80,24);
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
            log.error("setPtySize(): NOT connected!");
            return;
        }
        this.channel.setPtySize(col, row, wp, hp);
    }

    @Override
    public String getPtyTermType() {
        return this.options.termType;
    }

    public void setPtyType(String type) {
        if (this.isConnected() == false) {
            log.error("setPtyType(): NOT connected!");
            return;
        }
        this.options.termType = type;
        this.channel.setPtyType(type);
    }

    @Override
    public boolean setPtyTermType(String type) {
        this.setPtyType(type);
        return true;
    }

    @Override
    public boolean setPtyTermSize(int col, int row, int wp, int hp) {
        this.setPtySize(col, row, wp, hp);
        return true;
    }

    @Override
    public int[] getPtyTermSize() {
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
