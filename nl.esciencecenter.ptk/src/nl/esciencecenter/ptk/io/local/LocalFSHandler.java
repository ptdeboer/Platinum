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

package nl.esciencecenter.ptk.io.local;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.io.FSHandler;
import nl.esciencecenter.ptk.io.FSNode;
import nl.esciencecenter.ptk.io.RandomReadable;
import nl.esciencecenter.ptk.io.RandomWritable;

public class LocalFSHandler extends FSHandler
{
    private static Object instanceMutex = new Object();

    private static LocalFSHandler instance = null;

    public static LocalFSHandler getDefault()
    {
        synchronized (instanceMutex)
        {
            if (instance == null)
            {
                instance = new LocalFSHandler();
            }

            return instance;
        }
    }

    public String getScheme()
    {
        return "file";
    }

    @Override
    public FSNode newFSNode(URI uri)
    {
        return new LocalFSNode(this, uri);
    }

    @Override
    public RandomReadable createRandomReader(FSNode node) throws IOException
    {
        return new LocalFSReader((LocalFSNode) node);
    }

    @Override
    public RandomWritable createRandomWriter(FSNode node) throws IOException
    {
        return new LocalFSWriter((LocalFSNode) node);
    }

    public List<FSNode> listLocalRoots()
    {
        File roots[] = null;

        // for windows this method returns the drives:
        if (GlobalProperties.isWindows() == true)
        {
            // alt get drives to avoid annoying pop-up
            roots = getWindowsDrives();
        }
        else
        {
            // should trigger initialize Security Context
            @SuppressWarnings("unused")
            SecurityManager sm = System.getSecurityManager();
            // disable ?
            System.setSecurityManager(null);
            roots = java.io.File.listRoots();
        }

        ArrayList<FSNode> nodes = new ArrayList<FSNode>();
        for (File root : roots)
        {
            nodes.add(newFSNode(root.toURI()));
        }
        return nodes;
    }

    protected static File[] getWindowsDrives()
    {
        ArrayList<File> rootsV = new ArrayList<File>();

        // update system property
        boolean skipFloppy = true; // GlobalProperties.getBoolProperty(GlobalProperties.PROP_SKIP_FLOPPY_SCAN,
                                   // true);

        // Create the A: drive whether it is mounted or not
        if (skipFloppy == false)
        {
            String drivestr = "A:\\";
            rootsV.add(new File(drivestr));
        }

        // Run through all possible mount points and check
        // for their existence.
        for (char c = 'C'; c <= 'Z'; c++)
        {
            char device[] =  { c, ':', '\\' };
            String deviceName = new String(device);
            File deviceFile = new File(deviceName);

            if ((deviceFile != null) && (deviceFile.exists()))
            {
                rootsV.add(deviceFile);
            }
        }

        File[] rootsArr = rootsV.toArray(new File[0]);

        return rootsArr;
    }

}
