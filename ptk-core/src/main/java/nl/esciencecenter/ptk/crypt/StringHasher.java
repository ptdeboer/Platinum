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

package nl.esciencecenter.ptk.crypt;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import nl.esciencecenter.ptk.object.Duplicatable;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;

/**
 * Stateful String Hasher Util class.<br>
 * <strong>Salting</strong>: If a salt is specified the byte value of the salt string is added as extra digest after
 * processing the source text.
 */
public class StringHasher implements Cloneable, Duplicatable<StringHasher>
{
    public final static String SHA_256 = "SHA-256";

    public final static String SHA_1 = "SHA-1";

    public final static String MD5 = "MD5";

    // ========================================================================
    //
    // ========================================================================

    // public static enum HashType

    private static ClassLogger logger;

    static
    {
        logger = ClassLogger.getLogger(StringHasher.class);
    }

    // ========================================================================
    // Helper methods.
    // ========================================================================
    /**
     * Truncate the hash bytes (digest) and exor the remaining bytes with the beginning.
     */
    public static byte[] truncate(byte digest[], int maxLen)
    {
        byte subDigest[] = new byte[maxLen];
        for (int i = 0; i < maxLen; i++)
            subDigest[i] = 0; // clear
        // truncate hash and exor the remainder:
        for (int i = 0; i < digest.length; i++)
            subDigest[i % maxLen] ^= digest[i];

        logger.debugPrintf("Truncating digest: %s -> %s\n", StringUtil.toHexString(digest),
                StringUtil.toHexString(subDigest));

        return subDigest;
    }

    // ========================================================================
    //
    // ========================================================================

    private MessageDigest messageDigest;

    private Charset charSet = null;

    protected StringHasher()
    {

    }

    public StringHasher(String hashType) throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        init(hashType, "UTF8");
    }

    public StringHasher(String hashType, String charSet) throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        init(hashType, charSet);
    }

    @Override
    public boolean shallowSupported()
    {
        return false;
    }

    @Override
    public StringHasher duplicate()
    {
        return clone();
    }

    @Override
    public StringHasher duplicate(boolean shallow)
    {
        return clone();
    }

    public StringHasher clone()
    {
        StringHasher hasher = new StringHasher();
        hasher.copyFrom(this);
        return hasher;
    }

    protected void init(String hashType, String encoding) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        setEncoding(encoding);
        setHashType(hashType);
    }

    protected void copyFrom(StringHasher other)
    {
        this.charSet = other.charSet;

        if (other.messageDigest != null)
        {
            // exception free clone:
            try
            {
                this.messageDigest = MessageDigest.getInstance(other.messageDigest.getAlgorithm());
            }
            catch (NoSuchAlgorithmException e)
            {
                logger.logException(ClassLogger.FATAL, e,
                        "Impossible Exception:Already existing MessageDigest Instance returns NoSuchAlgorithmException"
                                + "of its own algorithm:" + other.messageDigest.getAlgorithm());
            }
        }
    }

    public void setHashType(String hashType) throws NoSuchAlgorithmException
    {
        messageDigest = MessageDigest.getInstance(hashType);

        if ((StringUtil.equalsIgnoreCase(hashType, MD5)) || (StringUtil.equalsIgnoreCase(hashType, SHA_1)))
            logger.warnPrintf("Don't use outdated MD5 od SHA-1\n");
    }

    public void setEncoding(String charSetStr) throws UnsupportedEncodingException
    {
        charSet = Charset.forName(charSetStr);

        if (charSet == null)
            throw new UnsupportedEncodingException("No such Character Encoding:" + charSetStr);
    }

    public Charset getEncoding()
    {
        return this.charSet;
    }

    public void reset()
    {
        messageDigest.reset();
    }

    public String createFileChecksum(String checksumType, String filename) throws IOException
    {
        reset();

        FileInputStream fis = new FileInputStream(filename);

        byte[] dataBytes = new byte[1024];

        int nread = 0;
        while ((nread = fis.read(dataBytes)) != -1)
        {
            messageDigest.update(dataBytes, 0, nread);
        }

        // ignore;
        try
        {
            fis.close();
        }
        catch (Exception e)
        {
            ;
        }

        byte[] mdbytes = messageDigest.digest();

        return StringUtil.toHexString(mdbytes);
    }

    /**
     * Create hash from provided text. The hash length (in bytes) will be limited to maxhashLen. The remainder of the
     * hash will be exored with the actual truncated hash.
     * 
     * @param text
     *            - String to hash
     * @param saltText
     *            - Optional salt text.
     * 
     * @param maxHashLen
     *            - maximum hash length in bytes or -1.
     * @return hash - in hexidecimal String format, without "0x" prefixed.
     */
    public String createHashToHexString(String text, String salt, int maxHashLen)
    {
        byte digest[] = _hash(text.getBytes(charSet), true, salt.getBytes(charSet), false);

        if (maxHashLen > 0)
        {
            digest = truncate(digest, maxHashLen);
        }

        return StringUtil.toHexString(digest);
    }

    /**
     * Create hash from provided text. The hash length (in bytes) will be limited to maxhashLen. The remainder of the
     * hash will be exored with the actual truncated hash.
     * 
     * @param text
     *            - String to hash
     * @param maxHashLen
     *            - maximum hash length in bytes or -1.
     * @return hash - base64 encoded hash value.
     */
    public String createHashToBase64String(String text, String saltText, int maxHashLen)
    {
        byte digest[] = hash(text, true, saltText, false);

        if (maxHashLen > 0)
        {
            digest = truncate(digest, maxHashLen);
        }

        return StringUtil.base64Encode(digest);
    }

    public String createHashToHexString(String text)
    {
        byte digest[] = hash(text, true, null, false);
        return StringUtil.toHexString(digest, true);
    }

    /**
     * Create Hash from String and return hash value as Base64 encoded String. When using SHA-256 these 32 bytes will be
     * encode to 48 characters.
     */
    public String createHashToBase64String(String text, String saltText)
    {
        byte digest[] = hash(text, true, saltText, false);
        return StringUtil.base64Encode(digest);
    }

    /**
     * @param text
     *            - actual text to create hash from. Used default char set to get the string bytes.
     * @param reset
     *            - reset state of hasher before applied hash.
     * @param saltText
     *            - optional salt text to add.
     * @param prefixSalt
     *            - if true, saltBytes are applied before hashing the actual bytes if false, the source bytes are hashed
     *            first, then the saltBytes are digested.
     */
    public byte[] hash(String text, boolean reset, String saltText, boolean prefixSalt)
    {
        if (text == null)
            throw new NullPointerException("Source text can not be null!");

        byte saltBytes[] = null;
        if (saltText != null)
            saltBytes = saltText.getBytes(charSet);
        return _hash(text.getBytes(charSet), reset, saltBytes, prefixSalt);
    }

    /**
     * Hash sourceBytes[] and optionally use saltBytes[] either as prefix (prefixSalt==true) or postFix
     * (prefixSalt=false) and return he resulting bytes. This changed the state of the hasher.
     * 
     * @param sourceBytes
     *            - actual bytes to create hash from.
     * @param reset
     *            - reset state of hasher before applied hash.
     * @param saltBytes
     *            - optional salt bytes to add.
     * @param prefixSalt
     *            - if true, saltBytes are applied before hashing the actual bytes if false, the source bytes are hashed
     *            first, then the saltBytes are digested.
     */
    public byte[] hash(byte sourceBytes[], boolean reset, byte saltBytes[], boolean prefixSalt)
    {
        return _hash(sourceBytes, reset, saltBytes, prefixSalt);
    }

    /**
     * Hash implementation.
     */
    protected byte[] _hash(byte sourceBytes[], boolean reset, byte saltBytes[], boolean prefixSalt)
    {
        if (reset)
            reset();

        if (sourceBytes == null)
            throw new NullPointerException("Source bytes can't be NULL!");

        if ((saltBytes != null) && (prefixSalt == true))
            messageDigest.update(saltBytes);

        messageDigest.update(sourceBytes);

        if ((saltBytes != null) && (prefixSalt == false))
            messageDigest.update(saltBytes);

        byte byteData[] = messageDigest.digest();

        return byteData;
    }

}
