package nl.esciencecenter.ptk.vbrowser.viewers.events;

import org.slf4j.LoggerFactory;

import nl.esciencecenter.ptk.events.EventDispatcher;

public class ViewerEventDispatcher extends
        EventDispatcher<ViewerEventType,ViewerEvent, ViewerListener> {

    public ViewerEventDispatcher(boolean autoStart) {
        super(autoStart);
    }

    @Override
    protected boolean matchEventSource(ViewerListener listener, Object wantedEventSource, ViewerEvent event) {
        LoggerFactory.getLogger(ViewerEventDispatcher.class).info(">>>COMPARE:'{}' <=> '{}'", event.getEventSource(), wantedEventSource);
        // filter viewers?
        return super.matchEventSource(listener, wantedEventSource, event);
    }

}
