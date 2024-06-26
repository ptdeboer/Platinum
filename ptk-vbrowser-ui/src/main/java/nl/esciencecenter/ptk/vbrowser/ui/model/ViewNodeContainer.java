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

package nl.esciencecenter.ptk.vbrowser.ui.model;

import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Interface for any (J)Component which can contain ViewNodes. A ViewNodeContainer in itself is also
 * a ViewNodeComponent.
 */
public interface ViewNodeContainer extends ViewNodeComponent {

    // === Hierarchy === //
    BrowserInterface getBrowserInterface();

    /**
     * Return managed JComponent, at this moment this is always the ViewNodeContainer itself.
     *
     * @return type safe JComponent
     */
    JComponent getJComponent();

    // === //

    ViewNode getNodeUnderPoint(Point p);

    /**
     * Create Pop-up menu when (right-)clicked the specified actionSourceNode.
     *
     * @param actionSourceNode ViewNode the click occurred.
     * @param canvasMenu       - whether this is a click the canvas (white space between the icons).
     * @return JPopupMenu
     */
    JPopupMenu createNodeActionMenuFor(ViewNode actionSourceNode, boolean canvasMenu);

    // === Selection Model === //

    void clearNodeSelection();

    List<ViewNode> getNodeSelection();

    /**
     * Toggle selection
     */
    void setNodeSelection(ViewNode node, boolean isSelected);

    /**
     * Toggle selection of range
     */
    void setNodeSelectionRange(ViewNode firstNode, ViewNode lastNode, boolean isSelected);

    // === AWT === //

    /**
     * AWT bounds of selected ViewNode
     *
     * @param node ViewNode
     * @return AWT Bounds.
     */
    Rectangle findBoundsOfSelectionNode(ViewNode node);

}
