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

import java.util.Vector;

import nl.esciencecenter.ptk.util.logging.ClassLogger;

public class EventDispatcher<EventT extends IEvent<EventSourceT>, EventSourceT extends IEventSource, EventListenerT extends IEventListener>
{
    private static ClassLogger logger = ClassLogger.getLogger(EventDispatcher.class);

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
                throw new Error("Dispatcher Already Started!");

            thread = new Thread(this);
            thread.start();
        }

        protected void doDispatcherLoop()
        {
            while (!mustStop)
            {
                if (Thread.interrupted())
                {
                    Thread.currentThread().interrupt(); // keep interupted
                                                        // status.
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
                        this.wait(1000);
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
            synchronized (this)
            {
                this.notifyAll();
            }
        }

        boolean isRunning()
        {
            if (thread == null)
                return false;

            if (thread.isAlive())
            {
                return true;
            }

            return false;
        }

    }

    protected Vector<EventT> events;

    protected Vector<EventListenerT> listeners;

    private Dispatcher dispatcher;

    public EventDispatcher()
    {
        initDispatcher();
    }

    private void initDispatcher()
    {
        this.dispatcher = new Dispatcher();
        dispatcher.start();
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

    public void addListener(EventListenerT listener)
    {
        this.listeners.add(listener);
    }

    public void removeListener(EventListenerT listener)
    {
        synchronized (events)
        {
            this.listeners.remove(listener);
        }
    }

    private EventT popEvent()
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
        return (this.events.size() > 0);
    }

    protected void handleEvent()
    {
        EventT event = this.popEvent();

        // Use Super type i.s.o generics:
        IEventListener array[];

        synchronized (listeners)
        {
            array = listeners.toArray(new IEventListener[0]);
        }

        for (IEventListener listener : array)
        {
            listener.notifyEvent(event);
        }

    }

    public void stop()
    {
        this.dispatcher.stop();
    }
}
