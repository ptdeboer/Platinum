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

import java.util.Hashtable;
import java.util.Map;

import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * ProxyNodeFactory 
 */
public abstract class ProxyFactory
{
    private static ClassLogger logger; 
    {
        logger=ClassLogger.getLogger(ProxyFactory.class);
    }
    
	public class ProxyCacheElement
	{
	    /** Atomic locator ! */ 
	    public final VRL locator;
	    
	    private ProxyNode node;
	    
	    public ProxyCacheElement(VRL locator)
	    {
	        this.locator=locator; 
	    }
	    
	    public synchronized ProxyNode getNode()
	    {
	        return node; 
	    }
	    
	    public synchronized void setNode(ProxyNode node)
        {
            this.node=node; 
        }
	    
	    public synchronized boolean hasNode()
        {
            return (this.node!=null);  
        }
    }
    // ========================================================================
    // 
    // ========================================================================
	
	protected boolean enableCache=true;
	
	protected Map<VRL,ProxyCacheElement> nodeCache=new Hashtable<VRL,ProxyCacheElement>();
     
    // ========================================================================
    // 
    // ========================================================================

	final public ProxyNode openLocation(String locationString) throws ProxyException
	{
	    try
	    {
	        return openLocation(new VRL(locationString)); 
    	}
	    catch (VRLSyntaxException e)
	    {
	        throw new ProxyException("VRISyntaxException",e); 
	    }          
	}

	final public void setEnableCache(boolean value)
	{  
	    this.enableCache=value; 
	}
	
	final public ProxyNode openLocation(VRL locator) throws ProxyException
	{
	    if (enableCache==false)
        {
	        return doOpenLocation(locator); 
        }
	    else
	    {
	        ProxyCacheElement cacheEl;
	        
	        synchronized(this.nodeCache)
	        {
	            cacheEl = this.nodeCache.get(locator);
	            // create new element
	            if (cacheEl==null)
	            {
	                this.nodeCache.put(locator,cacheEl=new ProxyCacheElement(locator));
                    logger.debugPrintf("+++ Cache: new element for:%s\n",locator); 
	            }
	            
                logger.debugPrintf("--- Cache: cached element for:%s\n",locator);
	        }
	        
	        ProxyNode node;
	        
	        // Now synchronized around cache element !
	        synchronized(cacheEl)
	        {
	            if (cacheEl.hasNode()==true)
	            {
	                node = cacheEl.getNode();
	                logger.debugPrintf("<<< Cache: Cache Hit: cached proxy node for:%s\n",locator);
	                return node; 
	            }
	            
                logger.debugPrintf(">>> Cache: START OpenLocation for:%s\n",locator);
	              
                // ====================
	            // Actual openLocation
                // ====================
                {
                	node=doOpenLocation(locator); 
                	cacheEl.setNode(node); 
                }
                
	            logger.debugPrintf(">>> Cache: FINISHED OpenLocation for:%s\n",locator);
	        }
	        
	        // New Node: Perform prefetch here. 
	        node.doPrefetchAttributes();
	        
            return node; 
	    }
	}
	
	public void clearCache()
	{
	   this.nodeCache.clear(); 
	}
	
    protected void handleException(String message, Exception e)
    {
        logger.logException(ClassLogger.ERROR, e, " %s\n",message); 
    }
    
	// ========================================================================
	// Abstract interface 
	// ========================================================================
	
	abstract public ProxyNode doOpenLocation(VRL locator) throws ProxyException;

	abstract public boolean canOpen(VRL locator,StringHolder reason);


}
