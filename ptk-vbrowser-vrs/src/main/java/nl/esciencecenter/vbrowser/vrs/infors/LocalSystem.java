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

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.io.FSPath;
import nl.esciencecenter.ptk.io.FSUtil;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsIOException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixed LocalSystem node.
 */
public class LocalSystem extends InfoRSPathNode {

    protected FSUtil fsUtil = null;

    private InfoResourceNode homeNode;

    public LocalSystem(InfoRootNode infoRootNode) throws VrsException {
        super(infoRootNode, InfoRSConstants.LOCALSYSTEM, InfoRS.createPathVRL(InfoRSConstants.LOCALSYSTEM));
        fsUtil = FSUtil.fsutil();
        initChilds();
    }

    public String getIconURL(int size) {
        return "icons/infors/system-128.png";
    }

    protected void initChilds() throws VrsException {
        initSubNodes();
        initHome();
        initDrives();
    }

    public InfoResourceNode getHomeNode() throws VrsException {
        if (this.homeNode == null) {
            homeNode = initHome();
        }
        return homeNode;
    }

    protected InfoResourceNode initHome() throws VrsException {
        FSPath home;
        try {
            home = fsUtil.getUserHomeDir();
        } catch (IOException e) {
            throw new VrsIOException(e.getMessage(), e);
        }

        URI uri = home.toURI();
        VRL vrl = new VRL(uri);
        String name = vrl.getPath();
        String subPath = "Home";
        homeNode = createSubPathLinkNode(subPath, vrl, name, "icons/infors/home_folder-48.png");
        this.addSubNode(homeNode);
        return homeNode;
    }

    protected void initDrives() throws VrsException {
        List<FSPath> roots;
        roots = fsUtil.listRoots();
        int index = 0;

        for (FSPath root : roots) {
            URI uri = root.toURI();
            VRL vrl = new VRL(uri);

            String name = vrl.getPath();

            String subPath = "Root " + index++;

            this.addSubNode(createSubPathLinkNode(subPath, vrl, name, "icons/infors/hdd_mount-128.png"));
        }
    }

    protected InfoResourceNode createSubPathLinkNode(String subPath, VRL targetVrl, String name, String iconUrl)
            throws VRLSyntaxException {
        InfoResourceNode node = InfoResourceNode.createSubPathLinkNode(this, subPath, name, targetVrl, iconUrl, true);
        return node;
    }

    public Map<String, AttributeDescription> getAttributeDescriptions() throws VrsException {
        Map<String, AttributeDescription> descs = super.getAttributeDescriptions();
        Map<String, AttributeDescription> resourceAttrs = getResourceAttrDescriptions();
        descs.putAll(resourceAttrs);

        return descs;
    }

    public Map<String, AttributeDescription> getResourceAttrDescriptions() {
        LinkedHashMap<String, AttributeDescription> descs = new LinkedHashMap<String, AttributeDescription>();

        descs.put(InfoRSConstants.LOCALSYSTEM_OSNAME, new AttributeDescription(InfoRSConstants.LOCALSYSTEM_OSNAME,
                AttributeType.STRING, false, "LocalSystem OS Type"));
        descs.put(InfoRSConstants.LOCALSYSTEM_OSVERSION, new AttributeDescription(
                InfoRSConstants.LOCALSYSTEM_OSVERSION, AttributeType.STRING, false, "LocalSystem OS Version"));
        descs.put(InfoRSConstants.LOCALSYSTEM_OSARCH, new AttributeDescription(InfoRSConstants.LOCALSYSTEM_OSARCH,
                AttributeType.STRING, false, "LocalSystem Architecture"));
        descs.put(InfoRSConstants.LOCALSYSTEM_HOMEDIR, new AttributeDescription(InfoRSConstants.LOCALSYSTEM_HOMEDIR,
                AttributeType.STRING, false, "LocalSystem user home directory"));
        descs.put(InfoRSConstants.LOCALSYSTEM_JAVAHOME, new AttributeDescription(InfoRSConstants.LOCALSYSTEM_JAVAHOME,
                AttributeType.STRING, false, "LocalSystem JRE home"));
        descs.put(InfoRSConstants.LOCALSYSTEM_JAVAVERSION, new AttributeDescription(
                InfoRSConstants.LOCALSYSTEM_JAVAVERSION, AttributeType.STRING, false, "LocalSystem JRE Version"));
        return descs;
    }

    public Attribute getResourceAttribute(String name) throws VrsException {
        if (name == null) {
            return null;
        }

        Attribute attr = super.getResourceAttribute(name);
        if (attr != null) {
            return attr;
        }

        if (name.equals(InfoRSConstants.LOCALSYSTEM_OSNAME)) {
            attr = new Attribute(name, GlobalProperties.getOsName());
        } else if (name.equals(InfoRSConstants.LOCALSYSTEM_OSARCH)) {
            attr = new Attribute(name, GlobalProperties.getOsArch());
        } else if (name.equals(InfoRSConstants.LOCALSYSTEM_OSVERSION)) {
            attr = new Attribute(name, GlobalProperties.getOsVersion());
        } else if (name.equals(InfoRSConstants.LOCALSYSTEM_JAVAHOME)) {
            attr = new Attribute(name, GlobalProperties.getJavaHome());
        } else if (name.equals(InfoRSConstants.LOCALSYSTEM_JAVAVERSION)) {
            attr = new Attribute(name, GlobalProperties.getJavaVersion());
        } else if (name.equals(InfoRSConstants.LOCALSYSTEM_HOMEDIR)) {
            attr = new Attribute(name, GlobalProperties.getGlobalUserHome());
        }

        return attr;
    }

}
