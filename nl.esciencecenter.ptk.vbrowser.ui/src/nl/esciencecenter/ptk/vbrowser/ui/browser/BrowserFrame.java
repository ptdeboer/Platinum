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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.border.EtchedBorder;

import nl.esciencecenter.ptk.object.Disposable;
import nl.esciencecenter.ptk.ui.widgets.NavigationBar;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionMethod;
import nl.esciencecenter.ptk.vbrowser.ui.iconspanel.IconsPanel;
import nl.esciencecenter.ptk.vbrowser.ui.model.UIViewModel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeSource;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNodeDataSource;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ProxyNodeTableDataUpdater;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTable;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTableModel;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.TableDataProducer;
import nl.esciencecenter.ptk.vbrowser.ui.tree.ResourceTree;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerPanel;

/**
 * Master Browser frame.
 * 
 */
public class BrowserFrame extends JFrame
{
    public static enum BrowserViewMode
    {
        ICONS16(16), ICONS48(48), ICONS96(96), ICONLIST16(16), ICONSLIST48(48), TABLE, CONTENT_VIEWER;

        int iconSize = 48;

        private BrowserViewMode()
        {
        }

        private BrowserViewMode(int size)
        {
            iconSize = size;
        }

        public int getIconSize()
        {
            return iconSize;
        }
    };

    public class TabButtonHandler implements ActionListener
    {
        protected TabContentPanel tabPane;

        public TabButtonHandler(TabContentPanel pane)
        {
            tabPane = pane;
        }

        public void actionPerformed(ActionEvent e)
        {
            // redirect to ProxyBrowser controller:
            // actionListener.actionPerformed(new ActionEvent(tabPane,e.getID(),e.getActionCommand()));
            menuActionListener.actionPerformed(e);
        }
    }

    private final static ClassLogger logger = ClassLogger.getLogger(BrowserFrame.class);

    private static final long serialVersionUID = 3076698217838089389L;

    // ==========
    // Instance
    // ==========

    private BrowserInterface browserController;

    private JPanel uiMainPanel;

    private JSplitPane uiMainSplitPane;

    private JScrollPane uiLeftScrollPane;

    private ResourceTree uiResourceTree;

    private JTabbedPane uiRightTabPane;

    private JPanel uiTopPanel;

    private NavigationBar uiNavigationBar;

    private JTabbedPane uiLeftTabPane;

    private JMenuBar uiMainMenuBar;

    private ActionListener menuActionListener;

    private JToolBar uiViewBar;

    private JButton uiViewAsIconsBtn;

    private JButton uiViewAsIconListBtn;

    private JButton uiViewAsTableBtn;

    private JPanel uiToolBarPanel;

    public BrowserFrame(BrowserInterface controller, ActionListener actionListener)
    {
        this.browserController = controller;
        this.menuActionListener = actionListener;
        initGUI();
    }

    public void initGUI()
    {
        {
            this.uiMainPanel = new JPanel();
            this.add(uiMainPanel);
            this.uiMainPanel.setLayout(new BorderLayout());

            {
                uiMainMenuBar = createMenuBar(menuActionListener);
                setJMenuBar(uiMainMenuBar);
            }
            {
                // === Top Panel === //
                this.uiTopPanel = new JPanel();
                this.uiMainPanel.add(uiTopPanel, BorderLayout.NORTH);
                uiTopPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
                uiTopPanel.setLayout(new BorderLayout());

                {
                    // === Nav Bar === //
                    this.uiNavigationBar = new NavigationBar();
                    uiTopPanel.add(uiNavigationBar, BorderLayout.NORTH);
                    this.uiNavigationBar.setEnableNagivationButtons(true);
                }

                {
                    // === Tool Bar Panel === //

                    uiToolBarPanel = new JPanel();
                    uiTopPanel.add(uiToolBarPanel, BorderLayout.CENTER);
                    uiToolBarPanel.setLayout(new FlowLayout());

                    {
                        // === View Icons Tool Bar === //

                        this.uiViewBar = new JToolBar();
                        uiToolBarPanel.add(uiViewBar);

                        {
                            uiViewAsIconsBtn = new JButton();
                            uiViewBar.add(uiViewAsIconsBtn);
                            // viewAsIconsBut.setText("IC");
                            uiViewAsIconsBtn.setIcon(loadIcon("menu/viewasicons.png"));
                            uiViewAsIconsBtn.setActionCommand(ActionMethod.VIEW_AS_ICONS.toString());
                            uiViewAsIconsBtn.addActionListener(menuActionListener);
                            // uiViewAsIconsBtn.setToolTipText(Messages.TT_VIEW_AS_ICONS);
                        }
                        {
                            uiViewAsIconListBtn = new JButton();
                            uiViewBar.add(uiViewAsIconListBtn);
                            // viewAsIconRows.setText("ICR");
                            uiViewAsIconListBtn.setIcon(loadIcon("menu/viewasiconlist_medium.png"));
                            uiViewAsIconListBtn.setActionCommand(ActionMethod.VIEW_AS_ICON_LIST.toString());
                            uiViewAsIconListBtn.addActionListener(menuActionListener);
                            uiViewAsIconListBtn.setEnabled(true);
                        }
                        {
                            uiViewAsTableBtn = new JButton();
                            uiViewBar.add(uiViewAsTableBtn);
                            // viewAsListBut.setText("AL");
                            uiViewAsTableBtn.setActionCommand(ActionMethod.VIEW_AS_TABLE.toString());
                            uiViewAsTableBtn.addActionListener(menuActionListener);
                            uiViewAsTableBtn.setIcon(loadIcon("menu/viewastablelist.png"));
                            // uiViewAsTableBtn.setEnabled(false);
                            // uiViewAsTableBtn.setToolTipText(Messages.TT_VIEW_AS_TABLE);
                        }
                    }
                }

            }

            {
                // === Split Pane === //
                this.uiMainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                this.uiMainSplitPane.setResizeWeight(0.2d);

                this.uiMainPanel.add(uiMainSplitPane, BorderLayout.CENTER);
                {
                    // LEFT
                    this.uiLeftTabPane = new JTabbedPane();
                    this.uiLeftScrollPane = new JScrollPane();
                    this.uiLeftTabPane.add(uiLeftScrollPane);

                    this.uiMainSplitPane.add(this.uiLeftTabPane, JSplitPane.LEFT);

                    {
                        // no data source during initialization !
                        this.uiResourceTree = new ResourceTree(this.browserController, null);
                        this.uiLeftScrollPane.setViewportView(this.uiResourceTree);
                        this.uiResourceTree.setFocusable(true);
                    }
                }
                {
                    // RIGHT
                    this.uiRightTabPane = new JTabbedPane();
                    this.uiMainSplitPane.add(this.uiRightTabPane, JSplitPane.RIGHT);

                    // ... iconsPanel
                    {
                        IconsPanel iconsPanel = new IconsPanel(this.browserController, null);
                        addTab("Icons", iconsPanel, false);
                    }
                    // default table panel
                    // {
                    // ResourceTable tablePanel = new ResourceTable(new ProxyNodeTableModel(null));//new
                    // ProxyNodeTableModel(node));
                    // addTab("Table",tablePanel);
                    // }

                }
            }
        }

        // default sizes:
        this.setSize(1000, 600);
    }
    
    protected TabContentPanel addTab(String name, JComponent comp, boolean setFocus)
    {
        TabContentPanel tabPanel = TabContentPanel.createTab(name, comp);
 
        int newIndex = uiRightTabPane.getTabCount();
        uiRightTabPane.add(tabPanel, newIndex);
        // use size from ui model: 
        tabPanel.setScrollBarUnitIncrement(48/2);
        
        TabButtonHandler handler = new TabButtonHandler(tabPanel);
        TabTopLabelPanel topTapPnl = new TabTopLabelPanel(tabPanel, handler);
        uiRightTabPane.setTabComponentAt(newIndex, topTapPnl);

        if (newIndex > 0)
        {
            Component tabComp = uiRightTabPane.getTabComponentAt(newIndex - 1);
            if (tabComp instanceof TabTopLabelPanel)
            {
                ((TabTopLabelPanel) tabComp).setEnableAddButton(false);
            }
        }
        if (setFocus)
        {
            uiRightTabPane.setSelectedIndex(newIndex);
        }
        return tabPanel;
    }

    protected IconsPanel getIconsPanel()
    {
        return getIconsPanel(true);
    }

    protected IconsPanel getIconsPanel(boolean autoCreate)
    {
        TabContentPanel currentTab = this.getCurrentTab();
        if (currentTab == null)
        {
            if (autoCreate == false)
            {
                return null;
            }
            
            currentTab = this.addTab("Icons", null, false);
        }

        JComponent comp = currentTab.getContent();
        if (comp instanceof IconsPanel)
        {
            return (IconsPanel) comp;
        }

        if (autoCreate == false)
        {
            return null;
        }
        
        ProxyNode node = this.getViewedProxyNode();
        IconsPanel pnl = new IconsPanel(this.browserController, null);
        pnl.setDataSource(node, true);
        currentTab.setContent(pnl);
        
        return pnl;
    }

    protected TabContentPanel createIconsPanelTab(ProxyNode node, boolean setFocus)
    {
        TabContentPanel tab = this.addTab("Icons", null, setFocus);
        IconsPanel pnl = new IconsPanel(this.browserController, null);
        pnl.setDataSource(node, true);
        tab.setContent(pnl);
        return tab;
    }

    protected void updateTableTab(boolean autoCreate, ProxyNode node)
    {
        TabContentPanel tab = this.getCurrentTab();

        if (tab == null)
        {
            if (autoCreate == false)
                return;

            tab = this.addTab("Table", null, true);
        }

        JComponent comp = tab.getContent();
        ResourceTable tbl = null;

        if (comp instanceof ResourceTable)
        {
            tbl = (ResourceTable) comp;
        }
        else
        {
            if (autoCreate == false)
                return;

            tbl = new ResourceTable(this.browserController, new ResourceTableModel(false));
            tab.setContent(tbl);
        }

        tbl.setDataSource(node, true);
    }

    protected void addViewerPanel(ViewerPanel viewer, boolean setFocus)
    {
        // TabContentPanel currentTab = this.getCurrentTab();
        TabContentPanel tab = this.addTab(viewer.getName() + ":", null, setFocus);
        tab.setContent(viewer);
        return;
    }

    protected JComponent getFirstTab(Class<? extends JComponent> clazz)
    {
        for (int i = 0; i < this.uiRightTabPane.getComponentCount(); i++)
        {
            TabContentPanel tab = this.getTab(i);
            if (tab.contains(clazz))
            {
                return tab.getContent();
            }
        }
        return null;
    }

    private JMenuBar createMenuBar(ActionListener actionListener)
    {
        JMenuBar menu = new JMenuBar();

        {
            // Location
            JMenu mainMenu = new JMenu();
            menu.add(mainMenu);
            mainMenu.setText("Location");
            mainMenu.setMnemonic(KeyEvent.VK_L);
            {
                JMenuItem viewNewWindowMenuItem = new JMenuItem();
                mainMenu.add(viewNewWindowMenuItem);
                viewNewWindowMenuItem.setText("New Window");
                viewNewWindowMenuItem.setMnemonic(KeyEvent.VK_W);
                viewNewWindowMenuItem.addActionListener(actionListener);
                viewNewWindowMenuItem.setActionCommand(ActionMethod.CREATE_NEW_WINDOW.toString());
            }
            {
                JMenuItem openMenuItem = new JMenuItem();
                mainMenu.add(openMenuItem);
                openMenuItem.setText("Open Location");
                openMenuItem.setMnemonic(KeyEvent.VK_O);
                openMenuItem.addActionListener(actionListener);
                openMenuItem.setActionCommand(ActionMethod.OPEN_LOCATION.toString());
            }
            {
                JMenuItem openInWinMenuItem = new JMenuItem();
                mainMenu.add(openInWinMenuItem);
                openInWinMenuItem.setText("Open in new Window");
                openInWinMenuItem.setMnemonic(KeyEvent.VK_N);
                openInWinMenuItem.addActionListener(actionListener);
                openInWinMenuItem.setActionCommand(ActionMethod.OPEN_IN_NEW_WINDOW.toString());
            }
            {
                JMenuItem openInWinMenuItem = new JMenuItem();
                mainMenu.add(openInWinMenuItem);
                openInWinMenuItem.setText("Open in new Tab");
                openInWinMenuItem.setMnemonic(KeyEvent.VK_T);
                openInWinMenuItem.addActionListener(actionListener);
                openInWinMenuItem.setActionCommand(ActionMethod.NEW_TAB.toString());
            }
            JSeparator jSeparator = new JSeparator();
            mainMenu.add(jSeparator);
        }

        // "View" Menu
        {
            JMenu viewMenu = new JMenu();
            menu.add(viewMenu);
            viewMenu.setText("Tools");
            viewMenu.setMnemonic(KeyEvent.VK_T);
            {
                JMenuItem viewMI = new JMenuItem();
                viewMenu.add(viewMI);
                viewMI.setText("Tools");
                // viewMI.setMnemonic(KeyEvent.VK_W);
                viewMI.addActionListener(actionListener);
                // viewNewWindowMenuItem.setActionCommand(ActionMethod.CREATE_NEW_WINDOW.toString());
            }
        }

        // ============
        // "View" Menu
        // ============
        {
            JMenu viewMenu = new JMenu();
            menu.add(viewMenu);
            viewMenu.setText("View");
            viewMenu.setMnemonic(KeyEvent.VK_V);
            {
                JMenuItem viewMI = new JMenuItem();
                viewMenu.add(viewMI);
                viewMI.setText("View");
                // viewMI.setMnemonic(KeyEvent.VK_W);
                viewMI.addActionListener(actionListener);
                // viewNewWindowMenuItem.setActionCommand(ActionMethod.CREATE_NEW_WINDOW.toString());
            }
        }

        // ============
        // "Help" Menu
        // ============

        {
            JMenu viewMenu = new JMenu();
            menu.add(viewMenu);
            viewMenu.setText("Help");
            viewMenu.setMnemonic(KeyEvent.VK_H);
            {
                JMenuItem viewMI = new JMenuItem();
                viewMenu.add(viewMI);
                viewMI.setText("Help");
                // viewMI.setMnemonic(KeyEvent.VK_W);
                viewMI.addActionListener(actionListener);
                viewMI.setActionCommand(ActionMethod.GLOBAL_HELP.toString());
            }
            {
                JMenuItem viewMI = new JMenuItem();
                viewMenu.add(viewMI);
                viewMI.setText("About");
                // viewMI.setMnemonic(KeyEvent.VK_W);
                viewMI.addActionListener(actionListener);
                viewMI.setActionCommand(ActionMethod.GLOBAL_ABOUT.toString());
            }

        }
        return menu;
    }

    public ResourceTree getResourceTree()
    {
        return this.uiResourceTree;
    }

    public JTabbedPane getTabbedPane()
    {
        return this.uiRightTabPane;
    }

    public void setNavigationBarListener(ActionListener handler)
    {
        this.uiNavigationBar.addTextFieldListener(handler);
        this.uiNavigationBar.addNavigationButtonsListener(handler);
    }

    public ViewNode getCurrentTabViewedNode()
    {
        TabContentPanel tab = this.getCurrentTab();
        if (tab != null)
            return tab.getViewNode();
        // EMPTY TABS!
        // this.uiResourceTree.getSel
        return null;
    }

    public TabContentPanel getCurrentTab()
    {
        Component tab = this.uiRightTabPane.getSelectedComponent();
        if (tab instanceof TabContentPanel)
        {
            return ((TabContentPanel) tab);
        }
        return null;
    }

    public boolean closeTab(TabContentPanel tab, boolean disposeContent)
    {
        int tabIndex = uiRightTabPane.indexOfComponent(tab);

        if (tabIndex < 0)
        {
            return false;
        }

        this.uiRightTabPane.removeTabAt(tabIndex);
        int index = this.uiRightTabPane.getTabCount();

        if (index > 0)
        {
            Component comp = this.uiRightTabPane.getTabComponentAt(index - 1);
            if (comp instanceof TabTopLabelPanel)
            {
                ((TabTopLabelPanel) comp).setEnableAddButton(true); // always enable last + button.
            }
        }

        if (disposeContent)
        {
            JComponent content = tab.getContent();
            if (content instanceof Disposable)
            {
                ((Disposable) content).dispose();
            }
        }

        return true;
    }

    public TabContentPanel getTab(int index)
    {
        Component tab = this.uiRightTabPane.getComponent(index);
        if (tab instanceof TabContentPanel)
        {
            return ((TabContentPanel) tab);
        }
        return null;
    }

    public NavigationBar getNavigationBar()
    {
        return this.uiNavigationBar;
    }

    private ImageIcon loadIcon(String urlstr)
    {
        return new ImageIcon(getClass().getClassLoader().getResource(urlstr));
    }

    public void setViewMode(BrowserViewMode mode)
    {

        switch (mode)
        {
            case ICONS16:
            case ICONS48:
            case ICONS96:
                this.getIconsPanel(true).updateUIModel(UIViewModel.createIconsModel(mode.getIconSize()));
                break;
            case ICONLIST16:
            case ICONSLIST48:
                this.getIconsPanel(true).updateUIModel(UIViewModel.createIconsListModel(mode.getIconSize()));
                break;
            case TABLE:
                this.updateTableTab(true, this.getViewedProxyNode());
                break;
            case CONTENT_VIEWER:
                // this.getViewerPanel(true);
                break;
            default:
                logger.errorPrintf("***FIXME: setViewMode not supported:%s\n", mode);
                break;
        }
    }

    protected ProxyNode getViewedProxyNode()
    {
        TabContentPanel tab = getCurrentTab();

        if (tab == null)
            return null;

        JComponent comp = tab.getContent();

        // Todo: Combine DataSource/DataProducer interfaces

        if (comp instanceof IconsPanel)
        {
            ViewNodeSource dataSource = ((IconsPanel) comp).getDataSource();
            if (dataSource instanceof ProxyNodeDataSource)
            {
                return ((ProxyNodeDataSource) dataSource).getRootNode();
            }
        }

        if (comp instanceof ResourceTable)
        {
            TableDataProducer dataProducer = ((ResourceTable) comp).getDataProducer();
            if (dataProducer instanceof ProxyNodeTableDataUpdater)
            {
                return ((ProxyNodeTableDataUpdater) dataProducer).getRootProxyNode();
            }
        }

        return null;
    }

    public void setTabTitle(TabContentPanel tab, String name)
    {
        int index = this.uiRightTabPane.indexOfComponent(tab);

        if (index < 0)
            return;

        this.uiRightTabPane.setTitleAt(index, name);
        tab.setName(name);

    }

}
