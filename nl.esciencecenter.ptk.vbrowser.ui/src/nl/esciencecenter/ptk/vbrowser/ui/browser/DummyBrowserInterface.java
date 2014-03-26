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

import nl.esciencecenter.ptk.ui.SimpelUI;
import nl.esciencecenter.ptk.ui.UI;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.Action;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyNodeDnDHandler;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyNodeDnDHandler.DropAction;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeComponent;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeContainer;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/** 
 * Default Browser Interface Adaptor
 */
public class DummyBrowserInterface  implements BrowserInterface
{
    final static ClassLogger logger=ClassLogger.getLogger(DummyBrowserInterface.class); 
    
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
    public void handleException(String message,Throwable e)
    {
        logger.logException(ClassLogger.ERROR, e, message, "Exception:%s\n"); 
    }

    @Override
    public JPopupMenu createActionMenuFor(ViewNodeContainer container, ViewNode viewNode, boolean canvasMenu)
    {
        return null;
    }

    @Override
    public void handleNodeAction(ViewNodeComponent viewComp, ViewNode node, Action action)
    {
        logger.errorPrintf("handleNodeAction:%s", node); 
    }

    @Override
    public UI getUI()
    {
        return new SimpelUI(); 
    }

    @Override
    public boolean doDrop(Component uiComponent, Point optPoint, ViewNode viewNode, DropAction dropAction, List<VRL> vris)
    {
        try
        {
            return ProxyNodeDnDHandler.getInstance().doDrop(viewNode, dropAction, vris, null);
        }
        catch (ProxyException e)
        {
            handleException("Drop Failed",e);
            return false;
        }
    }

}
