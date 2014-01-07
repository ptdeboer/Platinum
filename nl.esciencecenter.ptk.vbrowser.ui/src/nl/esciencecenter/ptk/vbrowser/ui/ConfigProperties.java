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

package nl.esciencecenter.ptk.vbrowser.ui;

import java.util.Properties;

import nl.esciencecenter.ptk.util.StringUtil;

public class ConfigProperties extends Properties
{
	private static final long serialVersionUID = -5793232356668006148L;
	
	// Optional Parent!
    final ConfigProperties parent; 

    public ConfigProperties(ConfigProperties parent)
    {
        this.parent=parent; 
    }
    
    public ConfigProperties()
    {
        this.parent=null; 
    }
    
	public String getProperty(String name)
	{
		String val=super.getProperty(name); 
		if ((val==null) && (parent!=null)) 
		{
			val=parent.getProperty(name); 
		}
		return val;
	}

	public String getStringProperty(String propName,String defaultValue) 
	{
		String val=getProperty(propName); 
		if (val==null)
			return defaultValue; 
		return val;
	}
	
	public int getIntProperty(String propName,int defaultValue) 
	{
		String val=getProperty(propName); 
		if (StringUtil.isWhiteSpace(val))
			return defaultValue; 
		return Integer.parseInt(val); 
	}

	public boolean getBoolProperty(String propName,boolean defaultValue) 
	{
		String val=getProperty(propName); 
		if (StringUtil.isWhiteSpace(val)==false)
			return defaultValue; 
		return Boolean.parseBoolean(val); 
	}

}
