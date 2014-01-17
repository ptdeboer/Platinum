package nl.esciencecenter.vbrowser.vrs.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;



import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.ResourceSystemInfo;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
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
                vrsContext.putResourceSystemInfo(id,info); 
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
            VResourceSystemFactory vrsInstance = vrsClass.newInstance(); 
            registryFactory(vrsInstance);
        }
        catch (Throwable t)
        {
            logger.logException(loggerLevel, t, "Exception when registering VRS Factory Class:%s", vrsClass);
        }
    }

    public void registryFactory(VResourceSystemFactory factory)
    {
        synchronized(registeredServices)
        {
            registeredServices.put(factory.getClass().getCanonicalName(), factory); 
        }
        
        synchronized(registeredSchemes)
        {
            for (String scheme:factory.getSchemes())
            {
                ArrayList<SchemeInfo> list = registeredSchemes.get(scheme);    
                if (list==null)
                {
                    list=new ArrayList<SchemeInfo>(); 
                    registeredSchemes.put(scheme,list); 
                }
                
                list.add(new SchemeInfo(scheme,factory));
            }
        }
        
    }

}
