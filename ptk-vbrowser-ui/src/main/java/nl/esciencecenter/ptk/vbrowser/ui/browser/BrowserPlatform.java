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

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.ui.icons.IconProvider;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.ptk.vbrowser.ui.browser.laf.LookAndFeelType;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.DnDUtil;
import nl.esciencecenter.ptk.vbrowser.ui.properties.UIProperties;
import nl.esciencecenter.ptk.vbrowser.ui.properties.UIPropertiesSaver;
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
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static nl.esciencecenter.ptk.vbrowser.ui.browser.laf.LookAndFeelType.NATIVE;

/**
 * Browser Platform. Typically, one Platform instance per application environment is created.
 * The BrowserPlatform object contains all configurable context(s) in an non static environment.
 * The main backing context object is VRSContext, which is also a singleton per platform.
 */
@Slf4j
public class BrowserPlatform {

    private static final Map<String, BrowserPlatform> platforms = new Hashtable<>();

    /**
     * Get specific BrowserPlatform. Multiple browser platforms may be register/created in one
     * single JVM.
     */
    public static BrowserPlatform getInstance(String instanceId) {
        BrowserPlatform instance;

        synchronized (platforms) {
            instance = platforms.get(instanceId);

            if (instance == null) {
                try {
                    instance = new BrowserPlatform(instanceId);
                    platforms.put(instanceId, instance);
                } catch (Exception e) {
                    log.error("FATAL: Could not initialize browser platform:'{}'!", instanceId);
                    log.error("Exception during initialization", e);
                }
            }
        }
        // load default?
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
    private UIProperties guiSettings;
    private ViewerEventDispatcher viewerEventDispatcher;
    private final List<ProxyBrowserController> browsers = new ArrayList<ProxyBrowserController>();
    private UIPropertiesSaver uiPropertiesSaver;

    protected BrowserPlatform(String id) throws Exception {
        init(id);
    }

    private void init(String id) throws Exception {
        // init defaults:
        this.platformID = id;
        this.proxyRegistry = ProxyFactoryRegistry.createInstance();

        // ===================================
        // Init VRS Classes
        // ===================================
        initVRSContext();

        // ===================================
        // Swing resources and producers
        // ===================================
        this.uiPropertiesSaver = new UIPropertiesSaver(getVRSContext());

        // root Frame and Icon Renderer/provider:
        this.rootFrame = new JFrame();

        // ===================================
        // Init Viewers and ViewerPlugins.
        // ===================================
        this.viewerEventDispatcher = new ViewerEventDispatcher(true);
        initViewers();
    }

    protected void initVRSContext() {
        VRSProperties props = new VRSProperties("VRSBrowserProperties");
        this.vrsContext = new VRSContext(props);
    }

    public void setPersistentConfigLocation(VRL configHome, boolean enablePersistantConfig) {
        this.getVRSContext().setPersistantConfigLocation(configHome, enablePersistantConfig);
    }

    /**
     * @return persistent configuration directory where to store properties for example
     * '$HOME/.vrsrc/'. <br>
     * Is null for non-persistent platforms.
     */
    public VRL getPersistantConfigLocation() {
        return this.getVRSContext().getPersistantConfigLocation();
    }

    protected void initViewers() throws Exception {
        // create custom sub-directory for viewer settings. 
        ViewerResourceLoader resourceHandler = new ViewerResourceLoader(vrsContext, "viewers");
        // Viewer Registry for this Platform:
        this.viewerRegistry = new PluginRegistry(resourceHandler);
    }

    public VRSContext getVRSContext() {
        return vrsContext;
    }

    public void registerVRSFactory(Class<? extends VResourceSystemFactory> clazz) throws Exception {
        this.getVRSContext().getRegistry().registerFactory(clazz);
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
     * @param show - show browser frame.
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
        return DnDUtil.getDefaultTransferHandler(); // TODO: static instance !
    }

    /**
     * @return Viewer and other plug-in registry for this platform.
     */
    public PluginRegistry getViewerRegistry() {
        return viewerRegistry;
    }

    public VRL createCustomConfigDir(String subPath) throws Exception {
        VRL configDir = this.getVRSContext().getPersistantConfigLocation();

        if (configDir == null) {
            return null;
        }

        return configDir.resolvePath(subPath);
    }

    /**
     * @return Icon Factory for this platform.
     */
    public IconProvider getIconProvider() {

        // lazy loading:
        synchronized (this) {
            if (this.iconProvider == null) {
                this.iconProvider = new IconProvider(rootFrame, new ResourceLoader(new VRSClient(vrsContext)));
            }
        }

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
        log.debug("{}:register():{}", this, browser);
        this.browsers.add(browser);
        return browser;
    }

    protected void unregister(ProxyBrowserController browser) {
        log.debug("{}:unregister():{}", this, browser);
        this.browsers.remove(browser);
        if (this.browsers.size() <= 0) {
            log.info("Closed last browser for this Platform:{}. Initiating shutdown.", this.getPlatformID());
            shutDown();
        }
    }

    public String getAboutText() {
        return "" + //
                "      === Platinum Toolkit ===     \n" + //
                "  VBrowser 2.0 (Under construction)\n" + //
                "                                   \n";
    }

    public void initLookAndFeel() {
        if (this.getGuiSettings().getLaFEnabled()) {
            this.switchLookAndFeelType(rootFrame, guiSettings.getLAFType(),true);
        }
    }

    public void switchLookAndFeelType(JFrame browserFrame, LookAndFeelType lafType, boolean enable) {


//        if (!SwingUtilities.isEventDispatchThread()) {
//            log.info("switchLookAndFeelType():{} => invokeLater()!", lafType);
//
//            SwingUtilities.invokeLater(new Runnable() {
//                @Override
//                public void run() {
//                    switchLookAndFeelType(lafType);
//                }
//            });
//            return;
//        } else {
//            log.info("switchLookAndFeelType():{}", lafType);
//        }

        if ((lafType==null) || (!enable)) {
            // switch back for now and disable.
            lafType=NATIVE;
        }

        try {
            switch (lafType) {
                case NATIVE:
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    break;
                case DEFAULT:
                case METAL:
                    // On linux this is also default:
                    UIManager.setLookAndFeel(javax.swing.plaf.metal.MetalLookAndFeel.class.getCanonicalName());
                    break;
                case WINDOWS:
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                    break;
                case GTK:
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                    break;
//                case PLASTIC_3D:
//                    UIManager.setLookAndFeel(Plastic3DLookAndFeel.class.getCanonicalName());
//                    break;
//                case PLASTIC_XP:
//                    UIManager.setLookAndFeel(PlasticXPLookAndFeel.class.getCanonicalName());
//                    break;
                case NIMBUS:
                    UIManager.setLookAndFeel(NimbusLookAndFeel.class.getCanonicalName());
                    break;
//                case SEAGLASS:
//                    UIManager.setLookAndFeel(SeaGlassLookAndFeel.class.getCanonicalName());
//                    break;
                case MOTIF:
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                    break;
                default:
                    log.warn("Look and feel not recognised:{}", lafType);
                    return;
                //break;
            }
            this.guiSettings.setLAFType(lafType);
            SwingUtilities.updateComponentTreeUI(browserFrame);
            browserFrame.pack();

        } catch (Exception e) {
            log.error("Failed to switch Look and Feel:" + lafType, e);
            e.printStackTrace();
        }
    }


    // ==========================
    // Persistent UI Properties.
    // ==========================

    /**
     * Load/Reload properties. Call this again if persistent config settings have changed.
     *
     * @return
     */
    public UIProperties loadUIProperties() throws VrsException {
        VRL propLoc=getGUISettingsLoc();
        if (propLoc != null) {
            if (new VRSClient(vrsContext).existsFile(propLoc)) {
                this.guiSettings = uiPropertiesSaver.loadFrom(propLoc);
                return this.guiSettings;
            }
        }
        this.guiSettings = new UIProperties();
        return guiSettings;
    }

    public void saveUIProperties() throws VrsException {
        if (!getVRSContext().hasPersistantConfig()) {
            return;
        }
        this.uiPropertiesSaver.saveProperties(this.guiSettings, getGUISettingsLoc());
    }

    public VRL getGUISettingsLoc() throws VRLSyntaxException {
        return this.getVRSContext().getPersistantConfigLocation().resolvePath(this.platformID + ".props");
    }
    // ==============  
    // Dispose/Misc.
    // ==============

    /**
     * Try to perform graceful shutdown.
     */
    public void shutDown() {
        dispose();
        // Use nano sleep to allow for thread switching and cleanup.
        try {
            Thread.sleep(0, 42);
        } catch (InterruptedException e) {
            log.warn("Interrupted during shutdown:" + e.getMessage(), e);
        }
        log.info("Post shutdown().");
    }

    /**
     * Immediately close and dipspose all registered resources;
     */
    public void dispose() {
        if (rootFrame!=null) {
            this.rootFrame.dispose();
        }
        this.viewerEventDispatcher.stop();
        this.viewerEventDispatcher.dispose();

        if (vrsContext != null) {
            vrsContext.dispose();
        }
        this.vrsContext = null;
    }

    public String toString() {
        return "BrowserPlatform:[platformID='" + this.platformID + "']";
    }

}
