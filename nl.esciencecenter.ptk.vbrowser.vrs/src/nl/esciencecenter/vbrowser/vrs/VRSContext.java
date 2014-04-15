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

import java.util.Properties;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.ssl.CertificateStore;
import nl.esciencecenter.ptk.ssl.CertificateStoreException;
import nl.esciencecenter.ptk.ui.SimpelUI;
import nl.esciencecenter.ptk.ui.UI;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.vrs.credentials.Credential;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.Registry;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfo;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfoRegistry;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/** 
 * Main Context of the Virtual Resource System. 
 * Hold Registry, ResourceSystemInfoRegistry and instantiated ResourceSystems. 
 */
public class VRSContext
{
    private static final ClassLogger logger=ClassLogger.getLogger(VRSContext.class); 
    
    private static long instanceCounter=0; 
    
    // ---
    // Instance 
    // --- 
    private long id=instanceCounter++; 
    
    protected Registry registry;
    
    protected VRSProperties vrsProperties;

    protected ResourceSystemInfoRegistry resourceInfoRegistry; 

    protected UI ui;

    private VRL persistantConfigLocation=null;

    private boolean hasPersistantConfig=false;
    
    public VRSContext()
    {
        init(new VRSProperties("VRSContext")); 
    }

    public long getID()
    {
        return id; 
    }
    
    public VRSContext(Properties props)
    {
        init(new VRSProperties("VRSContext",props,true));
    }
    
    public VRSContext(VRSProperties props)
    {
        init(props.duplicate(false)); 
    }

    private void init(VRSProperties privateProperties)
    {
        logger.infoPrintf("***New VRSContext(), id="+id+"***");
        // default Static Registry ! 
        this.registry=Registry.getInstance(); 
        this.vrsProperties=privateProperties;
        resourceInfoRegistry=new ResourceSystemInfoRegistry(this);
        ui=new SimpelUI(); 
    }
    
    public Registry getRegistry()
    {
        return registry;  
    }
    
    /** 
     * @return Configure properties for this VRSContext. 
     */
    public VRSProperties getProperties()
    {
        return this.vrsProperties; 
    }
    
    public CertificateStore getCertificateStore() throws VrsException
    {
        try
        {
            return CertificateStore.getDefault(true);
        }
        catch (CertificateStoreException e)
        {
           throw new VrsException(e.getMessage(),e); 
        }
    }
    
    public ResourceSystemInfoRegistry getResourceSystemInfoRegistry()
    {
        return this.resourceInfoRegistry; 
    }
    
    public ResourceSystemInfo getResourceSystemInfoFor(VRL vrl, boolean autoCreate) throws VrsException
    {
        VResourceSystemFactory fac = registry.getVResourceSystemFactoryFor(this, vrl.getScheme());
        if (fac==null)
        {   
            throw new VrsException("Scheme not supported. No ResourceSystemFactory for:"+vrl);
        }
        
        String id=fac.createResourceSystemId(vrl);
        
        ResourceSystemInfo info= resourceInfoRegistry.getInfo(id);
        
        if ((info==null) && (autoCreate==true))
        {
            info=new ResourceSystemInfo(resourceInfoRegistry,vrl,id);
        }
        info=fac.updateResourceInfo(this, info, vrl); 
        return info; 
    }

    public void putResourceSystemInfo(ResourceSystemInfo info)
    {
        resourceInfoRegistry.putInfo(info); 
    }
    /** 
     * For head-less environments, getUI() will return a dummy Object.    
     * For non graphical environments this method will return a dummy ui. 
     * When registered in the VBrowser this method will return an interactive callback interface to the VBrowser. 
     * @return register UI or dummy UI for non-interactive environments. 
     */
    public UI getUI()
    {
        return ui; 
    }
    
    public VRL getHomeVRL()
    {
        VRL vrl=new VRL("file",null,GlobalProperties.getGlobalUserHome());
        return vrl; 
    }

    public VRL getCurrentPathVRL()
    {
        return new VRL("file",null,GlobalProperties.getGlobalUserDir());
    }

    public String getUserName()
    {
        return GlobalProperties.getGlobalUserName(); 
    }

    public void setPersistantConfigLocation(VRL configHome, boolean enabled) 
    {
        this.persistantConfigLocation=configHome; 
        this.hasPersistantConfig=enabled; 
    }
    
    public VRL getPersistantConfigLocation()
    {
        return this.persistantConfigLocation; 
    }
    
    public boolean hasPersistantConfig() 
    {
        return this.hasPersistantConfig; 
    }

    public String getVO()
    {
        return null;
    }

    public Credential getCredential(String credentialType)
    {
        return null; 
    }

    public VRL getInfoRootNodeVRL() throws VRLSyntaxException
    {
        return new VRL("info:/"); 
    }
    
}
