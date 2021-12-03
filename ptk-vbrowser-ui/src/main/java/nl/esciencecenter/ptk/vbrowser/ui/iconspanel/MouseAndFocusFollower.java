package nl.esciencecenter.ptk.vbrowser.ui.iconspanel;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Mouse and Focus follower for both IconsPanel and IconItem.
 * When an IconItem has focus the parent panel needs to keep its border effect to avoid
 * jiggy graphics.
 */
public class MouseAndFocusFollower extends MouseAdapter implements FocusListener {

    @Override
    public void mouseEntered(MouseEvent e) {
        e.getComponent().requestFocusInWindow();
    }

    public void focusGained(FocusEvent e) {
        updateFocus(e.getComponent(), true);
    }

    @Override
    public void focusLost(FocusEvent e) {
        updateFocus(e.getComponent(), false);
    }

    /**
     * Update focus effect for both IconsPanel and IconItem.
     */
    public void updateFocus(Component component, boolean value) {
        if (component instanceof IconsPanel) {
            ((IconsPanel) component).updateFocus(value);
        }

        if (component instanceof IconItem) {
            ((IconItem) component).updateFocus(value);
            // Keep parent border enabled if child IconItem has focus.
            Container panel = component.getParent();
            if (panel instanceof IconsPanel) {
                ((IconsPanel) panel).updateFocus(value);
            }
        }
    }

}
