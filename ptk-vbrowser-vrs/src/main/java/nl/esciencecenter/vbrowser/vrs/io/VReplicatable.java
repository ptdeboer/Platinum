package nl.esciencecenter.vbrowser.vrs.io;

import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public interface VReplicatable {

    VRL[] getReplicas();

    boolean registerReplicas(VRL[] replicaVRLs);

    boolean unregisterReplicas(VRL[] replicaVRLs);

}
