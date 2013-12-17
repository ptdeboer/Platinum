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

package nl.esciencecenter.ptk.vbrowser.ui.resourcetable;

import nl.esciencecenter.ptk.util.Comparer;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTableModel.RowData;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;

public class TableRowComparer implements Comparer<RowData>
{
    public boolean ignoreCase=true;
    private String columnName;
    private int order=1;
    
    private void init(String columnName,boolean reverse)
    {
        this.columnName=columnName; 
        this.order=(reverse==false)?(1):(-1); 
    }

    public TableRowComparer(String columnName,boolean reverse)
    {
        init(columnName,reverse); 
    }
    
    public TableRowComparer(String columnName)
    {
        init(columnName,false); 
    }    
    
    public int compare(RowData v1, RowData v2)
    {
    	if (v1==null) 
    		 debug("Received NULL object v1"); 

    	if (v2==null) 
    		debug("Received NULL object v2"); 

        if (v1==null) 
            if (v2==null)
                return 0;
            else
                return order*-1; 
        else
            if (v2==null)
                return order*1;
            else
                ; // continue 
        
        RowData r1=(RowData)v1; 
        RowData r2=(RowData)v2;
        
        Attribute o1 = r1.getAttribute(columnName);
        Attribute o2 = r2.getAttribute(columnName);
        
        debug("comparing:"+o1+" <==> "+o2); 
        
        if (o1==null) 
            if (o2==null)
                return 0;
            else
                return order*-1; 
        else
            if (o2==null)
                return order*1;
            else
                ; // continue 
        
        int result;  
        
        if ((o1 instanceof Attribute) && (o2 instanceof Attribute))
        {
        	if (ignoreCase)
        		result=order*((Attribute)o1).compareToIgnoreCase((Attribute)o2);
        	else
        		result=order*((Attribute)o1).compareTo((Attribute)o2);
        }
        else
        {
        	 if (ignoreCase)
        		 result= order*o1.toString().compareToIgnoreCase(o2.toString());
        	 else
        		 result= order*o1.toString().compareTo(o2.toString());
        }
        
        return result; 
    }

	private void debug(String msg) 
	{
	    //sSystem.err.printf("TableRowComparator:%s\n",msg); 
	}
    
}