package nl.esciencecenter.vbrowser.vrs.io;

import java.util.List;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VRSTransferManager
{
    private static final ClassLogger logger=ClassLogger.getLogger(VRSTransferManager.class); 
    
    protected VRSClient vrsClient; 
    
    public VRSTransferManager(VRSClient vrsClient)
    {
        this.vrsClient=vrsClient;
    }

    public void doLinkDrop(VRL destVrl, List<VRL> vrls)
    {
        logger.errorPrintf("doLinkDrop on:%s,  vrls=%s\n",destVrl,new ExtendedList<VRL>(vrls));
    }

    public void doCopyMove(VRL destVrl, List<VRL> vrls,boolean isMove)
    {
        logger.errorPrintf("doCopyMove(isMove=%s) on:%s, vrls=%s\n",isMove,destVrl,new ExtendedList<VRL>(vrls)); 
    }
    
}
