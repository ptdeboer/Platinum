package nl.esciencecenter.ptk.vbrowser.ui.browser;

/**
 * Pre defined view modes.
 */
public enum BrowserViewMode {

    ICONS16(16), ICONS48(48), ICONS96(96), ICONLIST16(16), ICONSLIST48(48), TABLE, CONTENT_VIEWER;

    int iconSize = 48;

    BrowserViewMode() {
    }

    BrowserViewMode(int size) {
        iconSize = size;
    }

    public int getIconSize() {
        return iconSize;
    }
}
