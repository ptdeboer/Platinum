/*
 * Copyrighted 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").  
 * You may not use this file except in compliance with the License. 
 * For details, see the LICENCE.txt file location in the root directory of this 
 * distribution or obtain the Apache License at the following location: 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
 * ---
 */
// source: 

package nl.esciencecenter.ptk.vbrowser.ui.browser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.TransferHandler;

import com.sun.media.jfxmediaimpl.platform.Platform;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.net.URIFactory;
import nl.esciencecenter.ptk.ui.icons.IconProvider;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.DnDUtil;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactory;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactoryRegistry;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerRegistry;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerResourceHandler;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VRSProperties;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.util.VRSResourceLoader;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Browser Platform.
 * 
 * Typically one Platform instance per application environment is created.
 */
public class BrowserPlatform
{
    private static Map<String,BrowserPlatform> platforms=new Hashtable<String,BrowserPlatform>(); 
    
    /** 
     * Get specific BrowserPlatform. 
     * Multiple browser platform may be register/created in one single JVM.  
     */
    public static BrowserPlatform getInstance(String ID)
    {
        BrowserPlatform instance; 
        
        synchronized(platforms)
        {
            instance=platforms.get(ID);
            
            if (instance==null)
            {
                instance=new BrowserPlatform(ID); 
                platforms.put(ID,instance);  
            }
        }            
        
        return instance;
    }

    // ========================================================================
    // Instance
    // ========================================================================

    private String platformID; 
    
    private ProxyFactoryRegistry proxyRegistry = null;

    private ViewerRegistry viewerRegistry;

    private VRSResourceLoader resourceLoader;

    private JFrame rootFrame;

    private IconProvider iconProvider;

    private VRSContext vrsContext; 
    
    protected BrowserPlatform(String id)
    {
        try
        {
            init(id);
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    private void init(String id) throws URISyntaxException
    {
        this.platformID=id; 
        // init defaults:
        this.proxyRegistry = ProxyFactoryRegistry.getInstance();
        
        URI cfgDir = getPlatformConfigDir();
        
        initVRSContext(cfgDir); 
        
        // Default viewer resource Loader/Resource Handler: 
        resourceLoader = new VRSResourceLoader(getVRSContext());
        ViewerResourceHandler resourceHandler = new ViewerResourceHandler(resourceLoader);
        resourceHandler.setViewerConfigDir(cfgDir);
        this.viewerRegistry = new ViewerRegistry(resourceHandler);

        rootFrame = new JFrame();
        iconProvider = new IconProvider(rootFrame, resourceLoader);
    }
    
    private void initVRSContext(URI cfgDir)
    {
        VRSProperties props=new VRSProperties(); 
        vrsContext=new VRSContext(props);  
    }

    public VRSContext getVRSContext()
    {
        return vrsContext; 
    }
    

    public void registerVRSFactory(Class<? extends VResourceSystemFactory> clazz) throws Exception 
    {
        vrsContext.getRegistry().registryFactory(clazz); 
    }
    
    public void setResourceLoader(ResourceLoader resourceLoader)
    {
        viewerRegistry.getResourceHandler().setResourceLoader(resourceLoader);
    }

    public String getPlatformID()
    {
        return platformID; 
    }

    public ProxyFactory getProxyFactoryFor(VRL locator)
    {
        return this.proxyRegistry.getProxyFactoryFor(locator);
    }

    public BrowserInterface createBrowser()
    {
        return createBrowser(true);
    }

    public BrowserInterface createBrowser(boolean show)
    {
        return new ProxyBrowser(this, show);
    }

    public void registerProxyFactory(ProxyFactory factory)
    {
        this.proxyRegistry.registerProxyFactory(factory);
    }

    /**
     * Returns Internal Browser DnD TransferHandler for DnDs between browser
     * frames and ViewNodeComponents.
     */
    public TransferHandler getTransferHandler()
    {
        // default;
        return DnDUtil.getDefaultTransferHandler();
    }

    public ViewerRegistry getViewerRegistry()
    {
        return viewerRegistry;
    }

    public URI getPlatformConfigDir()
    {
        try
        {
            return new URIFactory("file:///" + GlobalProperties.getGlobalUserHome()).appendPath(
                    "." + getPlatformID().toLowerCase()).toURI();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public IconProvider getIconProvider()
    {
        return iconProvider;
    }


}
