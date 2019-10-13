/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.util;

public class StringUtil {

    public static boolean isEmpty(String str) {
        return ((str == null) || (str.equals("")));
    }

    public static <b> String toHexStr(byte[] bytes) {
        return toHexStr(bytes, "", -1, "");
    }

    public static <b> String toHexStr(byte[] bytes, String identStr, int maxColums, String eolStr) {
        if (bytes == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int columns = 0;
        sb.append(identStr);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
            columns++;
            if (columns % maxColums == 0) {
                sb.append(eolStr);
                sb.append(identStr);
            }
        }
        return sb.toString();
    }


}
