package settings;

import java.util.Properties;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.util.logging.ClassLogger;

public class Settings
{
    private static Settings instance=null; 
    
    public final String TEST_AES256BITS_ENCRYPTION_PROP="platinum.test.encryption.AES256bits";
    
    public final String TEST_LOCALTESTDIR_PROP="platinum.test.localTestDir"; 
    
    public static Settings getInstance()
    {
        if (instance==null)
        {
            instance=new Settings(); 
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
        return getBoolValue(TEST_AES256BITS_ENCRYPTION_PROP,false); 
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

    public String getValue(String name, String defaultValue)
    {   
        Object value=properties.get(name);
        
        if (value!=null)
        {
            if (value instanceof String)
            {
                return (String)value;
            }
            else
            {
                return value.toString(); 
            }
        }
        
        return defaultValue;
    }
    
    public String getLocalTestDir()
    {
        return getValue(TEST_LOCALTESTDIR_PROP, GlobalProperties.getGlobalTempDir()); 
    }

    public String getTestSubdir(String subPath)
    {
        String testDir=getLocalTestDir();
        // 
        return testDir+"/"+subPath;
    } 
    
}
