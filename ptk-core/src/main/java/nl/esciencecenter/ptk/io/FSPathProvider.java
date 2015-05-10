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
import java.net.URI;
import java.nio.file.LinkOption;
import java.util.List;

import nl.esciencecenter.ptk.io.exceptions.FileURISyntaxException;

/**
 * Combined interface for resolving both local filenames and URIs.
 */
public interface FSPathProvider {

    public abstract List<FSPath> listRoots();

    public abstract URI resolvePathURI(String path) throws FileURISyntaxException;

    public abstract FSPath resolvePath(String path) throws FileURISyntaxException;

    public abstract FSPath resolvePath(java.net.URI uri) throws FileURISyntaxException;

    public abstract RandomReadable createRandomReader(FSPath node) throws IOException;

    public abstract RandomWritable createRandomWriter(FSPath node) throws IOException;

    public abstract InputStream createInputStream(FSPath node) throws IOException;

    public abstract OutputStream createOutputStream(FSPath node, boolean append) throws IOException;

    public abstract LinkOption[] linkOptions();

}
