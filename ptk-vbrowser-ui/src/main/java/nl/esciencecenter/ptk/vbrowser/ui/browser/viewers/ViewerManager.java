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

import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserFrame;
import nl.esciencecenter.ptk.vbrowser.ui.browser.ProxyBrowserController;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.viewers.PluginRegistry;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerContext;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerFrame;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerPlugin;
import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerEvent;
import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerEventType;
import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerListener;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Manages all the embedded viewers inside and outside the VBrowser.
 */
public class ViewerManager implements ViewerListener {

    private static Logger logger = LoggerFactory.getLogger(ViewerManager.class);

    public class FrameWatcher implements WindowListener {

        protected ViewerFrame frame;

        public FrameWatcher(ViewerFrame frame) {
            this.frame = frame;
        }

        @Override
        public void windowOpened(WindowEvent e) {
        }

        @Override
        public void windowClosing(WindowEvent e) {
            ViewerManager.this.viewerFrameClosing(frame, e);
        }

        @Override
        public void windowClosed(WindowEvent e) {
        }

        @Override
        public void windowIconified(WindowEvent e) {
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
        }

        @Override
        public void windowActivated(WindowEvent e) {
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
        }

    }

    private ProxyBrowserController browserController;

    public ViewerManager(ProxyBrowserController proxyBrowser) {
        browserController = proxyBrowser;
    }

    public PluginRegistry getViewerRegistry() {
        return browserController.getPlatform().getViewerRegistry();
    }

    public void register(ViewerPlugin viewer) {
        viewer.addViewerListener(this);
    }

    public void unregister(ViewerPlugin viewer) {
        viewer.removeViewerListener(this);
    }

    /**
     * Factory method to instanciate a viewer, do not start or register the instance.
     *
     * @param resourceType   - optional resource type.
     * @param mimeType       - optional mimeType
     * @param resourceStatus -option resource Statys
     * @param optViewerClass - optional preferred ViewerClass
     * @return instanciated ViewerPlugin
     * @throws ProxyException
     */
    public ViewerPlugin createViewerFor(String resourceType, String mimeType, String resourceStatus,
                                        String optViewerClass) throws ProxyException {
        PluginRegistry registry = getViewerRegistry();

        Class<?> clazz = null;

        if (optViewerClass != null) {
            clazz = loadViewerClass(optViewerClass);
        }

        if ((clazz == null) && (mimeType != null)) {
            if (clazz == null) {
                clazz = registry.getMimeTypeViewerClass(mimeType, resourceType, resourceStatus);
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

    public ViewerContext createViewerContext(String optMenuMethod, VRL vrl, boolean standaloneWindow) {

        ViewerContext context = new ViewerContext(getViewerRegistry(), browserController.getUI(), optMenuMethod, vrl,
                standaloneWindow);

        context.setViewerEventDispatcher(browserController.getPlatform().getViewerEventDispatcher());

        return context;
    }

    public void viewerFrameClosing(ViewerFrame frame, WindowEvent e) {
        ViewerPlugin viewer = frame.getViewer();
        logger.debug("WindowsClosing:{}", viewer);
        try {
            viewer.disposeViewer();
            unregister(viewer);
            frame.dispose();
        } catch (Throwable ex) {
            this.handleException("Exception when disposing viewer:" + viewer, ex);
        }
    }

    @Override
    public void notifyEvent(ViewerEvent theEvent) {
        logger.debug("ViewerEvent:{}", theEvent);
        ViewerPlugin viewer = theEvent.getEventSource().getViewer();

        if (theEvent.getEventType() == ViewerEventType.VIEWER_DISPOSED) {
            unregister(viewer);
        }
    }

    public void startStandaloneViewer(ViewerPlugin viewer, VRL vrl, String optMenuMethod, boolean start) {
        startViewer(null, true, viewer, vrl, optMenuMethod, start);
    }

    public void startEmbeddedViewer(BrowserFrame frame, ViewerPlugin viewer, VRL vrl, String optMenuMethod,
                                    boolean start) {
        startViewer(frame, false, viewer, vrl, optMenuMethod, start);
    }

    public void startViewer(BrowserFrame browserFrame, boolean standaloneWindow, ViewerPlugin viewer, VRL vrl,
                            String optMenuMethod, boolean start) {

        ViewerContext context = createViewerContext(optMenuMethod, vrl, standaloneWindow);

        if (standaloneWindow || viewer.isStandaloneViewer()) {

            ViewerFrame frame = new ViewerFrame(viewer);
            frame.addWindowListener(new FrameWatcher(frame));
            frame.initViewer(context);
            frame.pack();
            frame.setSize(frame.getPreferredSize());
            frame.setLocationRelativeTo(browserFrame);
            frame.setVisible(true);
        } else {
            browserFrame.addViewerPanel(viewer, true);
            viewer.initViewer(context);
        }

        register(viewer);

        if (start) {
            try {
                viewer.startViewer(vrl, optMenuMethod);
            } catch (VrsException ex) {
                browserController.handleException("Failed to start Viewer with vrl:" + vrl, ex);
            }
        }
    }

    private void handleException(String message, Throwable ex) {
        browserController.handleException(message, ex);
    }

}
