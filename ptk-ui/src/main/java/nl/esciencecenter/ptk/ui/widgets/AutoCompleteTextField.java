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
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.io.FSUtil;
import nl.esciencecenter.ptk.util.StringUtil;

public class AutoCompleteTextField extends JComboBox<String> {

    private static final Logger logger = LoggerFactory.getLogger(AutoCompleteTextField.class);

    private static final long serialVersionUID = 2531178303560053536L;

    public final static String COMBOBOX_CHANGED = "COMBOBOX_CHANGED";

    public final static String COMBOBOX_AUTOCOMPLETED = "COMBOBOX_AUTOCOMPLETED";

    // === instance === 

    private StringList history = new StringList();

    public class CBDocument extends PlainDocument {
        private static final long serialVersionUID = -7002767598883985096L;

        public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
            if (str == null)
                return;

            super.insertString(offset, str, a);
            String clear = str.replaceAll("\\p{Cntrl}", "");

            if (!StringUtil.isEmpty(clear)) {
                // doesn't work yet:
                //completeText();
            }
        }
    }

    public AutoCompleteTextField() {
        init();
    }

    private void init() {
        StringList list = new StringList();
        list.addUnique("file:/");
        list.addUnique(FSUtil.getDefault().getWorkingDirURI().toString());
        list.addUnique(FSUtil.getDefault().getUserHomeURI().toString());
        list.sort(true);
        this.setHistory(list);

        if (getEditor() != null) {
            JTextField tf = getTextField();
            if (tf != null) {
                tf.setDocument(new CBDocument());
                addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        AutoCompleteTextField.this.handleTextFieldEvent(event);
                    }
                });
            }
        }
        this.setEditable(true);
        this.setActionCommand(COMBOBOX_CHANGED);
    }

    public void setActionCommand(String comboboChangeCmd) {
        super.setActionCommand(comboboChangeCmd);
    }

    protected void handleTextFieldEvent(ActionEvent event) {
        logger.info("handleTextFieldEvent():{}", event);
        if (event.getActionCommand().equals(this.getActionCommand())) {
            addFieldToHistory();
        }
    }

    protected void completeText() {
        JTextField tf = getTextField();
        String text = tf.getText();

        ComboBoxModel<String> aModel = getModel();
        String current;

        //        StringList tmp = new StringList();
        //        for (int i = 0; i < aModel.getSize(); i++) {
        //            current = aModel.getElementAt(i).toString();
        //
        //            if (current.toLowerCase().startsWith(text.toLowerCase())) {
        //                tmp.addUnique(current);
        //            }
        //        }
        //
        //        if (!tmp.isEmpty()) {
        //            ComboBoxModel<String> tmpListModel = new DefaultComboBoxModel<String>(tmp.toArray());
        //            setModel(tmpListModel);
        //        }

        for (int index = 0; index < aModel.getSize(); index++) {
            current = aModel.getElementAt(index).toString();

            if (current.toLowerCase().startsWith(text.toLowerCase())) {
                tf.setText(current);
                tf.setSelectionStart(text.length());
                tf.setSelectionEnd(current.length());
                int currentSelected = this.getSelectedIndex();

                if (currentSelected != index) {
                    this.setActionCommand(COMBOBOX_AUTOCOMPLETED);
                    setSelectedIndex(index);
                    this.setActionCommand(COMBOBOX_CHANGED);
                }
                break;
            }
        }
    }

    public JTextField getTextField() {
        return (JTextField) getEditor().getEditorComponent();
    }

    protected void addFieldToHistory() {
        String insertedText = getTextField().getText();
        if (!StringUtil.isEmpty(insertedText) || !insertedText.equals(" ")) {
            history.addUnique(insertedText);
        }

        history.sort(true);
        updateHistoryToComboBox();
        selectField(insertedText);
    }

    protected void updateHistoryToComboBox() {
        ComboBoxModel<String> historyListModel = new DefaultComboBoxModel<String>(history.toArray());
        setModel(historyListModel);
    }

    public boolean selectField(String text) {
        int index = history.indexOf(text);
        if (index < 0)
            return false;

        if (getSelectedIndex() == index) {
            return true; // already selected 
        } else {
            String orgCmd = this.getActionCommand();
            this.setActionCommand(COMBOBOX_AUTOCOMPLETED);
            setSelectedIndex(index);
            this.setActionCommand(orgCmd);
            return true;
        }
    }

    public String getText() {
        return this.getTextField().getText();
    }

    public void setText(String txt, boolean addToHistory) {
        this.getTextField().setText(txt);
        if (addToHistory)
            addFieldToHistory();
    }

    public void setDropTarget(DropTarget dt) {
        this.getTextField().setDropTarget(dt);
    }

    public void clearHistory() {
        this.history.clear();
    }

    public List<String> getHistory() {
        return history;
    }

    public void setHistory(List<String> history) {
        StringList list = new StringList(history);
        list.sort();
        this.history = list;
        this.updateHistoryToComboBox();
    }

}
