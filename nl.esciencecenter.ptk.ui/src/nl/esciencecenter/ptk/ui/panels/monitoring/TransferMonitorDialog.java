/*
 * Copyrighted 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").  
 * You may not use this file except in compliance with the License. 
 * For details, see the LICENCE.txt file location in the root directory of this 
 * distribution or obtain the Apache License at the following location: 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
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

import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.task.ActionTask;
import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.task.ITaskSource;
import nl.esciencecenter.ptk.task.MonitorStats;
import nl.esciencecenter.ptk.task.TaskWatcher;
import nl.esciencecenter.ptk.task.TransferMonitor;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;

/**
 * Transfer Monitor dialog for (VFS)Transfers.
 */
public class TransferMonitorDialog extends javax.swing.JDialog implements ActionListener
{
    private static final long serialVersionUID = -8463719389609233817L;

    private static ClassLogger logger;

    static
    {
        logger = ClassLogger.getLogger(TransferMonitorDialog.class);
    }

    // === --- === //

    private JPanel buttonPanel;
    private JButton okButton;
    private TransferMonitorPanel transferPanel;
    private JButton cancelButton;
    private JTextArea logText;
    private TransferMonitor vfsTransferInfo;
    private JScrollPane logScrollPane;
    private long delay;
    private boolean suspended = false;

    ActionTask updateTask = null;

    // Not yet:
    // private ITaskSubTaskMonitor vfsTransferInfo;
    // private BrowserController browserController;

    Presentation presentation = Presentation.createDefault();

    // new monitor statistics object to move shared
    // code between VFSTransfer and default Task monitor
    private MonitorStats monitorStats = null;

    private int currentLogEventNr;

    // private BrowserController browserController;

    public TransferMonitorDialog(JFrame frame)
    {
        super(frame);
        init();
    }

    public TransferMonitorDialog(JFrame frame, TransferMonitor transfer)
    {
        super(frame);

        this.vfsTransferInfo = transfer;
        this.monitorStats = new MonitorStats(transfer);

        init();
    }

    public TransferMonitorDialog(TransferMonitor transfer)
    {
        this.vfsTransferInfo = transfer;
        this.monitorStats = new MonitorStats(transfer);
        // this.browserController=bc;

        init();
    }

    private void init()
    {
        // UIPlatform.getPlatform().getWindowRegistry().register(this);
        this.setLocationRelativeTo(null);
        initGUI();
        // initial update:
        update(false);
        //
        initUpdateTask();
    }

    protected void initUpdateTask()
    {
        updateTask = new ActionTask(TaskWatcher.getTaskWatcher(), "TransferMonitorDialog.updateTask")
        {
            public void doTask()
            {
                while (vfsTransferInfo.isDone() == false)
                {
                    if (suspended == true)
                    {
                        logger.infoPrintf("Dialog Suspended for: %s\n", vfsTransferInfo.toString());
                        return;
                    }

                    // if()
                    try
                    {
                        update(false);

                        // delayed dialog:
                        if ((monitorStats.getTotalTimeRunning() > delay) && (isVisible() == false))
                        {
                            setVisible(true);
                        }

                        if (vfsTransferInfo.isDone())
                        {

                            if (vfsTransferInfo.hasError())
                            {
                                handle("Transfer Exception!", vfsTransferInfo.getException());
                            }

                            okButton.setEnabled(true);
                            cancelButton.setEnabled(false);
                        }
                    }
                    catch (Throwable t)
                    {
                        // bugs in update():
                        logger.logException(ClassLogger.FATAL, t, "Exception during update:%s\n", t);
                    }

                    try
                    {
                        Thread.sleep(100);// 10fps
                    }
                    catch (InterruptedException e)
                    {
                        logger.logException(ClassLogger.ERROR, e, "Sleep Interrupted!");
                    }
                }// while()

                logger.infoPrintf("Post updateLoop for (done) transfer:%s\n", vfsTransferInfo.toString());

                // task done: final update to show statistics when job is done!
                update(true);

            }

            @Override
            public void stopTask()
            {
                logger.infoPrintf("stopTask() called for:%s\n", vfsTransferInfo.toString());
                // stop THIS task, not the actual Transfer Task !
                // (must use start() again to the dialog update!
                suspended = true;
            }
        };

    }

    protected void handle(String action, Throwable e)
    {
        logger.logException(ClassLogger.ERROR, e, "%s\n", action, e);
    }

    /** Restart the update task */
    public synchronized void start()
    {
        String title = "Transfering:" + vfsTransferInfo.getSource() + " to "
                + vfsTransferInfo.getDestination().getHost();// +":"+vfsTransferInfo.getStatus();

        this.setTitle(title);

        suspended = false;
        updateTask.startTask();
    }

    protected void update(boolean finalUpdate)
    {
        logger.debugPrintf(">>> Before Update\n");

        this.transferPanel.update(finalUpdate);

        logger.debugPrintf("Update total  :'%s'\n", transferPanel.getTotalTimeText());
        logger.debugPrintf("Update subTask:'%s'\n", transferPanel.getSubTimeText());

        updateLog(finalUpdate);
        
        if (this.vfsTransferInfo.isDone())
        {
            this.cancelButton.setEnabled(false);
            this.okButton.setEnabled(true);
        }
    }

    public void updateLog(boolean isFinalUpdate)
    {
        ITaskMonitor monitor = vfsTransferInfo;
        
        StringHolder textHolder=new StringHolder();
        
        if (isFinalUpdate)
        {
            monitor.getLogText(true,0,textHolder);
            // Final Update: Get All text.  
            String newText=textHolder.value; 
            this.logText.setText(newText);  
        }
        else
        {
            this.currentLogEventNr=monitor.getLogText(false,currentLogEventNr,textHolder);
            // only do incremental updates: 

            if (StringUtil.isEmpty(textHolder.value)==false) 
            {
                this.logText.append(textHolder.value); // .append(newText); 
            }
        }
            
        if (isFinalUpdate)
            this.logText.revalidate(); // Final text update sometimes gets lost, use asynchronous revalidate().
        
    }
    
    private void initGUI()
    {
        try
        {
            BorderLayout thisLayout = new BorderLayout();
            thisLayout.setHgap(8);
            thisLayout.setVgap(8);
            getContentPane().setLayout(thisLayout);
            {
                transferPanel = new TransferMonitorPanel(this.vfsTransferInfo);
                getContentPane().add(transferPanel, BorderLayout.NORTH);
            }
            {
                logScrollPane = new JScrollPane();
                getContentPane().add(logScrollPane, BorderLayout.CENTER);
                {
                    logText = new JTextArea();
                    logScrollPane.setViewportView(logText);
                    logText.setText("Logger");
                    // logText.setPreferredSize(new java.awt.Dimension(343,
                    // 58));
                    logText.setEditable(false);
                    logText.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                    // NOT logText.setPreferredSize(new java.awt.Dimension(306,
                    // 19));
                }
            }
            {
                buttonPanel = new JPanel();
                getContentPane().add(buttonPanel, BorderLayout.SOUTH);
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

            this.pack();
            this.setSize(new Dimension(600, 400));

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        this.addWindowListener(new DialogCloseListener());
    }

    public class DialogCloseListener implements WindowListener
    {
        @Override
        public void windowActivated(WindowEvent e)
        {
        }

        @Override
        public void windowClosed(WindowEvent e)
        {
        }

        @Override
        public void windowClosing(WindowEvent e)
        {
            TransferMonitorDialog.this.stop();
        }

        @Override
        public void windowDeactivated(WindowEvent e)
        {
        }

        @Override
        public void windowDeiconified(WindowEvent e)
        {
        }

        @Override
        public void windowIconified(WindowEvent e)
        {
        }

        @Override
        public void windowOpened(WindowEvent e)
        {
        }

    }

    private void setDelay(long millis)
    {
        this.delay = millis;
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == this.okButton)
        {
            dispose();
        }

        if (e.getSource() == this.cancelButton)
        {
            // stop already initiated
            if (vfsTransferInfo.isCancelled() == true)
                cancelButton.setEnabled(false);

            this.vfsTransferInfo.setIsCancelled();
        }
    }

    /**
     * Stop the update task. The running Thread is stopped. Use start() again to
     * restart the update task
     */
    public synchronized void stop()
    {
        suspended = true;

        if (this.updateTask != null)
        {
            if (this.updateTask.isAlive())
                this.updateTask.signalTerminate();
        }
    }

    public void dispose()
    {
        stop();
        this.transferPanel.dispose();
        super.dispose();
    }

    public static TransferMonitorDialog showTransferDialog(ITaskSource taskSource, TransferMonitor transfer, long delay)
    {
        return showTransferDialog(transfer, delay);
    }

    public static TransferMonitorDialog showTransferDialog(TransferMonitor transfer, long delay)
    {
        // for very short transfers do not show the dialog:

        TransferMonitorDialog dialog = new TransferMonitorDialog(transfer);
        dialog.setDelay(delay);

        if (delay <= 0)
        {
            dialog.setVisible(true);
            dialog.requestFocus();
        }

        dialog.start();
        return dialog;
    }

}
