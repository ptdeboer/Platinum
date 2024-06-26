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

package nl.esciencecenter.ptk.vbrowser.ui.fsnode;

import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactory;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTable;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTableModel;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTableUpdater;

import javax.swing.*;
import java.awt.*;

public class ShowProxyNodeTable {
    public static void main(String[] args) {
        try {
            BrowserPlatform platform = BrowserPlatform.getInstance("testbrowser");

            ProxyFactory fac = new FSNodeProxyFactory(platform);

            platform.registerProxyFactory(fac);

            final ProxyNode node = fac.openLocation("file:/home/");

            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        JFrame frame = new JFrame();
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                        {
                            JPanel panel = new JPanel();
                            panel.setLayout(new BorderLayout());
                            frame.add(panel);

                            {
                                JScrollPane scrollPanel = new JScrollPane();
                                panel.add(scrollPanel, BorderLayout.CENTER);
                                {
                                    ResourceTableModel model = new ResourceTableModel(true);
                                    ResourceTable table = new ResourceTable(null, model);
                                    table.setDataProducer(new ResourceTableUpdater(null, node,
                                            model), true);
                                    scrollPanel.setViewportView(table);
                                }
                            }
                        }

                        frame.setSize(new Dimension(700, 300));
                        frame.pack();
                        frame.setVisible(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };

            SwingUtilities.invokeLater(runnable);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
