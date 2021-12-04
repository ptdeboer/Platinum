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

package nl.esciencecenter.ptk.ui.widgets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ComboBoxIconTextPanel extends JPanel implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(AutoCompleteTextField.class);

    private AutoCompleteTextField textField;
    private JLabel iconLabel;
    private ActionListener textFieldListener;
    private String comboBoxChangedCmd;
    private String comboBoxEditedCmd;
    private String comboBoxAutocompletedCmd;

    public ComboBoxIconTextPanel() {
        super();
        initGUI();
    }

    private void initGUI() {

        {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.setBorder(new BevelBorder(BevelBorder.LOWERED));
        }
        {
            iconLabel = new JLabel();
            this.add(iconLabel);
//            iconLabel.setBorder(new EmptyBorder(new Insets(1,1,1,1)));
        }
        {
            textField = new AutoCompleteTextField();
            this.add(textField);
            textField.setText("TextField", false);
//            textField.setLocation(16, 0);
        }

//        // move border from textfield to panel:
//        this.setBackground(textField.getBackground());
//        this.setBorder(textField.getBorder());
//      textField.setBorder(new BevelBorder(BevelBorder.LOWERED));
    }

    public void clearHistory() {
        textField.clearHistory();
    }

    public void setDropTarget(DropTarget dt) {
        this.iconLabel.setDropTarget(dt);
        this.textField.setDropTarget(dt);
    }

    public void setText(String txt, boolean addToHistory) {
        this.textField.setText(txt, addToHistory);
    }

    public void setIcon(Icon icon) {
        iconLabel.setIcon(icon);
        this.revalidate();
        this.repaint();
    }

    public String getText() {
        return this.textField.getText();
    }

    public void setURI(java.net.URI uri, boolean addToHistory) {
        this.setText(uri.toString(), addToHistory);
    }

    public void setTextActionListener(ActionListener listener) {
        // wrap textfield listener: 
        this.textFieldListener = listener;
        this.textField.removeActionListener(this);
        this.textField.addActionListener(this);
    }

    public void setComboActionCommands(String comboChanged, String comboAutocompleted, String comboEdited) {
        this.comboBoxChangedCmd = comboChanged;
        this.comboBoxAutocompletedCmd = comboAutocompleted;
        this.comboBoxEditedCmd = comboEdited;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        logger.debug("Event:{}", e);

        String cmd = e.getActionCommand();

        if (cmd.equals(AutoCompleteTextField.COMBOBOX_CHANGED))
            cmd = this.comboBoxChangedCmd;

        if (cmd.equals(AutoCompleteTextField.COMBOBOX_AUTOCOMPLETED))
            cmd = this.comboBoxAutocompletedCmd;

        // Hardcoded Combobox event (TODO: check origin)
        if (cmd.equals("comboBoxEdited"))
            cmd = this.comboBoxEditedCmd;


        if (cmd == null) {
            logger.debug("NULL command for event:{}", e);
            return; // filter out combo command.  
        }

        ActionEvent wrapEvent = new ActionEvent(e.getSource(), e.getID(), cmd, e.getWhen(), e.getModifiers());

        this.textFieldListener.actionPerformed(wrapEvent);
    }

    public List<String> getHistory() {
        return this.textField.getHistory();
    }

    public void setHistory(List<String> history) {
        this.textField.setHistory(history);
    }

}
