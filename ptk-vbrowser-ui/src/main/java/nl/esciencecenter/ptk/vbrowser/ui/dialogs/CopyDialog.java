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

package nl.esciencecenter.ptk.vbrowser.ui.dialogs;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import nl.esciencecenter.ptk.vbrowser.ui.attribute.AttributePanel;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Copy Options Dialog. Asks user what to do when copying a file to a destination
 * which already exists.
 */
public class CopyDialog extends javax.swing.JDialog implements ActionListener {

    public enum CopyOption {Overwrite, Skip, Rename, Cancel}

    private JPanel topPanel;
    private JTextField destVrl;
    private JTextField sourceTF;
    private JLabel optionLbl;
    //
    private AttributePanel sourceAttrPnl;
    private AttributePanel destAttrPnl;
    //
    private CopyOption copyOption = null;
    private JButton renameBut;
    private JButton skipBut;
    private JButton overwriteBut;
    private JCheckBox alwaysSkipCB;
    private JCheckBox alwaysOverwriteCB;
    private JButton cancelBut;

    /**
     * Display Copy options dialog when copying from source to destination and the
     * destination resource already exists.
     *
     * @param parentFrame optional parent JFrame (can be null)
     * @param sourceVrl   source VRL
     * @param sourceAttrs optional set of significant attributes from source (can be null).
     * @param destVrl     destination VRL
     * @param destAttrs   optional set of significant attributes from destination (can be null).
     * @param modal       whether dialog should be model. If true this method will only return
     *                    dialog has closed.
     * @return
     */
    public static CopyDialog showCopyDialog(final JFrame parentFrame,
                                            final VRL sourceVrl,
                                            final AttributeSet sourceAttrs,
                                            final VRL destVrl,
                                            final AttributeSet destAttrs,
                                            boolean modal) {
        final CopyDialog inst = new CopyDialog(parentFrame);

        inst.setSourceVRL(sourceVrl);
        inst.setDestVRL(destVrl);
        inst.setSourceAttrs(sourceAttrs);
        inst.setDestAttrs(destAttrs);

        inst.validate();
        inst.setToPreferredSize();
        inst.setLocationRelativeTo(null); // Center!

        if (modal)
            inst.setModalityType(ModalityType.APPLICATION_MODAL);
        inst.setVisible(true);

        return inst;
    }

    public CopyDialog(JFrame frame) {
        super(frame);
        initGUI();
    }

    public void setSourceVRL(VRL vrl) {
        this.sourceTF.setText(vrl.toString());
    }

    public void setDestVRL(VRL vrl) {
        this.destVrl.setText(vrl.toString());
    }

    public void setSourceAttrs(AttributeSet attrs) {
        this.sourceAttrPnl.setAttributes(attrs, false);
    }

    public void setDestAttrs(AttributeSet attrs) {
        this.destAttrPnl.setAttributes(attrs, false);
    }

    private void initGUI() {
        try {

            topPanel = new JPanel();
            BorderLayout topPanelLayout = new BorderLayout();
            topPanel.setLayout(topPanelLayout);
            this.getContentPane().add(topPanel, BorderLayout.CENTER);
            topPanel.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
            {
                JPanel borderPanel = new JPanel();
                BorderLayout borderPanelLayout = new BorderLayout();
                borderPanel.setLayout(borderPanelLayout);
                topPanel.add(borderPanel, BorderLayout.NORTH);
                borderPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
                borderPanel.setEnabled(false);
                {
                    JTextArea messageTA = new JTextArea();
                    borderPanel.add(messageTA, BorderLayout.CENTER);
                    messageTA.setText("Destination already exists. Want do you want to do ?");
                    messageTA.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
                }
            }
            {
                JPanel optionsPanel = new JPanel();
                FormLayout optionsPanelLayout = new FormLayout(
                        "max(p;5dlu), max(p;5dlu), 5dlu, 20dlu, 5dlu, max(p;50dlu):grow, 5dlu, max(p;5dlu)",
                        "max(p;5dlu), 5dlu, max(p;5dlu), max(p;15dlu):grow, 5dlu, 5dlu, max(p;5dlu), max(p;15dlu):grow, 5dlu, max(p;15dlu), max(p;15dlu), max(p;15dlu), 5dlu, max(p;5px)");
                optionsPanel.setLayout(optionsPanelLayout);

                topPanel.add(optionsPanel, BorderLayout.CENTER);

                optionsPanel.add(createSeperatorLbl(), new CellConstraints("2, 1, 6, 1, default, default"));
                {
                    JLabel sourceLbl = new JLabel();
                    optionsPanel.add(sourceLbl, new CellConstraints("2, 3, 3, 1, default, default"));
                    sourceLbl.setText("Source:");
                    sourceLbl.setFont(new java.awt.Font("Dialog", 1, 12));
                }
                {
                    sourceTF = new JTextField();
                    optionsPanel.add(sourceTF, new CellConstraints("6, 3, 2, 1, default, default"));
                    sourceTF.setText("<source>");
                }
                {
                    JLabel destLbl = new JLabel();
                    optionsPanel.add(destLbl, new CellConstraints("2, 7, 3, 1, default, default"));
                    destLbl.setText("Destination:");
                    destLbl.setFont(new java.awt.Font("Dialog", 1, 12));
                }
                optionsPanel.add(createSeperatorLbl(), new CellConstraints("2, 5, 6, 1, default, default"));
                {
                    destVrl = new JTextField();
                    optionsPanel.add(destVrl, new CellConstraints("6, 7, 2, 1, default, default"));
                    destVrl.setText("<dest>");
                }
                optionsPanel.add(getSourceAttrsPanel(), new CellConstraints("4, 4, 3, 1, default, default"));
                optionsPanel.add(createSeperatorLbl(), new CellConstraints("2, 9, 6, 1, default, default"));
                optionsPanel.add(getDestAttrsPanel(), new CellConstraints("4, 8, 3, 1, default, default"));
                optionsPanel.add(getAlwaysOverwriteCB(), new CellConstraints("4, 11, 3, 1, default, default"));
                optionsPanel.add(getAlwaysSkipCB(), new CellConstraints("4, 12, 3, 1, default, default"));
                optionsPanel.add(getOptionLbl(), new CellConstraints("2, 10, 3, 1, default, default"));
            }
            {
                JPanel buttonPanel = new JPanel();
                this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
                buttonPanel.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
                buttonPanel.setPreferredSize(new java.awt.Dimension(468, 39));
                {
                    buttonPanel.add(getOverwriteBut());
                    buttonPanel.add(getSkipBut());
                    buttonPanel.add(getRenameBut());
                    buttonPanel.add(getCancelBut());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setToPreferredSize() {
        this.pack();
        this.setSize(this.getPreferredSize());
    }

    protected AttributePanel getSourceAttrsPanel() {
        if (this.sourceAttrPnl == null) {
            this.sourceAttrPnl = new AttributePanel();
        }

        return sourceAttrPnl;
    }

    protected AttributePanel getDestAttrsPanel() {
        if (this.destAttrPnl == null) {
            this.destAttrPnl = new AttributePanel();
        }

        return destAttrPnl;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == this.cancelBut) {
            exit(CopyOption.Cancel);
        }

        if (source == this.overwriteBut) {
            exit(CopyOption.Overwrite);
        }

        if (source == this.renameBut) {
            exit(CopyOption.Rename);
        }

        if (source == this.skipBut) {
            exit(CopyOption.Skip);
        }

        if ((source == this.alwaysOverwriteCB) || (source == this.alwaysSkipCB)) {
            boolean sourceIsOverwrite = (source == this.alwaysOverwriteCB);

            boolean alwaysOverwrite = this.alwaysOverwriteCB.isSelected();
            boolean alwaysSkip = this.alwaysSkipCB.isSelected();

            // mutual exclusive selection:
            if (sourceIsOverwrite) {
                if (alwaysOverwrite == true) {
                    alwaysSkip = false;
                    this.alwaysSkipCB.setSelected(false);
                }
            } else {
                if (alwaysSkip == true) {
                    alwaysOverwrite = false;
                    this.alwaysOverwriteCB.setSelected(false);
                }
            }
            this.overwriteBut.setText(alwaysOverwrite ? "Overwrite all" : "Overwrite");
            this.skipBut.setText(alwaysSkip ? "Skip all" : "Skip");
        }
    }

    public synchronized void exit(CopyOption option) {
        this.copyOption = option;
        this.dispose();
        this.notifyAll();
    }

    public synchronized boolean waitForExit() {
        try {
            if (this.copyOption != null)
                return true;

            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return (this.copyOption != null);
    }

    public boolean getSkipAll() {
        return this.alwaysSkipCB.isSelected();
    }

    public boolean getOverwriteAll() {
        return this.alwaysOverwriteCB.isSelected();
    }

    /**
     * Returns selected copy option or NULL when dialog is still open and no
     * selection has been made.
     */
    public CopyOption getCopyOption() {
        return this.copyOption;
    }

    private Component getOverwriteBut() {
        if (this.overwriteBut == null) {
            this.overwriteBut = new JButton();
            overwriteBut.setText("Overwrite");
            overwriteBut.addActionListener(this);
        }
        return overwriteBut;
    }

    private JButton getSkipBut() {
        if (skipBut == null) {
            skipBut = new JButton();
            skipBut.setText("Skip");
            skipBut.addActionListener(this);
        }
        return skipBut;
    }

    private JButton getRenameBut() {
        if (renameBut == null) {
            renameBut = new JButton();
            renameBut.setText("Rename");
            renameBut.addActionListener(this);
        }
        return renameBut;
    }

    private JButton getCancelBut() {
        if (this.cancelBut == null) {
            cancelBut = new JButton();
            cancelBut.setText("Cancel");
            cancelBut.addActionListener(this);
        }
        return cancelBut;
    }

    private JCheckBox getAlwaysOverwriteCB() {
        if (alwaysOverwriteCB == null) {
            alwaysOverwriteCB = new JCheckBox();
            alwaysOverwriteCB.setText("Always overwrite");
            alwaysOverwriteCB.addActionListener(this);
        }
        return alwaysOverwriteCB;
    }

    private JCheckBox getAlwaysSkipCB() {
        if (alwaysSkipCB == null) {
            alwaysSkipCB = new JCheckBox();
            alwaysSkipCB.setText("Always skip");
            alwaysSkipCB.addActionListener(this);
        }
        return alwaysSkipCB;
    }

    private JLabel getOptionLbl() {
        if (optionLbl == null) {
            optionLbl = new JLabel();
            optionLbl.setText("Options");
            optionLbl.setFont(new java.awt.Font("Dialog", 1, 12));
        }
        return optionLbl;
    }

    private JLabel createSeperatorLbl() {
        JLabel label = new JLabel();
        label.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        return label;
    }

}
