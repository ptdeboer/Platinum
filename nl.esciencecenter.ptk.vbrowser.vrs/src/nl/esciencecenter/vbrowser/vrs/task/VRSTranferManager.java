package nl.esciencecenter.vbrowser.vrs.task;

import java.util.List;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.ptk.task.ActionTask;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VRSTranferManager
{
    private static final ClassLogger logger=ClassLogger.getLogger(VRSTranferManager.class); 
    
    protected VRSClient vrsClient; 
    
    protected VRSTaskWatcher taskWatcher;  
    
    public VRSTranferManager(VRSClient vrsClient)
    {
        this.vrsClient=vrsClient;
        // Use static instance for now: 
        this.taskWatcher=VRSTaskWatcher.getTaskWatcher(); 
    }
    
    public VRSTaskMonitor doLinkDrop(final List<VRL> vrls,final VRL destVrl)
    {
        final VRSTaskMonitor monitor=new VRSTaskMonitor(VRSActionType.LINK,vrls,destVrl);
        
        ActionTask task=new ActionTask(taskWatcher,"Link drop on:"+destVrl,monitor) {

            @Override
            protected void doTask() throws Exception
            {
                doLinkDrop(vrls,destVrl,monitor); 
            }

            @Override
            protected void stopTask() throws Exception
            {
                // set canceled flag. 
                monitor.setIsCancelled(); 
            } 
            
        };
        
        task.startTask(); 
        
        return monitor;  
    }

    public VRSTaskMonitor doCopyMove(final List<VRL> vrls,final VRL destVrl, final boolean isMove)
    {   
        VRSActionType type=isMove?VRSActionType.MOVE:VRSActionType.COPY; 
        final VRSTaskMonitor monitor=new VRSTaskMonitor(type,vrls,destVrl);
        
        ActionTask task=new ActionTask(taskWatcher,"VRS "+type+"to:"+destVrl,monitor) {

            @Override
            protected void doTask() throws Exception
            {
                doCopyMove(vrls,destVrl,isMove,monitor); 
            }

            @Override
            protected void stopTask() throws Exception
            {
                monitor.setIsCancelled(); 
            } 
            
        };
        
        task.startTask(); 
        
        return monitor;  
    }
    
    // === 
    // Implementation
    // ===
    
    protected void doLinkDrop(List<VRL> vrls,VRL destVrl, VRSTaskMonitor monitor)
    {
        logger.errorPrintf("***FIXME:doLinkDrop():%s, vrls=%s\n",destVrl,new ExtendedList<VRL>(vrls));
        
        String taskName="LinkDrop"; 
        monitor.startTask(taskName, vrls.size());
        monitor.logPrintf(">>> doLinkDrop on:%s,  vrls=%s\n",destVrl,new ExtendedList<VRL>(vrls));
        
        int index=0; 
        
        for (VRL vrl:vrls)
        {
            monitor.logPrintf(" - linkDrop %s\n",vrl);
            monitor.updateTaskDone(++index);
            try
            {
                Thread.sleep(250);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        monitor.endTask(taskName);
    }

    protected void doCopyMove(List<VRL> vrls, VRL destVrl, boolean isMove,VRSTaskMonitor monitor) throws VrsException
    {
        logger.errorPrintf("***FIXME:doCopyMove():%s, vrls=%s\n",destVrl,new ExtendedList<VRL>(vrls));
        
        String taskName=isMove?"Move":"Copy"; 
        monitor.startTask(taskName, vrls.size());
        monitor.logPrintf(">>> doCopyMove(isMove=%s) on:%s, vrls=%s\n",isMove,destVrl,new ExtendedList<VRL>(vrls));
        int index=0; 
        
        // VPath destPath = vrsClient.openLocation(destVrl); 
        // List<VPath> paths=vrsClient.openLocations(vrls); 
        
        for (VRL vrl:vrls)
        {
            monitor.logPrintf(" - %s: %s\n",taskName,vrl); 
            monitor.updateTaskDone(++index);
            try
            {
                Thread.sleep(250);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        monitor.endTask(taskName);
    }


}
