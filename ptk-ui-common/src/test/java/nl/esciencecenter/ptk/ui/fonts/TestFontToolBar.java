package nl.esciencecenter.ptk.ui.fonts;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class TestFontToolBar {

    public static class FontChangeListener implements FontToolbarListener {

        @Override
        public void updateFont(Font font, Map<?, ?> renderingHints) {
            System.out.printf("New Font:%s\n", font);
            if (renderingHints != null) {
                for (Object key : renderingHints.keySet()) {
                    System.out.printf("renderingHint:'%s':'%s'", key, renderingHints.get(key));
                }
            }
        }

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        frame.getContentPane().add(panel);

        FontToolBar bar = new FontToolBar(null,16,32);
        bar.setListener(new FontChangeListener());
        panel.add(bar, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
