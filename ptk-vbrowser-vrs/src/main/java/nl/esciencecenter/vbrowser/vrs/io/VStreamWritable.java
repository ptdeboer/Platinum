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

package nl.esciencecenter.vbrowser.vrs.io;

import java.io.OutputStream;

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;

public interface VStreamWritable {

    /**
     * If append==false, a new file will be created or an existing file will be overwritten. The
     * remainder will be truncated. If append is true the file will be opened at the end of the
     * file.
     */
    public OutputStream createOutputStream(boolean append) throws VrsException;

}
