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

package nl.esciencecenter.ptk.task;

/**
 * Abstract Action Task.<br>
 * Manages a runnable task.
 */
public abstract class ActionTask implements Runnable
{

    // =========================================================================
    //
    // =========================================================================

    /** Owner of this Task */
    private ITaskSource taskSource;

    private String taskName;

    /**
     * Number of thread associated with this task.
     */
    private Thread threads[];

    private Object threadMutex = new Object();

    private ITaskMonitor taskMonitor = null;

    private boolean isCancelled = false;

    // === protected ===
    protected Throwable exceptions[];

    public ActionTask(ITaskSource taskWatcher, String taskName)
    {
        init(taskWatcher, taskName, new TaskMonitorAdaptor(taskName,1));
    }

    public ActionTask(ITaskSource taskWatcher, String taskName, ITaskMonitor monitor)
    {
        init(taskWatcher, taskName, monitor);
    }

    private void init(ITaskSource taskWatcher, String taskName, ITaskMonitor monitor)
    {
        this.taskSource = taskWatcher;
        this.taskName = taskName;
        // default monitor:
        if (monitor != null)
        {
            this.taskMonitor = monitor;
        }
        else
        {
            this.taskMonitor = new TaskMonitorAdaptor(taskName,1);
        }

        if (taskWatcher != null)
        {
            taskWatcher.registerTask(this);
        }
    }

    final public ITaskSource getTaskSource()
    {
        return this.taskSource;
    }

    protected void setTaskSource(ITaskSource newSource)
    {
        this.taskSource = newSource;
    }

    final public ITaskMonitor getMonitor()
    {
        return this.taskMonitor;
    }

    final public String getTaskName()
    {
        return this.taskName;
    }

    /**
     * Returns master thread
     */
    final public Thread getThread()
    {
        if (threads == null)
            return null;

        return this.threads[0];
    }

    public boolean hasThread(Thread thread)
    {
        if (this.threads == null)
        {
            return false;
        }

        synchronized (threadMutex)
        {
            for (Thread thr : threads)
            {
                if (thr == thread)
                {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Returns number of threads associated with this task.<br>
     * Number of threads equals the number of parallel invocations of this task.
     */
    final int getNumThreads()
    {
        if (this.threads == null)
        {
            return 0;
        }
        return this.threads.length;
    }

    /**
     * Returns thread of parallel task.
     * 
     * @param index
     *            number of task.
     * @return actual thread.
     */
    final public Thread getThread(int index)
    {
        if (threads == null)
        {
            return null;
        }

        return this.threads[index];
    }

    /**
     * Returns true if at least one thread is alive.
     */
    final public boolean isAlive()
    {
        if (threads == null)
            return false;

        synchronized (threadMutex)
        {
            for (Thread thread : threads)
            {
                if ((thread != null) && (thread.isAlive()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Start a single thread and run this task.
     */
    final public void startTask()
    {
        this.threads = new Thread[1];
        this.threads[0] = new Thread(this);
        this.threads[0].start(); // goto run()
    }

    /**
     * Start a daemon thread for this task which keeps running.
     */
    final public void startDaemonTask()
    {
        this.threads = new Thread[1];
        this.threads[0] = new Thread(this);
        this.threads[0].setDaemon(true);
        this.threads[0].start(); // goto run()
    }

    /**
     * Tries to join with all active threads.
     */
    final public boolean join() throws InterruptedException
    {
        if ((threads == null) || (threads.length <= 0))
        {
            return false;
        }

        boolean joined = false;
        for (Thread thread : threads)
        {
            if ((thread != null) && (thread.isAlive()))
            {
                thread.join();
                joined = true;
            }
        }

        return joined;
    }

    /**
     * Tries to join with active thread.
     */
    final public boolean join(int index) throws InterruptedException
    {
        if ((threads == null) || (threads[index] == null) || threads[index].isAlive() == false)
        {
            return false;
        }

        this.threads[index].join();

        return true;
    }

    /**
     * Tries to wait untill all threads have finished.
     */
    final public void waitForAll() throws InterruptedException
    {
        // will return when thread finishes or already has finished.
        waitForAll(0);
    }

    /**
     * Tries to wait untill all threads have finished or timeout (in milli seconds) has been reached.
     */
    final public void waitForAll(long timeOutInMillis) throws InterruptedException
    {
        joinAll(timeOutInMillis);
    }

    /**
     * Use the Thread.join() method to join with the running task thread. Will immediately return if thread already
     * finished.
     */
    final public void joinAll(long timeOutMillis) throws InterruptedException
    {
        Thread _threadz[];

        if ((threads == null) || (threads.length <= 0))
        {
            return;
        }

        synchronized (threads)
        {
            _threadz = threads;
        }

        for (Thread thread : _threadz)
        {
            if (thread != null)
            {
                thread.join(timeOutMillis);
            }
        }

    }

    /**
     * Invoke interrupt() to all threads. The default behavior for a thread is that if the interrupted() state is set,
     * the thread should stop executing and perform a graceful shutdown.
     * 
     * @See {@link Thread#isInterrupted()}
     * @See {@link Thread#interrupt()}
     * 
     */
    final public void interruptAll()
    {
        Thread _threadz[];

        if ((threads == null) || (threads.length <= 0))
        {
            return;
        }

        synchronized (threads)
        {
            _threadz = threads;
        }

        for (Thread thread : _threadz)
        {
            if (thread != null)
            {
                thread.interrupt();
            }
        }
    }

    final public boolean hasException()
    {
        return (getException() != null);
    }

    final public Throwable getException()
    {
        if (exceptions == null)
        {
            return null;
        }
        return exceptions[0];
    }

    final public Throwable[] getExceptions()
    {
        return exceptions;
    }

    final protected void setException(Throwable t)
    {
        setException(0, t);
    }

    final protected void setException(int index, Throwable t)
    {
        if (this.exceptions == null)
        {
            this.exceptions = new Throwable[getNumThreads()];
        }

        exceptions[index] = t;
    }

    @Override
    final public void run()
    {
        // === PRE ===

        if (this.taskSource != null)
        {
            this.taskSource.notifyTaskStarted(this);
        }

        // === TASK ===

        Throwable taskError = null;

        try
        {
            this.doTask();
        }
        catch (Throwable t)
        {
            // unhandled exception by doTask() !
            taskError = t;
        }
        finally
        {
            ;
        }

        // === POST ===

        // Exception:
        if (taskError != null)
        {
            this.setException(taskError);

            if (taskSource != null)
            {
                taskSource.notifyTaskException(this, taskError);
            }
        }

        // === Finalization ===

        // notify end of task:
        if (taskMonitor != null)
        {
            // update monitor if task hasn't done this!
            if (taskMonitor.isDone() == false)
            {
                taskMonitor.endTask(taskMonitor.getTaskName());

            }
        }

        if (this.taskSource != null)
        {
            this.taskSource.notifyTaskTerminated(this);
        }

        // do not wait for dispose but cleanup directly after execution
        clearThreads();
    }

    private void clearThreads()
    {
        if (threads == null)
        {
            return;
        }

        synchronized (threadMutex)
        {
            // trigger release resources held by threads:
            for (int i = 0; i < threads.length; i++)
                threads[i] = null; // nullify !

            this.threads = null;
        }
    }

    final public ITaskMonitor getTaskMonitor()
    {
        return this.taskMonitor;
    }

    final protected void dispose()
    {
        clearThreads();
    }

    /**
     * Set isCancelled() flag to true and interrupts all (waiting) threads.
     */
    final public void signalTerminate()
    {
        try
        {
            // I) Update state to cancelled
            this.isCancelled = true;
            // forward to monitor !
            if (this.taskMonitor != null)
            {
                this.taskMonitor.setIsCancelled();
            }

            try
            {
                // II) Call stop task for asynchronous callback.
                this.stopTask();
            }
            catch (Throwable t)
            {
                ;
            }

            // III) Send interrupt() to all running threads to wake them up.
            interruptAll();

        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    /**
     * Checks whether one of current running threads is in interrupted stated. <br
     * To check whether this task needs to stop, use isCancelled() since an isInterrupted() flag might be erased after
     * an actual interrupt() call. Also an task may continue after an interrupt(), but must stop after isCancelled().
     * 
     * @return true if the one of the current active thread is in interrupted state.
     */
    final public boolean isInterrupted()
    {
        if ((this.threads == null) || (threads.length <= 0))
        {
            return false;
        }

        for (Thread thread : threads)
        {
            if (thread.isInterrupted())
            {
                return true;
            }
        }

        return false;
    }

    final public boolean isCancelled()
    {
        return this.isCancelled;
    }

    /**
     * Sub class may signal this task should stop.
     */
    final protected void setIsCancelled()
    {
        this.isCancelled = true;
    }

    @Override
    public String toString()
    {
        String threadInfo = "";
        if (threads != null)
        {
            for (int i = 0; i < threads.length; i++)
            {
                threadInfo += "[" + threads[i].getId() + "]";
            }
        }

        return "ActionTask:[" + threadInfo + "]" + this.taskName;
    }

    // =======================================================================
    // Abstract Task interface;
    // =======================================================================

    /**
     * Main task to do. Sub classes need to implement this method.
     */
    abstract protected void doTask() throws Exception;

    /**
     * If possible, try to stop the running task when this method is called. This to encourage preemptive task
     * scheduling.<br>
     * To trigger the stopTask() call signalTerminate(). It is also recommended to check the isCancelled() method and
     * check the state of the running thread. This method provides an asynchronous callback method as an alternative to
     * isCancelled() which must be called explicitly by the implementing task.
     */
    abstract protected void stopTask() throws Exception;

}
