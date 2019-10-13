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

import java.util.List;

/**
 * Reference implementation of QuickSort for Lists and Arrays. Updated to use Generics.
 */
public class QSort<Type> {

    // ===========================================================
    // Instance
    // ===========================================================

    protected Comparer<Type> comp;

    /**
     * Create a QSort object. One way to use this would with dynamic class creation:
     *
     * <PRE>
     * <p>
     * QSort sorter = new QSort(new Comparer()
     * {
     * public int compare(Object a,Object b)
     * {
     * if (a.key == b.key)
     * return 0;
     * else  if (a.key &lt; b.key)
     * return -1
     * else
     * return 1;
     * }
     * });
     * <p>
     * sorter.sort(array);
     *
     * </PRE>
     */
    public QSort(Comparer<Type> comparer) {
        comp = comparer;
    }

    /**
     * Sorts the array, according to the Comparer. The returned vector (I[]) provides mapping
     * information about the new order. The sorted list Y[] equals to the original list X[] as
     * follows: Y[I[i]] = X[i] Or: I[index-in-Y]=index-in-X
     **/
    public int[] sort(Type[] list) {
        return quicksort(newIndex(list.length), list, 0, list.length - 1);
    }

    /**
     * Sorts the array, according to the Comparer.
     */
    public int[] sort(List<Type> list) {
        return quicksort(newIndex(list.size()), list, 0, list.size() - 1);
    }

    /**
     * Sorts a subsequence of the array, according to the Comparer.
     */
    public int[] sort(int[] mapping, Type[] list, int start, int end) {
        return quicksort(mapping, list, start, end - 1);
    }

    /**
     * Sorts a subsequence of the array, according to the Comparer.
     */
    public int[] sort(int[] mapping, List<Type> list, int start, int end) {
        return quicksort(mapping, list, start, end - 1);
    }

    private int[] newIndex(int len) {
        int[] index = new int[len];

        for (int i = 0; i < len; i++)
            index[i] = i;
        return index;
    }

    private int[] quicksort(int[] mapping, Type[] list, int p, int r) {
        //
        if (p < r) {
            int q = partition(mapping, list, p, r);
            if (q == r) {
                q--;
            }
            quicksort(mapping, list, p, q);
            quicksort(mapping, list, q + 1, r);
        }
        //
        return mapping;
    }

    private int[] quicksort(int[] mapping, List<Type> list, int p, int r) {
        //
        if (p < r) {
            int q = partition(mapping, list, p, r);
            if (q == r) {
                q--;
            }
            quicksort(mapping, list, p, q);
            quicksort(mapping, list, q + 1, r);
        }
        //
        return mapping;
    }

    private int partition(int[] mapping, Type[] list, int p, int r) {
        //
        Type pivot = list[p];
        int lo = p;
        int hi = r;
        //
        while (true) {

            while (comp.compare(list[hi], pivot) >= 0 && lo < hi) {
                hi--;
            }
            while (comp.compare(list[lo], pivot) < 0 && lo < hi) {
                lo++;
            }
            if (lo < hi) {
                Type T = list[lo];
                list[lo] = list[hi];
                list[hi] = T;

                int i = mapping[lo];
                mapping[lo] = mapping[hi];
                mapping[hi] = i;
            } else
                return hi;
        }
    }

    private int partition(int[] mapping, List<Type> list, int p, int r) {
        //
        Type pivot = list.get(p);
        int lo = p;
        int hi = r;
        //
        while (true) {

            while (comp.compare(list.get(hi), pivot) >= 0 && lo < hi) {
                hi--;
            }
            while (comp.compare(list.get(lo), pivot) < 0 && lo < hi) {
                lo++;
            }
            if (lo < hi) {

                Type loVal = list.get(lo);
                Type hiVal = list.get(hi);
                // note: type <? extends Object> doesn't accept Object
                list.set(lo, hiVal);
                list.set(hi, loVal);

                int i = mapping[lo];
                mapping[lo] = mapping[hi];
                mapping[hi] = i;
            } else
                return hi;
        }
    }

}