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

package nl.esciencecenter.ptk.vbrowser.ui.proxy.vrs;

import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactory;

import nl.esciencecenter.vbrowser.vrs.task.VRSTranferManager;
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
    
    private static VRSContext staticContext; 
    
    public static VRSProxyFactory createFor(BrowserPlatform platform) 
    {
        return new VRSProxyFactory(platform);
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

    private VRSTranferManager transferManager; 
    
    protected VRSViewNodeDnDHandler viewNodeDnDHandler=null;
    
    protected VRSProxyFactory(BrowserPlatform platform)
    {
        super(platform); 
        
        this.vrsContext=getProxyVRSContext();
        this.vrsClient=new VRSClient(vrsContext);
        this.transferManager=vrsClient.getVRSTransferManager(); 
        this.viewNodeDnDHandler=new VRSViewNodeDnDHandler(transferManager); 
    }
    
    public VRSContext getVRSContext()
    {
        return vrsContext; 
    }
    
    public VRSClient getVRSClient()
    {
        return this.vrsClient; 
    }
    
    public VRSTranferManager getTransferManager()
    {
        return transferManager;
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
            VPath vnode=vrsClient.openPath(createVRL(locator));
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
