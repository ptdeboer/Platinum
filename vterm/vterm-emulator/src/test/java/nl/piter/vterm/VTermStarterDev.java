package nl.piter.vterm;

import lombok.extern.slf4j.Slf4j;
import nl.piter.vterm.emulator.VTermChannelProvider;
import nl.piter.vterm.ui.panel.VTerm;

/**
 * Custom starter for dev environment.
 * This will include the log4j.xml from test/resources.
 */
@Slf4j
public class VTermStarterDev {

    public static void main(String[] args) {

        String cwd = System.getProperty("user.dir");
        log.info("Starting [dev] from:{}",cwd);

        VTermChannelProvider provider = new VTermChannelProvider();
        new VTerm()
                .withVTermChannelProvider(provider)
                .start(args);

    }

}
