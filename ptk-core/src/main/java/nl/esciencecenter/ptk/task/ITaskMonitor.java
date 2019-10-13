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

import nl.esciencecenter.ptk.data.StringHolder;

/**
 * Interface for Action Tasks, or other objects, which can be monitored and provide statistics about
 * the progress.
 */
public interface ITaskMonitor {

    /**
     * Start new Task. Use logical taskName to distinguish between multiple tasks using the same
     * monitor. For nested tasks use startSubTask and endSubTask as main tasks may not be nested.
     *
     * @param taskName - New logical task name. Main tasks may not be nested.
     * @param numTodo  - estimated of number of steps to be done by this task.
     */
    void startTask(String taskName, long numTodo);

    /**
     * @return current main task name.
     */
    String getTaskName();

    void updateTaskDone(long numDone);

    TaskStats getTaskStats();

    /**
     * End current main taks. logical taskName must match name given at <code> startTask() </code>.
     *
     * @param taskName - logical taskName which has ended.
     */
    void endTask(String taskName);

    // === subtask ===

    void startSubTask(String subTaskName, long numTodo);

    String getCurrentSubTaskName();

    TaskStats getSubTaskStats(String subTaskName);

    void updateSubTaskDone(String subTaskName, long numDone);

    void endSubTask(String name);

    // === flow control ===

    boolean isDone();

    /**
     * Notify monitor the actual task has been cancelled or it is stop state.
     */
    void setIsCancelled();

    boolean isCancelled();

    // == timers/done ===

    long getStartTime();

    // === Logging/Etc ===

    void logPrintf(String format, Object... args);

    /**
     * Returns logging events into one text String. Set resetLogBuffer to true to reset the log
     * buffer so that each getLogTexT() will return the events since the last getLogText() call.
     * Specify log event offset in logEventOffset.
     *
     * @return returns current log event number.
     */
    int getLogText(boolean clearLogBuffer, int logEventOffset, StringHolder logTextHolder);

    /**
     * Has error/exception, etc.
     */
    boolean hasError();

    Throwable getException();

    void setException(Throwable t);


}
