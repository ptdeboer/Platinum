package uitests.panels;

import nl.esciencecenter.ptk.ui.dialogs.ExceptionDialog;

import java.io.IOException;

public class ShowExceptionDailog {
    public static void main(String[] args) {
        ExceptionDialog.show(null, "Test Non Modal Exception Dailog", new IOException(
                new Exception("Inner Exception")), false);

        ExceptionDialog.show(null, "Test Modal Exception Dailog", new IOException(new Exception(
                "Inner Exception")), true);

    }
}
