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

package nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import nl.esciencecenter.ptk.object.Disposable;
import nl.esciencecenter.ptk.ui.dialogs.ExceptionDialog;
import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerListener;

/** 
 * Embedded Viewer Panel for VBrowser (viewers) plugins. 
 * 
 * @author Piter T. de Boer 
 */
public abstract class ViewerPanel extends JPanel implements Disposable
{
    private static final long serialVersionUID = -8312081272922735365L;

    private JPanel innerPanel;

    private URI viewedUri;

    private boolean isBusy; 
    
    private List<ViewerListener> listeners=new ArrayList<ViewerListener>(); 
    
    private PluginRegistry viewerRegistry=null;
    
    protected ViewerPanel()
    {
        this.setLayout(new BorderLayout());
    }
    
    protected void setViewerRegistry(PluginRegistry viewerRegistry)
    {
        if (this.viewerRegistry!=null)
        {
            throw new Error("Cannot set ViewerRegistry Twice!");
        }
        
        this.viewerRegistry=viewerRegistry;
    }
    
    protected PluginRegistry getViewerRegistry()
    {
        return viewerRegistry;
    }
    
    /** 
     * Add custom content to this panel.
     * @return
     */
    public JPanel getContentPanel()
    {
        return this; 
    }

    public JPanel initInnerPanel()
    {
        this.innerPanel=new JPanel();
        this.add(innerPanel,BorderLayout.CENTER); 
        this.innerPanel.setLayout(new FlowLayout());
        return innerPanel;
    }
    
    final public URI getURI()
    {
        return viewedUri; 
    }
    
    final protected void setURI(URI uri)
    {
        this.viewedUri=uri; 
    }
    
    
    final public void startViewerFor(URI newUri,String optMenuMethod)
    {
        this.setURI(newUri); 
        startViewer(optMenuMethod);
        // doUpdateURI(newUri); 
    }
    
    /** 
     * Update the Viewed Location. 
     * @param newUri
     */
    final public void updateURI(URI newURI)
    {
        setURI(newURI); 
        doUpdateURI(newURI); 
    }
    
    /** 
     * Whether Viewer has it own ScrollPane. 
     * If not the parent Component might embedd the viewer into a ScrollPanel. 
     * @return
     */
    public boolean haveOwnScrollPane()
    {
        return false; 
    }

    /** 
     * Whether to start this viewer in a StandAlone Dialog/Frame. 
     * Some Viewers are not embedded viewers and must be started in a seperate Window.  
     * @return
     */
    public boolean isStandaloneViewer()
    {
        return false;
    }

    /** 
     * Set title of master frame or Viewer tab 
     */
    public void setViewerTitle(final String name)
    {
        this.setName(name);

        // also update JFrame 
        if (isStandaloneViewer())
        {
            JFrame frame = getJFrame();
            if (frame != null)
                getJFrame().setTitle(name);
        }
        
    }
    
    /**
     * Returns parent JFrame if contained in one. Might return NULL if parent is
     * not a JFrame! use getTopLevelAncestor() to get the (AWT) toplevel
     * component.
     * 
     * @see javax.swing.JComponent#getTopLevelAncestor()
     * @return the containing JFrame or null.
     */
    final public JFrame getJFrame()
    {

        Container topcomp = this.getTopLevelAncestor();
        if (topcomp instanceof Frame)
            return ((JFrame) topcomp);

        return null;
    }

    final protected boolean hasJFrame()
    {
        return (this.getJFrame() != null);
    }
    
    final protected boolean closeViewer()
    {
        stopViewer(); 
        disposeViewer(); 
        
        if (isStandaloneViewer()==false)
            return false;
        
        JFrame frame = this.getJFrame(); 
  
        if (frame!=null)
        {
            frame.setVisible(false); 
        }
        return true;
    }
    
    @Override
    final public void dispose()
    {
        stopViewer(); 
        disposeViewer(); 
    }

    final public void initViewer()
    {
        doInitViewer(); 
    }
    
    final public void startViewer(String optMenuMethod)
    {
        doStartViewer(optMenuMethod); 
        
        // fireStarted(); 
    }
    
    final public void stopViewer()
    {
        doStopViewer(); 
        // fireStopped(); 
    }
    
    final public void disposeViewer()
    {
        doDisposeViewer(); 
        //fireDisposed(); 
    }
    
    // =========================================================================
    // Events
    // =========================================================================
    
    public void addViewerListener(ViewerListener listener)
    {
        this.listeners.add(listener); 
    }

    public void removeViewerListener(ViewerListener listener)
    {
        this.listeners.remove(listener); 
    }

    public void notifyBusy(boolean isBusy)
    {
        this.isBusy=isBusy; 
    }
    
    public boolean isBusy()
    {
        return this.isBusy;
    }
    
    /** 
     * Notify Viewer Manager or other Listeners that an Exception has occured. 
     * @param message
     * @param e
     */
    protected void notifyException(String message, Throwable ex)
    {
        ExceptionDialog.show(this, message, ex,false);
    }
    
    // =========================================================================
    // Abstract Interface 
    // =========================================================================
    
    /**
     * Initialize GUI Component of viewer. Do not start loading resource. 
     * Typically this method is called during The Swing Event Thread. 
     */
    abstract protected void doInitViewer();

    /** 
     * Start the viewer, load resources if necessary.
     */
    abstract protected void doStartViewer(String optionalMethod);

    /** 
     * Update content. 
     */
    abstract protected void doUpdateURI(URI uri);

    /**
     * Stop/suspend viewer. 
     * All background activity must stop. 
     * After a stopViewer() a startViewer() may occur to notify the viewer can be activateed again. 
     */
    abstract protected void doStopViewer();

    /**
     * Stop viewer and dispose resources. 
     * After a disposeViewer() a viewer will never be started but multiple disposeViewers() might ocure. 
     */ 
    abstract protected void doDisposeViewer();



  

}
