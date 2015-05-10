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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import nl.esciencecenter.ptk.object.Disposable;
import nl.esciencecenter.ptk.ui.dialogs.ExceptionDialog;
import nl.esciencecenter.ptk.ui.icons.IconProvider;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerEvent;
import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerEventDispatcher;
import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerEventSource;
import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerListener;
import nl.esciencecenter.ptk.vbrowser.viewers.vrs.ViewerResourceLoader;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Embedded Viewer Panel and (abstract) ViewerPlugin adaptor for VBrowser viewers (and Tools).
 * 
 * @author Piter T. de Boer
 */
public abstract class EmbeddedViewer extends JPanel implements Disposable, ViewerPlugin,
        MimeViewer, ViewerEventSource {
    private static final long serialVersionUID = 7872709733522871820L;

    private static PLogger logger = PLogger.getLogger(EmbeddedViewer.class);

    // =======
    //
    // =======

    private JPanel innerPanel;
    private VRL viewedUri;
    private boolean isBusy;
    private ViewerContext viewerContext;

    protected String textEncoding = "UTF-8";
    protected Cursor busyCursor = new Cursor(Cursor.WAIT_CURSOR);
    protected Properties properties;
    protected Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    protected IconProvider iconProvider = null;

    protected EmbeddedViewer() {
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
     * Whether Viewer has it own ScrollPane. If not the parent Component might embedd the viewer
     * into a ScrollPanel.
     * 
     * @return
     */
    public boolean haveOwnScrollPane() {
        return false;
    }

    /**
     * Whether to start this viewer always in a StandAlone Dialog/Frame. Some Viewers are not
     * embedded viewers and must be started in a seperate Window.
     * 
     * @return whether viewer is a stand-alone viewer which must be started in its own window
     *         (frame).
     */
    public boolean isStandaloneViewer() {
        return false;
    }

    public boolean isStartedAsStandalone() {
        return this.getViewerContext().getStartedAsStandalone();
    }

    /**
     * Set title of master frame or Viewer tab
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
     * @see javax.swing.JComponent#getTopLevelAncestor()
     * @return the containing JFrame or null.
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
     * @return true if frame could perform pack, athough the actual pack() might be delayed.
     */
    final public boolean requestFramePack() {
        JFrame frame = getJFrame();

        // only pack stand alone viewers embeeded in ViewerFrames.
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

    @Override
    final public void dispose() {
        stopViewer();
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
        doDisposeViewer();
        disposeFrame();
        fireDisposed();
    }

    private boolean disposeFrame() {

        JFrame frame = this.getJFrame();
        if (frame == null) {
            return false;
        }
        frame.setVisible(false);
        return true;
    }

    /**
     * Embedded viewer is actual ViewerPanel
     */
    @Override
    public EmbeddedViewer getViewerPanel() {
        return this;
    }

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

    protected ResourceLoader getResourceLoader() {
        return this.getResourceHandler().getResourceLoader();
    }

    protected IconProvider getIconProvider() {
        if (this.iconProvider == null) {
            iconProvider = new IconProvider(this, getResourceLoader());
        }

        return iconProvider;
    }

    protected Icon getIconOrBroken(String iconUrl) {
        return getIconProvider().getIconOrBroken(iconUrl);
    }

    public String getTextEncoding() {
        return this.textEncoding;
    }

    public void setTextEncoding(String charSet) {
        this.textEncoding = charSet;
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
            logger.warnPrintf("No viewer configuration directory configured\n");
            return null;
        }

        VRL vrl = confVrl.appendPath("/viewers/" + configPropsName);
        return vrl;
    }

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

    protected void saveConfigProperties(Properties configProps, String optName) throws IOException {
        try {
            getResourceHandler().saveProperties(getConfigPropertiesURI(optName), configProps);
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
        ExceptionDialog.show(this, message, ex, false);
    }

    // =========================================================================
    // Mime type Interface.
    // =========================================================================

    public boolean isMyMimeType(String mimeType) {
        String types[] = this.getMimeTypes();

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

    public void errorPrintf(String format, Object... args) {
        logger.errorPrintf(format, args);
    }

    protected void warnPrintf(String format, Object... args) {
        logger.warnPrintf(format, args);
    }

    protected void infoPrintf(String format, Object... args) {
        logger.infoPrintf(format, args);
    }

    protected void debugPrintf(String format, Object... args) {
        logger.debugPrintf("DEBUG:" + format, args);
    }

    public void showMessage(String format, Object... args) {
        // redirect to master browser:
        logger.errorPrintf("MESSAGE:" + format, args);
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
        logger.debugPrintf(">>> Firing event:%s\n", event);

        ViewerEventDispatcher dispatcher = getViewerEventDispatcher();

        if (dispatcher == null) {
            logger.errorPrintf("FIXME: No ViewerEvent Dispatcher!");
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
        // fireEvent(ViewerEvent.createDisposedEvent(this));
    }

    // =========================================================================
    // Abstract Interface
    // =========================================================================

    /**
     * Initialize GUI Component of viewer. Do not start loading resource. Typically this method is
     * called during The Swing Event Thread.
     * 
     * @param viewerContext
     *            - Contains setting from VBrowser
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
