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

import java.util.ListIterator;

/**
 * Wrapper around default ListIterator to provide ExtendedListIterator functionality:
 */
public class ExtendedListIteratorWrapper<T> implements ExtendedListIterator<T> {

    protected ListIterator<T> listIterator;

    public ExtendedListIteratorWrapper(ListIterator<T> iterator) {
        listIterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return listIterator.hasNext();
    }

    @Override
    public T next() {
        return listIterator.next();
    }

    @Override
    public boolean hasPrevious() {

        return listIterator.hasNext();
    }

    @Override
    public T previous() {
        return listIterator.previous();
    }

    @Override
    public int nextIndex() {
        return listIterator.nextIndex();
    }

    @Override
    public int previousIndex() {
        return listIterator.previousIndex();
    }

    @Override
    public void remove() {
        listIterator.remove();
    }

    @Override
    public void set(T e) {
        listIterator.set(e);
    }

    @Override
    public void add(T e) {
        listIterator.add(e);
    }

    @Override
    public ExtendedList<T> next(int numElements) {

        ExtendedList<T> subList = new ExtendedList<T>(numElements);

        for (int i = 0; i < numElements; i++) {
            if (listIterator.hasNext()) {
                subList.add(listIterator.next());
            } else {
                break;
            }
        }

        return subList;
    }

    @Override
    public ExtendedList<T> previous(int numElements, boolean keepOriginalOrder) {

        ExtendedList<T> subList = new ExtendedList<T>(numElements);

        for (int i = 0; i < numElements; i++) {
            if (listIterator.hasPrevious()) {
                // Costly Insert O(NxN) if implementation is ArrayList.
                subList.insert(0, listIterator.previous());
            } else {
                break;
            }
        }

        return subList;
    }

}
