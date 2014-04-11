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

import java.util.List;

import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfo;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfoRegistry;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Fixed LocalSystem node.
 */
public class SystemInfosNode extends InfoRSNode
{
    private final static ClassLogger logger = ClassLogger.getLogger(SystemInfosNode.class);

    public SystemInfosNode(InfoConfigNode parent) throws VrsException
    {
        super(parent, InfoRSConstants.SYSTEMINFOS_NODE, parent.createSubPathVRL(InfoRSConstants.SYSTEMINFOS_NODE));
        initChilds();
    }

    public String getName()
    {
        return "Server Configurations";
    }

    public String getIconURL(int size)
    {
        // need better icon.
        return "info/system-configs-48.png";
    }

    protected void initChilds() throws VrsException
    {
        initSubNodes();
        initConfigs();
    }

    public List<? extends InfoRSNode> list() throws VrsException
    {
        sync();
        return super.list();
    }

    /** 
     * list current registered ResourceSystemInfo descriptions. 
     * @return
     */
    protected List<ResourceSystemInfo> listResourceSystemInfos()
    {
        ResourceSystemInfoRegistry reg = this.getVRSContext().getResourceSystemInfoRegistry();
        List<ResourceSystemInfo> infos = reg.list();
        // filter
        return infos;
    }

    protected void initConfigs() throws VrsException
    {
        int index = 0;

        List<ResourceSystemInfo> infos = listResourceSystemInfos();

        logger.debugPrintf("Adding %d ResourceSystemInfos\n", infos.size());

        for (ResourceSystemInfo info : infos)
        {
            logger.debugPrintf(" - adding ResourceSystemInfo:%s\n", info);

            VRL serverVrl = info.getServerVRL();
            int port = info.getServerPort();
            if (port < 0)
            {
                port = 0;
            }
            String userInf = info.getUserInfo();

            if (userInf == null)
            {
                userInf = "";
            }
            else
            {
                userInf = userInf + "@";
            }
            String hostname = info.getServerHostname();
            if (StringUtil.isEmpty(hostname))
            {
                hostname = "Localhost";
            }

            String name = "Server " + serverVrl.getScheme() + ":" + hostname + ":" + port;
            String subPath = "ServerConfig-" + index++;

            this.addSubNode(createSystemInfoNode(subPath, info, name, "info/server-fs-network-48.png"));
        }
    }

    protected InfoResourceNode createSystemInfoNode(String subPath, ResourceSystemInfo info, String name, String iconUrl)
            throws VRLSyntaxException
    {
        VRL logicalVrl = this.createSubPathVRL(subPath);

        SystemInfoNode node = new SystemInfoNode(this, logicalVrl, info);
        node.setLogicalName(name);
        node.setIconUrl(iconUrl);
        return node;
    }

    public Attribute getResourceAttribute(String name) throws VrsException
    {
        if (name == null)
        {
            return null;
        }
        
        Attribute attr = super.getResourceAttribute(name);
        
        if (attr != null)
        {
            return attr;
        }
        
        return attr;
    }

    public boolean sync() throws VrsException
    {
        initChilds();
        return true;
    }

}
