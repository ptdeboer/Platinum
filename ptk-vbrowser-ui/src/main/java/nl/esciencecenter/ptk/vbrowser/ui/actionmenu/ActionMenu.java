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

package nl.esciencecenter.ptk.vbrowser.ui.actionmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeComponent;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeContainer;
import nl.esciencecenter.ptk.vbrowser.viewers.PluginRegistry;
import nl.esciencecenter.ptk.vbrowser.viewers.PluginRegistry.MenuEntry;
import nl.esciencecenter.ptk.vbrowser.viewers.PluginRegistry.PluginEntry;

public class ActionMenu extends JPopupMenu {

    /**
     * @param browserPlatform
     * @param actionListener
     * @param viewComp
     *            ViewComponent from which the pop-up click occured.
     * @param actionSourceNode
     *            actual ViewNode under click
     * @param canvasMenu
     *            whether this is a canvasClick
     * @return
     */
    public static ActionMenu createDefaultPopUpMenu(BrowserPlatform browserPlatform,
            ActionMenuListener actionListener, ViewNodeComponent viewComp,
            ViewNode actionSourceNode, boolean canvasMenu) {
        ViewNodeContainer container = null;

        if (viewComp instanceof ViewNodeContainer) {
            container = (ViewNodeContainer) viewComp;
        } else {
            container = viewComp.getViewContainer();
        }

        ViewNode containerNode = null;

        if (container == null) {
            if (canvasMenu) {
                // assert here
                throw new NullPointerException(
                        "For a canvasmenu the (parent) ViewNodeContainer can not be NULL!");
            } else {
                containerNode = null;
            }
        } else {
            containerNode = container.getViewNode();
        }

        ActionMenu menu = new ActionMenu(browserPlatform, viewComp, actionSourceNode,
                actionListener);

        ViewNode[] selections = null;
        if ((canvasMenu) && (container != null)) {
            selections = container.getNodeSelection();
        }

        boolean hasSelection = ((selections != null) && (selections.length > 0));
        boolean multiSelection = ((selections != null) && (selections.length > 1));

        String nodeMimeType = null;

        if (canvasMenu) {
            JMenuItem menuItem = new JMenuItem("CanvasMenu");
            menu.add(menuItem);
            menuItem.setEnabled(false);
            JSeparator sep = new JSeparator();
            menu.add(sep);
        } else {
            JMenuItem menuItem = new JMenuItem("NodeMenu:" + actionSourceNode.getName());
            menu.add(menuItem);
            menuItem.setEnabled(false);

            JSeparator sep = new JSeparator();
            menu.add(sep);
            nodeMimeType = actionSourceNode.getMimeType();
        }

        JSeparator sep = new JSeparator();
        menu.add(sep);
        menu.add(menu.createItem(container, "Open ", ActionCmdType.OPEN_LOCATION));

        {
            JMenu openMenu = new JMenu("Open in");
            menu.add(openMenu);

            openMenu.add(menu.createItem(container, "New Window", ActionCmdType.OPEN_IN_NEW_WINDOW));
            openMenu.add(menu.createItem(container, "New Tab", ActionCmdType.OPEN_IN_NEW_TAB));
        }

        // Viewer specific menu action for this mimeType
        menu.add(new JSeparator());
        {
            if (actionSourceNode != null) {
                menu.addMimeViewerMenuMethods(container, actionSourceNode);
            }
        }

        // View With ->
        if (!canvasMenu) {
            menu.createViewersMenu(container, actionSourceNode);
        }

        // Default options:
        menu.add(new JSeparator());
        {
            if (canvasMenu) {
                menu.add(createNewMenu(container, menu, containerNode));
            } else {
                menu.add(createNewMenu(container, menu, actionSourceNode));
            }
        }

        // --------------
        // Delete/rename
        // ---------------

        if (multiSelection) {
            menu.add(menu.createItem(container, "Delete All", ActionCmdType.DELETE_SELECTION));
        } else {
            menu.add(menu.createItem(container, "Delete", ActionCmdType.DELETE));
            menu.add(menu.createItem(container, "Rename", ActionCmdType.RENAME));
        }

        // CopyPasta
        menu.add(new JSeparator());
        {
            JMenuItem item;
            String name = "Copy";
            if (multiSelection) {
                name = "Copy All";
            }

            menu.add(item = menu.createItem(container, name, ActionCmdType.COPY_SELECTION));

            // enable copy in canvas menu only when there is something selected
            if (canvasMenu) {
                if (hasSelection) {
                    item.setEnabled(true);
                } else {
                    item.setEnabled(false);
                }
            }

            menu.add(menu.createItem(container, "Paste", ActionCmdType.PASTE));
            sep = new JSeparator();
            menu.add(sep);
            menu.add(menu.createItem(container, "Refresh", ActionCmdType.REFRESH));
        }

        // Resource Sub Menu:
        sep = new JSeparator();
        menu.add(sep);

        {
            JMenu selSubMenu = new JMenu("Selection");
            {
                menu.add(selSubMenu);
                JMenu actionSourceSubMenu = new JMenu("ActionSource");
                {
                    selSubMenu.add(actionSourceSubMenu);
                    if (actionSourceNode != null) {
                        JMenuItem menuItem = new JMenuItem("<" + actionSourceNode.getResourceType()
                                + ">" + actionSourceNode.getVRL());
                        actionSourceSubMenu.add(menuItem);
                    }
                }
                JMenu locSubMenu = new JMenu("Container");
                {
                    selSubMenu.add(locSubMenu);
                    if (containerNode != null) {
                        JMenuItem menuItem = new JMenuItem("<" + containerNode.getResourceType()
                                + ">" + containerNode.getVRL());
                        locSubMenu.add(menuItem);
                    }
                }

                JMenu selsSubMenu = new JMenu("Selections");
                {
                    selSubMenu.add(selsSubMenu);
                    if (selections != null) {
                        for (ViewNode node : selections) {
                            JMenuItem menuItem = new JMenuItem("<" + node.getResourceType() + ">"
                                    + node.getVRL().toString());
                            selsSubMenu.add(menuItem);
                        }
                    } else {
                        selsSubMenu.setEnabled(false);
                    }
                }
            }
            // Properties
            {
                menu.add(menu.createItem(container, "Properties", ActionCmdType.SHOW_PROPERTIES));
            }
        }

        return menu;
    }

    // ========================================================================
    // Sub Menu Factory methods
    // ========================================================================

    public static JMenu createNewMenu(Object eventSource, ActionMenu menu, ViewNode viewNode) {
        List<String> types = viewNode.getAllowedChildTypes();
        // filter

        JMenu subMenu = new JMenu("New");

        if ((types == null) || (types.size() <= 0)) {
            subMenu.setEnabled(false);
            return subMenu;
        }

        for (String type : types) {
            String args[] = new String[1];
            args[0] = type;
            subMenu.add(menu.createMethodItem(eventSource, type, ActionCmdType.CREATE_NEW, args));
        }

        subMenu.setEnabled(true);
        return subMenu;
    }

    // ========================================================================
    // Inner Classes
    // ========================================================================

    /**
     * Translates pop action events to MenuActions and invokes them on the ActionMenuListener.
     */
    public class PopupHandler implements ActionListener {
        private ActionMenuListener menuActionListener;

        public PopupHandler(ActionMenuListener listener) {
            this.menuActionListener = listener;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ActionCmd theAction = ActionCmd.createFrom(e);
            menuActionListener.handlePopUpMenuAction(getViewComponent(), getViewNode(), theAction);
        }
    }

    // ========================================================================
    // Instance
    // ========================================================================

    private PopupHandler popupHandler;

    private BrowserPlatform platform;

    private ViewNodeComponent viewComponent;

    private ViewNode viewNode;

    public ActionMenu(BrowserPlatform browserPlatform, ViewNodeComponent viewComp,
            ViewNode viewNode, ActionMenuListener actionListener) {
        init(browserPlatform, viewComp, viewNode, actionListener);
    }

    protected ViewNodeComponent getViewComponent() {
        return viewComponent;
    }

    protected ViewNode getViewNode() {
        return viewNode;
    }

    protected void init(BrowserPlatform browserPlatform, ViewNodeComponent viewComp,
            ViewNode viewNode, ActionMenuListener actionListener) {
        this.popupHandler = new PopupHandler(actionListener);
        this.platform = browserPlatform;
        this.viewComponent = viewComp;
        this.viewNode = viewNode;
    }

    protected JMenuItem createItem(Object eventSource, String name, ActionCmdType actionMeth) {
        JMenuItem mitem = new JMenuItem();
        mitem.setText(name);
        mitem.setActionCommand(new ActionCmd(eventSource, actionMeth).toString());
        mitem.addActionListener(this.popupHandler);

        return mitem;
    }

    protected JMenuItem createItem(Object eventSource, String name, ActionCmdType actionMeth,
            String argument) {
        JMenuItem mitem = new JMenuItem();
        mitem.setText(name);
        mitem.setActionCommand(new ActionCmd(eventSource, actionMeth, argument).toString());
        mitem.addActionListener(this.popupHandler);

        return mitem;
    }

    protected JMenuItem createMethodItem(Object eventSource, String name, ActionCmdType actionMeth,
            String arguments[]) {
        JMenuItem mitem = new JMenuItem();
        mitem.setText(name);
        mitem.setActionCommand(new ActionCmd(eventSource, actionMeth, arguments).toString());
        mitem.addActionListener(this.popupHandler);

        return mitem;
    }

    private void addMimeViewerMenuMethods(Object eventSource, ViewNode actionSourceNode) {
        PluginRegistry viewReg = platform.getViewerRegistry();

        String mimeType = actionSourceNode.getMimeType();
        List<MenuEntry> entries = new ArrayList<MenuEntry>();
        viewReg.addMimeMenuEntries(entries, mimeType);
        viewReg.addToolMenuMappings(entries, new ViewNodeMenuMapper(actionSourceNode));

        if ((entries == null) || (entries.size() <= 0)) {
            return;
        }

        for (MenuEntry entry : entries) {
            String args[] = new String[2];
            args[0] = entry.getViewerClassName();
            // optional method name to invoke when viewer is started, may be null.
            args[1] = entry.getMethodName();
            add(createMethodItem(eventSource, entry.getMenuName(), ActionCmdType.VIEW_WITH, args));
        }
    }

    protected void createViewersMenu(Object eventSource, ViewNode viewNode) {

        PluginRegistry viewReg = platform.getViewerRegistry();
        PluginEntry[] viewers = viewReg.getViewers();
        JMenu subMenu = new JMenu("View with");

        for (PluginEntry viewer : viewers) {
            JMenuItem item = createItem(eventSource, viewer.getName(), ActionCmdType.VIEW_WITH,
                    viewer.getViewerClass().getCanonicalName());
            subMenu.add(item);
        }

        add(subMenu);
    }

}
