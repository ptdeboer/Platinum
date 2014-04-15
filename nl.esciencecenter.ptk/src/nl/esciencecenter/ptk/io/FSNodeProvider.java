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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/** 
 * Delegated FileSystem handler. 
 */
public interface FSNodeProvider
{
    public abstract String getScheme(); 

    public abstract List<FSNode> listRoots() throws IOException;
    
    public abstract FSNode newFSNode(java.net.URI uri);

    public abstract RandomReadable createRandomReader(FSNode node) throws IOException;

    public abstract RandomWritable createRandomWriter(FSNode node) throws IOException;

    public abstract InputStream createInputStream(FSNode node) throws IOException;

    public abstract OutputStream createOutputStream(FSNode node,boolean append) throws IOException;

}