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

package nl.esciencecenter.ptk.vbrowser.ui.model;

import nl.esciencecenter.ptk.data.LongHolder;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.vbrowser.vrs.event.VRSEventListener;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Data Source for ViewNodes.
 */
public interface ViewNodeDataSource {

    /**
     * Register listener to receive data source update events. Listeners received events about
     * created ViewNodes
     */
    void addDataSourceEventListener(VRSEventListener listener);

    void removeDataSourceEventListener(VRSEventListener listener);

    /**
     * Toplevel resource or root node.
     *
     * @throws ProxyException
     */
    ViewNode getRoot(UIViewModel uiModel) throws ProxyException; // throws ProxyException;

    /**
     * Get childs of specified resource.
     *
     * @param uiModel - the UIModel
     * @param locator - location of resource
     * @param offset  - get childs starting from this offset
     * @param range   - maximum number of childs wanted. Use -1 for all.
     */
    ViewNode[] getChilds(UIViewModel uiModel, VRL locator, int offset, int range,
                         LongHolder numChildsLeft) throws ProxyException;

    /**
     * Open locations and create ViewNodes.
     *
     * @param uiModel   - the UIModel to use
     * @param locations - resource locations
     * @return created ViewNodes
     */
    ViewNode[] createViewNodes(UIViewModel uiModel, VRL[] locations) throws ProxyException;

}
