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

package nl.esciencecenter.vbrowser.vrs.webrs;

import java.net.URISyntaxException;

import nl.esciencecenter.ptk.ssl.CertUI;
import nl.esciencecenter.ptk.ssl.CertificateStore;
import nl.esciencecenter.ptk.ssl.CertificateStore.CaCertOptions;
import nl.esciencecenter.ptk.ssl.CertificateStoreException;
import nl.esciencecenter.ptk.ui.UI;
import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.ptk.web.WebClient;
import nl.esciencecenter.ptk.web.WebConfig.AuthenticationType;
import nl.esciencecenter.ptk.web.WebException;
import nl.esciencecenter.ptk.web.WebException.Reason;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsIOException;
import nl.esciencecenter.vbrowser.vrs.node.VResourceSystemNode;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class WebResourceSystem extends VResourceSystemNode
{
    public static final String DEFAULT_HTTPRS_SERVERID = "webrs";
   
    private final static PLogger logger=PLogger.getLogger(WebResourceSystem.class); 

    // ========================================================================
    //
    // ========================================================================

    private VRSContext vrsContext;

    private VRL sourceVrl;

    protected WebClient webClient; 
    
    public WebResourceSystem(VRSContext context, ResourceConfigInfo info) throws VrsException
    {
        super(context,info.getServerVRL()); 
        
        this.vrsContext = context;
        this.sourceVrl=info.getServerVRL(); 
        // this.cache=new HTTPCache(context);
        try
        {
            CertificateStore certStore=context.getCertificateStore();
            
            // multithreaded web client (!) 
            webClient=WebClient.createMultiThreadedFor(info.getServerVRL().toURI(),AuthenticationType.NONE);
            webClient.setCertificateStore(certStore);
            
        }
        catch (WebException | URISyntaxException | CertificateStoreException e)
        {
            throw new VrsException(e.getMessage(),e); 
        } 
        
        // auto connect
        connect(); 
    }

    public String getID()
    {
        return DEFAULT_HTTPRS_SERVERID;
    }

    public VRSContext getVRSContext()
    {
        return this.vrsContext;
    }

//    /**
//     * Always return Proxy Object. If no proxy has been defined it returns a
//     * Proxy.NO_PROXYtype which means no proxy. This way you can always use
//     * getProxy();
//     * 
//     * @param isHTTPS
//     * @return
//     */
//    public Proxy getHTTPProxy(boolean isHTTPS)
//    {
//        if (isHTTPS == false)
//        {
//            if (httpProxy == null)
//                // check ServerInfo for this resource ?
//                httpProxy = this.vrsContext.getConfigManager().getHTTPProxy();
//            return httpProxy;
//        }
//        else
//        {
//            if (httpsProxy == null)
//                // check ServerInfo for this resource ?
//                httpsProxy = this.vrsContext.getConfigManager().getHTTPSProxy();
//            return httpsProxy;
//        }
//    }

    //@Override
    public void connect() throws VrsException
    {
        int numTries=3;
        WebException lastException=null;
        
        boolean doImport=false; 
        boolean asked=false; 
        
        for (int i=0;i<numTries;i++)
        {   
            try
            {
                webClient.connect();
                return;
            }
            catch (WebException e)
            {
                lastException=e; 
            }
            
            if ((lastException!=null) && (lastException.getReason()==Reason.HTTPS_SSLEXCEPTION))
            {
                try
                {
                    if (asked==false)
                    {
                        UI ui = this.getVRSContext().getUI(); 
                        doImport=ui.askYesNo("Invalid Certificate", "Invalid Certificate or server Certificate not recognized\n Import Certificate ?", false); 
                        asked=true;
                    }
                    
                    if (doImport==true)
                    {
                        addCertificate(getServerVRL());
                    }
                    else
                    {
                        break; 
                    }
                }
                catch (Exception e)
                {
                    throw new VrsIOException("Failed to authenticate server ssl connection.\n"+e.getMessage(),e); 
                }    
            }
            else
            {
                break; 
            }
        } 

        throw new VrsException(lastException.getMessage(),lastException);
    }

    protected boolean addCertificate(VRL vrl) throws Exception
    {
        UI ui = this.getVRSContext().getUI(); 
        
        if ((ui==null) || ui.isEnabled()==false)
        {
            PLogger.getLogger(WebResourceSystem.class).warnPrintf("Non interactive invironment. Cannot add certificate for:%s\n",vrl); 
            return false;
        }
        
        CertificateStore caCerts = getVRSContext().getCertificateStore(); 
        CaCertOptions options=new CaCertOptions(); 
        options.storeAccepted=true; 
        options.interactive=true; 
        
        if (caCerts==null)
        {
            logger.warnPrintf("Invalid Certicifate or not recognized cert for:%s\n",vrl); 
            return false; 
        }
        else
        {
            boolean succeeded=CertUI.interactiveImportCertificate(caCerts,vrl.getHostname(),vrl.getPort(),options); 
            return succeeded;
        }
    }

    //@Override
    public void disconnect() throws VrsException
    {
        try
        {
            webClient.disconnect();
        }
        catch (WebException e)
        {
           throw new VrsException(e.getMessage(),e); 
        } 
    }

    //@Override
    public void dispose()
    {
    }

    public VRL getVRL()
    {
        return this.getServerVRL();  
    }

    public VRL resolve(String path) throws VRLSyntaxException 
    {
        return sourceVrl.uriResolve(path);
    }

    public WebClient getWebClient()
    {
       return webClient;
    }

    @Override
    public VPath resolvePath(VRL vrl) throws VrsException
    {
        return new WebNode(this,vrl); 
    }

}
