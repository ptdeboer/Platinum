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
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeComponent;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeContainer;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.PluginRegistry;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.PluginRegistry.MimeMenuEntry;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.PluginRegistry.ViewerEntry;

public class ActionMenu extends JPopupMenu
{
    private static final long serialVersionUID = 7948518745148426493L;

    /** 
     * Default Menu Factory. 
     */
    public static ActionMenu createDefaultPopUpMenu(BrowserPlatform browserPlatform, ActionMenuListener actionListener,
            ViewNodeComponent viewComp,
            ViewNode viewNode, boolean canvasMenu)
    {

        ActionMenu menu = new ActionMenu(browserPlatform, viewComp, viewNode, actionListener);

        // Check container of viewComponent if present, for a canvasMenu this is mandatory.
        ViewNodeContainer _container = viewComp.getViewContainer();
        if ((_container == null) && (viewComp instanceof ViewNodeContainer))
        {
            _container = (ViewNodeContainer) viewComp;
        }

        if ((canvasMenu) && (_container == null))
        {
            // assert here
            throw new NullPointerException("For a canvasmenu the (parent) ViewNodeContainer can not be NULL!");
        }

        ViewNode[] selections = null;
        if ((canvasMenu) && (_container != null))
        {
            selections = _container.getNodeSelection();
        }

        boolean hasSelection = ((selections != null) && (selections.length > 0));
        boolean multiSelection = ((selections != null) && (selections.length > 1));

        String nodeMimeType = null;

        if (canvasMenu)
        {
            JMenuItem menuItem = new JMenuItem("CanvasMenu");
            menu.add(menuItem);
            menuItem.setEnabled(false);
            JSeparator sep = new JSeparator();
            menu.add(sep);
        }
        else
        {
            JMenuItem menuItem = new JMenuItem("NodeMenu:" + viewNode.getName());
            menu.add(menuItem);
            menuItem.setEnabled(false);

            JSeparator sep = new JSeparator();
            menu.add(sep);
            nodeMimeType = viewNode.getMimeType();
        }

        JSeparator sep = new JSeparator();
        menu.add(sep);
        menu.add(menu.createItem(viewComp, "Open ", ActionMethod.OPEN_LOCATION));

        {
            JMenu openMenu = new JMenu("Open in");
            menu.add(openMenu);

            openMenu.add(menu.createItem(viewComp, "New Window", ActionMethod.OPEN_IN_NEW_WINDOW));
            openMenu.add(menu.createItem(viewComp, "New Tab", ActionMethod.OPEN_IN_NEW_TAB));
        }

        // Mime Menu Options + "View With"
        menu.add(new JSeparator());
        {
            if (nodeMimeType != null)
            {
                menu.addMimeViewerMenuMethods(viewComp, nodeMimeType);
            }
        }

        // View With ->
        {
            menu.createViewersMenu(viewComp, viewNode);
        }

        // Default options:
        menu.add(new JSeparator());
        {
            menu.add(createNewMenu(viewComp, menu, viewNode));
        }

        // --------------
        // Delete/rename
        // ---------------

        if (multiSelection)
        {
            menu.add(menu.createItem(viewComp, "Delete All", ActionMethod.DELETE_SELECTION));
        }
        else
        {
            menu.add(menu.createItem(viewComp, "Delete", ActionMethod.DELETE));
            menu.add(menu.createItem(viewComp, "Rename", ActionMethod.RENAME));
        }

        // CopyPasta
        menu.add(new JSeparator());
        {
            JMenuItem item;
            String name = "Copy";
            if (multiSelection)
            {
                name = "Copy All";
            }
            
            menu.add(item = menu.createItem(viewComp, name, ActionMethod.COPY_SELECTION));

            // enable copy in canvas menu only when there is something selected
            if (canvasMenu)
            {
                if (hasSelection)
                {
                    item.setEnabled(true);
                }
                else
                {
                    item.setEnabled(false);
                }
            }

            menu.add(menu.createItem(viewComp, "Paste", ActionMethod.PASTE));
            sep = new JSeparator();
            menu.add(sep);
            menu.add(menu.createItem(viewComp, "Refresh", ActionMethod.REFRESH));
        }

        // Resource Sub Menu:
        sep = new JSeparator();
        menu.add(sep);

        {
            JMenu selSubMenu = new JMenu("Selection");
            {
                menu.add(selSubMenu);
                JMenu locSubMenu = new JMenu("Location");
                {
                    selSubMenu.add(locSubMenu);
                    JMenuItem menuItem = new JMenuItem("<" + viewNode.getResourceType() + ">" + viewNode.getVRL());
                    locSubMenu.add(menuItem);
                }
                JMenu selsSubMenu = new JMenu("Selections");
                {
                    selSubMenu.add(selsSubMenu);
                    if (selections != null)
                    {
                        for (ViewNode node : selections)
                        {
                            JMenuItem menuItem = new JMenuItem("<" + node.getResourceType() + ">" + node.getVRL().toString());
                            selsSubMenu.add(menuItem);
                        }
                    }
                    else
                    {
                        selsSubMenu.setEnabled(false);
                    }
                }
            }
            // Properties
            {
                menu.add(menu.createItem(viewComp, "Properties", ActionMethod.SHOW_PROPERTIES));
            }
        }

        return menu;
    }

    // ========================================================================
    // Sub Menu Factory methods
    // ========================================================================

    public static JMenu createNewMenu(Object eventSource, ActionMenu menu, ViewNode viewNode)
    {
        List<String> types = viewNode.getAllowedChildTypes();
        // filter

        JMenu subMenu = new JMenu("New");

        if ((types == null) || (types.size() <= 0))
        {
            subMenu.setEnabled(false);
            return subMenu;
        }

        for (String type : types)
        {
            String args[] = new String[1];
            args[0] = type;
            subMenu.add(menu.createMethodItem(eventSource, type, ActionMethod.CREATE_NEW, args));
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
    public class PopupHandler implements ActionListener
    {
        private ActionMenuListener menuActionListener;

        public PopupHandler(ActionMenuListener listener)
        {
            this.menuActionListener = listener;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Action theAction = Action.createFrom(e);
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

    public ActionMenu(BrowserPlatform browserPlatform, ViewNodeComponent viewComp, ViewNode viewNode, ActionMenuListener actionListener)
    {
        init(browserPlatform, viewComp, viewNode, actionListener);
    }

    protected ViewNodeComponent getViewComponent()
    {
        return viewComponent;
    }

    protected ViewNode getViewNode()
    {
        return viewNode;
    }

    protected void init(BrowserPlatform browserPlatform, ViewNodeComponent viewComp, ViewNode viewNode, ActionMenuListener actionListener)
    {
        this.popupHandler = new PopupHandler(actionListener);
        this.platform = browserPlatform;
        this.viewComponent = viewComp;
        this.viewNode = viewNode;
    }

    protected JMenuItem createItem(Object eventSource, String name, ActionMethod actionMeth)
    {
        JMenuItem mitem = new JMenuItem();
        mitem.setText(name);
        mitem.setActionCommand(new Action(eventSource, actionMeth).toString());
        mitem.addActionListener(this.popupHandler);

        return mitem;
    }

    protected JMenuItem createItem(Object eventSource, String name, ActionMethod actionMeth, String argument)
    {
        JMenuItem mitem = new JMenuItem();
        mitem.setText(name);
        mitem.setActionCommand(new Action(eventSource, actionMeth, argument).toString());
        mitem.addActionListener(this.popupHandler);

        return mitem;
    }

    protected JMenuItem createMethodItem(Object eventSource, String name, ActionMethod actionMeth, String arguments[])
    {
        JMenuItem mitem = new JMenuItem();
        mitem.setText(name);
        mitem.setActionCommand(new Action(eventSource, actionMeth, arguments).toString());
        mitem.addActionListener(this.popupHandler);

        return mitem;
    }

    private void addMimeViewerMenuMethods(Object eventSource, String mimeType)
    {
        PluginRegistry viewReg = platform.getViewerRegistry();

        List<MimeMenuEntry> entries = viewReg.getMimeMenuEntries(mimeType);

        if (entries == null)
        {
            return;
        }

        for (MimeMenuEntry entry : entries)
        {
            String args[] = new String[2];
            args[0] = entry.getViewerClassName();
            // optional method name to invoke when viewer is started, may be null.
            args[1] = entry.getMethodName();
            add(createMethodItem(eventSource, entry.getMenuName(), ActionMethod.VIEW_WITH, args));
        }
    }

    protected void createViewersMenu(Object eventSource, ViewNode viewNode)
    {
        PluginRegistry viewReg = platform.getViewerRegistry();

        ViewerEntry[] viewers = viewReg.getViewers();
        JMenu subMenu = new JMenu("View with");

        for (ViewerEntry viewer : viewers)
        {
            JMenuItem item = createItem(eventSource, viewer.getName(), ActionMethod.VIEW_WITH, viewer.getViewerClass()
                    .getCanonicalName());
            subMenu.add(item);
        }

        add(subMenu);
    }

}
