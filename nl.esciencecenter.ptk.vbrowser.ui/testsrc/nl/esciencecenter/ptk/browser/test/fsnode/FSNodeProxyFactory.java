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

package nl.esciencecenter.ptk.browser.test.fsnode;

import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.io.FSNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactory;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/** 
 * Example ProxyNode Factory based on (Generic) FSNode class. 
 */
public class FSNodeProxyFactory extends ProxyFactory
{
    private static ProxyFactory instance; 
    
    public static synchronized ProxyFactory getDefault() 
    {
        if (instance==null)
            instance=new FSNodeProxyFactory();
              
        return instance; 
   }
    // ========================================================================
    // 
    // ========================================================================

    public ProxyNode doOpenLocation(VRL locator) throws ProxyException
    {
        return new FSNodeProxyNode(this,locator); 
    }

	@Override
	public boolean canOpen(VRL locator,StringHolder reason) 
	{
	    return locator.hasScheme(FSNode.FILE_SCHEME);
	}

}