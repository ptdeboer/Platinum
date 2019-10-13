package nl.esciencecenter.vbrowser.vrs.credentials;

public interface Credential {

    String getCredentialType();

    String getUserPrincipal();

    String getGroupPrincipal();

    boolean isValid();

}
