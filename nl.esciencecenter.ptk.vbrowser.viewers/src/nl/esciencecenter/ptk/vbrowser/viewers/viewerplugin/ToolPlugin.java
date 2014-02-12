package nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin;

import javax.swing.Icon;

/**
 * Interface for Viewers which are Custom "Tools". These viewers will appears
 * under the "Tools" menu and optionally have their own ToolBar.
 */
public interface ToolPlugin
{

    public String getToolName(); 
    
    /**
     * Whether to add tool under "Tools" menu.
     */
    public boolean addToToolMenu();

    /**
     * Menu path to appear under "Tools" menu of the browser. For example
     * {"util","binary viewers"}.
     * 
     * @return array of menu path names.
     */
    public String[] getToolMenuPath();

    /**
     * Toolbar name to group other tools to the same ToolBar if createToolBar()
     * is true. If null, no toolbar will be created. 
     * 
     * @return
     */
    public String toolBarName();

    /**
     * Default method name to use when the viewer is started from the Tool Menu.
     * see {@link ViewerPanel#startViewerFor(java.net.URI, String)}
     * 
     * @return
     */
    public String defaultToolMethod();

    /**
     * Return custom tool Icon. Parameter size indicates minimum size of icon.
     * Icons are automatically resized to fit the menu or Toolbar.<br>
     * To avoid upscaling of the icon return at least an icon with a Height &gt; size and Width &gt; size. 
     */
    public Icon getToolIcon(int size);

}
