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

import java.util.Vector;

/**
 * Generic History class. Manages a Stack like History of elements to remember for example last
 * visited URLs in a browser.
 * <p>
 * Unlike a stack a pop() doesn't remove an entry. Instead the History has a back() method which
 * returns the latest added entry, but does not remove it. A forward() moves the History index one
 * further and keeps the original at the 'Stack'.
 * <p>
 * An add() functions the same way as a push() but if the current index into the history does not
 * point to the last entry, the remainder of the History Stack is removed.
 */
public class History<T> {

    protected Vector<T> elements;

    protected boolean noDoubles = true;

    /**
     * Index points to latest added entry.
     */
    protected int currentIndex = -1;

    public History() {
        elements = new Vector<T>();
    }

    public History(int size) {
        elements = new Vector<T>(size);
    }

    /**
     * Add element to history. If the element equals current element the element is not added. Also
     * if the index is not pointin to the last element, the history is truncated.
     * 
     * @param el
     *            - element to be added to the history.
     */
    public void add(T el) {
        // insert at current index.
        // currentIndex points to next 'empty' space.
        synchronized (elements) {
            if ((noDoubles) && (isLast())) {
                // if last element equals current, don't add
                if (currentIndex > 0) {
                    if (this.elements.get(currentIndex).equals(el)) {
                        // Do not add double (last) entry:
                        return;
                    }
                }
            }

            currentIndex++;

            elements.setSize(currentIndex);// truncate first

            elements.add(currentIndex, el); // current point to latest added
        }
    }

    public boolean isLast() {
        return (currentIndex == size() - 1);
    }

    /** Go back in history and return current element but keep current stack order. */
    public T back() {
        synchronized (elements) {
            // int len=elements.size();

            if (currentIndex <= 0)
                return null;

            currentIndex--; // go back

            T el = elements.get(currentIndex);

            return el;
        }
    }

    /**
     * Go forward in current stack and return 'next' element. Returns null if already most recent
     * element.
     */
    public T forward() {
        synchronized (elements) {
            int len = elements.size();

            if (currentIndex + 1 >= len)
                return null;

            currentIndex++; // go forward;
            T el = elements.get(currentIndex);

            return el;
        }
    }

    public int size() {
        return elements.size();
    }

}
