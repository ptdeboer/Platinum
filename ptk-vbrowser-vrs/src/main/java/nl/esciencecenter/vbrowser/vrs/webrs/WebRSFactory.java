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

package nl.esciencecenter.vbrowser.vrs.webrs;

import nl.esciencecenter.vbrowser.vrs.VRS;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;


public class WebRSFactory implements VResourceSystemFactory
{
	
    public static String webSchemes[]={VRS.HTTP_SCHEME,VRS.HTTPS_SCHEME}; 
//
//	@Override
//	public VResourceSystem createNewResourceSystem(VRSContext context, ServerInfo info,VRL location)
//			throws VrsException 
//	{
//		return WebResourceSystem.getClientFor(context,info,location); 
//	}

    @Override
    public String[] getSchemes()
    {
        return webSchemes; 
    }

    @Override
    public String createResourceSystemId(VRL vrl)
    {
        int port=vrl.getPort(); 
        if (port<=0)
        {
            port=VRS.getDefaultPort(vrl.getScheme());
        }
        return vrl.getScheme()+":"+vrl.getHostname()+":"+port;  
    }

    @Override
    public ResourceConfigInfo updateResourceInfo(VRSContext context,ResourceConfigInfo info, VRL vrl)
    {
        return info; 
    }

    @Override
    public nl.esciencecenter.vbrowser.vrs.VResourceSystem createResourceSystemFor(VRSContext context,ResourceConfigInfo info,VRL vrl) throws VrsException
    {
        return new WebResourceSystem(context,info); 
    }

}
