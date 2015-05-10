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

package nl.esciencecenter.ptk.vbrowser.ui.browser.viewers;

import nl.esciencecenter.ptk.vbrowser.ui.browser.ProxyBrowserController;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.viewers.PluginRegistry;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerContext;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerFrame;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerPlugin;

/**
 * Manages all the embedded viewers inside and outside the VBrowser.
 */
public class ViewerManager {

    private ProxyBrowserController browser;

    protected boolean filterOctetStreamMimeType = true;

    public ViewerManager(ProxyBrowserController proxyBrowser) {
        browser = proxyBrowser;
    }

    public PluginRegistry getViewerRegistry() {
        return browser.getPlatform().getViewerRegistry();
    }

    public ViewerPlugin
            createViewerFor(String resourceType, String mimeType, String optViewerClass)
                    throws ProxyException {
        PluginRegistry registry = getViewerRegistry();

        Class<?> clazz = null;

        if (optViewerClass != null) {
            clazz = loadViewerClass(optViewerClass);
        }

        if ((clazz == null) && (mimeType != null)) {
            if (clazz == null) {
                clazz = registry.getMimeTypeViewerClass(mimeType);
            }
        }

        if (clazz == null)
            return null;

        if (ViewerPlugin.class.isAssignableFrom(clazz) == false) {
            throw new ProxyException("Viewer Class is not a ViewerPlugin class:" + clazz);
        }

        if (ProxyViewer.class.isAssignableFrom(clazz)) {
            // skip internal viewers for now, need different constructor.
            return null;
        }

        ViewerPlugin viewer = registry.createViewer((Class<? extends ViewerPlugin>) clazz);
        return viewer;
    }

    private Class<?> loadViewerClass(String optViewerClass) {
        try {
            return this.getClass().getClassLoader().loadClass(optViewerClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ViewerFrame createViewerFrame(ViewerPlugin viewer, ViewerContext context,
            boolean initViewer) {
        return ViewerFrame.createViewerFrame(viewer, context, initViewer);
    }
}
