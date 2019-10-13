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

package nl.esciencecenter.vbrowser.vrs;

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.io.VDeletable;
import nl.esciencecenter.vbrowser.vrs.io.VFSDeletable;
import nl.esciencecenter.vbrowser.vrs.io.VPathRenamable;
import nl.esciencecenter.vbrowser.vrs.io.VRenamable;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.nio.file.LinkOption;
import java.util.List;

/**
 * Virtual File System Path exends VPath and add FileSystem methods.
 */
public interface VFSPath extends VPath, VRenamable, VPathRenamable, VFSDeletable, VDeletable {

    // downcast to VFileSystem. 
    VFileSystem getFileSystem() throws VrsException;

    @Override
    VRL resolveVRL(String path) throws VrsException;

    // Downcast VPath to VFSPath: 
    @Override
    VFSPath resolve(String path) throws VrsException;

    // Downcast VPath to VFSPath:
    @Override
    VFSPath getParent() throws VrsException;

    /**
     * @return true if current path is root of this file system, for example "/" or "C:/".
     */
    boolean isRoot() throws VrsException;

    boolean isDir(LinkOption... linkOptions) throws VrsException;

    boolean isFile(LinkOption... linkOptions) throws VrsException;

    boolean exists(LinkOption... linkOptions) throws VrsException;

    /**
     * Return length of file. For directories this is unspecified. Some file system implementation
     * return the size of the directories entry.
     */
    long fileLength(LinkOption... linkOptions) throws VrsException;

    // Downcast to VFSPath List. 
    List<? extends VFSPath> list() throws VrsException;

    /**
     * Create last part of this path as directory.
     *
     * @param ignoreExisting - set to true if implementation should ignore an already existing directory.
     * @return true
     * @throws VrsException
     */
    boolean mkdir(boolean ignoreExisting) throws VrsException;

    /**
     * Create complete path as directory.
     *
     * @param ignoreExisting - set to true if implementation should ignore already existing directories.
     * @return true
     * @throws VrsException
     */
    boolean mkdirs(boolean ignoreExisting) throws VrsException;

    /**
     * Create path as zero length file.
     *
     * @return true if create is supported and was succesfull.
     */
    boolean createFile(boolean ignoreExisting) throws VrsException;

    // explicit inheritance and downcast return type from VRenamable 
    VFSPath renameTo(String newNameOrPath) throws VrsException;

    // downcast return type from VPathRenamble 
    VFSPath renameTo(VPath other) throws VrsException;

}
