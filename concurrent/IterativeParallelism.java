package info.kgeorgiy.ja.shevchenko.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation for interface {@link ListIP};
 */
@SuppressWarnings("unused")
public class IterativeParallelism implements ListIP {
    private final ParallelMapper mapper;

    /**
     * Constructor with mapper.
     * @param mapper mapper
     */
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * default constructor.
     */
    public IterativeParallelism() {
        mapper = null;
    }
    /**
     * Join values to string.
     *
     * @param threads number of concurrent threads.
     * @param values  values to join.
     * @return list of joined result of {@link #toString()} call on each value.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return makeParallel(threads, values,
                stream -> stream.map(Object::toString).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining()));
    }

    /**
     * Filters values by predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to filter.
     * @param predicate filter predicate.
     * @return list of values satisfying given predicated. Order of values is preserved.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return makeParallelWithStreamResult(threads, values,
                stream -> stream.filter(predicate));
    }

    /**
     * Maps values.
     *
     * @param threads number of concurrent threads.
     * @param values  values to filter.
     * @param f       mapper function.
     * @return list of values mapped by given function.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return makeParallelWithStreamResult(threads, values,
                stream -> stream.map(f));
    }

    /**
     * Returns maximum value.
     *
     * @param threads    number or concurrent threads.
     * @param values     values to get maximum of.
     * @param comparator value comparator.
     * @return maximum of given values
     * @throws InterruptedException   if executing thread was interrupted.
     * @throws NoSuchElementException if no values are given.
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        if(values == null) {
            throw new NoSuchElementException();
        }
        return makeParallel(threads, values,
                stream -> stream.max(comparator).orElseThrow(),
                stream -> stream.max(comparator).orElseThrow());
    }

    /**
     * Returns minimum value.
     *
     * @param threads    number or concurrent threads.
     * @param values     values to get minimum of.
     * @param comparator value comparator.
     * @return minimum of given values
     * @throws InterruptedException   if executing thread was interrupted.
     * @throws NoSuchElementException if no values are given.
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    /**
     * Returns whether all values satisfies predicate.
     *
     * @param threads   number or concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return whether all values satisfies predicate or {@code true}, if no values are given
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return makeParallel(threads, values,
                stream -> stream.allMatch(predicate),
                stream -> stream.allMatch(item -> item));
    }

    /**
     * Returns whether any of values satisfies predicate.
     *
     * @param threads   number or concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return whether any value satisfies predicate or {@code false}, if no values are given
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return makeParallel(threads, values,
                stream -> stream.anyMatch(predicate),
                stream -> stream.anyMatch(item -> item));
    }

    private <A, B> List<B> makeParallelWithStreamResult(final int threads, final List<? extends A> list,
                                                                  final Function<Stream<? extends A>, Stream<? extends B>> process) throws InterruptedException {
        return makeParallel(threads, list,
                stream -> process.apply(stream).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    private <T, R> R makeParallel(final int threads, final List<? extends T> list,
                                final Function<Stream<? extends T>, R> process,
                                final Function<Stream<? extends R>, R> collector) throws InterruptedException {
        final int activeThreads = Math.min(threads, list.size());
        final int butchSize = list.size()/activeThreads;
        int biggerButches = list.size()%activeThreads;
        int st, en = 0;
        final List<Stream<? extends T>> args = new ArrayList<>();

        for (int i = 0; i < activeThreads; i++) {
            st = en;
            en = st + butchSize + (biggerButches-- > 0 ? 1 : 0);
            args.add(list.subList(st, en).stream());
        }

        if (mapper != null) {
            return collector.apply(mapper.map(process, args).stream());
        }

        final Thread[] flows = new Thread[activeThreads];
        final List<R> res = new ArrayList<>(Collections.nCopies(activeThreads, null));

        for (int i = 0; i < activeThreads; i++) {
            final int ind = i; //in every thread it's variable
            flows[i] = new Thread(() -> res.set(ind, process.apply(args.get(ind))));
            flows[i].start();
        }
        InterruptedException[] exc = new InterruptedException[activeThreads];
        for (int i = 0; i < activeThreads; i++) {
            // :NOTE: утечка потоков
            while(true) {
                try {
                    flows[i].join();
                    break;
                } catch (InterruptedException e) {
                    if (exc[i] == null) {
                        exc[i] = e;
                    } else {
                        exc[i].addSuppressed(e);
                    }
                }
            }
        }
        for (InterruptedException e : exc) {
            if (e != null) throw e;
        }
        return collector.apply(res.stream());
    }
}

