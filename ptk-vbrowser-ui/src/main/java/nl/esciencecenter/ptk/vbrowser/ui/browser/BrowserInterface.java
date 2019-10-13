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

package nl.esciencecenter.ptk.vbrowser.ui.browser;

import nl.esciencecenter.ptk.task.ITaskSource;
import nl.esciencecenter.ptk.ui.UI;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionCmd;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyNodeDnDHandler.DropAction;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeComponent;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Master Browser Interface.
 */
public interface BrowserInterface {

    /**
     * Returns master platform this browser is associated with.
     */
    BrowserPlatform getPlatform();

    /**
     * Forward exception to master browser.
     */
    void handleException(String actionText, Throwable exception);

    /**
     * Create custom pop-up menu for specified ViewComponent and optional selected ViewNode. <br>
     * If a user right-clicks on a ViewNode or on the under laying canvas (ViewNodeContainer) this
     * method is called.
     *
     * @param viewComponent - actual ViewComponent the event is coming from. For example an IconPanel or
     *                      ResourceTree.
     * @param viewNode      - effective selected ViewNode on which the menu click occurs.
     * @param canvasMenu    - whether this is a canvas click on a ViewNodeContainer or an actual ViewNode. If
     *                      true the viewComponent must be a ViewNodeContainer. Multi-selection actions are
     *                      currently canvas menus actions, since the menu should apply to the selection and
     *                      not the clicked-on ViewNode.
     * @return
     */
    JPopupMenu createActionMenuFor(ViewNodeComponent viewComponent, ViewNode viewNode,
                                   boolean canvasMenu);

    /**
     * Is invoked after an action from the pop-up menu has been called.
     *
     * @param viewComponent - ViewNodeComponent or ViewNodeContainer.
     * @param node          - effective ViewNode
     * @param action        - actual action.
     */
    void handleNodeAction(ViewNodeComponent viewComponent, ViewNode node, ActionCmd action);

    /**
     * Return simple UI Interface.
     * This is a proxy object fore headless environments.
     */
    UI getUI();

    /**
     * Perform Drop.
     *
     * @param uiComponent Swing component.
     * @param optPoint    coordinates inside uiComponent.
     * @param viewNode    effective ViewNode
     * @param dropAction  Actual drop action, Copy, Move, Paste or Link.
     * @param vris        list of resource locations.
     * @return true
     * if drop succeed and has finished.
     */
    boolean doDrop(Component uiComponent, Point optPoint, ViewNode viewNode,
                   DropAction dropAction, List<VRL> vris);

    /**
     * Return task source/task watcher for backgrounded tasks originating from this browser.<br>
     * . Can be null if no task source or watcher initialized.
     *
     * @return
     */
    ITaskSource getTaskSource();

}
