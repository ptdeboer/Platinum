package nl.esciencecenter.vbrowser.vrs.data.xml;

import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlAnnotationIntrospector;

/**
 * Extending NopAnnotationIntrospector is recommended.
 */
public class VRSJacksonXmlAnnotarionIntrospector extends JacksonXmlAnnotationIntrospector {

    private static final long serialVersionUID = -6754790211041739285L;

    public VRSJacksonXmlAnnotarionIntrospector() {
        super();
        init();
    }

    protected void init() {

    }

    @Override
    public PropertyName findRootName(AnnotatedClass ac) {

        PropertyName propName = super.findRootName(ac);
        System.err.printf("findRootName(): propName=%s\n", propName);
        return propName;
    }

    public PropertyName findNameForSerialization(Annotated ac) {
        Class<?> rawClass = ac.getRawType();

        System.err.printf("findNameForSerialization(): rawClass=%s\n", rawClass.getCanonicalName());
        System.err.printf("findNameForSerialization(): Annotated=%s\n", ac);

        PropertyName propName = super.findNameForSerialization(ac);
        System.err.printf("findNameForSerialization(): propName=%s\n", propName);
        if (propName == null) {
            return null;
        }

        String name = propName.getSimpleName();
        if ("attributes".equals(name)) {
            name = "attribute";
        }
        // propName=PropertyName.construct(propName.getNamespace(), name);
        propName = PropertyName.construct(name, propName.getNamespace());

        return propName;
    }

    public String findTypeName(AnnotatedClass ac) {
        String propName = super.findTypeName(ac);
        System.err.printf("findTypeName(): propName=%s\n", propName);
        return propName;
    }

}
