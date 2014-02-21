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

package nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin;

/** 
 * Default interface for Viewer Plugins. 
 * All Viewer plugins extend this interface. 
 */
public interface ViewerPlugin
{
    /** 
     * Short to be display in menu.
     */
    public String getViewerName();

    /**
     * Bindings to get Actual ViewerPanel object associated with this ViewerPlugin.  
     * This means the ViewerPanel should be initialized when this method is called. 
     * Only one ViewerPanel may be associated with one ViewerPlugin.  
     * 
     * @return Actual ViewerPanel component.  
     */
    public ViewerPanel getViewerPanel();

}
