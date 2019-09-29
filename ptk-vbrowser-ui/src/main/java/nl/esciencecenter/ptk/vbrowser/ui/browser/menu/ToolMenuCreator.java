package nl.esciencecenter.ptk.vbrowser.ui.browser.menu;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.data.Pair;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionCmd;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionCmdType;
import nl.esciencecenter.ptk.vbrowser.viewers.PluginRegistry;
import nl.esciencecenter.ptk.vbrowser.viewers.PluginRegistry.PluginEntry;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerPlugin;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ToolMenuCreator {

    private final ActionListener actionListener;

    protected PluginRegistry pluginRegistry;

    public ToolMenuCreator(PluginRegistry pluginRegistry, ActionListener actionListener) {
        this.pluginRegistry = pluginRegistry;
        this.actionListener = actionListener;
    }

    public JMenu create(JMenu toolsMenu) {
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
            JMenu subMenu = rootMenu;
            String key = "root";

            if ((list != null) && (list.size() >= 0)) {
                for (String subName : list) {
                    JMenu parent = menuPaths.get(key);
                    key += "-" + subName;
                    // SubMenu
                    subMenu = menuPaths.get(key);
                    if (subMenu == null) {
                        subMenu = new JMenu(subName);
                        parent.add(subMenu);
                        menuPaths.put(key, subMenu);
                    }
                    log.debug("Created subMenu:{}=>{}", key, subName);
                }
            }
            // JMenuItem
            {
                Class<? extends ViewerPlugin> clazz = plugin.getViewerClass();
                JMenuItem toolMI = new JMenuItem();
                subMenu.add(toolMI);
                toolMI.setText(plugin.getName());
                ActionCmd action = new ActionCmd(null, ActionCmdType.STARTTOOL, clazz.getCanonicalName());
                toolMI.setActionCommand(action.toString());
                toolMI.addActionListener(actionListener);
            }
        }
        return toolsMenu;
    }

}
