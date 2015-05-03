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

package nl.esciencecenter.ptk.vbrowser.ui.iconspanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import javax.swing.JViewport;


import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.ptk.vbrowser.ui.model.UIViewModel;

/** 
 * Icons Panel layout manager. 
 * Default Icon Flow: Horizontal Flow:<br>
 * Starts icons in upper left and adds icons to the right, fitting window width, and expand downwards. 
 * List View Flow: Vertical Flow:<br>
 * Start Upper Left, adding icons downwards, fitting window height, and expand to the left. 
 *  
 * @author Piter T. de Boer.
 */
public class IconLayoutManager implements LayoutManager
{
	private static PLogger logger=PLogger.getLogger(IconLayoutManager.class); 
		
	{
	    //logger.setLevelToDebug();
	}
	
	private UIViewModel uiModel;
	private Dimension prefSize=null;
	private boolean changed=true; 
	
	public IconLayoutManager(UIViewModel model)
	{
		this.uiModel=model; 
	}

	public void setUIModel(UIViewModel model)
	{
		this.uiModel=model;
	}

	public UIViewModel getUIModel()
	{
		return this.uiModel; 
	}

	public void addLayoutComponent(String name, Component comp)
	{
		logger.debugPrintf(">>> addLayoutComponent:'%s' => %c <<<\n",name,comp); 
		this.changed=true; 
	}

	public void layoutContainer(Container parent)
	{
	    logger.debugPrintf(">>> layoutContainer() <<<\n");
		checkAlignIcons(parent,true); 
	}
	
	private Dimension checkAlignIcons(Container parent,boolean doLayout)
	{
	    Dimension newSize=alignIcons(parent,doLayout);
		this.changed=(newSize!=prefSize); 
		prefSize=newSize;
		return this.prefSize; 
	}
	
	public Dimension minimumLayoutSize(Container parent)
	{
	    logger.debugPrintf(">>> minimumLayoutSize() <<<\n");
        return checkAlignIcons(parent,false);  
	}

	public Dimension preferredLayoutSize(Container parent)
	{
	    logger.debugPrintf(">>> preferredLayoutSize() <<<\n");
	    
	    // TBI: check bug: align icons now during preferesLayoutSize calculations.  
	    return alignIcons(parent,true); 
	}
	
	public void removeLayoutComponent(Component comp)
	{
		this.changed=true; 
		logger.debugPrintf(">>> removeLayoutComponent: %s <<<\n",comp); 
	}
	
	/**
     * Custom Layout method.
     *  
     * Important: Is executed during (SWing) object lock.
     * Do not trigger new resize events to prevent an endless aligniIcons loop !  
     */

    protected Dimension alignIcons(Container container,boolean doLayout)
    {
    	logger.debugPrintf(">>> alignIcons:"+doLayout+"<<<\n"); 

        int row = 0;
        int column = 0;

        int maxy = 0;
        int maxx=0; 
        
        // Layout wihtin boundaries of current container.  
        Dimension targetSize=getTargetSize(container);  

        // get max width and max height 
        Dimension cellMaxPrefSize=new Dimension(0,0); 
        Dimension cellMaxMinSize=new Dimension(0,0); 
        
        
        Component[] childs = container.getComponents();
        
        // BODY 
        if ((childs==null) || (childs.length==0)) 
        {
            return new Dimension(0,0); 
        }
        
        // PASS I) Scan buttons for preferred and minimum sizes 
        for (Component comp : childs)
        {
            if (comp==null)
                continue;
            
            updateMax(cellMaxPrefSize,comp.getPreferredSize()); 
            updateMax(cellMaxMinSize,comp.getMinimumSize()); 
        }
        
        int cellMaxWidth=cellMaxPrefSize.width;
        int cellMaxHeight=cellMaxPrefSize.height;
                
        if (cellMaxWidth>uiModel.getMaxIconLabelWidth())
        	cellMaxWidth=uiModel.getMaxIconLabelWidth(); 
        
        // scan button for grid width and height 
        
        int currentXpos = uiModel.getIconHGap(); // start with offset 
        int currentYpos = uiModel.getIconVGap(); // start with offset 
        
        for (Component comp : childs)
        {
            logger.debugPrintf("Evaluation:%s\n",comp); 
            
            if ((comp==null) || (comp.isVisible()==false))
            {
                continue;
            }
            
            // Ia) place IconLabel
            Point currentPos=null;
            
            if (uiModel.getIconLabelPlacement()==UIViewModel.UIDirection.VERTICAL)
            {
                currentPos = new Point(currentXpos+cellMaxWidth/2-comp.getSize().width/2, currentYpos);
            }
            else
            {
                currentPos = new Point(currentXpos, currentYpos); // align to left
            }

            // Ib) Update Current Component:
            if (doLayout)
            {
                // actual update of Component: 
                comp.setLocation(currentPos);
                prefSize=comp.getPreferredSize();
                if (prefSize.width>cellMaxWidth)
                	prefSize.width=cellMaxWidth; 
                comp.setSize(prefSize); 
                //comp.validate(); // now 
            }
            
            // II) Current Icon Flow Layout stats  
            int bottom = currentPos.y + comp.getSize().height;

        	if (bottom > maxy)
        	{
        		maxy = bottom; // lowest y coordinate of this row
        	}
        	
        	int right = currentPos.x + comp.getSize().width;

        	if (right > maxx)
        	{
        		maxx = right; // rightmost x coordinate of this column
        	}
        	
        	//III) Calculate Next Position 
            if (this.uiModel.getIconLayoutDirection()==UIViewModel.UIDirection.HORIZONTAL)
            {
            	currentXpos+=cellMaxWidth+uiModel.getIconHGap();
            	column++; //next column

            	// check next position
            	if (currentXpos+cellMaxWidth >= targetSize.width)
            	{
            		// reset to xpos to left margin, increase new ypos. 
            		currentXpos = uiModel.getIconHGap();// reset to default offset
            		currentYpos = maxy + uiModel.getIconVGap(); // next row
            		//ypos += celly + browser_icon_gap_width; // next row
            		column = 0;
            		row++;
            	}
            }
            else
            {
            	currentYpos+=cellMaxHeight+uiModel.getIconVGap();
            	row++;
            	//  check next position
            	if (currentYpos+cellMaxHeight  > targetSize.height)
            	{
            		// reset to ypos to top margin, increase new xpos. 
            		currentYpos = uiModel.getIconVGap();;// reset to defaul offset
            		currentXpos = maxx + uiModel.getIconHGap(); // next row
            		//ypos += celly + browser_icon_gap_width; // next row
            		row = 0;
            		column++;
            	}
            }
        } // END for (node:nodeLocations)
       
        
        // IV) update sizes 
        // Important: alignIcons() is called during doLayout(), so 
        // NO setSize may be called, since this re-triggers a doLayout ! 
        // JScrollPane Update: 
        //   when setting the preferredSize, the ParentScrollPane 
        //   will be informed about the new size and update the croll bars. 
        
        //update with real size
        Dimension size = new Dimension(maxx,maxy);
        // check:
        return size; 
    }

	private void updateMax(Dimension maxDimension, Dimension dim)
    {
	    if (maxDimension.height<dim.height)
	        maxDimension.height=dim.height;
	    
	    if (maxDimension.width<dim.width)
	        maxDimension.width=dim.width;
    }

    private Dimension getTargetSize(Container container)
	{
        Dimension targetSize=null;
     
        // get parent: 
        Container parent=container.getParent(); 
        
        // Panel is embedded in a ScrollPane or similar widget. 
		if (parent instanceof JViewport)
        {
			// Get VISIBLE part of ViewPort as target size. 
		    // Container gran = container.getParent(); 
		    // if (gran instanceof JScrollPane)
        	JViewport vport=(JViewport)parent; 
            targetSize=vport.getExtentSize();
        }
        else 
        {
           targetSize = container.getSize();
        }
		
		logger.debugPrintf("Target Size of container=(%d,%d)\n",targetSize.width,targetSize.height);
		return targetSize; 
	}

}
