package nl.esciencecenter.ptk.jfx.util;

import java.io.File;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class FXFileChooser implements Runnable
{
    public static enum ChooserType
    {
        OPEN_FILE,
        OPEN_DIR, 
        SAVE_FILE
     }; 
    
    protected String filePath=null; 
    protected java.net.URI chosenPath=null;
    protected ChooserType chooserType;
    
    protected FXFileChooser(ChooserType type)
    {
        this.chooserType=type; 
    }
    
    public java.net.URI startFileChooser(String path)
    {
        File userDirectory = new File(path);
        
        if (!userDirectory.canRead())
        {
            File[] roots = File.listRoots(); 
            userDirectory = roots[0];
        }
        
        // Extention filter
        // FileChooser.ExtensionFilter extentionFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        // fileChooser.getExtensionFilters().add(extentionFilter);
        
        File chosenFile=null; 
        
        switch(chooserType)
        {
            case OPEN_FILE:
            {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(userDirectory);
                chosenFile = fileChooser.showOpenDialog(null);
                break; 
            }
            case SAVE_FILE:
            {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(userDirectory);
                chosenFile = fileChooser.showSaveDialog(null);
                break; 
            }
            case OPEN_DIR:
            {
                DirectoryChooser fileChooser = new DirectoryChooser();
                fileChooser.setInitialDirectory(userDirectory);
                chosenFile = fileChooser.showDialog(null);
                break; 
            }
        }
        
        if (chosenFile!=null)
        {
            chosenPath=chosenFile.toURI();
        }
        else
        {
            chosenPath=null; 
        }
                
        return chosenPath; 
    }
    
    protected void setPath(String path)
    {
        this.filePath=path; 
    }
    
    public java.net.URI getChosenPath()
    {
        return chosenPath; 
    }

    
    @Override
    public void run()
    {
        chosenPath=startFileChooser(filePath); 
        
        synchronized(this)
        {
            this.notifyAll(); // wakeup 
        }
        
    }
    
    // ======
    // Static  
    // ====== 
    
    public static java.net.URI staticStartFileChooser(ChooserType chooserType,String path)
    {
        // will trigger JFX initialization:
        JFXPanel fxPanel = new JFXPanel(); 
        
        FXFileChooser chooser = new FXFileChooser(chooserType);
        chooser.setPath(path); 
        Platform.runLater(chooser); 
        
        try
        {
            synchronized(chooser)
            {
                chooser.wait();
                return chooser.getChosenPath();
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            return null; 
        }
        finally
        {
            fxPanel.setEnabled(false); // dispose() ?  
        }
    }
    

}
