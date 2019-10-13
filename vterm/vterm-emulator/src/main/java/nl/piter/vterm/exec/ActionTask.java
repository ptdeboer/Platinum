/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.exec;

import lombok.extern.slf4j.Slf4j;

/**
 * Wrapper for background task/parallel tasks.
 */
@Slf4j
public abstract class ActionTask implements Runnable {

    private final Object owner;
    private final String name;
    private Thread thread;
    private Throwable exception;

    public ActionTask(Object owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    public Thread thread() {
        return this.thread;
    }

    final protected void setException(Throwable e) {
        this.exception = e;
    }

    final public Throwable getException() {
        return this.exception;
    }

    public boolean isInterupted() {
        if (this.thread == null) return false;
        return this.thread.isInterrupted();
    }

    public boolean isAlive() {
        if (this.thread == null) return false;
        return this.thread.isAlive();
    }

    /**
     * Actual runnable() interface calling the 'doTask'.
     */
    final public void run() {
        try {
            this.doTask();
        } catch (Throwable t) {
            // unhandled exception by doTask() !
            exception = t;
            log.debug("run():Exception:{}", t.getMessage());
            log.debug("run():Exception>>>", t);
        } finally {
        }
    }

    /**
     * Start a single thread and run this task.
     */
    final public void startTask() {
        this.thread = new Thread(this);
        this.thread.start(); // goto run()
    }


    /**
     * Tries to wait until all threads have finished.
     */
    final public void waitFor() throws InterruptedException {
        // will return when thread finishes or already has finished.
        waitFor(0);
    }

    /**
     * Tries to wait untill all threads have finished or timeout (in milli seconds) has been
     * reached.
     */
    final public void waitFor(long timeOutInMillis) throws InterruptedException {
        join(timeOutInMillis);
    }

    final public void join(long timeOutInMillis) throws InterruptedException {
        thread.join();
    }

    /**
     * Actual methods to implement
     */
    public abstract void doTask();

    public abstract void stopTask();


}