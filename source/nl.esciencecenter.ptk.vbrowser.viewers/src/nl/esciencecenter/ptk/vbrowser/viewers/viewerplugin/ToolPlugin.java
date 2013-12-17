package nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin;

import javax.swing.Icon;

/** 
 * Interface for Viewer which are Custom Tools. 
 * These viewers will appears under the "Tools" menu and optionally have their own ToolBar. 
 */
public interface ToolPlugin
{
 
    /**
     * Added under "Tools" menu 
     */
    public boolean addToToolMenu(); 
    
    /** 
     * Menu path to appear under "Tools" menu of the browser.  
     * @return
     */
    public String[] getToolMenuPath();
    
    /** 
     * Create custom ToolBar to be added to the browser. 
     * @return
     */
    boolean createToolBar(); 
    
    /**
     * Name to group other tools to the same ToolBar if createToolBar() is true. 
     * @return
     */
    public String toolBarName(); 

    /** 
     * Custom method name to use when the viewer is started from the Tool Menu.
     * see {@link ViewerPanel#startViewerFor(java.net.URI, String)} 
     * @return
     */
    public String viewerToolMethod(); 
    
    /** 
     * Custom Tool Icon. 
     * Method may return a tool mismatching the requested size. 
     * Icons are automatically resized to fit the menu or Toolbar. 
     */
    public Icon getToolIcon(int size);
    
}
