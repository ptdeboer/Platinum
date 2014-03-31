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

package nl.esciencecenter.ptk.vbrowser.ui.resourcetable;

import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.Action;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeActionListener;

public class ResourceTableControler implements ViewNodeActionListener
{
    private ResourceTable table;

    private BrowserInterface browserController;

    public ResourceTableControler(ResourceTable resourceTable,
            BrowserInterface browserController)
    {
        this.table = resourceTable;
        this.browserController = browserController;
    }

    public void handle(String action, Throwable e)
    {
        browserController.handleException(action, e);
    }

    public BrowserInterface getBrowserInterface()
    {
        return browserController; 
    }

    @Override
    public void handleNodeActionEvent(ViewNode node, Action action)
    {
        System.err.printf("FIXME: ResourceTableControler:handleNodeActionEvent():%s on:%s\n", action, node);
    }
}
