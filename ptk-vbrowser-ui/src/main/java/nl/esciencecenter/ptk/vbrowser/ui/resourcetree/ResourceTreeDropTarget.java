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

package nl.esciencecenter.ptk.vbrowser.ui.resourcetree;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.ViewNodeDropTarget;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;

import java.awt.*;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

/**
 * A node in a JTree can't have DropTargets, so the Parent component (JTree) handles the drops.
 */
@Slf4j
public class ResourceTreeDropTarget extends ViewNodeDropTarget {

    public ResourceTreeDropTarget(ResourceTree tree) {
        super(tree);
    }

    public ResourceTree getResourceTree() {
        return (ResourceTree) this.getComponent();
    }

    /*
     * Override for ResourceTree, check which node is active under point 'p'.
     */
    @Override
    protected ViewNode getViewNode(Component targetComponent, Point p) {
        if ((targetComponent instanceof ResourceTree) == false) {
            log.error("drop():Source object not a ResourceTree!!!");
            return null;
        }

        ResourceTree tree = ((ResourceTree) targetComponent);
        ResourceTreeNode rtnode = tree.getRTNodeUnderPoint(p);
        ViewNode viewNode = null;

        if (rtnode != null) {
            viewNode = rtnode.getViewNode();
        }

        return viewNode;
    }

    public void drop(DropTargetDropEvent dtde) {
        super.drop(dtde);
    }

    public void dragOver(DropTargetDragEvent dtde) {
        // check/update ResourceTree paths:
        super.dragOver(dtde);
    }

}
