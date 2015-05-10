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
 * String holder class for VAR String types.
 */
public class StringHolder implements VARHolder<String> {

    public String value = null;

    public StringHolder() {
        this.value = null;
    }

    public StringHolder(String str) {
        this.value = str;
    }

    public String toString() {
        return value;
    }

    public synchronized void dispose() {
        this.value = null;
    }

    public String get() {
        return this.value;
    }

    public void set(String val) {
        this.value = val;
    }

    public boolean isSet() {
        return (value != null);
    }

}