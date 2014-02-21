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

import nl.esciencecenter.vbrowser.vrs.VRSTypes;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class InfoRootNode extends InfoRSNode
{

    protected InfoRS infors;

    protected LocalSystem localSystem;

    protected InfoConfigNode configNode;

    public InfoRootNode(InfoRS infoRS) throws VrsException
    {
        super(infoRS, InfoRSConstants.INFOROOTNODE, new VRL("info", null, 0, "/"));
        infors = infoRS;
        init();
    }

    protected void init() throws VrsException
    {
        initChilds();
    }

    protected void initChilds() throws VrsException
    {
        this.nodes.clear();
        this.addNode(getConfigNode());
        this.addNode(getLocalSystem());
    }

    public InfoRSNode getNode(VRL vrl) throws VrsException
    {
        String paths[] = vrl.getPathElements();

        if (paths == null)
            return this;

        int n = paths.length;
        if (n == 0)
        {
            return this;
        }

        if (n > 0)
        {
            InfoRSNode node = this.findNode(vrl, true);

            if (node != null)
            {
                return node;
            }
        }

        throw new VrsException("Node not found:" + vrl);
    }

    protected LocalSystem getLocalSystem() throws VrsException
    {
        if (localSystem == null)
        {
            initLocalSystem();
        }

        return localSystem;
    }

    protected InfoConfigNode getConfigNode()
    {
        if (configNode == null)
        {
            initConfigNode();
        }

        return configNode;
    }

    protected void initLocalSystem() throws VrsException
    {
        localSystem = new LocalSystem(this);
    }

    protected void initConfigNode()
    {
        configNode = new InfoConfigNode(this);
    }

    public void addResourceLink(String folderName, String logicalName, VRL targetLink, String optIconURL) throws VrsException
    {
        InfoRSNode parentNode;

        if (folderName != null)
        {
            parentNode = this.getSubNode(folderName);
            if (parentNode == null)
            {
                parentNode = this.createResourceFolder(folderName, null);
            }
        }
        else
        {
            parentNode = this;
        }

        InfoResourceNode node = InfoResourceNode.createLinkNode(parentNode, logicalName, targetLink, optIconURL, true);
        parentNode.addNode(node);
    }

    protected InfoResourceNode createResourceFolder(String folderName, String optIconURL) throws VrsException
    {
        InfoRSNode node = this.getSubNode(folderName);

        if (node instanceof InfoResourceNode)
        {
            return (InfoResourceNode) node;
        }
        else if (node != null)
        {
            throw new VrsException("Type Mismatch: InfoRSNode name'" + folderName + "' already exists, but is not a InfoResourceNode:"
                    + node);
        }
        else
        {
            InfoResourceNode folder = InfoResourceNode.createFolderNode(this, folderName, optIconURL);
            this.addNode(folder);
            return folder;
        }
    }

    public List<String> getChildResourceTypes()
    {
        // Root Node support default InfoRS types: 
       return defaultFolderChildTypes;  
    }
    
}
