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

package nl.esciencecenter.ptk.vbrowser.ui.tree;

import java.awt.Component;
import java.awt.Image;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;

public class ResourceTreeCellRenderer extends DefaultTreeCellRenderer
{
    // public static Color COLOR_LIGHT_BLUE=new Color(128,128,255);

    /** */
    private static final long serialVersionUID = -2947025684699355279L;

    private ResourceTree myTree;

    public ResourceTreeCellRenderer(ResourceTree tree)
    {
        this.myTree = tree;
    }

    public Component getTreeCellRendererComponent(
            JTree jtree,
            Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus)
    {
        // let DefaultTreeCellRender do the main work
        Component c = super.getTreeCellRendererComponent(
                jtree, value, selected,
                expanded, leaf, row,
                hasFocus);

        // ===
        // Assert: Component 'c' should be equal to >>>this<<<
        // ===

        ResourceTreeNode node = (ResourceTreeNode) value;
        // ResourceTree tree=(ResourceTree)jtree;

        ViewNode item = node.getViewItem();
        this.setIcon(item.getIcon());
        this.setEnabled(item.isBusy() == false);

        if (node.hasFocus())
        {
            // use HTML make up
            // setBackgroundNonSelectionColor(Color.YELLOW);
            this.setText("<html><u>" + node.getName() + "</u></html");
        }
        else
        {
            // setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
            this.setText(node.getName());
        }

        return this;
    }

    public boolean imageUpdate2(Image img, int infoflags,
            int x, int y, int w, int h)
    {
        return super.imageUpdate(img, infoflags, x, y, w, h);

    }

}
