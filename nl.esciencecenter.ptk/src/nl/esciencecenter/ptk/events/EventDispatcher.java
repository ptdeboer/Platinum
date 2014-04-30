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

import java.util.ListIterator;
import java.util.Vector;

import nl.esciencecenter.ptk.util.logging.ClassLogger;

/**
 * Generic Event Dispatcher.
 * 
 * @param <EventT>
 *            Event Type.
 * @param <EventSourceT>
 *            Class which sends the Events.
 * @param <EventListenerT>
 *            Event listener which receives events with type EventT.
 */
public class EventDispatcher<EventT, EventSourceT, EventListener extends IEventListener<EventT>>
{
    private static ClassLogger logger = ClassLogger.getLogger(EventDispatcher.class);

    // ========================================================================
    //
    // ========================================================================

    protected int eventIdleWaitTime = 10 * 1000;

    protected Object waitMutex = new Object();

    protected class Dispatcher implements Runnable
    {
        protected boolean mustStop = false;

        protected boolean isStopped = false;

        protected Thread thread = null;

        @Override
        public void run()
        {
            try
            {
                doDispatcherLoop();
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }

            isStopped = true;
        }

        void start()
        {
            if (thread != null)
            {
                throw new Error("Dispatcher Already Started!");
            }

            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        protected void doDispatcherLoop()
        {
            while (!mustStop)
            {
                if (Thread.interrupted())
                {
                    // keep interrupted status.
                    Thread.currentThread().interrupt();
                    break;
                }

                if (hasEvents())
                {
                    logger.infoPrintf("hasEvents(): Nr of events=%d", events.size());
                    handleEvent();
                }
                else
                {
                    try
                    {
                        synchronized (waitMutex)
                        {
                            waitMutex.wait(eventIdleWaitTime);
                        }

                        logger.infoPrintf("Wakeup No events\n");
                    }
                    catch (InterruptedException e)
                    {
                        logger.infoPrintf("<Interrupted>\n");
                    }
                }
            }
        }

        void stop()
        {
            this.mustStop = true;
            wakeUp();
        }

        void interrupt()
        {
            if (thread != null)
            {
                thread.interrupt();
            }
        }

        void wakeUp()
        {
            synchronized (waitMutex)
            {
                waitMutex.notifyAll();
            }
        }

        boolean isRunning()
        {
            if (thread == null)
            {
                return false;
            }

            if (thread.isAlive())
            {
                return true;
            }

            return false;
        }
    }

    public class EventListenerEntry
    {
        protected EventListener listener;

        protected EventSourceT eventSource;

        public EventListenerEntry(EventListener listener, EventSourceT eventSource)
        {
            this.listener = listener;
            this.eventSource = eventSource;
        }
    }

    // ========================================================================
    //
    // ========================================================================

    protected Vector<EventT> events = new Vector<EventT>();

    protected Vector<EventListenerEntry> listeners = new Vector<EventListenerEntry>();

    private Dispatcher dispatcher = null;

    public EventDispatcher(boolean autoStart)
    {
        initDispatcher(autoStart);
    }

    private void initDispatcher(boolean autoStart)
    {
        this.dispatcher = new Dispatcher();
        if (autoStart)
        {
            dispatcher.start();
        }

        logger.setLevelToDebug();
    }

    public void fireEvent(EventT newEvent)
    {
        addEvent(newEvent);
        wakeupDispatcher();
    }

    protected void addEvent(EventT newEvent)
    {
        synchronized (events)
        {
            events.add(newEvent);
        }
    }

    protected void removeEvent(EventT event)
    {
        synchronized (events)
        {
            events.remove(event);
        }
    }

    protected void wakeupDispatcher()
    {
        dispatcher.wakeUp();
    }

    public void addListener(EventListener listener, EventSourceT eventSource)
    {
        synchronized (listeners)
        {
            this.listeners.add(new EventListenerEntry(listener, eventSource));
        }
    }

    public void removeListener(EventListener listener)
    {
        synchronized (listeners)
        {
            ListIterator<EventListenerEntry> iterator = listeners.listIterator();

            EventListenerEntry theEntry = null;

            while (iterator.hasNext())
            {
                theEntry = iterator.next();

                if (theEntry == listener)
                {
                    break;
                }
            }

            if (theEntry != null)
            {
                listeners.remove(listener);
            }
        }
    }

    protected EventT popEvent()
    {
        synchronized (this.events)
        {
            if (events.size() <= 0)
            {
                return null;
            }

            EventT event = this.events.get(0);
            this.events.remove(0);
            return event;
        }
    }

    public boolean hasEvents()
    {
        synchronized (this.events)
        {
            return (this.events.size() > 0);
        }
    }

    protected void handleEvent()
    {
        EventT event = this.popEvent();

        if (event == null)
        {
            logger.infoPrintf("No Event\n");
            return;
        }

        if (listeners.size() <= 0)
        {
            logger.infoPrintf("No Event Listeners registered for event:%s\n", event);
            return;
        }

        // Use iterator
        ListIterator<EventListenerEntry> iterator = listeners.listIterator();

        while (iterator.hasNext())
        {
            EventListenerEntry entry = iterator.next();
            entry.listener.notifyEvent(event);
        }
    }

    public void stop()
    {
        this.dispatcher.stop();
    }

    public void dispose()
    {
        this.listeners.clear();
        this.events.clear();
    }
}
