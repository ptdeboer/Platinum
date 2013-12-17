package nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin;

/** 
 * Default interface for Viewer Plugins. 
 * 
 */
public interface ViewerPlugin
{

    public String getViewerName();

    /**
     * Bindings to get Actual ViewerPanel object associated with this ViewerPlugin.  
     * This means the ViewerPanel should be initialized when this method is called. 
     * Only one ViewerPanel may be associated with one ViewerPlugin.  
     * 
     * @return Actual ViewerPanel component.  
     */
    public ViewerPanel getViewerPanel();

}
