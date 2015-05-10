/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.ptk.util.vterm;

import java.io.IOException;
import java.net.URI;
import java.util.Hashtable;
import java.util.Map;

import nl.esciencecenter.ptk.crypt.Secret;
import nl.esciencecenter.ptk.exec.ChannelOptions;
import nl.esciencecenter.ptk.exec.ShellChannel;
import nl.esciencecenter.ptk.exec.ShellChannelFactory;

public class VTermChannelProvider {
    public static class TermChannelOptions implements ChannelOptions {
        public boolean useChannelCompression;

        public String channelCompressionType;

        public boolean useChannelXForwarding;

        public String channelXForwardingHost;

        public int channelXForwardingPort;

        @Override
        public String toString() {
            return "ChannelOptions [useChannelCompression=" + useChannelCompression
                    + ", channelCompressionType=" + channelCompressionType
                    + ", useChannelXForwarding=" + useChannelXForwarding
                    + ", channelXForwardingHost=" + channelXForwardingHost
                    + ", channelXForwardingPort=" + channelXForwardingPort + "]";
        }

        @Override
        public String getOption(String name) {
            return null;
        }

        @Override
        public String getChannelType() {
            return null;
        }

    }

    // ===
    //
    // ===

    protected Map<String, ShellChannelFactory> factories = new Hashtable<String, ShellChannelFactory>();

    protected Map<String, TermChannelOptions> defaultOptions = new Hashtable<String, TermChannelOptions>();

    public VTermChannelProvider() {
    }

    public void registerChannelFactory(String type, ShellChannelFactory factory) {
        factories.put(type, factory);
    }

    public ShellChannel createChannel(String type, URI uri, String username, Secret password,
            ChannelOptions options) throws IOException {
        if ("BASH".equals(type)) {
            return new BASHChannel(uri, options);
        }

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
