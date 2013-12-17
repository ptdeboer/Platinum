/*
 * Copyrighted 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").  
 * You may not use this file except in compliance with the License. 
 * For details, see the LICENCE.txt file location in the root directory of this 
 * distribution or obtain the Apache License at the following location: 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
 * ---
 */
// source: 

package nl.esciencecenter.ptk.vbrowser.ui.proxy;

import java.util.Vector;

import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.dummy.DummyProxyFactory;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Registry for ProxyFactories to multiple sources
 */
public class ProxyFactoryRegistry
{
    private static ProxyFactoryRegistry instance;
    
    private static ClassLogger logger; 
    
    static
    {
        logger=ClassLogger.getLogger("ProxyRegistry.class"); 
    }
    
    public static ProxyFactoryRegistry getInstance()
    {
        if (instance==null)
           instance=new ProxyFactoryRegistry(); 
        
        return instance; 
    }
    
    // ========================================================================
    //
    // ========================================================================
    
    private Vector<ProxyFactory> factories=new Vector<ProxyFactory>(); 
    
    protected ProxyFactoryRegistry()
    {
        initRegistry(); 
    }
    
    protected void initRegistry()
    {
        logger.debugPrintf("--- ProxyRegistry:initRegistry() ---\n"); 
    }
    
    public ProxyFactory getProxyFactoryFor(VRL locator)
    {
    	synchronized(this.factories)
    	{
    		for (ProxyFactory fac:factories)
    		{
    		    StringHolder reason=new StringHolder(); 
    		    
    			if (fac.canOpen(locator,reason))
    			{
    				return fac;
    			}
    			else
    			{
    			    logger.debugPrintf("Factory %s couldn't open location. Reason=%s\n",fac,reason);
    			}
    		}
    	}
    
    	return null; 
    }

    public ProxyFactory getDefaultProxyFactory()
    {
        return DummyProxyFactory.getDefault(); 
    }

    public void registerProxyFactory(ProxyFactory factory)
    {
        this.factories.add(factory); 
    }
    
    public void unregisterProxyFactory(ProxyFactory factory)
    {
        this.factories.remove(factory); 
    }
}
