package nl.esciencecenter.ptk.vbrowser.viewers.events;

import nl.esciencecenter.ptk.events.EventDispatcher;
import org.slf4j.LoggerFactory;

public class ViewerEventDispatcher extends
        EventDispatcher<ViewerEventType, ViewerEvent, ViewerListener> {

    public ViewerEventDispatcher(boolean autoStart) {
        super(autoStart);
    }

    @Override
    protected boolean matchEventSource(ViewerListener listener, Object wantedEventSource, ViewerEvent event) {
        LoggerFactory.getLogger(ViewerEventDispatcher.class).debug(">>>COMPARE:'{}' <=> '{}'", event.getEventSource(), wantedEventSource);
        // filter viewers?
        return super.matchEventSource(listener, wantedEventSource, event);
    }

}
