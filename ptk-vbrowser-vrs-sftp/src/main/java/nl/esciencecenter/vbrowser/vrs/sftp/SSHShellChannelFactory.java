package nl.esciencecenter.vbrowser.vrs.sftp;

import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.io.VShellChannelCreator;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import nl.piter.vterm.api.ShellChannel;
import nl.piter.vterm.api.ShellChannelFactory;
import nl.piter.vterm.api.TermChannelOptions;
import nl.piter.vterm.api.TermUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SSHShellChannelFactory implements ShellChannelFactory {

    private final static Logger logger = LoggerFactory.getLogger(SSHShellChannelFactory.class);

    protected VRSClient vrsClient;

    public SSHShellChannelFactory(VRSContext context) {
        this.vrsClient = new VRSClient(context);
    }

    public ShellChannel createChannel(String user, String host, int port, char[] password,
                                      TermChannelOptions options, TermUI ui) throws IOException {
        try {
            VRL vrl = new VRL("ssh", user, host, port, "/");
            VResourceSystem vrs = this.vrsClient.getVResourceSystemFor(vrl);

            logger.error(">>> Found vrs:{}", vrs);

            if (vrs instanceof VShellChannelCreator) {
                return ((VShellChannelCreator) vrs).createShellChannel(vrl, options);
            } else {
                throw new IOException("ShellChannel not supported for resource:" + vrl);
            }
        } catch (VrsException e) {
            throw new IOException("Failed to createShellChannel to:" + user + "@" + host + ":" + port, e);
        }
    }

}
