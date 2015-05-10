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

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.util.List;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeComponent;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeContainer;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyNodeDnDHandler.DropAction;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class DnDUtil {
    protected static PLogger dndLogger = PLogger.getLogger(DnDUtil.class);

    public static DnDTransferHandler getDefaultTransferHandler() {
        return DnDTransferHandler.getDefault();
    }

    // === logging ===

    public static void debugPrintf(String format, Object... args) {
        dndLogger.debugPrintf(format, args);
    }

    public static void debugPrintln(String message) {
        dndLogger.debugPrintf("%s\n", message);
    }

    public static void warnPrintf(String format, Object... args) {
        dndLogger.warnPrintf(format, args);
    }

    public static void infoPrintf(String format, Object... args) {
        dndLogger.infoPrintf(format, args);
    }

    public static void errorPrintf(String format, Object... args) {
        dndLogger.errorPrintf(format, args);
    }

    public static void logException(Exception e, String format, Object... args) {
        dndLogger.logException(PLogger.ERROR, e, format, args);
    }

    /**
     * Convert ambigous integer to action DropAction Enum.
     * 
     * @param dndAction
     * @return
     */
    static public DropAction getDropAction(int dndAction) {
        if ((dndAction & DnDConstants.ACTION_COPY) > 0) {
            return DropAction.COPY;
        } else if ((dndAction & DnDConstants.ACTION_MOVE) > 0) {
            return DropAction.MOVE;
        } else if ((dndAction & DnDConstants.ACTION_LINK) > 0) {
            return DropAction.LINK;
        } else {
            throw new Error("Invalid Drop Action:" + dndAction);
        }
    }

    // ====================
    // BrowserInterface
    // ====================

    public static BrowserInterface getBrowserInterface(Component component) {
        if (component instanceof ViewNodeComponent) {
            if (component instanceof ViewNodeContainer) {
                ViewNodeContainer container = (ViewNodeContainer) component;
                return container.getBrowserInterface();
            } else {
                return ((ViewNodeComponent) component).getViewContainer().getBrowserInterface();
            }
        }
        return null;
    }

    // ========================================================================================
    // Static Drop Handlers:
    // ========================================================================================

    /**
     * Paste Data call when for example CTRL-V IS called ! Supplied component is the Swing Component
     * which has the focus when CTRL-V was called !
     * 
     * @param dropAction
     */
    public static boolean doPasteData(Component uiComponent, ViewNode viewNode, Transferable data,
            DropAction effectiveDnDAction) {
        DnDUtil.infoPrintf("doPasteData:(action:%s) on:%s\n", effectiveDnDAction, viewNode);

        return performAcceptedDrop(uiComponent, null, viewNode, data, null, effectiveDnDAction);
    }

    /**
     * Perform the actual drop, after the drop has been accepted. This could be an Interactive drop
     * on ViewNode.
     */
    static public boolean performAcceptedDrop(Component uiComponent, Point point,
            ViewNode targetViewNode, Transferable data, DropAction userDropAction,
            DropAction effectiveDropAction) {
        DnDUtil.infoPrintf("performAcceptedDrop():%s -> %s\n", uiComponent, targetViewNode);

        // Should already be cecked
        if (DnDData.canConvertToVRLs(data) == false) {
            ExtendedList<DataFlavor> flavs = new ExtendedList<DataFlavor>(
                    data.getTransferDataFlavors());
            DnDUtil.errorPrintf("performAcceptedDrop(): Unsupported Data/Flavor(s):\n%s\n",
                    flavs.toString(" - ", "", "\n"));
            return false;
        }

        BrowserInterface browser = DnDUtil.getBrowserInterface(uiComponent);

        if (browser == null) {
            DnDUtil.errorPrintf("performAcceptedDrop(): No BrowserInterface registered for:%s\n",
                    targetViewNode);
            return false;
        }

        boolean result = false;

        try {
            List<VRL> vris = DnDData.getVRLsFrom(data);
            DnDUtil.debugPrintf("performAcceptedDrop(): Actual Drop on ViewNode:%s\n",
                    targetViewNode);
            for (int i = 0; i < vris.size(); i++) {
                DnDUtil.debugPrintf(" -vri[#%d]=%s\n", i, vris.get(i));
            }

            result = browser.doDrop(uiComponent, point, targetViewNode, effectiveDropAction, vris);
        } catch (Exception e) {
            DnDUtil.logException(e, "performAcceptedDrop(): Couldn't get VRLs from data:%s\n", data);
        }

        return result; // Handled!
    }

    public static String mimeTypeClassName(Class<?> cls) {
        String className = cls.getName().replace("class ", "");
        return className;
    }

}
