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

import java.nio.file.LinkOption;
import java.util.List;

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/** 
 * Explicit file system path. 
 * 
 * @author Piter T. de Boer
 *
 */
public interface VFSPath extends VPath
{
    // downcast to VFileSystem. 
    public VFileSystem getVFileSystem() throws VrsException; 
    
    @Override
    public VRL resolvePathVRL(String path) throws VrsException; 
    
    // Downcast VPath to VFSPath: 
    @Override
    public VFSPath resolvePath(String path) throws VrsException; 

    // Downcast VPath to VFSPath:
    @Override
    public VFSPath getParent() throws VrsException; 
       
    /**
     * @return true if current path is root of this file system, for example "/" or "C:". 
     */
    public abstract boolean isRoot() throws VrsException; 
    
    public abstract boolean isDir(LinkOption... linkOptions) throws VrsException; 
    
    public abstract boolean isFile(LinkOption... linkOptions) throws VrsException; 
 
    public abstract boolean exists(LinkOption... linkOptions) throws VrsException; 
    
    public abstract List<? extends VFSPath> list() throws VrsException; 
 
    /** 
     * Create last part of this path as directory. 
     * @param ignoreExisting
     * @throws VrsException
     */
    public abstract void mkdir(boolean ignoreExisting) throws VrsException;

    /** 
     * Create complete path as directory. 
     * @param ignoreExisting
     * @throws VrsException
     */
    public abstract void mkdirs(boolean ignoreExisting) throws VrsException;

    public abstract void createFile(boolean ignoreExisting) throws VrsException;
    
}
