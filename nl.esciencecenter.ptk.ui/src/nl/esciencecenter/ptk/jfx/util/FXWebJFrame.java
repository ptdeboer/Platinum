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

package nl.esciencecenter.ptk.jfx.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.URISyntaxException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class FXWebJFrame extends JFrame
{
    private static final long serialVersionUID = -8338688804088729908L;
    
    private FXWebJPanel webKitPanel;
    
    public FXWebJFrame()
    {
        initComponents(); 
    }
    
    protected void initComponents()
    {
        {
            webKitPanel = new FXWebJPanel(new BorderLayout(),true);
            getContentPane().add(webKitPanel);
        }
    }
    
    public void loadURL(final String url)
    {
        webKitPanel.loadURL(url);
    }
    
    public java.net.URI getURI() throws URISyntaxException
    {
        return webKitPanel.getURI(); 
    }
    
    public static FXWebJFrame launch(final String url)
    {
        final FXWebJFrame frame=new FXWebJFrame(); 
        
        Runnable starter=new Runnable()
        {
            public void run()
            {
                frame.setPreferredSize(new Dimension(1024, 600));
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
                frame.loadURL(url);
                
                frame.pack();
                frame.setVisible(true);
            }
        };

        SwingUtilities.invokeLater(starter);
        
        return frame; 
    }
}
