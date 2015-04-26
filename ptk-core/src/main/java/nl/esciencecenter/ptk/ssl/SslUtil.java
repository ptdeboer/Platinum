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

package nl.esciencecenter.ptk.ssl;

import java.net.URLStreamHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import nl.esciencecenter.ptk.util.logging.ClassLogger;

public class SslUtil
{
    private static ClassLogger logger;
    
    static
    {
        logger=ClassLogger.getLogger(SslUtil.class); 
    }
    
    public static final int DEFAULT_TIMEOUT = 30000; 
    
    /**
     *  Open SSL ('SSLv3') socket using CertificateStore for trusted certificates. 
     *  
     *  @param cacerts - the trusted certificate store which may contain a user 'private' key.
     *  @param host - the hostname to connect to 
     *  @param port - the port. 
     *  @param open - create and open socket.  
     */
    public static SSLSocket createSSLv3Socket(CertificateStore cacerts,String host, int port, int timeOut,boolean open) throws Exception
    {
        SSLContext sslContext = cacerts.createSSLContext(SslConst.PROTOCOL_SSLv3); 
        return createSSLSocket(sslContext, host, port, timeOut,open);
    }
    
    /** 
     * Open SSL socket using specified SSLContext. 
     */
    public static SSLSocket createSSLSocket(SSLContext context, String host, int port, int timeOut,boolean open) throws Exception
    {
        SSLSocketFactory factory = context.getSocketFactory();

        logger.debugPrintf("Opening connection to %s:%d...\n", host, port);
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);

        if (timeOut <= 0)
            timeOut = DEFAULT_TIMEOUT;
        
        socket.setSoTimeout(timeOut);

        logger.debugPrintf("Starting SSL handshake...\n");
        if (open)
        {
            socket.startHandshake();
            logger.debugPrintf("No errors, certificate is trusted for: %s:%d\n", host, port);
        }
        
        return socket;
    }
    
    public static void setStaticHttpsSslContext(SSLContext context)
    {
        // Might not work in custom http/https context: 
        // Check web service compatibility here: 
        try
        {
            SSLSocketFactory factory = context.getSocketFactory();
            sun.net.www.protocol.https.HttpsURLConnectionImpl.setDefaultSSLSocketFactory(factory); 
            // sun.net.www.protocol.https.HttpsURLConnectionImpl.setDefaultSSLSocketFactory(context.getSocketFactory());
            // sun.net.www.protocol.https.HttpsURLConnectionImpl.setDefaultAllowUserInteraction(true);
        }
        catch (Throwable e)
        {
            logger.logException(ClassLogger.ERROR,e,"Failed to initialize SSLSocketFactory\n");
        }
    }

    // com.sun.net.ssl.internal.www.protocol.https.HttpsURLConnectionImpl.setDefaultHostnameVerifier(hv);

    /**
     * Create HttpsHandler which conforms to the classes used in the above
     * methods
     */
    public static URLStreamHandler createHttpsHandler()
    {
        // return class same as initialized above!
        return new sun.net.www.protocol.https.Handler();
    }
}
