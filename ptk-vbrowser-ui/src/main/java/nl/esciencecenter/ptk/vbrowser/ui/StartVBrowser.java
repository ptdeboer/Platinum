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
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.vrs.VRSProxyFactory;
import nl.esciencecenter.ptk.vbrowser.viewers.PluginRegistry;
import nl.esciencecenter.ptk.vbrowser.viewers.ToolPlugin;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerPlugin;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsRuntimeException;
import nl.esciencecenter.vbrowser.vrs.infors.InfoRootNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Start vanilla VBrowser with Virtual Info Resource Root.
 * For a full VRS initialized version: see StartVRSBrowser.
 */
public class StartVBrowser {


    public static void main(String[] args) {
        try {
            new StartVBrowser().start(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BrowserPlatform getPlatform() {
        // vbrowser
        return BrowserPlatform.getInstance("vbrowser");
    }

    private static void handleError(String message, Throwable e) {
        // assume no logging framework available:
        System.err.printf("Exception:%s:%s\n", message, e);
        e.printStackTrace();
    }

    // ===
    // Starter
    // ===
    private String confDir = ".vrsrc";
    private Class<? extends VResourceSystemFactory>[] vrsClasses;
    private String[][] defaultLinks;
    private Class<? extends ViewerPlugin>[] viewers;

    public StartVBrowser withViewers(Class<? extends ViewerPlugin>[] viewers) {
        this.viewers = viewers;
        return this;
    }

    public StartVBrowser withVRSPlugins(Class<? extends VResourceSystemFactory>[] vrsClasses) {
        this.vrsClasses = vrsClasses;
        return this;
    }

    public StartVBrowser withConfigDir(String confDir) {
        this.confDir = confDir;
        return this;
    }

    public StartVBrowser withDefaultLinks(String[][] links) {
        this.defaultLinks = links;
        return this;
    }

    public ProxyBrowserController start(String[] args) {

        try {
            // BrowserPlatform
            BrowserPlatform platform = getPlatform();

            // VRSContext
            VRSContext context = platform.getVRSContext();

            // Setting the persistant configuration location before initializing the plugins allow
            // the plugins to read from the configuration, but not already writing to it.
            VRL config = context.getHomeVRL().resolvePath(confDir);
            platform.setPersistantConfigLocation(config, false);

            // Init VRSRegistry/VRSPlugins
            initVRSPlugins(context, vrsClasses);
            initVRSViewers(platform, this.viewers);

            // Now enable location to load()/save
            platform.setPersistantConfigLocation(config, true);

            // ProxyBrowser
            ProxyBrowserController browser = (ProxyBrowserController) platform.createBrowser();
            VRSProxyFactory fac = VRSProxyFactory.createFor(platform);
            platform.registerProxyFactory(fac);

            // Start with Root InfoNode:
            InfoRootNode rootNode = fac.getVRSClient().getInfoRootNode();
            rootNode.loadPersistantConfig();

            if (defaultLinks != null) {
                for (String[] link : defaultLinks) {
                    // Add default links, will be ignored if already exists.
                    rootNode.addResourceLink("My Links", link[0], new VRL(link[1]), null, false);
                }
            }

            // main location to start browsing:
            ProxyNode root = fac.openLocation("info:/");
            browser.setRoot(root, true, true);

            return browser;
        } catch (Exception e) {
            handleError("Fatal: couldn't initialize VBrowser platform:" + e.getMessage(), e);
            throw new VrsRuntimeException(e.getMessage(), e);
        }
    }

    public void initVRSPlugins(VRSContext context, Class<? extends VResourceSystemFactory>[] vrsClasses) {
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

    public void initVRSViewers(BrowserPlatform platform, Class<? extends ViewerPlugin>[] viewers) {
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
