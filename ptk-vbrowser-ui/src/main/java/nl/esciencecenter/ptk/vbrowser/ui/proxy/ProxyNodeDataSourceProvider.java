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

package nl.esciencecenter.ptk.vbrowser.ui.proxy;

import nl.esciencecenter.ptk.data.LongHolder;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyDataSource;
import nl.esciencecenter.ptk.vbrowser.ui.model.UIViewModel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.event.VRSEventListener;
import nl.esciencecenter.vbrowser.vrs.event.VRSEventNotifier;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.util.List;

/**
 * DataSource which produces ViewItems from ProxyItems. Is DataSource Adaptor. Bridging class
 * connects ProxyNode (Resource) with ViewNodes (UI Object) and Attributes.
 */
public class ProxyNodeDataSourceProvider implements ProxyDataSource {

    private final ProxyNode rootNode;

    private final ProxyFactory proxyFactory;

    private final VRSEventNotifier eventNotifier;

    public ProxyNodeDataSourceProvider(ProxyNode sourceNode) {
        this.rootNode = sourceNode;
        this.proxyFactory = sourceNode.getProxyFactory();
        this.eventNotifier = proxyFactory.getProxyNodeEventNotifier();
    }

    @Override
    public ViewNode getRoot(UIViewModel uiModel) throws ProxyException {
        return rootNode.createViewItem(uiModel);
    }

    public ProxyNode[] getChildProxyItems(VRL locator, int offset, int range,
                                          LongHolder numChildsLeft) throws ProxyException {
        ProxyNode[] childs;

        if (rootNode.hasLocator(locator)) {
            childs = ProxyNode.toArray(rootNode.getChilds(offset, range, numChildsLeft));
        } else {
            ProxyNode parent = proxyFactory.openLocation(locator);
            childs = ProxyNode.toArray(parent.getChilds(offset, range, numChildsLeft));
        }

        return childs;
    }

    public ViewNode[] createViewItems(UIViewModel uIModel, ProxyNode[] childs)
            throws ProxyException {
        //
        if (childs == null)
            return null;
        int len = childs.length;
        ViewNode[] items = new ViewNode[len];
        for (int i = 0; i < len; i++) {
            items[i] = childs[i].createViewItem(uIModel);
        }
        return items;
    }

    @Override
    public void addDataSourceEventListener(VRSEventListener listener) {
        eventNotifier.addListener(listener, null);
    }

    @Override
    public void removeDataSourceEventListener(VRSEventListener listener) {
        eventNotifier.removeListener(listener);
    }

    public ViewNode[] getChilds(UIViewModel uIModel, VRL locator) throws ProxyException {
        return createViewItems(uIModel, getChildProxyItems(locator, 0, -1, null));
    }

    @Override
    public ViewNode[] getChilds(UIViewModel uiModel, VRL locator, int offset, int range,
                                LongHolder numChildsLeft) throws ProxyException {
        return createViewItems(uiModel, getChildProxyItems(locator, offset, range, numChildsLeft));
    }

    @Override
    public ViewNode[] createViewNodes(UIViewModel uiModel, VRL[] locations) throws ProxyException {
        //
        int len = locations.length;
        ViewNode[] nodes = new ViewNode[len];
        for (int i = 0; i < len; i++) {
            ProxyNode node = proxyFactory.openLocation(locations[i]);
            nodes[i] = node.createViewItem(uiModel);
        }
        return nodes;
    }

    @Override
    public List<String> getAttributeNames(VRL locator) throws ProxyException {
        ProxyNode node = proxyFactory.openLocation(locator);
        return node.getAttributeNames();
    }

    @Override
    public List<Attribute> getAttributes(VRL locator, String[] attrNames) throws ProxyException {
        ProxyNode node = proxyFactory.openLocation(locator);
        return node.getAttributes(attrNames);
    }

    @Override
    public Presentation getPresentation() throws ProxyException {
        return rootNode.getPresentation();
    }

    @Override
    public ProxyNode getRootNode() {
        return this.rootNode;
    }

    @Override
    public String toString() {
        return "<ProxyNodeDataSource>:" + rootNode;
    }
}
