package info.kgeorgiy.ja.shevchenko.concurrent;

import java.util.List;
import java.util.function.Function;

/**
 * Implementation for task class.
 * @param <T> start type
 * @param <R> end type
 */
public class Task<T, R> implements Runnable {
    private final Function<? super T, ? extends R> f;
    private final T argument;
    private final List<R> res;
    private final int ind;
    private final TaskManager manager;

    /**
     * Constructor.
     * @param f target function
     * @param t argument
     * @param res list with results of operation
     * @param i index
     * @param manager task manager
     */
    public Task(Function<? super T, ? extends R> f, T t, List<R> res, int i, TaskManager manager) {
        this.f = f;
        this.argument = t;
        this.res = res;
        this.ind = i;
        this.manager = manager;
    }

    /**
     * Runs Runnable.
     */
    @Override
    public void run() {
        res.set(ind, f.apply(argument));
        synchronized (manager) {
            manager.oneIsDone();
        }
    }
}
