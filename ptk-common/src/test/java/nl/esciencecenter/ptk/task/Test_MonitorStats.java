package nl.esciencecenter.ptk.task;

import org.junit.Assert;
import org.junit.Test;

public class Test_MonitorStats {

    @Test
    public void testCreateEmpty() {
        TaskMonitorAdaptor taskMonitor = new TaskMonitorAdaptor();
        MonitorStats empty = new MonitorStats(taskMonitor);

        // Assert defaults
        Assert.assertEquals("NILL MonitorStats getETA() should return -1", -1, empty.getETA());
        Assert.assertEquals("NILL MonitorStats getTotalDoneTime() should return -1", -1, empty.getTotalDoneTime());
    }

    @Test
    public void testCreateNull() {
        MonitorStats empty = new MonitorStats(null);

        // Assert defaults
        Assert.assertEquals("NULL MonitorStats getETA() should return -1", -1, empty.getETA());
        Assert.assertEquals("NULL MonitorStats getTotalDoneTime() should return -1", -1, empty.getTotalDoneTime());
    }

}
