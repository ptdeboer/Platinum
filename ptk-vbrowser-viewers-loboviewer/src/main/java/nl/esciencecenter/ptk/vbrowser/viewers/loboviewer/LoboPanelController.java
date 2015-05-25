/*
 * Copyright 2006-2010 Virtual Laboratory for e-Science (www.vl-e.nl)
 * Copyright 2012-2013 Netherlands eScience Center.
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

package nl.esciencecenter.ptk.vbrowser.viewers.loboviewer;

import java.net.URL;

import nl.esciencecenter.ptk.ui.dialogs.ExceptionDialog;
import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import org.lobobrowser.ua.NavigationEvent;
import org.lobobrowser.ua.NavigationVetoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoboPanelController implements org.lobobrowser.ua.NavigationListener
{
    private static final Logger logger=LoggerFactory.getLogger(LoboPanelController.class);
    

    private LoboBrowser loboBrowser;
    
    public LoboPanelController(LoboBrowser lobo)
    {
        this.loboBrowser=lobo; 
    }

    public void debugPrintf(String format,Object... args)
    {
        logger.debug(String.format(format,args)); 
    }

    void handle(String msg,Throwable e)
    {
        logger.error("Exception:{} => {}",msg,e); 
        ExceptionDialog.show(this.loboBrowser, "Lobo Exception", new VrsException("Lobo Error:"+msg+"\n"+e.getMessage(),e),false);
    }

    public boolean isStandalone()
    {
        return this.loboBrowser.isStartedAsStandalone();
    }
    
    @Override
    public void beforeLocalNavigate(NavigationEvent event) throws NavigationVetoException 
    {
        java.net.URL url=event.getURL();
        logger.debug("beforeLocalNavigate:{}",url);

        
        try
        {
            if (redirect(url,false,false))
            {
                throw new NavigationVetoException();
            }
        }
        catch (Exception e)
        {
            handle("Invalid URL:"+url,e); 
            // block:
            throw new NavigationVetoException();
        }
    }


     @Override
     public void beforeNavigate(NavigationEvent event) throws NavigationVetoException
     {
         // Local = 'Frame' Event
         //Object linkObject = event.getLinkObject();
         java.net.URL url=event.getURL();
         VRL vrl=null; 
      
         logger.debug("beforeNavigate:{}",url);
         
         try
         {  
             vrl=new VRL(url); 
         }
         catch (Exception e)
         {
             handle("Invalid URL:"+url,e);
             throw new NavigationVetoException();
         }
         
         // ===
         // always forward clicked links to master browser
         // View Event will be called back by updateLocation(). 
         // After calling navigate() the call will pass here again
         // and isFromClick will be false. 
         // This way only view events from VBrowser will be handled. 
         // === 
         if (event.isFromClick()==true)
         {
             debugPrintf("beforeNavigate isFromClick()==true:%s\n",url);
             loboBrowser.fireHyperLinkEvent(vrl,false);
             throw new NavigationVetoException();
         }
         else
         {
             debugPrintf("beforeNavigate isFromClick()==false:%s\n",url);
         }
         
         // Only manual navigate calls may proceed here
         // These are called from updateLocation() which contain
         // already 'approved' MasterBrowser VRLs for this viewer. 
      }

     @Override
     public void beforeWindowOpen(NavigationEvent event) throws NavigationVetoException
     {
         // Object linkObject = event.getLinkObject();
         java.net.URL url=event.getURL(); 
         logger.debug("beforeWindowOpen:{}",url);
         
         try
         {
             if (redirect(url,true,false))
                 throw new NavigationVetoException();
         }
         catch (Exception e)
         {
             handle("Invalid URL:"+url,e); 
             // block:
             throw new NavigationVetoException();
         }
     }

     private boolean redirect(URL url,boolean openNew,boolean fireFollowEvents) throws VrsException
     {
        boolean handled=loboBrowser.handleLink(new VRL(url),openNew); 
        
        if (handled==true)
        {
            return true;
        }
        
        if (fireFollowEvents)
        {
            loboBrowser.fireLinkFollowedEvent(new VRL(url)); 
        }
        
        return false;
     }

    public boolean handleLink(VRL vrl, boolean standalone) throws VrsException
    {
        return this.loboBrowser.handleLink(vrl,standalone); 
    }


}
