package nl.esciencecenter.ptk.vbrowser.viewers.events;

import nl.esciencecenter.ptk.events.IEvent;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerPanel;

public class ViewerEvent implements IEvent<ViewerPanel>
{
    protected ViewerPanel source; 
    
    public ViewerEvent(ViewerPanel source)
    {
        this.source=source;
    }
    
    public ViewerPanel getSource()
    {
        return this.source;
    }

}
