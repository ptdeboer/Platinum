package nl.esciencecenter.vbrowser.vrs.sftp;

import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.io.VShellChannelCreator;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import nl.piter.vterm.api.ChannelOptions;
import nl.piter.vterm.api.ShellChannel;
import nl.piter.vterm.api.ShellChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SSHShellChannelFactory implements ShellChannelFactory {

    private final static Logger logger = LoggerFactory.getLogger(SSHShellChannelFactory.class);

    protected VRSClient vrsClient;

    public SSHShellChannelFactory(VRSContext context) {
        this.vrsClient = new VRSClient(context);
    }

    public ShellChannel createChannel(java.net.URI uri, String user, char[] password,
                                      ChannelOptions options) throws IOException {
        try {
            VRL vrl = new VRL(uri);
            VResourceSystem vrs = this.vrsClient.getVResourceSystemFor(vrl);

            logger.error(">>> Found vrs:{}", vrs);

            if (vrs instanceof VShellChannelCreator) {
                return ((VShellChannelCreator) vrs).createShellChannel(vrl, options);
            } else {
                throw new IOException("ShellChannel not supported for resource:" + uri);
            }
        } catch (VrsException e) {
            throw new IOException("Failed to createShellChannel to:" + uri, e);
        }
    }

}
