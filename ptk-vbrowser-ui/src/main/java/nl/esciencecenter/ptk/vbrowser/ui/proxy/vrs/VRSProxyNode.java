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
import nl.esciencecenter.ptk.data.LongHolder;
import nl.esciencecenter.ptk.presentation.IPresentable;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.vbrowser.vrs.VEditable;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.infors.VInfoResource;
import nl.esciencecenter.vbrowser.vrs.infors.VResourceConfigurable;
import nl.esciencecenter.vbrowser.vrs.io.VDeletable;
import nl.esciencecenter.vbrowser.vrs.io.VFSDeletable;
import nl.esciencecenter.vbrowser.vrs.io.VRenamable;
import nl.esciencecenter.vbrowser.vrs.presentation.VRSPresentation;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * VRS ProxyNode: binds VPath and ProxyNode.
 */
@Slf4j
public class VRSProxyNode extends ProxyNode {

    private final VPath vnode;

    public VRSProxyNode(VRSProxyFactory vrsProxyFactory, VPath vnode, VRL locator)
            throws ProxyException {
        super(vrsProxyFactory, locator);
        this.vnode = vnode;
    }

    protected VRSProxyFactory factory() {
        return this.getProxyFactory();
    }

    @Override
    protected boolean doExists() throws ProxyException {
        if (vnode instanceof VFSPath) {
            try {
                return ((VFSPath) vnode).exists(LinkOption.NOFOLLOW_LINKS);
            } catch (VrsException e) {
                throw createProxyException("Couldn't determine whether path exists:" + locator, e);
            }
        } else {
            //vpath.exists() ? 
            return true;
        }
    }

    protected void doPrefetchAttributes() throws ProxyException {
        super.doPrefetchAttributes();
    }

    protected VPath vpath() {
        return vnode;
    }

    @Override
    protected VRSProxyNode doGetParent() throws ProxyException {
        VPath parent;

        try {
            parent = vnode.getParent();
            if (parent == null)
                return null;

            return new VRSProxyNode(this.getProxyFactory(), parent,
                    new VRL(parent.getVRL().toURI()));
        } catch (Exception e) {
            throw createProxyException("Couldn't get parent of:" + locator, e);
        }
    }

    @Override
    protected List<? extends ProxyNode>
    doGetChilds(int offset, int range, LongHolder numChildsLeft) throws ProxyException {
        log.debug("@{}: doGetChilds:{}", this.id, this);

        try {
            VPath targetPath = vnode;

            // check links first: 
            if (isResourceLink()) {
                VRSProxyNode targetNode = (VRSProxyNode) this.resolveResourceLink();
                targetPath = targetNode.vnode;
            }

            if (targetPath.isComposite() == false) {
                return null;
            }

            List<? extends VPath> nodes = targetPath.list();

            return subrange(createNodes(nodes), offset, range);
        } catch (Exception e) {
            throw createProxyException("Couldn't get childs of:" + locator, e);
        }
    }

    protected List<VRSProxyNode> createNodes(List<? extends VPath> nodes) throws ProxyException {
        if (nodes == null) {
            return null;
        }

        int len = nodes.size();

        ArrayList<VRSProxyNode> pnodes = new ArrayList<VRSProxyNode>(len);
        for (int i = 0; i < len; i++) {
            pnodes.add(createNode(nodes.get(i)));
        }
        return pnodes;
    }

    protected VRSProxyNode createNode(VPath node) throws ProxyException {
        try {
            return new VRSProxyNode(factory(), node, new VRL(node.getVRL().toURI()));
        } catch (Exception e) {
            throw createProxyException("Error creating proxy node from:" + node, e);
        }
    }

    @Override
    protected String doGetIconURL(String status, int size) throws ProxyException {
        String url;
        {
            try {
                url = vnode.getIconURL(size);
            } catch (VrsException e) {
                throw new ProxyException(e.getMessage(), e);
            }
        }

        return url;
    }

    @Override
    public VRSProxyFactory getProxyFactory() {
        return (VRSProxyFactory) super.getProxyFactory();
    }

    public String toString() {
        return "<VRSProxyNode:" + getResourceType() + ":" + getVRL();
    }

    @Override
    protected String doGetMimeType() throws ProxyException {
        String mimeType = null;
        try {
            mimeType = vnode.getMimeType();
        } catch (Exception e) {
            throw new ProxyException("Couldn't determine mime type of:" + vnode, e);
        }

        return mimeType;
    }

    @Override
    protected boolean doGetIsComposite() throws ProxyException {
        boolean isComposite;
        try {
            isComposite = vnode.isComposite();
        } catch (VrsException e) {
            throw new ProxyException(e.getMessage(), e);
        }
        return isComposite;
    }

    @Override
    protected List<String> doGetChildTypes() throws ProxyException {
        try {
            return vnode.getChildResourceTypes();
        } catch (VrsException e) {
            throw new ProxyException(e.getMessage(), e);
        }
    }

    // ========================================================================
    // Misc 
    // ========================================================================

    private ProxyException createProxyException(String msg, Exception e) {
        return new ProxyException(msg + "\n" + e.getMessage(), e);
    }

    @Override
    protected String doGetName() {
        return vnode.getName();
    }

    @Override
    protected String doGetResourceType() throws ProxyException {
        try {
            return vnode.getResourceType();
        } catch (VrsException e) {
            throw new ProxyException(e.getMessage(), e);
        }
    }

    @Override
    protected String doGetResourceStatus() throws ProxyException {
        try {
            return this.vnode.getResourceStatus();
        } catch (Exception e) {
            throw createProxyException("Couldn't get status of:" + vnode, e);
        }
    }

    @Override
    protected List<String> doGetAttributeNames() throws ProxyException {
        try {
            return vnode.getAttributeNames();
        } catch (VrsException e) {
            throw new ProxyException(e.getMessage(), e);
        }
    }

    @Override
    protected Map<String, AttributeDescription> doGetAttributeDescriptions(String[] names) throws ProxyException {
        try {
            return vnode.getAttributeDescriptions(names);
        } catch (VrsException e) {
            throw new ProxyException(e.getMessage(), e);
        }
    }

    @Override
    protected List<Attribute> doGetAttributes(String[] names) throws ProxyException {
        try {
            return vnode.getAttributes(names);
        } catch (Exception e) {
            throw new ProxyException("Couldn't get attributes\n" + e.getMessage(), e);
        }
    }

    @Override
    protected void doUpdateAttributes(Attribute[] attrs) throws ProxyException {
        if (this.vnode instanceof VEditable) {
            try {
                ((VEditable) this.vnode).setAttributes(attrs);
            } catch (VrsException e) {
                throw new ProxyException("Couldn't set attributes:" + e.getMessage(), e);
            }
        } else {
            throw new ProxyException("Resource (attributes) arn't editable:" + this.vnode);
        }

    }

    @Override
    protected Presentation doGetPresentation() throws ProxyException {
        if (vnode instanceof IPresentable) {
            return ((IPresentable) vnode).getPresentation();
        }

        String type;
        try {
            type = vnode.getResourceType();
            VRL vrl = vnode.getVRL();
            return VRSPresentation.getPresentationFor(vrl, type, true);
        } catch (VrsException e) {
            throw new ProxyException(e.getMessage(), e);
        }
    }

    @Override
    protected boolean doGetIsEditable() {
        return (this.vnode instanceof VEditable);
    }

    @Override
    protected boolean doIsResourceLink() {
        if (this.vnode instanceof VInfoResource) {
            return ((VInfoResource) vnode).isResourceLink();
        }

        // Race Condition: During prefetch phase this might ocure !  
        if (vnode.getVRL() == null) {
            log.error("@{}: FIXME:getVRL() of vnode is null:{}", this.id, vnode);
            return false;
        }

        // All .vlink AND .rsfx .rslx files are ResourceLinks ! 
        return vnode.getVRL().isVLink() == true;

    }

    @Override
    protected VRL doGetResourceLinkTargetVRL() {
        if (this.vnode instanceof VInfoResource) {
            return ((VInfoResource) vnode).getTargetVRL();
        }
        return null;
    }

    @Override
    protected ProxyNode doCreateNew(String type, String optNewName) throws ProxyException {
        try {
            VPath newPath = vnode.create(type, optNewName);
            return this.createNode(newPath);
        } catch (VrsException e) {
            throw new ProxyException(e.getMessage(), e);
        }
    }

    @Override
    protected void doDelete(boolean recurse, ITaskMonitor optMonitor) throws ProxyException {
        try {
            if (vnode instanceof VFSDeletable) {
                ((VFSDeletable) vnode).delete(recurse, optMonitor);
            } else if (vnode instanceof VDeletable) {
                ((VDeletable) vnode).delete();
            } else {
                throw new ProxyException("Resource can't be deleted:" + this);
            }
        } catch (VrsException e) {
            throw new ProxyException(e.getMessage(), e);
        }
    }

    @Override
    protected ProxyNode doRenameTo(String nameOrNewPath) throws ProxyException {
        if (vnode instanceof VRenamable) {
            try {
                VPath newPath = ((VRenamable) vnode).renameTo(nameOrNewPath);
                return factory().registerVRSProxyNode(newPath);
            } catch (VrsException e) {
                throw new ProxyException(e.getMessage(), e);
            }
        } else {
            throw new ProxyException("Resource can't be renamed:" + vnode);
        }
    }

    @Override
    protected ResourceConfigInfo doGetResourceConfigInfo() throws ProxyException {
        VResourceSystem vrs;
        try {
            if (vnode instanceof VResourceConfigurable) {
                return ((VResourceConfigurable) vnode).getResourceConfigInfo();
            } else {
                vrs = vnode.getResourceSystem();
                return vrs.getVRSContext().getResourceSystemInfoFor(vrs);
            }
        } catch (VrsException e) {
            throw new ProxyException(e.getMessage(), e);
        }
    }

    @Override
    protected ResourceConfigInfo doUpdateResourceConfigInfo(ResourceConfigInfo info)
            throws ProxyException {
        VResourceSystem vrs;
        try {
            if (vnode instanceof VResourceConfigurable) {
                return ((VResourceConfigurable) vnode).updateResourceConfigInfo(info);
            } else {
                vrs = vnode.getResourceSystem();
                return vrs.getVRSContext().getResourceSystemInfoRegistry().putInfo(info);
            }
        } catch (VrsException e) {
            throw new ProxyException(e.getMessage(), e);
        }
    }

}