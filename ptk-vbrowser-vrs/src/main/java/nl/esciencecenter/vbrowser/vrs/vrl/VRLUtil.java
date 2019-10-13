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

package nl.esciencecenter.vbrowser.vrs.vrl;

import java.net.URI;
import java.util.List;

public class VRLUtil {

    public static URI[] toURIs(VRL[] vrls) {
        URI[] uris = new URI[vrls.length];
        for (int i = 0; i < vrls.length; i++) {
            if (vrls[i] != null) {
                uris[i] = vrls[i].toURINoException();
            } else {
                uris[i] = null;
            }
        }
        return uris;
    }

    public static URI[] toURIs(List<VRL> vrls) {
        if (vrls == null)
            return null;

        URI[] uris = new URI[vrls.size()];

        for (int i = 0; i < vrls.size(); i++) {
            VRL vrl = vrls.get(i);
            if (vrl != null) {
                uris[i] = vrls.get(i).toURINoException();
            }
        }

        return uris;
    }

    public static VRL getServerVRL(VRL vrl) {
        return new VRL(vrl.getScheme(), vrl.getUserinfo(), vrl.getHostname(), vrl.getPort(), "/", null, null);
    }

}
