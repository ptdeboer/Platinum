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

package nl.esciencecenter.ptk.ui.panels.monitoring;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.Vector;

/**
 * Docks panels in vertical Boxed JPanel container. Can be used for multiple chained (sub)Tasks.
 */
public class DockingPanel extends JPanel {
    public DockingPanel() {
        super();
        initGUI();
    }

    public void add(JPanel panel) {
        super.add(panel);
        this.revalidate();
    }

    public JPanel[] getPanels() {
        Component[] comps = this.getComponents();
        if (comps == null)
            return null;

        Vector<JPanel> panels = new Vector<JPanel>();

        for (Component comp : comps)
            if (comp instanceof JPanel)
                panels.add((JPanel) comp);

        JPanel[] arr = new JPanel[panels.size()];
        arr = panels.toArray(arr);
        return arr;
    }

    private void initGUI() {
        try {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
