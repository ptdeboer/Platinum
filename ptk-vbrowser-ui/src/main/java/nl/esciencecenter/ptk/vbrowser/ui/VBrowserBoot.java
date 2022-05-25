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

package nl.esciencecenter.ptk.vbrowser.ui;

import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.browser.ProxyBrowserController;
import nl.esciencecenter.ptk.vbrowser.ui.browser.viewers.ProxyPropertiesEditor;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.vrs.VRSProxyFactory;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerPlugin;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsRuntimeException;
import nl.esciencecenter.vbrowser.vrs.infors.InfoRootNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Start vanilla VBrowser with Virtual Info Resource Root.
 * For a full VRS initialized version: see StartVBrowser.
 * The default boot does not register extra and/or external plugins.
 */
public class VBrowserBoot {

    public static void main(String[] args) {
        try {
            new VBrowserBoot().start(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleError(String message, Throwable e) {
        // assume no logging framework available:
        System.err.printf("Exception:%s:%s\n", message, e);
        e.printStackTrace();
    }

    public static BrowserPlatform getBrowserPlatform() {
        // BrowserPlatform
        return BrowserPlatform.getInstance("vbrowser");
    }

    // ===
    // Starter
    // ===
    private String confDir = ".vrsrc";
    private Class<? extends VResourceSystemFactory>[] vrsClasses;
    private String[][] defaultLinks;
    private Class<? extends ViewerPlugin>[] viewers;

    public VBrowserBoot withViewers(Class<? extends ViewerPlugin>[] viewers) {
        this.viewers = viewers;
        return this;
    }

    public VBrowserBoot withVRSPlugins(Class<? extends VResourceSystemFactory>[] vrsClasses) {
        this.vrsClasses = vrsClasses;
        return this;
    }

    public VBrowserBoot withConfigDir(String confDir) {
        this.confDir = confDir;
        return this;
    }

    public VBrowserBoot withDefaultLinks(String[][] links) {
        this.defaultLinks = links;
        return this;
    }

    public ProxyBrowserController start(String[] args) {

        try {
            BrowserPlatform platform = getBrowserPlatform();

            // Default VRSContext
            VRSContext context = platform.getVRSContext();

            // Setting the persistent configuration location before initializing the plugins allow
            // the plugins to read from the configuration, but not already writing to it.
            VRL config = context.getHomeVRL().resolvePath(confDir);
            platform.setPersistentConfigLocation(config, false);

            // Init VRSRegistry/VRSPlugins
            registerInternalPlugins(platform);
            registerVRSPlugins(context, this.vrsClasses);
            registerVRSViewers(platform, this.viewers);

            // Now enable location to load()/save configurations.
            platform.setPersistentConfigLocation(config, true);
            platform.loadUIProperties(true);
            platform.initLookAndFeel();

            // ProxyBrowser
            ProxyBrowserController browser = (ProxyBrowserController) platform.createBrowser();
            VRSProxyFactory fac = VRSProxyFactory.createFor(platform);
            platform.registerProxyFactory(fac);

            // Start with Root InfoNode:
            InfoRootNode rootNode = fac.getVRSClient().getInfoRootNode();
            rootNode.loadPersistantConfig();

            // Add optional 'Favorites' if it doesn't exist yet
            String myLinksName = "Favorites";
            boolean myLinksExists = rootNode.getSubNodes().stream()
                    .anyMatch(n -> n.getName().equals(myLinksName));

            if ((defaultLinks != null) && (!myLinksExists)) {
                for (String[] link : defaultLinks) {
                    rootNode.addResourceLink(myLinksName, link[0], new VRL(link[1]), true, null, false);
                }
                rootNode.save();
            }

            // Main virtual root to browsing. Use the 'info' system resource:
            ProxyNode root = fac.openLocation("info:/");
            browser.setRoot(root, true, true);

            return browser;
        } catch (Exception e) {
            handleError("Fatal: couldn't initialize VBrowser platform:" + e.getMessage(), e);
            throw new VrsRuntimeException(e.getMessage(), e);
        }
    }

    private void registerInternalPlugins(BrowserPlatform platform) {
        platform.getViewerRegistry().registerPlugin(ProxyPropertiesEditor.class);
    }

    public void registerVRSPlugins(VRSContext context, Class<? extends VResourceSystemFactory>[] vrsClasses) {
        if (vrsClasses == null) {
            return;
        }
        for (int i = 0; i < vrsClasses.length; i++) {
            try {
                context.getRegistry().registerFactory(vrsClasses[i]);
            } catch (InstantiationException | IllegalAccessException e) {
                handleError("Couldn't register VRS:" + vrsClasses[i].getCanonicalName(), e);
            }
        }
    }

    public void registerVRSViewers(BrowserPlatform platform, Class<? extends ViewerPlugin>[] viewers) {
        if (viewers == null) {
            return;
        }

        for (int i = 0; i < viewers.length; i++) {
            try {
                platform.getViewerRegistry().registerPlugin(viewers[i]);
            } catch (Throwable e) {
                handleError("Couldn't register VRS:" + viewers[i].getCanonicalName(), e);
            }
        }
    }
}
