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

package nl.esciencecenter.ptk.vbrowser.viewers;

import nl.esciencecenter.ptk.data.Pair;
import nl.esciencecenter.ptk.vbrowser.viewers.menu.MenuMapping;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import java.util.List;

/**
 * Interface for Viewers which are Custom "Tools". These viewers will appears under the "Tools" menu
 * and optionally have their own ToolBar.
 */
public interface ToolPlugin {

    /**
     * @return Tool Name to use in menus. Might be different then ViewerName.
     */
    String getToolName();

    /**
     * @return Whether to add tool under "Tools" menu.
     */
    boolean addToToolMenu();

    /**
     * @return Menu path to appear under "Tools" menu of the browser. For example
     * {"util","binary viewers"}.
     */
    String[] getToolMenuPath();

    /**
     * @return Toolbar name to group other tools to the same ToolBar if createToolBar() is true. If
     * null, no toolbar will be created.
     */
    String toolBarName();

    /**
     * @return Default method name to use when the viewer is started from the Tool Menu. see
     * {@link ViewerJPanel#startViewerFor(VRL, String)}
     */
    String defaultToolMethod();

    /**
     * @return Return custom tool Icon. Parameter size indicates minimum size of icon. Icons are
     * automatically resized to fit the menu or Toolbar.<br>
     * To avoid upscaling of the icon return at least an icon with a Height &gt; size and
     * Width &gt; size.
     */
    Icon getToolIcon(int size);

    /**
     * @return Returns the mapping of a MenuMapping to a list of menu methods.<br>
     */
    List<Pair<MenuMapping, List<String>>> getMenuMappings();
}
