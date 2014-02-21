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

package nl.esciencecenter.ptk.vbrowser.ui.proxy.vrs;

import java.util.List;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.ptk.ui.panels.monitoring.TransferMonitorDialog;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeDnDHandler;
import nl.esciencecenter.vbrowser.vrs.task.VRSTaskMonitor;
import nl.esciencecenter.vbrowser.vrs.task.VRSTranferManager;
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
        else if (dropAction==DropAction.COPY || dropAction==DropAction.MOVE || dropAction==DropAction.COPY_PASTE || dropAction==DropAction.CUT_PASTE)
        {
            boolean isMove=( (dropAction==DropAction.MOVE) || (dropAction==DropAction.CUT_PASTE));
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
