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

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;

/** 
 * Fixed Config Node.
 * Hold global VRS configuration and settings.  
 */
public class InfoConfigNode extends InfoRSPathNode
{
    protected SystemInfosNode infosNode=null;
    
    public InfoConfigNode(InfoRootNode infoRootNode) throws VrsException
    {
        super(infoRootNode, InfoRSConstants.INFOCONFIGNODE, InfoRS.createPathVRL(InfoRSConstants.INFOCONFIGNODE));
        init(); 
    }

    public InfoConfigNode(InfoConfigNode parentNode, String subName) throws VrsException
    {
        super(parentNode, InfoRSConstants.INFOCONFIGNODE, InfoRS.createPathVRL(InfoRSConstants.INFOCONFIGNODE + "/" + subName));
        init();
    }

    public String getIconURL(int size)
    {
        return "info/configure-128.png";
    }

    public String getName()
    {
        return "Config";
    }

    protected void init() throws VrsException
    {
        initChilds(); 
    }
    
    protected void initChilds() throws VrsException
    {
        this.infosNode=new SystemInfosNode(this); 
        this.addSubNode(infosNode);  
    }

}
