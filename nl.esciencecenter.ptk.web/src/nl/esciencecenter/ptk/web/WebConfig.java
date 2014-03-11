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

package nl.esciencecenter.ptk.web;

import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.ptk.crypt.Secret;
import nl.esciencecenter.ptk.ssl.SslConst;

/**
 * All configurable parameters to connect to a web service.
 */
public class WebConfig
{
    public static class SslOptions
    {
        /**
         * Default SSL/TSL protocol to use.
         */
        public String protocol = SslConst.PROTOCOL_TLS;

        /**
         * Whether to check fully qualified hostnames or not.
         */
        public boolean disable_strict_hostname_checking = true;
    }

    public static enum AuthenticationType
    {
        NONE, BASIC
    };

    /**
     * Default time out of 30 seconds. The is the time to setup a TCP
     * connection. It is not a request time out.
     */
    protected int tcpConnectionTimeout = 30000; // 30 seconds;

    protected String serverHostname = "localhost";

    protected int serverPort = 80;

    protected AuthenticationType authenticationType = AuthenticationType.NONE;

    protected String username = null;

    protected Secret password;

    /**
     * Service path is the path in the URL after the hostname, without a slash.
     */
    protected String servicePath = "";

    /**
     * Protocol is either http or https.
     */
    protected String protocol = "http";

    /**
     * Whether to use a HTTP proxy.
     */
    protected boolean useProxy = false;

    protected String proxyHostname;

    protected int proxyPort;

    protected boolean isMultiThreaded = false;

    protected boolean allowUserInteraction = true;

    /**
     * URI part after serviceUri which initializes a new JSESSION ID. If set to
     * null, no JSESSION ID will be requested. Use WebClient.setJSessionID() to
     * manually specify the used JSESSION ID.
     */
    protected String jsessionInitPart = "REST/JSESSION";

    protected SslOptions sslOptions = new SslOptions(); // defaults

    protected WebConfig()
    {
    }

    public WebConfig(java.net.URI serviceUri)
    {
        init(serviceUri);
    }

    public WebConfig(URI serviceUri, AuthenticationType authenticationType, boolean multiThreaded)
    {
        this.authenticationType = authenticationType;
        this.isMultiThreaded = multiThreaded;
        init(serviceUri);
    }

    /**
     * Parse scheme,host,port and optional the username from this URI and update
     * the configuration settings.
     */
    public void updateURI(java.net.URI uri)
    {
        init(uri);
    }

    protected void init(java.net.URI serviceUri)
    {
        this.protocol = serviceUri.getScheme().toLowerCase();
        this.serverHostname = serviceUri.getHost();
        this.serverPort = serviceUri.getPort();

        if (this.serverPort <= 0)
        {
            if (this.isHTTPS())
            {
                serverPort = SslConst.HTTPS_PORT;
            }
            else
            {
                serverPort = SslConst.HTTP_PORT;
            }
        }

        // =========
        // sanitize:
        // =========

        this.servicePath = serviceUri.getPath();

        if (servicePath != null)
        {
            if (servicePath.equals(""))
            {
                servicePath = "/";
            }

            // make absolute:
            if (servicePath.startsWith("/") == false)
            {
                servicePath = "/" + servicePath;
            }

            // strip last slash:
            if ((servicePath.length() > 1) && (servicePath.endsWith("/")))
            {
                servicePath = this.servicePath.substring(0, servicePath.length() - 1);
            }
        }

        if (serviceUri.getUserInfo() != null)
        {
            this.username = serviceUri.getUserInfo();
        }
    }

    /**
     * Return Web Service URI as java.net.URI including the web service path
     * name, for example "https://www.cnn.nl:443/theNews/"
     * 
     * @return The Web service URI as java.net.URI
     * @throws URISyntaxException
     */
    public java.net.URI getServiceURI() throws URISyntaxException
    {
        return new java.net.URI(this.protocol, this.username, this.serverHostname, this.serverPort, this.servicePath, null, null);
    }

    /**
     * Return web server URI as java.net.URI, for example "http://www.cnn.nl/"
     * 
     * This is the HOST URI without the service path name.
     * 
     * @return The server or host URI as java.net.URI
     * @throws URISyntaxException
     */
    public java.net.URI getServerURI() throws URISyntaxException
    {
        return new java.net.URI(this.protocol, this.username, this.serverHostname, this.serverPort, null, null, null);
    }

    public boolean hasPassword()
    {
        return ((password != null) && (!password.isEmpty()));
    }

    public boolean isHTTPS()
    {
        return (protocol != null ? (protocol.equals(SslConst.HTTPS_SCHEME)) : false);
    }

    public boolean isHTTP()
    {
        return (protocol != null ? (protocol.equals(SslConst.HTTP_SCHEME)) : false);
    }

    public void setCredentials(String user, Secret passwd)
    {
        this.username = user;
        this.password = passwd;
    }

    public boolean hasCredentials()
    {
        return ((username != null) && (password != null) && (password.isEmpty() == false));
    }

    public void setJSessionInitPart(String part)
    {
        this.jsessionInitPart = part;
    }

    public String getHostname()
    {
        return serverHostname;
    }

    public int getPort()
    {
        return serverPort;
    }

    public String getUsername()
    {
        return username;
    }

    /**
     * Returns password as char array. Please clear array after usage.
     * 
     * @return - password as char array.
     */
    public char[] getPasswordChars()
    {
        if (this.password == null)
            return null;

        return this.password.getChars();
    }

    /**
     * Returns whether a JSESSION must be initialized when connecting.
     * 
     * @return
     */
    public boolean useJSession()
    {
        if (useAuthentication())
        {
            return (this.jsessionInitPart != null);
        }
        else
        {
            return false;
        }
    }

    public boolean getUseBasicAuthentication()
    {
        return (this.authenticationType == AuthenticationType.BASIC);
    }

    public boolean useAuthentication()
    {
        if (this.authenticationType == null)
            return false;

        return (this.authenticationType != AuthenticationType.NONE);
    }

    public boolean isMultiThreaded()
    {
        return isMultiThreaded;
    }

    public void setMultiThreaded(boolean value)
    {
        this.isMultiThreaded = value;
    }

    public boolean getAllowUserInteraction()
    {
        return allowUserInteraction;
    }

    /**
     * Create URI string without throwing URISyntax Exceptions
     */
    public String getServiceURIString()
    {
        String uriStr = this.protocol + "://";
        if (this.username != null)
            uriStr += this.getUsername() + "@";

        uriStr += this.serverHostname + "/";
        uriStr += this.servicePath;
        return uriStr;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public String getServicePath()
    {
        return servicePath;
    }

}