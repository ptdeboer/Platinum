package nl.esciencecenter.ptk.util.vterm;

import java.io.IOException;
import java.net.URI;

import javax.swing.SwingUtilities;

import nl.esciencecenter.ptk.exec.ShellChannel;

public class StartVTerm
{
    
    public static void main(String[] arg)
    {
        startVTerm();
    }
    
    public static void startVTerm()
    {
        startVTerm(new VTermChannelProvider(),null,null); 
    }
    
    public static VTerm startVTerm(ShellChannel shellChan)
    {
        return startVTerm(new VTermChannelProvider(),null,shellChan); 
    }
    
    public static VTerm startVTerm(URI loc)
    {
        return startVTerm(new VTermChannelProvider(),loc,null);
    }
    
    public static VTerm startVTerm(VTermChannelProvider channelProvider,final URI optionalLocation,final ShellChannel shellChan)
    {
        final VTerm term = new VTerm(channelProvider);

        // always create windows during Swing Event thread 
        Runnable creator=new Runnable()
        {
            public void run()
            {
                
                // center on screen
                term.setLocationRelativeTo(null);
                term.setVisible(true);
                term.showSplash();
                term.requestFocus();
        
                term.updateFrameSize();
                if (shellChan!=null)
                {
                    term.setShellChannel(shellChan);
                    term.startSession(); 
                }
                
                if (optionalLocation!=null)
                {
                    try
                    {
                        term.openLocation(optionalLocation);
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } 
                }
                
            }
        };
        
        SwingUtilities.invokeLater(creator); 

        return term; 

        /*
         * { Insets insets = frame.getInsets(); int width =
         * awtTerm.getTermWidth(); int height = awtTerm.getTermHeight(); width +=
         * (insets.left + insets.right); height += (insets.top + insets.bottom);
         * frame.setSize(width, height); }
         */
    }
    
}
