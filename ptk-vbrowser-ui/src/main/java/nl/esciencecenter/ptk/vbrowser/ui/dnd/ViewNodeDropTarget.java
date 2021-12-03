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

package nl.esciencecenter.ptk.vbrowser.ui.dnd;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyNodeDnDHandler.DropAction;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeComponent;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeContainer;
import nl.esciencecenter.vbrowser.vrs.VRSTypes;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.util.List;

/**
 * ViewNodeDropTarget handler, handles 'drop' on ViewNode Components. Install this
 * DropTargetListener to support drops on the component.
 * <p>
 * Swing/AWT Compatible DnD Support.
 */
@Slf4j
public class ViewNodeDropTarget extends DropTarget implements DropTargetListener {
    public ViewNodeDropTarget(Component comp) {
        super(comp, DnDConstants.ACTION_LINK, null, true);
        this.setComponent(comp);
        // setDefaultActions(DnDConstants.ACTION_COPY);
    }

    protected ViewNode getTargetViewNode() {
        Component comp = getComponent();
        if (comp instanceof ViewNodeComponent) {
            return ((ViewNodeComponent) comp).getViewNode();
        }
        return null;
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        log.debug("dragEnter:{}", dtde);
        super.dragEnter(dtde);

        updateDropAction(dtde);
    }

    public void dragOver(DropTargetDragEvent dtde) {
        log.debug("dragOver:{}", dtde);
        super.dragOver(dtde);

        updateDropAction(dtde);
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
        log.debug("dropActionChanged:{}", dtde);
        super.dropActionChanged(dtde);
        // accept change ?
        updateDropAction(dtde);
    }

    public void dragExit(DropTargetEvent dte) {
        log.debug("dragExit:{}", dte);
        super.dragExit(dte);
    }

    // Actual Drop!
    public void drop(DropTargetDropEvent dtde) {
        super.clearAutoscroll();

        Component uiComp = dtde.getDropTargetContext().getComponent();
        Point p = dtde.getLocation();

        ViewNode targetNode = this.getViewNode(uiComp, p);

        if (targetNode == null) {
            log.error("drop(): No Target ViewNode for:{}!", dtde);
            dtde.rejectDrop();
        }

        // check actual DropEvent
        Component targetComponent = dtde.getDropTargetContext().getComponent();
        Transferable transferable = dtde.getTransferable();
        int sourceActions = dtde.getSourceActions();
        int userDropAction = dtde.getDropAction();
        int effectiveAction = effectiveDropAction(transferable, targetNode, sourceActions,
                userDropAction);

        boolean succes = false;

        if (effectiveAction > 0) {
            dtde.acceptDrop(effectiveAction);
            succes = DnDUtil.performAcceptedDrop(uiComp, p, targetNode, transferable,
                    DnDUtil.getDropAction(userDropAction), DnDUtil.getDropAction(effectiveAction));
        } else {
            log.debug("Effective ActionCmd=NONE for:{}", dtde);
        }

        dtde.getDropTargetContext().dropComplete(succes);
    }

    /**
     * Check what the effective DropAction would be for this event and update accept/reject drag.
     * Return effective actions if this would the final drop.
     *
     * @param dtde DropTargetDropEvent
     * @return Effective DropAction for this drop event.
     */
    protected DropAction updateDropAction(DropTargetDragEvent dtde) {

        // current actions:
        int userDropAction = dtde.getDropAction();
        // all actions allowed:
        int sourceActions = dtde.getSourceActions();

        log.debug("> updateAllowDrop(): source actions/drop action={}/{}",
                sourceActions, userDropAction);

        // {
        // Object source = dtde.getSource();
        // DragSourceContext dgctx=(DragSourceContext)source);
        // }

        ViewNode targetNode = getViewNode(dtde.getDropTargetContext().getComponent(),
                dtde.getLocation());

        Transferable transferable = dtde.getTransferable();

        if (targetNode != null) {
            // Match allowed drop actions with allowed source Actions and
            // 'current' user drop action.
            int effectiveAction = effectiveDropAction(transferable, targetNode, sourceActions,
                    userDropAction);

            if (effectiveAction > 0) {
                dtde.acceptDrag(effectiveAction);
                return DnDUtil.getDropAction(effectiveAction);
            } else {
                dtde.rejectDrag();
                return null;
            }
        } else {
            dtde.rejectDrag();
            return null;
        }
    }

    /**
     * A ViewNodeDropTarget must be installed on a ViewNode.
     */
    protected ViewNode getViewNode(Component targetComponent, Point p) {
        ViewNode viewNode = null;

        if (targetComponent instanceof ViewNodeComponent) {
            if (targetComponent instanceof ViewNodeContainer) {
                ViewNodeContainer container = (ViewNodeContainer) targetComponent;
                viewNode = container.getNodeUnderPoint(p);
            } else {
                viewNode = ((ViewNodeComponent) targetComponent).getViewNode();
            }
        }

        return viewNode;
    }

    /**
     * Match sourceActions with dropTarget action and current user (drop) action and return
     * effective allowed action.
     */
    protected int effectiveDropAction(Transferable transferable, ViewNode targetNode,
                                      int sourceActions, int userDropAction) {
        List<String> childTypes = targetNode.getAllowedChildTypes();

        int dropActions = matchDataTypeDropActions(transferable, childTypes);
        // source actions and drop actions must intersect.
        int mask = dropActions & sourceActions;

        if (mask > 0) {
            // default user action is 'move'.
            if ((mask & DnDConstants.ACTION_MOVE & userDropAction) > 0) {
                return DnDConstants.ACTION_MOVE;
            } else if ((mask & DnDConstants.ACTION_COPY & userDropAction) > 0) {
                return DnDConstants.ACTION_COPY;
            } else if ((mask & DnDConstants.ACTION_LINK) > 0) {
                return DnDConstants.ACTION_LINK;
            } else {
                return DnDConstants.ACTION_NONE;
            }
        }

        return DnDConstants.ACTION_NONE;
    }

    /**
     * Check match between transferable and (allowed) Resource childTypes.
     *
     * @param transferable - VRS Path or external object
     * @param childTypes   - allowed child types of current (ViewNode) drop target.
     * @return matching actions from DnDConstants
     * @see DnDConstants
     */
    public int matchDataTypeDropActions(Transferable transferable, List<String> childTypes) {
        DataFlavor[] sourceFlavors = transferable.getTransferDataFlavors();

        for (DataFlavor flav : sourceFlavors) {
            log.debug("matchDropActions() - sourceFlavor:{}", flav);
        }

        int matchingActions = DnDConstants.ACTION_NONE;

        boolean areVRLs = DnDData.canConvertToVRLs(transferable);
        boolean areVFSPaths = DnDData.canConvertToVFSPaths(transferable);

        log.debug("matchDropActions(): Dropped types are VFSPath ={}", areVFSPaths);
        log.debug("matchDropActions(): Dropped types are VRLs    ={}", areVRLs);

        // Allow Links to Any VRL type:
        if (childTypes.contains(VRSTypes.VLINK_TYPE) && areVRLs) {
            matchingActions |= DnDConstants.ACTION_LINK;
        }

        // Files: Drop on directory: Allow Copy, Move and (V)Link:
        if (areVFSPaths && childTypes.contains(VRSTypes.FILE_TYPE)) {
            matchingActions |= DnDConstants.ACTION_LINK | DnDConstants.ACTION_COPY_OR_MOVE;
        }

        return matchingActions;
    }

    public String toString() {
        return "ViewNodeDropTarget[viewNode=" + this.getTargetViewNode() + "]";
    }

}
