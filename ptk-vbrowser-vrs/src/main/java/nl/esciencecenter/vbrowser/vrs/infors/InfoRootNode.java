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
import nl.esciencecenter.ptk.util.ContentReader;
import nl.esciencecenter.ptk.util.ContentWriter;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VRS;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.data.xml.XMLData;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Root Resource to start browsing from.<br>
 */
@Slf4j
public class InfoRootNode extends InfoResourceNode {

    protected InfoRS infors;
    protected LocalSystem localSystem;
    protected InfoConfigNode configNode;
    protected boolean autoSaveConfig = true;

    public InfoRootNode(InfoRS infoRS) throws VrsException {
        super(infoRS, InfoRSConstants.INFOSYSTEMROOTNODE, new VRL(VRS.INFORS_SCHEME, null, 0, "/"));
        infors = infoRS;
        init();
    }

    protected void init() throws VrsException {
        initChilds();
    }

    protected void initChilds() throws VrsException {
        this.subNodes.clear();

        this.addSubNode(getConfigNode());
        this.addSubNode(getLocalSystem());
    }

    /**
     * Root node has top level recursive find node method.
     */
    public InfoRSPathNode findNode(VRL vrl) throws VrsException {
        String[] paths = vrl.getPathElements();

        if (paths == null) {
            return this;
        }

        int n = paths.length;

        if (n == 0) {
            return this;
        }

        if (n > 0) {
            InfoRSPathNode node = this.findSubNode(vrl, true);

            if (node != null) {
                return node;
            }
        }

        throw new VrsException("Node not found:" + vrl);
    }

    protected LocalSystem getLocalSystem() throws VrsException {
        if (localSystem == null) {
            initLocalSystem();
        }

        return localSystem;
    }

    protected InfoConfigNode getConfigNode() throws VrsException {
        if (configNode == null) {
            initConfigNode();
        }

        return configNode;
    }

    protected void initLocalSystem() throws VrsException {
        localSystem = new LocalSystem(this);
    }

    protected void initConfigNode() throws VrsException {
        configNode = new InfoConfigNode(this);
    }

    public List<String> getChildResourceTypes() {
        // Root Node support default InfoRS types:
        return defaultFolderChildTypes;
    }

    @Override
    public boolean isResourceLink() {
        return false;
    }

    @Override
    public VRL getTargetVRL() {
        return null;
    }

    @Override
    public boolean isResourceFolder() {
        return true;
    }

    // ========================================================================
    // Persistant
    // ========================================================================

    /**
     * Only root info node can be saved to XML.
     *
     * @return persistant Info Nodes as XML String.
     */
    public String toXML() throws VrsException {
        XMLData xmlData = new XMLData(this.getVRSContext());
        String xml = xmlData.toXML(this);
        return xml;
    }

    public VRL getPersistantConfigVRL() {
        VRL configVrl = this.getVRSContext().getPersistantConfigLocation();
        if (configVrl == null) {
            log.error("Persistant configuration enabled, but no persistant save location defined");
            return null;
        }

        VRL rootConfigVrl = configVrl.appendPath("infors.rsfx");
        return rootConfigVrl;
    }

    protected void save() {
        // check autosave
        if ((this.getVRSContext().hasPersistantConfig() == false) || (autoSaveConfig == false)) {
            log.debug("save():hasPersistantConfig=False");
            return;
        }

        VRL saveVrl = getPersistantConfigVRL();

        try {
            saveTo(saveVrl);
            log.debug("Saved to {}", saveVrl);
        } catch (VrsException e) {
            log.error("Failed to save RootNode:{} to:{}", this, saveVrl);
            log.error(e.getMessage(), e);
        }
    }

    protected void saveTo(VRL configVrl) throws VrsException {
        log.debug("Saving InfoRootNode to:{}", configVrl);

        VRSClient vrsClient = this.infors.getVRSClient();
        try {
            String xml = toXML();
            xml = XMLData.prettyFormat(xml, 3);

            VFSPath path = vrsClient.openVFSPath(configVrl);
            VFSPath dir = path.getParent();

            if (dir.exists() == false) {
                log.debug("Creating new config dir:{}", dir);
                dir.mkdirs(true);
            }
            try (OutputStream outps = vrsClient.createOutputStream(path, false)) {
                new ContentWriter(outps, false).write(xml);
            }

        } catch (Exception e) {
            throw new VrsException(e.getMessage(), e);
        }
    }

    protected void load() {
        if (this.getVRSContext().hasPersistantConfig() == false) {
            return;
        }

        VRL loadVrl = getPersistantConfigVRL();

        try {
            loadFrom(loadVrl);
        } catch (VrsException e) {
            log.error("Failed to save RootNode:{} to:{}", this, loadVrl);
            log.error(e.getMessage(), e);
        }
    }

    protected void loadFrom(VRL loadVrl) throws VrsException {
        VRSClient vrsClient = this.infors.getVRSClient();

        try {
            VFSPath path = vrsClient.openVFSPath(loadVrl);
            if (path.exists() == false) {
                log.debug("Root config XML file not found:{}", loadVrl);
                return;
            }
            String xml;
            try (InputStream inps = vrsClient.createInputStream(path)) {
                xml = new ContentReader(inps).readString();
            }
            XMLData data = new XMLData(this.getVRSContext());
            data.addXMLResourceNodesTo(this, xml);

        } catch (Exception e) {
            throw new VrsException("Failed to load config from:" + loadVrl + ".\n" + e.getMessage(), e);
        }
    }

    public void loadPersistantConfig() {
        this.load();
    }

}
