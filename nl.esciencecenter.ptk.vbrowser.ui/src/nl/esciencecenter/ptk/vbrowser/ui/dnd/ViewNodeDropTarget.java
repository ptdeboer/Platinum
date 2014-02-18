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

package nl.esciencecenter.ptk.vbrowser.ui.dnd;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.List;

import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeComponent;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeContainer;
import nl.esciencecenter.vbrowser.vrs.VRSTypes;

/**
 * ViewNodeDropTarget handler, handles 'drop' on ViewNode Components. Install
 * this DropTargetListener to support drops on the component.
 * 
 * Swing/AWT Compatible DnD Support.
 */
public class ViewNodeDropTarget extends DropTarget implements DropTargetListener // ,
// DragSourceListener,
{
    private static final long serialVersionUID = 1985854014807809151L;

    public ViewNodeDropTarget(Component comp)
    {
        super(comp, DnDConstants.ACTION_LINK, null, true);
        this.setComponent(comp);
        // setDefaultActions(DnDConstants.ACTION_COPY);
    }

    protected ViewNode getTargetViewNode()
    {
        Component comp = getComponent();
        if (comp instanceof ViewNodeComponent)
        {
            return ((ViewNodeComponent) comp).getViewNode();
        }
        return null;
    }

    public void dragEnter(DropTargetDragEvent dtde)
    {
        DnDUtil.debugPrintf("dragEnter:%s\n", dtde);
        super.dragEnter(dtde);

        updateAllowDrop(dtde);
    }

    public void dragOver(DropTargetDragEvent dtde)
    {
        DnDUtil.debugPrintf("dragOver:%s\n", dtde);
        super.dragOver(dtde);

        // dtde.acceptDrag (DnDConstants.ACTION_LINK);
        boolean accept = updateAllowDrop(dtde);

    }

    public void dropActionChanged(DropTargetDragEvent dtde)
    {
        DnDUtil.debugPrintf("dropActionChanged:%s\n", dtde);
        super.dropActionChanged(dtde);
        // accept change ?
        boolean accept = updateAllowDrop(dtde);
    }

    public void dragExit(DropTargetEvent dte)
    {
        DnDUtil.debugPrintf("dragExit:%s\n", dte);
        super.dragExit(dte);
    }

    // Actual Drop!
    public void drop(DropTargetDropEvent dtde)
    {
        super.clearAutoscroll();

        // boolean accept=updateAllowDrop(dtde);

        DnDUtil.doDrop(dtde);
    }

    private boolean updateAllowDrop(DropTargetDragEvent dtde)
    {
        Component targetComponent = dtde.getDropTargetContext().getComponent();

        // TransferHandler handler;
        // if (targetComponent instanceof JComponent)
        // {
        // JComponent jcomp = ((JComponent) targetComponent);
        // handler = jcomp.getTransferHandler();
        // }

        Transferable transferable = dtde.getTransferable();

        // handler.setDragImage();
        // As a ViewNodeDropTarget except only ViewNodeComponents!
        if (targetComponent instanceof ViewNodeComponent)
        {
            ViewNode targetNode;

            if (targetComponent instanceof ViewNodeContainer)
            {
                ViewNodeContainer container = (ViewNodeContainer) targetComponent;
                targetNode = container.getNodeUnderPoint(dtde.getLocation());
            }
            else
            {
                targetNode = ((ViewNodeComponent) targetComponent).getViewNode();
            }

            if (targetNode != null)
            {

                List<String> childTypes = targetNode.getAllowedChildTypes();

                int actions = matchFlavorsWithResourceTypes(transferable, childTypes);

                if (actions > 0)
                {
                    dtde.acceptDrag(actions);
                    return true;
                }
                else
                {
                    dtde.rejectDrag();
                    return false;
                }
            }
            else
            {
                dtde.rejectDrag();
                return false;
            }

            // Accept all DnD actions:
            // dtde.acceptDrag(dtde.getSourceActions());
            // showUnderDrag(true);
        }
        else
        {
            // DnDUtil.warnPrintf("Received drag for NON ViewNode component:%s\n",source);
            dtde.rejectDrag();
            return false;
        }
    }

    /**
     * Check match between transferable and (Allow) ChildTypes.
     * 
     * @param transferable
     * 
     * @param sourceFlavors
     *            - source flavors from drag source
     * @param childTypes
     *            - allowed child types of current (ViewNode) drop target.
     * 
     * @return matching actions from DnDConstants
     * 
     * @see DnDConstants
     */
    public int matchFlavorsWithResourceTypes(Transferable transferable, List<String> childTypes)
    {
        DataFlavor[] sourceFlavors = transferable.getTransferDataFlavors();

        // for (DataFlavor flav:sourceFlavors)
        // {
        // DnDUtil.errorPrintf("matchFlavorsWithResourceTypes(): - sourceFlavor:%s\n",flav);
        // }

        int matchingActions = DnDConstants.ACTION_NONE;

        boolean areVRLs = DnDData.canConvertToVRLs(transferable);
        boolean areVFSPaths = DnDData.canConvertToVFSPaths(transferable);

        for (DataFlavor flav : sourceFlavors)
        {
            String mimeType = flav.getMimeType();

            if (mimeType == null)
            {
                continue;
            }

            // Allow Links to Any VRL type:
            if (childTypes.contains(VRSTypes.VLINK_TYPE) && areVRLs)
            {
                matchingActions |= DnDConstants.ACTION_LINK;
            }

            // Files: Drop on directory: Allow Copy, Move and (V)Link:
            if (areVFSPaths && childTypes.contains(VRSTypes.DIR_TYPE))
            {
                matchingActions |= DnDConstants.ACTION_LINK | DnDConstants.ACTION_COPY_OR_MOVE;
            }

        }

        return matchingActions;
    }

    public String toString()
    {
        return "ViewNodeDropTarget[viewNode=" + this.getTargetViewNode() + "]";
    }

}
