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

package nl.esciencecenter.vbrowser.vrs.infors;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.data.HashMapList;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VRSTypes;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.node.VPathNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.*;

/**
 * Super class for all InfoRS Nodes. Manages a list of "sub" nodes.
 */
@Slf4j
public abstract class InfoRSPathNode extends VPathNode {

    // ===============
    // Class Constants
    // ===============

    public final static String[] subNodeTypes_arr = {InfoRSConstants.RESOURCEFOLDER,//
            InfoRSConstants.RESOURCELINK, VRSTypes.VLINK_TYPE};

    public static StringList defaultFolderChildTypes = new StringList(subNodeTypes_arr);

    static protected String[] inforsImmutableAttributeNames = {ATTR_SCHEME, //
            ATTR_LOCATION,//
            ATTR_RESOURCE_TYPE,//
            ATTR_NAME,//
            // Host + Port have been removed!
            ATTR_ICONURL,//
            ATTR_PATH,//
            ATTR_MIMETYPE //
    };

    // ===============
    // Instance
    // ===============

    /**
     * Store all settings of this node into persistent VRSPoperties object.
     */
    protected AttributeSet attributes;

    protected ArrayList<InfoRSPathNode> subNodes = new ArrayList<>();

    protected InfoRSPathNode parent;

    protected String resourceType = null;

    private boolean isEditable;

    protected InfoRSPathNode(VResourceSystem infoRs, String type, VRL vrl) {
        super(infoRs, vrl);
        this.resourceType = type;
        this.attributes = new AttributeSet(type);
    }

    /**
     * Return subset of immutable attributes.
     *
     * @return List containing the Immutable Attributes.
     */
    public Map<String, AttributeDescription> getImmutableAttributeDescriptions() {
        Map<String, AttributeDescription> list = super.getImmutableAttributeDescriptions();

        HashMapList<String, AttributeDescription> mylist = new HashMapList<String, AttributeDescription>();
        for (String name : inforsImmutableAttributeNames) {
            AttributeDescription descr = list.get(name);
            if (descr != null) {
                mylist.put(name, descr);
            }
        }
        return mylist;
    }

    public VRSContext getVRSContext() {
        return this.getInfoRS().getVRSContext();
    }

    protected InfoRS getInfoRS() {
        return (InfoRS) resourceSystem;
    }

    protected InfoRSPathNode(InfoRSPathNode parent, String type, VRL vrl) {
        super(parent.resourceSystem, vrl);
        this.resourceType = type;
        this.attributes = new AttributeSet(type);
    }

    final public String getResourceType() {
        return resourceType;
    }

    final public boolean isRoot() {
        return (parent != null);
    }

    public AttributeSet getInfoAttributes() {
        return this.attributes.duplicate(true);
    }

    @Override
    public boolean isComposite() {
        return true;
    }

    @Override
    public String getMimeType() {
        return VRSTypes.VBROWSER_VRS_MIMETYPE_PREFIX + "-infors-" + getResourceType();
    }

    @Override
    public InfoRSPathNode getParent() {
        return this.parent;
    }

    public VRL getParentVRL() {
        if (this.parent != null) {
            return parent.getVRL();
        }
        return new VRL("info", null, 0, "/");
    }

    @Override
    public List<? extends InfoRSPathNode> list() throws VrsException {
        return getSubNodes();
    }

    public List<? extends InfoRSPathNode> getSubNodes() {
        return subNodes;
    }

    public List<? extends InfoResourceNode> listResourceNodes() throws VrsException {
        // todo: Generic filter of sub classes.

        ArrayList<InfoResourceNode> filteredNodes = new ArrayList<InfoResourceNode>();

        if ((subNodes == null) || (subNodes.size() < 0)) {
            return null;
        }

        for (InfoRSPathNode node : subNodes) {
            if (node instanceof InfoResourceNode) {
                filteredNodes.add((InfoResourceNode) node);
            }
        }
        return filteredNodes;
    }

    final protected void setParent(InfoRSPathNode newParent) throws VrsException {
        if (newParent == null) {
            this.parent = null;
        } else if ((parent != null) && (parent != newParent)) {
            throw new VrsException("Internal Error. Node can not switch parents: current=" + parent + ", new ="
                    + newParent);
        }
        this.parent = newParent;
    }

    final public void addSubNode(InfoRSPathNode node) throws VrsException {
        synchronized (node) {
            node.setParent(this);
        }

        synchronized (subNodes) {
            subNodes.add(node);
        }
    }

    final public void setSubNodes(InfoRSPathNode[] nodes) {
        synchronized (subNodes) {
            subNodes.clear();
            for (InfoRSPathNode node : nodes) {
                subNodes.add(node);
            }
        }
    }

    final public void setSubNodes(List<InfoRSPathNode> nodes) {
        synchronized (subNodes) {
            subNodes.clear();
            for (InfoRSPathNode node : nodes) {
                subNodes.add(node);
            }
        }
    }

    final protected void delSubNode(InfoRSPathNode node) throws VrsException {
        synchronized (subNodes) {
            subNodes.remove(node);
            node.setParent(null);
        }
    }

    final protected void initSubNodes() {
        if (subNodes == null) {
            subNodes = new ArrayList<InfoRSPathNode>();
        } else {
            // keep object as it is used as mutex.
            synchronized (subNodes) {
                subNodes.clear();
            }
        }
    }

    /**
     * Performs recursive and linear search on ArrayList to find (sub)node with specified vrl.
     */
    public InfoRSPathNode findSubNode(VRL vrl, boolean recursive) {
        String subPath = vrl.getPath();
        if (subPath == null) {
            return null;
        }

        synchronized (subNodes) {
            for (InfoRSPathNode node : subNodes) {
                if (node == this) {
                    throw new Error("Cycled detected, sub node equals me!");
                }

                if (node.getVRL().equals(vrl)) {
                    return node;
                }

                if (recursive && subPath.startsWith(node.getVRL().getPath())) {
                    // recursive search:
                    InfoRSPathNode subNode = node.findSubNode(vrl, true);
                    if (subNode != null) {
                        return subNode;
                    }
                    // continue;
                }
            }
        }
        return null;
    }

    /**
     * Return node with logical (base)name.
     *
     * @param name logical name or basename of node.
     * @return - InfoRSNode or null.
     */
    protected InfoRSPathNode getSubNodeByName(String name) {
        // quick unsynchronized access:
        for (InfoRSPathNode node : subNodes) {
            if (StringUtil.equals(node.getName(), name)) {
                return node;
            }
        }
        return null;
    }

    public VRL createSubPathVRL(String subPath) throws VRLSyntaxException {
        return getVRL().resolvePath(subPath);
    }

    public VRL createNewSubNodeVRL() throws VRLSyntaxException {
        return getVRL().resolvePath("v" + this.getNumNodes());
    }

    public int getNumNodes() {
        return subNodes.size();
    }

    /**
     * Get top level root resource node. performs a upwards recursive search to get the root node.
     */
    protected InfoRootNode getRootNode() {
        InfoRSPathNode node = this;

        while (node.parent != null) {
            if (node == node.parent) {
                throw new Error("InfoRS hierachy error: parent of this node equals current node:" + node);
            }
            node = node.parent;
        }

        if (node instanceof InfoRootNode) {
            return (InfoRootNode) node;
        } else {
            log.warn("Couldn't find rootNode for:{}", this);
            return null;
        }
    }

    public String toString() {
        return "<InfoRSNode:" + this.getResourceType() + ">:" + getVRL();
    }

    public void setIsEditable(boolean editable) {
        this.isEditable = editable;
    }

    public boolean isEditable() {
        return this.isEditable;
    }

}
