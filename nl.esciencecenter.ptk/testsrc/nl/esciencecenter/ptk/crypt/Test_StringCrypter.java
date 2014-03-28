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

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import junit.framework.Assert;
import nl.esciencecenter.ptk.crypt.CryptScheme;
import nl.esciencecenter.ptk.crypt.Secret;
import nl.esciencecenter.ptk.crypt.StringCrypter;
import nl.esciencecenter.ptk.testsettings.Settings;
import nl.esciencecenter.ptk.util.StringUtil;

import org.junit.Test;

public class Test_StringCrypter
{
    public final static String SHA_256 = "SHA-256";

    public final static String SHA_1 = "SHA-1";

    public final static String MD5 = "MD5";

    // hash examples:
    public final static String MD5_HASH_12345 = "827CCB0EEA8A706C4C34A16891F84E7B";

    public final static String SHA256_HASH_12345 = "5994471ABB01112AFCC18159F6CC74B4F511B99806DA59B3CAF5A9C173CACFC5";


    public static Secret getLegacyAppKey1()
    {
        // 'legacy app key 1'
        return new Secret("123CSM34567890ENCRYPTION".toCharArray());
        // return new Secret("123CSM34567890ENCRYPTIONC3PR4KEY5678901234567890".toCharArray());

    }
    
    @Test
    public void test_HashMD5_12345() throws Exception
    {
        // check keys used in junit tests:
        testHash("MD5", "12345", MD5_HASH_12345);
    }

    @Test
    public void test_HashSHA256_12345() throws Exception
    {
        // check keys uses in junit tests:
        testHash("SHA-256", "12345", SHA256_HASH_12345);
    }

    // Test raw key
    @Test
    public void test_CryptDESedeECBPKCS5_BinaryKey12345() throws Exception
    {
        String sourceText = "12345";
        String passphraseSourceTxt = "12345";

        // echo -n 12345 | openssl enc -des-ede3 -nosalt -pass pass:12345
        // -base64 -md md5 -p
        // key=827CCB0EEA8A706C4C34A16891F84E7BC9D297BFBE75522A
        // C0u0GdKyYyE=
        // first part is 16 byte MD5 hash of "12345" but where does openssl get
        // the other 8 from ?
        String rawKeyString = "827CCB0EEA8A706C4C34A16891F84E7B" + "C9D297BFBE75522A";
        String expectedCrypt = "C0u0GdKyYyE=";

        byte rawKey[] = StringUtil.parseBytesFromHexString(rawKeyString);
        byte IV[] = null;

        Charset charSet = Charset.forName("UTF-8");

        // No IV needed for ECB:
        IvParameterSpec ivSpec = null;
        if (IV != null)
            ivSpec = new IvParameterSpec(IV);

        // SecretKeyFactory keyFactory =
        // SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        // cipher = Cipher.getInstance(encryptionScheme);

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        DESedeKeySpec keySpec = new DESedeKeySpec(rawKey);
        keySpec.getKey();

        SecretKey key = keyFactory.generateSecret(keySpec);
        // Default DESede actually uses DESede+ECB+PKCS5Padding
        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] cleartext = sourceText.getBytes(charSet);
        byte[] ciphertext = cipher.doFinal(cleartext);

        String base64 = StringUtil.base64Encode(ciphertext);
        outPrintf("testRawKey: rawkey =%s\n", StringUtil.toHexString(rawKey));
        outPrintf("testRawKey: cipher =%s\n", StringUtil.toHexString(ciphertext));
        outPrintf("testRawKey: base64 =%s\n", base64);

        Assert.assertEquals("Use of Rawkey (no salt nor IV) doesn't result in expected crypt", expectedCrypt, base64);
    }

    @Test
    public void test_CryptDESedeECBPKCS5_SHA256_12345() throws Throwable
    {
        // echo -n 12345 | openssl enc -des-ede3 -nosalt -pass pass:12345 -base64 -md sha256 -p
        // key=5994471ABB01112AFCC18159F6CC74B4F511B99806DA59B3
        // xC5gLUJ1UxI=
        testEncrypt("12345", "12345", "xC5gLUJ1UxI=", CryptScheme.DESEDE_ECB_PKCS5, "SHA-256",
                StringCrypter.CHARSET_UTF8);

        // echo -n 0123456789012345678901234 | openssl enc -des-ede3 -nosalt -pass pass:12345 -base64 -md sha256 -p
        // key=5994471ABB01112AFCC18159F6CC74B4F511B99806DA59B3
        // 3FpToewkL3fDIeGFWHCj9olKRKuErWn33oCk2oQdQdQ=
        testEncrypt("12345", "0123456789012345678901234", "3FpToewkL3fDIeGFWHCj9olKRKuErWn33oCk2oQdQdQ=",
                CryptScheme.DESEDE_ECB_PKCS5, "SHA-256", StringCrypter.CHARSET_UTF8);
    }

    @Test
    public void test_CryptAES128ECB_SHA256_12345() throws Throwable
    { 
        // echo -n 12345 | openssl enc -aes-128-ecb -nosalt -pass pass:12345 -base64 -md sha256 -p
        // key=5994471ABB01112AFCC18159F6CC74B4
        // sMLZyY92elQmQpxtEzCSmg==

        testEncrypt("12345", "12345", "sMLZyY92elQmQpxtEzCSmg==", CryptScheme.AES128_ECB_PKCS5, "SHA-256", StringCrypter.CHARSET_UTF8);
        
        // echo -n 0123456789012345678901234 | openssl enc -aes-128-ecb -nosalt -pass pass:12345 -base64 -md sha256 -p
        // key=5994471ABB01112AFCC18159F6CC74B4
        // 2dWhGz7KaPo3WO7u5+rrWFNvcxxBcHY8TQ/OF12YSmg=
        
        testEncrypt("12345", "0123456789012345678901234", "2dWhGz7KaPo3WO7u5+rrWFNvcxxBcHY8TQ/OF12YSmg=", CryptScheme.AES128_ECB_PKCS5, "SHA-256", StringCrypter.CHARSET_UTF8);
    }
    
    @Test
    public void test_CryptAES256ECB_SHA256_12345() throws Throwable
    { 
        if (Settings.getInstance().testAES256Encryption()==false)
        {
            Settings.getLogger(Test_StringCrypter.class).warnPrintf("Not testing 256 bits AES encryption\n"); 
            return; 
        }
        
        try
        {
            // echo -n 12345 | openssl enc -aes-256-ecb -nosalt -pass pass:12345 -base64 -md sha256 -ps
            // key=5994471ABB01112AFCC18159F6CC74B4F511B99806DA59B3CAF5A9C173CACFC5
            // yy7m98PdS/LxdH6XI32Z6g==
            testEncrypt("12345", "12345", "yy7m98PdS/LxdH6XI32Z6g==", CryptScheme.AES256_ECB_PKCS5, "SHA-256", StringCrypter.CHARSET_UTF8);
        }
        catch (InvalidKeyException e)
        {
            throw new Exception("Got invalid key exception. Unlimited Key length Encryption might not be supported. ",e); 
        }
        // echo -n 0123456789012345678901234 | openssl enc -des-ede3 -nosalt -pass pass:12345 -base64 -md sha256 -p
        // key=5994471ABB01112AFCC18159F6CC74B4F511B99806DA59B3
        // 3FpToewkL3fDIeGFWHCj9olKRKuErWn33oCk2oQdQdQ=
        //testEncrypt("12345", "0123456789012345678901234", "3FpToewkL3fDIeGFWHCj9olKRKuErWn33oCk2oQdQdQ=",
        //        CryptScheme.DESEDE_ECB_PKCS5, "SHA-256", StringCrypter.CHARSET_UTF8);
    }
    
    @Test
    public void test_CryptLegacyAppKey1() throws Exception
    {
        // echo  -n changeme | openssl enc -des-ede3 -nosalt -K 31323343534D3334353637383930454E4352595054494F4E00 -base64 -p
        // echo -n 'zJPWCwDeSgG8j2uyHEABIQ==' | base64 -d | openssl  enc -des-ede3 -K 31323343534D3334353637383930454E4352595054494F4E -d
        
        // Legacy App1 doesn't use hash digest!
        StringCrypter crypter = new StringCrypter(getLegacyAppKey1(), CryptScheme.DESEDE_ECB_PKCS5, null,
                StringCrypter.CHARSET_UTF8);
        // not recommend, use (iterative) hashing instead.
        crypter.setUsePlainCharBytes(true);
              
        test_EncryptDecrypt(crypter, "changeme", "zJPWCwDeSgG8j2uyHEABIQ==");
        test_EncryptDecrypt(crypter, "SomeWords7777", "Gs2dtmYD3jWetekNGGcEPg==");
    }

    @Test
    public void test_CryptLegacyAppKey2() throws Exception
    {
        StringCrypter crypter = new StringCrypter(getLegacyAppKey1(), CryptScheme.DESEDE_ECB_PKCS5, null,
                StringCrypter.CHARSET_UTF8);
        crypter.setUsePlainCharBytes(true);
        outPrintf("decrypt=%s\n", crypter.decryptString("zJPWCwDeSgG8j2uyHEABIQ=="));
    }

    @Test
    public void test_CryptDESedeECBPKCS5_SHA256_idValues() throws Throwable
    {
        // echo -n patientIdValue | openssl enc -des-ede3 -nosalt -pass
        // pass:12345 -base64 -md sha256 -p
        // key=5994471ABB01112AFCC18159F6CC74B4F511B99806DA59B3
        // xC5gLUJ1UxI=
        testEncrypt("12345", "patientIdValue", "XP6jSIrgIQJfCdRLTgelpw==", CryptScheme.DESEDE_ECB_PKCS5, "SHA-256",
                StringCrypter.CHARSET_UTF8);
        // echo -n patientIdValue | openssl enc -des-ede3 -nosalt -pass
        // pass:12345 -base64 -md sha256 -p
        // key=5994471ABB01112AFCC18159F6CC74B4F511B99806DA59B3
        // xC5gLUJ1UxI=
        testEncrypt("12345", "patientNameValue", "Ibr09TaiR4rbwFa3LgpncBmtruIG+dPX", CryptScheme.DESEDE_ECB_PKCS5,
                "SHA-256", StringCrypter.CHARSET_UTF8);
    }

    // ==============
    // Helper methods
    // ==============

    protected void testHash(String hashScheme, String text, String expectedHash) throws Exception
    {
        MessageDigest messageDigest = MessageDigest.getInstance(hashScheme);
        String password = text;

        messageDigest.update(password.getBytes("UTF-8"));
        // adding a salt is nothing more then digest the salt after the source
        // text.

        byte byteHash[] = messageDigest.digest();
        String hash = StringUtil.toHexString(byteHash);
        outPrintf("%s(%s)=%s\n", hashScheme, text, hash);

        Assert.assertEquals("Expected " + hashScheme + " doesn't match\n", expectedHash, hash);
        // C9D297BFBE75522A
        // messageDigest.update(new byte[]{0});
        // byteHash = messageDigest.digest();
        // printf("MD5(2)=%s\n",StringUtil.toHexString(byteHash));
    }

    // Default java PBE is not recommended:
    // @Test
    // public void testPBE() throws Exception
    // {
    // String password="12345";
    // String sourceText="12345";
    // char passChars[]=password.toCharArray();
    //
    // PBEKeySpec pbeKeySpec;
    //
    // SecretKeyFactory keyFac;
    //
    // // Salt
    // // byte[] salt = {
    // // (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
    // // (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
    // // };
    //
    // // Iteration count
    // int count = 20;
    //
    // // Create PBE parameter set
    // //PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
    //
    // pbeKeySpec = new PBEKeySpec(passChars);
    //
    //
    // // String keyEncoding="PBEwithMD5AndDESede";
    // // String encoding="PBEWithMD5AndTripleDES";
    // // String cipherEncoding="DESede/ECB/PKCS5Padding";
    // // String cipherEncoding="PBEwithMD5AndDESede";
    //
    // String keyEncoding="PBEWithMD5AndTripleDES";
    // String cipherEncoding=keyEncoding;
    //
    // keyFac = SecretKeyFactory.getInstance(keyEncoding);
    // SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
    //
    // // Create PBE Cipher
    // Cipher pbeCipher = Cipher.getInstance(cipherEncoding);
    //
    // // Initialize PBE Cipher with key and parameters
    // // pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
    // pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey);
    //
    // byte[] cleartext = sourceText.getBytes();
    // // Encrypt the cleartext
    // byte[] ciphertext = pbeCipher.doFinal(cleartext);
    //
    // printf("PBE) key    =%s\n",StringUtil.toHexString(pbeKey.getEncoded()));
    // printf("PBE) cipher =%s\n",StringUtil.toHexString(ciphertext));
    // printf("PBE) base64 =%s\n",StringUtil.base64Encode(ciphertext));
    // }

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


    protected static void test_EncryptDecrypt(StringCrypter crypter, String text, String expectedCryptBase64) throws Exception
    {
        String crypt = crypter.encryptToBase64(text);
        outPrintf("uuencrypt='%s' => '%s'\n", text, crypt);

        Assert.assertEquals("Encrypted String doesn't match actual!", expectedCryptBase64, crypt);

        String decrypt = crypter.decryptString(expectedCryptBase64);
        Assert.assertEquals("Decrypted String doesn't match actual!", text, decrypt);
    }
    
    protected static void outPrintf(String format, Object... args)
    {
        System.out.printf(format, args);
    }

}
