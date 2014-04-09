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

package nl.esciencecenter.vbrowser.vrs.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.ptk.io.RandomReadable;
import nl.esciencecenter.ptk.io.RandomWritable;
import nl.esciencecenter.ptk.io.ResourceProvider;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VRSResourceProvider implements ResourceProvider
{
    protected VRSClient vrsClient; 
    
    public VRSResourceProvider(VRSClient vrsClient)
    {
        this.vrsClient=vrsClient; 
    }

    @Override
    public URI resolvePathURI(String path) throws URISyntaxException
    {
        try
        {
            return vrsClient.resolvePath(path).toURI();
        }
        catch (VRLSyntaxException e)
        {
            throw new URISyntaxException(path,e.getMessage());
        } 
    }

    @Override
    public OutputStream createOutputStream(URI uri) throws IOException
    {
        try
        {
            return vrsClient.createOutputStream(new VRL(uri));
        }
        catch (VrsException e)
        {
            throw new IOException(e.getMessage(),e);
        }
    }

    @Override
    public InputStream createInputStream(URI uri) throws IOException
    {
        try
        {
            return vrsClient.createInputStream(new VRL(uri));
        }
        catch (VrsException e)
        {
            throw new IOException(e.getMessage(),e);
        }
    }

    @Override
    public RandomReadable createRandomReader(URI uri) throws IOException
    {
        throw new IOException("not implemented: createRandomReader():"+uri); 
    }

    @Override
    public RandomWritable createRandomWriter(URI uri) throws IOException
    {
        throw new IOException("not implemented: createRandomWriter():"+uri);
    }

}
