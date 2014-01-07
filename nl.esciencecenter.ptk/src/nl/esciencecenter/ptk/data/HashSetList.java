package nl.esciencecenter.ptk.data;

import java.util.LinkedHashSet;
import java.util.List;

/** 
 * Combined Set and List. 
 * Added support for List and Array methods. 
 */
public class HashSetList<TK> extends LinkedHashSet<TK>
{
    private static final long serialVersionUID = 6450134077211545785L;

    public List<TK> toList(List<TK> list)
    {
        for (TK key:this)
        {
            list.add(key);
        }
        return list; 
    }
    
}
