/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.emulator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Util {

    public static String byte2hexstr(int val) {
        if (val < 0) {
            return "<EOF>"; // -1= EOF;
        }

        String str = Integer.toHexString(val);
        if (str.length() < 2)
            return "0" + str; // pad with 0;
        else
            return str;
    }


    /**
     * Mini Byte buffer can used as byte stack.
     */
    public static class MiniBuffer {
        // Use direct memory buffers?: ByteBuffer bytes = ByteBuffer.allocateDirect(200);

        byte[] bytes;
        int index = 0;

        public MiniBuffer(int size) {
            bytes = new byte[size];
        }

        public void put(byte b) {
            bytes[index] = b;
            index++;
        }

        public byte pop() throws IOException {
            if (index <= 0)
                throw new IOException("Byte Byffer is empty: can not pop");

            index--;
            return bytes[index];
        }

        /**
         * eat current byte, if buffer is empty. Do nothing
         */
        public void eat() throws IOException {
            /*(void)*/
            pop();
        }

        public String toString(String encoding) throws UnsupportedEncodingException {
            return new String(bytes, 0, index, encoding);
        }

        // set index to 0;
        public void reset() {
            index = 0;
        }

        /**
         * Returns duplicate of byte buffer
         */
        public byte[] getBytes() {
            byte[] b2 = new byte[index];
            System.arraycopy(bytes, 0, b2, 0, index);
            return b2;
        }

        public int size() {
            return index;
        }

        public int capacity() {
            return bytes.length;
        }

        public int freeSpace() {
            return bytes.length - index;
        }

        /**
         * Auto casts integer to byte value. Uses lower 0x00ff value
         */
        public void put(int c) {
            put((byte) (c & 0x00ff));
        }


        public int current() throws IOException {
            if (index <= 0)
                throw new IOException("Byte Byffer is empty: no current in buffer");
            return bytes[index - 1];
        }

    }

}
