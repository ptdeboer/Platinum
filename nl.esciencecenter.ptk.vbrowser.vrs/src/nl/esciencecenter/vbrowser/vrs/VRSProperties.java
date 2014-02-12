package nl.esciencecenter.vbrowser.vrs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import nl.esciencecenter.ptk.object.Duplicatable;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VRSProperties implements Serializable, Cloneable, Duplicatable<VRSProperties>//, Comparable<VRSProperties>
{
    private static final long serialVersionUID = 1515535666077358909L;
    
    protected Map<String,Object>  properties=null; 
    
    /**
     * Creates new VRSProperties and copies values from sourceProperties; 
     * Converts key object to String based key. 
     * @param sourceProperties source Properties to copy. 
     */
    public VRSProperties(Map<? extends Object,Object> sourceProperties)
    {
        this.properties=new Hashtable<String,Object>(); 
        
        for (Object key:properties.keySet())
        {
            this.properties.put(key.toString(), sourceProperties.get(key)); 
        }
    }
    
    public VRSProperties()
    {
        properties=new Hashtable<String,Object>(); 
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
        return new VRSProperties(properties);
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

}
