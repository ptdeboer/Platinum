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

import java.util.Vector;

import nl.esciencecenter.ptk.util.logging.ClassLogger;

/**
 * ActionTask Watcher/Manager 
 */
public class TaskWatcher implements ITaskSource
{
    private static ClassLogger logger; 
    private static TaskWatcher instance=null;
    
    static
    {
        logger=ClassLogger.getLogger(TaskWatcher.class); 
    }
    
    // === //
    
    private String name; 

    private int maxTerminatedTasks=100; 
    
    protected Vector<ActionTask> activeTasks=new Vector<ActionTask>(); 

    protected Vector<ActionTask> terminatedTasks=new Vector<ActionTask>();

    public static ITaskSource getTaskWatcher()
    {
        if (instance==null)
            instance=new TaskWatcher("Global Taskwatcher"); 

        return instance; 
    }
    
    public TaskWatcher(String name)
    {
        this.name=name;
    }
    
    @Override 
    public String getTaskSourceName()
    {
        return name; 
    }
    
    @Override
    public void registerTask(ActionTask actionTask)
    {
        logger.debugPrintf("(+)RegisterTask:%s\n",actionTask); 
        synchronized(activeTasks)
        {
            this.activeTasks.add(actionTask); 
        }
    }
    
    @Override
    public void unregisterTask(ActionTask actionTask)
    {
        logger.debugPrintf("(-)RegisterTask:%s\n",actionTask);
        
        synchronized(activeTasks)
        {
            this.activeTasks.remove(actionTask); 
        }
        
        synchronized(terminatedTasks)
        {
            this.activeTasks.remove(actionTask); 
        }
    }
    
    @Override
    public void notifyTaskStarted(ActionTask actionTask) 
    {   
        logger.debugPrintf("(>)notifyTaskStarted:%s\n",actionTask);
        this.setHasActiveTasks(true);
    }

    @Override
    public void notifyTaskTerminated(ActionTask actionTask)
    {
        logger.debugPrintf("(*)notifyTaskTerminated:%s\n",actionTask);
        deschedule(actionTask);
        this.setHasActiveTasks(checkHasActiveTasks());
    }
    
    protected void deschedule(ActionTask actionTask)
    {
        synchronized(activeTasks)
        {
            boolean removed=this.activeTasks.remove(actionTask); 
            if (removed==false)
            {
                ; // already in terminatedTasks ? 
            }
        }
        
        synchronized(terminatedTasks)
        {
            this.terminatedTasks.add(actionTask); 
        }    
        synchronized(this.terminatedTasks)
        {
            if (this.terminatedTasks.size()>maxTerminatedTasks)
            {
                for (int i=0;i<maxTerminatedTasks;i++)
                    terminatedTasks.remove(0); // not efficient array remove.
            }
        }
        
        logger.infoPrintf("deschedule(): Number active/terminated tasks: %d/%d\n", activeTasks.size(),terminatedTasks.size()); 
    }
    
    public boolean checkHasActiveTasks()
    {
        int size=activeTasks.size(); 
        
        if (size>0)
        {
            int index=size-1; 
            
            while(index>=0)
            {
                // synchronize per element check inside while loop. 
                // Do no claim whole array during scan. 
                
                synchronized(activeTasks)
                {
                    // concurrent manipulation: size has already changed! 
                    if (index>=activeTasks.size())
                    {
                        
                    }
                    else if (activeTasks.get(index).isAlive()==false)
                    {
                        deschedule(activeTasks.get(index));
                    }
                }// exit sync!
                
                index--;
            }
        }
        
        synchronized(this.activeTasks)
        {
            if (this.activeTasks.size()>0)
                return true; 
        } 
                
        return false; 
    }
       
    /** 
     * This method checks whether the current execution Thread belongs to a Registered Action Task. 
     * 
     * @return - actual ActionTask linked to the current execution thread. 
     */
    public ActionTask getCurrentThreadActionTask()
    {
        return findActionTaskForThread(Thread.currentThread()); 
    }
    
    /**
     * Find ActionTask with specified thread id. 
     * Since all ActionTasks are currently started in their own thread, this 
     * method will find the actionTask currently executed in the specified thread. 
     * 
     * @param thread Thread which started an ActionTask 
     * @return ActionTaks or null which is started within to the specified Thread. 
     */
    public ActionTask findActionTaskForThread(Thread thread)
    {
        if (thread==null)
            return null; 
        
        ActionTask tasks[]=getActiveTaskArray();
        
        for (ActionTask task:tasks)
        {
            if ((task!=null) && (task.hasThread(thread)) )
                return task;
        }
         
        return null; 
    }
    
    /** 
     * Return a private copy of the task list, for thread safe operations.
     */
    protected final ActionTask[] getActiveTaskArray()
    {
        synchronized (activeTasks)
        {
            ActionTask tasks[]=new ActionTask[activeTasks.size()];
            tasks=activeTasks.toArray(tasks);
            return tasks;
        }
    }

    @Override
    public void notifyTaskException(ActionTask task,Throwable ex)
    {
        // Optional handling of an exception throw by an ActionTask. 
        // Ignore here. Subclasses might do something here. 

        logger.logException(ClassLogger.ERROR,task,ex,"TaskException for %s\n",task); 
    }
    
    /** 
     * Check whether there are active tasks running for the TaskSource 
     */ 
    public boolean hasActiveTasks(ITaskSource source)
    {
        logger.debugPrintf("hasActiveTasks() for:%s\n",source.getTaskSourceName());
        
        ActionTask tasks[]=getActiveTaskArray();
        
        if ((tasks==null) || (tasks.length<=0))
            return false; 
        
        boolean active=false;
         
        for (ActionTask task:tasks)
        {
            logger.debugPrintf("Checking action task:%s\n", task);
            if ((task.getTaskSource()!=null) && (task.getTaskSource()==source))
            {
                if (task.isAlive())
                    active=true;
            }
        }
        
        return active; 
    }

    public void setHasActiveTasks(boolean active)
    {
        logger.debugPrintf("(?)setHasActiveTasks:%s\n",active);
        // todo update TaksListeners. 
    }
    
    public void stopAllTasks()
    {
        ActionTask tasks[]=getActiveTaskArray();
        
        // send stop signal first: 
        for (ActionTask task:tasks)
        {
            task.signalTerminate(); 
        }
        
        try
        {
            Thread.sleep(50);
        }
        catch (InterruptedException e)
        {
            logger.logException(ClassLogger.ERROR,e,"***Error: Exception:"+e); 
        }         
        
        // now send interrupt:   
        for (ActionTask task:tasks)
        {
            // send intterupt. 
            if (task.isAlive())
                task.interruptAll();  
        }
    }
    
}
