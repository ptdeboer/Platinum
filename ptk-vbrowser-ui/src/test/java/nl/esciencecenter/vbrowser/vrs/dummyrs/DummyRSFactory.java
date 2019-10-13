package nl.esciencecenter.vbrowser.vrs.dummyrs;

import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import nl.esciencecenter.vbrowser.vrs.vrl.VRLUtil;

public class DummyRSFactory implements VResourceSystemFactory {
    protected String[] schemes = new String[]{"dummy"};

    public static final String dummyIntPar = "dummy.intpar.value";
    public static final String dummyStringPar = "dummy.stringpar.value";
    public static final String dummyBoolPar = "dummy.boolpar.value";
    public static final String dummyEnumPar = "dummy.enumpar.value";

    public static final String[] attrNames = {dummyIntPar, dummyStringPar, dummyBoolPar,
            dummyEnumPar};

    @Override
    public String[] getSchemes() {
        return schemes;
    }

    @Override
    public String createResourceSystemId(VRL vrl) {
        return VRLUtil.getServerVRL(vrl).toString();
    }

    @Override
    public ResourceConfigInfo updateResourceInfo(VRSContext context, ResourceConfigInfo info,
                                                 VRL vrl) {
        info.setDefaultAttribute(new Attribute(dummyIntPar, 1), true);
        info.setDefaultAttribute(new Attribute(dummyStringPar, "stringValue"), true);
        info.setDefaultAttribute(new Attribute(dummyBoolPar, true), true);
        info.setDefaultAttribute(new Attribute(dummyEnumPar, new String[]{"enum1", "enum2"}, 0),
                true);

        for (String name : attrNames) {
            info.getConfigAttributeSet().setEditable(name, true);
        }

        return info;
    }

    @Override
    public VResourceSystem createResourceSystemFor(VRSContext context, ResourceConfigInfo info,
                                                   VRL vrl) throws VrsException {
        return new DummyRS(context, info, vrl);
    }

}
