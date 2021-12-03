package nl.esciencecenter.vbrowser.vrs.event;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.events.EventDispatcher;

@Slf4j
public class VRSEventNotifier extends EventDispatcher<VRSEventType, VRSEvent, VRSEventListener> {

    private static final VRSEventNotifier instance;

    static {
        instance = new VRSEventNotifier(true);
    }

    /**
     * Single instance for all VRSEvents!
     */
    public static VRSEventNotifier getInstance() {
        return instance;
    }

    // === Instance === //

    public VRSEventNotifier(boolean autoStart) {
        super(autoStart);
    }

    @Override
    protected boolean matchEventSource(VRSEventListener listener, Object wantedEventSource, VRSEvent event) {
        log.debug(">>>COMPARE:'{}' <=> '{}'", event.getEventSource(), wantedEventSource);
        // filter VRLs?
        return super.matchEventSource(listener, wantedEventSource, event);
    }

    public void scheduleEvent(VRSEvent newEvent) {
        this.fireEvent(newEvent);
    }

}
