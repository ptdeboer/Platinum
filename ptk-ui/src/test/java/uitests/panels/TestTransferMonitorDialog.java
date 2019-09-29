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

public class TestTransferMonitorDialog {


    public static void main(String args[]) {

        try {
            // 50 fps
            testTransfers(new int[]{20,0}, 1000*1024,50*1024, 1024);
            testTransfers(new int[]{0,1}, 10*100*1024*1024,500*1024*1024, 100*1024);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //        testTranfers(50);

    }


    public static void testTransfers(int timeDelta[], long totalSize, int sizePerTransfer, int transferStep) throws URISyntaxException {


            int numSources = (int)(totalSize / sizePerTransfer);

            URI uris[] = new URI[numSources];
            for (int i = 0; i < numSources; i++)
                uris[i] = new URI("file", "host", "/source/file_" + i);

            JFrame frame = new JFrame();
            // dimmy:
            TransferMonitor transfer = new TransferMonitor("Transfer", new URI[] { new URI("file",
                    "host", "/source") }, new URI("file", "host", "/dest"));

            TransferMonitorDialog inst = new TransferMonitorDialog(frame, transfer);
            inst.setModal(false);
            inst.setVisible(true);
            inst.start();

            transfer.startTask("TransferTask", totalSize);

            String subTaskID = "?";

            for (int i = 0; i <= totalSize; i += transferStep) {
                if (transfer.isCancelled()) {
                    transfer.logPrintf("\n*** CANCELLED ***\n");
                    break;
                }

                int subTaskNr=(i/sizePerTransfer);

                if ((i % sizePerTransfer) == 0) {
                    subTaskID = "Transfer #" + subTaskNr;
                    transfer.startSubTask(subTaskID, sizePerTransfer);
                    transfer.logPrintf("--- New Transfer:%s ---\n -> nr=%d \n", subTaskID,subTaskNr);
                }

                transfer.updateTaskDone(i);
                transfer.updateSourcesDone(i / sizePerTransfer);
                transfer.updateSubTaskDone(subTaskID, i % sizePerTransfer);

                try {
                    Thread.sleep(timeDelta[0],timeDelta[1]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // inclusive boundaries:
                transfer.updateSubTaskDone(subTaskID,  sizePerTransfer);

            }

            // inclusive boundaries: -> update to 100% (note for implementation)
            transfer.updateTaskDone(totalSize); // -> reach 100% :-)
            transfer.endTask(null);
       }
}
