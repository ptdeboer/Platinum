package nl.esciencecenter.ptk.task;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class TaskStats {

    public String name = null;
    public long todo = -1;
    public long done = -1;
    public long startTimeMillies = -1;
    public long stopTimeMillies = -1;
    public long todoLastUpdateTimeMillies = -1;
    public long doneLastUpdateTimeMillies = -1;
    public boolean isDone = false;

    protected TaskStats() {
    }

    public TaskStats(String taskName, long todo) {
        this.name = taskName;
        this.todo = todo;
    }

    public void markEnd() {
        isDone = true;
        this.stopTimeMillies = System.currentTimeMillis();
        this.doneLastUpdateTimeMillies = System.currentTimeMillis();
        this.todoLastUpdateTimeMillies = System.currentTimeMillis();
    }

    public void markStart() {
        isDone = false;
        long time = System.currentTimeMillis();
        // init!
        this.done = 0;
        this.startTimeMillies = time;
        this.todoLastUpdateTimeMillies = time;
        this.doneLastUpdateTimeMillies = time;
    }

    public void updateDone(long numDone) {
        this.done = numDone;
        this.doneLastUpdateTimeMillies = System.currentTimeMillis();
    }

}
