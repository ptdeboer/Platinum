/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.api;

import java.io.IOException;
import java.net.URI;

/**
 * Interface for resources which can create a 'Shell'. Typically a shell has a pseudo tty interface.
 *
 * @see ShellChannel
 */
public interface ShellChannelFactory {

    ShellChannel createChannel(URI uri, String username, char[] password, ChannelOptions options) throws IOException;

}
