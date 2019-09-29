package nl.esciencecenter.vbrowser.vrs.data.xml;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Mixin Module can replace non (jackson) annotated classes with XML annotated classes.
 */
public class VRSXMLMixinModule extends SimpleModule {

       public VRSXMLMixinModule() {
        super("VRSXMLMixinModule", new Version(0, 0, 1, "1", "vrs.data.xml", "nl.esciencecenter.ptk.vbrowser"));
    }

    @Override
    public void setupModule(SetupContext context) {
        // context.setMixInAnnotations(VRSProperties.class, XMLProperties.class);
    }

}
