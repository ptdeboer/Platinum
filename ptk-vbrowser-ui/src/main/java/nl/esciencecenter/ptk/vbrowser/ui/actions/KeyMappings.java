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

package nl.esciencecenter.ptk.vbrowser.ui.actions;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

@Slf4j
public class KeyMappings {

    public static final InputUIAction SELECT_ALL = new InputUIAction("SelectAll");
    public static final InputUIAction ESCAPE = new InputUIAction("Escape");
    public static final InputUIAction LEFT = new InputUIAction("Left");
    public static final InputUIAction RIGHT = new InputUIAction("Right");
    public static final InputUIAction UP = new InputUIAction("Up");
    public static final InputUIAction DOWN = new InputUIAction("Down");
    public static final InputUIAction ENTER = new InputUIAction("Enter");
    public static final InputUIAction MAXIMIZE = new InputUIAction("Maximize");
    public static final InputUIAction ZOOM_IN = new InputUIAction("ZoomIn");
    public static final InputUIAction ZOOM_OUT = new InputUIAction("ZoomOut");
    public static final InputUIAction ZOOM_RESET = new InputUIAction("ZoomReset");

    // Custom Actions: View, Full, Close/ALT-F4
    public static final InputUIAction OPEN_ACTION = new InputUIAction("OpenAction");
    public static final InputUIAction FULL_OPEN = new InputUIAction("FullOpen");
    public static final InputUIAction CLOSE_ACTION = new InputUIAction("CloseAction");
    public static final InputUIAction OPEN_MENU = new InputUIAction("OpenMenu");

    public static void addSelectionKeyMappings(JComponent comp, boolean whenAncestor) {
        int focusType = whenAncestor ? JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT : JComponent.WHEN_FOCUSED;
        InputMap inpMap = comp.getInputMap(focusType);

        inpMap.put(KeyStroke.getKeyStroke('A', InputEvent.CTRL_DOWN_MASK), SELECT_ALL.getName());
        inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ESCAPE.getName());

        ActionMap map = comp.getActionMap();
        map.put(SELECT_ALL.getName(), SELECT_ALL);
        map.put(ESCAPE.getName(), ESCAPE);
    }

    /***
     * Add cursor movement if not supported. JTree does support cursors movements.
     */
    public static void addMovementKeyMappings(JComponent comp, boolean whenAncestor) {
        int focusType = whenAncestor ? JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT : JComponent.WHEN_FOCUSED;
        InputMap inpMap = comp.getInputMap(focusType);

        // Warning: Overrides default cursor + selection behaviour:
        inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), LEFT.getName());
        inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), RIGHT.getName());
        inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), UP.getName());
        inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), DOWN.getName());
        inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ENTER.getName());

        ActionMap map = comp.getActionMap();
        map.put(RIGHT.getName(), RIGHT);
        map.put(LEFT.getName(), LEFT);
        map.put(UP.getName(), UP);
        map.put(DOWN.getName(), DOWN);
    }

    public static void addActionKeyMappings(JComponent comp, boolean whenAncestor) {
        int focusType = whenAncestor ? JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT : JComponent.WHEN_FOCUSED;
        InputMap inpMap = comp.getInputMap(focusType);

        // Custom:
        // 'M' and SHIFT-F10: open icon Menu
        // Space = enter directory
        // Enter = open file.
        // F = Open Full/Container
        //
        inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), OPEN_ACTION.getName());
        inpMap.put(KeyStroke.getKeyStroke('O', 0), OPEN_ACTION.getName());
        inpMap.put(KeyStroke.getKeyStroke(' ', 0), OPEN_ACTION.getName());
        inpMap.put(KeyStroke.getKeyStroke('F', 0), FULL_OPEN.getName());
        inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), MAXIMIZE.getName());
        inpMap.put(KeyStroke.getKeyStroke('M', 0), OPEN_MENU.getName());
        inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.SHIFT_DOWN_MASK), OPEN_MENU.getName());

        ActionMap map = comp.getActionMap();
        map.put(OPEN_ACTION.getName(), OPEN_ACTION);
        map.put(FULL_OPEN.getName(), FULL_OPEN);
        map.put(CLOSE_ACTION.getName(), CLOSE_ACTION);
        map.put(OPEN_MENU.getName(), OPEN_MENU);
        map.put(MAXIMIZE.getName(), MAXIMIZE);
    }

    public static void addCopyPasteKeymappings(JComponent comp) {
        // Copy Past Keyboard bindings:
        {
            InputMap imap = comp.getInputMap();
            imap.put(KeyStroke.getKeyStroke("ctrl X"),
                    TransferHandler.getCutAction().getValue(Action.NAME));
            imap.put(KeyStroke.getKeyStroke("ctrl C"),
                    TransferHandler.getCopyAction().getValue(Action.NAME));
            imap.put(KeyStroke.getKeyStroke("ctrl V"),
                    TransferHandler.getPasteAction().getValue(Action.NAME));
        }
        ActionMap map = comp.getActionMap();
        // Use TransferHandler actions:
        map.put(TransferHandler.getCutAction().getValue(Action.NAME),
                TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME),
                TransferHandler.getPasteAction());
    }

}
