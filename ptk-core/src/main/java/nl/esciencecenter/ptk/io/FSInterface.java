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

import nl.esciencecenter.ptk.exceptions.FileURISyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.List;

/**
 * Combined interface for resolving local filenames, URLs and URIs.
 */
public interface FSInterface {

    List<FSPath> listRoots();

    URI resolvePathURI(String path) throws FileURISyntaxException;

    FSPath resolvePath(String path) throws FileURISyntaxException;

    FSPath resolvePath(java.net.URI uri) throws FileURISyntaxException;

    FSPath resolvePath(java.net.URL url) throws FileURISyntaxException;

    RandomReadable createRandomReader(FSPath node) throws IOException;

    RandomWritable createRandomWriter(FSPath node) throws IOException;

    InputStream createInputStream(FSPath node) throws IOException;

    OutputStream createOutputStream(FSPath node, boolean append) throws IOException;

    LinkOption[] linkOptions();

    BasicFileAttributes getBasicAttributes(FSPath fsPath, LinkOption... linkOptions) throws IOException;

    PosixFileAttributes getPosixAttributes(FSPath fsPath, LinkOption... linkOptions) throws IOException;

    FSPath mkdir(FSPath fsNode) throws IOException;

    FSPath mkdirs(FSPath fsNode) throws IOException;

}
