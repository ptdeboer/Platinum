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

import javax.swing.JPopupMenu;

import nl.esciencecenter.ptk.ui.UI;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.Action;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeContainer;

public interface BrowserInterface
{
    /** 
     * Returns master platform this browser is associated with
     */ 
    public BrowserPlatform getPlatform(); 
    
    public void handleException(String actionText,Throwable exception);

	public JPopupMenu createActionMenuFor(ViewNodeContainer container, ViewNode viewNode,boolean canvasMenu);

    public void handleNodeAction(ViewNode node, Action action);

    /** Return simple UI Interface */ 
    public UI getUI(); 

}
