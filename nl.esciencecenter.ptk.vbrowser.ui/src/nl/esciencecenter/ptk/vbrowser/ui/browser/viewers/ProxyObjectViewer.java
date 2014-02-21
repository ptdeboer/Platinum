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

package nl.esciencecenter.ptk.vbrowser.ui.browser.viewers;

import java.awt.BorderLayout;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.EmbeddedViewer;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;

/**
 * Implementation of an Image Viewer.<br>
 */
public class ProxyObjectViewer extends EmbeddedViewer
{
    // private ImageIcon icon=null;
    JScrollPane scrollPane;
    JPanel mainPanel;
    JPanel topPanel;  
    JPanel midPanel; 
    
    ViewNode viewNode;
    private JLabel iconLabel; 
    
    // private JLabel imageLabel; // store image in Label Component

    public ProxyObjectViewer(ViewNode node)
    {
        this.viewNode=node; 
    }
    
    @Override
    public String[] getMimeTypes()
    {
        return null; 
    }

    public void initGui()
    {
        {
            this.setSize(800, 600);

            BorderLayout thisLayout = new BorderLayout();
            this.setLayout(thisLayout);
            // this.setLayout(null); // absolute layout

            {
                this.scrollPane = new JScrollPane();
                this.scrollPane.setSize(800, 600);
                this.getContentPanel().add(scrollPane, BorderLayout.CENTER); // addToRootPane(imagePane,BorderLayout.CENTER);
                {
                    this.mainPanel = new JPanel();
                    scrollPane.setViewportView(mainPanel);
                    mainPanel.setLayout(new BorderLayout());
                }
                
                
            }

            this.setToolTipText(getViewerName());
            
            {
                this.iconLabel=new JLabel(); 
                mainPanel.add(iconLabel,BorderLayout.CENTER); 
            }
        }

    }

    @Override
    public void doInitViewer()
    {
        initGui();
    }
    
    @Override
    public void doStopViewer()
    {
    }

    @Override
    public void doDisposeViewer()
    {
    }

    @Override
    public String getViewerName()
    {
        // remove html color codes:
        return "ObjectViewer";
    }

    @Override
    public String getViewerClass()
    {
        return this.getClass().getCanonicalName(); 
    }

    
    @Override
    public void doStartViewer(String optMethodName) 
    {
        try
        {
            update(viewNode);
        }
        catch (Exception e)
        {
            this.notifyException("Failed to update Resource:"+viewNode,e);
        }
    }


    public void update(ViewNode node) throws Exception 
    {
        if (node==null)
        {
            return; 
        }

        notifyBusy(true);

        try
        {
            iconLabel.setIcon(node.getIcon());
            iconLabel.setText(node.getName());
        }
        catch (Exception e)
        {
            throw new VrsException(e);
        }
        finally
        {
            notifyBusy(false);
        }
    }

    @Override
    protected void doUpdateURI(URI uri)
    {
        // need viewNode;
    }

    @Override
    public Map<String, List<String>> getMimeMenuMethods()
    {
        return null;
    }

   
}
