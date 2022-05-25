package nl.esciencecenter.ptk.vbrowser.ui.browser.laf;

/**
 * Runtime supported LAF's.
 * Updated for Jdk 11.
 */
public enum LookAndFeelType {
    DEFAULT("Default"),
    NATIVE("Native"),
    WINDOWS("Windows"),
    METAL("Metal"),
    MOTIF("Motif (CDE)"),
    GTK("GTK"),
    NIMBUS("Nimbus"),
    ;
    final private String name;

    LookAndFeelType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}