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

import java.awt.Component;
import java.awt.Point;
import java.util.List;

import javax.swing.JPopupMenu;

import nl.esciencecenter.ptk.ui.UI;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.Action;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyNodeDnDHandler.DropAction;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeComponent;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public interface BrowserInterface
{
    /**
     * Returns master platform this browser is associated with
     */
    public BrowserPlatform getPlatform();

    /**
     * Forward Exception to Master Browser.
     */
    public void handleException(String actionText, Throwable exception);

    /**
     * Create custom pop-up menu for specified ViewCompnent and optional selected ViewNode. <br>
     * If a user right click on a ViewNode this method is called.
     * 
     * @param viewComponent
     *            - Actual ViewComponent the event is coming from
     * @param viewNode
     *            - option selected ViewNode on which the menu click occurs.
     * @param canvasMenu
     *            - whether this is a canvas click on a ViewContainer or an actual ViewNode. if true the viewComponent
     *            is a ViewNodeContainer. Multi-selection click are always canvas clicks, since there not a single view node selected.  
     * @return
     */
    public JPopupMenu createActionMenuFor(ViewNodeComponent viewComponent, ViewNode viewNode, boolean canvasMenu);

    public void handleNodeAction(ViewNodeComponent viewComponent, ViewNode node, Action action);

    /**
     * Return simple UI Interface
     */
    public UI getUI();

    public boolean doDrop(Component uiComponent, Point optPoint, ViewNode viewNode, DropAction dropAction, List<VRL> vris);

}
