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
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDropEvent;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeComponent;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeDnDHandler.DropAction;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class DnDUtil
{
    private static ClassLogger logger=ClassLogger.getLogger("DND"); 
    
    static
    {
    	staticInit();
    }
    
    private static void staticInit()
    {
    	// logger.setLevelToDebug();
    }
    
    public static DnDTransferHandler getDefaultTransferHandler()
    {
        return DnDTransferHandler.getDefault(); 
    }

    // === logging ===
    
    public static void debugPrintf(String format,Object... args)
    {
        logger.debugPrintf("DnD:"+format,args); 
    }

    public static void debugPrintln(String message)
    {
        logger.debugPrintf("DnD:%s\n",message);
    }

    public static void warnPrintf(String format,Object... args)
    {
        logger.warnPrintf("DnD:"+format,args);
        
    }
    public static void infoPrintf(String format,Object... args)
    {
        logger.infoPrintf("DnD:"+format,args);
    }

    public static void errorPrintf(String format,Object... args)
    {
        logger.errorPrintf("DnD:"+format,args); 
    }

    public static void logException(Exception e, String format,Object... args)
    {
        logger.logException(ClassLogger.ERROR,e,"DnD:"+format,args); 
    }

    
    static public DropAction getDropAction(int dndAction)
    {
	   if ((dndAction & DnDConstants.ACTION_COPY)>0) 
       {
		   return DropAction.COPY; 
       }
       else if ((dndAction & DnDConstants.ACTION_MOVE)>0) 
       {
    	   return DropAction.MOVE; 
       }
       else if ((dndAction & DnDConstants.ACTION_LINK)>0) 
       {
    	   return DropAction.LINK; 
       }
       else
       {
    	   throw new Error("Invalid Drop Action:"+dndAction); 
       }
	}
    
    // ========================================================================================
    // Static Drop Handlers: 
    // ========================================================================================
    
    public static void doDrop(DropTargetDropEvent dtde)
    {
        // todo: see super drop() which uses delegates!
        // for now: 
        
        DnDUtil.debugPrintf("drop:%s\n",dtde);
    	
    	DropTargetContext dtc = dtde.getDropTargetContext();
    	Component comp = dtc.getComponent();
    	Point p=dtde.getLocation();
    	int dndAction=dtde.getDropAction();
    	
        // II : get data: 
    	Transferable data = dtde.getTransferable();
    	TransferHandler handler=null;
    	
    	if (comp instanceof JComponent)
    	{
    	    JComponent jcomp=((JComponent)comp);
    	    handler = jcomp.getTransferHandler(); 
    	}
    	
    	ViewNode viewNode;
    	
    	if (comp instanceof ViewNodeComponent)
    	{
    	    viewNode=((ViewNodeComponent)comp).getViewNode(); 
    	}
    	else
    	{
    	    DnDUtil.errorPrintf("handleDrop(): Received Drop for NON ViewNodeComponent:%s\n",dtde);
    	    dtde.rejectDrop(); 
    	    return; 
    	}
    	
    	// check dropped data: 
    	if (DnDData.canConvertToVRLs(data))
    	{
    		// I: accept drop: 
    		dtde.acceptDrop (dndAction);

            // Copy/Move/Link:
            DropAction dropAction=getDropAction(dndAction);
            //II: Do Drop: 
            handleActualDrop(comp,p,viewNode,data,dropAction); 
            
            // III: complete the drag ! 
            dtde.getDropTargetContext().dropComplete(true);
    	}
    	else
    	{
    		dtde.rejectDrop(); 
    	}
    	
    }

    // ========================================================================
    // Drop/Paste actions
    // ========================================================================
    
    /**
     * Paste Data call when for example CTRL-V IS called ! Supplied component
     * is the Swing Component which has the focus when CTRL-V was called !
     */
    public static boolean doPasteData(Component uiComponent,ViewNode viewNode, Transferable data)
    {
        DnDUtil.debugPrintf("doPasteData() o:"+viewNode); 
        
    	if (DnDData.canConvertToVRLs(data))
    	{
    		// I: accept drop: 
            // dtde.acceptDrop (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK);

			return handleActualDrop(uiComponent,null,viewNode,data,DropAction.PASTE); 
    	}          
            
        return false;
    }
    
    /**
     *  Interactive drop on ViewNode 
     * @param dndAction 
     */ 
    static public boolean handleActualDrop(Component uiComp,Point point,ViewNode targetViewNode,Transferable data, DropAction dndAction) 
    {
        DnDUtil.debugPrintf("interActiveDrop():%s -> %s\n",uiComp, targetViewNode);
        
        if (DnDData.canConvertToVRLs(data)==false)
        {
            DnDUtil.errorPrintf("interActiveDrop(): Unsupported Data/Flavor:%s\n",data);
            return false;
        }
        
        if (targetViewNode.getDnDHandler()==null)
        {
            DnDUtil.errorPrintf("No DNDHandler for:%s\n",targetViewNode); 
            return false;
        }
        
        boolean result=false;
        
        try
        {
            List<VRL> vris = DnDData.getVRLsFrom(data); 
            DnDUtil.debugPrintf("doInterActiveDrop(): Actual Drop on ViewNode:%s\n",targetViewNode); 
            for (int i=0;i<vris.size();i++)
            {
                DnDUtil.debugPrintf(" -vri[#%d]=%s\n",i,vris.get(i));
            }
            
            result=targetViewNode.getDnDHandler().doDrop(targetViewNode,dndAction,vris); 
        }
        catch (Exception e)
        {
            DnDUtil.logException(e,"interActiveDrop(): Couldn't get VRLs from data:%s\n",data);
        }
            
        return result; // Handled! 
    }
    

}
