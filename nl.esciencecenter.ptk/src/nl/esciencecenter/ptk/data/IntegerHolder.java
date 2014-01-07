/*
 * Copyrighted 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").  
 * You may not use this file except in compliance with the License. 
 * For details, see the LICENCE.txt file location in the root directory of this 
 * distribution or obtain the Apache License at the following location: 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
 * ---
 */
// source: 

package nl.esciencecenter.ptk.data;

/**
 * Integer holder class for VAR Integer types.
 */
public class IntegerHolder implements VARHolder<Integer>
{
    public Integer value = null;

    public IntegerHolder(Integer val)
    {
        this.value = val;
    }

    public IntegerHolder()
    {
    }

    public int intValue()
    {
        if (value != null)
            return value;

        throw new NullPointerException("Value in IntegerHolder is NULL");

    }

    /** 
     * Returns Holder value or defValue if holder does not contain any value. 
     */
    public int intValue(int defValue)
    {
        if (value != null)
            return value;

        return defValue;
    }

    public boolean isSet()
    {
        return (value != null);
    }

    public Integer get()
    {
        return this.value;
    }

    public void set(Integer val)
    {
        this.value = val;
    }

}
