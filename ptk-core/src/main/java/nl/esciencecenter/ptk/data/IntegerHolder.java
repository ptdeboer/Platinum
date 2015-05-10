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

/**
 * Integer holder class for VAR Integer types.
 */
public class IntegerHolder implements VARHolder<Integer> {

    public Integer value = null;

    public IntegerHolder(Integer val) {
        this.value = val;
    }

    public IntegerHolder() {
    }

    /**
     * @return autoboxed primitive int value of Integer Object.
     * @throws NullPointerException
     *             if Integer Object is not defined.
     */
    public int intValue() {
        if (value != null)
            return value;

        throw new NullPointerException("Value in IntegerHolder is NULL");
    }

    /**
     * Returns Holder value or defValue if holder does not contain any value.
     */
    public int intValue(int defValue) {
        if (value != null)
            return value;

        return defValue;
    }

    public boolean isSet() {
        return (value != null);
    }

    public Integer get() {
        return this.value;
    }

    public void set(Integer val) {
        this.value = val;
    }

}
