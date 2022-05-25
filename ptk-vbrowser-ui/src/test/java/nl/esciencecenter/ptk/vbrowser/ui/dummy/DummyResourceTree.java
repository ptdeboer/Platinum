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

package nl.esciencecenter.ptk.vbrowser.ui.dummy;

import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.BrowserInterfaceAdaptor;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactory;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNodeDataSourceProvider;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetree.ResourceTree;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import java.awt.*;

public class DummyResourceTree {

    public static void main(String[] args) {

        try {

            BrowserPlatform platform = StartDummyBrowser.getDummyPlatform();

            JFrame frame = new JFrame();
            JPanel panel = new JPanel();
            frame.add(panel);

            panel.setLayout(new BorderLayout());

            ResourceTree tree;

            VRL vrl = new VRL("dummy:///");
            ProxyFactory dummyFac = platform.getProxyFactoryFor(vrl);
            ProxyNode root = dummyFac.openLocation("dummy:///");

            ProxyNodeDataSourceProvider dataSource = new ProxyNodeDataSourceProvider(root);
            tree = new ResourceTree(new BrowserInterfaceAdaptor(platform), dataSource);
            //tree=new ResourceTree(null,dataSource);

            JScrollPane pane = new JScrollPane();

            pane.setViewportView(tree);
            panel.add(pane, BorderLayout.CENTER);
            frame.setSize(new Dimension(600, 400));

            //frame.pack();
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
