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

package nl.esciencecenter.ptk.vbrowser.ui.tree;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDropEvent;

import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.DnDData;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.DnDUtil;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.ViewNodeDropTarget;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;

/**
 * Swing/AWT Compatible DnD Support.
 * 
 * Node in a JTree can't have DropTarget's. So the Parent component (JTree)
 * handle the drops.
 * 
 * @author P.T. de Boer.
 */
public class ResourceTreeDropTarget extends ViewNodeDropTarget
{
    private static final long serialVersionUID = -9095804562165852802L;

    private static ClassLogger logger = ClassLogger.getLogger(ResourceTreeDropTarget.class);

    // === //

    public ResourceTreeDropTarget(ResourceTree tree)
    {
        super(tree);
    }

    public ResourceTree getResourceTree()
    {
        return (ResourceTree)this.getComponent(); 
    }
    
    /** 
     * Drop On Node ! 
     */
    public void drop(DropTargetDropEvent dtde)
    {
        // todo: check super: drop() !
        // Global.debugPrintln(this,"Dropping:"+dtde);

        DropTargetContext dtc = dtde.getDropTargetContext();
        Component comp = dtc.getComponent();
        Point p = dtde.getLocation();
        Transferable data = dtde.getTransferable();
        int dndAction=dtde.getDropAction(); 
        
        if ((comp instanceof ResourceTree) == false)
        {
            logger.errorPrintf("drop():Source object not a ResourceTree!!!\n");
            dtde.rejectDrop();
            return;
        }
        
        ResourceTree tree = ((ResourceTree) comp);
        ResourceTreeNode rtnode = tree.getRTNodeUnderPoint(p);
        ViewNode viewNode=null;
        
        if (rtnode!=null)
        {
            viewNode=rtnode.getViewNode(); 
        }
        
        // check dropped data:
        if (DnDData.canConvertToVRLs(data))
        {
            // I: accept drop:
            dtde.acceptDrop(dndAction);
 
            //dtde points into ResourceTree: provide JTree as GUI Component here: 
            boolean succes = DnDUtil.handleActualDrop(getResourceTree(), dtde.getLocation(),viewNode, data,DnDUtil.getDropAction(dndAction));

            // supply ResourceTree as (J)Component but TreeNode as VComponent !
            // DnDTransferHandler.getDefault().interActiveDrop(comp,rtnode,p,data);
            // III: complete the drag !
            if (succes)
            {
            	dtde.getDropTargetContext().dropComplete(true);
            }
            else
            {
                dtde.getDropTargetContext().dropComplete(false); 
            }
        }
        else
        {
            dtde.rejectDrop();
        }
    }

	

}
