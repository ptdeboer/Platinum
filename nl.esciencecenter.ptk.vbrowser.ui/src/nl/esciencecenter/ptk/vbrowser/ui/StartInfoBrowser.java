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

package nl.esciencecenter.ptk.vbrowser.ui;

import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.browser.ProxyBrowserController;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.DnDData;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.vrs.VRSProxyFactory;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.infors.InfoRootNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Start Browser with virtual Info Resource.  
 */
public class StartInfoBrowser 
{

	public static void main(String args[])
	{
		try 
		{
			BrowserPlatform platform=BrowserPlatform.getInstance("ptkvb"); 
		    
		    ProxyBrowserController frame=(ProxyBrowserController)platform.createBrowser();
		    
		    VRSProxyFactory fac = VRSProxyFactory.createFor(platform);  
		    
		    platform.registerProxyFactory(fac); 
		    
		    VRSContext context=platform.getVRSContext();
		    VRL config=context.getHomeVRL().resolvePath(".vrsrc"); 
		    context.setPersistantConfigLocation(config, true);

		    ClassLogger.getLogger(InfoRootNode.class).setLevelToDebug();
		    
		    InfoRootNode rootNode = fac.getVRSClient().getInfoRootNode();
		    rootNode.loadPersistantConfig(); 
		    // add links:
		    rootNode.addResourceLink("Links", "Root:/",new VRL("file:///"), null); 
		    
		    ProxyNode root = fac.openLocation("info:/");
		
			frame.setRoot(root,true,true); 
			
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		
	}
}
