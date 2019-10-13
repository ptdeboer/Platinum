/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.channels.impl;

import nl.piter.vterm.api.ChannelOptions;
import nl.piter.vterm.api.ShellChannel;
import nl.piter.vterm.api.ShellChannelFactory;

import java.net.URI;

public class BASHChannelFactory implements ShellChannelFactory {

    @Override
    public ShellChannel createChannel(URI uri, String username, char[] password, ChannelOptions options) {
        return new BASHChannel(uri, options);
    }

}
