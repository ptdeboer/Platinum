package nl.esciencecenter.ptk.vbrowser.ui.actions;

import lombok.extern.slf4j.Slf4j;

import java.awt.event.ActionEvent;

@Slf4j
public class InputUIAction extends UIAction {

    InputUIAction(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        log.debug("ActionPerformed: <{}>:'{}': {}", this.getName(), e.getActionCommand(), e);
        // Should delegate to ViewComponent here.
        Object comp = e.getSource();
        if (comp instanceof UIActionListener) {
            ((UIActionListener) comp).uiActionPerformed(this, e);
        }
    }

}
