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

package nl.esciencecenter.ptk.vbrowser.ui.dnd;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import nl.esciencecenter.ptk.ui.icons.IconProvider;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeComponent;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeDnDHandler.DropAction;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * ViewNodeDropTarget handler, handles 'drop' on ViewNode Components. 
 * Install this DropTargetListener to support drops on the component. 
 * 
 * Swing/AWT Compatible DnD Support. 
 */
public  class ViewNodeDropTarget extends DropTarget implements DropTargetListener //, DragSourceListener,
{
    private static final long serialVersionUID = 1985854014807809151L;

    public ViewNodeDropTarget(Component comp) 
    {
    	super(comp, DnDConstants.ACTION_LINK, null,true);
    	this.setComponent(comp); 
    	setDefaultActions(DnDConstants.ACTION_LINK);
	}
    
    protected ViewNode getTargetViewNode()
    {
    	Component comp=getComponent(); 
    	if (comp instanceof ViewNodeComponent)
    	{
    		return ((ViewNodeComponent)comp).getViewNode(); 
    	}
    	return null;
    }
    
	public void _dragEnter(DropTargetDragEvent dtde)
    {
	     super.dragEnter(dtde); 
		
		// dtde.acceptDrag (DnDConstants.ACTION_LINK);
	    
		DnDUtil.debugPrintf("dragEnter:%s\n",dtde);
        Component targetComponent = dtde.getDropTargetContext().getComponent(); 
      	
        TransferHandler handler; 
        if (targetComponent instanceof JComponent)
    	{
    	    JComponent jcomp=((JComponent)targetComponent);
    	    handler = jcomp.getTransferHandler();
    	}
        
      	// handler.setDragImage(); 
        // As a ViewNodeDropTarget except only ViewNodeComponents!
        if (targetComponent instanceof ViewNodeComponent)
        {
        	ViewNode targetNode=((ViewNodeComponent)targetComponent).getViewNode();
        	
        	if (targetNode!=null)
        	{
	            //  update GUI: select the JButton
	            //((VComponent)source).setMouseOver(true);
	            // one of my component, so I accept this drop:
        		//dtde.acceptDrag (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK);
	            //DropTargetContext dtc = dtde.getDropTargetContext();
        	}
        	else
        	{
        		dtde.rejectDrag(); // error 
        	}
        	
            // Accept all DnD actions: 
            //dtde.acceptDrag(dtde.getSourceActions());
            //showUnderDrag(true);
        }
        else
        {
            //DnDUtil.warnPrintf("Received drag for NON ViewNode component:%s\n",source); 
        	dtde.rejectDrag(); // NOT here!
        }
    }
    
    public void dragOver(DropTargetDragEvent dtde)
    {
        super.dragOver(dtde); 
    	
    	DnDUtil.debugPrintf("dragOver:%s\n",dtde);
    	// done in super: 
    	// Component source = dtde.getDropTargetContext().getComponent();
    	// source.requestFocus(); 
    }

    public void dropActionChanged(DropTargetDragEvent dtde)
    {
        super.dropActionChanged(dtde);
        DnDUtil.debugPrintf("dropActionChanged:%s\n",dtde);
    }
   
    public void dragExit(DropTargetEvent dte)
    {
        super.dragExit(dte); 
        DnDUtil.debugPrintf("dragExit:%s\n",dte);
    }

    // Actual Drop!
    public void drop(DropTargetDropEvent dtde)
    {
        super.clearAutoscroll();
        DnDUtil.doDrop(dtde);
    }
  
}
