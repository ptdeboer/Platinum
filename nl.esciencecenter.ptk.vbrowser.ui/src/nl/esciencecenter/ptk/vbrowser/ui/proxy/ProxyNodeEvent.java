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

import nl.esciencecenter.vbrowser.vrs.vrl.VRL;


public class ProxyNodeEvent
{
	public static enum ProxyNodeEventType
	{
		RESOURCES_ADDED,
		RESOURCES_DELETED,
		RESOURCES_RENAMED,
		ATTRIBUTES_CHANGED, 
		REFRESH_RESOURCES
	}
    
    // ========================================================================
    // 
    // ========================================================================
	
    public static ProxyNodeEvent createChildsAddedEvent(VRL optionalParent,VRL childs[])
    {
        ProxyNodeEvent event=new ProxyNodeEvent(ProxyNodeEventType.RESOURCES_ADDED);
        event.parent=optionalParent; 
        event.resources=childs; 
        return event; 
    }
    
    public static ProxyNodeEvent createChildAddedEvent(VRL parent,VRL child)
    {
        VRL childs[]=new VRL[1]; 
        childs[0]=child; 
        return createChildsAddedEvent(parent,childs);
    }
	
	public static ProxyNodeEvent createChildDeletedEvent(VRL optionalParent,VRL childs[])
    {
        ProxyNodeEvent event=new ProxyNodeEvent(ProxyNodeEventType.RESOURCES_DELETED);
        event.parent=optionalParent; 
        event.resources=childs; 
        return event; 
    }
	
	public static ProxyNodeEvent createChildDeletedEvent(VRL optionalParent,VRL child)
    {
        ProxyNodeEvent event=new ProxyNodeEvent(ProxyNodeEventType.RESOURCES_DELETED);
        event.parent=optionalParent; 
        event.resources=new VRL[1];
        event.resources[0]=child; 
        return event; 
    }
    
    public static ProxyNodeEvent createRefreshEvent(VRL optionalParent, VRL res)
    {
        ProxyNodeEvent event=new ProxyNodeEvent(ProxyNodeEventType.REFRESH_RESOURCES);
        event.parent=optionalParent; 
        event.resources=new VRL[1];
        event.resources[0]=res; 
        return event; 
    }
    

	// ========================================================================
    // 
    // ========================================================================
	
	protected ProxyNodeEventType type; 
	
	/** Optional parent resource. */  
	protected VRL parent; 
	
	/** Sources this event applies to */ 
	protected VRL[] resources; 
	
	/** Optional attribute names involved */ 
	protected String attributeNames[]; 
	
	protected ProxyNodeEvent(ProxyNodeEventType type)
	{
	    this.type=type; 
	}
	
	public ProxyNodeEventType getType()
	{
		return this.type; 
	}
	
	/** Resources this event applies to. */ 
	public VRL[] getResources()
	{
		return this.resources;
	}

    /** 
     * If the parent resource has been specified, it is the parent
     * of all the resource from getResources() 
     */  
	public VRL getParent()
	{
		return parent; 
	}
	
	/** Attributes this event applies to if this is an Attribute Event */ 
	public String[] getAttributeNames()
	{
		return this.attributeNames; 
	}

	public String toString()
	{
	    return "DataSourceEvent:"+this.type+":(parent="+parent+", resources={"+flattenStr(resources)+"})"; 
	}

    private String flattenStr(VRL[] locs)
    {   
        if (locs==null)
            return "";
        
        String str="";
        for (int i=0;i<locs.length;i++)
        {
            str+=locs[i];
            if (i+1<locs.length)
                str+=","; 
        }
        
        return str; 
    }


}
