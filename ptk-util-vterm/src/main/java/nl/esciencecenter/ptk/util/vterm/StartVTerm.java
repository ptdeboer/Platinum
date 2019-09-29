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

package nl.esciencecenter.ptk.util.vterm;

import nl.piter.vterm.VTermStarter;
import nl.piter.vterm.api.ShellChannel;
import nl.piter.vterm.emulator.VTermChannelProvider;

import java.net.URI;

public class StartVTerm {

    public static void main(String[] arg) {
        startVTerm();
    }

    public static void startVTerm() {
        new VTermStarter().withChannelProvider(new VTermChannelProvider()).start(new String[0]);
    }

    public static void startVTerm(final VTermChannelProvider provider, final ShellChannel shellChan, final URI loc) {
        new VTermStarter().withChannelProvider(provider).start(shellChan,loc);
    }
}
