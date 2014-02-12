package nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin;

import java.awt.Cursor;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.swing.Icon;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.net.URIFactory;
import nl.esciencecenter.ptk.ui.icons.IconProvider;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.viewers.vrs.ViewerResourceLoader;

public abstract class EmbeddedViewer extends ViewerPanel implements ViewerPlugin,MimeViewer
{
    private static final long serialVersionUID = -873655384459474749L;
        
    private static ClassLogger logger=ClassLogger.getLogger(EmbeddedViewer.class);
    // ===  
    
    protected IconProvider iconProvider=null;
    
    protected String textEncoding = "UTF-8";

    protected Cursor busyCursor = new Cursor(Cursor.WAIT_CURSOR);
    
    protected Properties properties;
        
    public EmbeddedViewer()
    {
        super();
    }
    
    public Cursor getBusyCursor()
    {
        return busyCursor;
    }

    public void setBusyCursor(Cursor busyCursor)
    {
        this.busyCursor = busyCursor;
    }

    public Cursor getDefaultCursor()
    {
        return defaultCursor;
    }

    public void setDefaultCursor(Cursor defaultCursor)
    {
        this.defaultCursor = defaultCursor;
    }

    protected Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    
    public ViewerResourceLoader getResourceHandler()
    {
        PluginRegistry reg=getViewerRegistry(); 
        
        if (reg==null)
            return null; 
        
        return reg.getResourceHandler(); 
    }

    public String getURIBasename()
    {
        return new URIFactory(getURI()).getBasename(); 
    }
    
    protected ResourceLoader getResourceLoader()
    {
        return this.getResourceHandler().getResourceLoader();
    }
    
    protected IconProvider getIconProvider()
    {
        if (this.iconProvider==null)
        {
            iconProvider=new IconProvider(this, getResourceLoader()); 
        }
        
        return iconProvider;
    }
    
    protected Icon getIconOrBroken(String iconUrl)
    {
        return getIconProvider().getIconOrBroken(iconUrl); 
    }
    
    public String getTextEncoding()
    {
        return this.textEncoding;
    }
    
    public void setTextEncoding(String charSet)
    {
        this.textEncoding=charSet;
    }
    
    /** 
     * Returns most significant Class Name
     */
    public String getViewerClass()
    {
        return this.getClass().getCanonicalName(); 
    }
    
    /**
     * Embedded viewer is actual ViewerPanel
     */ 
    @Override
    public ViewerPanel getViewerPanel()
    {
        return this; 
    }
    
    public boolean canView(String mimeType)
    {
       return  new StringList(getMimeTypes()).contains(mimeType); 
    }
    
    public URI getConfigPropertiesURI(String configPropsName) throws URISyntaxException
    {
        URI confUri=this.getResourceHandler().getViewerConfigDir();
        if (confUri==null)
            return null;
        
        URIFactory factory=new URIFactory(confUri);
        factory.appendPath("/viewers/"+configPropsName);
        return factory.toURI(); 
    }
    
    protected Properties loadConfigProperties(String configPropsName) throws IOException
    {   
        if (properties==null)
        {
            try
            {
                properties=getResourceHandler().loadProperties(getConfigPropertiesURI(configPropsName));
            }
            catch (URISyntaxException e)
            {
                throw new IOException("Invalid properties location:"+e.getReason(),e);
            }
        }
        return properties; 
    }
    
    protected void saveConfigProperties(Properties configProps,String optName) throws IOException
    {
        try
        {
            getResourceHandler().saveProperties(getConfigPropertiesURI(optName),configProps);
        }
        catch (URISyntaxException e)
        {
            throw new IOException("Invalid properties location:"+e.getReason(),e);
        }
    }
    
    public boolean isStandaloneViewer()
    {
        return false;
    }
        
    public void errorPrintf(String format,Object... args)
    {
        logger.errorPrintf(format,args); 
    }

    protected void warnPrintf(String format,Object... args)
    {
        logger.warnPrintf(format,args); 
    }

    protected void infoPrintf(String format,Object... args)
    {
        logger.infoPrintf(format,args); 
    }

    protected void debugPrintf(String format,Object... args)
    {
        logger.debugPrintf("DEBUG:"+format,args); 
    }

    public void showMessage(String format, Object... args)
    {
        //redirect to master browser: 
        logger.errorPrintf("MESSAGE:"+format,args); 
    }

}
