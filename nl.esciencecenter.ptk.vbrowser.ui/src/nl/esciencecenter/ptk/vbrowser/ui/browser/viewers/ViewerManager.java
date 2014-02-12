package nl.esciencecenter.ptk.vbrowser.ui.browser.viewers;

import nl.esciencecenter.ptk.vbrowser.ui.browser.ProxyBrowserController;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerFrame;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerPanel;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerPlugin;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.PluginRegistry;

public class ViewerManager
{
    private ProxyBrowserController browser;

    public ViewerManager(ProxyBrowserController proxyBrowser)
    {
        browser=proxyBrowser;
    }

    public ViewerPanel createViewerFor(ViewNode node,String optViewerClass) throws ProxyException
    {
        String resourceType = node.getResourceType();
        // String resourceStatus = node.getResourceStatus();
        String mimeType = node.getMimeType();
        
        return createViewerFor(resourceType,mimeType,optViewerClass); 
    }
    
    public ViewerPanel createViewerFor(String resourceType,String mimeType,String optViewerClass) throws ProxyException
    {
        PluginRegistry registry = browser.getPlatform().getViewerRegistry();

        Class<?> clazz=null; 
        
        if (optViewerClass!=null)
        {
            clazz = loadViewerClass(optViewerClass); 
        }
        
        if ((clazz==null) && (mimeType!=null))
        {
            if (clazz==null)
            {
                clazz=registry.getMimeTypeViewerClass(mimeType);
            }
        }
        
        if (clazz==null)
            return null; 
        
        if (ViewerPlugin.class.isAssignableFrom(clazz)==false)
        {
            throw new ProxyException("Viewer Class is not a ViewerPlugin class:"+clazz); 
        }
        
        ViewerPanel viewer = registry.createViewer((Class<? extends ViewerPlugin>) clazz);
        return viewer;
    }
    
    private Class<?> loadViewerClass(String optViewerClass)
    {
        try
        {
            return this.getClass().getClassLoader().loadClass(optViewerClass);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            return null; 
        } 
    }

    public ViewerFrame createViewerFrame(ViewerPanel viewerClass, boolean initViewer)
    {
        return ViewerFrame.createViewerFrame(viewerClass,initViewer);
    }
}
