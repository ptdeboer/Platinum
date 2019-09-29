package nl.piter.vterm.ui.dialogs;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionDialog {

    public static void show(Object source, Throwable e) {
        log.error("Exception from:{} => '{}'", source, e.getMessage());
        log.error("Exception>>>", e);
    }

}
