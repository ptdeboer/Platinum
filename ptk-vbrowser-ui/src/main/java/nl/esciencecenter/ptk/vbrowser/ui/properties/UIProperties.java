package nl.esciencecenter.ptk.vbrowser.ui.properties;

import nl.esciencecenter.vbrowser.vrs.VRSProperties;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;

public class UIProperties extends VRSProperties {
    // default values:

    private static Color default_label_selected_bg_color = Color.darkGray;

    private static Color default_label_selected_fg_color = Color.black;

    private static int default_mouse_selection_button = MouseEvent.BUTTON1;

    private static int default_mouse_action_button = MouseEvent.BUTTON1;

    private static int default_mouse_alt_button = MouseEvent.BUTTON3;

    private static int default_mouse_popup_button = default_mouse_alt_button;

    // ========
    // Instance
    // ========

    public UIProperties() {
        super("UIProperties");
    }

    public UIProperties(UIProperties parent) {
        super("UIProperties", parent);
    }

    public UIProperties(Map<? extends Object, Object> properties) {
        super("UIProperties", properties, false);
    }

    public UIProperties(String name, Map<? extends Object, Object> properties) {
        super(name, properties, false);
    }

    public UIProperties duplicate() {
        return new UIProperties(getName(), getProperties());
    }

    public Color getSelectedBGColor() {
        return getColor(UIPropertyNames.LABEL_SELECTED_BG_COLOR, default_label_selected_bg_color);
    }

    public Color getSelectedFGColor() {
        return getColor(UIPropertyNames.LABEL_SELECTED_FG_COLOR, default_label_selected_fg_color);
    }

    public Color getColor(String name, Color defaultValue) {
        Object value = this.get(name);
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Color) {
            return (Color) value;
        }
        // auto decode 
        Color color = Color.decode(value.toString());
        this.set(name, color);
        return color;
    }

    public void setColor(String name, Color defaultValue) {
        this.set(name, defaultValue);
    }

    protected void initDefaults() {
        setProperty(UIPropertyNames.SINGLE_CLICK_ACTION, "" + true);
    }

    public void setProperty(String name, String value) {
        set(name, value);
    }

    /**
     * This method exists because the e.isPopupTrigger() doesn't always work under windows.
     */
    public boolean isPopupTrigger(MouseEvent e) {
        if (e.isPopupTrigger())
            return true;

        return e.getButton() == getMousePopupButton();

    }

    public int getMousePopupButton() {
        return getIntegerProperty(UIPropertyNames.MOUSE_POPUP_BUTTON, default_mouse_popup_button);
    }

    public int getMouseAltButton() {
        return getIntegerProperty(UIPropertyNames.MOUSE_ALT_BUTTON, default_mouse_alt_button);
    }

    public int getMouseSelectionButton() {
        return getIntegerProperty(UIPropertyNames.MOUSE_SELECTION_BUTTON,
                default_mouse_selection_button);
    }

    public int getMouseActionButton() {
        return getIntegerProperty(UIPropertyNames.MOUSE_SELECTION_BUTTON,
                default_mouse_action_button);
    }

    public boolean getSingleClickAction() {
        return getBooleanProperty(UIPropertyNames.SINGLE_CLICK_ACTION, true);
    }

    /**
     * Wrapper to detection 'ActionCmd Events' since the PLAF way to detect event doesn't always work.
     * Typically this is a single mouse click or a double mouse click.
     *
     * @param e
     * @return
     */
    public boolean isAction(MouseEvent e) {
        int mask = e.getModifiersEx();

        if ((mask & MouseEvent.CTRL_DOWN_MASK) > 0) {
            // CONTROL DOWN, not an action, but a selection !
            return false;
        }

        if ((mask & MouseEvent.SHIFT_DOWN_MASK) > 0) {
            // SHIFT DOWN, not an action, but a selection !
            return false;
        }

        if (e.getButton() != getMouseActionButton()) {
            return false;
        }

        if (getSingleClickAction() && (e.getClickCount() == 1)) {
            return true;
        }

        return (getSingleClickAction() == false) && (e.getClickCount() == 2);

    }

    public boolean isSelection(MouseEvent e) {
        if (e.getButton() == getMouseSelectionButton()) {
            if (getSingleClickAction() == false && (e.getClickCount() == 1)) {
                return true;
            }

            return (getSingleClickAction()) && (e.getClickCount() == 1);
        }

        return false;
    }

    public int getShowDailogDelay() {
        return getIntegerProperty(UIPropertyNames.UI_MONITOR_SHOW_DIALOG_DELAY, 2);
    }

    public boolean getAlwaysShowDailog() {
        return getBooleanProperty(UIPropertyNames.UI_MONITOR_ALWAYS_SHOW_DIALOG, true);
    }

}
