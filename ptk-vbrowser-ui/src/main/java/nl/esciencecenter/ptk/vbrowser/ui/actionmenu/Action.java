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

package nl.esciencecenter.ptk.vbrowser.ui.actionmenu;

import java.awt.event.ActionEvent;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;

public class Action 
{
    /** 
     * Argument separator character used in argument lists. 
     */
    public static final String ARGUMENT_SEPERATOR_CHAR=";";
    
    public static final String COMMAND_SEPERATOR_CHAR=":"; 
    
	// ===
	// default actions, no arguments.
	// ===
	
//    public final static Action DELETE=new Action("Delete",ActionMethod.DO_DELETE);
//	
//	public final static Action REFRESH=new Action("Refresh",ActionMethod.DO_REFRESH);
//	
//	public final static Action PROPERTIES=new Action("Properties",ActionMethod.DO_PROPERTIES);
//
//	public final static Action SELECTION_ACTION=new Action("SelectionAction",ActionMethod.DO_SELECTION_ACTION);
//
//	public final static Action DEFAULT_ACTION=new Action("DefaultAction",ActionMethod.DO_DEFAULT_ACTION);

	// === instance === // 
	private ActionMethod actionMethod; 

	private StringList arguments=null;

    private Object source; 
    
	public Action(Object eventSource,ActionMethod actionMethod)
	{
	    this.source=eventSource; 
	    this.actionMethod=actionMethod; 
	}

	public Action(Object eventSource,ActionMethod actionMethod,String argument)
    {
	    this.source=eventSource; 
        this.actionMethod=actionMethod; 
        this.arguments=new StringList(); 
        arguments.add(argument); 
    }

	public Action(Object eventSource,ActionMethod actionMethod,String arguments[])
	{
	    this.source=eventSource; 
	    this.actionMethod=actionMethod; 
	    this.arguments=new StringList(arguments); 
    }

	public ActionMethod getActionMethod()
	{
		return this.actionMethod;
	}
	
	public String getActionMethodString()
	{
		return this.actionMethod.getMethodName(); 
	}

	public String toString()
	{
		String str=actionMethod.toString();
		//optional arguments; 
		if ((this.arguments!=null) && (this.arguments.size()>0))
				str=str+COMMAND_SEPERATOR_CHAR+this.arguments.toString(ARGUMENT_SEPERATOR_CHAR); 
		return str;
	}
	
	public static Action createFrom(ActionEvent event) 
	{
	    String cmdStr=event.getActionCommand(); 
		String strs[]=cmdStr.split(COMMAND_SEPERATOR_CHAR); 
		
		String methodStr=null;
		String argsStr=null;
		
		if (strs.length>0)
			methodStr=strs[0];
		
		if (strs.length>1)
			argsStr=strs[1]; 
		
		Action action=new Action(event.getSource(),ActionMethod.createFrom(methodStr)); 
		action.parseArgs(argsStr); 

		return action; 
	}

	protected void parseArgs(String argsStr) 
	{	
		if ((argsStr==null) || argsStr.equals(""))
			return; 
		
		this.arguments=StringList.createFrom(argsStr,ARGUMENT_SEPERATOR_CHAR); 
	}

	public StringList getArgs()
	{
		return this.arguments; 
	}
	
	public String getArg0()
	{
	    if ((arguments==null) || (arguments.size()<1)) 
	        return null; 
	    
	    return this.arguments.get(0);  
    }
	
	public String getArg1()
	{
	    if ((arguments==null) || (arguments.size()<2)) 
	        return null; 
	        
	    return this.arguments.get(1);  
    }

    public Object getEventSource()
    {
        return source; 
    }

    // === 
    // Factory method 
    // ===
    
    public static Action createSelectionAction(ViewNode node)
    {
        String arg=null;
        if (node!=null)
        {
            arg=node.getVRL().toString();
        }
        Action action=new Action(null,ActionMethod.SELECTION_ACTION,arg);
        return action; 
    }
    
    public static Action createDefaultAction(ViewNode node)
    {
        String arg=null;
        if (node!=null)
        {
            arg=node.getVRL().toString();
        }
        Action action=new Action(null,ActionMethod.DEFAULT_ACTION,arg); 
        return action; 
    }

    public static Action createGlobalAction(ActionMethod meth)
    {
        Action action=new Action(null,meth);  
        return action; 
    }
    
}
