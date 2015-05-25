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
import nl.esciencecenter.ptk.vbrowser.ui.tool.vtermstarter.VTermStarter;
import nl.esciencecenter.ptk.vbrowser.viewers.loboviewer.LoboBrowser;
import nl.esciencecenter.ptk.vbrowser.viewers.loboviewer.LoboBrowserInit;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.infors.InfoRootNode;
import nl.esciencecenter.vbrowser.vrs.sftp.SftpFileSystemFactory;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Start VBrowser with Virtual Resource System.
 */
public class StartVRSBrowser {
    
    public static void main(String args[]) {
        try {
            startVBrowser(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BrowserPlatform getPlatform() {
        return BrowserPlatform.getInstance("vbrowser");
    }

    public static void initVRSPlugins(VRSContext context) {
        try {
            context.getRegistry().registerFactory(SftpFileSystemFactory.class);
        } catch (InstantiationException | IllegalAccessException e) {
            handleError("Couldn't register VRS:" + SftpFileSystemFactory.class.getCanonicalName(), e);
        }

    }

    private static void handleError(String message, Throwable e) {
        System.err.printf("Exception:%s:%s\n", message, e);
        e.printStackTrace();
    }

    public static void initVRSViewers(BrowserPlatform platform) {
        VRSContext context = platform.getVRSContext();
        platform.getViewerRegistry().registerPlugin(VTermStarter.class);

        try
        {
            context.getRegistry().registerFactory(nl.esciencecenter.ptk.vbrowser.viewers.loboviewer.resfs.ResFS.class);
            platform.getViewerRegistry().registerPlugin(LoboBrowser.class);
            LoboBrowserInit.initPlatform(platform);
        }
        catch (Throwable e) { 
            handleError("Couldn't register LoboBrowser |+ResFS", e);
        }
    }

    public static ProxyBrowserController startVBrowser(String args[]) throws Exception {
        // BrowserPlatform/Context
        BrowserPlatform platform = getPlatform();

        // VRSContext
        VRSContext context = platform.getVRSContext();

        // Setting the persistant configuration location before initializing the plugins allow
        // the plugins to read from the configuration.
        VRL config = context.getHomeVRL().resolvePath(".vrsrc");
        platform.setPersistantConfigLocation(config, false);

        // Init VRSRegistry/VRSPlugins
        initVRSPlugins(context);
        initVRSViewers(platform);

        // Now enable location to load()/save
        platform.setPersistantConfigLocation(config, true);

        // Actual ProxyBrowser
        ProxyBrowserController browser = (ProxyBrowserController) platform.createBrowser();
        VRSProxyFactory fac = VRSProxyFactory.createFor(platform);
        platform.registerProxyFactory(fac);

        // Start with Root InfoNode:
        InfoRootNode rootNode = fac.getVRSClient().getInfoRootNode();
        rootNode.loadPersistantConfig();

        String user = context.getUserName();
        // Add default links, will be ignored if already exists.

        rootNode.addResourceLink("My Links", "Root:/", new VRL("file:///"), null, false);
        rootNode.addResourceLink("My Links", "Home/", context.getHomeVRL(), null, false);
        rootNode.addResourceLink("My Links", "sftp://" + user + "@localhost:22/", new VRL("sftp://" + user
                + "@localhost:22/"), null, false);

        // main location to start browsing:
        ProxyNode root = fac.openLocation("info:/");
        browser.setRoot(root, true, true);

        return browser;
    }

}
