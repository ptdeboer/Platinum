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

package nl.esciencecenter.ptk.ui.widgets;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.List;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.ptk.ui.dnd.DnDFlavors;
import nl.esciencecenter.ptk.util.logging.PLogger;

/**
 * Handle drops on the Navigation Bar.
 */
public class URIDropHandler implements DropTargetListener {
    private static PLogger logger = PLogger.getLogger(URIDropHandler.class);

    private URIDropTargetLister uriDropTargetListener;

    public URIDropHandler(URIDropTargetLister uriDropListener) {
        this.uriDropTargetListener = uriDropListener;
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        // accept/reject DataFlavor
        for (DataFlavor flavor : DnDFlavors.uriDataFlavors) {
            if (dtde.isDataFlavorSupported(flavor)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
                return;
            }
        }
        dtde.rejectDrag();
    }

    public void dragOver(DropTargetDragEvent dtde) {
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public void dragExit(DropTargetEvent dte) {
    }

    public void drop(DropTargetDropEvent dtde) {
        // check
        Transferable t = dtde.getTransferable();
        DropTargetContext dtc = dtde.getDropTargetContext();

        List<java.net.URI> uris = null;

        try {
            if (DnDFlavors.canConvertToURIs(t.getTransferDataFlavors(), true)) {
                // first accept drop!
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                uris = DnDFlavors.getURIList(t);

                if ((uris != null) && (uris.size() > 0)) {
                    uriDropTargetListener.notifyUriDrop(uris);
                    dtde.dropComplete(true);
                    return;
                } else {
                    logger.warnPrintf("drop(): Could not convert to one or more URIs:%s\n",
                            new ExtendedList<DataFlavor>(t.getTransferDataFlavors()));
                    dtde.dropComplete(false);
                }
            } else {
                logger.warnPrintf("drop(): Dropped data is not valid URI\n");
                dtde.rejectDrop();
            }
        } catch (UnsupportedFlavorException e) {
            logger.logException(PLogger.ERROR, e, "UnsupportedFlavorException:%s\n", e);
        } catch (IOException e) {
            logger.logException(PLogger.ERROR, e, "IOException:%s\n", e);
        }

        dtde.dropComplete(false);
    }

}
