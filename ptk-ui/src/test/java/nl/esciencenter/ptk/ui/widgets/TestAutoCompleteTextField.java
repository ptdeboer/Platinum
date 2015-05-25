package nl.esciencenter.ptk.ui.widgets;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.ui.dnd.DnDFlavors;
import nl.esciencecenter.ptk.ui.widgets.AutoCompleteTextField;
import nl.esciencecenter.ptk.ui.widgets.NavigationBar;
import nl.esciencecenter.ptk.ui.widgets.URIDropHandler;
import nl.esciencecenter.ptk.util.logging.PLogger;

public class TestAutoCompleteTextField {
    
    public static class BarListener implements ActionListener  {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.printf("Event:%s\n",e);
        }

    }
    
    public static void main(String args[]) {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        frame.getContentPane().add(panel);
        //
        PLogger.getLogger(DnDFlavors.class).setLevelToDebug();
        PLogger.getLogger(URIDropHandler.class).setLevelToDebug();

        AutoCompleteTextField bar = new AutoCompleteTextField();
        panel.add(bar, BorderLayout.CENTER);
        
        bar.setHistory(new StringList(new String[]{"file:/","http://the.web.net/foo","local:dummy"}));
        
        //\\//\\//\\
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
