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

package nl.esciencecenter.ptk.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import nl.esciencecenter.ptk.util.SortUtil;

/**
 * Helper class for StringLists. StringList extends the ArrayList class with
 * extra methods.
 * <p>
 * Note: ArrayList is an not synchronized generics class.
 */
public class StringList extends ExtendedList<String> implements Cloneable, Serializable
{
    private static final long serialVersionUID = -2559548865729284189L;

    /**
     * Factory method to merge two arrays. Duplicates are removed.
     */
    public static String[] merge(String[] arr1, String[] arr2)
    {
        StringList list = new StringList(arr1);
        list.merge(arr2);
        return list.toArray();
    }

    /**
     * Factory method to merge tree arrays. Duplicates are removed.
     */
    public static String[] merge(String[] arr1, String[] arr2, String arr3[])
    {
        StringList list = new StringList(arr1);
        list.merge(arr2);
        list.merge(arr3);
        return list.toArray();
    }

    /**
     *  Helper method to find a entry in a String Array 
     */
    public static int find(String[] list, String value)
    {
        if ((list == null) || (list.length == 0))
            return -1;

        for (int i = 0; i < list.length; i++)
        {
            if (value == null)
            {

                // null proof: allow ?
                if (list[i] == null)
                    return i;
            }
            else if (list[i].compareTo(value) == 0)
            {
                return i;
            }
        }

        return -1;
    }

    public static boolean hasEntry(String[] list, String val)
    {
        return (find(list, val) >= 0);
    }

    /**
     * Uses String.split(regexp) to create StringList
     * 
     * @see #java.lang.String.split();
     * @param str
     * @return
     */
    public static StringList createFrom(String str, String regexp)
    {
        if ((str == null) || (regexp == null))
            return null;

        String strs[] = str.split(regexp);
        return new StringList(strs);
    }

    /** 
     * Merge String array into one StringList. Uses merge().
     */
    public static StringList createFrom(String list1[], String list2[])
    {
        StringList list = new StringList(list1);
        list.merge(list2);
        return list;
    }

    /**
     *  Merge String arrays into one StringList. Uses merge()
     */
    public static StringList createFrom(String list1[], String list2[], String list3[])
    {
        StringList list = new StringList(list1);
        list.merge(list2);
        list.merge(list3);
        return list;
    }

    // ========================================================================
    //
    // ========================================================================

    public StringList(String... strs)
    {
        super(strs);
    }
    
    public StringList()
    {
        super();// =default;
    }

    /**
     * Creates a StringList with an capacity of num. Actual reported size() will
     * be 0! Only useful if the size is known in advance and the list size will
     * not change.
     * 
     * @param len
     */
    public StringList(int capacity)
    {
        super(capacity);
    }

    public StringList(Collection<? extends String> list)
    {
        super((list!=null)?list:new ArrayList<String>(0));
    }

    public int[] sort()
    {
        return SortUtil.qsort(this, false);
    }

    /**
     * Returns sorted copy. Uses duplicate().sort()
     */
    public StringList createSorted()
    {
        StringList list = this.duplicate();
        list.sort();
        return list;
    }

    public int[] sort(boolean ignoreCase)
    {
        return SortUtil.qsort(this, ignoreCase);
    }

    /**
     * Sort this list and remove all double entries.
     */
    public void unique(boolean isAlreadySorted)
    {
        if (isAlreadySorted == false)
        {
            sort();
        }

        int index = 0;

        while (index < this.size() - 1)
        {
            String first = this.get(index);
            String second = this.get(index + 1);

            if (first == second)
            {
                this.remove(second);
            }
            else
            {
                index++;
            }
        }
    }

    public StringList duplicate()
    {
        return new StringList(toArray());
    }

    public String[] toArray()
    {
        // downcast: allocate new String Array:
        String array[] = new String[size()];
        return this.toArray(array);// use super method.
    }

    public StringList clone()
    {
        return duplicate();
    }

    public boolean equals(Object object)
    {
        if (!(object instanceof StringList))
        {
            return false;
        }

        StringList other = (StringList) object;

        if (other.size() != this.size())
            return false;

        // compare elements
        return (compare((StringList) object) == 0);
    }

    public int compare(StringList otherList)
    {
        return compare(otherList, new SortUtil.StringComparer());
    }

}
