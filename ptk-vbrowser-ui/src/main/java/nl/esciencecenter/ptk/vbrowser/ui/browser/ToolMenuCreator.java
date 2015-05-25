package nl.esciencecenter.ptk.vbrowser.ui.browser;

import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import nl.esciencecenter.ptk.data.Pair;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.Action;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionMethod;
import nl.esciencecenter.ptk.vbrowser.viewers.PluginRegistry;
import nl.esciencecenter.ptk.vbrowser.viewers.PluginRegistry.PluginEntry;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerPlugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToolMenuCreator {
    
    private static final Logger logger=LoggerFactory.getLogger(ToolMenuCreator.class);
    
    protected PluginRegistry pluginRegistry;

    public ToolMenuCreator(PluginRegistry pluginRegistry) {
        this.pluginRegistry = pluginRegistry;
    }

    public JMenu createMenu(JMenu toolsMenu, ActionListener actionListener) {
        {
            JMenuItem viewMI = new JMenuItem();
            toolsMenu.add(viewMI);
            viewMI.setText("Tools");
            viewMI.addActionListener(actionListener);
            viewMI.setEnabled(false);
        }
        toolsMenu.add(new JSeparator());

        List<Pair<List<String>, PluginEntry>> tools = this.pluginRegistry.getToolMenus();
        Map<String, JMenu> menuPaths = new LinkedHashMap<String, JMenu>();
        menuPaths.put("root", toolsMenu);
        JMenu rootMenu = toolsMenu;

        // single list for now, need menu tree:
        for (Pair<List<String>, PluginEntry> menuDef : tools) {
            //
            PluginEntry plugin = menuDef.two();
            List<String> list = menuDef.one();
            JMenu subMenu=rootMenu;
            String key="root";
            
            if ((list!=null) && (list.size()>=0)) {
                for (String subName:list) {
                    JMenu parent=menuPaths.get(key);
                    key+="-"+subName; 
                    // SubMenu
                    subMenu = menuPaths.get(key);
                    if (subMenu == null) {
                        subMenu = new JMenu(subName);
                        parent.add(subMenu);
                        menuPaths.put(key, subMenu);
                    }
                    logger.info("Created subMenu:{}=>{}",key,subName);
                }
            }
            // JMenuItem
            {
                Class<? extends ViewerPlugin> clazz = plugin.getViewerClass();
                JMenuItem toolMI = new JMenuItem();
                subMenu.add(toolMI);
                toolMI.setText(plugin.getName());
                Action action = new Action(null, ActionMethod.STARTTOOL, clazz.getCanonicalName());
                toolMI.setActionCommand(action.toString());
                toolMI.addActionListener(actionListener);
            }
        }
        return toolsMenu;
    }

}
