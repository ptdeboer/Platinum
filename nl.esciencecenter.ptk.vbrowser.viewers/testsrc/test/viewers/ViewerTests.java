package test.viewers;

import java.net.URI;

import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerFrame;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerPanel;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerPlugin;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerRegistry;
import nl.esciencecenter.ptk.vbrowser.viewers.vrs.ViewerResourceHandler;

public class ViewerTests
{
    private static ViewerRegistry viewerRegistry;

    public static ViewerRegistry getViewerRegistry()
    {
        if (viewerRegistry == null)
        {
            viewerRegistry = new ViewerRegistry(new ViewerResourceHandler(new ResourceLoader()));
        }
        
        return viewerRegistry;
    }
    
    public static ViewerFrame startViewer(Class<? extends ViewerPlugin> class1, URI optionalURI)
    {
        ViewerPanel newViewer=getViewerRegistry().createViewer(class1); 
        
        ViewerFrame frame=createViewerFrame(newViewer,true); 
        frame.getViewer().startViewerFor(optionalURI,null); 
        frame.setVisible(true); 
        
        return frame;
    }

    public static ViewerFrame createViewerFrame(ViewerPanel newViewer, boolean initViewer)
    {
        
        ViewerFrame frame=new ViewerFrame(newViewer); 
        if (initViewer)
        {
            newViewer.initViewer();  
        }
        frame.pack(); 
        frame.setSize(frame.getPreferredSize()); 
        //frame.setSize(800,600); 
        
        return frame; 
    }
    
    public static void testViewer(Class<? extends ViewerPlugin> class1, URI uri)
    {
        ViewerFrame frame = startViewer(class1, uri);
        
    }

}
