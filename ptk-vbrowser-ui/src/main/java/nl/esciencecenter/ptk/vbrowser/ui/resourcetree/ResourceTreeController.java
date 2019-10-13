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
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionCmd;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeActionListener;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreePath;

@Slf4j
public class ResourceTreeController implements TreeExpansionListener, ViewNodeActionListener {

    private ResourceTree tree;

    private BrowserInterface browser;

    public ResourceTreeController(BrowserInterface browser, ResourceTree resourceTree,
                                  ResourceTreeModel model) {
        this.tree = resourceTree;
        this.browser = browser;
    }

    public void handleNodeActionEvent(ViewNode node, ActionCmd action) {
        this.browser.handleNodeAction(tree, node, action);
    }

    // From TreeExpansionListener
    public void treeExpanded(TreeExpansionEvent evt) {
        log.debug("TreeExpansionHandler.treeExpanded()");

        TreePath path = evt.getPath();
        if (evt.getSource().equals(tree) == false) {
            log.error("***Received event from different tree!");
            return;
        }
        // Get the last component of the path and
        // arrange to have it fully populated.
        ResourceTreeNode node = (ResourceTreeNode) path.getLastPathComponent();

        if (node.isPopulated() == false)
            tree.populate(node);
        // else update ?
    }

    // From TreeExpansionListener
    public void treeCollapsed(TreeExpansionEvent evt) {
        log.debug("TreeExpansionHandler.treeCollapsed()");
    }

    public BrowserInterface getBrowserInterface() {
        return browser;
    }

}
