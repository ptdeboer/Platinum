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

package nl.esciencecenter.ptk.ssl;

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

/**
 * Swing accept/reject Certificate Dialog.
 */
public class CertificateDialog extends javax.swing.JDialog {

       public static final int CANCEL = -1;
    public static final int OK = 0;
    public static final int TEMPORARY = 1;
    public static final int NO = 2;

    // --- //

    private JButton temporaryButton;
    private JLabel certInfoLabel;
    private JPanel topBorderPanel;
    private JPanel topPanel;

    private JTextArea upperText;
    private JPanel buttonPanel;
    private JScrollPane scrollPane;
    private JButton cancelButton;
    private JButton yesButton;
    private JPanel borderPanel;
    private JTextArea middleText;
    private int value = CANCEL;

    public CertificateDialog(JFrame frame) {
        super(frame);
        initGUI();
        this.setTitle("Certificate Dialog");
        // center on screen 
        this.setLocationRelativeTo(null);
    }

    public void exit(int val) {
        this.value = val;
        this.setVisible(false);
    }

    private void initGUI() {
        try {
            BorderLayout thisLayout = new BorderLayout();
            getContentPane().setLayout(thisLayout);
            {
                topPanel = new JPanel();
                BorderLayout topPanelLayout = new BorderLayout();
                topPanel.setLayout(topPanelLayout);
                getContentPane().add(topPanel, BorderLayout.NORTH);
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
                getContentPane().add(borderPanel, BorderLayout.CENTER);
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
                        yesButton = new JButton();
                        buttonPanel.add(yesButton);
                        yesButton.setText("yes");
                        yesButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                exit(OK);
                            }
                        });
                    }
                    {
                        temporaryButton = new JButton();
                        buttonPanel.add(temporaryButton);
                        temporaryButton.setText("Temporary");
                        temporaryButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                exit(TEMPORARY);
                            }
                        });
                        temporaryButton.setEnabled(false);
                        temporaryButton.setVisible(false);
                    }
                    {
                        cancelButton = new JButton();
                        buttonPanel.add(cancelButton);
                        cancelButton.setText("cancel");
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

    private void setMessageText(String text) {
        this.middleText.setText(text);
    }

    private void setQuestion(String text) {
        this.upperText.setText(text);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        CertificateDialog inst = new CertificateDialog(frame);
        inst.setVisible(true);
    }

    public static int showDialog(String text, String chainMessage) {
        JFrame frame = new JFrame();
        CertificateDialog inst = new CertificateDialog(frame);
        inst.setQuestion(text);
        inst.setMessageText(chainMessage);
        inst.setModal(true);
        inst.setVisible(true);
        return inst.value;
    }

}
