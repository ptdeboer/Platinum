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

package nl.esciencecenter.ptk.vbrowser.ui;


import java.net.URL;

/**
 * Start VBrowser with VRS Enabled(!)
 * In dev environment including log4j from test/resourcess
 */
public class StartVRSBrowserDev {
    
    public static void main(String args[]) {

        URL log4j = Thread.currentThread().getContextClassLoader().getResource("log4j.xml");
        System.err.printf("StartVRSBrowserDev:using log4j:%s\n",log4j);
        try {
            new StartVRSBrowser().start(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
