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
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.task.MonitorStats;
import nl.esciencecenter.ptk.task.MonitorStats.MonitorStatsType;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * TaskMonitor Panel uses ProgrssPanel as main panel and a ITaskMonitor as Monitor Source.
 */
public class TaskMonitorPanel extends JPanel implements ActionListener {
    private JTextField mainTaskTF;
    private JTextField mainTaskStatusTF;

    private JPanel progresPanel;
    private JProgressBar subProgressBar;
    private JTextField progresPercTF;
    private JTextField currentTF;
    private JProgressBar progressBar;
    private JTextField subTaskTF;
    private final Presentation presentation = Presentation.createDefault();
    private ITaskMonitor taskMonitor = null;
    private MonitorStats monitorStats;
    private boolean showTransfersSpeeds = true;

    public TaskMonitorPanel() {
        super();
        initGUI();
    }

    public TaskMonitorPanel(ITaskMonitor monitor) {
        super();
        initGUI();
        setMonitor(monitor);
    }

    public void setMonitor(ITaskMonitor monitor) {
        this.taskMonitor = monitor;
        this.monitorStats = new MonitorStats(taskMonitor);
        // update at start to initialize fields:
        update(false);
    }

    /**
     * Whether speeds in [GMK]B/s should be shown
     */
    public void setShowTransferSpeed(boolean val) {
        this.showTransfersSpeeds = val;
    }

    public void update(boolean isFinalUpdate) {
        if (taskMonitor == null)
            return;

        String subTask = this.taskMonitor.getCurrentSubTaskName();

        // Master Task:
        this.mainTaskTF.setText(getMainTaskText());
        this.mainTaskStatusTF.setText(getMainTaskStatusText());

        long todo = taskMonitor.getTaskStats().todo;

        if (todo <= 0) {
            this.progresPercTF.setText("?");
        } else {
            double value = (double) taskMonitor.getTaskStats().done / (double) todo;

            this.progressBar.setValue((int) (1000 * value));

            // round to 99.99
            value = Math.round(value * 10000.0) / 100.0;
            this.progresPercTF.setText("" + value + "%  ");
        }

        if (taskMonitor.isDone()) {
            this.progresPercTF.setText("Done.");

            if (taskMonitor.hasError()) {
                this.currentTF.setText("Error!");
            } else if (taskMonitor.isCancelled()) {
                this.currentTF.setText("Cancelled!");
            } else {
                this.currentTF.setText("Done in:"
                        + Presentation.createRelativeTimeString(
                        this.monitorStats.getTotalDoneDeltaTime(), false));
            }
        } else {
            // Sub Task if active:
            if (subTask == null) {
                this.currentTF.setText("?");
            } else {
                this.currentTF.setText(getSubTaskText());
                todo = monitorStats.getSubTaskTodo(subTask);
                if (todo > 0) {
                    double value = (double) monitorStats.getSubTaskDone(subTask) / (double) todo;
                    this.subProgressBar.setValue((int) (1000 * value));
                }
                // this.statusTF.setText(subTask);
                // this.subTaskProgresPnl.setProgressText(getSubTaskProgressText());
                // this.subProgressBar.setProgress(monitorStats.getSubTaskProgress());
            }
        }

    }

    private String getMainTaskText() {
        String task = this.taskMonitor.getTaskName();
        return task;
    }

    private String getMainTaskStatusText() {
        String taskStr = this.taskMonitor.getTaskName();

        if (monitorStats.hasSubTask(MonitorStatsType.TOTAL_BYTES_TRANSFERRED)) {
            String transferStr = monitorStats.createTotalBytesTransferredString();
            taskStr = "Total:" + transferStr;
        } else {
            taskStr = "...";
        }

        return taskStr;
    }

    private String getSubTaskText() {
        String taskStr = this.taskMonitor.getCurrentSubTaskName();

        if (monitorStats.hasSubTask(MonitorStatsType.CURRENT_BYTES_TRANSFERRED)) {
            String transferStr = monitorStats.createCurrentBytesTransferredString();
            taskStr = taskStr + " " + transferStr;
        }

        return taskStr;
    }

    public String getTitle() {
        return this.mainTaskTF.getText();
    }

    protected Container getContentPane() {
        return this;
    }

    private void initGUI() {
        try {
            {
                FormLayout transferInfoLayout = new FormLayout(
                        "5dlu, 5dlu, 16dlu,178dlu:grow, max(p;5dlu)",
                        "max(p;5dlu), max(p;8dlu), 5dlu, max(p;8dlu), 5dlu,max(p;15dlu), 5dlu");
                this.setLayout(transferInfoLayout);

                this.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
                this.add(getMainTaskTF(), new CellConstraints("2, 2, 3, 1, default, default"));
                this.add(getMainTaskStatusTF(), new CellConstraints("4, 4, 1, 1, default, default"));
                this.add(getProgresPanel(), new CellConstraints("2, 6, 3, 1, default, default"));
            }
            {
                // defaults:
                this.progressBar.setMaximum(1000);
                this.subProgressBar.setMaximum(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        // if (e.getSource()==this.okButton)
        // dispose();
        //
        // if (e.getSource()==this.cancelButton)
        // {
        // // stop already initiated
        // if (task.isCancelled())
        // cancelButton.setEnabled(false);
        //
        // this.task.signalTerminate();
        // }
    }

    /**
     * return progress information
     */
    public String getTotalProgressText() {
        ITaskMonitor info = taskMonitor;

        String progstr = "";

        String speedStr = sizeString((int) monitorStats.getTotalSpeed()) + "B/s";
        String amountStr = sizeString(info.getTaskStats().done) + " (of "
                + sizeString(info.getTaskStats().todo) + ")";

        if (info.isDone()) {
            // Final Times. no progress strings
            String finalStr = "Done:" + amountStr;

            if (showTransfersSpeeds)
                finalStr += " (" + speedStr + ")";

            long done = monitorStats.getTotalDoneTime();
            finalStr += " in " + Presentation.createRelativeTimeString(done, false);

            return finalStr;
        }

        progstr += amountStr;

        // TransferSpeed ONLY for VFS Transfers !
        if (showTransfersSpeeds)
            progstr += " (" + speedStr + ")";

        progstr += monitorStats.createTotalBytesTransferredString();

        return progstr;
    }

    public String sizeString(long size) {
        if (size < 0)
            return "?";

        return presentation.sizeString(size, true, 1, 1);
    }

    public void dispose() {
    }

    private JTextField getMainTaskTF() {
        if (mainTaskTF == null) {
            mainTaskTF = new JTextField();
            mainTaskTF.setText("Task");
        }
        return mainTaskTF;
    }

    private JTextField getMainTaskStatusTF() {
        if (mainTaskStatusTF == null) {
            mainTaskStatusTF = new JTextField();
            mainTaskStatusTF.setText("???");
        }
        return mainTaskStatusTF;
    }

    private JProgressBar getProgressBar() {
        if (progressBar == null) {
            progressBar = new JProgressBar();
        }
        return progressBar;
    }

    private JTextField getCurrentTF() {
        if (currentTF == null) {
            currentTF = new JTextField();
            currentTF.setText("Current Task");
        }
        return currentTF;
    }

    private JTextField getProgresPercTF() {
        if (progresPercTF == null) {
            progresPercTF = new JTextField();
            // alloc size 
            progresPercTF.setText("000.000%");
        }
        return progresPercTF;
    }

    private JProgressBar getSubProgressBar() {
        if (subProgressBar == null) {
            subProgressBar = new JProgressBar();
        }
        return subProgressBar;
    }

    public boolean isDone() {
        return this.taskMonitor.isDone();
    }

    private JPanel getProgresPanel() {
        if (progresPanel == null) {
            progresPanel = new JPanel();
            FormLayout progresPanelLayout = new FormLayout(
                    "max(p;5dlu), max(p;128dlu):grow, max(p;5dlu), max(p;15dlu), 5dlu, max(p;5dlu), 5dlu",
                    "max(p;5dlu), max(p;5dlu), max(p;5dlu), max(p;5dlu)");
            progresPanel.setLayout(progresPanelLayout);
            progresPanel.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
            progresPanel.add(getProgressBar(), new CellConstraints("2, 2, 3, 1, default, default"));
            progresPanel.add(getCurrentTF(), new CellConstraints("2, 3, 1, 1, default, default"));
            progresPanel.add(getProgresPercTF(),
                    new CellConstraints("6, 2, 1, 1, default, default"));
            progresPanel.add(getSubProgressBar(), new CellConstraints(
                    "4, 3, 3, 1, default, default"));
        }
        return progresPanel;
    }

}
