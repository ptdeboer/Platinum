package nl.esciencecenter.ptk.vbrowser.ui.dailogs;

import nl.esciencecenter.ptk.vbrowser.ui.dialogs.CopyDialog;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class ShowCopyDialog {

    /**
     * Auto-generated main method to display this JDialog
     */
    public static void main(String[] args) {
        try {
            VRL sourceVrl = new VRL("file:///usr/lib/verylongurl_1234567890_ABCDEFGHIJKLMNOPQRSTUVW_abcdefghijklmnopqrstuvxwz/test.txt");
            VRL destVrl = new VRL("file:///etc/passwd");

            AttributeSet set = new AttributeSet();
            set.set("Hostname", "localhost");
            set.set("Length", 12346);
            set.set("Modification", "1984-Jan-01 12:34:56");

            AttributeSet set2 = set.duplicate();
            set2.set("Modification", "1984-Jan-01 12:34:59");
            set2.set("Creation", "1984-Jan-01 12:34:56");
            set2.set("Usage", "1984-Jan-01 12:34:56");

            CopyDialog dialog = CopyDialog.showCopyDialog(null,
                    sourceVrl,
                    set,
                    destVrl,
                    set2,
                    true);

            System.out.println(">>> DONE <<<");
            System.out.println("option       =" + dialog.getCopyOption());
            System.out.println("skip all     =" + dialog.getSkipAll());
            System.out.println("overwrite all=" + dialog.getOverwriteAll());

        } catch (VRLSyntaxException e1) {
            e1.printStackTrace();
        }

    }

}
