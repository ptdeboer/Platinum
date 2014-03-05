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
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VRSProperties implements Serializable, Cloneable, Duplicatable<VRSProperties>//, Comparable<VRSProperties>
{
    private static final long serialVersionUID = 1515535666077358909L;
    
    /** 
     * Use LinkedHashMap to keep order of properties. 
     */
    protected LinkedHashMap<String,Object>  properties=null; 
    
    protected String propertiesName="VRSProperties"; 
    
    /**
     * Creates new VRSProperties and copies values from sourceProperties; 
     * Converts key object to String based key. 
     * @param sourceProperties source Properties to copy. 
     */
    public VRSProperties(String name, Map<? extends Object,Object> sourceProperties)
    {
        init(name); 
        
        for (Object key:sourceProperties.keySet())
        {
            this.properties.put(key.toString(), sourceProperties.get(key)); 
        }
    }
    
    public VRSProperties(String name)
    {
        init(name); 
    }

    protected void init(String name)  
    {
        this.propertiesName=name; 
        properties=new LinkedHashMap<String,Object>(); 
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
        return properties; 
    }
    
    public String getStringProperty(String name)
    {
        Object val=this.properties.get(name); 
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
        Object val=this.properties.get(name); 
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
        Object val=this.properties.get(name); 
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
        this.properties.put(name, value); 
    }
    
    public Object get(String name)
    {
        return properties.get(name); 
    }

    public VRSProperties duplicate()
    {
        return new VRSProperties(propertiesName,properties);
    }

    @Override
    public boolean shallowSupported()
    {
        return false;
    }

    @Override
    public VRSProperties duplicate(boolean shallow)
    {
        return duplicate(); 
    }
    
    /**
     * @return actual keySet of properties Map. 
     */
    public Set<String> keySet()
    {
        return this.properties.keySet(); 
    }
    /**
     * Returns copy of Key Set as String List. 
     * @return String list of key set. 
     */
    public List<String> keyList()
    {
        List<String> keys=new ArrayList<String>();  
        
        for (Object key:properties.keySet())
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
        properties.putAll(vrsProps.properties);
    }

    public boolean getBooleanProperty(String name, boolean defaultValue)
    {
        Object val=this.properties.get(name); 
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
        properties.put(name, new Boolean(value)); 
    }
    
    public void set(String name, String value)
    {
        // null value mean not set -> clear value. 
        
        if (value==null)
        {
            properties.remove(name); 
            return;
        }
        
        properties.put(name, value); 
    }
    
    public void set(String name,int value)
    {
        properties.put(name, new Integer(value)); 
    }

    public void remove(String name)
    {
        properties.remove(name);
    }
    
    public VRL getVRLProperty(String name) throws VRLSyntaxException
    {
        Object val=this.properties.get(name); 
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
       properties.clear(); 
    }

    @Override
    public String toString()
    {
        return "VRSProperties[properties=" + properties + "]";
    }



//    @Override
//    public int compareTo(VRSProperties others)
//    {
//        object values must be comparible 
//    }

    
}
