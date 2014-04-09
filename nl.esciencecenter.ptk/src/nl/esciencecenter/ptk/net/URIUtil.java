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

package nl.esciencecenter.ptk.net;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** 
 * URI Factory methods. 
 */
public class URIUtil
{

    public static URI replacePath(URI uri, String newPath) throws URISyntaxException
    {
        return new URIFactory(uri).setPath(newPath).toURI();
    }

    public static URI replaceScheme(URI uri, String newScheme) throws URISyntaxException
    {
        return new URIFactory(uri).setScheme(newScheme).toURI();
    }

    public static URI replaceHostname(URI uri, String newHostname) throws URISyntaxException
    {
        return new URIFactory(uri).setHostname(newHostname).toURI();
    }


    public static URI resolvePath(URI workingDir, URI userHome, boolean resolveTilde, String path) throws URISyntaxException
    {
        if ((resolveTilde) && (path != null) && path.contains("~"))
        {
            String homePath = URIFactory.uripath(userHome.getPath());
            URIUtil.resolveTilde(homePath,path); 
        }

        return URIUtil.resolvePathURI(workingDir, path);
    }
    
    public static URI resolvePathURI(URI uri, String relativePath) throws URISyntaxException
    {
        URIFactory fac=new URIFactory(uri); 
        String newPath=fac.resolvePath(relativePath);
        return fac.setPath(newPath).toURI(); 
    }

    public static URI appendPath(URI uri, String path) throws URISyntaxException
    {
        return new URIFactory(uri).appendPath(path).toURI();
    }

    public static URI replaceUserinfo(URI uri, String userInfo) throws URISyntaxException
    {
        return new URIFactory(uri).setUserInfo(userInfo).toURI();
    }

    public static List<URI> parseURIList(String urilist, String pathSeperatorRE)
    {
        Scanner scanner = new Scanner(urilist.trim());
        scanner.useDelimiter(pathSeperatorRE);

        List<URI> uris = new ArrayList<URI>();

        while (scanner.hasNext())
        {
            String lineStr = scanner.next();

            try
            {
                uris.add(new URIFactory(lineStr).toURI());
            }
            catch (URISyntaxException e)
            {
                ; // skip ?
            }
        }
        scanner.close();
        return uris;
    }

    /** 
     * If path start with tilde, replace actual tilde with userHome. 
     */
    public static String resolveTilde(String homePath, String path)
    {
        String subPath; 
        
        if (path.startsWith("~/"))
        {   
            subPath=path.substring(2); 
            path=homePath+"/"+subPath; 
        }
        else if (path.startsWith("~"))
        {
            subPath=path.substring(1); 
            path=homePath+"/"+subPath; 
        }
        else if (path.startsWith("/~"))
        {
            subPath=path.substring(2);
            path=homePath+"/"+subPath; 
        }
        
        return path; 
    }


}
