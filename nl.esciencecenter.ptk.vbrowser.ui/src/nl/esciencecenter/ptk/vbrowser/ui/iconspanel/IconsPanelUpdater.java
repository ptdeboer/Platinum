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

package nl.esciencecenter.ptk.vbrowser.ui.iconspanel;

import nl.esciencecenter.ptk.task.ITaskSource;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserTask;
import nl.esciencecenter.ptk.vbrowser.ui.browser.ProxyBrowserController;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyDataSource;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyDataSourceUpdater;
import nl.esciencecenter.ptk.vbrowser.ui.model.UIViewModel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.vbrowser.vrs.event.VRSEvent;
import nl.esciencecenter.vbrowser.vrs.event.VRSEventListener;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class IconsPanelUpdater implements VRSEventListener, ProxyDataSourceUpdater
{
    private final static ClassLogger logger = ClassLogger.getLogger(IconsPanelUpdater.class);

    private ProxyDataSource dataSource;

    private IconsPanel iconsPanel;

    private ViewNode rootNode;

    public IconsPanelUpdater(IconsPanel panel, ProxyDataSource dataSource)
    {
        this.iconsPanel = panel;
        this.dataSource = dataSource;
        setDataSource(dataSource, true);
    }

    public ViewNode getRootNode()
    {
        return rootNode;
    }

    public UIViewModel getUIModel()
    {
        return this.iconsPanel.getUIViewModel();
    }

    protected BrowserInterface getMasterBrowser()
    {
        return this.iconsPanel.getMasterBrowser();
    }

    protected ITaskSource getTaskSource()
    {
        BrowserInterface masterB = getMasterBrowser();

        if (masterB instanceof ProxyBrowserController)
        {
            return masterB.getTaskSource();
        }

        return null;
    }

    public void setDataSource(ProxyDataSource dataSource, boolean update)
    {
        // unregister
        if (this.dataSource != null)
        {
            this.dataSource.removeDataSourceEventListener(this);
        }

        this.dataSource = dataSource;

        // register
        if (this.dataSource != null)
        {
            this.dataSource.addDataSourceEventListener(this);
        }

        if ((update) && (dataSource != null))
        {
            updateRoot();
        }
    }

    @Override
    public void notifyVRSEvent(VRSEvent e)
    {
        VRL vrls[] = e.getResources();
        VRL parent = e.getParent();

        VRL otherVrls[] = e.getOtherResources();

        // check parent:
        if ((parent != null) && (rootNode.getVRL().equals(parent) == false))
        {
            return;
        }

        switch (e.getType())
        {
            case RESOURCES_ADDED:
            {
                if (parent == null)
                {
                    logger.errorPrintf("Cannot check if new resource are for me. parent==null!\n");
                    return;
                }

                refresh();
                break;
            }
            case RESOURCES_DELETED:
            {
                deleteChilds(vrls);
                break;
            }
            default:
            {
                refresh();
                break;
            }
        }
    }

    private void deleteChilds(VRL[] vrls)
    {
        // perform delete during Event thread.
        IconListModel model = iconsPanel.getModel();

        for (VRL vrl : vrls)
        {
            model.deleteItem(vrl, true);
        }

        iconsPanel.revalidate();
    }

    public void refresh()
    {
        updateRoot();
        iconsPanel.revalidate();
    }

    private IconItem[] createIconItems(ViewNode[] nodes)
    {
        if (nodes == null)
        {
            return null;
        }

        int len = nodes.length;

        IconItem items[] = new IconItem[len];

        for (int i = 0; i < len; i++)
        {
            items[i] = createIconItem(nodes[i]);
        }

        return items;
    }

    protected IconItem createIconItem(ViewNode node)
    {
        IconItem item = new IconItem(iconsPanel, getUIModel(), node);
        item.initDND(iconsPanel.getPlatform().getTransferHandler(), iconsPanel.getDragGestureListener());
        return item;
    }

    private void handle(String actionText, ProxyException e)
    {
        this.iconsPanel.getMasterBrowser().handleException(actionText, e);
    }

    public ProxyDataSource getDataSource()
    {
        return this.dataSource;
    }

    protected void updateRoot()
    {
        if (dataSource == null)
        {
            updateChilds(null);// clear/reset;
            return;
        }

        doPopulate(true, true);
    }

    // ======================
    // Backgrounded tasks.
    // ======================

    private void doPopulate(final boolean fetchRoot, final boolean fetchChilds)
    {
        BrowserTask task = new BrowserTask(this.getTaskSource(), "Populating IconsPanel.")
        {

            @Override
            protected void doTask() throws Exception
            {
                try
                {
                    if (fetchRoot)
                    {
                        rootNode = dataSource.getRoot(getUIModel());
                    }

                    if (fetchChilds)
                    {
                        ViewNode[] childs = dataSource.getChilds(getUIModel(), rootNode.getVRL(), 0, -1, null);
                        updateChilds(childs);
                    }
                }
                catch (ProxyException e)
                {
                    handle("Couldn't populate  root location.", e);
                }
            }
        };

        task.startTask();

    }

    private void updateChilds(ViewNode[] childs)
    {
        this.iconsPanel.getModel().setItems(createIconItems(childs));
    }

}
