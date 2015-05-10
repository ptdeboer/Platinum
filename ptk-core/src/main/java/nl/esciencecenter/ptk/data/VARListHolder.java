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

/**
 * Common VAR Holder interface for lists.
 */
public interface VARListHolder<T> {
    /**
     * Whether value is set.
     */
    public boolean isSet();

    /**
     * Set actual list value.
     */
    public void set(List<T> value);

    /**
     * Set value in list.
     */
    public void set(int index, T newValue);

    /**
     * Add value to list.
     */
    public void add(T newValue);

    /**
     * Get value.
     */
    public List<T> get();

    /**
     * Get value form list.
     */
    public T get(int index);

    /**
     * @return true if list is null or has 0 values.
     */
    public boolean isEmpty();

}
