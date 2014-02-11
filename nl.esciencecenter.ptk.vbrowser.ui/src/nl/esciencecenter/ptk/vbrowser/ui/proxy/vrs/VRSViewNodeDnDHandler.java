package nl.esciencecenter.ptk.vbrowser.ui.proxy.vrs;

import java.util.List;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeDnDHandler;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.io.VRSTransferManager;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VRSViewNodeDnDHandler extends ViewNodeDnDHandler
{
    protected VRSClient vrsClient; 
    
    public VRSViewNodeDnDHandler(VRSClient vrsClient)
    {
        this.vrsClient=vrsClient; 
    }

    public boolean doDrop(ViewNode targetDropNode, DropAction dropAction, List<VRL> vrls)
    {
        VRL destVrl=targetDropNode.getVRL(); 
        
        System.err.printf("VRSViewNodeDnDHandler::DROP:%s:on %s, list=%s\n",dropAction,targetDropNode,new ExtendedList<VRL>(vrls));
        
        VRSTransferManager vrsManager=vrsClient.getVRSTransferManager(); 
        
        if (dropAction==DropAction.LINK)
        {
            vrsManager.doLinkDrop(destVrl,vrls);
        }
        else if ((dropAction==DropAction.COPY) || (dropAction==DropAction.MOVE))
        {
            boolean isMove=(dropAction==DropAction.MOVE);
            vrsManager.doCopyMove(destVrl, vrls, isMove); 
        }
        else
        {
            System.err.printf("FIXME: VRSViewNodeDnDHandler unrecognized DROP:%s:on %s, list=%s\n",dropAction,targetDropNode,new ExtendedList<VRL>(vrls));
        }
        
        return true; 
    }
}
