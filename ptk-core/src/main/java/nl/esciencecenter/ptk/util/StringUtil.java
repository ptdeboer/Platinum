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

package nl.esciencecenter.ptk.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.esciencecenter.ptk.data.StringList;

/**
 * StringUtil class contains a collection of standard String manipulation methods.
 */
public class StringUtil {

    public static final char[] HEX_CHAR_TABLE = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F' };

    public static byte[] HEX_CHAR_TABLE_reverse = new byte[256];

    public static final String NIL_STRING = new String(new byte[] {}); // empty

    static {
        initByteTables();
    }

    private static void initByteTables() {
        for (int i = 0; i < HEX_CHAR_TABLE.length; i++)
            HEX_CHAR_TABLE_reverse[HEX_CHAR_TABLE[i] & 0x00ff] = (byte) i;
    }

    /**
     * @return true when string is null or empty ("")
     */
    public static boolean isEmpty(String str) {
        if (str == null)
            return true;
        if (str.compareTo("") == 0)
            return true;
        return false;
    }

    public static boolean notEmpty(String str) {
        return (isEmpty(str) == false);
    }

    /**
     * Merge 2 String Arrays. null entries are skipped. duplicates are 'merged'.
     */
    public static String[] mergeLists(String[] list1, String[] list2) {
        return StringList.merge(list1, list2);
    }

    /**
     * Merge 3 String Arrays. null entries are skipped. duplicates are 'merged'.
     */
    public static String[] mergeLists(String[] list1, String[] list2, String[] list3) {
        return StringList.merge(list1, list2, list3);
    }

    /**
     * @return null save copy of String or null
     */
    public static String duplicate(String str) {
        if (str == null)
            return null;
        return new String(str);
    }

    /**
     * Compare objects based upon their String representation.
     */
    public static int compare(Object o1, Object o2) {
        String s1 = null;
        String s2 = null;
        if (o1 != null)
            s1 = o1.toString();
        if (o2 != null)
            s2 = o2.toString();
        return compare(s1, s2, false);
    }

    public static int compare(String s1, String s2) {
        return compare(s1, s2, false);
    }

    public static int compareIgnoreCase(String s1, String s2) {
        return compare(s1, s2, true);
    }

    /**
     * NULL pointer proof compare method. see: java.lang.String.compareTo()
     */
    public static int compare(String s1, String s2, boolean ignoreCase) {
        // if (s1) < (s2) return negative
        // if (s1) > (s2) return positive
        if (s1 == null) {
            if (s2 == null) {
                return 0;
            } else {
                // null < (not)null ?
                return -1;
            }
        }
        // (not) null > null ?
        if (s2 == null) {
            return 1;
        }
        // 
        if (ignoreCase) {
            return s1.compareToIgnoreCase(s2);
        } else {
            return s1.compareTo(s2);
        }
    }

    /**
     * Create InputStream from specified String to read from.
     * 
     * @throws UnsupportedEncodingException
     */
    public static ByteArrayInputStream createStringInputStream(String xmlString, String encoding)
            throws UnsupportedEncodingException {
        ByteArrayInputStream stream;
        stream = new ByteArrayInputStream(xmlString.getBytes(encoding));
        return stream;
    }

    /**
     * Whether first String 'str' matches one of the other Strings given as variable arguments.
     * 
     * @param str1
     *            - String to match.
     * @param strs
     *            - variable argument list of Strings to check.
     * @return
     */
    public static boolean equals(String str1, String... strs) {
        return equals(false, str1, strs);
    }

    public static boolean equalsIgnoreCase(String str1, String... strs) {
        return equals(true, str1, strs);
    }

    public static boolean equals(boolean ignoreCase, String str1, String... strs) {
        for (String str : strs)
            if (compare(str1, str, ignoreCase) == 0)
                return true;
        return false;
    }

    /**
     * Return number with zeros padded to the left, like "0013"
     */
    public static String toZeroPaddedNumber(int number, int size) {
        return paddString("" + number, size, '0', true);
    }

    /**
     * Padd string with extra chars (paddChar) upto maxLen. If prefix==true the padding string will
     * be added before the text.
     * 
     * @return
     */
    public static String paddString(String text, int maxLen, char paddChar, boolean prefix) {
        //
        int len = text.length();
        int n = maxLen - len;
        if (n < 0) {
            n = 0;
        }
        char chars[] = new char[n];
        for (int i = 0; i < n; i++)
            chars[i] = paddChar;
        String paddStr = new String(chars);
        if (prefix)
            return paddStr + text;
        else
            return text + paddStr;
    }

    /**
     * Null Pointer proof String.endsWith() method. First argument is complete string and second
     * argument is the substring which the first argument should end with. If either strings is
     * NULL, false is returned.
     * 
     * @param fullString
     * @param subString
     * @return true if no argument is NULL and fullString.endsWith(subString)==true
     */
    public static boolean endsWith(String fullString, String subString) {
        if (fullString == null)
            return false;
        if (subString == null)
            return false;
        return fullString.endsWith(subString);
    }

    /**
     * If this string consists of multiple lines each newline will be prepended by the identStr; If
     * the String is a non newline terminated String, only the String itself will be prepended.
     * 
     * @param orgStr
     * @param indentStr
     * @return
     */
    public static String insertIndentation(String orgStr, String indentStr) {
        //
        if (orgStr == null)
            return null;
        if (indentStr == null)
            return orgStr;
        //
        String lines[] = orgStr.split("\n");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            // do not prepend empty lines !
            if (isEmpty(lines[i]) == false) {
                builder.append(indentStr);
                builder.append(lines[i]);
            }
            // has more lines ? Add newline BETWEEN lines
            if (i < (lines.length - 1))
                builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * @return String str or "" if String str is null
     */
    public static String noNull(String str) {
        if (str == null)
            return "";
        return str;
    }

    /**
     * @return true if the String strVal represents the string value "true"
     */
    public static boolean isTrueString(String strVal) {
        if (strVal == null)
            return false;
        try {
            Boolean val = Boolean.parseBoolean(strVal);
            return val;
        } catch (Exception e) {
            return false;
        }
    }

    /** Returns true if the String strVal represents the string value "false" */
    public static boolean isFalseString(String strVal) {
        if (strVal == null)
            return false;
        try {
            Boolean val = Boolean.parseBoolean(strVal);
            return (val == false);
        } catch (Exception e) {
            return false;
        }
    }

    /** Convert bytes to (non "0x" prefixed) hexadecimal String */
    public static String toHexString(byte[] raw) {
        return toHexString(raw, true);
    }

    /** Convert bytes to (non "0x" prefixed) hexadecimal String */
    public static String toHexString(byte[] raw, boolean upperCase) {
        //
        if (raw == null)
            return null;
        int len = raw.length;
        char chars[] = new char[len * 2];
        //
        for (int i = 0; i < len; i++) {
            chars[i * 2] = HEX_CHAR_TABLE[(raw[i] >> 4) & 0x0f]; // upper byte
            chars[i * 2 + 1] = HEX_CHAR_TABLE[raw[i] & 0x0f]; // lower byte
        }
        String str = new String(chars);
        if (upperCase == false)
            str = str.toLowerCase();
        return str;
    }

    /** Null Pointer Safe toString() method */
    public static String toString(Object obj) {
        if (obj == null) {
            return "<NULL>";
        } else {
            return obj.toString();
        }
    }

    /**
     * Compares Object based on STRING representation
     */
    public static boolean equalsIgnoreCase(Object obj1, Object obj2) {
        if (obj1 == null) {
            if (obj2 == null) {
                return true;
            } else {
                return false;
            }
        } else {
            if (obj2 == null) {
                return false;
            } else {
                return equals(true, obj1.toString(), obj2.toString());
            }
        }
    }

    /**
     * Compares Object based on STRING representation
     */
    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == null) {
            if (obj2 == null) {
                return true;
            } else {
                return false;
            }
        } else {
            if (obj2 == null) {
                return false;
            } else {
                return equals(false, obj1.toString(), obj2.toString());
            }
        }
    }

    /**
     * Returns false if String contains any non white space characters.
     */
    public static boolean isWhiteSpace(String str) {
        // check: null/empty is whitespace ?
        if ((str == null) || (str.equals("")))
            return true;
        //
        for (int i = 0; i < str.length(); i++) {
            if (Character.isWhitespace(str.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static String boolString(boolean val) {
        return boolString(val, "true", "false");
    }

    /** Convenience method for functional statements */
    public static String boolString(boolean val, String trueValue, String falseValue) {
        return val ? trueValue : falseValue;
    }

    /** Remove spaces, newlines and tabs. Does not respect quoted values! */
    public static String stripWhiteSpace(String val) {
        if ((val == null) || (val.equals("")))
            return null;
        return val.replaceAll("[ \t\n]", "");
    }

    /**
     * Exception-less parse method. Returns defaultValue 'defVal' if string can't be parsed
     */
    public static boolean parseBoolean(String valstr, boolean defVal) {
        if (StringUtil.isWhiteSpace(valstr))
            return defVal;
        try {
            return Boolean.parseBoolean(valstr);
        } catch (Throwable t) {
            ;
        }
        return defVal;
    }

    public static boolean isWhiteSpace(Object val) {
        if (val == null)
            return true;// ?
        return isWhiteSpace(val.toString());
    }

    public static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parse a "0x" prefixed Hexidecimal integer or a string which contains only [0-9A-F] characters
     */
    public static int parseHexidecimal(String stringValue) {
        return Integer.decode(stringValue);
    }

    public static long parseLong(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean matchWildcard(String string, String wildCardStr, boolean matchCase, boolean completeMatch) {
        return matchRE(string, Wildcard2Regex.wildcardToRegex(wildCardStr), matchCase, completeMatch);
    }

    public static boolean matchRE(String source, String RE) {
        int flags = 0;
        Pattern pattern = Pattern.compile(RE, flags);
        Matcher m = pattern.matcher(source);
        return m.matches();
    }

    public static boolean matchRE(String source, String RE, boolean matchCase, boolean completeMatch) {
        int flags = 0;
        if (matchCase == false) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        Pattern pattern = Pattern.compile(RE, flags);
        Matcher m = pattern.matcher(source);
        boolean match = m.matches();
        if (match == false) {
            return false;
        }
        if (completeMatch) {
            return (m.end() >= source.length());
        }
        return true;
    }

    /**
     * Parse long, optional "0x" prefixed, hexidecimal String into bytes.
     */
    public static byte[] parseBytesFromHexString(String bytesAsHexString) {
        // todo: better/faster parsing?
        if (bytesAsHexString == null)
            return null;

        if (bytesAsHexString.equals("")) {
            byte bytes[] = new byte[0];
            return bytes;
        }

        bytesAsHexString = bytesAsHexString.toUpperCase();

        if (bytesAsHexString.startsWith("0X"))
            bytesAsHexString = bytesAsHexString.substring(2);

        // 2 character per one byte;
        int n = bytesAsHexString.length();

        // add extra '0' before uneven length string arrays.
        if ((n % 2) == 1) {
            bytesAsHexString = "0" + bytesAsHexString;
            n = n + 1;
        }

        int len = n / 2;

        byte bytes[] = new byte[len];

        for (int i = 0; i < len; i++) {
            char c1 = bytesAsHexString.charAt(i * 2);
            char c2 = bytesAsHexString.charAt(i * 2 + 1);
            bytes[i] = (byte) (HEX_CHAR_TABLE_reverse[c1] * 16 + +HEX_CHAR_TABLE_reverse[c2]);
        }

        return bytes;
    }

    /**
     * Convert bytes to big integer number String.
     */
    public static String toBigIntegerString(byte[] bytes, boolean signed, boolean littleEndian) {

        if (bytes == null)
            return null;

        if (bytes.length <= 0)
            return "";
        int len = bytes.length;

        if (littleEndian) {
            for (int i = 0; i < len / 2; i++) {
                byte b = bytes[i];
                bytes[i] = bytes[len - i - 1];
                bytes[len - i - 1] = b;
            }
        }

        if (signed == false) {
            // pad zero byte to big endian array:
            byte newbytes[] = new byte[len + 1];
            newbytes[0] = 0;
            for (int i = 0; i < len; i++)
                newbytes[i + 1] = bytes[i];
            bytes = newbytes;
        }

        return new BigInteger(bytes).toString(10); // base 10
    }

    public static String base64Encode(byte[] bytes) {
        return javax.xml.bind.DatatypeConverter.printBase64Binary(bytes);
    }

    public static byte[] base64Decode(String base64) throws IOException {
        return javax.xml.bind.DatatypeConverter.parseBase64Binary(base64);
    }

    public static boolean hasWhiteSpace(String text) {
        if ((text == null) || (text.equals("")))
            return false;

        boolean val = StringUtil.matchRE(text, "[^ \t\n]+", true, true);// use
                                                                        // expensive
                                                                        // RE
        return (val == false);
    }

    /**
     * Format integer value to Hexadecimal string with optional prefix and a number of digits >=
     * numberOfDigits. A zero is prefix for each digits missing.
     */
    public static String toHexString(String prefix, int value, boolean upperCase, int numberOfDigits) {
        StringBuilder sb = new StringBuilder();
        String hexStr = Integer.toHexString(value);
        if (prefix != null) {
            sb.append(prefix);
        }

        if (hexStr.length() < numberOfDigits) {
            for (int i = 0; i < (numberOfDigits - hexStr.length()); i++) {
                sb.append("0");
            }
        }
        sb.append(hexStr);
        return sb.toString();
    }

}