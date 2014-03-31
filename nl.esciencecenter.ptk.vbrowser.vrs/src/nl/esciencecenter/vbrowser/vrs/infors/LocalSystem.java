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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.io.FSNode;
import nl.esciencecenter.ptk.io.FSUtil;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Fixed LocalSystem node.
 */
public class LocalSystem extends InfoRSNode
{
    protected FSUtil fsUtil = null;

    private InfoResourceNode homeNode;

    public LocalSystem(InfoRootNode infoRootNode) throws VrsException
    {
        super(infoRootNode, InfoRSConstants.LOCALSYSTEM, InfoRS.createPathVRL(InfoRSConstants.LOCALSYSTEM));
        fsUtil = FSUtil.getDefault();
        initChilds();
    }

    public String getIconURL(int size)
    {
        return "info/system-128.png";
    }

    protected void initChilds() throws VrsException
    {
        initSubNodes();
        initHome();
        initDrives();
    }

    public InfoResourceNode getHomeNode() throws VrsException
    {
        if (this.homeNode == null)
        {
            homeNode = initHome();
        }
        return homeNode;
    }

    protected InfoResourceNode initHome() throws VrsException
    {
        FSNode home = fsUtil.getUserHomeDir();

        URI uri = home.getURI();
        VRL vrl = new VRL(uri);
        String name = vrl.getPath();
        String subPath = "Home";
        homeNode = createSubPathLinkNode(subPath, vrl, name, "info/home_folder-48.png");
        this.addSubNode(homeNode);
        return homeNode;
    }

    protected void initDrives() throws VrsException
    {
        List<FSNode> roots = fsUtil.listRoots();

        int index = 0;

        for (FSNode root : roots)
        {
            URI uri = root.getURI();
            VRL vrl = new VRL(uri);

            String name = vrl.getPath();

            String subPath = "Root " + index++;

            this.addSubNode(createSubPathLinkNode(subPath, vrl, name, "info/hdd_mount-128.png"));
        }
    }

    protected InfoResourceNode createSubPathLinkNode(String subPath, VRL targetVrl, String name, String iconUrl) throws VRLSyntaxException
    {
        VRL logicalVrl = this.createSubPathVRL(subPath);

        InfoLinkNode node = new InfoLinkNode(this, logicalVrl, targetVrl);
        node.setLogicalName(name);
        node.setIconUrl(iconUrl);
        return node;
    }

    public List<AttributeDescription> getAttributeDescriptions() throws VrsException
    {
        List<AttributeDescription> descs = super.getAttributeDescriptions();
        List<AttributeDescription> resourceAttrs = getResourceAttrDescriptions();
        descs.addAll(resourceAttrs);

        return descs;
    }

    public List<AttributeDescription> getResourceAttrDescriptions()
    {
        ArrayList<AttributeDescription> descs = new ArrayList<AttributeDescription>();
        descs.add(new AttributeDescription(InfoRSConstants.LOCALSYSTEM_OSTYPE, AttributeType.STRING, false, "LocalSystem OS Type"));
        descs.add(new AttributeDescription(InfoRSConstants.LOCALSYSTEM_OSVERSION, AttributeType.STRING, false, "LocalSystem OS Version"));
        descs.add(new AttributeDescription(InfoRSConstants.LOCALSYSTEM_ARCHTYPE, AttributeType.STRING, false, "LocalSystem Architecture"));
        descs.add(new AttributeDescription(InfoRSConstants.LOCALSYSTEM_HOMEDIR, AttributeType.STRING, false,
                "LocalSystem user home directory"));
        descs.add(new AttributeDescription(InfoRSConstants.LOCALSYSTEM_JREHOME, AttributeType.STRING, false, "LocalSystem JRE home"));
        descs.add(new AttributeDescription(InfoRSConstants.LOCALSYSTEM_JREVERSION, AttributeType.STRING, false, "LocalSystem JRE Version"));
        return descs;
    }

    public Attribute getAttribute(String name) throws VrsException
    {
        if (name == null)
            return null;

        Attribute attr = super.getAttribute(name);
        if (attr != null)
            return attr;

        if (name.equals(InfoRSConstants.LOCALSYSTEM_OSTYPE))
        {
            attr = new Attribute(name, GlobalProperties.getOsName());
        }
        else if (name.equals(InfoRSConstants.LOCALSYSTEM_ARCHTYPE))
        {
            attr = new Attribute(name, GlobalProperties.getOsArch());
        }
        else if (name.equals(InfoRSConstants.LOCALSYSTEM_OSVERSION))
        {
            attr = new Attribute(name, GlobalProperties.getOsVersion());
        }
        else if (name.equals(InfoRSConstants.LOCALSYSTEM_JREHOME))
        {
            attr = new Attribute(name, GlobalProperties.getJavaHome());
        }
        else if (name.equals(InfoRSConstants.LOCALSYSTEM_JREVERSION))
        {
            attr = new Attribute(name, GlobalProperties.getJavaVersion());
        }
        else if (name.equals(InfoRSConstants.LOCALSYSTEM_HOMEDIR))
        {
            String path = this.getHomeNode().getVRL().getPath();
            attr = new Attribute(name, path);
        }

        return attr;
    }

}
