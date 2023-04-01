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
import nl.piter.vterm.channels.impl.PtyChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

public class SSHShellChannelFactory implements ShellChannelFactory {

    private final static Logger logger = LoggerFactory.getLogger(SSHShellChannelFactory.class);

    protected VRSClient vrsClient;

    public SSHShellChannelFactory(VRSContext context) {
        this.vrsClient = new VRSClient(context);
    }

    public ShellChannel createChannel(URI uri, String user, char[] password,
                                      TermChannelOptions options, TermUI ui) throws IOException {
        VRL vrl = new VRL(uri);

        try {

            VResourceSystem vrs = this.vrsClient.getVResourceSystemFor(vrl);

            if (vrl.hasScheme("file")) {
                return new PtyChannelFactory().createChannel(vrl.toURINoException(), user, password, options, ui);
            }

            logger.error(">>> Found vrs:{}", vrs);

            if (vrs instanceof VShellChannelCreator) {
                return ((VShellChannelCreator) vrs).createShellChannel(vrl, options);
            } else {
                throw new IOException("ShellChannel not supported for resource:" + vrl);
            }
        } catch (VrsException e) {
            throw new IOException("Failed to createShellChannel to: " + vrl);
        }
    }

}
