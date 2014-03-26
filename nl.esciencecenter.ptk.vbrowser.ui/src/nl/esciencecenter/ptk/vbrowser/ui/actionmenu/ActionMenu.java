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
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class ActionMenu extends JPopupMenu
{
    private static final long serialVersionUID = 7948518745148426493L;

    public static ActionMenu createSimpleMenu(BrowserPlatform browserPlatform, ActionMenuListener actionListener, ViewNodeContainer container,
            ViewNode viewNode, boolean canvasMenu)
    {
        VRL locator = viewNode.getVRL();
        ActionMenu menu = new ActionMenu(browserPlatform,container, actionListener);

        ViewNode[] selections = null;
        if (container != null)
        {
            selections = container.getNodeSelection();
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
        menu.add(menu.createItem(container,viewNode, "Open ", ActionMethod.OPEN_LOCATION));

        {
            JMenu openMenu = new JMenu("Open in");
            menu.add(openMenu);

            openMenu.add(menu.createItem(container,viewNode, "New Window", ActionMethod.OPEN_IN_NEW_WINDOW));
            openMenu.add(menu.createItem(container,viewNode, "New Tab", ActionMethod.OPEN_IN_NEW_TAB));
        }

        // Mime Menu Options + "View With"
        menu.add(new JSeparator());
        {
            if (nodeMimeType != null)
            {
                menu.addMimeViewerMenuMethods(container,viewNode, nodeMimeType);
            }

        }

        // View With ->
        {
            menu.createViewersMenu(container,viewNode);
        }

        // Default options:
        menu.add(new JSeparator());
        {
            menu.add(createNewMenu(container,menu,viewNode)); 
        }
        
        // --------------
        // Delete/rename
        // ---------------
        
        if (multiSelection)
        {
            menu.add(menu.createItem(container,viewNode, "Delete All", ActionMethod.DELETE_SELECTION));
        }
        else
        {
            menu.add(menu.createItem(container,viewNode, "Delete", ActionMethod.DELETE));
            menu.add(menu.createItem(container,viewNode, "Rename", ActionMethod.RENAME));
        }

        // CopyPasta
        menu.add(new JSeparator());
        {

            JMenuItem item;
            String name = "Copy";
            if (multiSelection)
                name = "Copy All";

            menu.add(item = menu.createItem(container,viewNode, name, ActionMethod.COPY_SELECTION));

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

            menu.add(menu.createItem(container,viewNode, "Paste", ActionMethod.PASTE));
            sep = new JSeparator();
            menu.add(sep);
            menu.add(menu.createItem(container,viewNode, "Refresh", ActionMethod.REFRESH));
        }

        // Resource Sub Menu:
        sep = new JSeparator();
        menu.add(sep);
        {

            JMenu subMenu = new JMenu("Location");
            {
                JMenuItem menuItem = new JMenuItem(locator.toString());
                subMenu.add(menuItem);
                menu.add(subMenu);
            }
            {
                menu.add(menu.createItem(container,viewNode, "Properties", ActionMethod.SHOW_PROPERTIES));
            }
        }

        return menu;

    }
    
    // ========================================================================
    // Sub Menu Factory methods 
    // ========================================================================
    
    public static JMenu createNewMenu(Object eventSource,ActionMenu menu,ViewNode viewNode)
    {
        List<String> types = viewNode.getAllowedChildTypes();
        // filter 
        
        JMenu subMenu = new JMenu("New");
        
        if ((types==null) || (types.size()<=0))
        {
            subMenu.setEnabled(false); 
            return subMenu; 
        }
        
        for (String type:types)
        {
            String args[] = new String[1];
            args[0] = type; 
            subMenu.add(menu.createMethodItem(eventSource,viewNode, type, ActionMethod.CREATE_NEW, args));
        }

        subMenu.setEnabled(true); 
        return subMenu; 
    }
    
    // ========================================================================
    // Inner Classes 
    // ========================================================================

    /**
     * Translates pop action events to MenuActions
     */
    public class PopupHandler implements ActionListener
    {
        private ActionMenuListener menuActionListener;

        private ViewNodeComponent viewComp;

        public PopupHandler(ViewNodeComponent viewComp, ActionMenuListener listener)
        {
            this.menuActionListener = listener;
            this.viewComp = viewComp;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            String cmdStr = e.getActionCommand();
            Action theAction = Action.createFrom(viewComp.getViewNode(), e);
            menuActionListener.handleMenuAction(viewComp,theAction);

        }
    }
    
    // ========================================================================
    // Instance
    // ========================================================================

    private PopupHandler popupHandler;
    
    private BrowserPlatform platform;

    public ActionMenu(BrowserPlatform browserPlatform, ViewNodeComponent viewNode, ActionMenuListener actionListener)
    {
        init(browserPlatform,viewNode, actionListener);
    }

    public void init(BrowserPlatform browserPlatform, ViewNodeComponent viewNode, ActionMenuListener actionListener)
    {
        this.popupHandler = new PopupHandler(viewNode, actionListener);
        this.platform=browserPlatform;
    }

    protected JMenuItem createItem(Object eventSource,ViewNode viewNode, String name, ActionMethod actionMeth)
    {
        JMenuItem mitem = new JMenuItem();
        mitem.setText(name);
        mitem.setActionCommand(new Action(eventSource, viewNode, actionMeth).toString());
        mitem.addActionListener(this.popupHandler);

        return mitem;
    }

    protected JMenuItem createItem(Object eventSource,ViewNode viewNode, String name, ActionMethod actionMeth, String argument)
    {
        JMenuItem mitem = new JMenuItem();
        mitem.setText(name);
        mitem.setActionCommand(new Action(eventSource, viewNode, actionMeth, argument).toString());
        mitem.addActionListener(this.popupHandler);

        return mitem;
    }

    protected JMenuItem createMethodItem(Object eventSource,ViewNode viewNode, String name, ActionMethod actionMeth, String arguments[])
    {
        JMenuItem mitem = new JMenuItem();
        mitem.setText(name);
        mitem.setActionCommand(new Action(eventSource, viewNode, actionMeth, arguments).toString());
        mitem.addActionListener(this.popupHandler);

        return mitem;
    }

    private void addMimeViewerMenuMethods(Object eventSource,ViewNode viewNode, String mimeType)
    {
        PluginRegistry viewReg = platform.getViewerRegistry();

        List<MimeMenuEntry> entries = viewReg.getMimeMenuEntries(mimeType);

        if (entries==null)
        {
            return; 
        }
        
        for (MimeMenuEntry entry : entries)
        {
            String args[] = new String[2];
            args[0] = entry.getViewerClassName();
            // optional method name to invoke when viewer is started, may be null. 
            args[1] = entry.getMethodName(); 
            add(createMethodItem(eventSource,viewNode, entry.getMenuName(), ActionMethod.VIEW_WITH, args));
        }

    }

    protected void createViewersMenu(Object eventSource,ViewNode viewNode)
    {
        PluginRegistry viewReg = platform.getViewerRegistry();

        ViewerEntry[] viewers = viewReg.getViewers();
        JMenu subMenu = new JMenu("View with");

        for (ViewerEntry viewer : viewers)
        {
            JMenuItem item = createItem(eventSource,viewNode, viewer.getName(), ActionMethod.VIEW_WITH, viewer.getViewerClass()
                    .getCanonicalName());
            subMenu.add(item);
        }
        
        add(subMenu);
    }



}
