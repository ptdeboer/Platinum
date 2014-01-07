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

package nl.esciencecenter.ptk.vbrowser.ui.model;

import java.awt.Point;

import javax.swing.JPopupMenu;

/**
 * Interface for any (J)Component which can contain ViewNodes.
 * A ViewNodeContainer in itself is also a ViewNodeComponent. 
 */
public interface ViewNodeContainer  extends ViewNodeComponent
{
	ViewNode getNodeUnderPoint(Point p);

	// === Menu === // 
    JPopupMenu createNodeActionMenuFor(ViewNode node, boolean canvasMenu);
    
	// === Selection Model === 
	void clearNodeSelection();

	ViewNode[] getNodeSelection();

	/** Toggle selection */ 
	void setNodeSelection(ViewNode node, boolean isSelected);
	
	/** Toggle selection of range */ 
    void setNodeSelectionRange(ViewNode firstNode, ViewNode lastNode,boolean isSelected);

    /** Request focus for child. Return true if it has focus. */ 
    boolean requestNodeFocus(ViewNode node, boolean value);
	
}
