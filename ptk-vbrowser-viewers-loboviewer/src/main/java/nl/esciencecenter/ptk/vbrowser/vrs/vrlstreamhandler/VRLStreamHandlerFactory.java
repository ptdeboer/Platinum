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

package nl.esciencecenter.ptk.vbrowser.vrs.vrlstreamhandler;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import nl.esciencecenter.ptk.ssl.SslUtil;
import nl.esciencecenter.ptk.util.logging.PLogger;

/**
 * The VRLStreamHandlerFactory.
 * 
 * It extends URLStreamHandler with the supported protocols from the VRS
 * Registry. Important: At startup this StreamHandleFactory has to be created
 * and set as default in:
 * 
 * <pre>
 * URL.setURLStreamHandlerFactory();
 * </pre>
 * 
 * Currently this is done in the Global class. After this, the URL class can be
 * use to VRS protocols !
 * <p>
 * Examples:
 * <li>URL url=new URL("gftp://fs2.da2.nikhef.nl/");</li>
 * 
 * @author P.T. de Boer
 */

public class VRLStreamHandlerFactory implements URLStreamHandlerFactory
{
    private static final PLogger logger=PLogger.getLogger(URLStreamHandler.class);
    
    private static VRLStreamHandlerFactory instance = null;

    // no constructor:

    // Whether to use SUN's 'file://' URL reader !
    private static boolean use_sun_file_handler = true;

    public synchronized static VRLStreamHandlerFactory getDefault()
    {
        if (instance == null)
        {
            instance = new VRLStreamHandlerFactory();
        }
        
        return instance;
    }

    // =========================================================================
    // //
    //
    // =========================================================================


    public URLStreamHandler createURLStreamHandler(String protocol)
    {
        logger.debugPrintf("createURLStreamHandler() for:%s\n",protocol);
        return new VRLStreamHandler();
    }

}
