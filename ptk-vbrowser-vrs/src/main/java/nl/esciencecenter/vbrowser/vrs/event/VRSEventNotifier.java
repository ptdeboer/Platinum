package nl.esciencecenter.vbrowser.vrs.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.ptk.events.EventDispatcher;

public class VRSEventNotifier extends
        EventDispatcher<VRSEventType,VRSEvent, VRSEventListener> {

    // ========================================================================
    //
    // ========================================================================

    private static VRSEventNotifier instance;

    private static Logger logger;

    static {
        logger = LoggerFactory.getLogger(VRSEventNotifier.class);
        instance = new VRSEventNotifier(true);
    }

    /**
     * Single instance for all VRSEvents!
     */
    public static VRSEventNotifier getInstance() {
        return instance;
    }
    
    // ========================================================================
    //
    // ========================================================================

    
    public VRSEventNotifier(boolean autoStart) {
        super(autoStart);
    }

    @Override
    protected boolean matchEventSource(VRSEventListener listener, Object wantedEventSource, VRSEvent event) {
        logger.info(">>>COMPARE:'{}' <=> '{}'", event.getEventSource(), wantedEventSource);
        // filter VRLs?
        return super.matchEventSource(listener, wantedEventSource, event);
    }

    public void scheduleEvent(VRSEvent newEvent) {
        this.fireEvent(newEvent);
    }

}
