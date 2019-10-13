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

package nl.esciencecenter.vbrowser.vrs.node;

import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VFileSystem;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Adaptor class for the VFileSystem.
 */
public abstract class VFileSystemNode extends VResourceSystemNode implements VFileSystem //, VFSPath
{
    protected VFileSystemNode(VRSContext context, VRL serverVrl) {
        super(context, serverVrl);
    }

    public VRL resolveVRL(String relativePath) throws VRLSyntaxException {
        return this.getServerVRL().resolvePath(relativePath);
    }

    @Override
    public VFSPath resolve(String relativePath) throws VrsException {
        return resolve(resolveVRL(relativePath));
    }

    @Override
    public VFSPath resolve(VRL vrl) throws VrsException {
        return createVFSNode(vrl);
    }

    // ===================
    // Abstract Interface
    // ===================

    abstract protected VFSPathNode createVFSNode(VRL vrl) throws VrsException;

}
