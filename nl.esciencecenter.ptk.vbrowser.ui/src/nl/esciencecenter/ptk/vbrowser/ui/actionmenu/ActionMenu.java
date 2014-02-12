/*
 * Copyrighted 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").  
 * You may not use this file except in compliance with the License. 
 * For details, see the LICENCE.txt file location in the root directory of this 
 * distribution or obtain the Apache License at the following location: 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
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
        ActionMenu menu = new ActionMenu(browserPlatform,viewNode, actionListener);

        ViewNode[] selected = null;
        if (container != null)
            selected = container.getNodeSelection();

        boolean hasSelection = ((selected != null) && (selected.length > 0));
        boolean multiSelection = ((selected != null) && (selected.length > 1));

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
        menu.add(menu.createItem(viewNode, "Open ", ActionMethod.OPEN_LOCATION));

        {
            JMenu openMenu = new JMenu("Open in");
            menu.add(openMenu);

            openMenu.add(menu.createItem(viewNode, "New Window", ActionMethod.OPEN_IN_NEW_WINDOW));
            openMenu.add(menu.createItem(viewNode, "New Tab", ActionMethod.OPEN_IN_NEW_TAB));
        }

        // Mime Menu Options + "View With"
        menu.add(new JSeparator());
        {
            if (nodeMimeType != null)
            {
                menu.addMimeViewerMenuMethods(viewNode, nodeMimeType);
            }

        }

        // View With ->
        {
            menu.createViewersMenu(viewNode);
        }

        // Default options:
        menu.add(new JSeparator());
        {
            menu.add(menu.createItem(viewNode, "Create", ActionMethod.CREATE));
            if (multiSelection)
                menu.add(menu.createItem(viewNode, "Delete All", ActionMethod.DELETE_SELECTION));
            else
                menu.add(menu.createItem(viewNode, "Delete", ActionMethod.DELETE));

            menu.add(menu.createItem(viewNode, "Rename", ActionMethod.RENAME));
        }

        // CopyPasta
        menu.add(new JSeparator());
        {

            JMenuItem item;
            String name = "Copy";
            if (multiSelection)
                name = "Copy All";

            menu.add(item = menu.createItem(viewNode, name, ActionMethod.COPY_SELECTION));

            // enable copy in cancas menu only when there is something selected
            if (canvasMenu)
                if (hasSelection)
                    item.setEnabled(true);
                else
                    item.setEnabled(false);

            menu.add(menu.createItem(viewNode, "Paste", ActionMethod.PASTE));
            sep = new JSeparator();
            menu.add(sep);
            menu.add(menu.createItem(viewNode, "Refresh", ActionMethod.REFRESH));
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
                menu.add(menu.createItem(viewNode, "Properties", ActionMethod.SHOW_PROPERTIES));
            }
        }

        return menu;

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

        private ViewNode viewNode;

        public PopupHandler(ViewNode viewNode, ActionMenuListener listener)
        {
            this.menuActionListener = listener;
            this.viewNode = viewNode;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            String cmdStr = e.getActionCommand();
            Action theAction = Action.createFrom(viewNode, e);
            menuActionListener.handleMenuAction(theAction);

        }
    }
    
    // ========================================================================
    // Instance
    // ========================================================================

    private PopupHandler popupHandler;
    
    private BrowserPlatform platform;

    public ActionMenu(BrowserPlatform browserPlatform, ViewNode viewNode, ActionMenuListener actionListener)
    {
        init(browserPlatform,viewNode, actionListener);
    }

    public void init(BrowserPlatform browserPlatform, ViewNode viewNode, ActionMenuListener actionListener)
    {
        this.popupHandler = new PopupHandler(viewNode, actionListener);
        this.platform=browserPlatform;
    }

    protected JMenuItem createItem(ViewNode viewNode, String name, ActionMethod actionMeth)
    {
        JMenuItem mitem = new JMenuItem();
        mitem.setText(name);
        mitem.setActionCommand(new Action(null, viewNode, actionMeth).toString());
        mitem.addActionListener(this.popupHandler);

        return mitem;
    }

    protected JMenuItem createItem(ViewNode viewNode, String name, ActionMethod actionMeth, String argument)
    {
        JMenuItem mitem = new JMenuItem();
        mitem.setText(name);
        mitem.setActionCommand(new Action(null, viewNode, actionMeth, argument).toString());
        mitem.addActionListener(this.popupHandler);

        return mitem;
    }

    protected JMenuItem createItem(ViewNode viewNode, String name, ActionMethod actionMeth, String arguments[])
    {
        JMenuItem mitem = new JMenuItem();
        mitem.setText(name);
        mitem.setActionCommand(new Action(null, viewNode, actionMeth, arguments).toString());
        mitem.addActionListener(this.popupHandler);

        return mitem;
    }

    private void addMimeViewerMenuMethods(ViewNode viewNode, String mimeType)
    {
        PluginRegistry viewReg = platform.getViewerRegistry();

        List<MimeMenuEntry> entries = viewReg.getMimeMenuEntries(mimeType);

        if (entries==null)
            return; 
        
        for (MimeMenuEntry entry : entries)
        {
            String args[] = new String[2];
            args[0] = entry.getViewerClassName();
            args[1] = entry.getMethodName(); // optional method name, may be
                                             // null.

            add(createItem(viewNode, entry.getMenuName(), ActionMethod.VIEW_WITH, args));
        }

    }

    private void createViewersMenu(ViewNode viewNode)
    {
        PluginRegistry viewReg = platform.getViewerRegistry();

        ViewerEntry[] viewers = viewReg.getViewers();
        JMenu subMenu = new JMenu("View with");

        for (ViewerEntry viewer : viewers)
        {
            JMenuItem item = createItem(viewNode, viewer.getName(), ActionMethod.VIEW_WITH, viewer.getViewerClass()
                    .getCanonicalName());
            subMenu.add(item);
        }
        
        add(subMenu);
    }



}
