package nl.esciencecenter.ptk.jfx.util;

import static javafx.concurrent.Worker.State.FAILED;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import nl.esciencecenter.ptk.util.logging.ClassLogger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

public class FXWebJPanel extends JPanel 
{
    private static final long serialVersionUID = -7501864809216238899L;
    
    private static final ClassLogger logger=ClassLogger.getLogger(FXWebJPanel.class); 
    // 
    
    private static String toURL(String str)
    {
        try
        {
            return new URL(str).toExternalForm();
        }
        catch (MalformedURLException exception)
        {
            return null;
        }
    }
    
    // ===
    //
    // ===
    
    private JFXPanel jfxPanel;
    private WebEngine engine;
    private JLabel lblStatus = new JLabel();
    private JButton btnGo = new JButton("Go");
    private JTextField txtURL = new JTextField();
    private JProgressBar progressBar = new JProgressBar();
    private EventListener hyperLinkListener;

    public FXWebJPanel(BorderLayout borderLayout,boolean initComponents)
    {
        super(borderLayout);
        
        if (initComponents)
        {
            initComponents();
        }
    }

    protected void initComponents()
    {
        jfxPanel = new JFXPanel();

        createScene();

        ActionListener al = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                loadURL(txtURL.getText());
            }
        };

        btnGo.addActionListener(al);
        txtURL.addActionListener(al);

        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);

        JPanel topBar = new JPanel(new BorderLayout(5, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        topBar.add(txtURL, BorderLayout.CENTER);
        topBar.add(btnGo, BorderLayout.EAST);

        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(lblStatus, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);
        add(jfxPanel, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    protected JFXPanel getJFXPanel()
    {
        return jfxPanel;
    }
    
    protected WebEngine getWebEngine()
    {
        return engine;
    }
    
    private void createScene()
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {

                WebView view = new WebView();
                engine = view.getEngine();

                engine.titleProperty().addListener(new ChangeListener<String>()
                {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue,
                            final String newValue)
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                updateTitle(newValue); 
                            }
                        });
                    }
                });

                engine.setOnStatusChanged(new EventHandler<WebEvent<String>>()
                {
                    @Override
                    public void handle(final WebEvent<String> event)
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                updateStatus(event); 

                            }
                        });
                    }
                });

                engine.locationProperty().addListener(new ChangeListener<String>()
                {
                    @Override
                    public void changed(ObservableValue<? extends String> ov,final String oldValue, final String newValue)
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                updateLocation(oldValue,newValue); 
                            }
                        });
                    }
                });

                engine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>()
                {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue,
                            final Number newValue)
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                progressBar.setValue(newValue.intValue());
                            }
                        });
                    }
                });

                engine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>()
                {

                    public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value)
                    {
                        if (engine.getLoadWorker().getState() == FAILED)
                        {
                            SwingUtilities.invokeLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    JOptionPane.showMessageDialog(FXWebJPanel.this, (value != null) ? engine.getLocation() + "\n"
                                            + value.getMessage() : engine.getLocation() + "\nUnexpected error.",
                                            "Loading error...", JOptionPane.ERROR_MESSAGE);
                                }
                            });
                        }
                    }
                });

                // Register Link Listener: 
                engine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() 
                        {
                            public void changed(ObservableValue ov, State oldState, State newState) 
                            {
                                logger.debugPrintf("changed(): newState=%s\n",newState);
                                
                                if (newState == Worker.State.SUCCEEDED) 
                                {
                                    Document doc = engine.getDocument();
                                    installHyperLinkListeners(doc);  
                                }
                            }
                        });
                
                jfxPanel.setScene(new Scene(view));
            }
        });
    }

    public EventListener getLinkListener()
    {
        if (this.hyperLinkListener==null)
        {
            this.hyperLinkListener = new EventListener() {
                @Override
                public void handleEvent(org.w3c.dom.events.Event ev)
                {
                    logger.debugPrintf("Event:%s:<Target>:%s\n",ev.getType(),ev.getTarget().toString());
                    // Handle here and block:
                    // ev.preventDefault(); 
                }
            };
        }
        return this.hyperLinkListener; 
    }
    
    public void loadURL(final String url)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                String tmp = toURL(url);

                if (tmp == null)
                {
                    tmp = toURL("http://" + url);
                }

                engine.load(tmp);
            }
        });
    }

   
    protected void updateLocation(String oldText,String newText)
    {
        txtURL.setText(newText);
    }

    protected void updateStatus(WebEvent<String> event)
    {
        lblStatus.setText(event.getData());
    }
    
    protected void updateTitle(String text)
    {
        Frame frame=this.getFrame();
        if (frame!=null)
            frame.setTitle(text);
    }
    
    /**
     * Get Parent (J)Frame.  
     */
    public Frame getFrame()
    {
        Component comp=this; 
        
        while (comp!=null)
        {
            Container parentComp = comp.getParent();
            if (parentComp instanceof Frame)
            {
                return (Frame)parentComp;
            }
            
            if (comp==parentComp)
                return null; // loop! 
            
            comp=parentComp; 
        }
        
        return null;
    }

    public URI getURI() throws URISyntaxException
    {
        return new URI(txtURL.getText()); 
    }
    
    private void installHyperLinkListeners(Document doc)
    {
        if (doc==null)
        {
            logger.warnPrintf("*** installHyperLinkListeners(): NULL Document!");
            return; 
        }

        // Element el = doc.getElementById("textarea");
        
        NodeList elList = doc.getElementsByTagName("a");
        
        for (int i=0;i<elList.getLength();i++)
        {
            Node el=elList.item(i);
            // System.err.println("> - Node:"+el);
            ((EventTarget) el).addEventListener("click", getLinkListener(), false);
            ((EventTarget) el).addEventListener("dblclick", getLinkListener(), false);
            ((EventTarget) el).addEventListener("mouseover", getLinkListener(), false);
        }

    }
    
}
