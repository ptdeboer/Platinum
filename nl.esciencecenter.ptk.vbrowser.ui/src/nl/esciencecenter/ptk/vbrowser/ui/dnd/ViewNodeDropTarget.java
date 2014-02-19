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
import java.awt.Point;
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
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeDnDHandler.DropAction;
import nl.esciencecenter.vbrowser.vrs.VRSTypes;

/**
 * ViewNodeDropTarget handler, handles 'drop' on ViewNode Components. Install
 * this DropTargetListener to support drops on the component.
 * 
 * Swing/AWT Compatible DnD Support.
 */
public class ViewNodeDropTarget extends DropTarget implements DropTargetListener
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

        updateDropAction(dtde);
    }

    public void dragOver(DropTargetDragEvent dtde)
    {
        DnDUtil.debugPrintf("dragOver:%s\n", dtde);
        super.dragOver(dtde);

        // dtde.acceptDrag (DnDConstants.ACTION_LINK);
        updateDropAction(dtde);

    }

    public void dropActionChanged(DropTargetDragEvent dtde)
    {
        DnDUtil.debugPrintf("dropActionChanged:%s\n", dtde);
        super.dropActionChanged(dtde);
        // accept change ?
        updateDropAction(dtde);
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

        Component uiComp = dtde.getDropTargetContext().getComponent();
        Point p = dtde.getLocation();

        ViewNode targetNode = this.getViewNode(uiComp, p);

        if (targetNode == null)
        {
            DnDUtil.errorPrintf("drop(): No Target ViewNode for:%s!\n", dtde);
            dtde.rejectDrop();
        }

        // check actual DropEvent
        Component targetComponent = dtde.getDropTargetContext().getComponent();
        Transferable transferable = dtde.getTransferable();
        int sourceActions = dtde.getSourceActions();
        int userDropAction = dtde.getDropAction();
        int effectiveAction = effectiveDropAction(transferable, targetNode, sourceActions, userDropAction);

        boolean succes = false;

        if (effectiveAction > 0)
        {
            dtde.acceptDrop(effectiveAction);
            succes = DnDUtil.performAcceptedDrop(uiComp, p, targetNode, transferable, DnDUtil.getDropAction(userDropAction),
                    DnDUtil.getDropAction(effectiveAction));
        }
        else
        {
            DnDUtil.debugPrintf("Effective Action=NONE for:%s\n",dtde);
        }
        
        dtde.getDropTargetContext().dropComplete(succes);
    }

    /**
     * Check what the effective DropAction would be for this event and update
     * accept/reject drag. Return effective actions if this would the final
     * drop.
     * 
     * @param dtde DropTargetDropEvent 
     * @return Effective DropAction for this drop event.
     */
    protected DropAction updateDropAction(DropTargetDragEvent dtde)
    {

        // current actions:
        int userDropAction = dtde.getDropAction();
        // all actions allowed:
        int sourceActions = dtde.getSourceActions();

        DnDUtil.debugPrintf("> updateAllowDrop(): source actions/drop action=%x/%x\n", sourceActions, userDropAction);

        // {
        // Object source = dtde.getSource();
        // DragSourceContext dgctx=(DragSourceContext)source);
        // }

        ViewNode targetNode = getViewNode(dtde.getDropTargetContext().getComponent(), dtde.getLocation());

        Transferable transferable = dtde.getTransferable();

        if (targetNode != null)
        {
            // Match allowed drop actions with allowed source Actions and
            // 'current' user drop action.
            int effectiveAction = effectiveDropAction(transferable, targetNode, sourceActions, userDropAction);

            if (effectiveAction > 0)
            {
                dtde.acceptDrag(effectiveAction);
                return DnDUtil.getDropAction(effectiveAction);
            }
            else
            {
                dtde.rejectDrag();
                return null;
            }
        }
        else
        {
            dtde.rejectDrag();
            return null;
        }
    }

    /** 
     * A ViewNodeDropTarget must be installed on a ViewNode. 
     */
    protected ViewNode getViewNode(Component targetComponent, Point p)
    {
        ViewNode viewNode = null;

        if (targetComponent instanceof ViewNodeComponent)
        {
            if (targetComponent instanceof ViewNodeContainer)
            {
                ViewNodeContainer container = (ViewNodeContainer) targetComponent;
                viewNode = container.getNodeUnderPoint(p);
            }
            else
            {
                viewNode = ((ViewNodeComponent) targetComponent).getViewNode();
            }
        }

        return viewNode;
    }

    /**
     * Match sourceActions with dropTarget action and current user (drop) action
     * and return effective allowed action.
     */
    protected int effectiveDropAction(Transferable transferable, ViewNode targetNode, int sourceActions, int userDropAction)
    {
        List<String> childTypes = targetNode.getAllowedChildTypes();

        int dropActions = matchDataTypeDropActions(transferable, childTypes);
        // source actions and drop actions must intersect.
        int mask = dropActions & sourceActions;

        if (mask > 0)
        {
            // default user action is 'move'.
            if ((mask & DnDConstants.ACTION_MOVE & userDropAction) > 0)
            {
                return DnDConstants.ACTION_MOVE;
            }
            else if ((mask & DnDConstants.ACTION_COPY & userDropAction) > 0)
            {
                return DnDConstants.ACTION_COPY;
            }
            else if ((mask & DnDConstants.ACTION_LINK) > 0)
            {
                return DnDConstants.ACTION_LINK;
            }
            else
            {
                return DnDConstants.ACTION_NONE;
            }
        }

        return DnDConstants.ACTION_NONE;
    }

    /**
     * Check match between transferable and (allowed) Resource childTypes.
     * 
     * @param transferable
     *            - VRS Path or external object
     * 
     * @param childTypes
     *            - allowed child types of current (ViewNode) drop target.
     * 
     * @return matching actions from DnDConstants
     * 
     * @see DnDConstants
     */
    public int matchDataTypeDropActions(Transferable transferable, List<String> childTypes)
    {
        DataFlavor[] sourceFlavors = transferable.getTransferDataFlavors();

        for (DataFlavor flav : sourceFlavors)
        {
            DnDUtil.debugPrintf("matchDropActions() - sourceFlavor:%s\n", flav);
        }

        int matchingActions = DnDConstants.ACTION_NONE;

        boolean areVRLs = DnDData.canConvertToVRLs(transferable);
        boolean areVFSPaths = DnDData.canConvertToVFSPaths(transferable);

        DnDUtil.debugPrintf("matchDropActions(): Dropped types are VFSPath =%s\n", areVFSPaths);
        DnDUtil.debugPrintf("matchDropActions(): Dropped types are VRLs    =%s\n", areVRLs);

        // Allow Links to Any VRL type:
        if (childTypes.contains(VRSTypes.VLINK_TYPE) && areVRLs)
        {
            matchingActions |= DnDConstants.ACTION_LINK;
        }

        // Files: Drop on directory: Allow Copy, Move and (V)Link:
        if (areVFSPaths && childTypes.contains(VRSTypes.FILE_TYPE))
        {
            matchingActions |= DnDConstants.ACTION_LINK | DnDConstants.ACTION_COPY_OR_MOVE;
        }

        return matchingActions;
    }

    public String toString()
    {
        return "ViewNodeDropTarget[viewNode=" + this.getTargetViewNode() + "]";
    }

}
