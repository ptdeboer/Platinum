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

package nl.esciencecenter.ptk.vbrowser.ui.browser.viewers;

import nl.esciencecenter.ptk.vbrowser.ui.browser.ProxyBrowserController;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerFrame;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerPanel;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerPlugin;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.PluginRegistry;

public class ViewerManager
{
    private ProxyBrowserController browser;

    public ViewerManager(ProxyBrowserController proxyBrowser)
    {
        browser=proxyBrowser;
    }

    public ViewerPanel createViewerFor(ViewNode node,String optViewerClass) throws ProxyException
    {
        String resourceType = node.getResourceType();
        // String resourceStatus = node.getResourceStatus();
        String mimeType = node.getMimeType();
        
        return createViewerFor(resourceType,mimeType,optViewerClass); 
    }
    
    public ViewerPanel createViewerFor(String resourceType,String mimeType,String optViewerClass) throws ProxyException
    {
        PluginRegistry registry = browser.getPlatform().getViewerRegistry();

        Class<?> clazz=null; 
        
        if (optViewerClass!=null)
        {
            clazz = loadViewerClass(optViewerClass); 
        }
        
        if ((clazz==null) && (mimeType!=null))
        {
            if (clazz==null)
            {
                clazz=registry.getMimeTypeViewerClass(mimeType);
            }
        }
        
        if (clazz==null)
            return null; 
        
        if (ViewerPlugin.class.isAssignableFrom(clazz)==false)
        {
            throw new ProxyException("Viewer Class is not a ViewerPlugin class:"+clazz); 
        }
        
        ViewerPanel viewer = registry.createViewer((Class<? extends ViewerPlugin>) clazz);
        return viewer;
    }
    
    private Class<?> loadViewerClass(String optViewerClass)
    {
        try
        {
            return this.getClass().getClassLoader().loadClass(optViewerClass);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            return null; 
        } 
    }

    public ViewerFrame createViewerFrame(ViewerPanel viewerClass, boolean initViewer)
    {
        return ViewerFrame.createViewerFrame(viewerClass,initViewer);
    }
}
