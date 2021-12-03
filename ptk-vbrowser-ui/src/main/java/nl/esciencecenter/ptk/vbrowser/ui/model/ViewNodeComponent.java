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

package nl.esciencecenter.ptk.vbrowser.ui.model;

/**
 * Interface for any (J)Component which can contain a single ViewNode. Super interface of
 * ViewNodeContainer
 */
public interface ViewNodeComponent {

    /**
     * UIViewModel
     */
    UIViewModel getUIViewModel();

    /**
     * Returns actual ViewNode or RootNode for Containers
     */
    ViewNode getViewNode();

//    /**
//     * Request focus for this component. Returns true if succesful.
//     */
//    boolean requestFocus(boolean value);

    /**
     * Parent of this ViewNodeComponent
     */
    ViewNodeContainer getViewContainer();

}
