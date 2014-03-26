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

import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.data.LongHolder;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VirtualRootNode extends ProxyNode
{
    protected String name="Root"; 
    
    protected List<ProxyNode> childs=new ArrayList<ProxyNode>();
    
    protected String iconUrl=null; 
        
    protected VirtualRootNode(ProxyFactory factory, VRL proxyLocation)
    {
        super(factory, proxyLocation);
    }

    @Override
    protected String doGetName() throws ProxyException
    {
        return name; 
    }

    @Override
    protected String doGetResourceType() throws ProxyException
    {
        return "Root"; 
    }

    @Override
    protected String doGetResourceStatus() throws ProxyException
    {
        return null;
    }

    @Override
    protected String doGetMimeType() throws ProxyException
    {
        return null;
    }

    @Override
    protected boolean doGetIsComposite() throws ProxyException
    {
        return true; 
    }

    @Override
    protected List<? extends ProxyNode> doGetChilds(int offset, int range, LongHolder numChildsLeft) throws ProxyException
    {
        return ProxyNode.subrange(childs,offset,range); 
    }

    @Override
    protected ProxyNode doGetParent() throws ProxyException
    {
        return this;
    }

    @Override
    protected List<String> doGetChildTypes() throws ProxyException
    {
        StringList list=new StringList(); 
        
        for (int i=0;i<childs.size();i++)
        {
            list.add(childs.get(i).getResourceType()); 
        }
        return list; 
    }

    @Override
    protected List<String> doGetAttributeNames() throws ProxyException
    {
        return null;
    }

    @Override
    protected List<Attribute> doGetAttributes(List<String> names) throws ProxyException
    {
        return null;
    }

    @Override
    protected Presentation doGetPresentation()
    {
        return null;
    }

    public void addChild(ProxyNode node)
    {
        if (childs==null)
            childs=new ArrayList<ProxyNode>(); 
        
       this.childs.add(node); 
    }

    public void setChilds(List<ProxyNode> nodes)
    {
       this.childs=new ArrayList<ProxyNode>(nodes);
    }

    public boolean hasChild(VRL locator)
    {
        return (this.getChild(locator)!=null); 
    }

    public ProxyNode getChild(VRL locator)
    {
        for (ProxyNode node:this.childs)
        {
            if (node.hasLocator(locator))
            {
                return node;
            }
        }
        
        return null; 
    }

    @Override
    protected String doGetIconURL(String status, int size) throws ProxyException
    {
        return iconUrl; 
    }

    @Override
    protected boolean doIsResourceLink()
    {
        return false;
    }

    @Override
    protected VRL doGetResourceLinkVRL()
    {
        return null;
    }

    @Override
    protected ProxyNode doCreateNew(String type, String optNewName) throws ProxyException
    {
        throw new ProxyException("Virtual root cannot create new nodes."); 
    }

    @Override
    protected void doDelete(boolean recurse) throws ProxyException
    {
        throw new ProxyException("Virtual root cannot be deleted."); 
    }

    @Override
    protected ProxyNode doRenameTo(String newName) throws ProxyException
    {
        this.name=newName; 
        return this; 
    }

}
