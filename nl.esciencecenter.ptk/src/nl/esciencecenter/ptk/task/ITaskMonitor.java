/*
 * Copyrighted 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").  
 * You may not use this file except in compliance with the License. 
 * For details, see the LICENCE.txt file location in the root directory of this 
 * distribution or obtain the Apache License at the following location: 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
 * ---
 */
// source: 

package nl.esciencecenter.ptk.task;

import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.events.IEventSource;

/** 
 * Interface for Action Tasks, or other objects,  which can be monitored and provide statistics about the progress. 
 *  
 */
public interface ITaskMonitor extends IEventSource 
{
	public static class TaskStats
	{
	    public String name=null; 
	    public long todo=-1;
	    public long done=-1;
        public long startTimeMillies=-1; 
        public long stopTimeMillies=-1;
        public long todoLastUpdateTimeMillies=-1; 
        public long doneLastUpdateTimeMillies=-1;
        public boolean isDone=false;
        
        protected TaskStats()
        {
        }
        
		public TaskStats(String taskName, 
				long taskTodo, 
				long taskDone,
                long taskStartTime,
                long taskEndTime,
                long todoUpdateTime,
                long doneUpdateTime)
		{
			this.name=taskName;
			this.todo=taskTodo;
			this.done=taskDone;
			this.startTimeMillies=taskStartTime; 
			this.stopTimeMillies=taskEndTime; 
			this.todoLastUpdateTimeMillies=todoUpdateTime;
			this.doneLastUpdateTimeMillies=doneUpdateTime; 
		}

        public TaskStats(String taskName, long todo)
        {
            this.name=taskName;
            this.todo=todo; 
        }

        public void markEnd()
        {
            isDone=true; 
            this.stopTimeMillies=System.currentTimeMillis(); 
            this.doneLastUpdateTimeMillies=System.currentTimeMillis(); 
            this.todoLastUpdateTimeMillies=System.currentTimeMillis(); 
            
            // update done ? 
            // this.done=todo; ? 
        }
        
        public void markStart()
        {
            isDone=false; 
            long time=System.currentTimeMillis(); 
            // init! 
            this.done=0;
            this.startTimeMillies=time; 
            this.todoLastUpdateTimeMillies=time;
            this.doneLastUpdateTimeMillies=time; 
        }

        public void updateDone(long numDone)
        {
            this.done=numDone;
            this.doneLastUpdateTimeMillies=System.currentTimeMillis(); 
        }
        
	}
	
	// ======================
	// ITaskMonitor interface 
	// ======================
	
	// Should be called be implementing monitor. Will be moved. 
	public void startTask(String taskName, long numTodo);

	public String getTaskName(); 
	
	public void updateTaskDone(long numDone);

	public TaskStats getTaskStats(); 

	public void endTask(String taskName);
    
	// === subtask === 

	public void startSubTask(String subTaskName, long numTodo);
    
	public String getCurrentSubTaskName();
    
	public TaskStats getSubTaskStats(String subTaskName); 
	
	public void updateSubTaskDone(String subTaskName,long numDone);
	
	public void endSubTask(String name);

	// === flow control === 
	
	boolean isDone(); 
	
	/** 
	 * Notify monitor the actual task has been cancelled or it is stop state. 
	 */ 
	public void setIsCancelled(); 
		
	public boolean isCancelled();

	// == timers/done === 
	
	public long getStartTime();

    // === Logging/Etc === 
    
	public void logPrintf(String format, Object... args);

    /**
     * Returns logging events into one text String.  
     * Set resetLogBuffer to true to reset the log buffer so that
     * each getLogTexT() will return the events since the last getLogText() call.
     * Specify log event offset in logEventOffset.
     * @return returns current log event number.  
     */ 
	public int getLogText(boolean clearLogBuffer,int logEventOffset,StringHolder logTextHolder);
    
    /** 
     * Has error/exception, etc. 
     */ 
	public boolean hasError();

	public Throwable getException();
    
	public void setException(Throwable t); 
	
}
