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

package nl.esciencecenter.ptk.io;

import java.io.File;
import java.net.UnknownHostException;

import nl.esciencecenter.ptk.GlobalProperties;

import org.junit.Assert;
import org.junit.Test;

public class Test_GlobalProperties 
{
	
	@Test
	public void testArchitectureInfo() throws UnknownHostException
	{
	    // Basically test whether methods work Exception free on tested platforms: 

		printf("> Global properties:\n");
		printf(" - user name =%s\n",assertNotEmpty(GlobalProperties.getGlobalUserName()));
		printf(" - user home =%s\n",assertNotEmpty(GlobalProperties.getGlobalUserHome()));
		printf(" - tmp dir   =%s\n",assertNotEmpty(GlobalProperties.getGlobalTempDir()));
		printf(" - hostname  =%s\n",assertNotEmpty(GlobalProperties.getHostname()));
		//
		printf("> OS arch/name/version = %s/%s/%s\n",GlobalProperties.getOsArch(),GlobalProperties.getOsName(),GlobalProperties.getOsVersion()); 
		printf(" - isWindows   = %s\n",GlobalProperties.isWindows());  
		printf(" - isLinux     = %s\n",GlobalProperties.isLinux());	
		printf(" - isMacOS     = %s\n",GlobalProperties.isMacOS());
		printf(" - isMacOSX    = %s\n",GlobalProperties.isMacOSX());
		printf(" - isMac       = %s\n",GlobalProperties.isMac());
	        
		//assertNotEmpty(""); 
	}

    @Test 
    public void testTempDir()
    {
        testGlobalDirProperty(GlobalProperties.getGlobalTempDir()); 
    }
    
    protected void testGlobalDirProperty(String dir)
    {
    	Assert.assertNotNull("getGlobalTempDir() must return a value",dir); 
    	
    	File file=new File(dir); 

    	Assert.assertTrue("System depended temp directory must exists:"+dir,file.exists()); 
    	Assert.assertTrue("System depended temp location must be a directory:"+dir,file.isDirectory()); 
    }
    
    @Test 
    public void testUserHome()
    {
        testGlobalDirProperty(GlobalProperties.getGlobalUserHome()); 
    }

    @Test 
    public void testUserDir()
    {
        testGlobalDirProperty(GlobalProperties.getGlobalUserDir()); 
    }

	private String assertNotEmpty(String string)
	{
		Assert.assertNotNull("String value may not be null",string); 
		Assert.assertFalse("String value may not be empty",string.equals("")); 
		return string;
	}

	
	private void printf(String format, Object... args) 
	{
		System.out.printf(format,args); 
	}
}
