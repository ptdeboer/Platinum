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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Mini dialog to show one Attribute.
 */
public class AttributeViewer extends JDialog implements FocusListener, ActionListener {

    private static final long serialVersionUID = -7363909981672843970L;
    // ---
    private JPanel panel;
    private JLabel attrNameLabel;
    private JTextField attrNameField;
    private JTextArea attrValueField;
    private JLabel nameLabel;
    private JTextField attrTypeField;
    private JLabel attrValueLabel;
    private JLabel attrTypeLabel;

    private Attribute attribute = new Attribute("LongAttribute", (long) 10);
    private JScrollPane attrScrollPane;

    public static void viewAttribute(Attribute attr) {
        editAttribute(attr, false);
    }

    Attribute newAttribute = null;
    private boolean editable = true;
    private JButton cancelButton;
    private JButton okButton;
    private JPanel buttonPanel;

    public static Attribute editAttribute(Attribute attr, boolean editable) {
        AttributeViewer av = new AttributeViewer(attr);
        av.setEditable(editable);
        av.setModal(true);
        av.setVisible(true);

        return av.newAttribute;
    }

    public AttributeViewer() {
        initGUI();
    }

    public void setEditable(boolean editbl) {
        this.editable = editbl;
        this.attrValueField.setEditable(editbl);
    }

    public AttributeViewer(Attribute attr) {
        this.attribute = attr;
        initGUI();
    }

    private void initGUI() {
        try {
            {
                this.setSize(379, 189);
            }
            {
                panel = new JPanel();
                getContentPane().add(panel, BorderLayout.CENTER);
                FormLayout panelLayout = new FormLayout(
                        "max(p;5dlu), right:pref, left:pref:grow,1px,left:pref:grow, 0px,max(p;5dlu)",
                        "max(p;5dlu), center:pref,10px,center:pref,center:pref,top:pref:grow,0px,max(p;10dlu)");
                panel.setLayout(panelLayout);
                panel.setPreferredSize(new java.awt.Dimension(350, 160));
                {
                    attrNameLabel = new JLabel();
                    panel.add(attrNameLabel, new CellConstraints("2, 4, 1, 1, default, default"));
                    attrNameLabel.setText("Attribute Name:");
                }
                {
                    attrNameField = new JTextField();
                    panel.add(attrNameField, new CellConstraints("3, 4, 2, 1, default, default"));
                    attrNameField.setText(attribute.getName());
                    attrNameField.setEditable(false);
                }
                {
                    attrTypeLabel = new JLabel();
                    panel.add(attrTypeLabel, new CellConstraints("2, 5, 1, 1, default, default"));
                    attrTypeLabel.setText("Attribute Type:");
                }
                {
                    attrTypeField = new JTextField();
                    panel.add(attrTypeField, new CellConstraints("3, 5, 2, 1, default, default"));
                    attrTypeField.setText(attribute.getType().toString());
                    attrTypeField.setEditable(false);
                }
                {
                    attrValueLabel = new JLabel();
                    panel.add(attrValueLabel, new CellConstraints("2, 6, 1, 1, default, default"));
                    attrValueLabel.setText("Value:");
                }
                {
                    attrScrollPane = new JScrollPane();
                    panel.add(attrScrollPane, new CellConstraints("3, 6, 3, 2, default, default"));
                    attrScrollPane.setPreferredSize(new Dimension(200, 40));
                    {
                        attrValueField = new JTextArea();
                        attrScrollPane.setViewportView(attrValueField);
                        attrValueField.setText(attribute.getStringValue());
                        attrValueField.setBorder(BorderFactory
                                .createEtchedBorder(BevelBorder.LOWERED));
                        attrValueField.setEditable(this.editable);

                    }
                }
                {
                    nameLabel = new JLabel();
                    panel.add(nameLabel, new CellConstraints("2, 2, 2, 1, center, default"));
                    nameLabel.setText("Viewing Attribute");
                }
            }
            {
                buttonPanel = new JPanel();
                getContentPane().add(buttonPanel, BorderLayout.SOUTH);
                {
                    okButton = new JButton();
                    buttonPanel.add(okButton);
                    okButton.setText("OK");
                    okButton.addActionListener(this);
                }
                {
                    cancelButton = new JButton();
                    buttonPanel.add(cancelButton);
                    cancelButton.setText("Cancel");
                    cancelButton.setVisible(this.editable);
                    cancelButton.addActionListener(this);
                }
            }

            // == listeners ===
            attrValueField.addFocusListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.validate(); // update sizet etc
        //GuiSettings.setToOptimalWindowSize(this); 
        // GuiSettings.placeToCenter(this);
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        updateAttribute();
    }

    private void updateAttribute() {
        this.newAttribute = new Attribute(attribute.getType(), attribute.getName(),
                attrValueField.getText());
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.okButton))
            updateAttribute();

        if (e.getSource().equals(this.cancelButton))
            this.newAttribute = null;

        exit();
    }

    private void exit() {
        this.setVisible(false);
    }
}
