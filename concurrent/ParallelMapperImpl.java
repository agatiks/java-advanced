package info.kgeorgiy.ja.shevchenko.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Implementation for parallel work mapper.
 */
@SuppressWarnings("unused")
public class ParallelMapperImpl implements ParallelMapper {
    private final TaskQueue tasks;
    private final Thread[] threads;

    /**
     * Constructor.
     * @param threadNum number of threads
     */
    public ParallelMapperImpl(int threadNum) throws IllegalArgumentException{
        if(threadNum <= 0) {
            throw new IllegalArgumentException("Incorrect number of threads");
        }
        this.threads = new Thread[threadNum];
        tasks = new TaskQueue();
        for (int i = 0; i < threadNum; i++) {
            threads[i] = new Thread(() -> {
                try {
                while (!Thread.interrupted()) {
                    Runnable task = tasks.poll();
                    task.run();
                }
                } catch (InterruptedException ignored) {
                } finally {
                    Thread.currentThread().interrupt();
                }
            });
            threads[i].start();
        }
    }

    /**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performs in parallel.
     *
     * @throws InterruptedException if calling thread was interrupted
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        if(f == null || args == null) {
            throw new IllegalArgumentException("Arguments can't be null.");
        }
        List<R> res = new ArrayList<>(Collections.nCopies(args.size(), null));
        TaskManager manager = new TaskManager(args.size());
        for (int i = 0; i < args.size(); i++) {
            tasks.push(new Task<T, R>(f, args.get(i), res, i, manager));
        }
        manager.waitResult();
        return res;
    }

    /** Stops all threads. All unfinished mappings leave in undefined state. */
    @Override
    public void close() {
        for (Thread thread : threads) {
            thread.interrupt();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
