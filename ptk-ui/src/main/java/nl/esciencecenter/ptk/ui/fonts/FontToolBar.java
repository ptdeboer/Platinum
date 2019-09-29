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

package nl.esciencecenter.ptk.ui.fonts;

import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * FontToolBar widget. Show a selections of system installed fonts.
 */
public class FontToolBar extends JToolBar implements ActionListener {
    // ========================================================================
    // ========================================================================
       // ========================================================================
    // ========================================================================

    // text viewer attributes:
    private String fontSizes[] = { "6", "7", "8", "9", "10", "11", "12", "13", "14", "16", "18", "20", "24", "36", "48" };

    private String[] fontFamilyNames;

    private int fontSizeEnumIndex = 9;

    private FontInfo fontInfo = new FontInfo();

    // ---

    private JComboBox fontFamilyCB;

    private JComboBox fontSizeCB = null;

    private JLabel fontLabel;

    private FontToolbarListener listener;

    private JToggleButton antiAliasingButton;

    private JToggleButton boldButton;

    private JToggleButton italicButton;

    // private JToggleButton monospacedButton;

    // ===
    // Constructor/Initializers
    // ===
    public FontToolBar() {
        initGUI();
    }

    public FontToolBar(FontToolbarListener listener) {
        this.listener = listener;
        initGUI();
    }

    public void setListener(FontToolbarListener listener) {
        this.listener = listener;
    }

    private void initFonts() {
        String systemFonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontFamilyNames = new String[2 + systemFonts.length];

        fontFamilyNames[0] = "default";
        fontFamilyNames[1] = "Monospaced";

        for (int i = 0; i < systemFonts.length; i++) {
            fontFamilyNames[i + 2] = systemFonts[i];
        }

        fontSizeEnumIndex = 9;

        fontInfo.setFontSize(new Integer(fontSizes[fontSizeEnumIndex]));
        fontInfo.setFontFamily(fontFamilyNames[0]);
    }

    public void initGUI() {
        initFonts();

        setLayout(new FlowLayout());
        this.setPreferredSize(new java.awt.Dimension(503, 42));
        {
            fontLabel = new JLabel("Font:");
            add(fontLabel);
        }
        {
            fontFamilyCB = new JComboBox();
            // FontComboBoxRenderer uses the fontname as render font
            fontFamilyCB.setRenderer(new FontComboBoxRenderer(this));
            add(fontFamilyCB);

            for (String val : fontFamilyNames) {
                fontFamilyCB.addItem(val);
            }

            fontFamilyCB.setSelectedIndex(0);
            fontFamilyCB.addActionListener(this);
            fontFamilyCB.setToolTipText("Select font type");
            fontFamilyCB.setFocusable(false);
            // DO NOT SET PREFERRED SIZE:
            // fontFamilyCB.setPreferredSize(new java.awt.Dimension(107, 22));
        }
        {
            fontSizeCB = new JComboBox();
            add(fontSizeCB);
            for (String val : fontSizes)
                fontSizeCB.addItem(val);

            fontSizeCB.setSelectedIndex(6);
            fontSizeCB.addActionListener(this);
            fontSizeCB.setToolTipText("Select font size");
            fontSizeCB.setFocusable(false);
            fontSizeCB.setPreferredSize(new java.awt.Dimension(46, 22));

        }
        {
            antiAliasingButton = new JToggleButton();
            add(antiAliasingButton);
            // antiAliasingButton.setSelected();
            antiAliasingButton.setIcon(getIconOrDefault("fonttoolbar/antialiasing.png"));
            antiAliasingButton.setActionCommand("antiAliasing");
            antiAliasingButton.setToolTipText("Toggle anti-aliasing");
            antiAliasingButton.addActionListener(this);
            // since java 1.6 not needed: rendering hints are done automatically 
            antiAliasingButton.setEnabled(true);
        }
        {
            boldButton = new JToggleButton();
            add(boldButton);
            // boldButton.setText("B");
            boldButton.setIcon(getIconOrDefault("fonttoolbar/bold.png"));
            boldButton.setActionCommand("bold");
            boldButton.setToolTipText("Toggle Bold text");
            boldButton.addActionListener(this);
        }
        {
            italicButton = new JToggleButton();
            add(italicButton);
            // boldButton.setText("B");
            italicButton.setIcon(getIconOrDefault("fonttoolbar/italic.png"));
            italicButton.setActionCommand("italic");
            italicButton.setToolTipText("Toggle Italic text");
            italicButton.addActionListener(this);
        }
    }

    private Icon getIconOrDefault(String iconstr) {
        try {
            return getIcon("icons/" + iconstr);
        } catch (FileNotFoundException e) {
            e.fillInStackTrace();
            // todo: B0rken image here. 
            return null;
        }

    }

    private Icon getIcon(String iconstr) throws FileNotFoundException {
        URL iconUrl = this.getClass().getClassLoader().getResource(iconstr);
        if (iconUrl == null)
            throw new FileNotFoundException("Coulnd't resolve iconUrl:" + iconstr);
        return new ImageIcon(iconUrl);
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if ((source == this.fontSizeCB) || (source == this.fontFamilyCB)) {
            updateFont();
        } else if (source == this.italicButton) {
            fontInfo.setItalic(italicButton.isSelected());
            updateFont();

        } else if (source == this.boldButton) {
            fontInfo.setBold(boldButton.isSelected());
            updateFont();
        } else if (source == this.antiAliasingButton) {
            fontInfo.setAntiAliasing(this.antiAliasingButton.isSelected());
            updateFont();
        }
    }

    public boolean isItalicSelected() {
        return this.italicButton.isSelected();
    }

    public boolean isBoldSelected() {
        return this.boldButton.isSelected();
    }

    public boolean isAASelected() {
        return this.antiAliasingButton.isSelected();
    }

    public void updateFont() {
        fontInfo.setFontFamily((String) fontFamilyCB.getSelectedItem());

        fontInfo.setFontSize(new Integer((String) fontSizeCB.getSelectedItem()));

        fontInfo.setAntiAliasing(this.antiAliasingButton.isSelected());

        fireUpdateEvent();
    }

    public void fireUpdateEvent() {
        if (listener != null) {
            // check antialiasing/rendering hints. 
            this.listener.updateFont(fontInfo.createFont(), fontInfo.getRenderingHints());
        }
    }

    public void setFontInfo(FontInfo info) {
        // set GUI fields:
        antiAliasingButton.setSelected(info.getRenderingHints() != null);
        boldButton.setSelected(info.isBold());
        italicButton.setSelected(info.isItalic());

        for (int i = 0; i < this.fontFamilyNames.length; i++) {
            if (info.getFontFamily().compareToIgnoreCase(fontFamilyNames[i]) == 0) {
                fontFamilyCB.setSelectedIndex(i);
                break;
            }
        }

        for (int i = 0; i < this.fontSizes.length; i++) {
            String typestr = "" + info.getFontSize();
            if (typestr.compareToIgnoreCase(fontSizes[i]) == 0) {
                this.fontSizeCB.setSelectedIndex(i);
                break;
            }
        }

        this.fontInfo = info;
    }

    /**
     * Select Font Family Name. Returns index number or -1 if not found.
     */
    public int selectFont(String familyName) {
        if (familyName == null)
            return -1;

        for (int i = 0; i < this.fontFamilyNames.length; i++) {
            if (familyName.compareToIgnoreCase(fontFamilyNames[i]) == 0) {
                fontFamilyCB.setSelectedIndex(i);
                return i;
            }
        }

        return -1;
    }

    public FontInfo getFontInfo() {
        return this.fontInfo;
    }

}
