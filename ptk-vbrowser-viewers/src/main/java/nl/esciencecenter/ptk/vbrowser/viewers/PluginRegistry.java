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

package nl.esciencecenter.ptk.vbrowser.viewers;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.data.Pair;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.vbrowser.viewers.internal.*;
import nl.esciencecenter.ptk.vbrowser.viewers.menu.MenuMapping;
import nl.esciencecenter.ptk.vbrowser.viewers.menu.MenuMappingMatcher;
import nl.esciencecenter.ptk.vbrowser.viewers.vrs.ViewerResourceLoader;
import nl.esciencecenter.ptk.vbrowser.viewers.x509viewer.X509Viewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Viewer and Tool Plugin Registry for the VBrowser.
 */
@Slf4j
public class PluginRegistry {

    public class PluginEntry {
        protected Class<? extends ViewerPlugin> pluginClass;

        protected String pluginName;

        PluginEntry(String viewerName, Class<? extends ViewerPlugin> viewerClass) {
            this.pluginClass = viewerClass;
            this.pluginName = viewerName;
        }

        public Class<? extends ViewerPlugin> getViewerClass() {
            return pluginClass;
        }

        public String getName() {
            return pluginName;
        }

        public String toString() {
            return "PluginEntry:[pluginName='" + pluginName + "',pluginClass='" + pluginClass + "']";
        }
    }

    public class MenuEntry {
        protected String methodName;

        protected String menuName;

        protected PluginEntry viewerEntry;

        public MenuEntry(String method, String menuNameValue, PluginEntry entry) {
            methodName = method;
            menuName = menuNameValue;
            viewerEntry = entry;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getMenuName() {
            return menuName;
        }

        public String getViewerClassName() {
            return viewerEntry.pluginClass.getCanonicalName();
        }
    }

    // ===============
    // Instance Fields
    // ===============

    private final ArrayList<PluginEntry> viewerPlugins = new ArrayList<PluginEntry>();

    private final Map<String, List<PluginEntry>> mimeTypeViewers = new HashMap<String, List<PluginEntry>>();

    private final Map<String, List<MenuEntry>> mimeMenuMappings = new HashMap<String, List<MenuEntry>>();

    private final List<Pair<MenuMapping, MenuEntry>> toolMenuMappings = new ArrayList<Pair<MenuMapping, MenuEntry>>();

    private final ArrayList<PluginEntry> toolPlugins = new ArrayList<PluginEntry>();

    private ViewerResourceLoader resourceHandler = null;

    /**
     * Unsorted tool menu list. Entries are appended as is.
     */
    protected List<Pair<List<String>, PluginEntry>> toolMenu = new ArrayList<Pair<List<String>, PluginEntry>>();

    public PluginRegistry(ViewerResourceLoader resourceHandler) {
        this.resourceHandler = resourceHandler;
        initDefaultViewers();
    }

    protected void initDefaultViewers() {
        registerPlugin(TextViewer.class);
        registerPlugin(ImageViewer.class);
        registerPlugin(ExtMediaViewer.class);
        registerPlugin(HexViewer.class);
        registerPlugin(X509Viewer.class);
        registerPlugin(JavaWebStarter.class);
    }

    public void registerPlugin(Class<? extends ViewerPlugin> viewerClass) {
        try {
            ViewerPlugin viewer = viewerClass.newInstance();
            String viewerName = viewer.getViewerName();
            PluginEntry entry = new PluginEntry(viewerName, viewerClass);
            viewerPlugins.add(entry);

            if (viewer instanceof MimeViewer) {
                MimeViewer mimeViewer = (MimeViewer) viewer;
                registerMimeTypes(mimeViewer.getMimeTypes(), entry);
                registerMimeMenuMappings(mimeViewer.getMimeMenuMethods(), entry);
            }

            if (viewer instanceof ToolPlugin) {
                registerTool((ToolPlugin) viewer, entry);
            }

        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Failed to register viewer class:" + viewerClass, e);
        }
    }

    protected void registerTool(ToolPlugin toolPlugin, PluginEntry entry) {
        toolPlugins.add(entry);

        if (toolPlugin.addToToolMenu()) {
            String[] menuPath = toolPlugin.getToolMenuPath();
            updateToolMenu(menuPath, toolPlugin.getToolName(), toolPlugin, entry);
        }

        List<Pair<MenuMapping, List<String>>> mappings = toolPlugin.getMenuMappings();
        if (mappings != null) {
            registerToolMenuMappings(mappings, entry);
        }
    }

    protected void updateToolMenu(String[] menuPath, String toolName, ToolPlugin toolPlugin, PluginEntry pluginEntry) {
        toolMenu.add(new Pair<List<String>, PluginEntry>(new StringList(menuPath), pluginEntry));
    }

    protected void registerMimeTypes(String[] mimeTypes, PluginEntry entry) {
        if (mimeTypes == null) {
            log.warn("No mime types for Viewer:<{}:>{}", entry.pluginClass, entry.pluginName);
            return;
        }
        for (String type : mimeTypes) {
            List<PluginEntry> list = this.mimeTypeViewers.get(type);

            if (list == null) {
                list = new ArrayList<PluginEntry>();
                mimeTypeViewers.put(type, list);
            }

            // Insert first so that later registered custom viewrs have higher priority.
            list.add(0, entry);
        }
    }

    protected void registerMimeMenuMappings(Map<String, List<String>> map, PluginEntry entry) {
        if ((map == null) || (map.size() <= 0)) {
            return;
        }

        String[] mimeTypes = map.keySet().toArray(new String[0]);

        for (String type : mimeTypes) {
            // Combine menu methods per MimeType:
            List<MenuEntry> combinedList = this.mimeMenuMappings.get(type);

            if (combinedList == null) {
                combinedList = new ArrayList<MenuEntry>();
                mimeMenuMappings.put(type, combinedList);
            }

            if (map.get(type) == null) {
                continue;
            }

            for (String methodDef : map.get(type)) {
                // Split: "<methodName>:<Menu Name>"
                String[] strs = methodDef.split(":");

                String method = strs[0];
                String menuName = method;
                if (strs.length > 1) {
                    menuName = strs[1];
                }

                MenuEntry menuEntry = new MenuEntry(method, menuName, entry);

                // Merge ?
                combinedList.add(menuEntry);
            }
        }
    }

    protected void registerToolMenuMappings(List<Pair<MenuMapping, List<String>>> mappings, PluginEntry viewerEntry) {
        if ((mappings == null) || (mappings.size() <= 0)) {
            return;
        }

        for (Pair<MenuMapping, List<String>> pair : mappings) {
            MenuMapping menuMap = pair.left();
            List<String> methodDefs = pair.right();

            if (methodDefs == null)
                continue;
            for (String methodDef : methodDefs) {
                MenuEntry menuEntry = this.createMenuEntry(methodDef, viewerEntry);
                Pair<MenuMapping, MenuEntry> menuPair = new Pair<MenuMapping, MenuEntry>(menuMap, menuEntry);
                toolMenuMappings.add(menuPair);
            }
        }
    }

    private MenuEntry createMenuEntry(String methodDef, PluginEntry viewerEntry) {
        // Split: "<methodName>:<Menu Name>"
        String[] strs = methodDef.split(":");

        String method = strs[0];
        String menuName = method;
        if (strs.length > 1) {
            menuName = strs[1];
        }

        MenuEntry menuEntry = new MenuEntry(method, menuName, viewerEntry);
        return menuEntry;
    }

    public Class<? extends ViewerPlugin> getMimeTypeViewerClass(String mimeType, String resourceType, String resourceStatus) {
        List<PluginEntry> list = this.mimeTypeViewers.get(mimeType);

        if ((list == null) || (list.size() < 0)) {
            return null;
        }

        return list.get(0).getViewerClass();
    }

    public ViewerPlugin createViewer(Class<? extends ViewerPlugin> viewerClass) {
        ViewerPlugin viewerPlugin = null;

        try {
            viewerPlugin = viewerClass.newInstance();
        } catch (Exception e) {
            log.error("Could not instanciate:" + viewerClass, e);
        }

        return viewerPlugin;
    }

    public ViewerResourceLoader getResourceHandler() {
        return resourceHandler;
    }

    public PluginEntry[] getViewers() {
        // return private array
        return this.viewerPlugins.toArray(new PluginEntry[0]);
    }

    public List<Pair<List<String>, PluginEntry>> getToolMenus() {
        return this.toolMenu;
    }

    /**
     * Addes menu entries for the specified mimeType to the MenuEntry List
     *
     * @param entries  - List to add entries to
     * @param mimeType
     */
    public int addMimeMenuEntries(List<MenuEntry> entries, String mimeType) {
        List<MenuEntry> list = mimeMenuMappings.get(mimeType);
        if ((list == null) || (list.size() <= 0))
            return 0;

        entries.addAll(list);
        return list.size();
    }

    public int addToolMenuMappings(List<MenuEntry> menuEntries, MenuMappingMatcher matcher) {
        if ((toolMenuMappings == null) || (toolMenuMappings.size() <= 0)) {
            return 0;
        }

        int num = 0;

        for (Pair<MenuMapping, MenuEntry> mapping : this.toolMenuMappings) {
            MenuMapping menuMap = mapping.left();
            if (matcher.matches(menuMap)) {
                MenuEntry entry = mapping.right();
                menuEntries.add(entry);
                num++;
            }
        }
        return num;
    }

}
