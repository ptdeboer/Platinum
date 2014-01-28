package nl.esciencecenter.vbrowser.vrs.registry;

import java.util.Properties;

import nl.esciencecenter.vbrowser.vrs.VRS;
import nl.esciencecenter.vbrowser.vrs.VRSProperties;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class ResourceSystemInfo
{
    public static final String SERVER_SCHEME="serverScheme";

    public static final String SERVER_USERINFO="serverUserinfo";

    public static final String NEED_USERINFO="needUserinfo";
    
    public static final String NEED_SERVERPATH="needServerPath"; 
    
    public static final String SERVER_HOSTNAME="serverHostname";
    
    public static final String SERVER_PORT="serverPort";
    
    public static final String SERVER_PATH="serverPath";
    
    public static final String SERVER_CONFIG_PROPERTIES="serverConfigProperties";
 
    // ==================
    // Instance 
    // ==================
   
    protected VRSProperties vrsConfig;   
 
    public ResourceSystemInfo(VRL serverVRL)
    {
        vrsConfig=new VRSProperties(null);
        setServerVRL(serverVRL); 
    }
    
    protected void setServerVRL(VRL vrl)
    {
        vrsConfig.set(SERVER_SCHEME,vrl.getScheme());
        vrsConfig.set(SERVER_HOSTNAME, vrl.getHostname());
        // must use default port; 
        int port=vrl.getPort(); 
        if (port<=0)
        {
            port=VRS.getDefaultPort(vrl.getScheme());
        }
        vrsConfig.set(SERVER_PATH, port);
        vrsConfig.set(SERVER_PATH, vrl.getPath());
        // don't check need UserInfo;
        vrsConfig.set(SERVER_USERINFO, vrl.getUserinfo());
    }
    
    public ResourceSystemInfo(Properties properties)
    {
        vrsConfig=new VRSProperties(properties);
    }
    
    public VRL getServerVRL()
    {
        String path="/";
        
        if (getNeedServerPath())
        {
            path=this.getServerPath(); 
        }
        
        if (getNeedUserInfo())
        {
            return new VRL(getServerScheme(),getUserInfo(),getServerHostname(),getServerPort(),path);
        }
        else
        {
            return new VRL(getServerScheme(),getServerHostname(),getServerPort(),path);
        }
    }

    /** 
     * Returns duplicate of properties. 
     * Changing this properties won't efffect this ResourceSystemInfo 
     * Use setProperties() to update the Resource Properties.
     *   
     * @return duplicate of Resource Properties.
     */
    public VRSProperties getProperties()
    {
        return vrsConfig.duplicate(); 
    }
    
    public void setProperties(VRSProperties properties)
    {
        properties.putAll(properties);
    }


    public String getServerScheme()
    {
        return vrsConfig.getStringProperty(SERVER_SCHEME);
    }
    
    public String getUserInfo()
    {
        return vrsConfig.getStringProperty(SERVER_USERINFO);
    }

    public int getServerPort()
    {
        return vrsConfig.getIntegerProperty(SERVER_PORT, -1); 
    }

    public String getServerHostname()
    {
        return vrsConfig.getStringProperty(SERVER_HOSTNAME);
    }

    public String getServerPath()
    {
        return vrsConfig.getStringProperty(SERVER_PATH); 
    }

    public boolean getNeedUserInfo()
    {
        return vrsConfig.getBooleanProperty(NEED_USERINFO,false);
    }
    
    public boolean getNeedServerPath()
    {
        return vrsConfig.getBooleanProperty(NEED_SERVERPATH,false);
    }

    public void setNeedUserInfo(boolean value)
    {
        vrsConfig.set(NEED_USERINFO,value);
    }
    
    public void setNeedServerPath(boolean value)
    {
        vrsConfig.set(NEED_SERVERPATH,value);
    }

}
