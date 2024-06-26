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

package nl.esciencecenter.vbrowser.vrs.task;

import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.task.TransferMonitor;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import nl.esciencecenter.vbrowser.vrs.vrl.VRLUtil;

import java.util.List;

/**
 * VRSTransfer monitor class. Monitor object for ongoing VRS Actions and Transfers.
 */
public class VRSTaskMonitor extends TransferMonitor {
    // ========================================================================
    // instance
    // ========================================================================

    private final VRSActionType actionType = VRSActionType.UNKNOWN;

    // instance methods
    protected VRSTaskMonitor(ITaskMonitor parentMonitor, VRSActionType vrsAction, String resourceType,
                             List<VRL> sources, VRL destination) {
        super((vrsAction != null) ? vrsAction.toString() : "VRSTransfer", VRLUtil.toURIs(sources), destination
                .toURINoException());
        setParent(parentMonitor); // add this transfer to parent monitor
    }

    // instance methods
    public VRSTaskMonitor(VRSActionType vrsAction, List<VRL> sources, VRL destination) {
        super((vrsAction != null) ? vrsAction.toString() : "VRSTransfer", VRLUtil.toURIs(sources), destination
                .toURINoException());
    }

    public VRSActionType getTaskType() {
        return actionType;
    }

    /**
     * @return Returns the current source as VRL.
     */
    public VRL getCurrentSource() {
        java.net.URI uri = super.getSource();

        if (uri == null)
            return null;

        return new VRL(uri);
    }

}
