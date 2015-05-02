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

package nl.esciencecenter.ptk.crypt;

import org.junit.Assert;

public class Demo_CryptoHash
{
    public final static String SHA_256 = "SHA-256";

    public final static String SHA_1 = "SHA-1";

    public final static String MD5 = "MD5";

    // hash examples:
    public final static String MD5_HASH_12345 = "827CCB0EEA8A706C4C34A16891F84E7B";

    public final static String SHA256_HASH_12345 = "5994471ABB01112AFCC18159F6CC74B4F511B99806DA59B3CAF5A9C173CACFC5";

    public static void main(String args[])
    {
        try
        {
            new Demo_CryptoHash().test_Patient1_12345();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    // @Test
    public void test_Patient1_12345() throws Throwable
    {
        //seems to fail:
        testEncrypt("12345", "patient01", "xC5gLUJ1UxI=", CryptScheme.DESEDE_ECB_PKCS5, "SHA-256", StringCrypter.CHARSET_UTF8);
    }

    protected void testEncrypt(String password, String value, String expectedCrypt, CryptScheme encryptionScheme,
            String keyHashingScheme, String charsetUtf8) throws Exception
    {
        StringCrypter crypter = new StringCrypter(Secret.wrap(password.toCharArray()), encryptionScheme,
                keyHashingScheme, charsetUtf8);
        String encryptStr = crypter.encryptToBase64(value);

        outPrintf(">Encoding: Hash:%s, Crypt:%s (charset=%s)\n", keyHashingScheme, encryptionScheme, charsetUtf8);
        outPrintf(" - password = %s\n", password);
        outPrintf(" - encrypt  = %s -> %s \n", value, encryptStr);

        Assert.assertEquals("Encrypted String doesn match expected!", expectedCrypt, encryptStr);

        String decryptedValue = crypter.decryptString(encryptStr);
        Assert.assertEquals("Decrypted String doesn't match expected1", value, decryptedValue);
    }

    protected static void outPrintf(String format, Object... args)
    {
        System.out.printf(format, args);
    }

}
