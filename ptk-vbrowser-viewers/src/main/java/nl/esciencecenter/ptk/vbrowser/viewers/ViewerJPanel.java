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

package nl.esciencecenter.ptk.vbrowser.viewers;

import nl.esciencecenter.ptk.object.Disposable;
import nl.esciencecenter.ptk.ui.dialogs.ExceptionDialog;
import nl.esciencecenter.ptk.ui.icons.IconProvider;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerEvent;
import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerEventDispatcher;
import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerEventSource;
import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerListener;
import nl.esciencecenter.ptk.vbrowser.viewers.vrs.ViewerResourceLoader;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Embedded Viewer JPanel and abstract ViewerPlugin adaptor for VBrowser Viewers and Tools.
 */
public abstract class ViewerJPanel extends JPanel implements Disposable, ViewerPlugin,
        MimeViewer, ViewerEventSource {
    // === Instance === //

    private JPanel innerPanel;
    private VRL viewedUri;
    private boolean isBusy;
    private ViewerContext viewerContext;

    protected Cursor busyCursor = new Cursor(Cursor.WAIT_CURSOR);
    protected Properties properties;
    protected Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    protected IconProvider iconProvider = null;

    protected ViewerJPanel() {
        this.setLayout(new BorderLayout());
    }

    protected PluginRegistry getViewerRegistry() {
        if (viewerContext != null) {
            return viewerContext.getPluginRegistry();
        }

        return null;
    }

    /**
     * Add custom content to this panel.
     *
     * @return
     */
    public JPanel getContentPanel() {
        return this;
    }

    @Override
    public ViewerPlugin getViewer() {
        return this;
    }

    public JPanel initInnerPanel() {
        this.innerPanel = new JPanel();
        this.add(innerPanel, BorderLayout.CENTER);
        this.innerPanel.setLayout(new FlowLayout());
        return innerPanel;
    }

    final public VRL getVRL() {
        return viewedUri;
    }

    final protected void setVrl(VRL vrl) {
        this.viewedUri = vrl;
    }

    /**
     * Whether Viewer has its own ScrollPane. If not the parent Component might embedd the viewer
     * into a ScrollPanel.
     *
     * @return whethee viewer manages its own scrolling/scrolpane.
     */
    public boolean haveOwnScrollPane() {
        return false;
    }

    /**
     * Whether to start this viewer always in a StandAlone Dialog/Frame. Some Viewers are not
     * embedded viewers and must be started in a seperate Window.
     *
     * @return whether viewer is a stand-alone viewer which must be started in its own window
     * (frame).
     */
    public boolean isStandaloneViewer() {
        return false;
    }

    public boolean isStartedAsStandalone() {
        return this.getViewerContext().getStartedAsStandalone();
    }

    /**
     * Set title of master frame or Viewer tab.
     */
    public void setViewerTitle(final String name) {
        this.setName(name);

        // also update JFrame
        if (isStandaloneViewer()) {
            JFrame frame = getJFrame();
            if (frame != null) {
                getJFrame().setTitle(name);
            }
        }
    }

    /**
     * Returns parent ViewerFrame (JFrame) if contained in one. Might return NULL if parent is not a
     * JFrame. Uses getTopLevelAncestor() to get the (AWT) toplevel component.
     *
     * @return the containing JFrame or null.
     * @see javax.swing.JComponent#getTopLevelAncestor()
     */
    final public ViewerFrame getJFrame() {
        Container topcomp = this.getTopLevelAncestor();

        // stand-alone viewer must be embedded in a ViewerFrame.
        if (topcomp instanceof ViewerFrame) {
            return ((ViewerFrame) topcomp);
        }

        return null;
    }

    final protected boolean hasJFrame() {
        return (this.getJFrame() != null);
    }

    /**
     * If this panel is embedded in a (J)Frame, request that the parent JFrame performs a pack() and
     * resizes the Frame to the preferred size. If this viewer is embedded in another panel, the
     * method will not perform a resize and return false.
     *
     * @return true if frame could perform pack, although the actual pack() might be delayed.
     */
    final public boolean requestFramePack() {
        JFrame frame = getJFrame();

        // only pack stand alone viewers embedded in ViewerFrames.
        if ((frame == null) || ((frame instanceof ViewerFrame) == false)) {
            return false;
        }

        frame.pack();
        return true;
    }

    /**
     * Stop and dispose Viewer. Calls stopViewer() and disposeViewer().
     */
    public final boolean closeViewer() {
        stopViewer();
        disposeViewer();
        return true;
    }

    /**
     * Inherited method from Disposable. Calss disposeViewer();
     */
    @Override
    final public void dispose() {
        disposeViewer();
    }

    @Override
    final public void initViewer(ViewerContext viewerContext) {
        this.viewerContext = viewerContext;
        doInitViewer();
    }

    final public ViewerContext getViewerContext() {
        return viewerContext;
    }

    @Override
    final public void startViewer(VRL vrl, String optMenuMethod) throws VrsException {
        setVrl(vrl);
        doStartViewer(vrl, optMenuMethod);
        fireStarted();
    }

    @Override
    final public void stopViewer() {
        doStopViewer();
        fireStopped();
    }

    @Override
    final public void disposeViewer() {
        try {
            doDisposeViewer();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        disposeFrame();
        fireDisposed();
    }

    protected boolean disposeFrame() {

        JFrame frame = this.getJFrame();
        if (frame == null) {
            return false;
        }
        frame.setVisible(false);
        frame.dispose();
        return true;
    }

    /**
     * An embedded viewer is a JPanel. The method return this component.
     */
    @Override
    public ViewerJPanel getViewerPanel() {
        return this;
    }

    /**
     * @return embedded resource loader.
     */
    public ViewerResourceLoader getResourceHandler() {
        PluginRegistry reg = getViewerRegistry();

        if (reg == null)
            return null;

        return reg.getResourceHandler();
    }

    // =========================================================================
    // Gui
    // =========================================================================

    public Cursor getBusyCursor() {
        return busyCursor;
    }

    public void setBusyCursor(Cursor busyCursor) {
        this.busyCursor = busyCursor;
    }

    public Cursor getDefaultCursor() {
        return defaultCursor;
    }

    public void setDefaultCursor(Cursor defaultCursor) {
        this.defaultCursor = defaultCursor;
    }

    public String getURIBasename() {
        return getVRL().getBasename();
    }

    protected ViewerResourceLoader getResourceLoader() {
        return this.getResourceHandler();
    }

    protected IconProvider getIconProvider() {
        if (this.iconProvider == null) {
            iconProvider = getResourceLoader().createIconProvider(this);
        }

        return iconProvider;
    }

    protected Icon getIconOrBroken(String iconUrl) {
        return getIconProvider().getIconOrBroken("icons/" + iconUrl);
    }

    /**
     * Returns most significant Class Name
     */
    public String getViewerClass() {
        return this.getClass().getCanonicalName();
    }

    public VRL getConfigPropertiesURI(String configPropsName) throws URISyntaxException {
        VRL confVrl = this.getResourceHandler().getViewerConfigDir();
        if (confVrl == null) {
            getLogger().warn("No viewer configuration directory configured\n");
            return null;
        }
        VRL vrl = confVrl.appendPath(configPropsName);
        return vrl;
    }

    /**
     * Load Configuration properties for this Viewer from the persistant property store.
     *
     * @param configPropsName - properties name for example "viewer.props".
     * @return Properties  loaded from the ViewerContext property store.
     * @throws IOException
     */
    protected Properties loadConfigProperties(String configPropsName) throws IOException {
        if (properties == null) {
            try {
                properties = getResourceHandler().loadProperties(
                        getConfigPropertiesURI(configPropsName));
            } catch (URISyntaxException e) {
                throw new IOException("Invalid properties location:" + e.getReason(), e);
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            }
        }
        return properties;
    }

    /**
     * Save Configuration properties for this Viewer to the persistant property store.
     *
     * @param configPropsName - properties name for example "viewer.props".
     * @throws IOException
     */
    protected void saveConfigProperties(Properties configProps, String configPropsName) throws IOException {
        try {
            getResourceHandler().saveProperties(getConfigPropertiesURI(configPropsName), configProps, "Saving properties:" + configPropsName);
        } catch (URISyntaxException e) {
            throw new IOException("Invalid properties location:" + e.getReason(), e);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public void notifyBusy(boolean isBusy) {
        this.isBusy = isBusy;
    }

    public boolean isBusy() {
        return this.isBusy;
    }

    /**
     * Notify Viewer Manager or other Listeners that an Exception has occured.
     */
    protected void notifyException(String message, Throwable ex) {
        this.fireEvent(ViewerEvent.createExceptionEvent(this, message, ex));
        ExceptionDialog.show(this, message, ex, false);
    }

    // =========================================================================
    // Mime type Interface.
    // =========================================================================

    public boolean isMyMimeType(String mimeType) {
        String[] types = this.getMimeTypes();

        if (types == null) {
            return false;
        }

        for (String type : types) {
            if (StringUtil.equals(type, mimeType)) {
                return true;
            }
        }

        return false;
    }

    // =========================================================================
    // Event Interface.
    // =========================================================================

    protected Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    public void error(String format, Object... args) {
        getLogger().error(format, args);
    }

    protected void warn(String format, Object... args) {
        getLogger().warn(format, args);
    }

    protected void info(String format, Object... args) {
        getLogger().info(format, args);
    }

    protected void debug(String format, Object... args) {
        getLogger().debug(format, args);
    }

    public void showMessage(String title, String format, Object... args) {
        getLogger().info("MESSAGE:" + String.format(format, args));
        if (viewerContext != null) {
            this.viewerContext.getUI().showMessage(title, String.format(format, args), false);
        }
    }

    protected void handle(String messageString, Throwable ex) {
        ExceptionDialog.show(this, messageString, ex, false);
    }

    // =========================================================================
    // Event Interface.
    // =========================================================================

    @Override
    public void addViewerListener(ViewerListener listener) {
        this.getViewerEventDispatcher().addListener(listener, this);
    }

    @Override
    public void removeViewerListener(ViewerListener listener) {
        this.getViewerEventDispatcher().removeListener(listener);
    }

    protected ViewerEventDispatcher getViewerEventDispatcher() {
        ViewerContext context = this.getViewerContext();
        if (context == null) {
            return null;
        }
        return context.getViewerEventDispatcher();
    }

    protected void fireEvent(ViewerEvent event) {
        getLogger().debug(">>> fireEvent():{}", event);

        ViewerEventDispatcher dispatcher = getViewerEventDispatcher();

        if (dispatcher == null) {
            getLogger().error("FIXME: No ViewerEvent Dispatcher!");
            return;
        }

        dispatcher.fireEvent(event);
    }

    protected void fireStarted() {
        fireEvent(ViewerEvent.createStartedEvent(this));
    }

    protected void fireStopped() {
        fireEvent(ViewerEvent.createStoppedEvent(this));
    }

    protected void fireDisposed() {
        fireEvent(ViewerEvent.createDisposedEvent(this));
    }

    // =========================================================================
    // Abstract Interface
    // =========================================================================

    /**
     * Initialize GUI Component of viewer. Do not start loading resource. Typically this method is
     * called during The Swing Event Thread.
     */
    abstract protected void doInitViewer();

    /**
     * Start the viewer, load resources if necessary.
     *
     * @param vrl
     * @param optionalMethod
     * @throws VrsException
     */
    abstract protected void doStartViewer(VRL vrl, String optionalMethod) throws VrsException;

    /**
     * Update content.
     */
    abstract protected void doUpdate(VRL vrl) throws VrsException;

    /**
     * Stop/suspend viewer. All background activity must stop. After a stopViewer() a startViewer()
     * may occur to notify the viewer can be activateed again.
     */

    abstract protected void doStopViewer();

    /**
     * Stop viewer and dispose resources. After a disposeViewer() a viewer will never be started but
     * multiple disposeViewers() might ocure.
     */
    abstract protected void doDisposeViewer();

    // =====================================
    // Explicit inheritance from MimeViewer
    // =====================================

    @Override
    abstract public String[] getMimeTypes();

    @Override
    abstract public Map<String, List<String>> getMimeMenuMethods();

    @Override
    abstract public String getViewerName();

}
