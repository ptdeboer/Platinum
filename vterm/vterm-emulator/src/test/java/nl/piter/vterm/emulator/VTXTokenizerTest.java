/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.emulator;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static nl.piter.vterm.emulator.Tokens.Token.CHARSET_G0_DUTCH;
import static nl.piter.vterm.emulator.VTxTokenDefs.CTRL_ESC;
import static org.junit.Assert.fail;

@Slf4j
public class VTXTokenizerTest {

    @Test
    public void testCharSequence1() throws Exception {

        String source = "abcdefghijklmnopqrstuvwxyz";

        byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
        InputStream inps = new ByteArrayInputStream(bytes);
        VTxTokenizer tokenizer = new VTxTokenizer(inps);
        Tokens.Token token = null;

        int index = 0;

        do {
            token = tokenizer.nextToken();

            // Text representation parse bytes sequence
            byte[] tokenBytes = tokenizer.getBytes();

            int arg1 = 0;
            int arg2 = 0;

            int numIntegers = tokenizer.args().numArgs();

            if (numIntegers > 0)
                arg1 = tokenizer.args().ints(0);

            if (numIntegers > 1)
                arg2 = tokenizer.args().ints(1);

            if (token != Tokens.Token.EOF) {
                // ASCII
                Assert.assertEquals("Char #" + index + " mismatch", source.charAt(index), (char) tokenBytes[0]);
                index++;
            } else {
                Assert.assertEquals("Wrong number of Char Tokens", source.length(), index);
            }

        } while ((token != null) && (token != Tokens.Token.EOF));

    }

    @Test
    public void testCharset4() {
        byte[] source = new byte[]{CTRL_ESC, '(', '4'};
        testSequence(source, CHARSET_G0_DUTCH, new int[0]);
    }

    @Test
    public void testSeqUP0() {
        byte[] source = new byte[]{CTRL_ESC, '[', 'A'};
        testSequence(source, Tokens.Token.UP, new int[0]);
    }

    @Test
    public void testSeqUP1() {
        byte[] source = new byte[]{CTRL_ESC, '[', '1', ';', 'A'};
        testSequence(source, Tokens.Token.UP, new int[]{1});
    }

    @Test
    public void testDecModes() {

        byte[] source = new byte[]{CTRL_ESC, '[', '?', '0', 'h'};
        testSequence(source, Tokens.Token.DEC_SETMODE, new int[]{0});

        byte[] source2 = new byte[]{CTRL_ESC, '[', '?', '0', 'l'};
        testSequence(source2, Tokens.Token.DEC_RESETMODE, new int[]{0});

        testSequence(new byte[]{CTRL_ESC, '[', '?', '1', 'h'}, Tokens.Token.DEC_SETMODE, new int[]{1});
        testSequence(new byte[]{CTRL_ESC, '[', '?', '1', 'l'}, Tokens.Token.DEC_RESETMODE, new int[]{1});
        testSequence(new byte[]{CTRL_ESC, '[', '?', '1', '2', 'h'}, Tokens.Token.DEC_SETMODE, new int[]{12});
        testSequence(new byte[]{CTRL_ESC, '[', '?', '1', '3', 'l'}, Tokens.Token.DEC_RESETMODE, new int[]{13});
        testSequence(new byte[]{CTRL_ESC, '[', '?', '1', '4', '5', 'h'}, Tokens.Token.DEC_SETMODE, new int[]{145});
        testSequence(new byte[]{CTRL_ESC, '[', '?', '9', '9', '9', 'l'}, Tokens.Token.DEC_RESETMODE, new int[]{999});

    }

    @Test
    public void testDecModesNill() {

        byte[] source = new byte[]{CTRL_ESC, '[', '?', 'h'};
        testSequence(source, Tokens.Token.DEC_SETMODE, new int[0]);

        byte[] source2 = new byte[]{CTRL_ESC, '[', '?', 'l'};
        testSequence(source2, Tokens.Token.DEC_RESETMODE, new int[0]);
    }

    @Test
    public void testXGRAPHMODE_setTitle() {

        // \[]0;XXXX;\007
        byte[] source = new byte[]{CTRL_ESC, ']', '0', ';', 'X', 'X', 'X', 'X', 007};
        testSequence(source, Tokens.Token.XGRAPHMODE, 0, "XXXX");

    }

    protected void testSequence(byte[] bytes, Tokens.Token expected, int[] expectedInts) {
        InputStream inps = new ByteArrayInputStream(bytes);
        VTxTokenizer tokenizer = new VTxTokenizer(inps);

        try {
            Tokens.Token token = tokenizer.nextToken();
            Assert.assertEquals(expected, token);
            Assert.assertEquals(expectedInts.length, tokenizer.args().numArgs());
            for (int i = 0; i < expectedInts.length; i++) {
                Assert.assertEquals("Integer at index:#" + i + " mismatches", expectedInts[i], tokenizer.args().ints(i));
                log.debug(" integer at #" + i + " matches:" + tokenizer.args().ints(i));
            }

        } catch (IOException e) {
            e.printStackTrace();
            fail("Caught Exception:" + e.getClass().getCanonicalName() + ":" + e.getMessage());
        }

    }


    protected void testSequence(byte[] bytes, Tokens.Token expected, int graphmodeInt, String graphmodeStr) {
        InputStream inps = new ByteArrayInputStream(bytes);
        VTxTokenizer tokenizer = new VTxTokenizer(inps);

        try {
            Tokens.Token token = tokenizer.nextToken();
            Assert.assertEquals(expected, token);
            Assert.assertEquals(graphmodeInt, tokenizer.args().ints(0));
            Assert.assertEquals(graphmodeStr, tokenizer.args().strArg());

        } catch (IOException e) {
            e.printStackTrace();
            fail("Caught Exception:" + e.getClass().getCanonicalName() + ":" + e.getMessage());
        }

    }


}
