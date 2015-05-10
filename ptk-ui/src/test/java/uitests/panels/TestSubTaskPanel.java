package uitests.panels;

import javax.swing.JFrame;

import nl.esciencecenter.ptk.task.ActionTask;
import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.ui.panels.monitoring.SubTaskPanel;

public class TestSubTaskPanel {

    public static void main(String[] args) {
        ActionTask task = new ActionTask(null, "StatusPanel background Task") {
            public void doTask() {
                int max = 1000;

                ITaskMonitor monitor = this.getTaskMonitor();

                monitor.startTask("Task/SubTask Panel test", max);

                String subTask = "";

                for (int i = 0; i <= max; i++) {

                    if ((i % 10) == 0) {
                        subTask = "Subtask:" + i;
                        monitor.startSubTask(subTask, 10);
                    }

                    monitor.updateSubTaskDone(subTask, i % 10);

                    // panel.setProgress(i);
                    monitor.updateTaskDone(i); // panel.setProgress((double)i/1000.0);

                    try {
                        Thread.sleep(60 - (i * 50) / max);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void stopTask() {
            }
        };

        task.startTask();

        JFrame frame = new JFrame();
        final SubTaskPanel panel = new SubTaskPanel(task.getTaskMonitor());

        panel.start();

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
