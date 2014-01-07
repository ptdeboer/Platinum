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

package nl.esciencecenter.ptk.object;

/** 
 * This interface is an alternative to clone() which supports Generics.
 * Also it can be checked whether the duplicate method returns a shallow or a deep copy.  
 * By default the Duplicate method returns a full (deep) Copy. This is different then clone().  
 * This is useful for 'value' objects where each created copy must be a full (stand alone) copy.
 */
public interface Duplicatable<Type> 
{
	/** 
	 * Return whether shallow copies are supported. 
	 * If shallow copies are supported, the duplicate(true) method
	 * will always return a shallow copy. 
	 * By default duplicate() should return a full (non-shallow) copy.
	 */  
	public boolean shallowSupported(); 
	
	/** 
	 * Return copy (clone) of object.
	 */ 
	public Type duplicate(); 
	
	/**
	 * Returns copy of object. 
	 * Specify whether shallow copy is allowed. 
	 * If shallow==true, the duplicate method might still return 
	 * a non shallow copy or throw an exception if shallowSupported()==false. 
	 */ 
	public Type duplicate(boolean shallow);
	
}
