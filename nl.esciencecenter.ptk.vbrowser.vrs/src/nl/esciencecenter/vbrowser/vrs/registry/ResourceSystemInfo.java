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

package nl.esciencecenter.vbrowser.vrs.registry;

import nl.esciencecenter.ptk.crypt.Secret;
import nl.esciencecenter.ptk.object.Duplicatable;
import nl.esciencecenter.vbrowser.vrs.VRS;
import nl.esciencecenter.vbrowser.vrs.VRSProperties;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
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
   
    protected AttributeSet properties;   
    
    private Secret passwd=null;
    
    private ResourceSystemInfoRegistry infoRegistry=null;
    
    final String id;
    
    public ResourceSystemInfo(ResourceSystemInfoRegistry registry, VRL serverVRL, String infoId)
    {
        this.infoRegistry=registry; 
        properties=new AttributeSet();
        setServerVRL(serverVRL); 
        this.id=infoId; 
    }
            
    protected ResourceSystemInfo(ResourceSystemInfoRegistry registry, AttributeSet attributes, String infoId)
    {
        this.infoRegistry=registry;
        this.properties=attributes;
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
        return properties.toVRSProperties();  
    }
    
    public void setProperty(String name,String value)
    {
        properties.set(name, value);  
    }
    
    public String getProperty(String name)
    {
        return properties.getStringValue(name); 
    }
    
    /** 
     * Add extra properties. To replace all properties use setProperties.  
     * @param properties - new properties to be addded to this ResourceInfo. 
     */
    public void updateProperties(VRSProperties newProperties)
    {
        properties.putAll(properties);
    }

    public String getServerScheme()
    {
        return properties.getStringValue(SERVER_SCHEME);
    }
    
    public String getUserInfo()
    {
        return properties.getStringValue(SERVER_USERINFO);
    }

    public void setUserInfo(String userInfo)
    {
        properties.set(SERVER_USERINFO,userInfo);
    }
    
    public int getServerPort()
    {
        return properties.getIntValue(SERVER_PORT, -1); 
    }

    public String getServerHostname()
    {
        return properties.getStringValue(SERVER_HOSTNAME);
    }

    public String getServerPath()
    {
        return properties.getStringValue(SERVER_PATH); 
    }

    public boolean getNeedUserInfo()
    {
        return properties.getBooleanValue(NEED_USERINFO,false);
    }
    
    public boolean getNeedServerPath()
    {
        return properties.getBooleanValue(NEED_SERVERPATH,false);
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

    /** 
     * @return Returns backing actual AttributeSet with configuration. 
     */
    public AttributeSet getAttributeSet()
    {
        return this.properties; 
    }
    
    public String toString()
    {
        return "ResourceSystemInfo:[id="+id
                +",serverVrl=+"+getServerVRL()
                +",password="+((passwd!=null)?"<PASSWORD>":"<No Password>")
                +",properties="+properties+"]"; 
    }


}
