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

package nl.esciencecenter.vbrowser.vrs.localfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.esciencecenter.ptk.io.FSUtil;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsIOException;
import nl.esciencecenter.vbrowser.vrs.io.VStreamCreator;
import nl.esciencecenter.vbrowser.vrs.node.VFSPathNode;
import nl.esciencecenter.vbrowser.vrs.node.VFileSystemNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class LocalFileSystem extends VFileSystemNode implements VStreamCreator
{
    private FSUtil fsUtil; 
    
    public LocalFileSystem(VRSContext context) throws VrsException
    {
        super(context,new VRL("file:/"));
        fsUtil=new FSUtil();
    }

    @Override
    protected LocalFSPathNode createVFSNode(VRL vrl) throws VrsException
    {
        
        try
        {
            return new LocalFSPathNode(this,fsUtil.newLocalFSNode(vrl.getPath()));
        }
        catch (IOException e)
        {
            throw new VrsException(e.getMessage(),e);
        }
    }

    @Override
    public InputStream createInputStream(VRL vrl) throws VrsException
    {
        return createVFSNode(vrl).createInputStream();  
    }

    @Override
    public OutputStream createOutputStream(VRL vrl) throws VrsException
    {
        return createVFSNode(vrl).createOutputStream();  
    }


    

}
