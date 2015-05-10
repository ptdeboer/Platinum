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

package nl.esciencecenter.ptk.ui;

import nl.esciencecenter.ptk.data.SecretHolder;

/**
 * Dummy non interactive UI, returns false, cancel or default value.
 */
public class DummyUI implements UI {

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void showMessage(String title, String message, boolean modal) {
    }

    @Override
    public boolean askYesNo(String title, String message, boolean defaultValue) {
        return false;
    }

    @Override
    public boolean askOkCancel(String title, String message, boolean defaultValue) {
        return false;
    }

    @Override
    public int askYesNoCancel(String title, String message) {
        return CANCEL_OPTION;
    }

    @Override
    public boolean askAuthentication(String message, SecretHolder secretHolder) {
        return false;
    }

    @Override
    public String askInput(String title, String message, String defaultValue) {
        return null;
    }

}
