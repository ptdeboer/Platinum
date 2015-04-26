package nl.esciencecenter.ptk.vbrowser.viewers;

import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerEventDispatcher;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Context created by the VBrowser from which the current viewer has started.
 */
public class ViewerContext
{
    protected String startMethod = null;

    protected VRL startVRL = null;

    protected boolean startedAsStandalone = false;

    protected PluginRegistry pluginRegistry;

    protected ViewerEventDispatcher eventDispatcher;

    public ViewerContext(PluginRegistry viewerRegistry)
    {
        this.pluginRegistry = viewerRegistry;
        ; // default: nill context.
    }

    public ViewerContext(PluginRegistry viewerRegistry, String startMethod, VRL vrl, boolean standAlone)
    {
        this.startMethod = startMethod;
        this.startVRL = vrl;
        this.startedAsStandalone = standAlone;
        this.pluginRegistry = viewerRegistry;
    }

    public void setViewerEventDispatcher(ViewerEventDispatcher dispatcher)
    {
        this.eventDispatcher = dispatcher;
    }

    /**
     * @return the method this viewer was originally started with. Might be null if none was specified.
     */
    public String getStartMethod()
    {
        return startMethod;
    }

    /**
     * @return the VRL this viewer was originally started with.
     */
    public VRL getStartVRL()
    {
        return startVRL;
    }

    /**
     * @return false if this viewer is embeded inside the VBrowser or true if started in a stand alone (viewer)Frame.
     */
    public boolean getStartedAsStandalone()
    {
        return this.startedAsStandalone;
    }

    public PluginRegistry getPluginRegistry()
    {
        return pluginRegistry;
    }

    public ViewerEventDispatcher getViewerEventDispatcher()
    {
        return eventDispatcher;
    }

}
