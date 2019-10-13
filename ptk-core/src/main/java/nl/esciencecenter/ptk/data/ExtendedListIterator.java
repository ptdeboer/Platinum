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

import java.util.List;
import java.util.ListIterator;

/**
 * Extended ListIterator, supports fetching a subset of a List in one <code>next()</code> or
 * <code>previous()</code> call.
 */
public interface ExtendedListIterator<Type> extends ListIterator<Type> {

    /**
     * Returns next <code>numElements</code> in this list as a sub list. <br>
     * The result is the same as if numElements time <code>next()</code> is called and the result in
     * returned in a separate (Extended)List.<br>
     * This method does NOT throw NoSuchElementException. If there are not enough elements left or
     * the end has been reached, this method will return null or an empty list or the actual number
     * of elements left.
     *
     * @see ListIterator#next()
     */
    List<Type> next(int numElements);

    /**
     * Returns previous numElements in this list. <br>
     * If the list does not contains numElements before the current position, this method will
     * return the amount that is left.<br>
     * This method does NOT throw NoSuchElementException. If there are no elements to return, this
     * method will return null or an empty list.
     *
     * @param numElements       - number previous elements to return in reversed order, unless
     *                          keepOriginalOrder==true.
     * @param keepOriginalOrder - this method perform <code>numElements</code> times a previous() and return the
     *                          sublist. This means the order, by default will be reversed. Set keepOrifinalOrder
     *                          the number of elements in the sub list are the same as in the original List.
     */
    List<Type> previous(int numElements, boolean keepOriginalOrder);

}
