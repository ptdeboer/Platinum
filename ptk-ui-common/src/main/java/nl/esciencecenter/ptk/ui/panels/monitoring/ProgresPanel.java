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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;

/**
 * Progress Panel. Shows progress bar + percentage field + status fields and a mini ETA field.
 */
public class ProgresPanel extends javax.swing.JPanel {
    //private JPanel miniButtonPnl;
    private JTextField progressTF;
    private JProgressBar progressBar;
    private JTextField timeTF;
    private JTextField progresPercTF;
    private long todo = 1000000;

    public ProgresPanel() {
        super();
        initGUI();
    }

    private void initGUI() {
        try {
            FormLayout thisLayout = new FormLayout(
                    "3dlu, 100dlu:grow, 5dlu, max(p;50dlu), 5dlu, max(p;24dlu), 3dlu",
                    "3dlu, max(p;10dlu), 3dlu, max(p;10dlu), 5dlu");
            this.setLayout(thisLayout);
            //this.setPreferredSize(new java.awt.Dimension(699, 70));
            {
                progressBar = new JProgressBar();
                this.add(progressBar, new CellConstraints("2, 2, 3, 1, default, default"));
                progressBar.setMinimum(0);
                progressBar.setMaximum((int) todo);
            }
            //            {
            //                miniButtonPnl = new JPanel();
            //                this.add(miniButtonPnl, new CellConstraints("6, 1, 1, 1, default, default"));
            //            }
            {
                progressTF = new JTextField();
                this.add(progressTF, new CellConstraints("2, 4, 1, 1, default, default"));
                progressTF.setText(" < ? > ");
            }
            {
                progresPercTF = new JTextField();
                this.add(progresPercTF, new CellConstraints("6, 2, 1, 1, default, default"));
                progresPercTF.setText("<999.99%>");
            }
            {
                timeTF = new JTextField();
                this.add(timeTF, new CellConstraints("4, 4, 3, 1, default, default"));
                timeTF.setText("<99:99:99s (99:99:99s)>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTotal(int total) {
        this.todo = total;
        this.progressBar.setMinimum(0);
        this.progressBar.setMaximum(total);
    }

    public void setProgress(long value) {
        double perc = ((double) value) / ((double) todo);
        setProgress(perc);
    }

    public void setProgress(double value) {
        this.progressBar.setValue((int) (value * todo));
        // round to 99.99 
        value = Math.round(value * 10000.0) / 100.0;
        this.progresPercTF.setText("" + value + "%  ");
    }

    public void setProgressText(String txt) {
        this.progressTF.setText(txt);
    }

    public void setTimeText(String txt) {
        this.timeTF.setText(txt);

    }

}
