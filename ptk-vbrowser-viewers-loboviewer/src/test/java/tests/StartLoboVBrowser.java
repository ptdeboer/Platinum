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

package tests;

import nl.esciencecenter.ptk.vbrowser.ui.StartVBrowser;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.viewers.loboviewer.LoboBrowser;
import nl.esciencecenter.ptk.vbrowser.viewers.loboviewer.LoboBrowserInit;
import nl.esciencecenter.vbrowser.vrs.VRSContext;


public class StartLoboVBrowser
{
    protected static BrowserPlatform platform = null;

    public static void main(String args[])
    {

        try
        {
            BrowserPlatform platform = StartVBrowser.getPlatform();

            VRSContext context = platform.getVRSContext(); 
            context.getRegistry().registerFactory(nl.esciencecenter.ptk.vbrowser.viewers.loboviewer.resfs.ResFS.class);
            platform.getViewerRegistry().registerPlugin(LoboBrowser.class);
            
            LoboBrowserInit.initPlatform(platform);
            
            StartVBrowser.startVBrowser(args); 
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // frame.setRoot(root);

    }

    
}
