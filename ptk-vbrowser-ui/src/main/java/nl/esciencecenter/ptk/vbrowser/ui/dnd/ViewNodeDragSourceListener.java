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

package nl.esciencecenter.ptk.vbrowser.ui.dnd;

import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

/**
 * DragSourceListener for ViewNodes. <br>
 * This way a ViewNode source can interact with Drag and Drops. ViewNodeDropTarget does most of the
 * work when dragging and dropping, but when dragging into the local desktop, the 'drag' is out of
 * reach of the VBrowser.
 */
public class ViewNodeDragSourceListener implements DragSourceListener {
    public ViewNodeDragSourceListener() {
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
        DnDUtil.log.debug("dragSource:dragEnter():{}", dsde);
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {
        DnDUtil.log.debug("dragSource:dragOver():{}", dsde);
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {
        DnDUtil.log.debug("dragSource:dropActionChanged():{}", dsde);
    }

    @Override
    public void dragExit(DragSourceEvent dse) {
        DnDUtil.log.debug("dragSource:dragExit():{}", dse);
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        DnDUtil.log.debug("dragSource:dragDropEnd():{}", dsde);
    }

}
