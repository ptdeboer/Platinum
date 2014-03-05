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

package nl.esciencecenter.ptk.vbrowser.ui.browser;

import java.net.URI;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.TransferHandler;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.net.URIFactory;
import nl.esciencecenter.ptk.ui.icons.IconProvider;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.GuiSettings;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.DnDUtil;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactory;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactoryRegistry;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.PluginRegistry;
import nl.esciencecenter.ptk.vbrowser.viewers.vrs.ViewerResourceLoader;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VRSProperties;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.util.VRSUtil;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Browser Platform.
 * 
 * Typically one Platform instance per application environment is created.
 */
public class BrowserPlatform
{
    private static ClassLogger logger = ClassLogger.getLogger(BrowserPlatform.class);

    private static Map<String, BrowserPlatform> platforms = new Hashtable<String, BrowserPlatform>();

    /**
     * Get specific BrowserPlatform. Multiple browser platforms may be
     * register/created in one single JVM.
     */
    public static BrowserPlatform getInstance(String ID)
    {
        BrowserPlatform instance;

        synchronized (platforms)
        {
            instance = platforms.get(ID);

            if (instance == null)
            {
                try
                {
                    instance = new BrowserPlatform(ID);
                }
                catch (Exception e)
                {
                    logger.errorPrintf("FATAL: Could not initialize browser platform:'%s'!\n", ID);
                    logger.logException(ClassLogger.FATAL, e, "Exception during initialization:%s\n", e);
                }
                platforms.put(ID, instance);
            }
        }

        return instance;
    }

    // ========================================================================
    // Instance
    // ========================================================================

    private String platformID;

    private ProxyFactoryRegistry proxyRegistry = null;

    private PluginRegistry viewerRegistry;

    private ResourceLoader resourceLoader;

    private JFrame rootFrame;

    private IconProvider iconProvider;

    private VRSContext vrsContext;
    
    private GuiSettings guiSettings; 

    protected BrowserPlatform(String id) throws Exception
    {
        init(id);
    }

    private void init(String id) throws Exception
    {
        this.platformID = id;
        // init defaults:
        this.proxyRegistry = ProxyFactoryRegistry.getInstance();

        // VRSContext for this platform and configuration properties:
        URI cfgDir = getPlatformConfigDir(null);
        initVRSContext(cfgDir);

        guiSettings=new GuiSettings(); 
        
        // Default viewer resource Loader/Resource Handler:
        this.resourceLoader = VRSUtil.createVRSResourceLoader(getVRSContext());
        ViewerResourceLoader resourceHandler = new ViewerResourceLoader(resourceLoader);
        // ~/.vbtk2/viewers
        resourceHandler.setViewerConfigDir(getPlatformConfigDir("viewers"));
        // Viewer Registry for this Platform:
        this.viewerRegistry = new PluginRegistry(resourceHandler);
        // root Frame and Icon Renderer/provider:
        this.rootFrame = new JFrame();
        this.iconProvider = new IconProvider(rootFrame, resourceLoader);
    }

    private void initVRSContext(URI cfgDir)
    {
        VRSProperties props = new VRSProperties("VRSBrowserProperties");
        vrsContext = new VRSContext(props);
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
        return new ProxyBrowserController(this, show);
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

    public PluginRegistry getViewerRegistry()
    {
        return viewerRegistry;
    }

    public URI getPlatformConfigDir(String optionalSubPath) throws Exception
    {
        String cfgPath = GlobalProperties.getGlobalUserHome();
        cfgPath += "." + getPlatformID().toLowerCase();

        if (StringUtil.isEmpty(optionalSubPath) == false)
        {
            cfgPath += cfgPath + optionalSubPath;
        }
        // normalize path
        cfgPath = URIFactory.uripath(cfgPath);

        return new URI("file", null, null, 0, cfgPath, null, null);
    }

    public IconProvider getIconProvider()
    {
        return iconProvider;
    }

    public GuiSettings getGuiSettings()
    {
       return guiSettings; 
    }

}
