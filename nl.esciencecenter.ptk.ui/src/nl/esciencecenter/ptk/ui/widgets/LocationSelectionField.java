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

package nl.esciencecenter.ptk.ui.widgets;

import java.awt.Color;
import java.awt.Event;
import java.awt.Font;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.TooManyListenersException;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.io.FSUtil;
import nl.esciencecenter.ptk.io.FileURISyntaxException;
import nl.esciencecenter.ptk.jfx.util.FXFileChooser;
import nl.esciencecenter.ptk.jfx.util.FXFileChooser.ChooserType;
import nl.esciencecenter.ptk.ui.icons.IconProvider;
import nl.esciencecenter.ptk.util.logging.ClassLogger;

public class LocationSelectionField extends JPanel implements URIDropTargetLister
{
    private static final long serialVersionUID = 7668815810402466501L;
 
    public static enum LocationType { FileType, DirType, URIType }; 
    
    public class FileLocationEvaluator implements ActionListener, FocusListener
    {
    	private JTextField locationTF;
    	//Color originalBGColor; 
    	
    	public FileLocationEvaluator(JTextField textField)
    	{
    		this.locationTF=textField;
    		//originalBGColor=textField.getBackground(); 
    	}

    	@Override
    	public void actionPerformed(ActionEvent e) 
    	{
    	    evaluate();
    	}
    	
    	public boolean evaluate()
    	{
    		String text=locationTF.getText();
    		StringHolder reasonH=new StringHolder(); 
    		boolean isValidPath=FSUtil.getDefault().isValidPathSyntax(text,reasonH); 
    		if (isValidPath==false)
    		{
    		    showError(reasonH.value);
    		    return false; 
    		}
    		
    		try
    		{
    			java.net.URI uri;
    			uri = FSUtil.getDefault().resolvePathURI(text);
    			if (locationType==LocationType.URIType)
    			{
    			    locationTF.setText(uri.toString());
    			}
    			else
    			{
    			    locationTF.setText(uri.getPath());
    			}
    			//locationTF.setBackground(originalBGColor);
    			
    			locationTF.setToolTipText("Enter location."); 
    			return true; 
    		}
    		catch (URISyntaxException e1) 
    		{
    		    showError("Syntax Error: Not a valid path or URI:"+e1.getInput());
    			//locationTF.setBackground(Color.RED);
    			return false;
    		}
    	}

    	public void showError(String errorTxt)
        {
    	    locationTF.setToolTipText("Syntax Error:"+errorTxt); 
        }

        @Override
		public void focusGained(FocusEvent e) 
		{
		}

		@Override
		public void focusLost(FocusEvent e)
		{
			evaluate(); 
		}
    }

    public class LSFMouseListener extends MouseAdapter
    {
        public LSFMouseListener() {}

        @Override
        public void mousePressed(MouseEvent e)
        {
            if ( (isLocationEnabled()==false) || (isLocationEditable()==false) )
                return; 
            
            if (enableMetaMenu==false)
                return; 
            
            Object source = e.getSource(); 
            boolean show=false;
            
            // left click on icon 
            if ( (source==fileIconLbl) && ((e.getModifiers() & Event.META_MASK)==0) )
                show=true;
            
            // right (meta) click on TextField:
            if ( (source==locationTF) && ((e.getModifiers() & Event.META_MASK)>0) )
                show=true;
            
            if (show)
            {
                showMetaMenu(e.getX(),e.getY()); 
            }
        }
    }
    
    // ========
    // Instance 
    // ========
    
    private JTextField locationTF;
    
    private JLabel fileIconLbl;

    protected LocationType locationType=LocationType.FileType; 
    
    protected String fileExtensions[]=null;
    
    protected boolean enableMetaMenu=true;

    private LSFMouseListener fieldMouseListener; 
    
    public LocationSelectionField(LocationType type)
    {
        this.locationType=type; 
        initGui();
        initDnD();
    }

    public void initGui()
    {
        this.fieldMouseListener=new LSFMouseListener();
        
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(new LineBorder(Color.GRAY));
        
        {
            fileIconLbl = new JLabel("");
            add(fileIconLbl);
            fileIconLbl.setIcon(loadIcon("files/directory16.png"));
            fileIconLbl.addMouseListener(fieldMouseListener);
        }
        
        {
            locationTF = new JTextField();
            locationTF.setForeground(Color.BLACK);
            add(locationTF);
            locationTF.setColumns(10);
            locationTF.setBorder(new EmptyBorder(2,2,2,2));
            locationTF.addMouseListener(fieldMouseListener);
            
            FileLocationEvaluator evaluator=new FileLocationEvaluator(locationTF);
            locationTF.addActionListener(evaluator);
            locationTF.addFocusListener(evaluator);
        }
        
        {
            JLabel fileSelectorIconLbl = new JLabel("|");   
            add(fileSelectorIconLbl);
        }
    }
    
    private Icon loadIcon(String iconUrl)
    {
        URL url = getClass().getClassLoader().getResource(iconUrl);
        if (url==null)
            return IconProvider.getDefault().getMiniBrokenImageIcon(); 
        return new ImageIcon(url);
    }

    // Accepts both URI and non URI text. 
    public void setLocationText(String txt)
    {
        this.locationTF.setText(txt); 
    }

    public void setLocationURI(java.net.URI uri)
    {
    	switch (locationType)
    	{
    		case DirType:
    		case FileType:
    	        this.locationTF.setText(uri.getPath());   
    			break;
		case URIType:
				this.locationTF.setText(uri.toString()); 
			break;
		default:
			throw new Error("InternalError: Invalid LocationType:"+locationType); 
    	}
    	
    }

    public java.net.URI getLocationURI() throws URISyntaxException
    {	
    	// Use FSUtil to resolve URI: 
    	return FSUtil.getDefault().resolvePathURI(locationTF.getText()); 
    }
    
    public boolean isURI()
    {
    	try
    	{
    		return (getLocationURI()!=null); 
    	}
    	catch (URISyntaxException e)
    	{
    		return false; 
    	}
    }
    
    public void setFileIcon(Icon icon)
    {
        fileIconLbl.setIcon(icon); 
    }
    
    public void setTextFont(Font font)
    {
        this.locationTF.setFont(font); 
    }

    public void setLocationActionCommand(String commandString)
    {
        this.locationTF.setActionCommand(commandString);
    }

    public void addLocationActionListener(ActionListener controller)
    {
        this.locationTF.addActionListener(controller); 
    }

    public String getLocationText()
    {
        return locationTF.getText();
    }

    public void setLocationEnabled(boolean value)
    {
        locationTF.setEnabled(value); 
    }
    
    public boolean isLocationEnabled()
    {
        return locationTF.isEnabled(); 
    }
    
    public void setLocationEditable(boolean value)
    {
        locationTF.setEditable(value); 
    }

    public boolean isLocationEditable()
    {
        return locationTF.isEditable(); 
    }

    /** 
     * Adds default support for dropped URI and URls. 
     */
    protected void initDnD()
    {
        DropTarget dt1=new DropTarget(); 
        DropTarget dt2=new DropTarget(); 

        // enable toolbar and icontext field:  
        this.fileIconLbl.setDropTarget(dt1);
        this.locationTF.setDropTarget(dt2); 
        
        try
        { 
            dt1.addDropTargetListener(new URIDropHandler(this));
            dt2.addDropTargetListener(new URIDropHandler(this));
        }
        catch (TooManyListenersException e)
        {
            ClassLogger.getLogger(this.getClass()).logException(ClassLogger.ERROR, e, "TooManyListenersException:"+e);
        }
    }

    public void notifyUriDrop(List<URI> uris)
    {
        if ((uris!=null) && (uris.size()>0)) 
        {
            this.locationTF.setText(uris.get(0).toString());         
        }
    }

    public void showMetaMenu(int relX,int relY)
    {
        JPopupMenu popUp=new JPopupMenu();  

        {
            JMenuItem mi=new JMenuItem("Location"); 
            popUp.add(mi); 
            mi.setEnabled(false); 
        }
        
        popUp.add(new JSeparator()); 
        
        {
            JMenuItem mi=new JMenuItem("Browse"); 
            mi.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e){
                    showFileSelector(); 
                }    
            });
            
            mi.setActionCommand("Browse");
            popUp.add(mi);
        }
        
         
        popUp.show(this, relX, relY);
    }
    
    public void showFileSelector()
    {
        try
        {
            URI path = FXFileChooser.staticStartFileChooser(ChooserType.OPEN_DIR,this.getLocationURI().getPath());
            if (path!=null)
            {
                this.setLocationURI(path);
            }
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }
}
