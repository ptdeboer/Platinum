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

package nl.esciencecenter.ptk.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/** 
 * Shared interface for ResourceLoader, FSUtil and (VBrowser) VRS Resource Loaders.
 * Provides methods to resolve relative paths to absolute URIs and openening them 
 * for reading and writing. 
 */
public interface ResourceProvider
{
    public java.net.URI resolvePathURI(String relpath) throws Exception; 

    public OutputStream createOutputStream(URI uri) throws Exception;

    public InputStream createInputStream(URI uri) throws Exception;

    public RandomReadable createRandomReader(URI uri) throws Exception;

    public RandomWritable createRandomWriter(URI uri) throws Exception;

}
