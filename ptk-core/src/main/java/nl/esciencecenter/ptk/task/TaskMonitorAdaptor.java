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

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.data.StringHolder;

import java.util.Hashtable;
import java.util.Map;

/**
 * Default Adaptor for ITaskMonitor interface.
 */
@Slf4j
public class TaskMonitorAdaptor implements ITaskMonitor {

    private static int instanceCounter = 0;

    // === //

    private int id = 0;

    protected TaskStats taskStats = new TaskStats();

    protected Map<String, TaskStats> subTaskStats = new Hashtable<String, TaskStats>();

    protected boolean isCancelled = false;

    protected String currentSubTaskName = null;

    protected ITaskMonitor parent;

    // === privates ===

    private final Object waitMutex = new Object();

    private Throwable exception = null;

    private LogBuffer taskLogger = null;

    public TaskMonitorAdaptor() {
        init();
    }

    public TaskMonitorAdaptor(String taskName, long todo) {
        init();
        startTask(taskName, todo);
    }

    private void init() {
        this.taskLogger = new LogBuffer();
        id = instanceCounter++;
    }

    public String getId() {
        return "" + id;
    }

    /**
     * Optional Parent
     */
    public void setParent(ITaskMonitor parent) {
        this.parent = parent;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void updateSubTaskDone(String taskName, long done) {
        if (taskName == null)
            return;
        TaskStats subTask = this.subTaskStats.get(taskName);
        if (subTask == null) {
            return;
        }

        subTask.updateDone(done);
    }

    protected TaskStats createSubTask(String name, long todo) {
        TaskStats subTask = new TaskStats(name, todo);
        subTaskStats.put(name, subTask);
        return subTask;
    }

    @Override
    public void startSubTask(String taskName, long todo) {
        this.currentSubTaskName = taskName;
        TaskStats subTask = this.subTaskStats.get(taskName);
        if (subTask == null) {
            subTask = createSubTask(taskName, todo);
        }
        subTask.markStart();
    }

    @Override
    public void endSubTask(String taskName) {
        if (taskName == null)
            return;
        TaskStats subTask = this.subTaskStats.get(taskName);
        if (subTask == null)
            return;

        subTask.markEnd();
    }

    @Override
    public void startTask(String taskName, long numTodo) {
        this.taskStats.name = taskName;
        this.taskStats.todo = numTodo;
        this.taskStats.markStart();
    }

    @Override
    public void updateTaskDone(long numDone) {
        this.taskStats.updateDone(numDone);
    }

    @Override
    public void endTask(String name) {
        this.taskStats.markEnd();
        this.wakeAll();
    }

    protected void wakeAll() {
        synchronized (waitMutex) {
            this.waitMutex.notifyAll();
        }
    }

    public void waitForCompletion() throws InterruptedException {
        waitForCompletion(0);
    }

    /**
     * This method will block until the setDone() method is called or the specified time has passed.
     * This method is simalar to: {@link java.lang.Object#wait(long)}
     */
    public void waitForCompletion(int timeout) throws InterruptedException {
        if (isDone())
            return;

        synchronized (waitMutex) {
            try {
                waitMutex.wait(timeout);
            } catch (InterruptedException e) {
                throw e;
                // could start wait cycle again, but reason of interrupt is
                // unknown here.
                // Typically isInterrupted means stop what you are doing.
            }
        }
    }

    @Override
    public boolean isDone() {
        if (taskStats.isDone)
            this.wakeAll(); // extra wakeup incase previous was missed! (Rare)
        return taskStats.isDone;
    }

    @Override
    public void setIsCancelled() {
        this.isCancelled = true;
    }

    /**
     * Add informative text (for end user).
     */
    public void logPrintf(String format, Object... args) {
        // Do not synchronize here: it might deadlock threads.
        {
            // sloppy code!
            if (format == null) {
                taskLogger.record("logPrintf: NULL format", true);
            } else {
                taskLogger.record(String.format(format, args), false);
            }
        }
    }

//    /**
//     * Add informative text (for end user).
//     */
//    public void log(String format, Object... args) {
//        log.error("FIXME: LOG:"+format,args);
//    }

    @Override
    public int getLogText(boolean clearLogBuffer, int logEventOffset, StringHolder logTextHolder) {
        String text = this.taskLogger.getText(logEventOffset);
        if (clearLogBuffer) {
            this.taskLogger.clear();
        }
        logTextHolder.set(text);
        return taskLogger.size();
    }

    @Override
    public TaskStats getTaskStats() {
        return this.taskStats;
    }

    @Override
    public TaskStats getSubTaskStats(String name) {
        if (name == null)
            return null;
        return this.subTaskStats.get(name);
    }

    public long getStartTime() {
        return taskStats.startTimeMillies;
    }

    public long getStopTime() {
        return this.taskStats.stopTimeMillies;
    }

    @Override
    public String getTaskName() {
        return this.taskStats.name;
    }

    @Override
    public String getCurrentSubTaskName() {
        return this.currentSubTaskName;
    }

    @Override
    public boolean hasError() {
        return (this.exception != null);
    }

    @Override
    public Throwable getException() {
        return this.exception;
    }

    @Override
    public void setException(Throwable tr) {
        this.exception = tr;
    }
}
