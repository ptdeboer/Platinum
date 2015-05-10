package nl.esciencecenter.vbrowser.vrs.dummyrs;

import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class DummyRS implements VResourceSystem {
    protected VRSContext vrsContext;
    protected ResourceConfigInfo resourceInfo;
    protected VRL serverVrl;

    public DummyRS(VRSContext context, ResourceConfigInfo info, VRL vrl) {
        resourceInfo = info;
        serverVrl = vrl;
        vrsContext = context;
    }

    @Override
    public VRL getServerVRL() {
        return serverVrl;
    }

    @Override
    public VRL resolveVRL(String path) throws VrsException {
        return serverVrl.resolvePath(path);
    }

    @Override
    public VPath resolvePath(String path) throws VrsException {
        return createNode(resolveVRL(path));
    }

    @Override
    public VPath resolvePath(VRL vrl) throws VrsException {
        return new DummyNode(this, vrl, false);
    }

    public VPath createNode(VRL vrl) {
        return new DummyNode(this, vrl, false);
    }

    @Override
    public VRSContext getVRSContext() {
        return vrsContext;
    }

}
