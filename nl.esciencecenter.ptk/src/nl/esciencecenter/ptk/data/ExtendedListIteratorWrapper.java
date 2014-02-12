package nl.esciencecenter.ptk.data;

import java.util.ListIterator;

/** 
 * Wrapper around default ListIterator to provide ExtendedListIterator functionality: 
 */
public class ExtendedListIteratorWrapper<T> implements ExtendedListIterator<T>
{
    protected ListIterator<T> listIterator; 
    
    public ExtendedListIteratorWrapper(ListIterator<T> iterator)
    {
        listIterator=iterator; 
    }

    @Override
    public boolean hasNext()
    {
        return listIterator.hasNext(); 
    }

    @Override
    public T next()
    {   
        return listIterator.next(); 
    }

    @Override
    public boolean hasPrevious()
    {

        return listIterator.hasNext();
    }

    @Override
    public T previous()
    {
        return listIterator.previous();
    }

    @Override
    public int nextIndex()
    {
        return listIterator.nextIndex(); 
    }

    @Override
    public int previousIndex()
    {
        return listIterator.previousIndex(); 
    }

    @Override
    public void remove()
    {
        listIterator.remove(); 
    }

    @Override
    public void set(T e)
    {
        listIterator.set(e); 
    }

    @Override
    public void add(T e)
    {
        listIterator.add(e); 
    }

    @Override
    public ExtendedList<T> next(int numElements)
    {
        ExtendedList<T> subList= new ExtendedList<T>(numElements); 
        
        for (int i=0;i<numElements;i++)
        {
            if (listIterator.hasNext())
            {
                subList.add(listIterator.next()); 
            }
            else
            {
                break;
            }
        }
        
        return subList; 
    }

    @Override
    public ExtendedList<T> previous(int numElements,boolean keepOriginalOrder)
    {
        ExtendedList<T> subList= new ExtendedList<T>(numElements); 
        
        for (int i=0;i<numElements;i++)
        {
            if (listIterator.hasPrevious())
            {
                // Costly Insert O(NxN) if implementation is ArrayList. 
                subList.insert(0, listIterator.previous()); 
            }
            else
            {
                break;
            }
        }
        
        return subList; 
    }

}
