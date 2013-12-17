package nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.viewers.internal.HexViewer;
import nl.esciencecenter.ptk.vbrowser.viewers.internal.ImageViewer;
import nl.esciencecenter.ptk.vbrowser.viewers.internal.JavaWebStarter;
import nl.esciencecenter.ptk.vbrowser.viewers.internal.TextViewer;
import nl.esciencecenter.ptk.vbrowser.viewers.x509viewer.X509Viewer;

public class ViewerRegistry
{
    private static ClassLogger logger = ClassLogger.getLogger(ViewerRegistry.class);

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

    private static ViewerRegistry instance;

    public static ViewerRegistry getDefault()
    {
        if (instance == null)
            instance = new ViewerRegistry(new ViewerResourceHandler(new ResourceLoader()));

        return instance;
    }

    // ===
    //
    // ===

    private ArrayList<ViewerEntry> viewers = new ArrayList<ViewerEntry>();

    private Map<String, List<ViewerEntry>> mimeTypeViewers = new HashMap<String, List<ViewerEntry>>();

    private Map<String, List<MimeMenuEntry>> mimeMenuMappings = new HashMap<String, List<MimeMenuEntry>>();

    private ArrayList<ViewerEntry> toolViewers = new ArrayList<ViewerEntry>();

    
    private ViewerResourceHandler resourceHandler = null;

    public ViewerRegistry(ViewerResourceHandler resourceHandler)
    {
        this.resourceHandler = resourceHandler;
        initViewers();
    }

    protected void initViewers()
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
            viewers.add(entry);

            if (viewer instanceof MimeViewer)
            {
                MimeViewer mimeViewer = (MimeViewer) viewer;
                registerMimeTypes(mimeViewer.getMimeTypes(), entry);
                registerMimeMenuMappings(mimeViewer.getMimeMenuMethods(), entry);
            }
            
            if (viewer instanceof ToolPlugin)
            {
                registerTool((ToolPlugin)viewer,entry);
            }

        }
        catch (InstantiationException | IllegalAccessException e)
        {
            logger.logException(ClassLogger.ERROR, e, "Failed to register viewer class:%s\n", viewerClass);
        }
    }

    private void registerTool(ToolPlugin viewer, ViewerEntry entry)
    {
        toolViewers.add(entry);  
        
        if (viewer.addToToolMenu())
        {
            String menuPath[]=viewer.getToolMenuPath();
            updateToolMenu(menuPath,viewer.getToolName()); 
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

    public ViewerPanel createViewer(Class<? extends ViewerPlugin> viewerClass)
    {
        ViewerPanel viewer = null;

        try
        {
            ViewerPlugin viewerPlugin = viewerClass.newInstance();
            viewer=viewerPlugin.getViewerPanel(); 
            viewer.setViewerRegistry(this);
        }
        catch (Exception e)
        {
            logger.logException(ClassLogger.ERROR, e, "Could not instanciate:%s\n", viewerClass);
        }

        return viewer;
    }

    public ViewerResourceHandler getResourceHandler()
    {
        return resourceHandler;
    }

    public ViewerEntry[] getViewers()
    {
        // return array
        return this.viewers.toArray(new ViewerEntry[0]);
    }

    /**
     * Returns list of Menu entries for the specified mimeType.
     */
    public List<MimeMenuEntry> getMimeMenuEntries(String mimeType)
    {
        return this.mimeMenuMappings.get(mimeType);
    }

}
