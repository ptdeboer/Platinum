package nl.esciencecenter.vbrowser.vrs.event;

import org.slf4j.LoggerFactory;

import nl.esciencecenter.ptk.events.EventDispatcher;

public class VRSEventDispatcher extends
        EventDispatcher<VRSEventType,VRSEvent, VRSEventListener> {

    public VRSEventDispatcher(boolean autoStart) {
        super(autoStart);
    }

    @Override
    protected boolean matchEventSource(VRSEventListener listener, Object wantedEventSource, VRSEvent event) {
        LoggerFactory.getLogger(VRSEventDispatcher.class).info(">>>COMPARE:'{}' <=> '{}'", event.getEventSource(), wantedEventSource);
        // filter VRLs?
        return super.matchEventSource(listener, wantedEventSource, event);
    }

}
