package nl.esciencecenter.vbrowser.vrs.event;

import java.util.Vector;

import nl.esciencecenter.ptk.task.ActionTask;
import nl.esciencecenter.ptk.util.logging.PLogger;

/*
 * Refactored into generics. 
 */
public class VRSEventNotifierOld {

    // ========================================================================
    //
    // ========================================================================

    private static VRSEventNotifierOld instance;

    private static PLogger logger;

    static {
        logger = PLogger.getLogger(VRSEventNotifier.class);
        instance = new VRSEventNotifierOld();
    }

    /**
     * Single instance for all VRSEvents!
     */
    public static VRSEventNotifierOld getInstance() {
        return instance;
    }

    // ========================================================================
    //
    // ========================================================================

    private Vector<VRSEventListener> listeners = new Vector<VRSEventListener>();

    private Vector<VRSEvent> events = new Vector<VRSEvent>();

    private ActionTask notifierTask;

    private volatile boolean doNotify = true;

    protected VRSEventNotifierOld() {
        startNotifier();
    }

    protected void startNotifier() {
        this.notifierTask = new ActionTask(null, "ProxyViewNodeEventNotifier task") {
            @Override
            protected void doTask() throws Exception {
                try {
                    doNotifyLoop();
                } catch (Throwable t) {
                    logger.errorPrintf("Notifyer event thread exception=%s\n", t);
                    t.printStackTrace();
                }
            }

            @Override
            protected void stopTask() throws Exception {
                stopNotifier();
            }
        };

        this.notifierTask.startDaemonTask();
    }

    public void stopNotifier() {
        this.doNotify = false;
    }

    protected void doNotifyLoop() {
        logger.debugPrintf("Starting notifyerloop");

        while (doNotify) {
            VRSEvent event = getNextEvent();
            if (event != null) {
                notifyEvent(event);
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        logger.debugPrintf("Notifyerloop has stopped.");
    }

    private void notifyEvent(VRSEvent event) {
        for (VRSEventListener listener : getListeners()) {
            try {
                listener.notifyEvent(event);
            } catch (Throwable t) {
                logger.errorPrintf("***Exception during event notifiation:%s\n", t);
                t.printStackTrace();
            }
        }
    }

    private VRSEventListener[] getListeners() {
        // create private copy
        synchronized (this.listeners) {
            VRSEventListener _arr[] = new VRSEventListener[this.listeners.size()];
            _arr = this.listeners.toArray(_arr);
            return _arr;
        }
    }

    private VRSEvent getNextEvent() {
        synchronized (this.events) {
            if (this.events.size() <= 0)
                return null;

            VRSEvent event = this.events.get(0);
            this.events.remove(0);
            return event;
        }
    }

    public void scheduleEvent(VRSEvent event) {
        synchronized (this.events) {
            this.events.add(event);
        }
    }

    public void addListener(VRSEventListener listener) {
        synchronized (this.listeners) {
            this.listeners.add(listener);
        }
    }

    public void removeListener(VRSEventListener listener) {
        synchronized (this.listeners) {
            this.listeners.remove(listener);
        }
    }

}
