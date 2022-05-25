package nl.esciencecenter.ptk.ui.widgets;

import nl.esciencecenter.ptk.data.StringList;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

public class TestShowNavigationBarNimbus {

    public static class BarListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.printf("Event:%s\n", e);
        }

    }

    public static void main(String[] args) {
        try {
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void start() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {

        // --- use Nimbus!
        UIManager.setLookAndFeel(NimbusLookAndFeel.class.getCanonicalName());
        // ---

        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        frame.getContentPane().add(panel);

        NavigationBar bar = new NavigationBar();
        BarListener listener = new BarListener();
        bar.addTextFieldListener(new BarListener());
        bar.addNavigationButtonsListener(listener);
        bar.setEnableNagivationButtons(true);
        panel.add(bar, BorderLayout.CENTER);

        bar.setHistory(new StringList("file:/", "http://the.web.net/foo", "local:dummy"));
//        bar.setIcon(loadIcon("home_folder.png"));
        bar.setIcon(loadIcon("icons/files/directory16.png"));

        //\\//\\//\\
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static ImageIcon loadIcon(String name) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(name);
        System.err.printf(">>>URL = %s=>%s\n", name, url);
        return new ImageIcon(url);
    }
}
