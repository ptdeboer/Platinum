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

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import ch.randelshofer.quaqua.snow_leopard.Quaqua16SnowLeopardLookAndFeel;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.seaglasslookandfeel.SeaGlassLookAndFeel;
import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.ui.icons.IconProvider;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.ptk.vbrowser.ui.browser.laf.LookAndFeelType;
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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Browser Platform. Typically one Platform instance per application environment is created.
 * The BrowserPlatform object contains all configurable context(s) in an non static environment.
 * The main context object is VRSContext.
 */
@Slf4j
public class BrowserPlatform {

    private static Map<String, BrowserPlatform> platforms = new Hashtable<String, BrowserPlatform>();

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
                    instance.switchLookAndFeelType(LookAndFeelType.NIMBUS);
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
//    private VRSContext vrsContext;
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
        VRSContext vrsContext = new VRSContext(props);
        this.vrsClient = new VRSClient(vrsContext);
    }

    public void setPersistantConfigLocation(VRL configHome, boolean enablePersistantConfig) {
        this.getVRSContext().setPersistantConfigLocation(configHome, enablePersistantConfig);
    }

    /**
     * @return persistent configuration directory where to store properties for example
     *         '$HOME/.mypropertiesrc/'. <br>
     *         Is null for non persistent platforms.
     */
    public VRL getPersistantConfigLocation() {
        return this.getVRSContext().getPersistantConfigLocation();
    }

    protected void initViewers() throws Exception {
        // create custom sub-directory for viewer settings. 
        ViewerResourceLoader resourceHandler = new ViewerResourceLoader(vrsClient, "viewers");
        // Viewer Registry for this Platform:
        this.viewerRegistry = new PluginRegistry(resourceHandler);
    }

    public VRSContext getVRSContext() {
        return vrsClient.getVRSContext();
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
            log.info(">>> Closed *last* browser for this Platform:{}", this.getPlatformID());
            shutDown();
        }
    }

    public String getAboutText() {
        return "" + //
                "      === Platinum Toolkit ===     \n" + //
                "  VBrowser 2.0 (Under construction)\n" + //
                "                                   \n";
    }

    public void switchLookAndFeelType(LookAndFeelType lafType) {
        if (!SwingUtilities.isEventDispatchThread()) {
            log.info("switchLookAndFeelType():{} => invokeLater()!",lafType);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    switchLookAndFeelType(lafType);
                }
            });
            return;
        } else {
            log.info("switchLookAndFeelType():{}",lafType);
        }

        try {
            switch (lafType) {
                case DEFAULT:
                case METAL:
                    // "javax.swing.plaf.metal.MetalLookAndFeel"
                    javax.swing.UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    break;
                case NATIVE:
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    break;
                case WINDOWS:
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                    break;
                case GTK:
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                    break;
                case KDEQT:
                    //org.freeasinspeech.kdelaf.KdeLAF
                    break;
                case PLASTIC_3D:
                    UIManager.setLookAndFeel(Plastic3DLookAndFeel.class.getCanonicalName());
                    break;
                case PLASTIC_XP:
                    UIManager.setLookAndFeel(PlasticXPLookAndFeel.class.getCanonicalName());
                    break;
                case QUAQUA:
                     UIManager.setLookAndFeel(Quaqua16SnowLeopardLookAndFeel.class.getCanonicalName());
                    break;
                case NIMBUS:
                    UIManager.setLookAndFeel(NimbusLookAndFeel.class.getCanonicalName());
                    break;
                case SEAGLASS:
                    UIManager.setLookAndFeel(SeaGlassLookAndFeel.class.getCanonicalName());
                    break;
                default:
                    log.warn("Look and feel not recognised:{}",lafType);
                    break;
            }
        }
        catch (Exception e) {
            log.error("Failed to switch Look and Feel:"+lafType,e);
            e.printStackTrace();
        }
        // load()/save()
    }
    // ==============  
    // Dispose/Misc.
    // ==============

    /**
     * Try to perform graceful shutdown.
     */
    public void shutDown() {
        dispose();
    }

    /**
     * Immediately close and dipsose all registered resources;
     */
    public void dispose() {
        if (vrsClient!=null) { 
            vrsClient.dispose();
        }
        if (getVRSContext()!=null) {
            getVRSContext().dispose();
        }
        this.viewerEventDispatcher.stop();
        this.viewerEventDispatcher.dispose();
        this.vrsClient = null;
    }

    public String toString() {
        return "BrowserPlatform:[platformID='" + this.platformID + "']";
    }


}
