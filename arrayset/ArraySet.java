package info.kgeorgiy.ja.shevchenko.arrayset;

import java.util.*;

@SuppressWarnings("unused")
public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private final List<T> list;
    private final Comparator<? super T> cmp;

    private static class ReverseList<T> extends AbstractList<T> {
        private final List<T> list;

        private ReverseList(List<T> list) {
            this.list = list;
        }

        @SuppressWarnings("unused")
        private ReverseList(ReverseList<T> list) {
            this.list = list.list;
        }

        @Override
        public T get(int index) {
            return list.get(list.size() - index - 1);
        }

        @Override
        public int size() {
            return list.size();
        }
    }


    public ArraySet(Collection<? extends T> collection, Comparator<? super T> cmp) {
        this.cmp = cmp;
        Set<T> set = new TreeSet<>(cmp); //O(1)
        set.addAll(collection); //O(n)
        this.list = new ArrayList<>(set);
    }

    public ArraySet(Collection<? extends T> collection) {
        this(collection, null);
    }

    public ArraySet(Comparator<? super T> cmp) {
        this(Collections.emptyList(), cmp);
    }


    public ArraySet(List<T> list, Comparator<? super T> cmp) {
        this.list = list;
        this.cmp = cmp;
    }

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    @Override
    public T lower(T t) {
        return get(search(t, -1, -1));

    }

    @Override
    public T floor(T t) {
        return get(search(t, 0, -1));
    }

    @Override
    public T ceiling(T t) {
        return get(search(t, 0, 0));
    }

    @Override
    public T higher(T t) {
        return get(search(t, 1, 0));
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList((list)).iterator(); //TODO: почему не list.iterator()
    }

    @Override
    public NavigableSet<T> descendingSet() {//TODO:Оставить NavigableSet или ArraySet
        return new ArraySet<>(new ReverseList<>(list), Collections.reverseOrder(cmp));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        int from = search(fromElement, fromInclusive ? 0 : 1, 0);
        int to = search(toElement, toInclusive ? 0 : -1, -1);
        if (from > to) {
            throw new IllegalArgumentException("fromKey > toKey");
        }
        if (check(from) || check(to)) {
            return new ArraySet<>(cmp);
        }
        return new ArraySet<>(list.subList(from, to + 1), cmp);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        try {
            return subSet(first(), true, toElement, inclusive);
            // можем воспользоваться subset т.к второй индекс в массиве нам всё равно искать,
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return new ArraySet<>(cmp);
        }
        // тогда бы почему не поискать 0, на асимптотику не повлияет
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        try {
            return subSet(fromElement, inclusive, last(), true);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return new ArraySet<>(cmp);
        }
    }

    @Override
    public Comparator<? super T> comparator() {
        return cmp;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public T first() {
        T elem = get(0);
        if (elem == null) {
            throw new NoSuchElementException();
        }
        return elem;
    }

    @Override
    public T last() {
        T elem = get(list.size() - 1);
        if (elem == null) {
            throw new NoSuchElementException();
        }
        return elem;

    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object elem) {
        return search((T) elem) >= 0;
    }

    @Override
    public int size() {
        return list.size();
    }

    private T get(int ind) {
        if (check(ind)) {
            return null;
        }
        return list.get(ind); //O(1)
    }

    private int search(T elem) { //O(logn)
        return Collections.binarySearch(list, elem, cmp);
    }


    private int search(T elem, int successShift, int failShift) { //O(logn)
        int ind = Collections.binarySearch(list, elem, cmp);
        return (ind >= 0) ? (ind + successShift) : (-ind + failShift - 1);
    }

    private boolean check(int i) {
        return i < 0 || i >= size();
    }

}
