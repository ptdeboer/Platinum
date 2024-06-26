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
import nl.esciencecenter.ptk.task.ITaskSource;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserTask;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyDataSource;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyDataSourceUpdater;
import nl.esciencecenter.ptk.vbrowser.ui.model.UIViewModel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.vbrowser.vrs.event.VRSEvent;
import nl.esciencecenter.vbrowser.vrs.event.VRSEventListener;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.util.List;

/**
 * Gets relevant data from the (Proxy)DataSource and updates the ResourceTreeModel.
 */
@Slf4j
public class ResourceTreeUpdater implements VRSEventListener, ProxyDataSourceUpdater {

    private final ResourceTree tree;

    private ProxyDataSource viewNodeSource;

    private ViewNode rootItem;

    public ResourceTreeUpdater(ResourceTree tree, ProxyDataSource viewNodeSource) {
        this.tree = tree;
        setDataSource(viewNodeSource, false);
    }

    public ResourceTreeModel getModel() {
        return tree.getModel();
    }

    public UIViewModel getUIModel() {
        return tree.getUIViewModel();
    }

    protected BrowserInterface getBrowserInterface() {
        return this.tree.getBrowserInterface();
    }

    protected ITaskSource getTaskSource() {
        BrowserInterface browser = this.getBrowserInterface();
        if (browser != null) {
            return browser.getTaskSource();
        }
        return null;
    }

    public void setDataSource(ProxyDataSource viewNodeSource, boolean update) {
        // unregister previous
        if (viewNodeSource != null)
            viewNodeSource.removeDataSourceEventListener(this);

        this.viewNodeSource = viewNodeSource;

        // register me as listener
        if (viewNodeSource != null) {
            viewNodeSource.addDataSourceEventListener(this);
        }
        if (update) {
            update();
        }
    }

    @Override
    public void update() {
        log.debug("updateRoot():");

        BrowserTask task = new BrowserTask(this.getTaskSource(),
                "update resource tree model for node" + rootItem) {
            @Override
            public void doTask() {
                _updateRoot();
            }

            @Override
            public void stopTask() {
            }
        };

        task.startTask();
    }

    /**
     * Get or repopulate childs nodes
     */
    public void updateChilds(final ResourceTreeNode node) {
        log.debug("updateChilds():{}", node.getVRL());

        BrowserTask task = new BrowserTask(this.getTaskSource(),
                "update resource tree model for node" + rootItem) {
            @Override
            public void doTask() {
                _updateChilds(node);
            }
        };

        task.startTask();
    }

    @Override
    public void notifyEvent(VRSEvent e) {
        log.debug("notifyEvent:{}", e);

        VRL parent = e.getParent();
        VRL[] sources = e.getResources();

        switch (e.getType()) {
            case RESOURCES_DELETED:
                deleteNodes(sources);
                break;
            case RESOURCES_UPDATED:
                update(sources, null);
                break;
            case RESOURCES_CREATED:
                addNodes(parent, sources);
                break;
            case RESOURCES_RENAMED:
                renameNodes(parent, sources, e.getOtherResources());
                break;
            case ATTRIBUTES_UPDATED:
                updateAttributes(e.getParent(), e.getResources(), e.getAttributeNames());
                break;
            default:
                log.error("FIXME: event not supported:{}", e);
        }
    }

    protected void updateAttributes(VRL parent, VRL[] resources, String[] attributeNames) {
        for (VRL vrl : resources) {
            updateAttributes(parent, vrl, attributeNames);
        }
    }

    protected void updateAttributes(VRL parent, VRL vrl, String[] attrNames) {
        // just refresh all:
        this.update(new VRL[]{vrl}, attrNames);
    }

    protected void renameNodes(VRL optParent, VRL[] sources, VRL[] targets) {
        // sources[i] => targets[i]
        for (int i = 0; i < sources.length; i++) {
            renameNode(optParent, sources[i], targets[i]);
        }
    }

    protected void renameNode(VRL optParent, VRL orgSource, VRL newSource) {
        if (orgSource.equals(newSource)) {
            // logical rename: just fresh attributes
            this.update(new VRL[]{orgSource}, null);
        } else if ((optParent != null) && optParent.isParentOf(orgSource)
                && (optParent.isParentOf(newSource))) {
            // sibling rename in similar tree branch: remove old and add new:
            log.debug("sibling rename of {}=> {}", orgSource.getBasename(),
                    newSource.getBasename());
            this.deleteNodes(new VRL[]{orgSource});
            this.addNodes(optParent, new VRL[]{newSource});
        } else {
            // delete old nodes, add new nodes from different tree branch.
            this.deleteNodes(new VRL[]{orgSource});

            log.error("FIXME: Guessing new PArent VRL of new (renamed) resource:{}",
                    newSource);
            VRL newParentVrl = newSource.getParent();

            // check parent:
            List<ResourceTreeNode> nodes = this.getModel().findNodes(newParentVrl);
            if (nodes != null) {
                this.addNodes(newParentVrl, new VRL[]{newSource});
            }
            log.debug("New node not yet in resoruce tree:{}", newSource);
        }
    }

    protected void deleteNodes(VRL[] sources) {
        for (VRL loc : sources) {
            List<ResourceTreeNode> nodes = getModel().findNodes(loc);

            if (nodes != null) {
                for (ResourceTreeNode node : nodes) {
                    getModel().deleteNode(node, true);
                }
            }
        }
    }

    protected void addNodes(final VRL parent, final VRL[] sources) {
        log.debug("addNodes():{}", parent);

        BrowserTask task = new BrowserTask(this.getTaskSource(),
                "update resource tree model for node" + rootItem) {
            @Override
            public void doTask() {
                _addNodes(parent, sources);
            }
        };

        task.startTask();
    }

    @Override
    public void update(final VRL[] sources, final String[] optAttrNames) {
        log.debug("refreshNodes():{}", sources.length);

        BrowserTask task = new BrowserTask(this.getTaskSource(), "refreshNodes " + sources.length) {
            @Override
            public void doTask() {
                _refreshNodes(sources, optAttrNames);
            }

            @Override
            public void stopTask() {
            }
        };

        task.startTask();
    }

    // ========================================================================
    // Actual Backgrounded Update Tasks
    // ========================================================================

    private void _updateRoot() {
        try {
            this.rootItem = viewNodeSource.getRoot(getUIModel());
            log.debug("updateRoot():{}", rootItem.getVRL());

            log.debug("_updateRoot():{}", rootItem.getVRL());

            ResourceTreeNode rtRoot = new ResourceTreeNode(null, rootItem, true);
            getModel().setRoot(rtRoot);

            // pre fetch childs and populate root in advance:

            ViewNode[] childs = viewNodeSource.getChilds(getUIModel(), rootItem.getVRL(), 0, -1,
                    null);

            if (childs == null) {
                rtRoot.clear();
                rtRoot.setPopulated(true);
            } else {
                getModel().setChilds(rtRoot, childs);
            }
        } catch (ProxyException e) {
            bgHandle("Failed to update ResourceTree RootNode.", e);
        }
    }

    private void _updateChilds(final ResourceTreeNode node) {
        ViewNode[] childs;

        try {
            childs = this.viewNodeSource.getChilds(getUIModel(), node.getVRL(), 0, -1, null);
            getModel().setChilds(node, childs);
        } catch (ProxyException e) {
            bgHandle("Failed to update child nodes.", e);
        }
    }

    private void _refreshNodes(VRL[] sources, String[] attrNames) {
        // Warning: potential N x M => O(N^2) if resources and nodes arrays are
        // large.

        for (VRL vrl : sources) {
            try {

                // Refresh Complete ViewNode:
                ViewNode[] viewNodes = viewNodeSource.createViewNodes(getUIModel(),
                        new VRL[]{vrl});

                if ((viewNodes == null) || (viewNodes.length <= 0)) {
                    log.error("Internal error: _refreshNodes(): no viewNodes for:{}",
                            vrl);
                    continue;
                }

                // Tree might have multiple entries, just update all
                List<ResourceTreeNode> nodes = getModel().findNodes(vrl);
                for (ResourceTreeNode node : nodes) {
                    _updateChilds(node);
                }

            } catch (ProxyException e) {
                bgHandle("Failed to update resource:" + vrl, e);
            }
        }
    }

    private void _addNodes(final VRL parent, final VRL[] sources) {
        try {
            ViewNode[] childs;

            childs = viewNodeSource.createViewNodes(getUIModel(), sources);

            List<ResourceTreeNode> nodes = getModel().findNodes(parent);

            if (nodes != null) {
                for (ResourceTreeNode node : nodes) {
                    getModel().addNodes(node, childs);
                }
            }

        } catch (ProxyException e) {
            bgHandle("Couldn't add new nodes.", e);
        }
    }

    private void bgHandle(String actionText, Throwable e) {
        this.tree.getBrowserInterface().handleException(actionText, e);
    }

    @Override
    public ProxyDataSource getDataSource() {
        return this.viewNodeSource;
    }

}
