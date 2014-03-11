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

package nl.esciencecenter.vbrowser.vrs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.ptk.object.Duplicatable;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;
import nl.esciencecenter.vbrowser.vrs.data.AttributeUtil;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VRSProperties implements Serializable, Cloneable, Duplicatable<VRSProperties>//, Comparable<VRSProperties>
{
    private static final long serialVersionUID = 1515535666077358909L;
    
    /**
     * Optional Parent for hierarchical properties. 
     */
    protected VRSProperties parent=null;  
    
    /** 
     * Use LinkedHashMap to keep order of properties. 
     */
    protected LinkedHashMap<String,Object>  _properties=null; 
    
    protected String propertiesName="VRSProperties"; 
    
    /**
     * Creates new VRSProperties and copies values from sourceProperties; 
     * Converts key object to String based key. 
     * @param sourceProperties source Properties to copy. 
     * @param duplicateProperties - if all property values implement the Duplicatable interface, duplicate values as well. 
     */
    public VRSProperties(String name, Map<? extends Object,Object> sourceProperties,boolean duplicateProperties)
    {
        init(name); 
        
        if (sourceProperties!=null)
        {
            for (Object key:sourceProperties.keySet())
            {
                Object value=sourceProperties.get(key);
                
                if (duplicateProperties)
                {
                    Object dupValue=null;  
                    
                    // use Attribute: 
                    AttributeType type=AttributeType.getObjectType(value, null);
                    
                    if (type!=null)
                    {
                        dupValue= AttributeUtil.duplicateObject(value);
                    }
                    
                    if (dupValue==null)
                    {
                        String typeStr="<NONE>";  
                        
                        if (type!=null) 
                        {
                            typeStr=type.toString(); 
                        }    
                       
                        throw new Error("Cannot duplicate or clone property (attribute type='"+typeStr+"'<"+value.getClass()+">:"+value); 
                    }
                    else
                    {
                        value=dupValue;
                    }
                }
                
                doPut(key.toString(), value); 
            }
        }
    }
    
    public VRSProperties(String name, VRSProperties parent)
    {
        init(name);
        this.parent=parent; 
    }
    
    public VRSProperties(String name)
    {
        init(name); 
    }

    protected void init(String name)  
    {
        this.propertiesName=name; 
        _properties=new LinkedHashMap<String,Object>(); 
    }
    
    public void setName(String name)
    {
        this.propertiesName=name; 
    }
    
    public String getName()
    {
        return propertiesName; 
    }
    
    /** 
     * @return actual backing Map of stored properties. 
     */
    public Map<String,Object> getProperties()
    {
        return _properties; 
    }
    
    public String getStringProperty(String name)
    {
        Object val=this.doGet(name); 
        if (val==null)
        {
            return null;
        }
        else if (val instanceof String)
        {
            return (String)val;
        }
        else
        {
            return val.toString();
        }
    }
    
    public int getIntegerProperty(String name,int defaultValue)
    {
        Object val=this.doGet(name); 
        if (val==null)
        {
            return defaultValue;
        }
        else if (val instanceof Integer)
        {
            return (Integer)val; //autoboxing
        }
        else if (val instanceof Long)
        {
            return ((Long)val).intValue(); //auto-cast 
        }
        else
        {
            return Integer.parseInt(val.toString());
        }
    }

    public long getLongProperty(String name,int defaultValue)
    {
        Object val=this.doGet(name); 
        if (val==null)
        {
            return defaultValue;
        }
        else if (val instanceof Integer)
        {
            return (Integer)val; //autoboxing
        }
        else if (val instanceof Long)
        {
            return (Long)val;  
        }
        else
        {
            return Long.parseLong(val.toString());
        }
    }
    
    public void set(String name, Object value)
    {
        doPut(name, value); 
    }
    
    public Object get(String name)
    {
        return doGet(name); 
    }

    protected void doPut(String name, Object value)
    {
        this._properties.put(name, value); 
    }

    protected void doRemove(String name) 
    {
        this._properties.remove(name);  
    }
    
    protected Object doGet(String name)
    {
        Object value=null; 
        
        if (parent!=null)
        {
            value=parent.doGet(name); 
        }
        
        if (value==null)
        {   
            value=_properties.get(name); 
        }
        
        return value; 
    }
    
    public VRSProperties duplicate()
    {
        return duplicate(false); 

    }

    @Override
    public boolean shallowSupported()
    {
        return true;
    }

    @Override
    public VRSProperties duplicate(boolean shallow)
    {
        return new VRSProperties(propertiesName,_properties,!shallow);
    }
    
    /**
     * @return actual keySet of properties Map. 
     */
    public Set<String> keySet()
    {
        return this._properties.keySet(); 
    }
    
    /**
     * Returns copy of Key Set as String List. 
     * @return String list of key set. 
     */
    public List<String> keyList()
    {
        List<String> keys=new ArrayList<String>();  
        
        for (Object key:_properties.keySet())
        {
            keys.add(key.toString()); 
        }
        
        return keys; 
    }

    /** 
     * Copy all properties: 
     * @param vrsProps
     */
    public void putAll(VRSProperties vrsProps)
    {
        _properties.putAll(vrsProps._properties);
    }

    public boolean getBooleanProperty(String name, boolean defaultValue)
    {
        Object val=this.doGet(name); 
        if (val==null)
        {
            return defaultValue;
        }
        else if (val instanceof Boolean)
        {
            return (Boolean)val; //autoboxing
        }
        else
        {
            return Boolean.parseBoolean(val.toString());
        }
    }

    public void set(String name, boolean value)
    {
        doPut(name, new Boolean(value));  
    }
    
    public void set(String name, String value)
    {
        // null value mean not set -> clear value. 
        
        if (value==null)
        {
            doRemove(name); 
        }
        else
        {
            doPut(name,value); 
        }
    }
    
    public void set(String name,int value)
    {
        doPut(name, new Integer(value)); 
    }

    public void remove(String name)
    {
        doRemove(name); 
    }
    
    public VRL getVRLProperty(String name, VRL defaultValue) throws VRLSyntaxException
    {
        VRL vrl=getVRLProperty(name);
            
        if (vrl!=null)
        {
            return vrl;
        }
        
        return defaultValue; 
    }
    
    public VRL getVRLProperty(String name) throws VRLSyntaxException
    {
        Object val=this.doGet(name); 
        if (val==null)
        {
            return null; 
        }
        else if (val instanceof VRL)
        {
            return (VRL)val;
        }
        else if (val instanceof java.net.URI)
        {
            return new VRL((java.net.URI)val); 
        }
        else if (val instanceof java.net.URL)
        {
            return new VRL((java.net.URL)val); 
        }
        else
        {
            return new VRL(val.toString()); 
        }
    }

    public boolean setIfNotSet(String name, String value)
    {
        if (this.getStringProperty(name)==null)
        {
            this.set(name, value); 
            return true; 
        }
        return false; 
    }

    public void clear()
    {
       _properties.clear(); 
    }

    @Override
    public String toString()
    {
        return "VRSProperties[properties=" + _properties + "]";
    }

    
}
