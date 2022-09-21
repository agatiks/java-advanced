package info.kgeorgiy.ja.shevchenko.concurrent;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Queue for tasks with notifying.
 */
@SuppressWarnings("unused")
public class TaskQueue {
    private static final int MAX_SIZE = (int) 1e6;
    private final Queue<Runnable> tasks;

    /**
     * Constructor.
     */
    public TaskQueue() {
        this.tasks = new LinkedList<>();
    }

    /**
     * Pushes task to queue.
     * @param task target task
     * @throws InterruptedException when can't wait
     */
    public synchronized void push(Runnable task) throws InterruptedException {
        while(tasks.size() == MAX_SIZE) {
            wait();
        }
        tasks.add(task);
        notifyAll();
    }

    /**
     * Polls top element of queue.
     * @return top element? last task
     * @throws InterruptedException when can't wait
     */
    public synchronized Runnable poll() throws InterruptedException {
        while (tasks.isEmpty()) {
            wait();
        }
        // :NOTE: notify не требуется
        return tasks.poll();
    }
}
