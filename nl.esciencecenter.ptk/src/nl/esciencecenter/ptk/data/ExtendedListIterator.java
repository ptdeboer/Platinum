package nl.esciencecenter.ptk.data;

import java.util.List;
import java.util.ListIterator;

/**
 * Extended ListIterator, supports fetching a subset of a List in one <code>next()</code> or <code>previous()</code> call. 
 * 
 * @author Piter T. de Boer
 */
public interface ExtendedListIterator<Type> extends ListIterator<Type>
{
    /**
     * Returns next numElements in this list. <br>
     * The result is the same as if numElements time <code>next()</code> is
     * called and the result in returned in a separate (Extended)List.<br>
     * 
     * This method does NOT throw NoSuchElementException. If there are not enough elements left of the end has been reached, 
     * this method will return null or an empty list or the actual number of elements left. 
     * 
     * @see ListIterator#next()
     */

    List<Type> next(int numElements);
    
    /**
     * Returns previous numElements in this list. <br>
     * If the list does not contains numElements before the current position,
     * this method will return the amount that is left.<br>
     * This method does NOT throw NoSuchElementException. If there are no
     * elements to return, this method will return null or an empty list.
     * 
     * @param numElements
     *            - number previous elements to return in reversed order, unless keepOriginalOrder==true. 
     * @param keepOriginalOrder
     *            - this method perform <code?numElements times a previous() and
     *            return the sublist. This means the order, by default will be
     *            reversed. Set keepOrifinalOrder the number of elements in the
     *            sub list are the same as in the original List.
     */
    List<Type> previous(int numElements, boolean keepOriginalOrder);

}
