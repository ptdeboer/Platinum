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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;



import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.infors.InfoRSFactory;
import nl.esciencecenter.vbrowser.vrs.localfs.LocalFSFileSystemFactory;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import nl.esciencecenter.vbrowser.vrs.webrs.WebRSFactory;

public class Registry
{
    private final static ClassLogger logger=ClassLogger.getLogger(Registry.class); 
    
    private static Registry instance; 
    
    public static Registry getInstance()
    {
        synchronized(Registry.class)
        {
            if (instance==null)
            {
                instance = new Registry(); 
            }
        }
        
        return instance; 
    }
    
    protected static class SchemeInfo
    {
        protected String scheme; 
        
        protected VResourceSystemFactory vrsFactory;
        
        SchemeInfo(String scheme,VResourceSystemFactory factory)
        {
            this.scheme=scheme; 
            this.vrsFactory=factory; 
        }
    }
    
    // ========================================================================
    //
    // ========================================================================
    
    private Map<String, ArrayList<SchemeInfo>> registeredSchemes = new LinkedHashMap<String, ArrayList<SchemeInfo>>();

    /**
     * List of services. VRSFactories are registered using their class names as
     * key.
     */
    private Map<String, VResourceSystemFactory> registeredServices = new HashMap<String, VResourceSystemFactory>();
    
    private ResourceSystemInstances instances=new ResourceSystemInstances(); 
    
    private Registry()
    {
        init(); 
    }
    
    private void init()
    {
        initFactories();
    }
    
    private void initFactories()
    {
        this.registryFactoryNoException(LocalFSFileSystemFactory.class,ClassLogger.ERROR);
        this.registryFactoryNoException(WebRSFactory.class,ClassLogger.ERROR);
        this.registryFactoryNoException(InfoRSFactory.class,ClassLogger.ERROR);
    }

    public VResourceSystemFactory getVResourceSystemFactoryFor(VRSContext vrsContext, String scheme)
    {
        ArrayList<SchemeInfo> list = registeredSchemes.get(scheme); 
        if ((list==null) || (list.size()<=0)) 
        {
               return null;  
        }
        
        return list.get(0).vrsFactory;
    }
    
    public VResourceSystem getVResourceSystemFor(VRSContext vrsContext, VRL vrl) throws VrsException
    {
        VResourceSystemFactory factory = getVResourceSystemFactoryFor(vrsContext,vrl.getScheme());
        
        if (factory==null)
        {
            throw new VrsException("No VResourceSystem registered for:"+vrl); 
        }
        
        synchronized(instances)
        {
            String id=factory.createResourceSystemId(vrl); 
            
            VResourceSystem resourceSystem = instances.get(id);
            
            if (resourceSystem==null)
            {
                ResourceSystemInfo info=vrsContext.getResourceSystemInfoFor(vrl,true); 
                info=factory.updateResourceInfo(vrsContext,info,vrl);
                vrsContext.putResourceSystemInfo(info); 
                resourceSystem=factory.createResourceSystemFor(vrsContext,info,vrl);
                instances.put(id, resourceSystem); 
            }
            return resourceSystem;
        }
    }
    
    public void registryFactoryNoException(Class<? extends VResourceSystemFactory> vrsClass,Level loggerLevel)
    {
        try
        {
            registryFactory(vrsClass);
        }
        catch (Throwable t)
        {
            logger.logException(loggerLevel, t, "Exception when registering VRS Factory Class:%s", vrsClass);
        }
    }

    public void registryFactory(Class<? extends VResourceSystemFactory> vrsClass) throws InstantiationException, IllegalAccessException
    {
        VResourceSystemFactory vrsInstance; 
        
        synchronized(registeredServices)
        {
            // ===
            // Protected VRSFactory instance is created here ! 
            // ===
            vrsInstance = vrsClass.newInstance(); 
            registeredServices.put(vrsClass.getCanonicalName(), vrsInstance); 
        }
        
        synchronized(registeredSchemes)
        {
            for (String scheme:vrsInstance.getSchemes())
            {
                ArrayList<SchemeInfo> list = registeredSchemes.get(scheme);    
                if (list==null)
                {
                    list=new ArrayList<SchemeInfo>(); 
                    registeredSchemes.put(scheme,list); 
                }
                
                list.add(new SchemeInfo(scheme,vrsInstance));
            }
        }
        
    }

}
