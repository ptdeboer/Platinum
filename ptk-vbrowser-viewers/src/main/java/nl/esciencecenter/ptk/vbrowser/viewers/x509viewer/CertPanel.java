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

package nl.esciencecenter.ptk.vbrowser.viewers.x509viewer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;

public class CertPanel extends JPanel {
       public static final int CANCEL = -1;

    public static final int OK = 0;

    public static final int TEMPORARY = 1;

    public static final int NO = 2;

    // === instance === // 

    private JLabel certInfoLabel;
    private JPanel topBorderPanel;
    private JPanel topPanel;
    private JTextArea upperText;
    private JPanel buttonPanel;
    private JScrollPane scrollPane;
    private JButton cancelButton;
    private JButton importButton;
    private JPanel borderPanel;
    private JTextArea middleText;
    private int value = CANCEL;
    private CertPanelListener certPanelListener;
    private boolean viewOnly;

    public CertPanel() {
        super();
        initGUI();
    }

    public void exit(int val) {
        this.value = val;
        // this.setVisible(false);
        certPanelListener.optionSelected();
    }

    public void setCertPanelListener(CertPanelListener listener) {
        this.certPanelListener = listener;
    }

    private void initGUI() {
        try {
            BorderLayout thisLayout = new BorderLayout();
            setLayout(thisLayout);
            {
                topPanel = new JPanel();
                BorderLayout topPanelLayout = new BorderLayout();
                topPanel.setLayout(topPanelLayout);
                add(topPanel, BorderLayout.NORTH);
                topPanel.setPreferredSize(new java.awt.Dimension(478, 84));
                topPanel.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
                {
                    topBorderPanel = new JPanel();
                    BorderLayout topBorderPanelLayout = new BorderLayout();
                    topBorderPanel.setLayout(topBorderPanelLayout);
                    topPanel.add(topBorderPanel, BorderLayout.CENTER);
                    topBorderPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    {
                        upperText = new JTextArea();
                        topBorderPanel.add(upperText, BorderLayout.CENTER);
                        upperText.setText("text");
                        upperText.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
                        upperText.setFont(new java.awt.Font("Dialog", 1, 14));
                        upperText.setEditable(false);
                    }
                }
            }
            {
                borderPanel = new JPanel();
                BorderLayout borderPanelLayout = new BorderLayout();
                borderPanel.setLayout(borderPanelLayout);
                add(borderPanel, BorderLayout.CENTER);
                borderPanel.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
                {
                    scrollPane = new JScrollPane();
                    borderPanel.add(scrollPane, BorderLayout.CENTER);
                    scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    {
                        middleText = new JTextArea();
                        scrollPane.setViewportView(middleText);
                        middleText.setText("Certificate Text");
                        middleText.setPreferredSize(new java.awt.Dimension(454, 53));
                        middleText.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
                        middleText.setFont(new java.awt.Font("DialogInput", 0, 11));
                        middleText.setEditable(false);
                    }
                }
                {
                    buttonPanel = new JPanel();
                    borderPanel.add(buttonPanel, BorderLayout.SOUTH);
                    buttonPanel.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
                    {
                        importButton = new JButton();
                        buttonPanel.add(importButton);
                        importButton.setText("Import");
                        importButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                exit(OK);
                            }
                        });
                    }
                    {
                        cancelButton = new JButton();
                        buttonPanel.add(cancelButton);
                        cancelButton.setText("Cancel");
                        cancelButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                exit(CANCEL);
                            }
                        });
                    }
                }
                {
                    certInfoLabel = new JLabel();
                    borderPanel.add(certInfoLabel, BorderLayout.NORTH);
                    certInfoLabel.setText("Certificate Information");
                    certInfoLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                }
            }
            this.setSize(578, 322);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setMessageText(String text) {
        this.middleText.setText(text);
    }

    /**
     * Auto-generated main method to display this JDialog
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        CertPanel inst = new CertPanel();
        frame.add(inst);
        frame.pack();
        frame.setVisible(true);

    }

    // public static int showDialog(String text, String chainMessage)
    // {
    // JFrame frame = new JFrame();
    // CertPanel inst = new CertPanel();
    // frame.add(inst);
    //
    // inst.setQuestion(text);
    // inst.setMessageText(chainMessage);
    // // frame.setModal(true);
    // frame.setVisible(true);
    //
    // return inst.value;
    // }

    void setQuestion(String text) {
        this.upperText.setText(text);
    }

    public int getOption() {
        return value;
    }

    public void setViewOnly(boolean viewOnly) {
        this.viewOnly = viewOnly;
        boolean add = (viewOnly == false);

        this.importButton.setEnabled(add);
        this.importButton.setVisible(add);
        if (viewOnly)
            this.cancelButton.setText("OK");
        else
            this.cancelButton.setText("Cancel");

    }

}
