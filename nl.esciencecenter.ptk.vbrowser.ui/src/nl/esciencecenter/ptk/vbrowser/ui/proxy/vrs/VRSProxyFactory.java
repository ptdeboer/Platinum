package nl.esciencecenter.ptk.vbrowser.ui.proxy.vrs;

import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactory;

import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VRS;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;



/** 
 * VRS Proxy Factory for VRSProxyNodes.  
 */
public class VRSProxyFactory extends ProxyFactory
{
    private static ClassLogger logger; 
    
    static
    {
    	logger=ClassLogger.getLogger(VRSProxyFactory.class);
    }
    
	private static VRSProxyFactory instance; 
	
    private static VRSContext staticContext; 
    
    public static VRSProxyFactory getDefault() 
    {
        if (instance==null)
            instance=new VRSProxyFactory();
              
        return instance; 
    }
    
    public static synchronized VRSContext getProxyVRSContext()
    {
        if (staticContext==null)
        {
            staticContext=VRS.createVRSContext(null);
        }
        
        return staticContext;
    }
    
    // ========================================================================
    // 
    // ========================================================================

    private VRSContext vrsContext;
    
    private VRSClient vrsClient;

    protected VRSViewNodeDnDHandler viewNodeDnDHandler=null;
    
    protected VRSProxyFactory()
    {
        super(); 
        
        this.vrsContext=getProxyVRSContext();
        this.vrsClient=new VRSClient(vrsContext); 
        this.viewNodeDnDHandler=new VRSViewNodeDnDHandler(vrsClient); 
    }
    
	public VRSProxyNode _openLocation(VRL vrl) throws ProxyException
	{
		try 
		{
			return (VRSProxyNode)openLocation(vrl);
		}
		catch (Exception e) 
		{
			throw new ProxyException("Failed to open location:"+vrl+"\n"+e.getMessage(),e); 
		} 
	}
	
	// actual open location: 
	
    public VRSProxyNode doOpenLocation(VRL locator) throws ProxyException
    {
    	logger.infoPrintf(">>> doOpenLocation():%s <<<\n",locator);
    	
        try
        {
            VPath vnode=vrsClient.openLocation(createVRL(locator));
            return new VRSProxyNode(this,vnode,locator);
        }
        catch (Exception e)
        {
            throw new ProxyException("Failed to open location:"+locator+"\n"+e.getMessage(),e); 
        }
    }
    
	private VRL createVRL(VRL locator)
    {
	    return new nl.esciencecenter.vbrowser.vrs.vrl.VRL(locator.getScheme(),
	            locator.getUserinfo(),
	            locator.getHostname(),
	            locator.getPort(),
	            locator.getPath(),
	            locator.getQuery(),
	            locator.getFragment()); 
    }

    @Override
	public boolean canOpen(VRL locator,StringHolder reason) 
	{
		// internal scheme!
		if (StringUtil.equals("myvle",locator.getScheme())) 
		{
		    reason.value="Internal 'MyVle' object"; 
			return true; 
		}
		
		VResourceSystemFactory vrs = vrsClient.getVRSFactoryForScheme(locator.getScheme()); 

		if (vrs!=null)
		    return true; 
		
		reason.value="Unknown scheme:"+locator.getScheme(); 
		return false; 
	}

    public VRSViewNodeDnDHandler getVRSProxyDnDHandler(ViewNode viewNode)
    {
        return viewNodeDnDHandler;
    }

}
