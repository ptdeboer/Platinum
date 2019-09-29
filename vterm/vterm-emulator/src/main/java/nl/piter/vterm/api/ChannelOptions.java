package nl.piter.vterm.api;

import java.util.Map;

/**
 *
 */
public interface ChannelOptions {

    int getDefaultRows();

    int getDefaultColumns();

    String getTermType();

    String getOption(String name);

    int getIntOption(String name, int defaultValue);

    Map<String,?> options();

}
