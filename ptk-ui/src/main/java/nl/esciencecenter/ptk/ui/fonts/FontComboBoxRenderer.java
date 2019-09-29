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

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Implementation of FontComboBoxRenderer. Render the text in the ComboBox with the font name
 * specified. Is a special component in the FontToolBar.
 * 
 * @author P.T. de Boer
 */
public class FontComboBoxRenderer extends JLabel implements ListCellRenderer {
    private boolean antiAliasing = true;

    private FontToolBar fontToolBar;

       public FontComboBoxRenderer(FontToolBar bar) {
        this.fontToolBar = bar;

        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        FontInfo info = fontToolBar.getFontInfo();

        setFont(new Font((String) value, info.getFontStyle(), 14));

        setText((String) value);

        // GuiSettings.setAntiAliasing(this,info.getAntiAliasing());

        return this;
    }

}
