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

package nl.esciencecenter.ptk.vbrowser.ui.proxy;

import java.util.List;

import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VirtualProxyNodeRootFactory extends ProxyFactory
{
    public static final String VROOT_VRL="virtual:0"; 
    
    private static VirtualProxyNodeRoot rootNode;
    
    // ========
    // instance
    // ========

    public VirtualProxyNodeRootFactory(BrowserPlatform platform)
    {
        super(platform);
    }

    @Override
    public ProxyNode doOpenLocation(VRL locator) throws ProxyException
    {
        if (locator.toString().equals(VROOT_VRL))
        {
            return this.getRoot();
        }
        
        if (getRoot().hasChild(locator))
        {
            return getRoot().getChild(locator); 
        }
        
        // delegate to ProxyNodeFactories of children: 
        List<? extends ProxyNode> nodes = getRoot().getChilds(); 

        String reasons="=== reasons ===\n";
        for (ProxyNode node:nodes)
        {
            ProxyFactory fac = node.getProxyFactory();
            StringHolder reason=new StringHolder();
            if (fac.canOpen(locator, reason)==true)
            {
                return fac.openLocation(locator); 
            }
            reasons+=reason.value+"\n"; 
        }
        
        throw new ProxyException("Unknown Virtual Node or Node not a child node:"+locator+"\n"+reasons); 
            
    }

    @Override
    public boolean canOpen(VRL locator, StringHolder reasonHolder) 
    {
        if (locator.toString().equals(VROOT_VRL))
            return true;
        
        
        try
        {
            List<? extends ProxyNode> nodes = getRoot().getChilds();
            for (ProxyNode node:nodes)
            {
                if (node.getProxyFactory().canOpen(locator, reasonHolder))
                    return true;
            }
        }
        catch (ProxyException e)
        {
            if (reasonHolder!=null)
            {
                reasonHolder.value="Got Exception:"+e+"\n"+e.getStackTrace();
                return false; 
            }
        }
        
        return false; 
    }

    public VirtualProxyNodeRoot getRoot() throws ProxyException
    {
        try
        {
            if (rootNode == null)
            {
                rootNode = new VirtualProxyNodeRoot(this, new VRL(VROOT_VRL));
            }
    
            return rootNode;
        }
        catch (VRLSyntaxException e)
        {
            throw new ProxyException("VRL Syntax Exception:" + e, e);
        }
    }

}