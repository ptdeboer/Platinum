package nl.esciencecenter.ptk.ui.widgets;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.ui.widgets.AutoCompleteTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TestAutoCompleteTextField {

    public static class BarListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.printf("Event:%s\n", e);
        }

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        AutoCompleteTextField bar = new AutoCompleteTextField();
        panel.add(bar, BorderLayout.CENTER);

        bar.setHistory(new StringList("file:/", "http://the.web.net/foo", "local:dummy"));

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
