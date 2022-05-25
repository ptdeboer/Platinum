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

package nl.esciencecenter.ptk.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class Test_StringUtil {

    @Test
    public void testStringUtl_IsNonWhiteSpace() {
        Assert.assertTrue("isWhiteSpace: NULL String should return FALSE", StringUtil.isWhiteSpace(null));
        Assert.assertTrue("isWhiteSpace: Empty String should return FALSE", StringUtil.isWhiteSpace(""));
        Assert.assertTrue("isWhiteSpace: Empty String should return FALSE", StringUtil.isWhiteSpace(" "));
        Assert.assertTrue("isWhiteSpace: Single Tab String should return FALSE", StringUtil.isWhiteSpace("\t"));
        Assert.assertTrue("isWhiteSpace: Single NewLine String should return FALSE", StringUtil.isWhiteSpace("\n"));
        Assert.assertTrue("isWhiteSpace: Double Tab String should return FALSE", StringUtil.isWhiteSpace("\t\t"));
        Assert.assertTrue("isWhiteSpace: Double NewLine String should return FALSE", StringUtil.isWhiteSpace("\n\n"));
        Assert.assertTrue("isWhiteSpace: Double NewLine String should return FALSE", StringUtil.isWhiteSpace("\t \n \n "));
        Assert.assertFalse("isWhiteSpace: Single char should return FALSE", StringUtil.isWhiteSpace("a"));
        Assert.assertFalse("isWhiteSpace: Spaced Single char should return FALSE", StringUtil.isWhiteSpace(" a"));
        Assert.assertFalse("isWhiteSpace: Spaced Single char should return FALSE", StringUtil.isWhiteSpace("a "));

    }

    @Test
    public void testStringUtil_Compare() {
        doStringUtilCompare("aap", "noot");
        doStringUtilCompare("noot", "aap");
        doStringUtilCompare("aap", "aap");
        doStringUtilCompare("", "");
        doStringUtilCompare("", "aap");
        doStringUtilCompare("aap", "");
        doStringUtilCompare(null, "");
        doStringUtilCompare("", null);
        doStringUtilCompare(null, null);
        doStringUtilCompare(null, "aap");
        doStringUtilCompare("aap", null);
        //
        doStringUtilCompare("aap", "Aap");
        doStringUtilCompare("noot", "nooT");
        doStringUtilCompare("Aap", "aap");
        doStringUtilCompare("nooT", "noot");
    }

    public void doStringUtilCompare(String s1, String s2) {
        int v1;
        int vign1;

        // =================================
        // value defined by this Unit Test!
        // =================================

        if (s1 == null) {
            if (s2 == null) {
                v1 = 0;
                vign1 = 0;
            } else {
                v1 = -1;  // NULL < NOT NULL
                vign1 = -1;
            }
        } else {
            if (s2 == null) {
                v1 = 1;  // NOT NULL > NULL
                vign1 = 1;
            } else {
                // String Compare:
                v1 = s1.compareTo(s2);
                vign1 = s1.compareToIgnoreCase(s2);
            }
        }

        // ---
        // Assert that StringUtil.compare() is equivalent with String.compare() 
        // ---

        int v2 = StringUtil.compare(s1, s2);
        Assert.assertEquals("StringUtil.compare('" + s1 + "','" + s2 + "') must be equal to String.compare()", v1, v2);
        int vign2 = StringUtil.compare(s1, s2, true);
        Assert.assertEquals("StringUtil.compare('" + s1 + "','" + s2 + "') must be equal to String.compare()", vign1, vign2);

    }

    // CONVERTERS

    @Test
    public void testHexDecodeBasic() {
        String[] prefixes = new String[]{"", "0x", "0X"};

        // shorts: 
        testHexDecode("", new byte[]{});
        testHexDecode("01", new byte[]{1});
        testHexDecode("10", new byte[]{0x10});
        testHexDecode("ff", new byte[]{(byte) 0x00ff});
        testHexDecode("0f", new byte[]{(byte) 0x0f});
        testHexDecode("f0", new byte[]{(byte) 0xf0});

        for (String pref : prefixes) {

            testHexDecode(pref + "0", new byte[]{0});
            testHexDecode(pref + "00", new byte[]{0});
            testHexDecode(pref + "000", new byte[]{0, 0});
            testHexDecode(pref + "0000", new byte[]{0, 0});
            testHexDecode(pref + "1", new byte[]{1});
            testHexDecode(pref + "01", new byte[]{1});
            testHexDecode(pref + "001", new byte[]{0, 1});
            testHexDecode(pref + "0001", new byte[]{0, 1});
            testHexDecode(pref + "10", new byte[]{0x10});
            testHexDecode(pref + "010", new byte[]{0, 0x10});
            testHexDecode(pref + "0010", new byte[]{0, 0x10});
            testHexDecode(pref + "00010", new byte[]{0, 0, 0x10});

            testHexDecode(pref + "123456789abcdef0", new byte[]{0x12, 0x34, 0x56, 0x78, (byte) 0x9a, (byte) 0xbc, (byte) 0xde, (byte) 0Xf0});
        }
        // other methods will be tested in testHexEncode:
    }

    public void testHexDecode(String hexStr, byte[] bytes) {
        byte[] newbytes = StringUtil.parseBytesFromHexString(hexStr);
        Assert.assertEquals("Size of byte arrays from '" + hexStr + "' must match", bytes.length, newbytes.length);

        // Inject Fault here: newbytes[0]=0;

        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != newbytes[i]) {
                String newStr = StringUtil.toHexString(newbytes);
                Assert.assertEquals("Bytes of:'" + hexStr + "' => '" + newStr + "' has wrong byte at position:" + i, bytes[i], newbytes[i]);
            }
        }
    }


    @Test
    public void testHexStrings() throws UnsupportedEncodingException {
        byte[] bytes1 = new byte[1];

        int[] nums = new int[]{0, 1, 9, 0x0a, 'A', 0x099, 0x0ff, 0x0f9, 0x09f};
        String[] strs = new String[]{"00", "01", "09", "0A", "41", "99", "FF", "F9", "9F"};

        for (int i = 0; i < nums.length; i++) {
            bytes1[0] = (byte) (nums[i] & 0x00ff);
            testHexEncode(bytes1, strs[i], true);
        }

        // 4 bytes sequences: 
        nums = new int[]{0, 1, 9, 0x0a, 'A', 0x099, 0x0ff, 0x0f9, 0x09f};
        strs = new String[]{"00000000", "00000001", "00000009", "0000000A", "00000041", "00000099", "000000FF", "000000F9", "0000009F"};
        testHexEncodeBytes4(nums, strs);


        nums = new int[]{0x01010101, 0xffffffff, 0x1f1f1f1f, 0xa1a1a1a1, 0x1a1a1a1a};
        strs = new String[]{"01010101", "FFFFFFFF", "1F1F1F1F", "A1A1A1A1", "1A1A1A1A"};
        testHexEncodeBytes4(nums, strs);
    }

    public void testHexEncodeBytes4(int[] nums, String[] strs) throws UnsupportedEncodingException {
        byte[] bytes4 = new byte[4];

        for (int i = 0; i < nums.length; i++) {
            // big endian test data: 
            bytes4[3] = (byte) (nums[i] & 0x00ff);
            bytes4[2] = (byte) ((nums[i] >> 8) & 0x00ff);
            bytes4[1] = (byte) ((nums[i] >> 16) & 0x00ff);
            bytes4[0] = (byte) ((nums[i] >> 24) & 0x00ff);

            testHexEncode(bytes4, strs[i].toUpperCase(), true);
            testHexEncode(bytes4, strs[i].toLowerCase(), false);
        }

    }

    public void testHexEncode(byte[] bytes, String hexString, boolean upperCase) throws UnsupportedEncodingException {
        String bytesStr = StringUtil.toHexString(bytes, upperCase);
        Assert.assertEquals("Hexidecimal encoded string doesn match.", hexString, bytesStr);
        // parse back: 
        byte[] rebytes = StringUtil.parseBytesFromHexString(bytesStr);

        for (int i = 0; i < bytes.length; i++)
            Assert.assertEquals("bytes '" + hexString + "' differ at position:" + i, bytes[i], rebytes[i]);
    }


    @Test
    public void testBigIntegerStrings() {
        testBigIntegerString(null, null);
        testBigIntegerString("", new byte[]{});
        testBigIntegerString("0", new byte[]{0});
        testBigIntegerString("1", new byte[]{1});
        testBigIntegerString("9", new byte[]{9});
        testBigIntegerString("10", new byte[]{10});
        testBigIntegerString("99", new byte[]{99});
        testBigIntegerString("65535", new byte[]{(byte) 0xff, (byte) 0xff});
        testBigIntegerString("65536", new byte[]{1, 0, 0});
        testBigIntegerString("4294967295", new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff});
        testBigIntegerString("4294967296", new byte[]{1, 0, 0, 0, 0});
        testBigIntegerString("18446744073709551615", new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff});
        testBigIntegerString("18446744073709551616", new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0});
        testBigIntegerString("18446744073709551617", new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 1});
        testBigIntegerString("79228162514264337593543950336", new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        testBigIntegerString("79228162514264337593543950337", new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1});

        // todo: signed (negative) 
    }

    private void testBigIntegerString(String expected, byte[] bytes) {
        // big endian: 
        String beStr = StringUtil.toBigIntegerString(bytes, false, false);
        Assert.assertEquals("Big Endian BigInteger string doesn't match expected", expected, beStr);

        byte[] reverseEndian;
        // little endian:
        if (bytes == null) {
            reverseEndian = null;
        } else {
            int len = bytes.length;
            reverseEndian = new byte[len];
            for (int i = 0; i < len; i++)
                reverseEndian[len - i - 1] = bytes[i];
        }
        String leStr = StringUtil.toBigIntegerString(reverseEndian, false, true);
        Assert.assertEquals("Little Endian BigInteger string doesn't match expected", expected, leStr);
    }


}