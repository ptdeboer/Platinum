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

import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JFrame;

import nl.esciencecenter.ptk.task.TransferMonitor;
import nl.esciencecenter.ptk.ui.panels.monitoring.TransferMonitorDialog;
import nl.esciencecenter.ptk.ui.panels.monitoring.TransferMonitorPanel;

public class TestTransferMonitorPanel {

    /**
     * Auto-generated main method to display this JDialog
     * 
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws URISyntaxException {
        int max = 1000 * 1024;
        int dif = 50 * 1024;
        int step = 1024;
        int numSources = max / dif;

        URI uris[] = new URI[numSources];
        for (int i = 0; i < numSources; i++)
            uris[i] = new URI("file", "host", "/source/file_" + i);

        JFrame frame = new JFrame();
        // dimmy:
        TransferMonitor transfer = new TransferMonitor("Transfer", new URI[] { new URI("file",
                "host", "/source") }, new URI("file", "host", "/dest"));

        TransferMonitorPanel inst = new TransferMonitorPanel(transfer);

        frame.add(inst);
        frame.pack();
        frame.setVisible(true);

        transfer.startTask("TransferTask", max);

        for (int i = 0; i <= max; i += step) {
            if (transfer.isCancelled()) {
                transfer.logPrintf("\n*** CANCELLED ***\n");
                break;
            }

            if ((i % dif) == 0) {
                transfer.startSubTask("Transfer #" + i / dif, dif);
                transfer.logPrintf("--- New Transfer ---\n -> nr=" + i / dif + "\n");
            }

            transfer.updateTaskDone(i);
            transfer.updateSourcesDone(i / dif);
            transfer.updateSubTaskDone(transfer.getCurrentSubTaskName(), i % dif);
            // do update self!
            inst.update(false);

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        inst.update(true);

        transfer.endTask(transfer.getTaskName());

    }

}
