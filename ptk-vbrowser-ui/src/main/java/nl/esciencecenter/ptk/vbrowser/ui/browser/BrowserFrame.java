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
import nl.esciencecenter.ptk.ui.widgets.NavigationBar;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionCmdType;
import nl.esciencecenter.ptk.vbrowser.ui.browser.menu.BrowserMenuBarCreator;
import nl.esciencecenter.ptk.vbrowser.ui.browser.tabs.BrowserJTabbedPaneController;
import nl.esciencecenter.ptk.vbrowser.ui.browser.tabs.TabContentPanel;
import nl.esciencecenter.ptk.vbrowser.ui.iconspanel.IconsPanel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyDataSource;
import nl.esciencecenter.ptk.vbrowser.ui.model.UIViewModel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTable;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTableModel;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetree.ResourceTree;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerPlugin;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Master Browser frame.
 */
@Slf4j
public class BrowserFrame extends JFrame {

    // Main Components/Managers
    private BrowserInterface browserController;
    private ResourceTree uiResourceTree;
    private NavigationBar uiNavigationBar;
    private ActionListener menuActionListener;
    private BrowserJTabbedPaneController tabManager;

    // Main Panels/Panes:
    private JPanel uiMainPanel;
    private JSplitPane uiMainSplitPane;
    private JScrollPane uiLeftScrollPane;
    private JTabbedPane uiRightTabPane;
    private JPanel uiTopPanel;
    private JPanel uiToolBarPanel;
    private JTabbedPane uiLeftTabPane;

    // Top level menus:
    private JMenuBar topMenuBar;
    private JToolBar viewToolBar;

    public BrowserFrame(BrowserInterface controller, ActionListener actionListener) {
        this.browserController = controller;
        this.menuActionListener = actionListener;
        initGUI();
    }

    public void initGUI() {
        // ===================
        // Toplevel Components
        // ===================
        {
            this.uiMainPanel = new JPanel();
            this.add(uiMainPanel);
            this.uiMainPanel.setLayout(new BorderLayout());
            {
                this.topMenuBar =new BrowserMenuBarCreator(this.browserController, menuActionListener).create();
                setJMenuBar(topMenuBar);
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
                        this.viewToolBar = new JToolBar();
                        uiToolBarPanel.add(viewToolBar);
                        {
                            JButton uiViewAsIconsBtn = new JButton();
                            viewToolBar.add(uiViewAsIconsBtn);
                            // viewAsIconsBut.setText("IC");
                            uiViewAsIconsBtn.setIcon(loadIcon("menu/viewasicons_small.png"));
                            uiViewAsIconsBtn.setActionCommand(ActionCmdType.VIEW_AS_ICONS.toString()+":16");
                            uiViewAsIconsBtn.addActionListener(menuActionListener);
                            // uiViewAsIconsBtn.setToolTipText(Messages.TT_VIEW_AS_ICONS);
                        }
                        {
                            JButton uiViewAsIconsBtn = new JButton();
                            viewToolBar.add(uiViewAsIconsBtn);
                            // viewAsIconsBut.setText("IC");
                            uiViewAsIconsBtn.setIcon(loadIcon("menu/viewasicons.png"));
                            uiViewAsIconsBtn.setActionCommand(ActionCmdType.VIEW_AS_ICONS.toString()+":48");
                            uiViewAsIconsBtn.addActionListener(menuActionListener);
                            // uiViewAsIconsBtn.setToolTipText(Messages.TT_VIEW_AS_ICONS);
                        }
                        {
                            JButton uiViewAsIconsBtn = new JButton();
                            viewToolBar.add(uiViewAsIconsBtn);
                            // viewAsIconsBut.setText("IC");
                            uiViewAsIconsBtn.setIcon(loadIcon("menu/viewasicons_big.png"));
                            uiViewAsIconsBtn.setActionCommand(ActionCmdType.VIEW_AS_ICONS.toString()+":96");
                            uiViewAsIconsBtn.addActionListener(menuActionListener);
                            // uiViewAsIconsBtn.setToolTipText(Messages.TT_VIEW_AS_ICONS);
                        }
                        {
                            JButton uiViewAsIconListBtn = new JButton();
                            viewToolBar.add(uiViewAsIconListBtn);
                            // viewAsIconRows.setText("ICR");
                            uiViewAsIconListBtn.setIcon(loadIcon("menu/viewasiconlist_medium.png"));
                            uiViewAsIconListBtn.setActionCommand(ActionCmdType.VIEW_AS_ICON_LIST.toString());
                            uiViewAsIconListBtn.addActionListener(menuActionListener);
                            uiViewAsIconListBtn.setEnabled(true);
                        }
                        {
                            JButton uiViewAsTableBtn = new JButton();
                            viewToolBar.add(uiViewAsTableBtn);
                            // viewAsListBut.setText("AL");
                            uiViewAsTableBtn.setActionCommand(ActionCmdType.VIEW_AS_TABLE.toString());
                            uiViewAsTableBtn.addActionListener(menuActionListener);
                            uiViewAsTableBtn.setIcon(loadIcon("menu/viewastablelist.png"));
                            // uiViewAsTableBtn.setEnabled(false);
                            // uiViewAsTableBtn.setToolTipText(Messages.TT_VIEW_AS_TABLE);
                        }
                    }
                }
            }
            // ====
            // Split Pane
            // ====
            {
                this.uiMainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                this.uiMainSplitPane.setResizeWeight(0.2d);
                this.uiMainPanel.add(uiMainSplitPane, BorderLayout.CENTER);
                // ====
                // LEFT: ResourceTree
                // ====
                {

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
                // ===
                // RIGHT: JTabbedPane
                // ====
                {
                    this.uiRightTabPane = new JTabbedPane();
                    this.tabManager=new BrowserJTabbedPaneController(uiRightTabPane,this.menuActionListener);
                    this.uiMainSplitPane.add(this.uiRightTabPane, JSplitPane.RIGHT);
                    // ... iconsPanel
                    {
                        IconsPanel iconsPanel = new IconsPanel(this.browserController, null);
                        tabManager.addTab("Icons", iconsPanel, false, true);
                    }
                }
            }
        }

        // default sizes:
        this.setSize(1000, 600);
    }

    protected IconsPanel getIconsPanel() {
        return getIconsPanel(true);
    }

    protected IconsPanel getIconsPanel(boolean autoCreate) {
        TabContentPanel currentTab = tabManager.getCurrentTab();
        if (currentTab == null) {
            if (autoCreate == false) {
                return null;
            }

            currentTab = tabManager.addTab("Icons", null, false, true);
        }

        JComponent comp = currentTab.getContent();
        if (comp instanceof IconsPanel) {
            return (IconsPanel) comp;
        }

        if (autoCreate == false) {
            return null;
        }

        ProxyNode node = this.getViewedProxyNode();
        IconsPanel pnl = new IconsPanel(this.browserController, null);
        pnl.setDataSource(node, true);
        currentTab.setContent(pnl);

        return pnl;
    }

    protected TabContentPanel createIconsPanelTab(ProxyNode node, boolean setFocus) {
        TabContentPanel tab = tabManager.addTab("Icons", null, setFocus, true);
        IconsPanel pnl = new IconsPanel(this.browserController, null);
        pnl.setDataSource(node, true);
        tab.setContent(pnl);
        return tab;
    }

    protected void updateTableTab(boolean autoCreate, ProxyNode node) {
        TabContentPanel tab = tabManager.getCurrentTab();

        if (tab == null) {
            if (autoCreate == false)
                return;

            tab = tabManager.addTab("Table", null, true, true);
        }

        JComponent comp = tab.getContent();
        ResourceTable tbl = null;

        if (comp instanceof ResourceTable) {
            tbl = (ResourceTable) comp;
        } else {
            if (autoCreate == false) {
                return;
            }

            tbl = new ResourceTable(this.browserController, new ResourceTableModel(false));
            tab.setContent(tbl);
        }

        tbl.setDataSource(node, true);
    }

    public void addViewerPanel(ViewerPlugin viewer, boolean setFocus) {
        // TabContentPanel currentTab = this.getCurrentTab();
        TabContentPanel tab = tabManager.addTab(viewer.getViewerName() + ":", null, setFocus,
                (viewer.haveOwnScrollPane() == false));
        tab.setContent(viewer.getViewerPanel());
        return;
    }

    public ResourceTree getResourceTree() {
        return this.uiResourceTree;
    }

    public void setNavigationBarListener(ActionListener handler) {
        this.uiNavigationBar.addTextFieldListener(handler);
        this.uiNavigationBar.addNavigationButtonsListener(handler);
    }

    public ViewNode getCurrentTabViewedNode() {
        TabContentPanel tab = tabManager.getCurrentTab();
        if (tab != null)
            return tab.getViewNode();
        // EMPTY TABS!
        // this.uiResourceTree.getSel
        return null;
    }

    public NavigationBar getNavigationBar() {
        return this.uiNavigationBar;
    }

    private ImageIcon loadIcon(String urlstr) {
        // move to resource loader:
        return new ImageIcon(getClass().getClassLoader().getResource("icons/" + urlstr));
    }

    public void setViewMode(BrowserViewMode mode) {
        // switch:
        switch (mode) {
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
                log.error("***FIXME: setViewMode(CONTENT_VIEWER) not supported here.");
                // this.getViewerPanel(true);
                break;
            default:
                log.error("***FIXME: setViewMode not supported:{}", mode);
                break;
        }
    }

    protected ProxyNode getViewedProxyNode() {
        TabContentPanel tab = tabManager.getCurrentTab();

        if (tab == null)
            return null;

        JComponent comp = tab.getContent();

        ProxyDataSource dataSource = null;

        if (comp instanceof IconsPanel) {
            dataSource = ((IconsPanel) comp).getDataSource();
        } else if (comp instanceof ResourceTable) {
            dataSource = ((ResourceTable) comp).getDataSource();
        } else if (comp instanceof ResourceTree) {
            dataSource = ((ResourceTree) comp).getDataSource();
        }
        if (dataSource == null) {
            return null;
        } else {
            return dataSource.getRootNode();
        }
    }

    public BrowserJTabbedPaneController getTabManager() {
        return this.tabManager;
    }

}
