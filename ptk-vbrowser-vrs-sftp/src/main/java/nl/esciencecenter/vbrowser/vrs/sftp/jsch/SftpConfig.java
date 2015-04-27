package nl.esciencecenter.vbrowser.vrs.sftp.jsch;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Value Object.
 */
public class SftpConfig
{
    public static final String SSH_USER_KNOWN_HOSTS="known_hosts"; 
    
    public static final String SSH_USER_CONFIG_SIBDUR=".ssh"; 
    
    public static final String SSH_USER_DEFAULT_ID_RSA="id_rsa";
    
    public String host;

    public int port;

    public String user;

    public String userConfigDir; 
    
    public char[] passwd;

    public String sshKnowHostFile=SSH_USER_KNOWN_HOSTS; 
    
    public String[] privateKeys;

    public String[] publicKeys;

    protected Properties properties = new Properties();

    public Properties getProperties()
    {
        return properties;
    }

    public void setProperty(String name, String value)
    {
        properties.setProperty(name, value);
    }

    public Object getProperty(String name)
    {
        return properties.get(name);
    }

    public String getStringProperty(String name)
    {
        Object val = properties.get(name);
        if (val == null)
        {
            return null;
        }
        return val.toString();
    }

    public java.net.URI getServerURI() throws URISyntaxException
    {
        return new java.net.URI("sftp",user,host,port,"/",null,null);
    }
    
    @Override
    public String toString()
    {
        return "SftpConfig:[host:'" + host + "',port:'" + port + ",user:'" + user + "',userConfigDir:'" + userConfigDir + "', passwd="
                + ((passwd!=null)?"<PWD>":"<NO PWD") + ",sshKnowHostFile:'" + sshKnowHostFile + "',privateKeys:'[" + Arrays.toString(privateKeys)
                + "],publicKeys:[" + Arrays.toString(publicKeys) + "]]";
    }
}
