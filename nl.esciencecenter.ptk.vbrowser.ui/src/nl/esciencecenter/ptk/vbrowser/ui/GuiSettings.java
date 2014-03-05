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

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Map;

import nl.esciencecenter.vbrowser.vrs.VRSProperties;

public class GuiSettings
{
    public static class UIProperties extends VRSProperties
    {
        private static final long serialVersionUID = -3984791544175204642L;
        
        public UIProperties()
        {
            super("UIProperties");
        }
        
        public UIProperties(Map<? extends Object,Object> properties)
        {
            super("UIProperties",properties);
        }

    }

    // ========================================================================
    // Configurable Properties 
    // ========================================================================

    public static final String MOUSE_SELECTION_BUTTON = "ui.mouse_selection_button";

    public static final String SINGLE_CLICK_ACTION = "ui.single_click_action";

    public static final String MOUSE_ALT_BUTTON = "ui.mouse_alt_button";

    public static final String MOUSE_POPUP_BUTTON = "ui.popup_button";

    // ==============
    // Default values
    // ==============
    
    private static Color default_label_selected_bg_color = Color.darkGray;

    private static Color default_label_selected_fg_color = Color.black;

    private static int default_mouse_selection_button = MouseEvent.BUTTON1;

    private static int default_mouse_action_button = MouseEvent.BUTTON1;

    private static int default_mouse_alt_button = MouseEvent.BUTTON3;

    private static int default_mouse_popup_button = default_mouse_alt_button;

    // ========================================================================
    // Instance fields
    // ========================================================================

    // User Configuration properties:
    private UIProperties properties = new UIProperties();

    public GuiSettings()
    {
        initDefaults();
    }

    protected void initDefaults()
    {
        setProperty(SINGLE_CLICK_ACTION, "" + true);
    }

    public void setProperty(String name, String value)
    {
        properties.set(name, value);
    }

    /**
     * This method exists because the e.isPopupTrigger() doesn't always work
     * under windows.
     */
    public boolean isPopupTrigger(MouseEvent e)
    {
        if (e.isPopupTrigger())
            return true;

        if (e.getButton() == getMousePopupButton())

            return true;

        return false;
    }

    public int getMousePopupButton()
    {
        return properties.getIntegerProperty(MOUSE_POPUP_BUTTON, default_mouse_popup_button);
    }

    public int getMouseAltButton()
    {
        return properties.getIntegerProperty(MOUSE_ALT_BUTTON, default_mouse_alt_button);
    }

    public int getMouseSelectionButton()
    {
        return properties.getIntegerProperty(MOUSE_SELECTION_BUTTON, default_mouse_selection_button);
    }

    public int getMouseActionButton()
    {
        return properties.getIntegerProperty(MOUSE_SELECTION_BUTTON, default_mouse_action_button);
    }

    public boolean getSingleClickAction()
    {
        return properties.getBooleanProperty(SINGLE_CLICK_ACTION, true);
    }

    /**
     * Wrapper to detection 'Action Events' since the PLAF way to detect event
     * doesn't always work. Typically this is a single mouse click or a double
     * mouse click.
     * 
     * @param e
     * @return
     */
    public boolean isAction(MouseEvent e)
    {
        int mask = e.getModifiersEx();

        if ((mask & MouseEvent.CTRL_DOWN_MASK) > 0)
        {
            // CONTROL DOWN, not an action, but a selection !
            return false;
        }

        if ((mask & MouseEvent.SHIFT_DOWN_MASK) > 0)
        {
            // SHIFT DOWN, not an action, but a selection !
            return false;
        }

        if (e.getButton() != getMouseActionButton())
            return false;

        if (getSingleClickAction() && (e.getClickCount() == 1))
            return true;

        if ((getSingleClickAction() == false) && (e.getClickCount() == 2))
            return true;

        return false;
    }

    public boolean isSelection(MouseEvent e)
    {
        if (e.getButton() == getMouseSelectionButton())
        {
            if (getSingleClickAction() == false && (e.getClickCount() == 1))
                return true;

            if ((getSingleClickAction()) && (e.getClickCount() == 1))
                return true;
        }

        return false;
    }

    public Color getSelectedBGColor()
    {
        return default_label_selected_bg_color;
    }
    
    public Color getSelectedFGColor()
    {
        return default_label_selected_fg_color;
    }
    
    
}
