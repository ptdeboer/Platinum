package nl.esciencecenter.ptk.task;

import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple Log Event Buffer. Allows random access to all log events.
 */
public class LogBuffer {

    @AllArgsConstructor
    static class LogEvent {
        long timeStamp;
        String message;
        boolean newline;
    }

    private final List<LogEvent> events = new LinkedList<>();

    public void record(String message, boolean newline) {
        events.add(new LogEvent(System.currentTimeMillis(), message, newline));
    }

    public void clear() {
        this.events.clear();
    }

    public int size() {
        return this.events.size();
    }

    public Collection<LogEvent> events() {
        return this.events;
    }

    public String getText(int logEventOffset) {
        StringBuilder sb = new StringBuilder();
        for (int index = logEventOffset; index < size(); index++) {
            LogEvent event = events.get(index);
            sb.append(event.message);
            if (event.newline) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

}
