package nl.esciencecenter.vbrowser.vrs.task;

import nl.esciencecenter.ptk.task.TaskWatcher;
import nl.esciencecenter.ptk.util.logging.ClassLogger;

/** 
 * ActionTask Watcher for VRSTasks. 
 *  
 */
public class VRSTaskWatcher extends TaskWatcher
{
    private static ClassLogger logger;
    
    private static VRSTaskWatcher instance=null;
    
    static
    {
        logger=ClassLogger.getLogger(TaskWatcher.class); 
    }
    
    public static VRSTaskWatcher getTaskWatcher()
    {
        if (instance==null)
        {
            instance=new VRSTaskWatcher("VRSTaskWatcher"); 
        }
        
        return instance; 
    }
    
    public VRSTaskWatcher(String name)
    {   
        super(name); 
    }
    
    
}
