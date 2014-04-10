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

import javax.swing.Icon;

import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Interface for Viewers which are Custom "Tools". These viewers will appears
 * under the "Tools" menu and optionally have their own ToolBar.
 */
public interface ToolPlugin
{

    public String getToolName(); 
    
    /**
     * Whether to add tool under "Tools" menu.
     */
    public boolean addToToolMenu();

    /**
     * Menu path to appear under "Tools" menu of the browser. For example
     * {"util","binary viewers"}.
     * 
     * @return array of menu path names.
     */
    public String[] getToolMenuPath();

    /**
     * Toolbar name to group other tools to the same ToolBar if createToolBar()
     * is true. If null, no toolbar will be created. 
     * 
     * @return
     */
    public String toolBarName();

    /**
     * Default method name to use when the viewer is started from the Tool Menu.
     * see {@link ViewerPanel#startViewerFor(VRL, String)}
     * 
     * @return
     */
    public String defaultToolMethod();

    /**
     * Return custom tool Icon. Parameter size indicates minimum size of icon.
     * Icons are automatically resized to fit the menu or Toolbar.<br>
     * To avoid upscaling of the icon return at least an icon with a Height &gt; size and Width &gt; size. 
     */
    public Icon getToolIcon(int size);

}
