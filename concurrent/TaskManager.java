package info.kgeorgiy.ja.shevchenko.concurrent;

/**
 * Implementation for class managing tasks.
 */
public class TaskManager {
    private int cnt;

    /**
     * Constructor.
     * @param cnt number of tasks.
     */
    public TaskManager(int cnt) {
        this.cnt = cnt;
    }

    /**
     * Listens tasks.
     * @throws InterruptedException when error while waiting
     */
    public synchronized void waitResult() throws InterruptedException {
        while (!isDone()) {
            wait();
        }
    }

    /**
     * Changes counter, when one task is done.
     */
    public synchronized void oneIsDone() {
        cnt--;
        if(isDone()) {
            notify();
        }
    }

    private boolean isDone() {
        return cnt == 0;
    }
}
