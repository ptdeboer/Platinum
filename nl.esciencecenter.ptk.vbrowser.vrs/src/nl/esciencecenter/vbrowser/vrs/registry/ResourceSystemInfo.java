package nl.esciencecenter.vbrowser.vrs.registry;

import java.util.Properties;

import nl.esciencecenter.ptk.crypt.Secret;
import nl.esciencecenter.ptk.object.Duplicatable;
import nl.esciencecenter.vbrowser.vrs.VRS;
import nl.esciencecenter.vbrowser.vrs.VRSProperties;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class ResourceSystemInfo implements Duplicatable<ResourceSystemInfo>
{
    // --- 
    // Flags/Switchess
    // --- 
    public static final String NEED_USERINFO="needUserinfo";
    
    public static final String NEED_SERVERPATH="needServerPath"; 

    // -----------------
    // Server attributes
    // -----------------
    
    public static final String SERVER_SCHEME="serverScheme";

    public static final String SERVER_USERINFO="serverUserinfo";

    public static final String SERVER_HOSTNAME="serverHostname";
    
    public static final String SERVER_PORT="serverPort";
    
    public static final String SERVER_PATH="serverPath";
    
    public static final String SERVER_CONFIG_PROPERTIES="serverConfigProperties";

    public static final String ATTR_SSH_IDENTITY = "sshIdentity"; 
    
 
    // ==================
    // Instance 
    // ==================
   
    protected VRSProperties properties;   
    
    private Secret passwd=null;
    
    private ResourceSystemInfoRegistry infoRegistry=null;
    
    final String id;
    
    public ResourceSystemInfo(ResourceSystemInfoRegistry registry, VRL serverVRL, String infoId)
    {
        this.infoRegistry=registry; 
        properties=new VRSProperties(null);
        setServerVRL(serverVRL); 
        this.id=infoId; 
    }
    
    protected ResourceSystemInfo(ResourceSystemInfoRegistry registry,VRSProperties props,String infoId)
    {
        this.infoRegistry=registry; 
        properties=props.duplicate(false);
        this.id=infoId;
    }
            
    protected void setServerVRL(VRL vrl)
    {
        properties.set(SERVER_SCHEME,vrl.getScheme());
        properties.set(SERVER_HOSTNAME, vrl.getHostname());
        // must use default port; 
        int port=vrl.getPort(); 
        if (port<=0)
        {
            port=VRS.getDefaultPort(vrl.getScheme());
        }
        properties.set(SERVER_PATH, port);
        properties.set(SERVER_PATH, vrl.getPath());
        // don't check need UserInfo;
        properties.set(SERVER_USERINFO, vrl.getUserinfo());
    }

    public void store()
    {
        if (this.infoRegistry==null)
        {
            throw new NullPointerException("No Registry. Can not store ResourceSystemInfo!"); 
        }
        
        this.infoRegistry.putInfo(this); 
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
        return properties.duplicate(); 
    }

    /** 
     * Replace properties. To update new properties, but keep the other properties use updateProperties(); 
     * @param properties - new properties to replace curent ones. 
     */
    public void setProperties(VRSProperties newProperties)
    {
        properties.clear(); 
        properties.putAll(newProperties);
    }
    
    public void setProperty(String name,String value)
    {
        properties.set(name, value);  
    }
    
    public String getProperty(String name)
    {
        return properties.getStringProperty(name); 
    }
    
    /** 
     * Add extra properties. To replace all properties use setProperties.  
     * @param properties - new properties to be addded to this ResourceInfo. 
     */
    public void updateProperties(VRSProperties properties)
    {
        properties.putAll(properties);
    }

    public String getServerScheme()
    {
        return properties.getStringProperty(SERVER_SCHEME);
    }
    
    public String getUserInfo()
    {
        return properties.getStringProperty(SERVER_USERINFO);
    }

    public void setUserInfo(String userInfo)
    {
        properties.set(SERVER_USERINFO,userInfo);
    }
    
    public int getServerPort()
    {
        return properties.getIntegerProperty(SERVER_PORT, -1); 
    }

    public String getServerHostname()
    {
        return properties.getStringProperty(SERVER_HOSTNAME);
    }

    public String getServerPath()
    {
        return properties.getStringProperty(SERVER_PATH); 
    }

    public boolean getNeedUserInfo()
    {
        return properties.getBooleanProperty(NEED_USERINFO,false);
    }
    
    public boolean getNeedServerPath()
    {
        return properties.getBooleanProperty(NEED_SERVERPATH,false);
    }

    public void setNeedUserInfo(boolean value)
    {
        properties.set(NEED_USERINFO,value);
    }
    
    public void setNeedServerPath(boolean value)
    {
        properties.set(NEED_SERVERPATH,value);
    }

    /** 
     * @return actual username without optional group and/or VO information.
     */
    public String getUsername()
    {
        String userInfo=this.getUserInfo();
        
        if (userInfo==null)
        {
            return null;
        }
        // split "<username>[:<VO>]" parts:
        String parts[]=userInfo.split(":"); 
        if (parts==null)
            return null; 
        return parts[0]; 
        
    }
    
    /** 
     * Return user logical group or VO. 
     * @return User group, VO or null if not defined. 
     */
    public String getUserVO()
    {
        String userInfo=this.getUserInfo();
        
        if (userInfo==null)
        {
            return null;
        }
        // split "<username>[:<VO>]" parts: 
        String parts[]=userInfo.split(":");
        if (parts.length<2)
        {
            return null; 
        }
        return parts[1]; 
        
    }

    public Secret getPassword()
    {
        return passwd; 
    }

    public void setPassword(Secret secret)
    {
        passwd=secret; 
    }

    public boolean setIfNotSet(String name, String value)
    {
        if (properties.get(name)==null)
        {
            properties.set(name, value); 
            return true;
        }
        return false; 
    }

    public String getID()
    {
        return this.id; 
    }
    
    public String toString()
    {
        return "ResourceSystemInfo:[id="+id
                +",password="+((passwd!=null)?"<PASSWORD>":"<No Password>")
                +",properties="+properties+"]"; 
    }

    @Override
    public boolean shallowSupported()
    {
        return false;
    }

    @Override
    public ResourceSystemInfo duplicate()
    {
        ResourceSystemInfo info=new ResourceSystemInfo(infoRegistry,properties.duplicate(false),id); 
        info.passwd=getPassword(); //copy ? 
        return info; 
    }

    @Override
    public ResourceSystemInfo duplicate(boolean shallow)
    {
        return duplicate(); 
    }
}
