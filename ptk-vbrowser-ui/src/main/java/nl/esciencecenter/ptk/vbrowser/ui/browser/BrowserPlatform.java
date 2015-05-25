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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.TransferHandler;

import nl.esciencecenter.ptk.ui.icons.IconProvider;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.DnDUtil;
import nl.esciencecenter.ptk.vbrowser.ui.properties.UIProperties;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactory;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactoryRegistry;
import nl.esciencecenter.ptk.vbrowser.viewers.PluginRegistry;
import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerEventDispatcher;
import nl.esciencecenter.ptk.vbrowser.viewers.vrs.ViewerResourceLoader;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VRSProperties;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.event.VRSEventNotifier;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Browser Platform. Typically one Platform instance per application environment is created.
 */
public class BrowserPlatform {

    private static PLogger logger = PLogger.getLogger(BrowserPlatform.class);

    private static Map<String, BrowserPlatform> platforms = new Hashtable<String, BrowserPlatform>();

    /**
     * Get specific BrowserPlatform. Multiple browser platforms may be register/created in one
     * single JVM.
     */
    public static BrowserPlatform getInstance(String ID) {
        BrowserPlatform instance;

        synchronized (platforms) {
            instance = platforms.get(ID);

            if (instance == null) {
                try {
                    instance = new BrowserPlatform(ID);
                } catch (Exception e) {
                    logger.errorPrintf("FATAL: Could not initialize browser platform:'%s'!\n", ID);
                    logger.logException(PLogger.FATAL, e, "Exception during initialization:%s\n", e);
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

    private JFrame rootFrame;

    private IconProvider iconProvider;

    private VRSContext vrsContext;

    private VRSClient vrsClient;

    private UIProperties guiSettings;

    private ViewerEventDispatcher viewerEventDispatcher;

    private List<ProxyBrowserController> browsers = new ArrayList<ProxyBrowserController>();

    protected BrowserPlatform(String id) throws Exception {
        init(id);
    }

    private void init(String id) throws Exception {
        this.platformID = id;
        // init defaults:
        this.proxyRegistry = ProxyFactoryRegistry.createInstance();

        // ===================================
        // Init VRS Classes
        // ===================================

        initVRSContext(null);

        // ===================================
        // Swing resources and producers
        // ===================================

        guiSettings = new UIProperties();

        // root Frame and Icon Renderer/provider:
        this.rootFrame = new JFrame();
        this.iconProvider = new IconProvider(rootFrame, new ResourceLoader(vrsClient, null));

        // ===================================
        // Init Viewers and ViewerPlugins.
        // ===================================

        this.viewerEventDispatcher = new ViewerEventDispatcher(true);
        initViewers();
    }

    protected void initVRSContext(VRL cfgDir) throws Exception {
        VRSProperties props = new VRSProperties("VRSBrowserProperties");
        this.vrsContext = new VRSContext(props);
        this.vrsClient = new VRSClient(getVRSContext());
    }

    public void setPersistantConfigLocation(VRL configHome, boolean enablePersistantConfig) {
        vrsContext.setPersistantConfigLocation(configHome, enablePersistantConfig);
    }

    /**
     * @return persistent configuration directory where to store properties for example
     *         '$HOME/.mypropertiesrc/'. <br>
     *         Is null for non persistent platforms.
     */
    public VRL getPersistantConfigLocation() {
        return vrsContext.getPersistantConfigLocation();
    }

    protected void initViewers() throws Exception {
        // create custom sub-directory for viewer settings. 
        ViewerResourceLoader resourceHandler = new ViewerResourceLoader(vrsClient, "viewers");
        // Viewer Registry for this Platform:
        this.viewerRegistry = new PluginRegistry(resourceHandler);
    }

    public VRSContext getVRSContext() {
        return vrsContext;
    }

    public void registerVRSFactory(Class<? extends VResourceSystemFactory> clazz) throws Exception {
        vrsContext.getRegistry().registerFactory(clazz);
    }

    public String getPlatformID() {
        return platformID;
    }

    public ProxyFactory getProxyFactoryFor(VRL locator) {
        return this.proxyRegistry.getProxyFactoryFor(locator);
    }

    public BrowserInterface createBrowser() {
        return createBrowser(true);
    }

    /**
     * Create actual browser.
     * 
     * @param show
     *            - show browser frame.
     * @return Master browser controller interface.
     */
    public BrowserInterface createBrowser(boolean show) {
        return register(new ProxyBrowserController(this, show));
    }

    public void registerProxyFactory(ProxyFactory factory) {
        this.proxyRegistry.registerProxyFactory(factory);
    }

    /**
     * Returns Internal Browser DnD TransferHandler for DnDs between ViewNodeComponents.
     */
    public TransferHandler getTransferHandler() {
        // default;
        return DnDUtil.getDefaultTransferHandler();
    }

    /**
     * @return Viewer and other plug-in registry for this platform.
     */
    public PluginRegistry getViewerRegistry() {
        return viewerRegistry;
    }

    public VRL createCustomConfigDir(String subPath) throws Exception {
        VRL configDir = this.vrsContext.getPersistantConfigLocation();

        if (configDir == null) {
            return null;
        }

        return configDir.resolvePath(subPath);
    }

    /**
     * @return Icon Factory for this platform.
     */
    public IconProvider getIconProvider() {
        return iconProvider;
    }

    public UIProperties getGuiSettings() {
        return guiSettings;
    }

    /**
     * Cross-platform event notifier. If all platform use URI (VRL) based events, these events can
     * be shared across implementations.
     * 
     * @return The global cross-platform VRSEventNotifier.
     */
    public VRSEventNotifier getVRSEventNotifier() {
        return VRSEventNotifier.getInstance();
    }

    /**
     * Generic ViewerEvent dispatcher.
     */
    public ViewerEventDispatcher getViewerEventDispatcher() {
        return this.viewerEventDispatcher;
    }

    protected ProxyBrowserController register(ProxyBrowserController browser) {
        logger.info("{}:register():{}", this, browser);
        this.browsers.add(browser);
        return browser;
    }

    protected void unregister(ProxyBrowserController browser) {
        logger.info("{}:unregister():{}", this, browser);
        this.browsers.remove(browser);
        if (this.browsers.size() <= 0) {
            logger.info(">>> Closed *last* browser for this Platform:{}", this.getPlatformID());
            shutDown();
        }
    }

    public String getAboutText() {
        return "" + //
                "      === Platinum Toolkit ===     \n" + //
                "  VBrowser 2.0 (Under construction)\n" + //
                "                                   \n";

    }

    // ==============  
    // Dispose/Misc.
    // ==============

    /**
     * Perform graceful shutdown.
     */
    public void shutDown() {
        dispose();
    }

    /**
     * Immediately close and dipose all registered resources;
     */
    public void dispose() {
        if (vrsClient!=null) { 
            vrsClient.dispose();
        }
        if (vrsContext!=null) { 
            vrsContext.dispose();
        }
        this.viewerEventDispatcher.stop();
        this.viewerEventDispatcher.dispose();
        this.vrsClient = null;
        this.vrsContext = null;
    }

    public String toString() {
        return "BrowserPlatform:[platformID='" + this.platformID + "']";
    }

}
