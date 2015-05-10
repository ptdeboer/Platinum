package nl.esciencecenter.ptk.browser.uitest.dummy;

import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;

public class DummyConfigInfo extends ResourceConfigInfo {

    public DummyConfigInfo(DummyProxyNode dummyProxyNode) {
        super(null, dummyProxyNode.getVRL(), "dummy-fs");

        super.setServerPath("/dummyPath", true);
        super.setProperty("DummyProperty", "DummyProperty-value");

    }

}
