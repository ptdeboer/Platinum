package nl.esciencecenter.ptk.vbrowser.ui.model;

import java.util.List;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class ViewNodeDnDHandler
{
	public enum DropAction {COPY,MOVE,LINK, PASTE}; 
	
	public ViewNodeDnDHandler() 
	{	
	}

	public ViewNodeDnDHandler(ViewNode viewNode)
    {
	    
    }

    public boolean doDrop(ViewNode targetDropNode, DropAction dropAction, List<VRL> vris)
	{
		System.err.printf("DROP:%s:%s:",dropAction,new ExtendedList<VRL>(vris));
		return true; 
	}

}
