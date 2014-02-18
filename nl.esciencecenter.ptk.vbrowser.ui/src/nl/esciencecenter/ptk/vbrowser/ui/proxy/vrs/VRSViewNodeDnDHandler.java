package nl.esciencecenter.ptk.vbrowser.ui.proxy.vrs;

import java.util.List;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.ui.panels.monitoring.TransferMonitorDialog;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeDnDHandler;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.task.VRSTranferManager;
import nl.esciencecenter.vbrowser.vrs.task.VRSTaskMonitor;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VRSViewNodeDnDHandler extends ViewNodeDnDHandler
{
    protected VRSTranferManager vrsManager; 
    
    public VRSViewNodeDnDHandler(VRSTranferManager vrsTaskManager)
    {
        vrsManager=vrsTaskManager; 
    }

    public boolean doDrop(ViewNode targetDropNode, DropAction dropAction, List<VRL> vrls)
    {
        VRSTaskMonitor monitor;
        
        VRL destVrl=targetDropNode.getVRL(); 
                
        if (dropAction==DropAction.LINK)
        {
            monitor = vrsManager.doLinkDrop(vrls,destVrl);
        }
        else if ((dropAction==DropAction.COPY) || (dropAction==DropAction.MOVE) || dropAction==DropAction.COPY_PASTE )
        {
            boolean isMove=(dropAction==DropAction.MOVE);
            monitor=vrsManager.doCopyMove(vrls,destVrl, isMove); 
        }
        else
        {
            System.err.printf("FIXME: VRSViewNodeDnDHandler unrecognized DROP:%s:on %s, list=%s\n",dropAction,targetDropNode,new ExtendedList<VRL>(vrls));
            return false; 
        }
        
        TransferMonitorDialog.showTransferDialog(monitor, 0); 
        
        return true; 
    }
}
