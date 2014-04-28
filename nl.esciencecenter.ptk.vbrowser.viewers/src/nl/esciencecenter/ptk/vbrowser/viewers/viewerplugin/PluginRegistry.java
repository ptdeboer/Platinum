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

package nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.viewers.internal.HexViewer;
import nl.esciencecenter.ptk.vbrowser.viewers.internal.ImageViewer;
import nl.esciencecenter.ptk.vbrowser.viewers.internal.JavaWebStarter;
import nl.esciencecenter.ptk.vbrowser.viewers.internal.TextViewer;
import nl.esciencecenter.ptk.vbrowser.viewers.vrs.ViewerResourceLoader;
import nl.esciencecenter.ptk.vbrowser.viewers.x509viewer.X509Viewer;

/**
 * Viewer and Tool Plugin Registry for the VBrowser. 
 */
public class PluginRegistry
{
    private static ClassLogger logger = ClassLogger.getLogger(PluginRegistry.class);

    public class ViewerEntry
    {
        protected Class<? extends ViewerPlugin> viewerClass;

        protected String viewerName;

        ViewerEntry(String viewerName, Class<? extends ViewerPlugin> viewerClass)
        {
            this.viewerClass = viewerClass;
            this.viewerName = viewerName;
        }

        public Class<? extends ViewerPlugin> getViewerClass()
        {
            return viewerClass;
        }

        public String getName()
        {
            return viewerName;
        }
    }

    public class MimeMenuEntry
    {
        String methodName;

        String menuName;

        ViewerEntry viewerEntry;

        public MimeMenuEntry(String method, String menuNameValue, ViewerEntry entry)
        {
            methodName = method;
            menuName = menuNameValue;
            viewerEntry = entry;
        }

        public String getMethodName()
        {
            return methodName;
        }

        public String getMenuName()
        {
            return menuName;
        }

        public String getViewerClassName()
        {
            return viewerEntry.viewerClass.getCanonicalName();
        }
    }

    // ===============
    // Instance Fields
    // ===============

    private ArrayList<ViewerEntry> viewerPlugins = new ArrayList<ViewerEntry>();

    private Map<String, List<ViewerEntry>> mimeTypeViewers = new HashMap<String, List<ViewerEntry>>();

    private Map<String, List<MimeMenuEntry>> mimeMenuMappings = new HashMap<String, List<MimeMenuEntry>>();

    private ArrayList<ViewerEntry> toolPlugins = new ArrayList<ViewerEntry>();

    private ViewerResourceLoader resourceHandler = null;

    public PluginRegistry(ViewerResourceLoader resourceHandler)
    {
        this.resourceHandler = resourceHandler;
        initDefaultViewers();
    }

    protected void initDefaultViewers()
    {
        registerViewer(TextViewer.class);
        registerViewer(ImageViewer.class);
        registerViewer(HexViewer.class);
        registerViewer(X509Viewer.class);
        registerViewer(JavaWebStarter.class);
    }

    public void registerViewer(Class<? extends ViewerPlugin> viewerClass)
    {

        try
        {
            ViewerPlugin viewer = viewerClass.newInstance();
            String viewerName = viewer.getViewerName();
            ViewerEntry entry = new ViewerEntry(viewerName, viewerClass);
            viewerPlugins.add(entry);

            if (viewer instanceof MimeViewer)
            {
                MimeViewer mimeViewer = (MimeViewer) viewer;
                registerMimeTypes(mimeViewer.getMimeTypes(), entry);
                registerMimeMenuMappings(mimeViewer.getMimeMenuMethods(), entry);
            }

            if (viewer instanceof ToolPlugin)
            {
                registerTool((ToolPlugin) viewer, entry);
            }

        }
        catch (InstantiationException | IllegalAccessException e)
        {
            logger.logException(ClassLogger.ERROR, e, "Failed to register viewer class:%s\n", viewerClass);
        }
    }

    private void registerTool(ToolPlugin viewer, ViewerEntry entry)
    {
        toolPlugins.add(entry);

        if (viewer.addToToolMenu())
        {
            String menuPath[] = viewer.getToolMenuPath();
            updateToolMenu(menuPath, viewer.getToolName());
        }

    }

    protected void updateToolMenu(String[] menuPath, String toolName)
    {

    }

    protected void registerMimeTypes(String[] mimeTypes, ViewerEntry entry)
    {
        for (String type : mimeTypes)
        {
            List<ViewerEntry> list = this.mimeTypeViewers.get(type);

            if (list == null)
            {
                list = new ArrayList<ViewerEntry>();
                mimeTypeViewers.put(type, list);
            }

            list.add(entry);
        }
    }

    protected void registerMimeMenuMappings(Map<String, List<String>> map, ViewerEntry entry)
    {
        if ((map == null) || (map.size() <= 0))
        {
            return;
        }

        String mimeTypes[] = map.keySet().toArray(new String[0]);

        for (String type : mimeTypes)
        {
            // Combine menu methods per MimeType:
            List<MimeMenuEntry> combinedList = this.mimeMenuMappings.get(type);

            if (combinedList == null)
            {
                combinedList = new ArrayList<MimeMenuEntry>();
                mimeMenuMappings.put(type, combinedList);
            }

            if (map.get(type) == null)
            {
                continue;
            }

            for (String methodDef : map.get(type))
            {
                // Split: "<methodName>:<Menu Name>"
                String strs[] = methodDef.split(":");

                String method = strs[0];
                String menuName = method;
                if (strs.length > 1)
                {
                    menuName = strs[1];
                }

                MimeMenuEntry menuEntry = new MimeMenuEntry(method, menuName, entry);

                // Merge ?
                combinedList.add(menuEntry);
            }

        }
    }

    public Class<? extends ViewerPlugin> getMimeTypeViewerClass(String mimeType)
    {
        List<ViewerEntry> list = this.mimeTypeViewers.get(mimeType);

        if ((list == null) || (list.size() < 0))
        {
            return null;
        }

        return list.get(0).getViewerClass();

    }

    public ViewerPlugin createViewer(Class<? extends ViewerPlugin> viewerClass)
    {
        ViewerPlugin viewerPlugin = null;

        try
        {
            viewerPlugin = viewerClass.newInstance();
        }
        catch (Exception e)
        {
            logger.logException(ClassLogger.ERROR, e, "Could not instanciate:%s\n", viewerClass);
        }

        return viewerPlugin;
    }

    public ViewerResourceLoader getResourceHandler()
    {
        return resourceHandler;
    }

    public ViewerEntry[] getViewers()
    {
        // return array
        return this.viewerPlugins.toArray(new ViewerEntry[0]);
    }

    /**
     * Returns list of Menu entries for the specified mimeType.
     */
    public List<MimeMenuEntry> getMimeMenuEntries(String mimeType)
    {
        return this.mimeMenuMappings.get(mimeType);
    }

}
