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

import nl.esciencecenter.ptk.crypt.Secret;

/**
 * Secret Holder class.
 */
public class SecretHolder implements VARHolder<Secret> {

    public Secret value = null;

    /**
     * Wrap Holder around secret chars, source characters are cleared!
     */
    public SecretHolder(char[] secret) {
        this.value = new Secret(secret, true); // auto clear source!
    }

    public SecretHolder(Secret secret) {
        this.value = secret;
    }

    public SecretHolder() {
    }

    public String toString() {
        return "<SecretHolder>(?)";
    }

    public synchronized void dispose() {
        if (value != null)
            value.dispose();
        this.value = null;
    }

    public Secret get() {
        return this.value;
    }

    public char[] getChars() {
        if (this.value == null)
            return null;
        return this.value.getChars();
    }

    public void set(Secret val) {
        this.value = val;
    }

    public boolean isSet() {
        return (value != null);
    }

}