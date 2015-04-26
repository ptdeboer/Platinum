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

package nl.esciencecenter.ptk.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Collection of sort and other list manipulation methods 
 */
public class SortUtil
{

    public static class StringComparer implements Comparer<String>
    {
        public boolean ignoreCase;
        public boolean reverseOrder;

        public StringComparer()
        {
            this.ignoreCase=false; 
            this.reverseOrder=false; 
        }
        
        public StringComparer(boolean ignoreCase)
        {
            this.ignoreCase=ignoreCase; 
            this.reverseOrder=false; 
        }

        public StringComparer(boolean ignoreCase,boolean reverseOrder)
        {
            this.ignoreCase=ignoreCase; 
            this.reverseOrder=reverseOrder; 
        }

        public int compare(String o1, String o2)
        {
            if (o1 == null)
            {
                if (o2 == null)
                {
                    return 0;
                }
                else
                {
                    return -1;
                }
            }
            else if (o2 == null)
            {
                return 1;
            }
            else
            {
                ; // continue
            }
            
            String str1 = o1.toString();
            String str2 = o2.toString();

            int result; 
            
            if (ignoreCase)
            {
                result = str1.compareToIgnoreCase(str2);
            }
            else
            {
                result = str1.compareTo(str2);
            }
            
            if (reverseOrder)
            {
                result=-result;
            }
            
            return result; 
        }
    }

    public static class IntegerComparer implements Comparer<Integer>
    {
        @Override
        public int compare(Integer i1, Integer i2)
        {
            if (i1==null)
                if (i2==null)
                    return 0;
                else
                    return -1; // null > not null 
            
            return ((Integer)i1).compareTo((Integer)i2); 
        }
    }
    
    /** 
     * Sort by name. 
     */
    public static int[] qsort(List<String> list, boolean ignoreCase)
    {
        if (list == null)
            return null;

        StringComparer comparer = new StringComparer();
        comparer.ignoreCase = ignoreCase;
        QSort<String> qsort = new QSort<String>(comparer);
        return qsort.sort(list);
    }
    
    /** 
     * In place sorting 
     */ 
    public static int[] qsort(List<Integer> list) 
    {
        if (list == null)
            return null;

        IntegerComparer comparer = new IntegerComparer();
        QSort<Integer> qsort = new QSort<Integer>(comparer);
        
        return qsort.sort(list);
    }
    
    /** 
     * Returns set of unique integers from an Integer List. 
     * Set alreadySorted==true for already sorted lists. 
     * Implementation type of Set<> is LinkedHashSet. 
     */
    public static Set<Integer> unique(List<Integer> list,boolean alreadySorted)
    {
        if (list==null)
            return null;
        
        if (list.size()==0)
            return new LinkedHashSet<Integer>(0); 
            
        if (alreadySorted==false)
        {
            qsort(list); 
        }
        
        Set<Integer> uniqueSet=new LinkedHashSet<Integer>();
        // first
        int k=list.get(0);
        uniqueSet.add(k); 
        
        for (int i=1;i<list.size();i++)
        {
            if (list.get(i)>k)
            {
                k=list.get(i);
                uniqueSet.add(k); 
            }
        }
        
        return uniqueSet;
    }       
        
    /** 
     * In place sorting 
     */ 
    public static <T> int[] qsort(List<T> list,Comparer<T> comparer) 
    {
        if (list == null)
            return null;
        
        QSort<T> qsort = new QSort<T>(comparer);
        
        return qsort.sort(list);
    }
    
}
