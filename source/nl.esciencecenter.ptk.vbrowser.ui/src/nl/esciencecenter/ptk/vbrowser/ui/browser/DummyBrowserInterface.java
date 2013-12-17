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

package nl.esciencecenter.ptk.vbrowser.ui.browser;

import javax.swing.JPopupMenu;

import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.Action;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeContainer;

/** 
 * Default Browser Interface Adaptor
 */
public class DummyBrowserInterface  implements BrowserInterface
{

    private BrowserPlatform platform;

    public DummyBrowserInterface(BrowserPlatform platform)
    {
        this.platform=platform; 
    }
    
    @Override
    public BrowserPlatform getPlatform()
    {
        return this.platform;
    }

    @Override
    public void handleException(String message,Throwable exception)
    {
        System.err.printf("%s\n",message);
        exception.printStackTrace(); 
    }

    @Override
    public JPopupMenu createActionMenuFor(ViewNodeContainer container, ViewNode viewNode, boolean canvasMenu)
    {
        return null;
    }

    @Override
    public void handleNodeAction(ViewNode node, Action action)
    {
        
    }

}
