package nl.esciencecenter.ptk.vbrowser.viewers.events;

import nl.esciencecenter.ptk.events.EventDispatcher;

public class ViewerEventDispatcher extends
        EventDispatcher<ViewerEvent, ViewerEventSource, ViewerListener> {

    public ViewerEventDispatcher(boolean autoStart) {
        super(autoStart);
    }

}
