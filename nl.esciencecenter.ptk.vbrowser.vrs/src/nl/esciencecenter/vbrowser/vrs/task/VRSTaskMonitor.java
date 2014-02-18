package nl.esciencecenter.vbrowser.vrs.task;

import java.util.List;

import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.task.TransferMonitor;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import nl.esciencecenter.vbrowser.vrs.vrl.VRLUtil;

/**
 * VRSTransfer monitor class. 
 * 
 * Monitor object for ongoing VRS Actions and Transfers. 
 * 
 * @author P.T. de Boer
 */
public class VRSTaskMonitor extends TransferMonitor
{    
    // ========================================================================
    // instance
    // ========================================================================

    private VRSActionType actionType=VRSActionType.UNKNOWN; 
    
    // instance methods
    protected VRSTaskMonitor(ITaskMonitor parentMonitor, VRSActionType vrsAction, String resourceType,List<VRL> sources, VRL destination)
    {
        super((vrsAction!=null)?vrsAction.toString():"VRSTransfer", VRLUtil.toURIs(sources), destination.toURINoException());
        setParent(parentMonitor); // add this transfer to parent monitor
    }
    
    // instance methods
    public VRSTaskMonitor(VRSActionType vrsAction, List<VRL> sources, VRL destination) 
    {
        super((vrsAction!=null)?vrsAction.toString():"VRSTransfer", VRLUtil.toURIs(sources), destination.toURINoException());
    }
    
    public VRSActionType getTaskType()
    {
        return actionType; 
    }
    
    /**
     * @return Returns the current source as VRL.
     */
    public VRL getCurrentSource()
    {
        java.net.URI uri= super.getSource();
        
        if (uri==null)
            return null;
        
        return new VRL(uri); 
    }

    
    // =======================================================================
    // Misc  
    // =======================================================================
    
        
}

