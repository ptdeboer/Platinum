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

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusListener;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of FontComboBoxRenderer. Render the text in the ComboBox with the font name
 * specified. Is a special component in the FontToolBar.
 */
public class FontComboBoxRenderer extends JPanel implements ListCellRenderer {

    private FontToolBar fontToolBar;
    private JLabel label;

    public FontComboBoxRenderer(FontToolBar bar) {
        super();
        this.fontToolBar = bar;
        initGui();
    }

    protected void initGui() {
        // this.setLayout(new FlowLayout(FlowLayout.CENTER,0,2));
        this.setLayout(new BorderLayout(0,0));
        this.label=new JLabel();
        this.add((label),BorderLayout.CENTER);

        this.setOpaque(false);
        this.label.setOpaque(true);
        this.label.setHorizontalAlignment(SwingConstants.CENTER);
        this.label.setVerticalAlignment(SwingConstants.CENTER);
        // one pixel between menu items
        this.setBorder(new EmptyBorder(1,0,0,0));
    }
    
    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        FontItem fontItem = (FontItem)value;
        this.label.setText(fontItem.getFontName());

        if (isSelected) {
            this.setForeground(list.getSelectionForeground());
            this.setBackground(list.getSelectionBackground());
            this.label.setForeground(list.getSelectionForeground());
            this.label.setBackground(list.getSelectionBackground());

        } else {
            this.setForeground(list.getForeground());
            this.setBackground(list.getBackground());
            this.label.setForeground(list.getForeground());
            this.label.setBackground(list.getBackground());
        }
        this.label.setFont(fontItem.getCustomFont());
        this.setToolTipText(fontItem.getFontName()); // nice in case font is invisible.
        return this;
    }

}
