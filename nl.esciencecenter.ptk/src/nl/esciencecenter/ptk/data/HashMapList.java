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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.ptk.util.logging.ClassLogger;

/**
 * Extended LinkedHashMap. Added more support for array and lists.
 * 
 * @author Piter T. de Boer.
 * 
 * @param <TK>
 *            - The Key Type
 * @param <TV>
 *            - The Value Type
 */
public class HashMapList<TK, TV> extends LinkedHashMap<TK, TV> implements Serializable
{
    private static final long serialVersionUID = -8373244037848706796L;

    private static ClassLogger logger = null;

    static
    {
        logger = ClassLogger.getLogger(HashMapList.class);
        logger.setLevelToDebug();
    }

    // =======================================================================
    //
    // =======================================================================

    public TV put(TK key, TV value)
    {
        TV prev = super.put(key, value);
        return prev;
    }

    /**
     * Adds All entries to current map
     */
    public void putAll(Map<? extends TK, ? extends TV> map)
    {
        super.putAll(map);
    }

    /**
     * Put selection from Map map into this Hashtable.
     */
    public void putAll(Map<? extends TK, ? extends TV> map, Iterable<TK> keys)
    {
        for (TK key : keys)
        {
            put(key, map.get(key));
        }
    }

    /**
     * Put selection from Map map into this Hashtable
     */
    public void putAll(Map<? extends TK, ? extends TV> map, TK keys[])
    {
        for (TK key : keys)
        {
            put(key, map.get(key));
        }
    }

    public TV elementAt(int i)
    {
        // todo: optimization for large sets:
        return this.get(this.getKey(i));
    }

    public TK getKey(int nr)
    {
        // todo: faster search
        Set<TK> set = this.keySet();

        int index = 0;
        if (size() <= 0)
            return null;

        for (TK key : set)
        {
            if (index == nr)
                return key;
            index++;
        }

        return null;
    }

    // ================
    // Arrays and Lists
    // ================

    public Iterator<TK> getKeyIterator()
    {
        return this.keySet().iterator();
    }

    public TK[] getKeyArray(TK[] arr)
    {
        return this.keySet().toArray(arr);
    }

    /**
     * Returns values as Array.
     * 
     * @param array
     *            - example array need for actual type.
     * @return
     */
    public TV[] toArray(TV[] array)
    {
        return this.values().toArray(array);
    }

}
