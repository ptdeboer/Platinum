/*
 * Copyright 2006-2010 Virtual Laboratory for e-Science (www.vl-e.nl)
 * Copyright 2012-2013 Netherlands eScience Center.
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

package nl.esciencecenter.ptk.vbrowser.ui.attribute;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;

public class AttributeEditorController implements ActionListener, WindowListener {
    private final static PLogger logger = PLogger.getLogger(AttributeEditorController.class);

    protected JFrame standAloneFrame = null;

    protected AttributeEditorForm attrEditorDialog = null;

    /**
     * Whether the attribute list may be extended.
     */
    protected boolean extendable = true;

    protected boolean isOk = true;

    public AttributeEditorController(AttributeEditorForm srbDialog) {
        this.attrEditorDialog = srbDialog;
    }

    public void update() {
        // get attribute from attribute panel and update attribute object
        Attribute attrs[] = attrEditorDialog.infoPanel.getAttributes();

        if (attrs == null) {
            logger.debugPrintf("AttributeEditorController: null Attributes\n");
            return;
        }
        for (int i = 0; i < attrs.length; i++) {
            logger.debugPrintf("Attr[%d]=%s\n", i, attrs[i]);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.attrEditorDialog.okButton) {
            // store but do not save
            update();

            isOk = true;
            Exit();

        } else if (e.getSource() == this.attrEditorDialog.cancelButton) {
            isOk = false;
            Exit();
        } else if (e.getSource() == this.attrEditorDialog.resetButton) {
            attrEditorDialog.setAttributes(this.attrEditorDialog.originalAttributes);
        }
    }

    public synchronized void Exit() {
        attrEditorDialog.Exit();

        // need to dispose of standalone frame also.

        if (standAloneFrame != null) {
            standAloneFrame.dispose();
        }
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        Exit();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

}
