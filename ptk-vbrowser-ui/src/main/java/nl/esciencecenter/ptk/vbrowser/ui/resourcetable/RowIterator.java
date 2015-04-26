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

package nl.esciencecenter.ptk.vbrowser.ui.resourcetable;

import java.util.Iterator;
import java.util.NoSuchElementException;

import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTableModel.RowData;

/** 
 * RowIterator, for safe row manipulations 
 */ 
public class RowIterator implements Iterator<RowData>
{
    int rowIndex=-1;
    
    private ResourceTableModel resourceModel=null;
    
    public RowIterator(ResourceTableModel model)
    {
        this.resourceModel=model;  
    }

    /** 
     * @return current row. Returns null when iterator has past the end of the list.  
     */
    public RowData current()
    {
        // rowIndex points to next entry. 
        if (rowIndex<-1)
        {
            return null;
        }
        return resourceModel.getRow(rowIndex+1); 
    }
    
    @Override
    public boolean hasNext()
    {
        return (resourceModel.getRow(rowIndex+1)!=null);  
    }

    @Override
    public RowData next()
    {
        rowIndex++; // pre increment. 
        RowData row = resourceModel.getRow(rowIndex+1);
        if (row==null)
        {
            throw new NoSuchElementException("Couldn't get row:"+rowIndex);
        }
        return row; 
    }
    
    /** 
     * Like next, but returns Row Key.  
     */ 
    public String nextKey()
    {
        RowData row = next(); 
        if (row==null)
        {
            throw new NoSuchElementException("Couldn't get row:"+rowIndex);
        }
        return row.getKey(); 
    }

    @Override
    public void remove()
    {   
        if (rowIndex<0) 
        {
            throw new NoSuchElementException("No more elements left or next() wasn't called first!");
        }
        // Removes CURRENT element, reduces rowIndex;
        // this is the element returned by a previous 'next()' call.  
        resourceModel.delRow(rowIndex); 
        rowIndex--; // backpaddle!
    }

}
