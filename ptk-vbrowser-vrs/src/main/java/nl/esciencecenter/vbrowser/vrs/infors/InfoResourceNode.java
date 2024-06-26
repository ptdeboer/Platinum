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
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.vbrowser.vrs.VEditable;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.data.*;
import nl.esciencecenter.vbrowser.vrs.data.xml.XMLData;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.io.VDeletable;
import nl.esciencecenter.vbrowser.vrs.io.VRenamable;
import nl.esciencecenter.vbrowser.vrs.io.VStreamAccessable;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Common parent for ResourcFolders, ResourceLinks and the RootInfoNode.<br>
 * Implements the Folder management methods and target resolving of ResourceLinks.
 */
@Slf4j
public class InfoResourceNode extends InfoRSPathNode implements VStreamAccessable, VInfoResourcePath, VRenamable,
        VDeletable, VEditable {

    public static InfoResourceNode createSubPathLinkNode(InfoRSPathNode parent, String subPath, String logicalName,
                                                         VRL targetLink, String optIconURL, boolean showLinkIcon) throws VRLSyntaxException {
        VRL logicalVRL = parent.createSubPathVRL(subPath);
        InfoResourceNode node = new InfoResourceNode(parent, InfoRSConstants.RESOURCELINK, logicalVRL);
        node.setTargetVRL(targetLink);
        node.setIconUrl(optIconURL);
        node.setShowLinkIcon(showLinkIcon);
        node.setLogicalName(logicalName);
        return node;
    }

    public static InfoResourceNode createLinkNode(InfoRSPathNode parent, String logicalName, VRL targetLink,
                                                  boolean targetIsComposite,
                                                  String optIconURL, boolean showLinkIcon) throws VRLSyntaxException {
        VRL logicalVRL = parent.createNewSubNodeVRL();
        InfoResourceNode node = new InfoResourceNode(parent, InfoRSConstants.RESOURCELINK, logicalVRL);
        node.setTargetVRL(targetLink);
        node.setIconUrl(optIconURL);
        node.setShowLinkIcon(showLinkIcon);
        node.setLogicalName(logicalName);
        node.setTargetIsComposite(targetIsComposite);
        return node;
    }

    public static InfoResourceNode createFolderNode(InfoRSPathNode parentNode, String folderName, String optIconURL)
            throws VRLSyntaxException {
        VRL logicalVRL = parentNode.createNewSubNodeVRL();// (folderName);
        InfoResourceNode node = new InfoResourceNode(parentNode, InfoRSConstants.RESOURCEFOLDER, logicalVRL);
        node.setTargetVRL(null);
        node.setIconUrl(optIconURL);
        node.setShowLinkIcon(false);
        node.setLogicalName(folderName);

        return node;
    }

    public static InfoResourceNode createResourceNode(InfoRSPathNode parentNode, String type, AttributeSet infoAttrs)
            throws VrsException {
        VRL logicalVRL = parentNode.createNewSubNodeVRL();
        InfoResourceNode node = new InfoResourceNode(parentNode, type, logicalVRL);
        node.updateAttributes(infoAttrs);
        return node;
    }

    // ==========
    // Instance
    // ==========

    private Presentation customPresentation;

    protected InfoResourceNode(InfoRS infoRS, String type, VRL logicalVRL) {
        super(infoRS, type, logicalVRL);
    }

    protected InfoResourceNode(InfoRSPathNode parent, String type, VRL logicalVRL) {
        super(parent, type, logicalVRL);

        this.setLogicalName(logicalVRL.getBasename());
    }

    protected InfoResourceNode(InfoRSPathNode parent, String type, VRL logicalVRL, VRL targetVRL) {
        super(parent, type, logicalVRL);

        this.setTargetVRL(targetVRL);
        this.setIconUrl(null);
        this.setLogicalName(logicalVRL.getBasename());
    }

    protected void setLogicalName(String name) {
        this.attributes.set(InfoRSConstants.RESOURCE_NAME, name);
    }

    public void setIconUrl(String iconUrl) {
        this.attributes.set(InfoRSConstants.RESOURCE_ICONURL, iconUrl);
    }

    public void setShowLinkIcon(boolean val) {
        this.attributes.set(InfoRSConstants.RESOURCE_SHOWLINKICON, val);
    }

    public boolean getShowLinkIcon(boolean defaultValue) {
        return attributes.getBooleanValue(InfoRSConstants.RESOURCE_SHOWLINKICON, defaultValue);
    }

    @Override
    public String getIconURL(int size) {
        String str = attributes.getStringValue(InfoRSConstants.RESOURCE_ICONURL);
        if (str != null) {
            return str;
        }
        String iconUrl = null;
        if (isResourceFolder()) {
            iconUrl = "icons/infors/vle-world-folder.png";
        } else if (isResourceLink()) {
            if (getTargetIsComposite(true)) {
                iconUrl = "icons/infors/resourcelink-dir.png";
            } else {
                iconUrl = "icons/infors/resourcelink-file.png";
            }
        }
        attributes.set(InfoRSConstants.RESOURCE_ICONURL, iconUrl);
        return iconUrl;

    }

    public String getName() {
        String name = attributes.getStringValue(InfoRSConstants.RESOURCE_NAME);
        if (name == null) {
            VRL targetVrl = this.getTargetVRL();
            if (targetVrl != null) {
                name = "link:" + targetVrl;
            } else {
                name = getVRL().getBasename();
            }
        }
        return name;
    }

    @Override
    public boolean isComposite() {
        if (!isResourceLink()) {
            return super.isComposite();
        }
        return getTargetIsComposite(super.isComposite());
    }

    public VRL getTargetVRL() {
        try {
            return attributes.getVRLValue(InfoRSConstants.RESOURCE_TARGETVRL);
        } catch (VRLSyntaxException e) {
            log.warn("Invalid VRL:{}", e.getMessage());
            return null;
        }
    }

    public void setTargetVRL(VRL vrl) {
        if (vrl == null) {
            attributes.remove(InfoRSConstants.RESOURCE_TARGETVRL);
        } else {
            attributes.set(InfoRSConstants.RESOURCE_TARGETVRL, vrl);
        }
    }

    public void setTargetIsComposite(boolean value) {
        attributes.set(InfoRSConstants.RESOURCE_TARGET_ISCOMPOSITE, value);
    }

    public boolean getTargetIsComposite(boolean defValue) {
        return attributes.getBooleanValue(InfoRSConstants.RESOURCE_TARGET_ISCOMPOSITE, defValue);
    }

    @Override
    public boolean isResourceLink() {
        return InfoRSConstants.RESOURCELINK.equals(getResourceType());
    }

    @Override
    public boolean isResourceFolder() {
        return InfoRSConstants.RESOURCEFOLDER.equals(getResourceType());
    }

    public VPath resolveTarget(VRL vrl) throws VrsException {
        return getInfoRS().getVRSClient().openPath(vrl);
    }

    public List<AttributeDescription> getResourceAttributeDescriptions() {
        ArrayList<AttributeDescription> descs = new ArrayList<AttributeDescription>();
        descs.add(new AttributeDescription(InfoRSConstants.RESOURCE_MIMETYPE, AttributeType.STRING, false,
                "Resource MimeType"));
        descs.add(new AttributeDescription(InfoRSConstants.RESOURCE_NAME, AttributeType.STRING, true,
                "Resource logical name"));
        descs.add(new AttributeDescription(InfoRSConstants.RESOURCE_TARGETVRL, AttributeType.VRL, true,
                "Resource Target VRL"));
        descs.add(new AttributeDescription(InfoRSConstants.RESOURCE_TARGET_ISCOMPOSITE, AttributeType.BOOLEAN, true,
                "Resource Target is Composite"));
        descs.add(new AttributeDescription(InfoRSConstants.RESOURCE_ICONURL, AttributeType.STRING, true,
                "Resource Icon URL"));
        descs.add(new AttributeDescription(InfoRSConstants.RESOURCE_SHOWLINKICON, AttributeType.BOOLEAN, true,
                "Resource show (mini) link icon"));
        return descs;
    }

    public Attribute getResourceAttribute(String name) throws VrsException {
        if (name == null)
            return null;

        Attribute attr = super.getResourceAttribute(name);
        if (attr != null)
            return attr;

        if (name.equals(InfoRSConstants.RESOURCE_TARGETVRL)) {
            attr = AttributeUtil.createVRLAttribute(name, getTargetVRL(), true);
        } else if (name.equals(InfoRSConstants.RESOURCE_TARGET_ISCOMPOSITE)) {
            attr = new Attribute(name, getTargetIsComposite(true));
        } else if (name.equals(InfoRSConstants.RESOURCE_MIMETYPE)) {
            attr = new Attribute(name, getMimeType());
        } else if (name.equals(InfoRSConstants.RESOURCE_SHOWLINKICON)) {
            attr = new Attribute(name, getShowLinkIcon(false));
        } else if (name.equals(InfoRSConstants.RESOURCE_ICONURL)) {
            attr = new Attribute(name, getIconURL(128));
        } else if (name.equals(InfoRSConstants.RESOURCE_NAME)) {
            attr = new Attribute(name, this.getName());
        }

        return attr;
    }

    protected void updateAttributes(AttributeSet attrs) {
        // update attributes:
        for (String key : attrs.keySet()) {
            Attribute attr = attrs.get(key);
            log.debug("updating attribute={}", attr);
            // delegate to AttributeSet:
            this.attributes.update(attr, true);
        }
        // check//assert new attributes
    }

    public String getMimeType() {
        if (isResourceLink()) {
            return InfoRSConstants.RESOURCELINK_MIMETYPE;
        } else if (isResourceFolder()) {
            return InfoRSConstants.RESOURCEFOLDER_MIMETYPE;
        } else {
            // default infors-<ResourceType> mime-type;
            return super.getMimeType();
        }
    }

    public List<String> getChildResourceTypes() {
        if (this.isResourceFolder()) {
            return defaultFolderChildTypes;
        } else if (this.isResourceLink()) {
            // resolve target Child Types ?
        }
        return null;
    }

    // ======================================
    // Presentation
    // ======================================

    public void setPresentation(Presentation presentation) {
        this.customPresentation = presentation;
    }

    public Presentation getPresentation() {
        return customPresentation;
    }

    // ======================================
    // Stream Read/Write Methods (load/save)
    // ======================================

    public OutputStream createOutputStream(boolean append) throws VrsException {
        // todo: create from xml stream
        throw new VrsException("Not now");
    }

    public InputStream createInputStream() throws VrsException {
        String xml = new XMLData(this.getVRSContext()).toXML(this);
        ByteArrayInputStream binps;
        binps = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        return binps;
    }

    // ======================================
    // Create/Delete Links/ResourceFolders
    // ======================================

    public InfoResourceNode addResourceLink(String folderName, String logicalName, VRL targetLink, boolean targetIsComposite,
                                            String optIconURL, boolean save) throws VrsException {
        log.debug(">>>Adding new resourceLink:{}", targetLink);

        InfoRSPathNode parentNode;

        if (folderName != null) {
            parentNode = this.getSubNodeByName(folderName);
            if (parentNode == null) {
                parentNode = this.createResourceFolder(folderName, null);
            }
        } else {
            parentNode = this;
        }

        InfoResourceNode node = InfoResourceNode.createLinkNode(parentNode, logicalName, targetLink, targetIsComposite,
                optIconURL, true);
        parentNode.addSubNode(node);

        if (save) {
            save();
        }

        return node;
    }

    protected InfoResourceNode createResourceFolder(String folderName, String optIconURL) throws VrsException {
        InfoRSPathNode node = this.getSubNodeByName(folderName);

        if (node instanceof InfoResourceNode) {
            return (InfoResourceNode) node;
        } else if (node != null) {
            throw new VrsException("Type Mismatch: InfoRSNode name'" + folderName
                    + "' already exists, but is not a InfoResourceNode:" + node);
        } else {
            InfoResourceNode folder = InfoResourceNode.createFolderNode(this, folderName, optIconURL);
            this.addPersistantNode(folder, true);
            return folder;
        }
    }

    @Override
    public InfoResourceNode create(String type, String name) throws VrsException {
        return this.create(type, name, false);
    }

    public InfoResourceNode create(String type, String name, boolean targetIsComposite) throws VrsException {
        if (StringUtil.equals(type, InfoRSConstants.RESOURCEFOLDER)) {
            return this.createFolder(name);
        } else if (StringUtil.equals(type, InfoRSConstants.RESOURCELINK)) {
            // create link with dummy VRL;
            VRL vrl = this.resolveVRL("NewLink");
            return this.createResourceLink(vrl, name, targetIsComposite);
        } else {
            throw new VrsException("Resource type not supported:" + type);
        }
    }

    @Override
    public InfoResourceNode createFolder(String name) throws VrsException {
        return createResourceFolder(name, null);
    }

    @Override
    public InfoResourceNode createResourceLink(VRL targetVRL, String logicalName, boolean targetIsComposite) throws VrsException {
        InfoResourceNode subNode = InfoResourceNode.createLinkNode(this, logicalName, targetVRL, targetIsComposite, null, true);
        addPersistantNode(subNode, true);
        return subNode;
    }

    // =================
    // Rename/Delete
    // ================

    @Override
    public InfoResourceNode renameTo(String newName) throws VrsException {
        this.setLogicalName(newName);
        save();
        // name change doesn't change path
        return this;
    }

    @Override
    public boolean delete() throws VrsException {
        // keep root node before delete, after delete parent is gone; 
        InfoRootNode rootNode = this.getRootNode();
        // delegate to parent
        parent = getParent();
        if (parent != null) {
            parent.delSubNode(this);
        } else {
            log.error("Received delete for parentless node:{}", this);
        }
        save();
        return true;
    }

    // ======================================
    // SubNode persistant interface.
    // ======================================

    @Override
    public InfoResourceNode createSubNode(String type, AttributeSet infoAttributes) throws VrsException {
        InfoResourceNode subNode = InfoResourceNode.createResourceNode(this, type, infoAttributes);
        this.addPersistantNode(subNode, true);
        return subNode;
    }

    protected void addPersistantNode(InfoRSPathNode subNode, boolean save) throws VrsException {
        addSubNode(subNode);
        if (save) {
            save();
        }
    }

    // ====================
    // Persistant interface
    // ====================

    /**
     * If context is persistent, save InfoNodes.
     */
    protected void save() {
        //
        if (getVRSContext().hasPersistantConfig() == false) {
            log.debug("Not saving InfoNode. Non persistent context");
            return;
        }
        // delegete to parent
        InfoRootNode rootNode = this.getRootNode();
        if (rootNode == null) {
            log.warn("save(): Can not save InfoNode: No RootNode!");
        } else {
            rootNode.save();
        }
    }

    @Override
    public boolean setAttributes(Attribute[] attrs) {
        for (Attribute attr : attrs) {
            this.setAttribute(attr, false);
        }
        this.save();
        return true;
    }

    @Override
    public boolean setAttribute(Attribute attr) {
        return this.setAttribute(attr, true);
    }

    public boolean setAttribute(Attribute attr, boolean save) {
        log.debug("setAttribute():{}", attr);
        this.attributes.put(attr);
        if (save) {
            this.save();
        }
        return true;
    }

}
