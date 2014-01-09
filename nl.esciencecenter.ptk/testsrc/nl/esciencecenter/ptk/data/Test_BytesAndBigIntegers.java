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

import nl.esciencecenter.ptk.util.StringUtil;

import org.junit.Assert;
import org.junit.Test;

public class Test_BytesAndBigIntegers 
{
    
    @Test
    public void test16bytesBigIntegerMax()
    {
        // create 16 bytes little endian unsinged integer. 
        byte bytes[]=new byte[16]; 
        int n=16;
        
        for (int i=0;i<n;i++)
        {
            bytes[i]=(byte)0x00ff;  
        }

        // for a all 0xff byte sequence, endianity doesn't matter. 
        testBigIntegerString("340282366920938463463374607431768211455",bytes,false,false); 
        testBigIntegerString("340282366920938463463374607431768211455",bytes,false,true); 
    }
 
    @Test
    public void test16bytesBigIntegerNegativeMax()
    {
        // create 16 bytes little endian singed integer. 
        byte bytes[]=new byte[16]; 
        int n=16;
        
        for (int i=0;i<n;i++)
        {
            bytes[i]=(byte)0x00;  
        }

        bytes[0]=(byte)0x80; 
        
        // 0x800000...0  
        testBigIntegerString("-170141183460469231731687303715884105728",bytes,true,false);
    }
    
    @Test
    public void test24bytesBigInteger()
    {
        // create 16 bytes unsinged integer. 
        byte bytes[]=new byte[24]; 
        int n=24;
        
        for (int i=0;i<n;i++)
        {
            bytes[i]=(byte)0x00ff;  
        }
        
        testBigIntegerString("6277101735386680763835789423207666416102355444464034512895",bytes,false,false); 
        testBigIntegerString("6277101735386680763835789423207666416102355444464034512895",bytes,false,true); 
    }
    
    private void testBigIntegerString(String expected, byte[] bytes,boolean signed, boolean isLE)
    {
        // actual endianity 
        String bigStr=StringUtil.toBigIntegerString(bytes,signed, isLE); 
        Assert.assertEquals("Big Endian BigInteger string doesn't match expected",expected, bigStr); 
        
        byte reverse[];
        
        // little endian:
        if (bytes==null)
        {
            reverse=null;
        }
        else
        {
            reverse=reverseEndian(bytes); 
        }
        String revStr=StringUtil.toBigIntegerString(reverse, signed,(isLE==false)); 
        Assert.assertEquals("Little Endian BigInteger string doesn't match expected",expected, revStr); 
    }

    public static byte[] reverseEndian(byte[] bytes)
    {
        byte reverse[];
                
        int len=bytes.length; 
        reverse=new byte[len];
        for (int i=0;i<len;i++)
            reverse[len-i-1]=bytes[i];

        return reverse; 
    } 
    
    
}