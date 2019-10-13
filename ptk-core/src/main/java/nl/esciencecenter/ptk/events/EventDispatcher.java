/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.ptk.events;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Generic Event Dispatcher.
 * <p>
 * Maintains an event queue of
 * <code>EventT<code> events and dispatches them to registered listeners.
 * Listeners can register themself to many EventSource instances.
 *
 * @param <EventTypeT> Event Type.
 * @param <EventT>     Event implementation.
 */
@Slf4j
public class EventDispatcher<EventTypeT, EventT extends IEvent<EventTypeT>, EventListenerT extends IEventListener<EventT>> {

    protected int eventIdleWaitTime = 60 * 1000;

    protected Object waitMutex = new Object();

    protected class Dispatcher implements Runnable {

        protected boolean mustStop = false;

        protected boolean isStopped = false;

        protected Thread thread = null;

        @Override
        public void run() {
            try {
                doDispatcherLoop();
            } catch (Throwable e) {
                e.printStackTrace();
            }

            isStopped = true;
        }

        public void start() {
            if (thread != null) {
                throw new Error("Dispatcher Already Started!");
            }

            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        /**
         * Actual dispatch loop which call handleEvent() to dispatch a single event.
         */
        protected void doDispatcherLoop() {
            while (!mustStop) {
                if (Thread.interrupted()) {
                    Thread.currentThread().interrupt();
                    break;
                }

                if (hasEvents()) {
                    handleEvent();
                } else {
                    try {
                        synchronized (waitMutex) {
                            waitMutex.wait(eventIdleWaitTime);
                        }
                    } catch (InterruptedException e) {
                        log.error("Interrupted", e);
                    }
                }
            }
        }

        public void stop() {
            this.mustStop = true;
            wakeUp();
        }

        protected void interrupt() {
            if (thread != null) {
                thread.interrupt();
            }
        }

        protected void wakeUp() {
            synchronized (waitMutex) {
                waitMutex.notifyAll();
            }
        }

        public boolean isRunning() {
            if (thread == null) {
                return false;
            }

            return thread.isAlive();

        }
    }

    public class EventListenerEntry {

        protected EventListenerT listener;

        protected Object eventSource;

        protected boolean receiveAll;

        public EventListenerEntry(EventListenerT listener, Object eventSource) {
            this.listener = listener;
            this.eventSource = eventSource;
            this.receiveAll = false;
        }
    }

    // ========================================================================
    //
    // ========================================================================

    protected ArrayList<EventT> events = new ArrayList<EventT>();

    protected ArrayList<EventListenerEntry> listeners = new ArrayList<EventListenerEntry>();

    private Dispatcher dispatcher = null;

    /**
     * Create an EventDispatcher for type <code>EventT</code>
     *
     * @param autoStart
     */
    public EventDispatcher(boolean autoStart) {
        init(autoStart);
    }

    private void init(boolean autoStart) {
        this.dispatcher = new Dispatcher();
        if (autoStart) {
            start();
        }
    }

    /**
     * Start the Event Dispatcher.
     */
    public void start() {
        if (dispatcher.isRunning() == false) {
            dispatcher.start();
        }
    }

    /**
     * Schedule Event
     *
     * @param newEvent
     */
    public void fireEvent(EventT newEvent) {
        addEvent(newEvent);
        wakeupDispatcher();
    }

    protected void addEvent(EventT newEvent) {
        synchronized (events) {
            events.add(newEvent);
        }
    }

    protected void removeEvent(EventT event) {
        synchronized (events) {
            events.remove(event);
        }
    }

    protected void wakeupDispatcher() {
        dispatcher.wakeUp();
    }

    public void addListener(EventListenerT listener, Object eventSource) {
        synchronized (listeners) {
            this.listeners.add(new EventListenerEntry(listener, eventSource));
        }
    }

    /**
     * Remove all the listener which are registered for the specified EventSource
     *
     * @param source the EventSourceT object to unregister all the Listeners for.
     */
    public void removeListenersFor(Object source) {
        synchronized (listeners) {
            // first filter
            ListIterator<EventListenerEntry> iterator = listeners.listIterator();
            EventListenerEntry entry = null;
            ArrayList<EventListenerEntry> filtered = new ArrayList<EventListenerEntry>();
            while (iterator.hasNext()) {
                entry = iterator.next();
                if (entry.eventSource.equals(source)) {
                    filtered.add(entry);
                }
            }
            // now remove:
            for (EventListenerEntry entryDel : filtered) {
                listeners.remove(entryDel);
            }
        }
    }

    public void removeListener(EventListenerT listener) {
        synchronized (listeners) {
            ListIterator<EventListenerEntry> iterator = listeners.listIterator();
            EventListenerEntry theEntry = null;
            // search
            while (iterator.hasNext()) {
                theEntry = iterator.next();
                if (theEntry.equals(listener)) {
                    break;
                }
            }
            // and destroy
            if (theEntry != null) {
                listeners.remove(listener);
            }
        }
    }

    protected EventT popEvent() {
        synchronized (this.events) {
            if (events.size() <= 0) {
                return null;
            }

            EventT event = this.events.get(0);
            this.events.remove(0);
            return event;
        }
    }

    public boolean hasEvents() {
        synchronized (this.events) {
            return (this.events.size() > 0);
        }
    }

    /**
     * Pop an event and dispatch it to the registered listeners.
     */
    protected void handleEvent() {
        EventT event = this.popEvent();

        if (event == null) {
            return;
        }

        if (listeners.size() <= 0) {
            return;
        }
        // Use iterator
        ListIterator<EventListenerEntry> iterator = listeners.listIterator();
        // 
        while (iterator.hasNext()) {
            EventListenerEntry entry = iterator.next();
            boolean notify = true;
            if (entry.receiveAll == false) {
                notify = matchEventSource(entry.listener, entry.eventSource, event);
            }
            if (notify) {
                entry.listener.notifyEvent(event);
            }
        }
    }

    public void stop() {
        this.dispatcher.stop();
    }

    public void dispose() {
        stop();
        this.listeners.clear();
        this.events.clear();
    }

    /**
     * When a single listener is registered to many EventSourceT instances, filter out the actual
     * event which matches the wanted EventSource. The
     * <code>equals()<code> method of the EventSourceT is used to match EventSourceT object types.
     *
     * @param listener    - registered listener.
     * @param eventSource - the EventSourceT Object the listener listenes for.
     * @param event       - the actual event to match.
     * @return true if the EvenSourceT object matches the eventSource from the event.
     */
    protected boolean matchEventSource(EventListenerT listener, Object eventSource, EventT event) {
        if (eventSource == null) {
            return true;
        }
        Object actualSource = event.getEventSource();
        if (actualSource == null)
            return false;
        return (actualSource.equals(eventSource));
    }

}
