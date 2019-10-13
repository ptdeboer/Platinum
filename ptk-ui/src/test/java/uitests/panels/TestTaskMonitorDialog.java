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

package uitests.panels;

import nl.esciencecenter.ptk.task.ActionTask;
import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.task.MonitorStats;
import nl.esciencecenter.ptk.ui.panels.monitoring.TaskMonitorDialog;

public class TestTaskMonitorDialog {

    public static void main(String[] args) {
        testMonitorDialog(10, 50, 100, 123);
    }

    public static void testMonitorDialog(final int N, final int M, final int sleepStart,
                                         final int mulK) {
        ActionTask task = new ActionTask(null, "TaskMonitorDailog tester") {
            public void doTask() {
                ITaskMonitor monitor = this.getMonitor();

                int sleep = sleepStart;
                int sleepDelta = 0; // go faster 
                int totalTodo = N * M * mulK;

                monitor.startTask("DailogTest", N);
                monitor.startSubTask("" + MonitorStats.MonitorStatsType.TOTAL_BYTES_TRANSFERRED,
                        totalTodo);
                monitor.startSubTask("" + MonitorStats.MonitorStatsType.TOTAL_SOURCES_COPIED, N);

                for (int i = 0; i < N; i++) {
                    String subTask = "Subtask:#" + (i + 1);
                    monitor.startSubTask(""
                            + MonitorStats.MonitorStatsType.CURRENT_BYTES_TRANSFERRED, M * mulK);
                    monitor.startSubTask(subTask, M);
                    monitor.logPrintf("New subtask:%s\n", subTask);

                    for (int j = 0; j < M; j++) {

                        try {
                            Thread.sleep(sleep);
                            sleep += sleepDelta;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        int totalDone = (i * M + j) * mulK;
                        monitor.updateSubTaskDone(""
                                + MonitorStats.MonitorStatsType.TOTAL_BYTES_TRANSFERRED, totalDone);
                        monitor.updateSubTaskDone(""
                                + MonitorStats.MonitorStatsType.CURRENT_BYTES_TRANSFERRED, j * mulK);
                        monitor.updateSubTaskDone(subTask, j);

                        if (this.isCancelled()) {
                            monitor.logPrintf("***Cancelled***\n", subTask);
                            return;
                        }
                    }

                    monitor.endSubTask("" + MonitorStats.MonitorStatsType.TOTAL_BYTES_TRANSFERRED);
                    monitor.updateSubTaskDone(""
                            + MonitorStats.MonitorStatsType.TOTAL_SOURCES_COPIED, (i + 1));

                    monitor.endSubTask(subTask);
                    monitor.updateTaskDone(i);
                }

                monitor.endSubTask("" + MonitorStats.MonitorStatsType.TOTAL_SOURCES_COPIED);
                // end all
                monitor.endTask(null);
            }

            @Override
            public void stopTask() {
            }
        };

        task.startTask();

        TaskMonitorDialog.showTaskMonitorDialog(null, task, 0);

        //         
        //         frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //         frame.pack();
        //         frame.setVisible(true);

    }
}
