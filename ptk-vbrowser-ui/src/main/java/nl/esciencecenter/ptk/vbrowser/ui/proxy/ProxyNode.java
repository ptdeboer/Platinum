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

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.data.LongHolder;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.ui.icons.IconProvider;
import nl.esciencecenter.ptk.vbrowser.ui.model.UIViewModel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.data.AttributeNames;
import nl.esciencecenter.vbrowser.vrs.event.VRSEventNotifier;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ProxyNode is abstract interface to Resource Nodes. It represents a "Proxy" of the actual viewed
 * Node.
 */
@Slf4j
public abstract class ProxyNode {

    private static int idCounter = 10000;

    // ========================================================================
    // helpers
    // ========================================================================

    // Null pointer safe toArray() method.
    public static ProxyNode[] toArray(List<? extends ProxyNode> nodes) {
        if (nodes == null)
            return null;

        return nodes.toArray(new ProxyNode[0]);
    }

    // Null pointer safe toArray() method.
    public static VRL[] toVRLArray(List<? extends ProxyNode> nodes) {
        if (nodes == null)
            return null;

        VRL[] vrls = new VRL[nodes.size()];

        for (int i = 0; i < nodes.size(); i++) {
            vrls[i] = nodes.get(i).getVRL();
        }
        return vrls;
    }

    /*
     * Get subrange of array nodes [offset:offset+range]
     */
    public static List<? extends ProxyNode> subrange(List<? extends ProxyNode> nodes, int offset,
                                                     int range) {
        // no change:
        if ((offset <= 0) && (range < 0))
            return nodes;

        if (offset < 0)
            throw new Error(
                    "subrange(): Parameter 'offset' can't be negative. Use 0 for 'don't care'");

        if (nodes == null)
            return null;

        int len = nodes.size();

        // no more nodes after len:
        if (offset >= len)
            return null;

        // range=-1 means all
        if (range < 0)
            range = len;

        // overflow, requested more then there are.
        if (offset + range > len)
            range = len - offset; // remainder

        if (range == 0)
            return null;

        // Assert: 0 <= offset < len
        // Assert: 0 <= range <= (len - offset)

        ArrayList<ProxyNode> subnodes = new ArrayList<ProxyNode>(range);
        for (int i = 0; i < range; i++)
            subnodes.add(nodes.get(offset + i));
        return subnodes;
    }

    public static int newID() {
        return idCounter++;
    }

    // ========================================================================
    // Cache !
    // ========================================================================

    /**
     * Cache for Object attributes !
     */
    public static class Cache {

        // Core fields, prefetch for optimized results.
        protected String name = null;
        protected Boolean is_composite = null;
        protected String mime_type = null;
        protected String resource_type = null;
        protected String resource_status = null;

        // Hierarchy: Parent, Childs:
        protected ProxyNode parent = null;
        protected List<? extends ProxyNode> childNodes = null;
        protected List<String> childTypes = null;
        protected long getChildsTime = -1;

        // resource links
        protected Boolean isResourceLink = null;
        protected VRL resourceLinkTargetVrl;
        protected ProxyNode linkResolvedNode;

        protected Cache() {
        }

        public void setName(String newName) {
            this.name = newName;
        }

        public void setResourceType(String value) {
            this.resource_type = value;
        }

        public void setIsComposite(boolean val) {
            this.is_composite = val;
        }

        public void setMimeType(String mimeType) {
            this.mime_type = mimeType;
        }

        public String getMimeType() {
            return this.mime_type;
        }

        public boolean getIsComposite() {
            return this.is_composite;
        }
    }

    public void clearCache() {
        this.cache = new Cache();
    }

    // ========================================================================
    // Instance.
    // ========================================================================

    protected final int id;
    protected final VRL locator;
    protected ProxyFactory proxyFactory;
    protected Cache cache = new Cache(); // default empty

    protected ProxyNode(ProxyFactory factory, VRL proxyVrl) {
        id = newID();
        this.locator = proxyVrl;
        this.proxyFactory = factory;
        log.trace("@{}: new ProxyNode():{}", this.id, proxyVrl);
    }

    protected ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public int getID() {
        return id;
    }

    public VRL getVRL() {
        return locator;
    }

    /**
     * Uncached method. Check whether resource really exists.
     */
    public boolean exists() throws ProxyException {
        return doExists();
    }

    /**
     * Called by ProxyNode factory to prefill core attributes. Subclass can extend this method to
     * prefetch attributes during the initialization of this Node.
     */
    protected void doPrefetchAttributes() throws ProxyException {
        // selection of important attributes.
        synchronized (cache) {
            // update name.
            this.cache.name = doGetName();

            try {
                this.cache.is_composite = doGetIsComposite();
            } catch (Exception e) {
                handle("Couldn't prefetch isComposite().", e);
            }

            try {
                this.cache.resource_type = doGetResourceType();
            } catch (Exception e) {
                handle("Couldn't prefetch resourceType.", e);
            }

            // might be null anyway
            try {
                this.cache.resource_status = doGetResourceStatus();
            } catch (Exception e) {
                handle("Couldn't prefetch resourceStatus.", e);
            }

            // might be null anyway.
            try {
                this.cache.mime_type = doGetMimeType();
            } catch (Exception e) {
                handle("Couldn't prefetch mimeType.", e);
            }

            try {
                this.cache.childTypes = doGetChildTypes();
            } catch (Exception e) {
                handle("Couldn't prefetch childTypes.", e);
            }

            try {
                this.cache.isResourceLink = doIsResourceLink();
            } catch (Exception e) {
                handle("Couldn't prefetch childTypes.", e);
            }
        }
    }

    private void handle(String message, Exception e) {
        this.getProxyFactory().handleException(message, e);
    }

    public Icon getIcon(UIViewModel model, boolean greyOut, boolean focus) throws ProxyException {
        return getIcon(model.getIconSize(), greyOut, focus);
    }

    public Icon getIcon(int size, boolean greyOut, boolean focus) throws ProxyException {
        IconProvider provider = this.getProxyFactory().getPlatform().getIconProvider();
        String mimeType = this.getMimeType();
        String iconUrl = this.getIconURL(getResourceStatus(), size);
        boolean isLink = this.isResourceLink();
        return provider.createDefaultIcon(iconUrl, this.isComposite(), isLink, mimeType, size,
                greyOut, focus);
    }

    public String getName() // no throw: name should already be fetched
    {
        if (this.cache.name == null) {
            log.debug("@{}: Optimization: getName() not prefetched.", this.id);
            try {
                this.cache.name = doGetName();
            } catch (ProxyException e) {
                handle("Method getName() Failed", e);
                this.cache.name = getVRL().getBasename();
            }
        }

        return this.cache.name;
    }

    public String getResourceStatus() {
        // could be prefetched or not. NULL could also mean no status info.
        return this.cache.resource_status;
    }

    public String getMimeType() throws ProxyException {
        if (this.cache.mime_type == null) {
            log.debug("@{}: Optimization: getMimeType() not prefetched.", this.id);
            this.cache.mime_type = doGetMimeType();
        }

        return this.cache.mime_type;
    }

    /**
     * Get Icon used for specified status and optional a prerender icon matching the specified size
     *
     * @param status Optional status attribute
     * @param size   Desired size
     * @return
     * @throws ProxyException
     */
    public String getIconURL(String status, int size) throws ProxyException {
        return this.doGetIconURL(status, size);
    }

    public boolean hasChildren() throws ProxyException {
        List<? extends ProxyNode> childs = this.getChilds();
        return (childs != null) && (childs.size() > 0);
    }

    public ViewNode createViewItem(UIViewModel model) throws ProxyException {
        // default
        Icon defaultIcon = getIcon(model, false, false);
        ViewNode viewNode = new ViewNode(locator, defaultIcon, getName(), isComposite());
        viewNode.setResourceType(this.getResourceType());
        viewNode.setMimeType(this.getMimeType());
        viewNode.setResourceStatus(this.getResourceStatus());
        // other
        viewNode.setIcon(ViewNode.FOCUS_ICON, getIcon(model, false, true));
        viewNode.setIcon(ViewNode.SELECTED_ICON, getIcon(model, true, false));
        viewNode.setIcon(ViewNode.SELECTED_FOCUS_ICON, getIcon(model, true, true));
        viewNode.setChildTypes(this.getChildTypes());
        return viewNode;
    }

    public boolean hasLocator(VRL locator) {
        return this.locator.equals(locator);
    }

    public List<? extends ProxyNode> getChilds() throws ProxyException {
        return getChilds(0, -1, null);
    }

    // ========================================================================
    // Cached methods
    // ========================================================================

    public List<? extends ProxyNode> getChilds(int offset, int range, LongHolder numChildsLeft)
            throws ProxyException {
        boolean autoResolve = false;

        // auto resolve LinkNodes: 
        ProxyNode targetNode = this;
        if ((autoResolve) && (this.isResourceLink())) {
            targetNode = this.resolveResourceLink();
        }

        synchronized (this.cache) {
            if (cache.childNodes == null) {
                List<? extends ProxyNode> childs = targetNode.doGetChilds(offset, range,
                        numChildsLeft);

                if ((offset > 0) || (range > 0)) {
                    // todo: update ranged childs into cache, but typically
                    // ranged results
                    // are used in the case the actual child list is to big or
                    // the invoker
                    // caches the data itself.
                    return childs; // do not cache ranged results!
                }

                // only cache complete results!
                cache.childNodes = childs;
                cache.getChildsTime = System.currentTimeMillis();

                if (cache.childNodes != null) {
                    for (ProxyNode child : cache.childNodes) {
                        child.doPrefetchAttributes();
                    }
                }
            }

            return cache.childNodes;
        }
    }

    public ProxyNode getParent() throws ProxyException {
        synchronized (this.cache) {
            if (cache.parent == null) {
                cache.parent = doGetParent();
            }

            return cache.parent;
        }
    }

    public VRL getParentLocation() throws ProxyException {
        ProxyNode parent = getParent();

        if (parent != null)
            return parent.getVRL();

        return null;
    }

    /**
     * Returns (cached) ResourceType value. Should not throw exception since attribute must be known
     * at creation time.
     *
     * @return
     */
    public String getResourceType() {
        if ((this.cache == null) || (this.cache.resource_type == null)) {
            log.debug("@{}: Optimization: getResourceType() not prefetched.", this.id);

            try {
                this.cache.resource_type = doGetResourceType();
            } catch (ProxyException e) {
                handle("getResourceType()", e);
            }
        }
        return this.cache.resource_type;
    }

    /**
     * Returns (cached) isComposite value. Should not throw exception since attribute must be known
     * at creation time.
     *
     * @return
     */
    public boolean isComposite() {
        if (this.cache.is_composite == null) {
            log.debug("@{}: Optimization: isComposite() not prefetched.", this.id);
            try {
                this.cache.is_composite = doGetIsComposite();
            } catch (Exception e) {
                handle("isComposite()", e);
                return true;
            }
        }

        return this.cache.is_composite;
    }

    public boolean isEditable() {
        return this.doGetIsEditable();
    }

    public List<String> getChildTypes() {
        if (this.cache.childTypes == null) {
            try {
                this.cache.childTypes = doGetChildTypes();
            } catch (ProxyException e) {
                handle("getCreateTypes", e);
            }
        }

        return this.cache.childTypes;
    }

    public List<String> getAttributeNames() throws ProxyException {
        List<String> names = doGetAttributeNames();

        if (names != null) {
            return names;
        }

        return getDefaultProxyAttributesNames();
    }

    public List<Attribute> getAttributes(List<String> attrNames) throws ProxyException {
        if (attrNames == null)
            return null;

        // todo List<String> vs String array[]
        return getAttributes(attrNames.toArray(new String[0]));
    }

    public void updateAttributes(Attribute[] attrs) throws ProxyException {
        this.doUpdateAttributes(attrs);
    }

    public Map<String, AttributeDescription> getAttributeDescriptions(String[] attrNames) throws ProxyException {
        if (attrNames == null)
            return null;

        // todo List<String> vs String array[]
        return doGetAttributeDescriptions(attrNames);
    }

    public List<Attribute> getAttributes(String[] names) throws ProxyException {
        List<Attribute> attrs = doGetAttributes(names);

        if (attrs != null)
            return attrs;

        return getDefaultProxyAttributes(names);
    }

    // ============
    // Presentation
    // ============

    public Presentation getPresentation() {
        try {
            return doGetPresentation();
        } catch (ProxyException e) {
            // Default Presentation!
            log.error("@{}: FIXME: Could not get presentation. Need default Presentation!", this.id);
            e.printStackTrace();
            return null;
        }
    }

    protected List<String> getDefaultProxyAttributesNames() {
        return new StringList(AttributeNames.ATTR_ICON, AttributeNames.ATTR_NAME,
                AttributeNames.ATTR_RESOURCE_TYPE, AttributeNames.ATTR_URI,
                AttributeNames.ATTR_MIMETYPE);
    }

    protected List<Attribute> getDefaultProxyAttributes(String[] names) throws ProxyException {
        List<Attribute> attrs = new ArrayList<Attribute>();

        // hard coded default attributes:
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (name.equals(AttributeNames.ATTR_ICON))
                attrs.add(new Attribute(name, this.getIconURL(this.getResourceStatus(), 48)));
            else if (name.equals(AttributeNames.ATTR_NAME))
                attrs.add(new Attribute(name, this.getName()));
            else if (name.equals(AttributeNames.ATTR_URI))
                attrs.add(new Attribute(name, this.getVRL()));
            else if (name.equals(AttributeNames.ATTR_RESOURCE_TYPE))
                attrs.add(new Attribute(name, this.getResourceType()));
            else if (name.equals(AttributeNames.ATTR_MIMETYPE))
                attrs.add(new Attribute(name, this.getMimeType()));
        }
        return attrs;
    }

    public ResourceConfigInfo getResourceConfigInfo() throws ProxyException {
        return this.doGetResourceConfigInfo();
    }

    public ResourceConfigInfo updateResourceConfigInfo(ResourceConfigInfo info)
            throws ProxyException {
        return this.doUpdateResourceConfigInfo(info);
    }

    public String toString() {
        return "<ProxyNode:" + getResourceType() + ":" + getVRL();
    }

    public ProxyNode resolveResourceLink() throws ProxyException {
        if (isResourceLink() == false) {
            return null;
        }

        if (this.cache.linkResolvedNode == null) {
            VRL vrl = this.getResourceLinkVRL();
            if (vrl == null) {
                throw new ProxyException("ResourceLink has NULL Target location:" + this);
            }
            ProxyNode node = proxyFactory.openLocation(vrl);
            this.cache.linkResolvedNode = node;
        }
        return cache.linkResolvedNode;
    }

    public boolean isResourceLink() {
        synchronized (cache) {
            if (this.cache.isResourceLink == null) {
                this.cache.isResourceLink = doIsResourceLink();
            }
        }
        return cache.isResourceLink;
    }

    public VRL getResourceLinkVRL() throws ProxyException {
        synchronized (cache) {
            if (this.cache.resourceLinkTargetVrl == null) {
                this.cache.resourceLinkTargetVrl = doGetResourceLinkTargetVRL();
            }
        }
        return cache.resourceLinkTargetVrl;
    }

    public ProxyNode createNew(String type, String name) throws ProxyException {
        ProxyNode node = doCreateNew(type, name);
        return node;
    }

    final public void delete(boolean recursive, ITaskMonitor optMonitor) throws ProxyException {
        doDelete(recursive, optMonitor);
        this.proxyFactory.cacheRemove(this);
    }

    public ProxyNode renameTo(String newName) throws ProxyException {
        return doRenameTo(newName);
    }

    public VRSEventNotifier getProxyNodeEventNotifier() {
        return proxyFactory.getProxyNodeEventNotifier();
    }

    public void dispose() {
        log.debug("@{}: dispose() for: <{}>:{}", this.id, this.getResourceType(), this.getVRL());
    }

    // ========================================================================
    // Protected implementation interface !
    // ========================================================================

    abstract protected boolean doExists() throws ProxyException;

    abstract protected String doGetName() throws ProxyException;

    abstract protected String doGetResourceType() throws ProxyException;

    abstract protected String doGetResourceStatus() throws ProxyException;

    abstract protected String doGetMimeType() throws ProxyException;

    abstract protected String doGetIconURL(String status, int size) throws ProxyException;

    // =====================
    // VRS Internal
    // =====================

    abstract protected ResourceConfigInfo doGetResourceConfigInfo() throws ProxyException;

    abstract protected ResourceConfigInfo doUpdateResourceConfigInfo(ResourceConfigInfo info)
            throws ProxyException;

    // ============================
    // Child/Composite Attributes
    // ============================

    abstract protected boolean doGetIsComposite() throws ProxyException;

    /**
     * Uncached doGetChilds, using optional range
     */
    protected abstract List<? extends ProxyNode> doGetChilds(int offset, int range,
                                                             LongHolder numChildsLeft) throws ProxyException;

    /**
     * Uncached doGetParent()
     */
    abstract protected ProxyNode doGetParent() throws ProxyException;

    /**
     * Resource Type this node can create/contain.
     */
    abstract protected List<String> doGetChildTypes() throws ProxyException;

    // ===========
    // Attributes
    // ===========

    abstract protected List<String> doGetAttributeNames() throws ProxyException;

    abstract protected List<Attribute> doGetAttributes(String[] names) throws ProxyException;

    abstract protected void doUpdateAttributes(Attribute[] attrs) throws ProxyException;

    abstract protected Map<String, AttributeDescription> doGetAttributeDescriptions(String[] names) throws ProxyException;

    abstract protected Presentation doGetPresentation() throws ProxyException;

    abstract protected boolean doGetIsEditable();

    // ===================
    // VLink//ResourceLink
    // ===================

    abstract protected boolean doIsResourceLink();

    abstract protected VRL doGetResourceLinkTargetVRL() throws ProxyException;

    // =====================
    // Create/Delete/Rename
    // =====================

    abstract protected ProxyNode doCreateNew(String type, String optNewName) throws ProxyException;

    abstract protected void doDelete(boolean recurse, ITaskMonitor optMonitor) throws ProxyException;

    abstract protected ProxyNode doRenameTo(String nameOrNewPath) throws ProxyException;

}
