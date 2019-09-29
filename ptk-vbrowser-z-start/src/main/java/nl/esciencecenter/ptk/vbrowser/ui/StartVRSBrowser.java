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
import nl.esciencecenter.ptk.vbrowser.ui.tool.vtermstarter.VTermStarter;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerPlugin;
import nl.esciencecenter.ptk.vbrowser.viewers.loboviewer.LoboBrowser;
import nl.esciencecenter.ptk.vbrowser.viewers.loboviewer.LoboBrowserInit;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.sftp.SftpFileSystemFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Start VBrowser with Virtual Resource System and configure Viewers and VRSPlugin extensions.
 */
public class StartVRSBrowser {

    public static void main(String[] args) {
        try {
            new StartVRSBrowser().start(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ProxyBrowserController start(String[] args) {

        String[][] links = new String[][]{
                {"Root:/", "file:///"},
                {"Home:~/", "file:///home/piter"},
                {"sftp://localhost/", "sftp://localhost/"}
        };

        // Static platform available pre-initailization.
        BrowserPlatform platform = new StartVBrowser().getPlatform();
        // Legacy static bindings:
        LoboBrowserInit.initPlatform(platform);

        ProxyBrowserController browser = new StartVBrowser()
                .withVRSPlugins(createVRSPlugins())
                .withViewers(createViewerPlugins())
                .withConfigDir(".vrsrc")
                .withDefaultLinks(links)
                .start(args);

        return browser;
    }

    public Class<? extends VResourceSystemFactory>[] createVRSPlugins() {
        List<Class<? extends VResourceSystemFactory>> vrsFactories = new ArrayList<>();
        vrsFactories.add(SftpFileSystemFactory.class);
        vrsFactories.add(nl.esciencecenter.ptk.vbrowser.viewers.loboviewer.resfs.ResFS.class);
        return vrsFactories.toArray(new Class[0]);
    }

    public Class<? extends ViewerPlugin>[] createViewerPlugins() {
        List<Class<? extends ViewerPlugin>> viewers = new ArrayList<>();
        viewers.add(LoboBrowser.class);
        viewers.add(VTermStarter.class);
        return viewers.toArray(new Class[0]);
    }

}
