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

package nl.esciencecenter.ptk.vbrowser.ui.proxy.vrs;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.ptk.data.Holder;
import nl.esciencecenter.ptk.data.ListHolder;
import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyNodeDnDHandler;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.event.VRSEvent;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.io.copy.VRSCopyManager;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.util.List;

/**
 * Handles DnD by doing actual VRS/VFS transfers.
 * Also updates ProxyFactory (cache) and fires update events to notify various resource panels that a backgrounded
 * transfer has been performed.
 */
@Slf4j
public class VRSProxyNodeDnDHandler implements ProxyNodeDnDHandler {

    protected VRSCopyManager vrsManager;
    protected VRSProxyFactory proxyFactory;

    public VRSProxyNodeDnDHandler(VRSProxyFactory vrsProxyFactory, VRSCopyManager vrsTaskManager) {
        vrsManager = vrsTaskManager;
        proxyFactory = vrsProxyFactory;
    }

    @Override
    public boolean doDrop(ViewNode targetDropNode, DropAction dropAction, List<VRL> vrls,
                          ITaskMonitor taskMonitor) throws ProxyException {
        VRL destVrl = targetDropNode.getVRL();

        Holder<VPath> destPathH = new Holder<VPath>();
        ListHolder<VPath> resultNodesH = new ListHolder<VPath>();
        ListHolder<VPath> deletedNodesH = new ListHolder<VPath>();

        if (dropAction == DropAction.LINK) {
            try {
                boolean result = vrsManager.doLinkDrop(vrls, destVrl, destPathH, resultNodesH,
                        taskMonitor);

                if (result) {
                    // register proxy nodes for prefetching.
                    ProxyNode targetNode = proxyFactory.registerVRSProxyNode(destPathH.get());

                    // register proxy nodes for prefetching.
                    List<ProxyNode> dropNodes = proxyFactory.registerVRSProxyNodes(resultNodesH.get());
                    this.fireNewNodesEvent(targetNode, dropNodes);
                }

                return result;
            } catch (VrsException e) {
                throw new ProxyException(e.getMessage(), e);
            }
        } else if (dropAction == DropAction.COPY || dropAction == DropAction.MOVE
                || dropAction == DropAction.COPY_PASTE || dropAction == DropAction.CUT_PASTE) {
            boolean isMove = ((dropAction == DropAction.MOVE) || (dropAction == DropAction.CUT_PASTE));
            try {
                boolean result = vrsManager.doCopyMove(vrls, destVrl, isMove, destPathH,
                        resultNodesH, deletedNodesH, taskMonitor);

                if (result) {
                    if (destPathH.value == null) {
                        throw new VrsException("CopyMove has NULL destination path holder value!");
                    }
                    if (resultNodesH.values == null) {
                        throw new VrsException("CopyMove has NULL destination nodes holder value!");
                    }

                    // register proxy node for prefetching.
                    ProxyNode targetNode = proxyFactory.registerVRSProxyNode(destPathH.value);

                    // register proxy nodes for prefetching.
                    List<ProxyNode> dropNodes = proxyFactory.registerVRSProxyNodes(resultNodesH.values);
                    this.fireNewNodesEvent(targetNode, dropNodes);

                    if ((deletedNodesH.values != null) && (deletedNodesH.values.size() > 0)) {
                        List<ProxyNode> deletedNodes = proxyFactory.registerVRSProxyNodes(deletedNodesH.values);
                        this.fireNodesDeletedEvent(deletedNodes);
                    }
                }
            } catch (VrsException e) {
                throw new ProxyException(e.getMessage(), e);
            }
        } else {
            log.error(String.format(
                    "FIXME: VRSViewNodeDnDHandler unrecognized DROP:%s:on %s, list=%s\n",
                    dropAction, targetDropNode, new ExtendedList<VRL>(vrls)));
            return false;
        }

        return true;
    }

    public void fireNewNodesEvent(ProxyNode targetNode, List<ProxyNode> dropNodes) {
        VRSEvent event = VRSEvent.createChildsAddedEvent(targetNode.getVRL(),
                ProxyNode.toVRLArray(dropNodes));
        proxyFactory.getProxyNodeEventNotifier().scheduleEvent(event);
    }

    public void fireNodesDeletedEvent(List<ProxyNode> deletedNodes) {
        VRSEvent event = VRSEvent.createNodesDeletedEvent(ProxyNode.toVRLArray(deletedNodes));
        proxyFactory.getProxyNodeEventNotifier().scheduleEvent(event);
    }
}
