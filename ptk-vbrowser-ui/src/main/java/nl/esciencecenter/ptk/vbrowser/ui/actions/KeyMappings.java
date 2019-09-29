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

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

import nl.esciencecenter.ptk.vbrowser.ui.dnd.DnDUtil;

public class KeyMappings {

    public static class InputAction extends UIAction {

               InputAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DnDUtil.debugPrintf("ActionPerformed:%s\n", e);
            Object source = e.getSource();
        }
    }

    public static final InputAction SELECT_ALL = new InputAction("SelectAll");

    public static final InputAction LEFT = new InputAction("Left");

    public static final InputAction RIGHT = new InputAction("Right");

    public static final InputAction UP = new InputAction("Up");

    public static final InputAction DOWN = new InputAction("Down");

    public static final InputAction ENTER = new InputAction("Enter");

    public static final InputAction MAXIMIZE = new InputAction("Maximize");

    public static final InputAction ZOOM_IN = new InputAction("ZoomIn");

    public static final InputAction ZOOM_OUT = new InputAction("ZoomOut");

    public static final InputAction ZOOM_RESET = new InputAction("ZoomReset");

    public static void addSelectionKeyMappings(JComponent comp) {
        InputMap inpMap = comp.getInputMap();

        inpMap.put(KeyStroke.getKeyStroke('A', InputEvent.CTRL_MASK), SELECT_ALL.getName());
        inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), LEFT.getName());
        inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), RIGHT.getName());
        inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), UP.getName());
        inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), DOWN.getName());
        inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ENTER.getName());
        inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK),
                MAXIMIZE.getName());

        ActionMap map = comp.getActionMap();
        map.put(SELECT_ALL.getName(), SELECT_ALL);
        map.put(RIGHT.getName(), RIGHT);
        map.put(LEFT.getName(), LEFT);
        map.put(UP.getName(), UP);
        map.put(DOWN.getName(), DOWN);
        map.put(ENTER.getName(), ENTER);
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

    public static void addZoomMappings(JComponent comp) {
        // todo:
        // InputMap inpMap = comp.getInputMap();
        // ActionMap map = comp.getActionMap();
    }
}
