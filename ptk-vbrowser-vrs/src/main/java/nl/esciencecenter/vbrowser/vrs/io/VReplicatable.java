package nl.esciencecenter.vbrowser.vrs.io;

import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public interface VReplicatable
{
    
    public VRL[] getReplicas();

    public boolean registerReplicas(VRL[] replicaVRLs);
    
    public boolean unregisterReplicas(VRL[] replicaVRLs);

}
