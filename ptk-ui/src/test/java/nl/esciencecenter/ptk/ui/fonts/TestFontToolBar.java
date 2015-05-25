package nl.esciencecenter.ptk.ui.fonts;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import nl.esciencecenter.ptk.ui.dnd.DnDFlavors;
import nl.esciencecenter.ptk.ui.widgets.URIDropHandler;
import nl.esciencecenter.ptk.util.logging.PLogger;

public class TestFontToolBar {
    
    public static class FontChangeListener implements FontToolbarListener {

        @Override
        public void updateFont(Font font, Map<?, ?> renderingHints) {
            System.out.printf("New Font:%s\n",font); 
            if (renderingHints!=null) { 
                for (Object key:renderingHints.keySet()) {
                    System.out.printf("renderingHint:'%s':'%s'",key,renderingHints.get(key));
                }
            }
        } 
        
    }
    
    public static void main(String args[]) {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        frame.getContentPane().add(panel);
        //\\
        PLogger.getLogger(DnDFlavors.class).setLevelToDebug();
        PLogger.getLogger(URIDropHandler.class).setLevelToDebug();
        //\\
        FontToolBar bar = new FontToolBar();
        bar.setListener(new FontChangeListener());
        panel.add(bar, BorderLayout.CENTER);
        //\\//\\//\\
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
