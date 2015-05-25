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

package nl.esciencecenter.vbrowser.vrs.event;

import java.io.Serializable;

import nl.esciencecenter.ptk.events.IEvent;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VRSEvent implements IEvent<VRSEventType>, Serializable  {

    private static final long serialVersionUID = -6655387700048315217L;

    // ========================================================================
    //
    // ========================================================================

    public static VRSEvent createChildsAddedEvent(VRL optionalParent, VRL childs[]) {
        VRSEvent event = new VRSEvent(optionalParent,VRSEventType.RESOURCES_CREATED);
        event.resources = childs;
        return event;
    }

    public static VRSEvent createChildAddedEvent(VRL parent, VRL child) {
        return createChildsAddedEvent(parent, new VRL[]{child});
    }

    public static VRSEvent createChildsDeletedEvent(VRL optionalParent, VRL childs[]) {
        VRSEvent event = new VRSEvent(optionalParent,VRSEventType.RESOURCES_DELETED);
        event.resources = childs;
        return event;
    }

    public static VRSEvent createChildDeletedEvent(VRL optionalParent, VRL child) {
        VRSEvent event = new VRSEvent(optionalParent,VRSEventType.RESOURCES_DELETED,new VRL[]{child});
        return event;
    }

    public static VRSEvent createNodesDeletedEvent(VRL nodeVrls[]) {
        VRSEvent event = new VRSEvent(null,VRSEventType.RESOURCES_DELETED);
        // multi event without parent.
        event.resources = nodeVrls;
        return event;
    }

    public static VRSEvent createRefreshEvent(VRL optionalParent, VRL res) {
        VRSEvent event = new VRSEvent(optionalParent,VRSEventType.RESOURCES_UPDATED);
        event.resources = new VRL[1];
        event.resources[0] = res;
        return event;
    }

    public static VRSEvent createNodeRenamedEvent(VRL optParentVRL, VRL oldVrl, VRL newVrl) {
        VRSEvent event = new VRSEvent(optParentVRL,VRSEventType.RESOURCES_RENAMED);
        event.resources = new VRL[] { oldVrl };
        event.otherResources = new VRL[] { newVrl };
        return event;
    }

    // ========================================================================
    //
    // ========================================================================

    protected VRSEventType type;

    /**
     * Optional parent resource.
     */
    protected VRL parentSource;

    /**
     * Sources this event applies to
     */
    protected VRL[] resources;

    /**
     * Optional new resources, when specified otherResources[i] is the new VRL for resources[i].
     */
    protected VRL[] otherResources;

    /**
     * Optional attribute names involved.
     */
    protected String attributeNames[];


    public VRSEvent(VRL sourceVRL, VRSEventType type) {
        this.parentSource=sourceVRL;
        this.type=type;
    }

    protected VRSEvent(VRL optionalParent, VRSEventType type, VRL[] vrls) {
        this.type = type;
        this.parentSource=optionalParent;
        this.resources=vrls;
    }

    public VRSEventType getType() {
        return this.type;
    }

    /** Resources this event applies to. */
    public VRL[] getResources() {
        return this.resources;
    }

    /** Resources this event applies to. */
    public VRL[] getOtherResources() {
        return this.otherResources;
    }

    /**
     * If the parent resource has been specified, it is the parent of all the resource from
     * getResources()
     */
    public VRL getParent() {
        return parentSource;
    }

    /** Attributes this event applies to if this is an Attribute Event */
    public String[] getAttributeNames() {
        return this.attributeNames;
    }

    @Override
    public String toString() {
        return "DataSourceEvent:" + this.type + ":(parentSource=" + parentSource + ", resources={" + flattenStr(resources) + "})";
    }

    private String flattenStr(VRL[] locs) {
        if (locs == null)
            return "";
        String str = "";
        for (int i = 0; i < locs.length; i++) {
            str += locs[i];
            if (i + 1 < locs.length)
                str += ",";
        }
        return str;
    }

    @Override
    public VRSEventType getEventType() {
        return this.type;
    }

    @Override
    public Object getEventSource() {
      return parentSource;
    }

}
