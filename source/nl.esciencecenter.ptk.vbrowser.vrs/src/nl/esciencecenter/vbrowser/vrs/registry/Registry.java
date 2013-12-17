package nl.esciencecenter.vbrowser.vrs.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;



import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.localfs.LocalFSFileSystemFactory;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

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
        try
        {
            this.registryFactory(new LocalFSFileSystemFactory());
        }
        catch (VrsException e)
        {
            logger.errorPrintf("FATAL: could not register default local FS\n"); 
            logger.logException(ClassLogger.ERROR, this, e, "FATAL: could not register default local FS\n");
        } 
    }

    public VResourceSystemFactory getVResourceSystemFactoryFor(String scheme)
    {
        ArrayList<SchemeInfo> list = registeredSchemes.get(scheme); 
        if ((list==null) || (list.size()<=0)) 
        {
               return null;  
        }
        
        return list.get(0).vrsFactory;
    }
    
    public VResourceSystem getVResourceSystemFor(VRL vrl) throws VrsException
    {
        VResourceSystemFactory factory = getVResourceSystemFactoryFor(vrl.getScheme());
        
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
                resourceSystem=factory.createResourceSystemFor(vrl);
                instances.put(id, resourceSystem); 
            }
            return resourceSystem;
        }
    }
    
    protected void registryFactory(VResourceSystemFactory factory)
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
