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

import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ComboBoxIconTextPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = -3502306954828479242L;
    private AutoCompleteTextField textField;
    private JLabel iconLabel;
    private ActionListener textFieldListener;
    private String comboBoxEditedCommand;
    private String comboBoxUpdateSelectionCommand;

    public ComboBoxIconTextPanel() {
        super();
        initGUI();
    }

    private void initGUI() {
        {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        }
        {
            iconLabel = new JLabel();
            this.add(iconLabel);
        }
        {
            textField = new AutoCompleteTextField();
            this.add(textField);
            textField.setText("TextField", false);
            textField.setLocation(16, 0);
        }

        // move border from textfield to panel: 
        this.setBackground(textField.getBackground());
        this.setBorder(textField.getBorder());
        textField.setBorder(null);
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

    public void setComboActionCommand(String str) {
        this.textField.setActionCommand(str);
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

    public void setComboEditedCommand(String str) {
        this.comboBoxEditedCommand = str;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        if (cmd.equals(AutoCompleteTextField.COMBOBOXEDITED))
            cmd = this.comboBoxEditedCommand;

        if (cmd.equals(AutoCompleteTextField.UPDATESELECTION))
            cmd = this.comboBoxUpdateSelectionCommand;

        if (cmd == null)
            return; // filter out combo command.  

        ActionEvent wrapEvent = new ActionEvent(e.getSource(), e.getID(), cmd, e.getWhen(),
                e.getModifiers());

        this.textFieldListener.actionPerformed(wrapEvent);
    }

}
