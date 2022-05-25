package nl.esciencecenter.ptk.vbrowser.ui.dailogs;

import nl.esciencecenter.ptk.vbrowser.ui.attribute.AttributeEditorForm;
import nl.esciencecenter.vbrowser.vrs.VRS;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.dummyrs.DummyRSFactory;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class ShowConfigAttributeEditor {
    public static void main(String[] args) {

        int len = 20;

        Attribute[] attrs = new Attribute[len];

        ResourceConfigInfo info = null;

        VRSContext context = VRS.createVRSContext(null);

        try {
            context.getRegistry().registerFactory(DummyRSFactory.class);

            info = context
                    .getResourceSystemInfoFor(
                            new VRL(
                                    "dummy://username@dummy.localhost.nocom:1234/?par1=par1Value&par2=par2Value#index"),
                            true);

            Attribute[] infoAttrs = info.getConfigAttributeSet().toArray();

            for (int i = 0; i < len; i++) {
                if ((i < infoAttrs.length) && (infoAttrs[i] != null)) {
                    attrs[i] = infoAttrs[i];
                } else {
                    attrs[i] = new Attribute("Field:" + i, "Value" + i);
                    attrs[i].setEditable((i % 2) == 0);
                }
            }

            attrs = AttributeEditorForm.editAttributes("Test AttributeForm", attrs, true);

            System.out.println("--- Dialog Ended ---");

            int i = 0;

            if ((attrs == null) || (attrs.length <= 0))
                System.out.println("NO NEW ATTRIBUTES!");
            else
                for (Attribute a : attrs) {
                    System.out.println(">>> Changed Attrs[" + i++ + "]=" + a);
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
