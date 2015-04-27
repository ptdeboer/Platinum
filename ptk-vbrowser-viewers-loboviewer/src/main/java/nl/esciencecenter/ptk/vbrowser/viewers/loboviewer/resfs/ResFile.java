/*
 * Copyright 2006-2010 Virtual Laboratory for e-Science (www.vl-e.nl)
 * Copyright 2012-2013 Netherlands eScience Center.
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

package nl.esciencecenter.ptk.vbrowser.viewers.loboviewer.resfs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceNotFoundException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsIOException;
import nl.esciencecenter.vbrowser.vrs.io.VStreamReadable;
import nl.esciencecenter.vbrowser.vrs.node.VPathNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import org.lobobrowser.main.ExtensionManager;

public class ResFile extends VPathNode implements VStreamReadable
{
    public ResFile(ResResourceSystem resResourceSystem, VRL vrl)
    {
        super(resResourceSystem, vrl);
    }

    public InputStream createInputStream() throws VrsException
    {

        try
        {
            URL url = getVRL().toURL();
            String host = url.getHost();
            ClassLoader classLoader;

            if (host == null)
            {
                classLoader = this.getClass().getClassLoader();
            }
            else
            {
                classLoader = ExtensionManager.getInstance().getClassLoader(host);
                if (classLoader == null)
                {
                    classLoader = this.getClass().getClassLoader();
                }
            }
            String file = url.getPath();
            InputStream in = classLoader.getResourceAsStream(file);
            if (in == null)
            {
                if (file.startsWith("/"))
                {
                    file = file.substring(1);
                    in = classLoader.getResourceAsStream(file);
                    if (in == null)
                    {
                        throw new ResourceNotFoundException("Resource " + file + " not found in " + host + ".", null);
                    }
                }
            }
            return in;

        }
        catch (IOException e)
        {
            throw new VrsIOException("Failed to load:" + vrl + "\n" + e.getMessage(), e);
        }

    }

    @Override
    public String getResourceType()
    {
        return "ResFile";
    }

}
