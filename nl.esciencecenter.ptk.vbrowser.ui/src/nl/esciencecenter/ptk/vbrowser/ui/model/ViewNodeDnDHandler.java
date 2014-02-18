package nl.esciencecenter.ptk.vbrowser.ui.model;

import java.util.List;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/** 
 * Default DnD Handler for ViewNodes. 
 */
public class ViewNodeDnDHandler
{
    private static ViewNodeDnDHandler defaultInstance=null; 

    public static ViewNodeDnDHandler getInstance()
    {
        if (defaultInstance==null)
        {
            defaultInstance=new ViewNodeDnDHandler() ;
        }
        return defaultInstance; 
    }

	public enum DropAction {COPY,MOVE,LINK, COPY_PASTE, CUT_PASTE}; 
	
	public ViewNodeDnDHandler() 
	{	
	}

    public boolean doDrop(ViewNode targetDropNode, DropAction dropAction, List<VRL> vris)
	{
		System.err.printf("DROP:%s:%s:",dropAction,new ExtendedList<VRL>(vris));
		return true; 
	}


}
