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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import nl.esciencecenter.vbrowser.vrs.VRSProperties;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;
import nl.esciencecenter.vbrowser.vrs.data.AttributeUtil;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Fixed Link Node. Not editable.
 */
public class SystemInfoNode extends InfoResourceNode
{

    protected SystemInfoNode(InfoRSPathNode parent, VRL logicalVRL, ResourceConfigInfo info)
    {
        super(parent, InfoRSConstants.RESOURCEINFO_CONFIG, logicalVRL);
        this.setTargetVRL(info.getServerVRL());
        this.setIconUrl(null); // default ?
        this.setShowLinkIcon(false);
        this.setLogicalName("link:" + logicalVRL.getBasename());
    }

    protected VRL getServerVRL()
    {
        return this.getTargetVRL();
    }

    public boolean isResourceLink()
    {
        return false;
    }

    public ResourceConfigInfo getResourceSystemInfo() throws VrsException
    {
        return getVRSContext().getResourceSystemInfoFor(getServerVRL(), false);
    }

    public List<AttributeDescription> getAttributeDescriptions() throws VrsException
    {
        List<AttributeDescription> descs = super.getImmutableAttributeDescriptions();
        List<AttributeDescription> resourceAttrs = getInfoAttrDescriptions();
        descs.addAll(resourceAttrs);

        return descs;
    }

    public List<AttributeDescription> getInfoAttrDescriptions() throws VrsException
    {

        LinkedHashSet<String> names = new LinkedHashSet<String>();
        List<AttributeDescription> descs = new ArrayList<AttributeDescription>();

        ResourceConfigInfo info = getResourceSystemInfo();

        if (info == null)
        {
            throw new VrsException("No getResourceSystemInfo for:" + getServerVRL());
        }

        VRSProperties props = info.getProperties();
        for (String key : props.keyList())
        {
            names.add(key);
        }

        for (String name : names)
        {
            AttributeDescription desc = new AttributeDescription(name, AttributeType.STRING, false, "Server property " + name);
            descs.add(desc);
        }

        return descs;
    }
    
    public Attribute getResourceAttribute(String name) throws VrsException
    {
        Attribute superAttr=super.getResourceAttribute(name); 
        
        ResourceConfigInfo info=this.getResourceSystemInfo();

        if (info==null)
        {
            return superAttr; // could be null; 
        }
        String value=info.getProperty(name);
        if (value==null)
        {
            return superAttr;// could be null; 
        }
        
        return AttributeUtil.createStringAttribute(name,value,false); 
    }
}
