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
import nl.esciencecenter.ptk.data.History;
import nl.esciencecenter.ptk.ui.SimpelUI;
import nl.esciencecenter.ptk.ui.UI;
import nl.esciencecenter.ptk.ui.widgets.NavigationBar;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.vbrowser.ui.UIGlobal;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionCmd;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionCmdType;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionMenu;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionMenuListener;
import nl.esciencecenter.ptk.vbrowser.ui.browser.laf.LookAndFeelType;
import nl.esciencecenter.ptk.vbrowser.ui.browser.tabs.TabContentPanel;
import nl.esciencecenter.ptk.vbrowser.ui.browser.tabs.TabTopLabelPanel;
import nl.esciencecenter.ptk.vbrowser.ui.browser.viewers.ProxyPropertiesEditor;
import nl.esciencecenter.ptk.vbrowser.ui.browser.viewers.ViewerManager;
import nl.esciencecenter.ptk.vbrowser.ui.dialogs.ExceptionDialog;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.CopyBuffer;
import nl.esciencecenter.ptk.vbrowser.ui.iconspanel.IconsPanel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyNodeDnDHandler.DropAction;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeComponent;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactory;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNodeDataSourceProvider;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTable;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetree.ResourceTree;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerPlugin;
import nl.esciencecenter.vbrowser.vrs.event.VRSEvent;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.mimetypes.MimeTypes;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Master Proxy Browser Controller.
 */
@Slf4j
public class ProxyBrowserController implements BrowserInterface, ActionMenuListener {

    private static int browserIdCounter = 0;

    // ========================================================================
    // Inner Classes
    // ========================================================================

    /**
     * Proxy Browser's UI interface.
     */
    public class ProxyUI extends SimpelUI {

        public ProxyUI() {
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    public class NavBarHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ProxyBrowserController.this.handleNavBarEvent(e);
        }
    }

    public class GlobalMenuActionHandler implements ActionListener {
        public GlobalMenuActionHandler() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ProxyBrowserController.this.handleGlobalMenuBarEvent(e);
        }
    }

    public class BrowserFrameListener implements WindowListener {
        @Override
        public void windowOpened(WindowEvent e) {
        }

        @Override
        public void windowClosing(WindowEvent e) {
            ProxyBrowserController.this.browserFrame.dispose();
        }

        @Override
        public void windowClosed(WindowEvent e) {
            ProxyBrowserController.this.frameClosed(browserFrame);
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

    // ========================================================================
    // Instance
    // ========================================================================

    private final int id = browserIdCounter++;
    private BrowserPlatform platform;
    private BrowserFrame browserFrame;
    private ProxyNode rootNode;
    private ProxyBrowserTaskWatcher taskWatcher = null;
    private final History<VRL> history = new History<VRL>();
    private ProxyActionHandler proxyActionHandler = null;
    private ViewerManager viewerManager;
    private final ProxyUI proxyUI = new ProxyUI();
    private final CopyBuffer copyBuffer = new CopyBuffer();
    //
    private ProxyNode currentViewNode;

    public ProxyBrowserController(BrowserPlatform platform, boolean show) {
        init(platform, show);
    }

    @Override
    public UI getUI() {
        return proxyUI;
    }

    public String getBrowserId() {
        return "proxy-browser-controller:" + id;
    }

    public JFrame getJFrame() {
        return browserFrame;
    }

    @Override
    public BrowserPlatform getPlatform() {
        return this.platform;
    }

    private void init(BrowserPlatform platform, boolean show) {
        this.platform = platform;
        this.browserFrame = new BrowserFrame(this, new GlobalMenuActionHandler());
        this.taskWatcher = new ProxyBrowserTaskWatcher(this);
        this.browserFrame.addWindowListener(new BrowserFrameListener());
        this.browserFrame.setNavigationBarListener(new NavBarHandler());
        this.browserFrame.setVisible(show);
        this.proxyActionHandler = new ProxyActionHandler(this);
        this.viewerManager = new ViewerManager(this);
    }

    @Override
    public JPopupMenu createActionMenuFor(ViewNodeComponent container, ViewNode viewNode, boolean canvasMenu) {
        return ActionMenu.createDefaultPopUpMenu(getPlatform(), this, container, viewNode, canvasMenu);
    }

    /**
     * Set Root Node and update UI.
     */
    public void setRoot(ProxyNode root, boolean update, boolean showAsRoot) {
        this.rootNode = root;
        ProxyNodeDataSourceProvider dataSource = new ProxyNodeDataSourceProvider(root);
        this.browserFrame.getCurrentResourceTree().setRoot(dataSource, update, showAsRoot);
        IconsPanel iconsPnl = browserFrame.getCreateIconsPanel(true); // autoinit
        if (iconsPnl != null) {
            iconsPnl.setDataSource(dataSource, update);
        }
        // init;
        this.currentViewNode = root;
    }

    /**
     * Event from Menu Bar
     */
    public void handleGlobalMenuBarEvent(ActionEvent e) {
        ActionCmd theAction = ActionCmd.createFrom(e);
        doHandleNodeAction(null, null, theAction, true);
    }

    @Override
    public void handlePopUpMenuAction(ViewNodeComponent viewComp, ViewNode viewNode, ActionCmd theAction) {
        doHandleNodeAction(viewComp, viewNode, theAction, false);
    }

    /**
     * Navigation bar event.
     *
     * @param e
     */
    public void handleNavBarEvent(ActionEvent e) {
        log.debug("[>] handleNavBarEvent():{}", e);
        String cmd = e.getActionCommand();

        NavigationBar.NavigationAction navAction = NavigationBar.getCommand(cmd);
        ActionCmdType meth = null;

        switch (navAction) {
            case BROWSE_BACK:
                meth = ActionCmdType.BROWSE_BACK;
                break;
            case BROWSE_UP:
                meth = ActionCmdType.BROWSE_UP;
                break;
            case BROWSE_FORWARD:
                meth = ActionCmdType.BROWSE_FORWARD;
                break;
            case REFRESH:
                meth = ActionCmdType.REFRESH;
                break;
            case LOCATION_COMBOBOX_EDITED:
                log.debug("ignoring:{}", navAction);
                break;
            case LOCATION_AUTOCOMPLETED:
            case LOCATION_CHANGED:
                meth = ActionCmdType.NAVBAR_LOCATION_CHANGED;
                break;
            default:
                log.error("FIXME: NavBar action not implemented:{}", navAction);
        }

        if (meth != null) {
            ActionCmd action = ActionCmd.createGlobalAction(meth);
            doHandleNodeAction(null, null, action, true);
        }
    }

    @Override
    public void handleNodeAction(ViewNodeComponent viewComp, ViewNode node, ActionCmd action) {
        doHandleNodeAction(viewComp, node, action, false);
    }

    /**
     * Actual action handler.
     *
     * @param viewComp         Either ViewNodeComponent of ViewNodeContainer from where the action originates.
     *                         Is null for global menu actions.
     * @param targetNode       originating ViewNode for example when a click or pop-up menu action was
     *                         triggered.
     * @param action           Actual action method with optional argument.
     * @param globalMenuAction true for Global menu and Navigation Bar event, false for ViewNodeComponent
     *                         events.
     */
    protected void doHandleNodeAction(ViewNodeComponent viewComp, ViewNode targetNode, ActionCmd action, boolean globalMenuAction) {
        Object eventSource = action.getEventSource();
        log.debug("nodeAction: [{}]{} on:{} (Object source={})", globalMenuAction ? "<GLOBAL>" : "<NODE>", action, targetNode, eventSource);

        VRL currentViewedVrl = this.getCurrentViewNode().getVRL();
        List<ViewNode> selectedNodes = this.getCurrentSelectedNodes();

        // Need better global action vs selection action logic here:
        if (globalMenuAction) {
            // Global action -> use selection or null;
            if (targetNode != null) {
                log.error("***FIXME: got targetNode for Global Action: {}", action);
                return; // Stop
            }
            if ((selectedNodes != null) && (selectedNodes.size() > 0)) {
                if (selectedNodes.size() > 1) {
                    // Some might
                    showMessage("Multi Selection Error",
                            "Global menu actions not supported for multiple selections. Please select one resource.");
                    return;
                }
                targetNode = selectedNodes.get(0);
            }
        } else if (targetNode == null) {
            // Non global => must use specific action node;
            log.error("***FIXME: NULL node for non Global Action: {}", action);
            return; // Stop
        }

        switch (action.getActionCmdType()) {
            case BROWSE_BACK:
                doBrowseBack();
                break;
            case BROWSE_FORWARD:
                doBrowseForward();
                break;
            case BROWSE_UP:
                doBrowseUp();
                break;
            case NAVBAR_LOCATION_CHANGED:
                this.doNavBarLocationChanged();
                break;
            case CREATE_NEW:
                this.proxyActionHandler.handleCreate(action, targetNode, action.getArg0(), action.getArg1());
                break;
            case COPY:
                this.proxyActionHandler.handleCopy(action, targetNode, false);
                break;
            case COPY_SELECTION:
                this.proxyActionHandler.handleCopySelection(action, targetNode, selectedNodes, false);
                break;
            case DELETE:
                this.proxyActionHandler.handleDelete(action, targetNode, false);
                break;
            case DELETE_RECURSIVE:
                this.proxyActionHandler.handleDelete(action, targetNode, true);
                break;
            case DELETE_SELECTION:
                this.proxyActionHandler.handleDeleteSelection(viewComp, action);
                break;
            case DEFAULT_ACTION:
            case OPEN_LOCATION:
                doDefaultAction(targetNode);
                break;
            case CREATE_NEW_WINDOW:
            case OPEN_IN_NEW_WINDOW:
                if (targetNode == null) {
                    showMessage("No selection", "Nothing selected");
                    return;
                }
                createBrowser(targetNode.getVRL());
                break;
            case OPEN_IN_NEW_TAB:
            case NEW_TAB:
                VRL targetVrl;
                if (targetNode != null) {
                    targetVrl = targetNode.getVRL();
                } else {
                    if (globalMenuAction == true) {
                        targetVrl = this.getCurrentViewNode().getVRL();
                    } else {
                        showMessage("No selection", "Nothing selected");
                        return;
                    }
                }
                createNewTab(targetVrl);
                break;
            case PASTE:
                this.proxyActionHandler.handlePaste(action, targetNode);
                break;
            case CLOSE_TAB:
                closeTab(eventSource);
                break;
            case REFRESH:
                doRefresh(currentViewedVrl);
                break;
            case VIEW_AS_ICONS:
                doViewAsIcons(action);
                break;
            case VIEW_AS_ICON_LIST:
                doViewAsList();
                break;
            case VIEW_AS_TABLE:
                doViewAsTable();
                break;
            case RENAME:
                this.proxyActionHandler.handleRename(action, targetNode);
                break;
            case SHOW_PROPERTIES:
                doOpenNodeViewer(targetNode, ProxyPropertiesEditor.class.getCanonicalName(), null, true);
                break;
            case VIEW_OPEN_DEFAULT:
                doOpenNodeViewer(targetNode, null, null, false);
                break;
            case VIEW_WITH:
                // Open viewer in new window.
                doOpenNodeViewer(targetNode, action.getArg0(), action.getArg1(), true);
                break;
            case STARTTOOL:
                doOpenNodeViewer(null, action.getArg0(), action.getArg1(), true);
                break;
            case SELECTION_ACTION:
                doDefaultSelectedAction(targetNode);
                break;
            case GLOBAL_ABOUT:
                doShowAbout();
                break;
            case GLOBAL_HELP:
                doShowHelp();
                break;
            case LOOKANDFEEL_ENABLED: {
                boolean enable = !this.platform.getGuiSettings().getLaFEnabled();
                this.platform.getGuiSettings().setLaFEnabled(enable);
                if (enable) {
                    this.switchLookAndFeel(platform.getGuiSettings().getLAFType(), enable);
                }
                break;
            }
            case LOOKANDFEEL: {
                if (!this.platform.getGuiSettings().getLaFEnabled()) {
                    // Must disable in gui.
                    showMessage("Please Enable LaF.", "Custom Look and Feel not enabled.");
                } else {
                    this.switchLookAndFeel(LookAndFeelType.valueOf(action.getArg0()), true);
                }
                break;
            }
            case GLOBAL_SET_SINGLE_ACTION_CLICK:
                boolean value = !this.platform.getGuiSettings().getSingleClickAction();
                this.platform.getGuiSettings().setSingleClickAction(value);
                break;
            case SAVE_SETTINGS:
                this.saveSettings();
                break;
            default:
                log.error(">>> FIXME: ActionCmd not implemented:{}", action);
                break;
        }
        // empty;
    }

    protected void doRefresh(VRL nodeVrl) {
        log.debug("doRefresh:{}", nodeVrl);
        // Full Sync.
        this.getProxyFactoryFor(nodeVrl).clearCache(nodeVrl);
        platform.getVRSEventNotifier().scheduleEvent(VRSEvent.createRefreshEvent(null, nodeVrl));
    }

    private void doViewAsTable() {
        this.browserFrame.setViewMode(BrowserViewMode.TABLE);
    }

    private void doViewAsList() {
        this.browserFrame.setViewMode(BrowserViewMode.ICONLIST16);
    }

    private void doOpenNodeViewer(final ViewNode node, String optViewerClass, final String optMenuMethod,
                                  boolean standaloneWindow) {

        log.debug("doOpenNodeViewer(),node={},optViewerClass={},optMenuMethod={}", node, optViewerClass, optMenuMethod);
        boolean filterOctetStreamMimeType = true;

        if (node == null) {
            try {
                log.error("doOpenNodeViewer():NULL node");
                ViewerPlugin viewer = viewerManager.createViewerFor(null, null, null, optViewerClass);
                doStartViewer(null, viewer, optMenuMethod, standaloneWindow);

            } catch (Exception e) {
                this.handleException("Couldn't start Default Viewer:" + optViewerClass, e);
            }

            return;
        }

        String resourceType = node.getResourceType();
        String resourceStatus = node.getResourceStatus();
        String mimeType = node.getMimeType();
        VRL vrl = node.getVRL();

        try {
            ViewerPlugin viewer;

            if ((filterOctetStreamMimeType) && (StringUtil.equals(mimeType, MimeTypes.MIME_BINARY))
                    && (optViewerClass == null)) {
                viewer = null;
            } else {
                viewer = viewerManager.createViewerFor(resourceType, mimeType, resourceStatus, optViewerClass);
            }

            //TODO: interactive menu: "Open With ->" 
            if (viewer == null) {
                viewer = new ProxyPropertiesEditor(this, node);
            }

            doStartViewer(vrl, viewer, optMenuMethod, standaloneWindow);

        } catch (Exception e) {
            this.handleException("Couldn't start Viewer for:" + node, e);
        }
    }

    private void doOpenViewer(VRL loc, String resourceType, String mimeType, String optViewerClass,
                              final String optMenuMethod, boolean standaloneWindow) {
        log.debug("doOpenViewer:{}", loc);

        try {

            ViewerPlugin viewer = viewerManager.createViewerFor(resourceType, mimeType, null, optViewerClass);
            if (viewer == null) {
                this.handleException("Couldn't create Viewer for:resourceType/mimeType :" + resourceType + "/"
                        + mimeType, null);
            } else {
                doStartViewer(loc, viewer, optMenuMethod, standaloneWindow);
            }

        } catch (Exception e) {
            this.handleException("Couldn't start Viewer for:" + loc, e);
        }

    }

    private void doStartViewer(final VRL vrl, ViewerPlugin viewer, final String optMenuMethod, boolean standaloneWindow) {

        if (standaloneWindow || viewer.isStandaloneViewer()) {
            viewerManager.startStandaloneViewer(viewer, vrl, optMenuMethod, false);
        } else {
            viewerManager.startEmbeddedViewer(browserFrame, viewer, vrl, optMenuMethod, false);
        }

        final ViewerPlugin finalViewer = viewer;

        BrowserTask task = new BrowserTask(this, "startViewerFor:" + vrl) {
            @Override
            protected void doTask() {
                try {
                    finalViewer.startViewer(vrl, optMenuMethod);
                } catch (Throwable e) {
                    handleException("Couldn't start Viewer for:" + vrl, e);
                }
            }
        };

        task.startTask();
    }

    private void doViewAsIcons(ActionCmd action) {
        String sizeStr = action.getArg0();
        int size = Integer.parseInt(sizeStr);
        if (size == 16) {
            this.browserFrame.setViewMode(BrowserViewMode.ICONS16);
        } else if (size == 48) {
            this.browserFrame.setViewMode(BrowserViewMode.ICONS48);
        } else if (size == 96) {
            this.browserFrame.setViewMode(BrowserViewMode.ICONS96);
        }
    }

    private void doBrowseForward() {
        VRL loc = history.forward();
        log.debug("doBrowseForward:{}\n", loc);

        if (loc != null)
            this.openLocation(loc, false, false);
    }

    private void doBrowseBack() {
        VRL loc = history.back();
        log.debug("doBrowseBack:{}\n", loc);

        if (loc != null)
            this.openLocation(loc, false, false);
    }

    private void doBrowseUp() {
        if (this.getCurrentViewNode() == null) {
            log.warn("doBrowseUp():Warning: NULL CurrentViewNode!");
            return;
        }

        final VRL loc = this.getCurrentViewNode().getVRL();

        BrowserTask task = new BrowserTask(this, "doBrowseUp():" + loc) {
            @Override
            protected void doTask() {
                try {
                    ProxyNode node = openProxyNode(loc);
                    VRL parentLoc = node.getParentLocation();
                    if (parentLoc == null)
                        return; // NO parent;

                    openLocation(parentLoc, true, false);
                } catch (Throwable e) {
                    handleException("Couldn't Browse upwards", e);
                }
            }
        };

        task.startTask();
    }

    private void addToHistory(VRL loc) {
        log.debug("addToHistory:{}", loc);
        this.history.add(loc);
    }

    public void doDefaultSelectedAction(ViewNode actionNode) {
        // Unselect:
        if (this.browserFrame.getCurrentResourceTree().hasFocus()) {
            IconsPanel iconsPanel = this.browserFrame.getCurrentIconsPanel(false);
            if (iconsPanel != null) {
                iconsPanel.clearNodeSelection();
            }
        } else {
            this.browserFrame.getCurrentResourceTree().clearSelection();
        }

    }

    public void doDefaultAction(ViewNode actionNode) {
        if (actionNode == null) {
            log.error("*** FIXME: Null ActionNode!");
            new Exception().printStackTrace();
            return;
        }

        // Determine default action to view node:
        if (actionNode.isComposite()) {
            openLocation(actionNode.getVRL(), true, false);
        } else {
            this.doOpenNodeViewer(actionNode, null, null, false);
        }
    }

    protected void doShowHelp() {
        this.proxyUI.showMessage("Help", "Help", false);

    }

    protected void doShowAbout() {
        this.proxyUI.showMessage("About", this.getPlatform().getAboutText(), false);
    }

    protected void createNewTab(VRL targetVrl) {
        if (targetVrl == null)
            throw new NullPointerException("createNewTab(): Node can not be null!");
        this.openLocation(targetVrl, true, true);
    }

    protected void closeTab(Object source) {
        // check event source:
        if (source instanceof TabContentPanel) {
            TabContentPanel tab = (TabContentPanel) source;
            this.browserFrame.getTabManager().closeTab(tab, true);
        } else if (source instanceof TabTopLabelPanel.TabButton) {
            TabContentPanel tab = ((TabTopLabelPanel.TabButton) source).getTabPanel();
            this.browserFrame.getTabManager().closeTab(tab, true);
        } else {
            log.warn("Unrecognized Tab Source:{}", source);
        }
    }

    private ProxyNode getCurrentViewNode() {
        return currentViewNode;
    }

    private void updateCurrentViewNode(ProxyNode pnode, boolean addHistory, boolean newTab) {
        this.currentViewNode = pnode;

        TabContentPanel tab;
        tab = this.browserFrame.getTabManager().getCurrentTab();

        if (tab == null) {
            newTab = true; // auto add !
        }

        if (newTab == false) {

            tab = this.browserFrame.getTabManager().getCurrentTab();
            tab.setName(pnode.getName());

            if (pnode.isComposite()) {
                JComponent comp = tab.getContent();

                if (comp instanceof IconsPanel) {
                    ((IconsPanel) comp).setDataSource(pnode, true);
                } else if (comp instanceof ResourceTable) {
                    ((ResourceTable) comp).setDataSource(pnode, true);
                } else {
                    log.error("***FIXME:setViewedNode():Unknown Component:{}", comp.getClass());
                    newTab = true;
                }
            } else {

                try {
                    String mimeType = pnode.getMimeType();
                    String resourceType = pnode.getResourceType();
                    doOpenViewer(pnode.getVRL(), resourceType, mimeType, null, null, false);
                } catch (ProxyException e) {
                    log.error("ProxyException: Failed to determine ResourceType+MimeType of:{} => {}", pnode, e);
                    log.error("***FIXME: Set SingleNode view:{}\n", pnode);
                }

            }
        }

        if (newTab) {
            tab = browserFrame.createIconsPanelTab(pnode, true);
        }

        browserFrame.getTabManager().setTabTitle(tab, pnode.getName());

        try {
            Icon icon = pnode.getIcon(16, false, false);
            updateNavBar(pnode.getVRL(), icon);
        } catch (Exception e) {
            log.warn("Exception: No icon for node:{} => {}", pnode, e);
        }

        if (addHistory) {
            addToHistory(pnode.getVRL());
        }
    }

    public void doNavBarLocationChanged() {
        String txt = this.browserFrame.getNavigationBar().getLocationText();
        VRL vrl;
        try {
            vrl = new VRL(txt);
            this.openLocation(vrl, true, false);
        } catch (VRLSyntaxException e) {
            this.handleException("Invalid URI Text:" + txt, e);
        }
    }

    protected ProxyFactory getProxyFactoryFor(VRL locator) {
        return platform.getProxyFactoryFor(locator);
    }

    /**
     * Resolve locator and open Proxy Node. Must not use this method during Swings event thread, as
     * this method might block the GUI.
     */
    protected ProxyNode openProxyNode(VRL locator) throws ProxyException {
        UIGlobal.assertNotGuiThread("Internal Error: Cannot open location during Swing's event thread. VRL=" + locator);

        final ProxyFactory factory = this.platform.getProxyFactoryFor(locator);

        if (factory == null) {
            throw new ProxyException("Couldn't open new location, no ProxyFactory for:" + locator);
        }

        ProxyNode node = factory.openLocation(locator);

        return node;
    }

    public void openLocation(final VRL locator, final boolean addToHistory, final boolean newTab) {
        log.debug("openLocation:{}", locator);

        // pre: update nav bar:
        this.updateNavBar(locator, null);

        BrowserTask task = new BrowserTask(this, "openLocation" + locator) {
            @Override
            protected void doTask() {
                try {
                    ProxyNode node = openProxyNode(locator);
                    if (node.exists() == false) {
                        showMessage("Invalid location", "Couldn't open location, resource doesn't exist:" + locator);
                    } else {
                        updateCurrentViewNode(node, addToHistory, newTab);
                    }
                } catch (Throwable e) {
                    handleException("Couldn't open location:" + locator, e);
                }
            }
        };
        task.startTask();
    }

    public void updateNavBar(VRL locator, Icon icon) {
        NavigationBar navbar = this.browserFrame.getNavigationBar();
        navbar.setLocationText(locator.toString(), false);

        if (icon != null) {
            navbar.setIcon(icon);
        }
    }

    private List<ViewNode> getCurrentSelectedNodes() {

        IconsPanel iconsPanel = this.browserFrame.getCurrentIconsPanel(false);
        ResourceTree resourceTree = this.browserFrame.getCurrentResourceTree();

        // Combine selections.
        List<ViewNode> allSelected = new ArrayList<>();
        if (iconsPanel != null) {
            allSelected.addAll(iconsPanel.getNodeSelection());
        }

        if (resourceTree != null) {
            allSelected.addAll(resourceTree.getNodeSelection());
        }

        return allSelected;
    }

    private ProxyBrowserController createBrowser(VRL targetVrl) {
        // Clone browser and update ViewNode
        ProxyBrowserController newB = new ProxyBrowserController(this.platform, true);
        newB.setRoot(this.rootNode, true, true);
        newB.openLocation(targetVrl, true, false);
        return newB;
    }

    public ProxyBrowserTaskWatcher getTaskSource() {
        return taskWatcher;
    }

    @Override
    public boolean doDrop(Component uiComponent, Point optPoint, ViewNode viewNode, DropAction dropAction,
                          List<VRL> vris) {
        // Delegate to action handler.
        return this.proxyActionHandler.handlePasteOrDrop(uiComponent, optPoint, viewNode, dropAction, vris);
    }

    public void updateHasActiveTasks(boolean active) {
        log.debug("HasActiveTasks={}", active);
    }

    // ========================== 
    // Dispose/lifecycle
    // ==========================

    private void saveSettings() {
        try {
            this.platform.saveUIProperties();
        } catch (Exception e) {
            this.handleException("Saving UI Properties", e);
        }
    }

    /**
     * Is called after BrowserFrame actually closed.
     */
    protected void frameClosed(BrowserFrame browser) {
        log.debug("BrowserFrame closed:{}", browser);
        this.platform.unregister(this);
    }

    // ========================== 
    // Message/Exception,etc 
    // ==========================

    @Override
    public void handleException(String actionText, Throwable ex) {
        log.error("Exception:" + actionText + " => " + ex.getMessage());
        log.error("{}", ex);
        // does ui synchronisation:
        ExceptionDialog.show(this.browserFrame, ex);
    }

    public void showMessage(String title, String message) {
        proxyUI.showMessage(title, message, false);
    }

    public String toString() {
        return "ProxyBrowserController:[id='" + this.getBrowserId() + "']";
    }

    private void switchLookAndFeel(LookAndFeelType lafType, boolean enable) {
        this.getPlatform().switchLookAndFeelType(this.browserFrame, lafType, enable);
    }

    public CopyBuffer getCopyBuffer() {
        return this.copyBuffer;
    }

}
