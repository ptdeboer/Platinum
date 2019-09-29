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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.text.DefaultCaret;

import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.task.ActionTask;
import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.task.TaskWatcher;
import nl.esciencecenter.ptk.util.StringUtil;

public class TaskMonitorDialog extends javax.swing.JDialog implements ActionListener,
        WindowListener {
       // === Static ===

    public static TaskMonitorDialog showTaskMonitorDialog(JFrame frame, ActionTask task,
            int delayMillis) {
        // for very short transfers do not show the dialog: 
        TaskMonitorDialog dialog = new TaskMonitorDialog(frame, task);
        dialog.setLocationRelativeTo(frame);
        dialog.setDelay(delayMillis);
        dialog.start(); // start monitoring, do not show yet. 
        //dialog.setVisible(true);
        return dialog;
    }

    // ========================================================================
    //
    // ========================================================================

    private TaskMonitorPanel monitorPanel;
    private JTextArea logText;
    private JScrollPane logSP;
    private JPanel LogPanel;
    //private BrowserController browserController;
    private JPanel buttonPanel;
    private JButton okButton;
    private JButton cancelButton;
    private ActionTask actionTask;
    private DockingPanel dockingPanel;
    private JScrollPane dockinSP;
    private int delay = -1;

    public TaskMonitorDialog(JFrame frame) {
        super(frame);
        //UIPlatform.getPlatform().getWindowRegistry().register(this);
        initGUI();
    }

    public TaskMonitorDialog(JFrame frame, ActionTask actionTask) {
        super(frame);
        // UIPlatform.getPlatform().getWindowRegistry().register(this);
        this.actionTask = actionTask;

        initGUI();
        initMonitoring();
    }

    private void initMonitoring() {
        this.monitorPanel.setMonitor(actionTask.getTaskMonitor());
    }

    private void initGUI() {
        try {
            BorderLayout thisLayout = new BorderLayout();
            thisLayout.setHgap(8);
            thisLayout.setVgap(8);
            getContentPane().setLayout(thisLayout);

            {
                LogPanel = new JPanel();
                getContentPane().add(LogPanel, BorderLayout.CENTER);
                BorderLayout LogPanelLayout = new BorderLayout();
                LogPanel.setLayout(LogPanelLayout);
                LogPanel.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
                {
                    logSP = new JScrollPane();
                    LogPanel.add(logSP, BorderLayout.CENTER);
                    {
                        // Warning: do NOT set size of LogText/ScrollPAne to allow auto-resize!
                        logText = new JTextArea();
                        logSP.setViewportView(logText);
                        logText.setText("\n\n");

                        DefaultCaret caret = (DefaultCaret) logText.getCaret();
                        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
                    }
                    logSP.setAutoscrolls(true);
                    logSP.setPreferredSize(new Dimension(600, 200));
                }
            }

            {
                buttonPanel = new JPanel();
                getContentPane().add(buttonPanel, BorderLayout.SOUTH);
                buttonPanel.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
                {
                    okButton = new JButton();
                    buttonPanel.add(okButton);
                    okButton.setText("OK");
                    okButton.addActionListener(this);
                    okButton.setEnabled(false);
                }
                {
                    cancelButton = new JButton();
                    buttonPanel.add(cancelButton);
                    cancelButton.setText("Cancel");
                    cancelButton.addActionListener(this);
                }
            }
            {
                //dockinSP = new JScrollPane();
                //getContentPane().add(dockinSP, BorderLayout.NORTH);

                {
                    dockingPanel = new DockingPanel();
                    //dockinSP.setViewportView(dockingPanel);
                    getContentPane().add(dockingPanel, BorderLayout.NORTH);
                    {
                        monitorPanel = new TaskMonitorPanel();
                        monitorPanel.setBorder(BorderFactory
                                .createEtchedBorder(BevelBorder.LOWERED));
                        dockingPanel.add(monitorPanel);
                    }
                }
            }

            pack();
        } catch (Exception e) {
            errorPrintf("Exception:%s\n", e);
        }

        this.addWindowListener(this);
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.okButton) {
            close();
        }

        if (e.getSource() == this.cancelButton) {
            cancel();
        }
    }

    public void cancel() {
        if (this.actionTask.isAlive()) {
            this.actionTask.signalTerminate();
        }
    }

    public void close() {
        stop();
        this.dispose();
    }

    ActionTask updateTask = new ActionTask(TaskWatcher.getTaskWatcher(), "Monitor Updater Task") {

        public void doTask() {
            while (actionTask.isAlive()) {
                update(false);

                if (actionTask.getTaskMonitor().isDone()) {
                    break;
                }
                try {
                    Thread.sleep(100);// 10fps 
                } catch (InterruptedException e) {
                    errorPrintf("Interrupted:" + e);
                }
            }

            okButton.setEnabled(true);
            cancelButton.setEnabled(false);

            // task done: final update
            update(true);
        }

        @Override
        public void stopTask() {
            // vfstransfer must stop the transfer. 
            //task.setMustStop();
        }
    };

    private int prevHeight = -1;

    private long startTime = 0;

    private int currentLogEventNr = 0;

    public void start() {
        // clear initial white space 
        this.logText.setText("");

        this.startTime = System.currentTimeMillis();
        startUpdater();
    }

    protected void errorPrintf(String format, Object... args) {
        System.err.printf(format, args);
    }

    protected void startUpdater() {
        updateTask.startTask();
    }

    public void stop() {
        if (this.updateTask.isAlive()) {
            this.updateTask.signalTerminate();
        }
    }

    protected void update(boolean isFinalUpdate) {
        if (delay >= 0) {
            if (this.isVisible() == false) {
                if (this.startTime + delay < System.currentTimeMillis())
                    this.setVisible(true);
            }
        }

        this.monitorPanel.update(isFinalUpdate);

        updateLog(isFinalUpdate);

        JPanel panels[] = this.dockingPanel.getPanels();
        for (JPanel panel : panels) {
            if (panel instanceof TransferMonitorPanel) {
                ((TransferMonitorPanel) panel).update(false);
            } else if (panel instanceof TaskMonitorPanel) {
                ((TaskMonitorPanel) panel).update(false);
            }
            // if dockingpanel has an new monitor: resize Dialog!
            int newHeight = this.dockingPanel.getPreferredSize().height;
            if (newHeight > prevHeight) {
                Dimension newSize = this.getPreferredSize();
                // do not shrink width. 
                if (newSize.width < this.getWidth())
                    newSize.width = this.getWidth();
                this.setSize(newSize);
                this.prevHeight = newHeight;
            }

            // skip
        }
    }

    public void updateLog(boolean isFinalUpdate) {
        ITaskMonitor monitor = actionTask.getTaskMonitor();

        StringHolder textHolder = new StringHolder();

        if (isFinalUpdate) {
            monitor.getLogText(true, 0, textHolder);
            this.logText.setText(textHolder.value);

        } else {
            this.currentLogEventNr = monitor.getLogText(false, currentLogEventNr, textHolder);
            // only do incremental updates: 

            if (StringUtil.isEmpty(textHolder.value) == false) {
                this.logText.append(textHolder.value); // .append(newText); 
            }
        }

        logText.revalidate();

        // if (isFinalUpdate)
        //    this.logText.revalidate(); // Final text update sometimes gets lost, use asynchronous revalidate().

    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        stop();
        close();
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    public void addSubMonitor(ITaskMonitor monitor) {
        this.dockingPanel.add(new TaskMonitorPanel(monitor));
    }

}
