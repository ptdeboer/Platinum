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
public class LongHolder implements VARHolder<Long> {

    public Long value = null;

    public LongHolder(Long val) {
        this.value = val;
    }

    public LongHolder() {
        value = new Long(0);
    }

    /**
     * @return autoboxed primitive long value of Long Object.
     * @throws NullPointerException if Long Object is not defined.
     */
    public long longValue() {
        if (value == null)
            throw new NullPointerException("Value in IntegerHolder is NULL");

        return value;
    }

    /**
     * Returns value or defeaultValue if holder does not contain any value.
     */
    public long longValue(long defeaultValue) {
        if (value == null)
            return defeaultValue;

        return value;
    }

    public boolean isNull() {
        return (value == null);
    }

    public Long get() {
        return this.value;
    }

    public void set(Long val) {
        this.value = val;
    }

    public boolean isSet() {
        return (value != null);
    }
}
