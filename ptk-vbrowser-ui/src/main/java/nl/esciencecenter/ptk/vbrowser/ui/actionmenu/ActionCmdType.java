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

package nl.esciencecenter.ptk.vbrowser.ui.actionmenu;

import java.io.Serializable;

/**
 * The action method is an enum, but enums can't be extended. So ActionMethods are global (static)
 * actions which are recognized by the ProxyBrowser and it's classes. For extended actions: use
 * DynamicAction. These can be defined by subclasses and plugins and support arguments.
 */
// enums are already first class Serializable objects.
public enum ActionCmdType implements Serializable {

    SELECTION_ACTION("SelectionAction"), // usually left-click
    DEFAULT_ACTION("DefaultAction"), // double click or single left-click
    // Menu actions
    DELETE("Delete"), //
    DELETE_RECURSIVE("DeleteRecursive"), //
    SHOW_PROPERTIES("Properties"), //
    CREATE_NEW("Create"), //
    RENAME("Rename"), //
    COPY("Copy"), //
    PASTE("Paste"), //
    // Navigation actions
    REFRESH("Refresh"), //
    CREATE_NEW_WINDOW("CreateNewWindow"), //
    OPEN_LOCATION("OpenLocation"), //
    OPEN_CONTAINER("OpenContainer"), //
    OPEN_IN_NEW_WINDOW("OpenInNewWindow"), //
    // Nav Bar
    BROWSE_BACK("BrowseBack"), //
    BROWSE_FORWARD("BrowseForward"), //
    BROWSE_UP("BrowseUp"), //
    NAVBAR_LOCATION_CHANGED("LocationChanged"), //
    // Viewers
    VIEW_AS_ICONS("ViewAsIcons"), //
    VIEW_AS_ICON_LIST("ViewAsList"), //
    VIEW_AS_TABLE("ViewAsTable"), //
    VIEW_OPEN_DEFAULT("ViewDefault"), //
    VIEW_WITH("ViewWith"),
    // Tools
    STARTTOOL("StartTool"),
    // Tab Nav
    NEW_TAB("NewTab"), //
    OPEN_IN_NEW_TAB("OpenInNewTab"), //
    CLOSE_TAB("CloseTab"), //
    // Selections
    DELETE_SELECTION("DeleteSelection"), //
    COPY_SELECTION("CopySelection"), //
    //
    GLOBAL_HELP("Help"), //
    GLOBAL_ABOUT("About"),
    //
    LOOKANDFEEL("LookAndFeel"),
    LOOKANDFEEL_ENABLED("LookAndFeelEnabled"),
    SAVE_SETTINGS("SaveSettings"),
    //
    GLOBAL_SET_SINGLE_ACTION_CLICK("Global Single Click Action");
    // === Instance ===

    private final String cmdName;

    ActionCmdType(String method) {
        this.cmdName = method;
    }

    public static ActionCmdType createFrom(String methodStr) {
        if (methodStr == null) {
            return null; // null in -> null out.
        }
        for (ActionCmdType meth : ActionCmdType.values()) {
            // check both enum name and method string
            if (methodStr.equalsIgnoreCase(meth.toString()))
                return meth;

            if (methodStr.equals(meth.cmdName))
                return meth;
        }
        return null;
    }

    public String[] getArgs() {
        String[] strs = this.cmdName.split(":");
        if (strs.length < 1) {
            return null;
        }

        String[] args = strs[1].split(",");
        return args;
    }

}
