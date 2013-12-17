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

import java.util.ArrayList;

import javax.swing.AbstractListModel;

import nl.esciencecenter.ptk.data.StringList;

public class HeaderModel extends AbstractListModel 
{
    private static final long serialVersionUID = -4513306632211174045L;
    
    private StringList values;

    private boolean isEditable=true; 

    public HeaderModel(StringList entries)
    {
        this.values=entries.duplicate(); 
    }
    
    public HeaderModel()
    {
        values=new StringList(); // empty list
    }
    
    public HeaderModel(String values[])
    {
        init(values);  
    }

    @Override
    public String getElementAt(int index)
    {
        return values.get(index); 
    }

    @Override
    public int getSize()
    {
        return values.size();
    }
    
    public void setValues(String vals[])
    {
        init(vals);
        this.fireContentsChanged(this,0,values.size()-1); 
    }
    
    private void init(String vals[])
    {
        this.values=new StringList(vals); 
    }
    
    public void setValues(StringList vals)
    {
        init(vals); 
        this.fireContentsChanged(this,0,values.size()-1);
    }
    
    private void init(StringList vals)
    {
        this.values=vals.duplicate();  
    }

    public ArrayList<String> toArray()
    {
        return this.values; 
    }

    public int indexOf(String name)
    {
        return this.values.indexOf(name); 
    }

    /**
     * Inserts newHeader after 'header' of before 'header'.
     * Fires intervalAdded event 
     */  
    public int insertHeader(String header, String newHeader,
            boolean insertBefore)
    {
        int index=-1; 
        
        if (insertBefore)
            index=this.values.insertBefore(header,newHeader); 
        else
            index=this.values.insertAfter(header,newHeader);
        
        this.fireIntervalAdded(this,index,index);
        
        return index; 
    }

    public int remove(String value)
    {
        int index=this.indexOf(value); 
        if (index<0)
            return -1;
        
        this.values.remove(index); 
        this.fireIntervalRemoved(this,index,index); 
        return index; 
    }

    public boolean isEditable()
    {
        return isEditable; 
    }

    public void setEditable(boolean val)
    {
        this.isEditable=val; 
    }

    public boolean contains(String name)
    {
        return values.contains(name); 
    }

    
}
