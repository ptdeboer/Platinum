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

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import nl.esciencecenter.ptk.data.History;
import nl.esciencecenter.ptk.ui.SimpelUI;
import nl.esciencecenter.ptk.ui.UI;
import nl.esciencecenter.ptk.ui.widgets.NavigationBar;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.UIGlobal;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.Action;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionMenu;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionMenuListener;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionMethod;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserFrame.BrowserViewMode;
import nl.esciencecenter.ptk.vbrowser.ui.browser.viewers.ProxyObjectViewer;
import nl.esciencecenter.ptk.vbrowser.ui.browser.viewers.ViewerManager;
import nl.esciencecenter.ptk.vbrowser.ui.dialogs.ExceptionDialog;
import nl.esciencecenter.ptk.vbrowser.ui.iconspanel.IconsPanel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyNodeDnDHandler.DropAction;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeContainer;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactory;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNodeDataSource;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNodeEvent;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNodeEventNotifier;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTable;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerFrame;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerPanel;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Proxy Resource Browser.
 * 
 */
public class ProxyBrowserController implements BrowserInterface, ActionMenuListener
{
    private static ClassLogger logger;

    {
        logger = ClassLogger.getLogger(ProxyBrowserController.class);
    }

    // ========================================================================
    // Inner Classes
    // ========================================================================

    /**
     * Proxy Browser's UI interface.
     */
    public class ProxyUI extends SimpelUI
    {
        @Override
        public boolean isEnabled()
        {
            return true;
        }
    }

    public class NavBarHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            ProxyBrowserController.this.handleNavBarEvent(e);
        }
    }

    public class ActionHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            ProxyBrowserController.this.handleActionEvent(e);
        }
    }

    public class BrowserFrameListener implements WindowListener
    {
        @Override
        public void windowOpened(WindowEvent e)
        {
        }

        @Override
        public void windowClosing(WindowEvent e)
        {
            ProxyBrowserController.this.browserFrame.dispose();
        }

        @Override
        public void windowClosed(WindowEvent e)
        {
        }

        @Override
        public void windowIconified(WindowEvent e)
        {
        }

        @Override
        public void windowDeiconified(WindowEvent e)
        {
        }

        @Override
        public void windowActivated(WindowEvent e)
        {
        }

        @Override
        public void windowDeactivated(WindowEvent e)
        {
        }

    }

    // ========================================================================
    // Instance
    // ========================================================================

    private BrowserPlatform platform;

    private BrowserFrame browserFrame;

    private ProxyNode rootNode;

    private ProxyBrowserTaskWatcher taskWatcher = null;

    private History<VRL> history = new History<VRL>();

    private ProxyActionHandler proxyActionHandler = null;

    private ViewerManager viewerManager;

    private ProxyUI proxyUI = new ProxyUI();

    public ProxyBrowserController(BrowserPlatform platform, boolean show)
    {
        init(platform, show);
    }

    public void handleTabEvent(ActionEvent e)
    {
        // TODO Auto-generated method stub

    }

    public String getBrowserId()
    {
        return "ProxyBrowser";
    }

    private void init(BrowserPlatform platform, boolean show)
    {
        this.platform = platform;
        this.browserFrame = new BrowserFrame(this, new ActionHandler());
        this.taskWatcher = new ProxyBrowserTaskWatcher(this);
        this.browserFrame.addWindowListener(new BrowserFrameListener());
        browserFrame.setNavigationBarListener(new NavBarHandler());
        browserFrame.setVisible(show);
        this.proxyActionHandler = new ProxyActionHandler(this);

        this.viewerManager = new ViewerManager(this);
    }

    @Override
    public BrowserPlatform getPlatform()
    {
        return this.platform;
    }

    @Override
    public void handleException(String actionText, Throwable ex)
    {
        logger.logException(ClassLogger.ERROR, ex, "Exception:%s\n", ex);
        // does ui syncrhonisation:
        ExceptionDialog.show(this.browserFrame, ex);
    }

    @Override
    public JPopupMenu createActionMenuFor(ViewNodeContainer container, ViewNode viewNode, boolean canvasMenu)
    {
        return ActionMenu.createSimpleMenu(this.getPlatform(), this, container, viewNode, canvasMenu);
    }

    /**
     * Set Root Node and update UI.
     */
    public void setRoot(ProxyNode root, boolean update, boolean showAsRoot)
    {
        this.rootNode = root;
        ProxyNodeDataSource dataSource = new ProxyNodeDataSource(root);
        this.browserFrame.getResourceTree().setRoot(dataSource, update, showAsRoot);
        IconsPanel iconsPnl = browserFrame.getIconsPanel();
        if (iconsPnl != null)
            iconsPnl.setDataSource(dataSource, update);
    }

    /**
     * Event from Menu Bar
     */
    public void handleActionEvent(ActionEvent e)
    {
        Action theAction = Action.createFrom(getCurrentViewNode(), e);
        handleAction(theAction);
    }

    @Override
    public void handleMenuAction(Action theAction)
    {
        handleAction(theAction);
    }

    public void handleAction(Action theAction)
    {
        logger.debugPrintf(">>> ActionPerformed:%s\n", theAction);
        ViewNode source = theAction.getActionSource();
        handleNodeAction(source, theAction);
    }

    public void handleNavBarEvent(ActionEvent e)
    {
        logger.debugPrintf(">>> NavBarAction: %s\n", e);
        String cmd = e.getActionCommand();

        NavigationBar.NavigationAction navAction = NavigationBar.NavigationAction.valueOf(cmd);
        ActionMethod meth = null;

        switch (navAction)
        {
            case BROWSE_BACK:
                meth = ActionMethod.BROWSE_BACK;
                break;
            case BROWSE_UP:
                meth = ActionMethod.BROWSE_UP;
                break;
            case BROWSE_FORWARD:
                meth = ActionMethod.BROWSE_FORWARD;
                break;
            case REFRESH:
                meth = ActionMethod.REFRESH;
                break;
            case LOCATION_CHANGED:
                this.updateLocationFromNavBar();
            case LOCATION_EDITED:
            default:
                logger.errorPrintf("FIXME: NavBar action not implemented:%s\n", navAction);
        }

        if (meth != null)
        {
            Action action = Action.createGlobalAction(meth);
            handleAction(action);
        }
    }

    @Override
    public void handleNodeAction(ViewNode node, Action action)
    {
        logger.debugPrintf(">>> nodeAction: %s on:%s\n", action, node);
        boolean global = false;

        if (node == null)
        {
            // global action from menu on current viewed node!
            node = this.getCurrentViewNode();
            global = true;
        }

        Object eventSource = action.getEventSource();

        switch (action.getActionMethod())
        {
            case BROWSE_BACK:
                doBrowseBack();
                break;
            case BROWSE_FORWARD:
                doBrowseForward();
                break;
            case BROWSE_UP:
                doBrowseUp();
                break;
            case CREATE_NEW:
                this.proxyActionHandler.handleCreate(action, node, action.getArg0(), action.getArg1());
                break;
            case CREATE_NEW_WINDOW:
                createBrowser(node);
                break;
            case COPY:
                this.proxyActionHandler.handleCopy(action, node);
                break;
            case COPY_SELECTION:
                this.proxyActionHandler.handleCopySelection(action, node);
                break;
            case DELETE:
                this.proxyActionHandler.handleDelete(action, node);
                break;
            case DEFAULT_ACTION:
                doDefaultAction(node);
                break;
            case DELETE_SELECTION:
                this.proxyActionHandler.handleDeleteSelection(action, node);
                break;
            case OPEN_LOCATION:
                doDefaultAction(node);
                break;
            case OPEN_IN_NEW_WINDOW:
                createBrowser(node);
                break;
            case OPEN_IN_NEW_TAB:
                createNewTab(node);
                break;
            case PASTE:
                this.proxyActionHandler.handlePaste(action, node);
                break;
            case NEW_TAB:
                createNewTab(node);
                break;
            case CLOSE_TAB:
                closeTab(eventSource);
                break;
            case REFRESH:
                doRefresh(node);
                break;
            case VIEW_AS_ICONS:
                doViewAsIcons();
                break;
            case VIEW_AS_ICON_LIST:
                doViewAsList();
                break;
            case VIEW_AS_TABLE:
                doViewAsTable();
                break;
            case SHOW_PROPERTIES:
                doOpenViewer(node, ProxyObjectViewer.class.getCanonicalName(), null, true);
                break;
            case VIEW_OPEN_DEFAULT:
                doOpenViewer(node, null, null, false);
                break;
            case VIEW_WITH:
                // Open viewer in new window.
                doOpenViewer(node, action.getArg0(), action.getArg1(), true);
                break;
            case SELECTION_ACTION:
                doDefaultSelectedAction(node);
                break;
            default:
                logger.errorPrintf("<<< FIXME: ACTION NOT IMPLEMENTED:%s >>>\n", action);
                break;
        }
    }

    protected void doRefresh(ViewNode node)
    {
        logger.errorPrintf(">>>Refresh:%s\n", node);

        ProxyNodeEventNotifier.getInstance().scheduleEvent(
                ProxyNodeEvent.createRefreshEvent(null, node.getVRL()));
    }

    private void doViewAsTable()
    {
        this.browserFrame.setViewMode(BrowserViewMode.TABLE);
    }

    private void doViewAsList()
    {
        this.browserFrame.setViewMode(BrowserViewMode.ICONLIST16);
    }

    private void doOpenViewer(final ViewNode node, String optViewerClass, final String optMenuMethod, boolean standaloneWindow)
    {
        logger.infoPrintf("doOpenViewer:%s\n", node);
        try
        {
            ViewerPanel viewer = viewerManager.createViewerFor(node, optViewerClass);
            // viewer=null;
            if (viewer == null)
            {
                viewer = new ProxyObjectViewer(node);
            }

            doStartViewer(node.getVRL(), viewer, optMenuMethod, standaloneWindow);
        }
        catch (Exception e)
        {
            this.handleException("Couldn't start Viewer for:" + node, e);
        }
    }

    private void doOpenViewer(VRL loc, String resourceType, String mimeType, String optViewerClass, final String optMenuMethod,
            boolean standaloneWindow)
    {
        logger.infoPrintf("doOpenViewer:%s\n", loc);
        try
        {

            ViewerPanel viewer = viewerManager.createViewerFor(resourceType, mimeType, optViewerClass);
            if (viewer == null)
            {
                this.handleException("Couldn't create Viewer for:resourceType/mimeType :" + resourceType + "/" + mimeType, null);
            }
            else
            {
                doStartViewer(loc, viewer, optMenuMethod, standaloneWindow);
            }

        }
        catch (Exception e)
        {
            this.handleException("Couldn't start Viewer for:" + loc, e);
        }

    }

    private void doStartViewer(final VRL vrl, ViewerPanel viewer, final String optMenuMethod, boolean standaloneWindow)
    {
        if (standaloneWindow || viewer.isStandaloneViewer())
        {
            ViewerFrame frame = viewerManager.createViewerFrame(viewer, true);
            frame.setVisible(true);
        }
        else
        {
            browserFrame.addViewerPanel(viewer, true);
            viewer.initViewer();
        }

        final ViewerPanel finalViewer = viewer;

        ProxyBrowserTask task = new ProxyBrowserTask(this, "startViewerFor" + vrl)
        {
            @Override
            protected void doTask()
            {
                try
                {
                    finalViewer.startViewerFor(vrl.toURI(), optMenuMethod);
                }
                catch (Throwable e)
                {
                    handleException("Couldn't start Viewer for:" + vrl, e);
                }
            }
        };

        task.startTask();
    }

    private void doViewAsIcons()
    {
        this.browserFrame.setViewMode(BrowserViewMode.ICONS48);
    }

    private void doBrowseForward()
    {
        VRL loc = history.forward();
        logger.debugPrintf("doBrowseForward:%s\n", loc);

        if (loc != null)
            this.openLocation(loc, false, false);
    }

    private void doBrowseBack()
    {
        VRL loc = history.back();
        logger.debugPrintf("doBrowseBack:%s\n", loc);

        if (loc != null)
            this.openLocation(loc, false, false);
    }

    private void doBrowseUp()
    {
        if (this.getCurrentViewNode() == null)
        {
            logger.warnPrintf("*** Warning: NULL CurrentViewNode!\n");
            return;
        }

        final VRL loc = this.getCurrentViewNode().getVRL();

        ProxyBrowserTask task = new ProxyBrowserTask(this, "doBrowseUp():" + loc)
        {
            @Override
            protected void doTask()
            {
                try
                {

                    ProxyNode node = openProxyNode(loc);
                    VRL parentLoc = node.getParentLocation();
                    if (parentLoc == null)
                        return; // NO parent;

                    openLocation(parentLoc, true, false);
                }
                catch (Throwable e)
                {
                    handleException("Couldn't Browse upwards", e);
                }
            }
        };

        task.startTask();
    }

    private void addToHistory(VRL loc)
    {
        logger.debugPrintf("addToHistory:%s\n", loc);
        this.history.add(loc);
    }

    // Open,etc //
    public void openNode(ViewNode actionNode)
    {
        openLocation(actionNode.getVRL(), true, false);
    }

    public void doDefaultSelectedAction(ViewNode actionNode)
    {
        // Container listeners should update selections.
        // Perform here optional Menu updates...
        logger.errorPrintf("***>>>> New Selection Node:%s\n", actionNode);
    }

    public void doDefaultAction(ViewNode actionNode)
    {
        if (actionNode == null)
        {
            logger.errorPrintf("FIXME: Null ActionNode\n");
        }

        // Determine default action to view node:
        if (actionNode.isComposite())
        {
            openLocation(actionNode.getVRL(), true, false);
        }
        else
        {
            this.doOpenViewer(actionNode, null, null, false);
        }
    }

    protected void createNewTab(ViewNode node)
    {
        if (node == null)
            throw new NullPointerException("createNewTab(): Node can not be null!");
        this.openLocation(node.getVRL(), true, true);
    }

    protected void closeTab(Object source)
    {
        // check event source:
        if (source instanceof TabContentPanel)
        {
            TabContentPanel tab = (TabContentPanel) source;
            this.browserFrame.closeTab(tab, true);
        }
        else if (source instanceof TabTopLabelPanel.TabButton)
        {
            TabContentPanel tab = ((TabTopLabelPanel.TabButton) source).getTabPanel();
            this.browserFrame.closeTab(tab, true);
        }
    }

    private void setViewedNode(ProxyNode node, boolean addHistory, boolean newTab)
    {
        TabContentPanel tab;
        tab = this.browserFrame.getCurrentTab();

        if (tab == null)
            newTab = true; // auto add !

        if (newTab == false)
        {
            tab = this.browserFrame.getCurrentTab();

            tab.setName(node.getName());

            if (node.isComposite())
            {
                JComponent comp = tab.getContent();

                if (comp instanceof IconsPanel)
                {
                    ((IconsPanel) comp).setDataSource(node, true);
                }

                else if (comp instanceof ResourceTable)
                {
                    ((ResourceTable) comp).setDataSource(node, true);
                }
                else
                {
                    logger.errorPrintf("\n" + ">>>\n>>> FIXME: Unknown Component:%s\n>>>\n", comp.getClass());
                }
            }
            else
            {

                try
                {
                    String mimeType = node.getMimeType();
                    String resourceType = node.getResourceType();
                    doOpenViewer(node.getVRL(), resourceType, mimeType, null, null, false);
                }
                catch (ProxyException e)
                {
                    logger.logException(ClassLogger.ERROR, e, "Failed to determine ResourceType+MimeType of:%s", node);
                    logger.errorPrintf("\n" + ">>>\n>>> FIXME: Set SingleNode view:%s\n>>>\n", node);
                }

            }
        }
        else
        {
            tab = browserFrame.createIconsPanelTab(node, true);
        }

        browserFrame.setTabTitle(tab, node.getName());

        try
        {
            Icon icon = node.getIcon(16, false, false);
            updateNavBar(node.getVRL(), icon);
        }
        catch (Exception e)
        {
            logger.logException(ClassLogger.WARN, e, "No icon for node:%s\n", node);
        }

        if (addHistory)
        {
            addToHistory(node.getVRL());
        }
    }

    public void updateLocationFromNavBar()
    {
        String txt = this.browserFrame.getNavigationBar().getLocationText();
        VRL vrl;
        try
        {
            vrl = new VRL(txt);
            this.openLocation(vrl, true, false);
        }
        catch (VRLSyntaxException e)
        {
            this.handleException("Invalid URI Text:" + txt, e);
        }
    }

    protected ProxyFactory getProxyFactoryFor(VRL locator)
    {
        return platform.getProxyFactoryFor(locator); 
    }
    
    /**
     * Resolve locator and open Proxy Node. Must not use this method during
     * swing's event thread, as this method might block the GUI.
     */
    protected ProxyNode openProxyNode(VRL locator) throws ProxyException
    {
        UIGlobal.assertNotGuiThread("Internal Error: Cannot open location during Swing's event thread. VRL=" + locator);

        final ProxyFactory factory = this.platform.getProxyFactoryFor(locator);

        if (factory == null)
        {
            throw new ProxyException("Couldn't open new location, no ProxyFactory for:" + locator);
        }

        ProxyNode node = factory.openLocation(locator);

        return node;
    }

    public void openLocation(final VRL locator, final boolean addToHistory, final boolean newTab)
    {
        logger.debugPrintf(">>> openLocation: %s\n", locator);

        // pre: update nav bar:
        this.updateNavBar(locator, null);

        ProxyBrowserTask task = new ProxyBrowserTask(this, "openLocation" + locator)
        {
            @Override
            protected void doTask()
            {
                try
                {
                    ProxyNode node = openProxyNode(locator);
                    setViewedNode(node, addToHistory, newTab);
                }
                catch (Throwable e)
                {
                    handleException("Couldn't open location:" + locator, e);
                }
            }
        };

        task.startTask();
    }

    public void updateNavBar(VRL locator, Icon icon)
    {
        NavigationBar navbar = this.browserFrame.getNavigationBar();
        navbar.setLocationText(locator.toString(), false);

        if (icon != null)
            navbar.setIcon(icon);
    }

    private ViewNode getCurrentViewNode()
    {
        ViewNode node = this.browserFrame.getCurrentTabViewedNode();
        if (node != null)
            return node;
        node = this.browserFrame.getResourceTree().getCurrentSelectedNode();
        if (node != null)
            return null;

        return browserFrame.getResourceTree().getModel().getRoot().getViewNode();
    }

    private ProxyBrowserController createBrowser(ViewNode node)
    {
        // clone browser and update ViewNode
        ProxyBrowserController newB = new ProxyBrowserController(this.platform, true);
        newB.setRoot(this.rootNode, true, true);
        newB.setCurrentViewNode(node);

        return newB;
    }

    private void setCurrentViewNode(ViewNode node)
    {
        logger.debugPrintf(">>> Update Current ViewNode: %s\n", node);
        this.openLocation(node.getVRL(), true, false);
    }

    public ProxyBrowserTaskWatcher getTaskWatcher()
    {
        return taskWatcher;
    }

    /** Message package */
    void messagePrintf(Object source, String format, Object... args)
    {
        logger.infoPrintf("MESG:" + format, args);
    }

    public void updateHasActiveTasks(boolean active)
    {
        logger.infoPrintf("HasActiveTasks=%s\n", active);
    }

    @Override
    public UI getUI()
    {
        return proxyUI;
    }

    @Override
    public boolean doDrop(Component uiComponent, Point optPoint, ViewNode viewNode, DropAction dropAction, List<VRL> vris)
    {
        // delegate to action handler.
        return this.proxyActionHandler.handleDrop(uiComponent, optPoint, viewNode, dropAction, vris); 
    }


}
