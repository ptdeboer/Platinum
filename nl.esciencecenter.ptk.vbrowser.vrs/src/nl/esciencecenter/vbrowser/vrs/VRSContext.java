package nl.esciencecenter.vbrowser.vrs;

import java.util.Properties;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.ssl.CertificateStore;
import nl.esciencecenter.ptk.ssl.CertificateStoreException;
import nl.esciencecenter.ptk.ui.SimpelUI;
import nl.esciencecenter.ptk.ui.UI;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.Registry;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfo;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfoRegistry;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VRSContext
{
    protected Registry registry;
    
    protected VRSProperties vrsProperties;

    protected ResourceSystemInfoRegistry resourceInfoRegistry; 

    protected UI ui; 
    
    public VRSContext()
    {
        init(new VRSProperties()); 
    }

    public VRSContext(Properties props)
    {
        init(new VRSProperties(props));
    }
    
    public VRSContext(VRSProperties props)
    {
        init(props.duplicate(false)); 
    }

    private void init(VRSProperties privateProperties)
    {
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
        return info; 
    }

    public void putResourceSystemInfo(ResourceSystemInfo info)
    {
        resourceInfoRegistry.putInfo(info); 
    }

    public UI getUI()
    {
        return ui; 
    }
    
    public VRL getHomeVRL()
    {
        return new VRL("file",null,GlobalProperties.getGlobalUserHome());
    }

    public VRL getCurrentPathVRL()
    {
        return new VRL("file",null,GlobalProperties.getGlobalUserDir());
    }

    public String getUserName()
    {
        return GlobalProperties.getGlobalUserName(); 
    }

}
