/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin;

import java.awt.Cursor;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.swing.Icon;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.net.URIFactory;
import nl.esciencecenter.ptk.ui.dialogs.ExceptionDialog;
import nl.esciencecenter.ptk.ui.icons.IconProvider;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.viewers.vrs.ViewerResourceLoader;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

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
        return getVRL().getBasename(); 
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
    
    public VRL getConfigPropertiesURI(String configPropsName) throws URISyntaxException
    {
        VRL confVrl=this.getResourceHandler().getViewerConfigDir();
        if (confVrl==null)
        {
            logger.warnPrintf("No viewer configuration directory configured\n");
            return null;
        }
        
        VRL vrl=confVrl.appendPath("/viewers/"+configPropsName);
        return vrl;
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
            catch (Exception e)
            {
                throw new IOException(e.getMessage(),e);
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
        catch (Exception e)
        {
            throw new IOException(e.getMessage(),e);
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

    protected void handle(String messageString,Throwable ex)
    {
        ExceptionDialog.show(this, messageString, ex, false);
    }
}
