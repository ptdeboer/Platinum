package nl.esciencecenter.ptk.testsettings;

import java.util.Properties;

import nl.esciencecenter.ptk.util.logging.ClassLogger;

public class TestSettings
{
    private static TestSettings instance=null; 
    
    public final String TEST_AES256BITS_ENCRYPTION="platinum.test.encryption.AES256bits"; 
    
    public static TestSettings getInstance()
    {
        if (instance==null)
        {
            instance=new TestSettings(); 
        }
        return instance; 
    }

    public static ClassLogger getLogger(Class<?> clazz)
    {
        return ClassLogger.getLogger(clazz); 
    }

    // ===
    // Instance 
    // === 
    
    protected Properties properties=new Properties();  
    
    public boolean testAES256Encryption()
    {
        return getBoolValue(TEST_AES256BITS_ENCRYPTION,false); 
    }

    public boolean getBoolValue(String name, boolean defaultValue)
    {   
        Object value=properties.get(name);
        
        if (value==null)
        {
            return defaultValue;
        }
        else if (value instanceof Boolean)
        {
            return (Boolean)value;
        }
        else
        {
            return Boolean.parseBoolean(value.toString());
        }
        
        
    }
    
    
}
