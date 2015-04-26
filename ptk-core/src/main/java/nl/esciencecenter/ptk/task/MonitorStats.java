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

import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.task.ITaskMonitor.TaskStats;
import nl.esciencecenter.ptk.util.StringUtil;

/**
 * Class which calculates statistics from an ITaskMonitor object. Also provides some extra status to String methods like
 * estimated time of arrival (ETA).
 */
public class MonitorStats
{
    /** 
     * Monitor stats types the taskMonitor could provide. 
     */
    public enum MonitorStatsType
    {
        TOTAL_BYTES_TRANSFERRED,
        CURRENT_BYTES_TRANSFERRED,
        TOTAL_SOURCES_COPIED,
        TOTAL_SOURCES_DELETED
    };
    
    // ========================================================================
    //
    // ========================================================================

    protected ITaskMonitor monitor = null;

    Presentation presentation=new Presentation();

    public MonitorStats(ITaskMonitor monitor)
    {
        this.monitor = monitor;
    }

    public MonitorStats(ITaskMonitor monitor, Presentation presentation)
    {
        this.monitor = monitor;
        this.presentation=presentation; 
    }

    /**
     * @return total current running time or total time it took to finish the task in milliseconds. 
     */
    public long getTotalDoneTime()
    {
        if (monitor==null)
            return -1; 
        
        TaskStats stats = monitor.getTaskStats(); // may never be null
        
        if (stats.startTimeMillies<=0)
        {
            // Start not recorded! 
            return -1; 
        }
        
        if (monitor.isDone())
        {
            if ( (stats.stopTimeMillies<=0) || (stats.startTimeMillies<=0) )
            {
                // start or stop not recorded. 
                return -1; 
            }
            else
            {             
                long done= (stats.stopTimeMillies - stats.startTimeMillies);
                return done; 
            }
        }
        else
        {
            return System.currentTimeMillis() - stats.startTimeMillies;
        }
    }

    /**
     * Returns ETA in millis. Depends on totalTodo/totalDone to calculate ETA.
     * 
     * <pre>
     * -1 = no statistics
     *  0 = done 
     * >0 = estimated finishing time in milli seconds
     * </pre>
     * 
     * @return
     */
    public long getETA()
    {
        if (monitor==null)
            return -1; 
        
        if (monitor.isDone())
        {
            return 0; // done
        }
        return calcETA(monitor.getTaskStats().done, monitor.getTaskStats().todo, getTotalSpeed());
    }

    public String getStatusText()
    {
        if (monitor.isDone() == false)
        {
            String subStr = monitor.getCurrentSubTaskName();

            if (StringUtil.isEmpty(subStr))
            {
                return "Busy ...";
            }
            else
            {
                return subStr;
            }
        }

        if (monitor.hasError())
        {
            return "Error!";
        }

        return "Finished!";
    }

    /**
     * Return ETA in milli sconds.
     * 
     * @return the return value is -1 for unknown, 0 for done or 0> for actual estimated time in milli seconds.
     */
    public long calcETA(long done, long todo, double speed)
    {
        // no statistics !
        if (done < 0)
        {
            return -1; // unknown
        }
        // nr of bytes/ nr of total work todo
        long delta = todo - done;

        if (speed <= 0)
        {
            return -1; // unknown, prevent divide by zero;
        }
        // return ETA in millis !
        return (long) ((1000 * delta) / speed);
    }

    /**
     * Return speed total transfer amount of workdone/seconds for transfer this is bytes/second. If nr of bytes equals
     * amount of work done.
     */
    public double getTotalSpeed()
    {
        // time is in millis, total work amount in bytes (for transfers)
        double speed = ((double) monitor.getTaskStats().done) / (double) (getTotalDoneDeltaTime());

        // convert from work/millisecond to work/seconds
        speed = speed * 1000.0;

        return speed;
    }

    public double getTotalProgress()
    {
        if (this.monitor.getTaskStats().todo <= 0)
        {
            return 0;
        }

        return ((double) monitor.getTaskStats().done) / (double) monitor.getTaskStats().todo;
    }

    /**
     * Return delta time between last update time and the start time.<br>
     * The actual last update time must be used and not current time to prevent 'degrading' of performance time
     * between updates! This happens when the transfer is stalled but the time continues.
     */
    public long getTotalDoneDeltaTime()
    {
        TaskStats stats = monitor.getTaskStats();
        if (stats==null)
        {
            return -1; 
        }
        // no stats yet. 
        if ((stats.doneLastUpdateTimeMillies<=0) || (stats.startTimeMillies<=0)) 
        {
            return -1; 
        }
        
        return stats.doneLastUpdateTimeMillies - stats.startTimeMillies;
    }

    public long getTotalDoneLastUpdateTime()
    {
        return monitor.getTaskStats().doneLastUpdateTimeMillies;
    }

    /**
     * Return total Time Running in millies.
     */
    public long getTotalTimeRunning()
    {
        long time = System.currentTimeMillis() - monitor.getTaskStats().startTimeMillies;
        return time;
    }

    // =========================================================================
    // Sub Task Stats
    // =========================================================================

    public String getCurrentSubTaskName()
    {
        return monitor.getCurrentSubTaskName();
    }

    public double getSubTaskProgress(String subTaskName)
    {
        TaskStats stats = monitor.getSubTaskStats(subTaskName);

        if (stats == null)
            return 0;

        if (stats.todo <= 0)
        {
            return 0; // Double.NaN; // divide by null.
        }

        return ((double) stats.done / (double) stats.todo);
    }

    public long getSubTaskDone(String subTaskName)
    {
        TaskStats subTask = monitor.getSubTaskStats(subTaskName);
        if (subTask == null)
            return 0;
        return subTask.done;
    }

    public long getSubTaskTodo(String subTaskName)
    {
        TaskStats subTask = monitor.getSubTaskStats(subTaskName);
        if (subTask == null)
            return 0;
        return subTask.todo;
    }

    /**
     * Return speed total transfer amount of &lt;task steps&gt;/seconds For example for file transfers this is
     * bytes/second.
     */
    public double getSubTaskSpeed(String subTaskName)
    {
        TaskStats stats = monitor.getSubTaskStats(subTaskName);
        return calculateTaskSpeed(stats);
    }

    /**
     * Returns Task speed in &lt;task steps&gt;/second.
     * 
     * @param stats
     *            TaskStats to use.
     * @return - speed of current task. Unit depends on used Task Step scale.
     */
    public double calculateTaskSpeed(TaskStats stats)
    {
        if (stats == null)
        {
            return 0.0;
        }

        // Delta time is in millis, total work amount in bytes (for transfers)
        double subTime = (double) getTaskDoneDeltaTime(stats);
        double speed = 0;

        if (speed > 0)
        {
            speed = stats.done / subTime;
        }
        else if (speed < 0)
        {
            speed = 0; // unknown, speed = 0!
        }
        else
        {
            // avoid divide by zero!
            speed = stats.done; // assume minimum delta time of '1ms'.
        }

        // convert from work/millisecond to work/seconds (or bytes/seconds)
        speed = speed * 1000.0;

        return speed;
    }

    /**
     * Return milli seconds between sub task updates. Method returns -1 if not known ! Return value of 0 is possible if
     * delta time < 1ms.
     * 
     * @param subTaskName
     *            - logical task name to check.
     * @return time in milliseconds between updates of subTasks, or -1 if value or subTaskName not known.
     * @see #getSubTaskDoneDeltaTime(String)
     */
    public long getSubTaskDoneDeltaTime(String subTaskName)
    {
        return getTaskDoneDeltaTime(monitor.getSubTaskStats(subTaskName));
    }

    /**
     * Return milli seconds between sub task updates. Beware of 0 values. Method return -1 if not known !
     * 
     * @param stats
     *            - TaskStats to check.
     * @return time in milliseconds between updates or -1 if not known.
     */
    public long getTaskDoneDeltaTime(TaskStats stats)
    {
        if (stats == null)
            return -1;

        return stats.doneLastUpdateTimeMillies - stats.startTimeMillies;
    }

    public long getSubTaskETA(String subTaskName)
    {
        if (monitor.isDone())
            return 0; // done

        return calculateSubTaskETA(monitor.getSubTaskStats(subTaskName));
    }

    public long calculateSubTaskETA(TaskStats stats)
    {
        if (stats == null)
            return -1;

        return calcETA(stats.done,
                stats.todo,
                this.calculateTaskSpeed(stats));
    }

    /**
     * Return total time of specified sub task running in millies.
     */
    public long getSubTaskTimeRunning(String taskName)
    {
        TaskStats stats = monitor.getSubTaskStats(taskName);

        if (stats == null)
            return -1;

        long time = System.currentTimeMillis() - monitor.getTaskStats().startTimeMillies;
        return time;
    }

    /**
     * Produce Time String of current Subtask. Returns time running plus estimated time of arrival.
     * 
     * @return
     */
    public String getCurrentSubTaskTimeStatusText()
    {
        String subTaskName = getCurrentSubTaskName();
        TaskStats stats = monitor.getSubTaskStats(subTaskName);

        long subTime = getTaskDoneDeltaTime(stats);

        String timestr = Presentation.createRelativeTimeString(subTime, false);

        long eta = calculateSubTaskETA(stats);

        if (eta < 0)
            timestr += " (?)";
        else if (eta == 0)
            timestr += " (done)";
        else
            timestr += " (" + Presentation.createRelativeTimeString(eta, false) + ")";

        return timestr;
    }

    public boolean hasSubTask(String taskName)
    {
        return (monitor.getSubTaskStats(taskName)!=null); 
    }

    public boolean hasSubTask(MonitorStatsType taskType)
    {
        if (taskType==null)
        {
            return false; 
        }
        return (monitor.getSubTaskStats(""+taskType)!=null); 
    }
    
    public String createTotalBytesTransferredString()
    {
        TaskStats stats = monitor.getSubTaskStats(""+MonitorStatsType.TOTAL_BYTES_TRANSFERRED);
        return createBytesTransferredString(stats);
    }
    
    public String createCurrentBytesTransferredString()
    {
        TaskStats stats = monitor.getSubTaskStats(""+MonitorStatsType.CURRENT_BYTES_TRANSFERRED);
        return createBytesTransferredString(stats);
    }
    
    public String createBytesTransferredString(TaskStats stats)
    {
        String speedStr; 

        if (stats==null)
        {
            return "?";
        }
        
        String amountStr = sizeString(stats.done) + " (of " + sizeString(stats.todo) + ")";

        
        // done can be 0, but update time must be > start time (divide by zero error).
        if ((stats == null) || (stats.done<0) || (stats.doneLastUpdateTimeMillies<=stats.startTimeMillies)) 
        {
            speedStr="(?)KB/s";
        }
        else
        {
            speedStr=stats.done/(stats.doneLastUpdateTimeMillies-stats.startTimeMillies+1)+"KB/s"; 
        }
                
        return amountStr+" "+speedStr; 
    }   
    
    public String sizeString(long done)
    {
        return presentation.sizeString(done, true, 1, 2); 
    }

}
