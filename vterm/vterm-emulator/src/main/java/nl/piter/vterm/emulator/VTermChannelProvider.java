/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.emulator;

import nl.piter.vterm.api.ChannelOptions;
import nl.piter.vterm.api.ShellChannel;
import nl.piter.vterm.api.ShellChannelFactory;
import nl.piter.vterm.api.TermChannelOptions;
import nl.piter.vterm.channels.impl.BASHChannelFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Hashtable;
import java.util.Map;

public class VTermChannelProvider {

    protected Map<String, ShellChannelFactory> factories = new Hashtable<String, ShellChannelFactory>();

    // ===
    //
    // ===
    protected Map<String, TermChannelOptions> defaultOptions = new Hashtable<String, TermChannelOptions>();

    public VTermChannelProvider() {
        this.registerChannelFactory("BASH", new BASHChannelFactory());
    }

    public void registerChannelFactory(String type, ShellChannelFactory factory) {
        factories.put(type, factory);
    }

    public ShellChannel createChannel(String type, URI uri, String username, char[] password,
                                      ChannelOptions options) throws IOException {


        ShellChannelFactory factory = factories.get(type);

        if (factory != null) {
            return factory.createChannel(uri, username, password, options);
        }

        throw new IOException("Channel type not supported:" + type + " (when connecting to:" + uri
                + ")");
    }

    public TermChannelOptions getChannelOptions(String type) {
        return defaultOptions.get(type);
    }

    public void setChannelOptions(String type, TermChannelOptions newOptions) {
        defaultOptions.put(type, newOptions);
        System.out.println("Channel options:" + newOptions);
    }


}
