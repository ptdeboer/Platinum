package nl.esciencecenter.vbrowser.vrs;

import java.util.Properties;

public class VRSProperties
{
    protected Properties properties=null; 
    
    public VRSProperties(Properties properties)
    {
        this.properties=properties; 
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
    
}
