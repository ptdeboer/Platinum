package nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin;

import java.net.URI;

import javax.swing.JFrame;

/** 
 * Viewer Frame for embedded Viewer Panel. 
 */
public class ViewerFrame extends JFrame
{
    private static final long serialVersionUID = 3613838609500660102L;
    
    protected ViewerPanel viewer; 
    
    public ViewerFrame(ViewerPanel viewer)
    {
        this.viewer=viewer;
        initGui(); 
    }

    protected void initGui()
    {
        this.add(viewer);
    }
    
    public ViewerPanel getViewer()
    {
        return viewer; 
    }
     
    public static ViewerFrame startViewer(Class<? extends ViewerPlugin> class1, URI optionalURI)
    {
        ViewerPanel newViewer=ViewerRegistry.getDefault().createViewer(class1); 
        
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
}
