/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.ui.panel;

import nl.piter.vterm.api.ShellChannel;
import nl.piter.vterm.emulator.VTermChannelProvider;

import javax.swing.*;
import java.io.IOException;
import java.net.URI;

/**
 * The VT10X/XTerm (VTx) emulator
 */
public class VTerm {

    private VTermChannelProvider channelProvider;

    private VTermJFrame vtermJFrame;

    public VTerm() {
    }

    public VTerm withVTermChannelProvider(VTermChannelProvider channelProvider) {
        this.channelProvider = channelProvider;
        return this;
    }

    public VTerm start(String[] args) {
        return start(null, null, args);
    }

    /**
     * Start with Custom ShellChannel provided by already authenticated sub system like an SFTP Browser.
     */
    public VTerm start(ShellChannel shellChan, URI optionalLoc) {
        return start(shellChan, optionalLoc, new String[0]);
    }

    /**
     * Used by VBrowser to open a location.
     */
    public VTerm start(URI loc) {
        return start(null, loc, new String[0]);
    }

    public VTerm start(final ShellChannel shellChan, final URI optionalLocation, String[] args) {
        // check args[]
        if (this.channelProvider == null) {
            this.channelProvider = new VTermChannelProvider();
        }

        this.vtermJFrame = new VTermJFrame(channelProvider);

        // Always create windows during Swing Event thread
        Runnable creator = new Runnable() {
            public void run() {
                // Center on screen
                vtermJFrame.setLocationRelativeTo(null);
                vtermJFrame.setVisible(true);
                vtermJFrame.showSplash();
                vtermJFrame.requestFocus();

                vtermJFrame.updateFrameSize();
                if (shellChan != null) {
                    vtermJFrame.setShellChannel(shellChan);
                    vtermJFrame.startSession();
                }

                if (optionalLocation != null) {
                    try {
                        vtermJFrame.openLocation(optionalLocation);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        SwingUtilities.invokeLater(creator);
        return this;
    }

    public void dispose() {
        if (this.vtermJFrame != null) {
            this.vtermJFrame.dispose();
        }
    }

}
