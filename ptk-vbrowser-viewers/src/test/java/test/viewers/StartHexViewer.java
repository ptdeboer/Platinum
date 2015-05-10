package test.viewers;

import nl.esciencecenter.ptk.vbrowser.viewers.internal.HexViewer;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class StartHexViewer {
    // === Main ===

    public static void main(String args[]) {
        // Global.setDebug(true);

        try {
            ViewerTests.testViewer(HexViewer.class, new VRL(
                    "file:///boot/vmlinuz-3.13.0-24-generic"));
            // viewStandAlone(null);
        } catch (Exception e) {
            System.out.println("***Error: Exception:" + e);
            e.printStackTrace();
        }

    }

}
