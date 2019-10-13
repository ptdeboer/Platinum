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

import java.util.Vector;

/**
 * ActionTask Watcher/Manager for ActionTasks.
 */
@Slf4j
public class TaskWatcher implements ITaskSource {

    private static TaskWatcher instance = null;

    // === //

    private String name;

    private int maxTerminatedTasks = 100;

    protected Vector<ActionTask> activeTasks = new Vector<ActionTask>();

    protected Vector<ActionTask> terminatedTasks = new Vector<ActionTask>();

    public static ITaskSource getTaskWatcher() {
        if (instance == null) {
            instance = new TaskWatcher("Global Taskwatcher");
        }

        return instance;
    }

    public TaskWatcher(String name) {
        this.name = name;
    }

    @Override
    public String getTaskSourceName() {
        return name;
    }

    @Override
    public void registerTask(ActionTask actionTask) {
        log.debug("(+)registerTask:{}", actionTask);
        synchronized (activeTasks) {
            this.activeTasks.add(actionTask);
        }
    }

    @Override
    public void unregisterTask(ActionTask actionTask) {
        log.debug("[-]unregisterTask:{}", actionTask);

        synchronized (activeTasks) {
            this.activeTasks.remove(actionTask);
        }

        synchronized (terminatedTasks) {
            this.activeTasks.remove(actionTask);
        }
    }

    @Override
    public void notifyTaskStarted(ActionTask actionTask) {
        log.debug("[>]notifyTaskStarted:{}", actionTask);
        this.setHasActiveTasks(true);
    }

    @Override
    public void notifyTaskTerminated(ActionTask actionTask) {
        log.debug("[*]notifyTaskTerminated:{}", actionTask);
        deschedule(actionTask);
        this.setHasActiveTasks(checkHasActiveTasks());
    }

    protected void deschedule(ActionTask actionTask) {
        synchronized (activeTasks) {
            boolean removed = this.activeTasks.remove(actionTask);
            if (removed == false) {
                // already in terminatedTasks ?
            }
        }

        synchronized (terminatedTasks) {
            this.terminatedTasks.add(actionTask);
        }

        synchronized (this.terminatedTasks) {
            if (this.terminatedTasks.size() > maxTerminatedTasks) {
                for (int i = 0; (i < maxTerminatedTasks) && (terminatedTasks.size() > 0); i++) {
                    terminatedTasks.remove(0); // not efficient array remove.
                }
            }
        }

        log.debug("deschedule(): Number active/terminated tasks: {}/{}", activeTasks.size(), terminatedTasks.size());
    }

    public boolean checkHasActiveTasks() {
        int size = activeTasks.size();

        if (size > 0) {
            int index = size - 1;

            while (index >= 0) {
                // synchronize per element check inside while loop.
                // Do no claim whole array during scan.

                synchronized (activeTasks) {
                    // concurrent manipulation: size has already changed!
                    if (index >= activeTasks.size()) {

                    } else if (activeTasks.get(index).isAlive() == false) {
                        deschedule(activeTasks.get(index));
                    }
                }// exit sync!

                index--;
            }
        }

        synchronized (this.activeTasks) {
            if (this.activeTasks.size() > 0)
                return true;
        }

        return false;
    }

    /**
     * This method checks whether the current execution Thread belongs to a Registered Action Task.
     *
     * @return - actual ActionTask linked to the current execution thread.
     */
    public ActionTask getCurrentThreadActionTask() {
        return findActionTaskForThread(Thread.currentThread());
    }

    /**
     * Find ActionTask with specified thread id. Since all ActionTasks are currently started in
     * their own thread, this method will find the actionTask currently executed in the specified
     * thread.
     *
     * @param thread Thread which started an ActionTask
     * @return ActionTaks or null which is started within to the specified Thread.
     */
    public ActionTask findActionTaskForThread(Thread thread) {
        if (thread == null)
            return null;

        ActionTask[] tasks = getActiveTaskArray();

        for (ActionTask task : tasks) {
            if ((task != null) && (task.hasThread(thread)))
                return task;
        }

        return null;
    }

    /**
     * Return a private copy of the task list, for thread safe operations.
     */
    protected final ActionTask[] getActiveTaskArray() {
        synchronized (activeTasks) {
            ActionTask[] tasks = new ActionTask[activeTasks.size()];
            tasks = activeTasks.toArray(tasks);
            return tasks;
        }
    }

    @Override
    public void notifyTaskException(ActionTask task, Throwable ex) {
        // Optional handling of an exception throw by an ActionTask.
        // Ignore here. Subclasses might do something here.
        log.error("Task Exception for task:{}", task);
        log.error("Exception:" + ex.getMessage(), ex);
    }

    /**
     * Check whether there are active tasks running for the TaskSource
     */
    public boolean hasActiveTasks(ITaskSource source) {
        log.debug("[?]hasActiveTasks() for:{}", source.getTaskSourceName());

        ActionTask[] tasks = getActiveTaskArray();

        if ((tasks == null) || (tasks.length <= 0))
            return false;

        boolean active = false;

        for (ActionTask task : tasks) {
            log.debug("Checking action task:{}", task);
            if ((task.getTaskSource() != null) && (task.getTaskSource() == source)) {
                if (task.isAlive())
                    active = true;
            }
        }

        return active;
    }

    public void setHasActiveTasks(boolean active) {
        log.debug("[>]setHasActiveTasks:{}", active);
        // todo update TaksListeners.
    }

    public void stopAllTasks() {
        ActionTask[] tasks = getActiveTaskArray();

        // send stop signal first:
        for (ActionTask task : tasks) {
            task.signalTerminate();
        }

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            log.error("Interrupted:" + e.getMessage(), e);
        }

        // now send interrupt:
        for (ActionTask task : tasks) {
            // send intterupt.
            if (task.isAlive())
                task.interruptAll();
        }
    }

}
