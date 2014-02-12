package nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin;

/** 
 * Default interface for Viewer Plugins. 
 * All Viewer plugins extend this interface. 
 */
public interface ViewerPlugin
{
    /** 
     * Short to be display in menu.
     */
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
