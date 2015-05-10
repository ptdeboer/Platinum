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

package nl.esciencecenter.ptk.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import nl.esciencecenter.ptk.util.Comparer;
import nl.esciencecenter.ptk.util.SortUtil;

/**
 * Helper class for Managed Lists like StringList. Implementation type is ArrayList.
 */
public class ExtendedList<T> extends ArrayList<T> implements Cloneable, Serializable {

    private static final long serialVersionUID = -7068991019200873766L;

    // ========================================================================
    //
    // ========================================================================

    protected boolean allowNull = true;

    /**
     * Construct list from Array. Creates new backing array, does not copy/clone the elements.
     */
    public ExtendedList(T[] values) {
        if (values == null) {
            return; // NIL list
        }
        add(values);
    }

    /**
     * Create empty list.
     */
    public ExtendedList() {
        super();// =default;
    }

    /**
     * Construct list around one value.
     * 
     * @param value
     */
    public ExtendedList(T value) {
        add(value);
    }

    /**
     * Creates a StringList with an capacity of num. Actual reported size() will be 0.
     * 
     * @param capacity
     *            - initial capacity of List
     */
    public ExtendedList(int capacity) {
        super(capacity);
    }

    /**
     * Clone list, values wil not be copied.
     */
    public ExtendedList(Collection<? extends T> list) {
        super(list);
    }

    /**
     * Concatenates Elements of this List to one String using '\n' as line separator. <br>
     * Calls toString(",");
     */
    public String toString() {
        return toString(",");
    }

    /**
     * Concatenates Elements to String array using elementSeperator between elements.
     */
    public String toString(String elementSeperator) {
        return toString(null, null, elementSeperator);
    }

    public String toString(String quote, String elementSeperator) {
        return toString(quote, quote, elementSeperator);
    }

    public String toString(String elementPrefix, String elementPostfix, String elementSeperator) {
        // two pass to alloc right size of target string
        int nrStrs = this.size();

        // allocate target size to speed up allocation.
        StringBuffer buf = new StringBuffer();

        // calculate target size
        for (int i = 0; i < nrStrs; i++) {
            if (elementPrefix != null)
                buf.append(elementPrefix);

            buf.append(this.get(i));

            if (elementPostfix != null)
                buf.append(elementPostfix);

            // add separator, but only BETWEEN strings
            if (i + 1 < nrStrs)
                buf.append(elementSeperator);
        }

        return buf.toString();
    }

    /**
     * Remove elements from this list. <br>
     * <strong>Polymorphism note</strong>: This method overrides remove(Object o) *only* for the
     * type "T", remove(Object o) is still possible because of Generics.
     */
    public void remove(T els[]) {
        if (els == null)
            return;

        for (T el : els)
            this.remove(el);
    }

    /**
     * Add all elements. Does not check for duplicates. NULL objects are omitted!
     */
    public void add(T[] els) {
        if (els == null)
            return;

        for (T el : els) {
            this.add(el);
        }
    }

    /**
     * Add element if not in the list already.
     */
    public void addUnique(T el) {
        add(el, true);
    }

    public void add(T[] strs, boolean unique) {
        if (strs == null) {
            return;
        }

        for (T str : strs) {
            this.add(str, unique);
        }
    }

    public void add(List<T> strs, boolean unique) {
        if (strs == null)
            return;

        for (T str : strs)
            this.add(str, unique);
    }

    /**
     * Add Value, if (unique==true) the entry won't be added if it already exists.
     */
    public boolean add(T str, boolean unique) {
        if (unique) {
            if (indexOf(str) < 0) {
                add(str);
                return true;
            }
            return false;
        } else {
            add(str);
            return true;
        }
    }

    /**
     * Add Elements if NOT already in this list. Returns number of elements really added.
     * <p>
     * This is an O(N*N) merge. For each element a linear search is used by calling contains().
     * 
     * @return number of unique elements added
     */
    public int merge(T[] els) {
        if (els == null)
            return 0; // nothing to merge

        int numAdded = 0;

        for (T el : els) {
            if (el != null) {
                if (this.contains(el) == false) {
                    this.add(el);
                    numAdded++;
                }
            }
        }

        return numAdded;
    }

    /**
     * Sort List according to newOrder array. Moves elements not found in the <code>newOrder</code>
     * array up to after the elements in specified newOrder array.<br>
     * No elements are removed if they are not found in the <code>newOrder<code>.
     */
    public void orden(T[] newOrder) {

        int p = 0;
        int dir = 0;
        boolean moveUp = true;

        if (moveUp == true) {
            p = 0; // begin
            dir = +1; // move up
        } else {
            p = this.size() - 1; // end of list
            dir = -1; // move down.
        }

        // O(M):: Sort element according template list
        for (int i = 0; i < newOrder.length; i++) {
            T el = newOrder[i];
            int index = indexOf(el);

            if (index >= 0) {
                // swap p <=> index;
                T org = get(p);
                set(p, get(index)); // move up
                set(index, org); // move org to old place:
                p += dir; // move to next position.
            }
        }
    }

    /**
     * Add elements from otherList if not already in this list. Returns number of elements really
     * added.
     */
    public int merge(ExtendedList<T> otherList) {

        if ((otherList == null) || (otherList.size() <= 0))
            return 0;

        int numAdded = 0;

        for (T el : otherList) {
            if (this.contains(el) == false) {
                this.add(el);
                numAdded++;
            }
        }

        return numAdded;
    }

    /**
     * Inplace sort. Sorts the elements in this list.
     * 
     * @param comparer
     *            Value comparer for type T
     * @return index of sorted entries see QSort.sort()
     */
    public int[] sort(Comparer<T> comparer) {
        return SortUtil.qsort(this, comparer);
    }

    /**
     * Returns sorted copy. Uses duplicate().sort()
     */
    public ExtendedList<T> createSorted(Comparer<T> comparer) {
        ExtendedList<T> list = this.clone();
        list.sort(comparer);
        return list;
    }

    /**
     * Sort this list and remove all double entries.
     */
    public void unique(Comparer<T> comparer) {

        int index = 0;

        while (index < this.size() - 1) {
            T first = this.get(index);
            T second = this.get(index + 1);

            if (first == second) {
                this.remove(second);
            } else {
                index++;
            }
        }
    }

    /**
     * Create duplicate by converting the elements to an explicit typed array first. Use this method
     * if the array contains mixed super- and sub- classes of type <T>.
     * 
     * @param nilArray
     *            - empty array (T[0]) to specify the type of the backing array to be used.
     * 
     * @return non shallow copy of this ElementList.
     */
    public ExtendedList<T> duplicate(T[] nilArray) {
        return new ExtendedList<T>(this.toArray(nilArray));
    }

    public ExtendedList<T> clone() {
        return new ExtendedList<T>(this);
    }

    /**
     * Insert new value at position. Note: For an ArrayList insert() and remove() methods are
     * expensive.
     */
    public void insert(int insertPosition, T value) {
        this.add(insertPosition, value);
    }

    /**
     * Insert new value at position. Note: For an ArrayList insert() and remove() methods are
     * expensive.
     */
    public void insert(int insertPosition, T[] values) {
        for (int i = 0; i < values.length; i++) {
            this.add(i, values[i]);
        }
    }

    /**
     * Insert newValue before 'beforeValue'. Note: For an ArrayList insert() and remove() methods
     * are expensive.
     */
    public int insertBefore(T beforeValue, T newValue) {
        int index = this.indexOf(beforeValue);
        if (index < 0)
            return -1;

        insert(index, newValue);

        return index;
    }

    /**
     * Insert newValue after 'afterValue'. Note: For an ArrayList insert() and remove() methods are
     * expensive.
     */
    public int insertAfter(String afterValue, T newValue) {
        int index = this.indexOf(afterValue);
        if (index < 0)
            return -1;

        insert(index + 1, newValue);

        return index;
    }

    public int compare(ExtendedList<T> otherList, Comparer<T> comparer) {
        int len = this.size();

        if (otherList.size() < len) {
            len = otherList.size();
        }

        // Compare elements and return String difference between mismatching
        // elements.
        // This is needed for the hypothetical case where a list of StringLists
        // is sorted.

        for (int i = 0; i < len; i++) {
            int val = comparer.compare(get(i), otherList.get(i));
            if (val != 0)
                return val;
        }

        // Longer lists > Shorter lists
        return this.size() - otherList.size();
    }

    /**
     * Adds all values of this list to a linkedHashSet. The LinkedHashSet keeps the order of this
     * list. Duplicate values are removed as the returned type is a Set
     * 
     * @returns LinkedHashSet containing the String List keeping the values in the same order.
     */
    public Set<T> toSet() {
        Set<T> set = new LinkedHashSet<T>();

        for (int i = 0; i < this.size(); i++) {
            set.add(this.get(i));
        }
        return set;
    }

    public ExtendedListIterator<T> iterator() {
        return new ExtendedListIteratorWrapper<T>(super.listIterator());
    }
}
