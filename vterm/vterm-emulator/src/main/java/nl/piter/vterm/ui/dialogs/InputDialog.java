/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.ui.dialogs;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InputDialog implements ActionListener {

    private String result = null;
    private final Dialog dialog;
    private final TextField textf;

    public InputDialog(String title, String text, boolean passwd) {
        super();
        dialog = new Dialog(new Frame(), title, true);
        Button ok = new Button("OK");
        Button cancel = new Button("CANCEL");
        textf = new TextField(20);
        textf.setText(text);
        if (passwd) {
            textf.setEchoChar('*');
        }
        dialog.setLayout(new FlowLayout());
        dialog.setLocation(100, 50);
        ok.addActionListener(this);
        cancel.addActionListener(this);
        dialog.add(textf);
        dialog.add(ok);
        dialog.add(cancel);
        dialog.pack();
        dialog.setVisible(true);
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
            }
            if (!dialog.isVisible()) {
                break;
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals("OK")) {
            result = textf.getText();
        } else if (action.equals("CANCEL")) {
        }
        dialog.setVisible(false);
        return;
    }

    public String getText() {
        return result;
    }
}
