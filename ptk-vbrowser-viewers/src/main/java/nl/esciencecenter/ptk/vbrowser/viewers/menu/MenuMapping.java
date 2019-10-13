package nl.esciencecenter.ptk.vbrowser.viewers.menu;

public class MenuMapping {

    private String resourceTypeRE;
    private String resourceSchemeRE;
    private String resourceStatusRE;
    private String mimeTypeRE;

    public MenuMapping(String type, String scheme, String status, String mimeType) {
        this.resourceSchemeRE = scheme;
        this.resourceTypeRE = type;
        this.resourceStatusRE = status;
        this.mimeTypeRE = mimeType;
    }

    public boolean matches(String resourceType, String resourceScheme, String resourceStatus,
                           String mimeType) {

        //System.err.printf("Matching this:%s <=> {%s,%s,%s,%s}\n",this,resourceType,resourceScheme,resourceStatus,mimeType);

        if (match(resourceTypeRE, resourceType) == false)
            return false;

        if (match(resourceSchemeRE, resourceScheme) == false)
            return false;

        if (match(resourceStatusRE, resourceStatus) == false)
            return false;

        return match(mimeTypeRE, mimeType) != false;

    }

    protected boolean match(String expression, String value) {
        // match all
        if (expression == null)
            return true;

        // treat null as empty string
        if (value == null)
            value = "";

        return (value.compareTo(expression) == 0);
    }

    @Override
    public String toString() {
        return "MenuMapping:[resourceTypeRE:" + resourceTypeRE + ",resourceSchemeRE:"
                + resourceSchemeRE + ", resourceStatusRE:" + resourceStatusRE + ", mimeTypeRE:"
                + mimeTypeRE + "]";
    }

}
