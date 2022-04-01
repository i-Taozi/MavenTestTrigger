/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.internal;

import java.util.*;

/**
 * 
 */
public final class IndexedMap<VALUE> implements Iterable<Map.Entry<Long, VALUE>> {
    private static final long MAX_LENGTH = 0x1F_FFFF_FFFF_FFFFL;
    private static final long MAX_DENSE_LENGTH = 0x7FFF_FFFF >> 4;
    private static final int MIN_SPARSE_LENGTH = 32;
    private static final int SPARSE_DENSE_RATIO = 8;
    private static final Elements<?> EMPTY_ELEMENTS = new EmptyElements<>();

    @SuppressWarnings("unchecked")
    private static <VALUE> Elements<VALUE> emptyElements() {
        return (Elements<VALUE>) EMPTY_ELEMENTS;
    }

    private long length;
    private Elements<VALUE> elements;

    public IndexedMap() {
        this.length = 0;
        this.elements = emptyElements();
    }

    public IndexedMap(long length) {
        this.length = length;
        this.elements = emptyElements();
    }

    public IndexedMap(long length, int capacity) {
        this.length = length;
        this.elements = new DenseElements<>(capacity);
    }

    private static abstract class Elements<VALUE> {
        /**
         * Returns the maximum property key index + 1.
         * 
         * @return the maximum property key index + 1
         */
        abstract long length();

        /**
         * Returns the underlying data structure's size or {@code 0} if unbound.
         * 
         * @return the data structure's size or {@code 0}
         */
        abstract int capacity();

        /**
         * Returns the total count of used entries in the underlying data structure.
         * 
         * @return total count of used entries
         */
        abstract int count();

        /**
         * Returns {@code true} if the property key was found.
         * 
         * @param propertyKey
         *            the property key
         * @return {@code true} if property key was found
         */
        abstract boolean has(long propertyKey);

        /**
         * Returns the mapped value or {@code null} if not found.
         * 
         * @param propertyKey
         *            the property key
         * @return the mapped value or {@code null} if not found
         */
        abstract VALUE get(long propertyKey);

        /**
         * Sets the property key to the new value.
         * 
         * @param propertyKey
         *            the property key
         * @param value
         *            the new property value
         */
        abstract void put(long propertyKey, VALUE value);

        /**
         * Deletes the property key.
         * 
         * @param propertyKey
         *            the property key
         */
        abstract void delete(long propertyKey);

        /**
         * Returns a dense representation for this object.
         * 
         * @return dense representation.
         */
        abstract Elements<VALUE> toDense();

        /**
         * Returns a sparse representation for this object.
         * 
         * @return sparse representation.
         */
        abstract Elements<VALUE> toSparse();

        /**
         * Returns a sparse representation for this object.
         * 
         * @return sparse representation.
         */
        abstract Elements<VALUE> toSparseOrShrink();

        /**
         * Returns the indexed keys as strings.
         * 
         * @return the indexed keys as strings
         */
        abstract List<String> keys();

        /**
         * Returns the indexed keys as strings.
         * 
         * @param from
         *            from index (inclusive)
         * @param to
         *            to index (exclusive)
         * @return the indexed keys as strings
         */
        abstract List<String> keys(long from, long to);

        /**
         * Returns the indexed keys.
         * 
         * @return the indexed keys
         */
        abstract long[] indices();

        /**
         * Returns the indexed keys.
         * 
         * @param from
         *            from index (inclusive)
         * @param to
         *            to index (exclusive)
         * @return the indexed keys
         */
        abstract long[] indices(long from, long to);

        @SuppressWarnings("unchecked")
        static <T> T[] newArray(int length) {
            return (T[]) new Object[length];
        }

        /**
         * Returns an ascending iterator over the complete range.
         * 
         * @return the range iterator
         */
        abstract Iterator<Map.Entry<Long, VALUE>> iterator();

        /**
         * Returns an ascending iterator over the complete range.
         * 
         * @return the range iterator
         */
        abstract Iterator<Long> keysIterator();

        /**
         * Returns an ascending iterator over the complete range.
         * 
         * @return the range iterator
         */
        abstract Iterator<VALUE> valuesIterator();

        /**
         * Returns an ascending iterator over the requested range.
         * 
         * @param from
         *            from index (inclusive)
         * @param to
         *            to index (exclusive)
         * @return the range iterator
         */
        abstract Iterator<Map.Entry<Long, VALUE>> ascendingIterator(long from, long to);

        /**
         * Returns an descending iterator over the requested range.
         * 
         * @param from
         *            from index (inclusive)
         * @param to
         *            to index (exclusive)
         * @return the range iterator
         */
        abstract Iterator<Map.Entry<Long, VALUE>> descendingIterator(long from, long to);
    }

    private static final class EmptyElements<VALUE> extends Elements<VALUE> {
        @Override
        long length() {
            return 0;
        }

        @Override
        int capacity() {
            return 0;
        }

        @Override
        int count() {
            return 0;
        }

        @Override
        boolean has(long propertyKey) {
            return false;
        }

        @Override
        VALUE get(long propertyKey) {
            return null;
        }

        @Override
        void put(long propertyKey, VALUE value) {
            throw new AssertionError();
        }

        @Override
        void delete(long propertyKey) {
        }

        @Override
        Elements<VALUE> toDense() {
            return new DenseElements<>();
        }

        @Override
        Elements<VALUE> toSparse() {
            return new SparseElements<>();
        }

        @Override
        Elements<VALUE> toSparseOrShrink() {
            return new SparseElements<>();
        }

        @Override
        List<String> keys() {
            return Collections.emptyList();
        }

        @Override
        List<String> keys(long from, long to) {
            return Collections.emptyList();
        }

        @Override
        long[] indices() {
            return new long[0];
        }

        @Override
        long[] indices(long from, long to) {
            return new long[0];
        }

        @Override
        Iterator<Long> keysIterator() {
            return Collections.emptyIterator();
        }

        @Override
        Iterator<VALUE> valuesIterator() {
            return Collections.emptyIterator();
        }

        @Override
        Iterator<Map.Entry<Long, VALUE>> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        Iterator<Map.Entry<Long, VALUE>> ascendingIterator(long from, long to) {
            return Collections.emptyIterator();
        }

        @Override
        Iterator<Map.Entry<Long, VALUE>> descendingIterator(long from, long to) {
            return Collections.emptyIterator();
        }

        @Override
        public String toString() {
            return "Empty=[]";
        }
    }

    private static final class DenseElements<VALUE> extends Elements<VALUE> {
        // Try to compact if 25% holes and capacity exceeds 32 elements
        private static final int COMPACT_THRESHOLD = 32;
        private static final int COMPACT_RATIO = 4;
        private static final int MIN_CAPACITY = 8;
        private static final Object[] EMPTY_ARRAY = {};

        private VALUE[] array;
        private int count;

        DenseElements(VALUE[] array, int count) {
            this.array = array;
            this.count = count;
        }

        @SuppressWarnings("unchecked")
        DenseElements() {
            this((VALUE[]) EMPTY_ARRAY, 0);
        }

        DenseElements(int capacity) {
            this(Elements.<VALUE> newArray(nextCapacity(capacity)), 0);
        }

        DenseElements(List<VALUE> c, int count) {
            this(c.toArray(Elements.<VALUE> newArray(nextCapacity(c.size()))), count);
        }

        @Override
        Elements<VALUE> toDense() {
            return this;
        }

        @Override
        Elements<VALUE> toSparse() {
            VALUE[] arrayNoHoles = removeTrailingHoles(array);
            return new SparseElements<>(new StubSortedMap<>(arrayNoHoles, count));
        }

        @Override
        Elements<VALUE> toSparseOrShrink() {
            VALUE[] arrayNoHoles = removeTrailingHoles(array);
            if (arrayNoHoles != array) {
                // shrunk array applicable for dense representation?
                int capacity = arrayNoHoles.length;
                if (!(capacity > MIN_SPARSE_LENGTH && count * SPARSE_DENSE_RATIO < capacity)) {
                    this.array = arrayNoHoles;
                    return this;
                }
            }
            return new SparseElements<>(new StubSortedMap<>(arrayNoHoles, count));
        }

        @Override
        long length() {
            return getLastNonHoleIndex(array) + 1;
        }

        @Override
        int capacity() {
            return array.length;
        }

        @Override
        int count() {
            return count;
        }

        @Override
        boolean has(long propertyKey) {
            assert (int) propertyKey == propertyKey : "invalid dense index: " + propertyKey;
            int index = (int) propertyKey;
            VALUE[] array = this.array;
            return index < array.length && array[index] != null;
        }

        @Override
        VALUE get(long propertyKey) {
            assert (int) propertyKey == propertyKey : "invalid dense index: " + propertyKey;
            int index = (int) propertyKey;
            VALUE[] array = this.array;
            return index < array.length ? array[index] : null;
        }

        @Override
        void put(long propertyKey, VALUE value) {
            assert (int) propertyKey == propertyKey : "invalid dense index: " + propertyKey;
            int index = (int) propertyKey;
            VALUE[] array = this.array;
            if (index < array.length) {
                VALUE prev = array[index];
                array[index] = value;
                if (prev == null) {
                    count += 1;
                }
            } else {
                add(index, value);
                count += 1;
            }
        }

        @Override
        void delete(long propertyKey) {
            assert (int) propertyKey == propertyKey : "invalid dense index: " + propertyKey;
            int index = (int) propertyKey;
            VALUE[] array = this.array;
            if (index < array.length && array[index] != null) {
                array[index] = null;
                count -= 1;
            }
        }

        @Override
        List<String> keys() {
            ArrayList<String> keys = new ArrayList<>(count());
            Iterator<String> iter = new DenseStringKeyIterator<>(0, array.length, array);
            while (iter.hasNext()) {
                keys.add(iter.next());
            }
            return keys;
        }

        @Override
        List<String> keys(long from, long to) {
            assert from <= to;
            int fromIndex = (int) Math.min(from, array.length);
            int toIndex = (int) Math.min(to, array.length);
            int size = Math.min(count(), toIndex - fromIndex);
            ArrayList<String> keys = new ArrayList<>(size);
            Iterator<String> iter = new DenseStringKeyIterator<>(fromIndex, toIndex, array);
            while (iter.hasNext()) {
                keys.add(iter.next());
            }
            return keys;
        }

        @Override
        long[] indices() {
            VALUE[] array = this.array;
            long[] indices = new long[count()];
            for (int i = 0, j = 0, len = array.length; i < len; ++i) {
                if (array[i] != null) {
                    indices[j++] = i;
                }
            }
            return indices;
        }

        @Override
        long[] indices(long from, long to) {
            VALUE[] array = this.array;
            if (from >= array.length || from >= to) {
                return new long[0];
            }
            int fromIndex = (int) Math.min(from, array.length);
            int toIndex = (int) Math.min(to, array.length);
            int range = (toIndex - fromIndex);
            int j = 0;
            long[] indices = new long[Math.min(range, count())];
            for (int i = fromIndex; i < toIndex; ++i) {
                if (array[i] != null) {
                    indices[j++] = i;
                }
            }
            if (j != indices.length) {
                indices = Arrays.copyOf(indices, j);
            }
            return indices;
        }

        @Override
        Iterator<Map.Entry<Long, VALUE>> iterator() {
            return new DenseEntryIterator<>(0, array.length, array);
        }

        @Override
        Iterator<Long> keysIterator() {
            return new DenseKeyIterator<>(0, array.length, array);
        }

        @Override
        Iterator<VALUE> valuesIterator() {
            return new DenseValueIterator<>(0, array.length, array);
        }

        @Override
        Iterator<Map.Entry<Long, VALUE>> ascendingIterator(long from, long to) {
            if (from >= array.length || from >= to) {
                return Collections.emptyIterator();
            }
            int fromIndex = (int) Math.min(from, array.length);
            int toIndex = (int) Math.min(to, array.length);
            return new DenseEntryIterator<>(fromIndex, toIndex, array);
        }

        @Override
        Iterator<Map.Entry<Long, VALUE>> descendingIterator(long from, long to) {
            if (from >= array.length || from >= to) {
                return Collections.emptyIterator();
            }
            int endIndex = (int) Math.min(from, array.length) - 1;
            int startIndex = Math.max(0, (int) Math.min(to, array.length) - 1);
            return new DenseEntryIterator<>(startIndex, endIndex, array);
        }

        @Override
        public String toString() {
            return "Dense=" + Arrays.toString(indices());
        }

        private void add(int index, VALUE value) {
            VALUE[] array = this.array;
            int newLength = nextCapacity(index);
            VALUE[] newArray = newArray(newLength);
            System.arraycopy(array, 0, newArray, 0, array.length);
            newArray[index] = value;
            this.array = newArray;
        }

        private static int nextCapacity(int v) {
            // TODO: better capacity allocation
            return Math.max(Integer.highestOneBit(v) << 1, MIN_CAPACITY);
        }

        private static <VALUE> VALUE[] removeTrailingHoles(VALUE[] array) {
            int length = array.length;
            if (length > COMPACT_THRESHOLD) {
                // TODO: probably need to save a few holes as space
                int trailingHoles = countTrailingHoles(array);
                if (trailingHoles * COMPACT_RATIO > length) {
                    int newLength = array.length - trailingHoles;
                    VALUE[] newArray = newArray(newLength);
                    System.arraycopy(array, 0, newArray, 0, newLength);
                    return newArray;
                }
            }
            return array;
        }

        private static <VALUE> int getLastNonHoleIndex(VALUE[] array) {
            for (int i = array.length - 1; i >= 0; --i) {
                if (array[i] != null) {
                    return i;
                }
            }
            return -1;
        }

        private static <VALUE> int countTrailingHoles(VALUE[] array) {
            int trailingHoles = 0;
            for (int i = array.length - 1; i >= 0; --i) {
                if (array[i] == null) {
                    trailingHoles += 1;
                } else {
                    break;
                }
            }
            return trailingHoles;
        }
    }

    private static final class SparseElements<VALUE> extends Elements<VALUE> {
        private final TreeMap<Long, VALUE> map;

        SparseElements() {
            map = new TreeMap<>();
        }

        SparseElements(SortedMap<Long, VALUE> sortedMap) {
            map = new TreeMap<>(sortedMap);
        }

        private VALUE[] toArray() {
            long length = length();
            assert 0 <= length && length <= Integer.MAX_VALUE;
            VALUE[] values = newArray((int) length);
            for (Map.Entry<Long, VALUE> e : map.entrySet()) {
                values[e.getKey().intValue()] = e.getValue();
            }
            return values;
        }

        @Override
        Elements<VALUE> toDense() {
            assert count() > 0;
            return new DenseElements<>(Arrays.asList(toArray()), count());
        }

        @Override
        Elements<VALUE> toSparse() {
            return this;
        }

        @Override
        Elements<VALUE> toSparseOrShrink() {
            return this;
        }

        @Override
        long length() {
            return map.isEmpty() ? 0 : map.lastKey() + 1;
        }

        @Override
        int capacity() {
            return 0;
        }

        @Override
        int count() {
            return map.size();
        }

        @Override
        boolean has(long propertyKey) {
            return map.containsKey(propertyKey);
        }

        @Override
        VALUE get(long propertyKey) {
            return map.get(propertyKey);
        }

        @Override
        void put(long propertyKey, VALUE value) {
            map.put(propertyKey, value);
        }

        @Override
        void delete(long propertyKey) {
            map.remove(propertyKey);
        }

        @Override
        List<String> keys() {
            ArrayList<String> keys = new ArrayList<>(count());
            for (Long k : map.keySet()) {
                keys.add(k.toString());
            }
            return keys;
        }

        @Override
        List<String> keys(long from, long to) {
            assert from <= to;
            int size = (int) Math.min(count(), to - from);
            ArrayList<String> keys = new ArrayList<>(size);
            for (Long k : map.subMap(from, true, to, false).keySet()) {
                keys.add(k.toString());
            }
            return keys;
        }

        @Override
        long[] indices() {
            long[] indices = new long[count()];
            int j = 0;
            for (Long k : map.keySet()) {
                indices[j++] = k;
            }
            return indices;
        }

        @Override
        long[] indices(long from, long to) {
            long length = length();
            if (from >= length || from >= to) {
                return new long[0];
            }
            long fromIndex = Math.min(from, length);
            long toIndex = Math.min(to, length);
            // Skip subMap() if whole range is requested.
            if (fromIndex == 0 && toIndex == length) {
                return indices();
            }
            long range = (toIndex - fromIndex);
            int j = 0;
            long[] indices = new long[(int) Math.min(range, count())];
            for (Long k : map.subMap(from, true, to, false).keySet()) {
                indices[j++] = k;
            }
            if (j != indices.length) {
                indices = Arrays.copyOf(indices, j);
            }
            return indices;
        }

        @Override
        Iterator<Map.Entry<Long, VALUE>> iterator() {
            return map.entrySet().iterator();
        }

        @Override
        Iterator<Long> keysIterator() {
            return map.keySet().iterator();
        }

        @Override
        Iterator<VALUE> valuesIterator() {
            return map.values().iterator();
        }

        @Override
        Iterator<Map.Entry<Long, VALUE>> ascendingIterator(long from, long to) {
            return map.subMap(from, true, to, false).entrySet().iterator();
        }

        @Override
        Iterator<Map.Entry<Long, VALUE>> descendingIterator(long from, long to) {
            return map.descendingMap().subMap(to, false, from, true).entrySet().iterator();
        }

        @Override
        public String toString() {
            return "Sparse=" + Arrays.toString(indices());
        }
    }

    private static abstract class DenseIterator<V, T> implements Iterator<T> {
        private final V[] values;
        private final int endIndex;
        private final int step;
        private int index;

        DenseIterator(int startIndex, int endIndex, V[] values) {
            assert 0 <= startIndex && startIndex <= values.length;
            assert -1 <= endIndex && endIndex <= values.length;
            this.index = startIndex;
            this.step = startIndex <= endIndex ? 1 : -1;
            this.endIndex = endIndex;
            this.values = values;
        }

        private final int getNextIndex() {
            V[] values = this.values;
            for (int i = index; i != endIndex; i += step) {
                if (values[i] != null) {
                    return i;
                }
            }
            return -1;
        }

        protected final V value(int index) {
            return values[index];
        }

        protected abstract T nextValue(int index);

        @Override
        public final T next() {
            int nextIndex = getNextIndex();
            if (nextIndex < 0) {
                throw new NoSuchElementException();
            }
            index = nextIndex + step;
            return nextValue(nextIndex);
        }

        @Override
        public final boolean hasNext() {
            return getNextIndex() >= 0;
        }

        @Override
        public final void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class DenseKeyIterator<V> extends DenseIterator<V, Long> {
        DenseKeyIterator(int startIndex, int endIndex, V[] values) {
            super(startIndex, endIndex, values);
        }

        @Override
        protected Long nextValue(int index) {
            return (long) index;
        }
    }

    private static final class DenseValueIterator<V> extends DenseIterator<V, V> {
        DenseValueIterator(int startIndex, int endIndex, V[] values) {
            super(startIndex, endIndex, values);
        }

        @Override
        protected V nextValue(int index) {
            return value(index);
        }
    }

    private static final class DenseStringKeyIterator<V> extends DenseIterator<V, String> {
        DenseStringKeyIterator(int startIndex, int endIndex, V[] values) {
            super(startIndex, endIndex, values);
        }

        @Override
        protected String nextValue(int index) {
            return Integer.toString(index);
        }
    }

    private static final class DenseEntryIterator<V> extends DenseIterator<V, Map.Entry<Long, V>> {
        DenseEntryIterator(int startIndex, int endIndex, V[] values) {
            super(startIndex, endIndex, values);
        }

        @Override
        protected Map.Entry<Long, V> nextValue(int index) {
            return new AbstractMap.SimpleImmutableEntry<>((long) index, value(index));
        }
    }

    private static final class StubSortedMap<V> extends AbstractMap<Long, V> implements SortedMap<Long, V> {
        private final V[] values;
        private final int size;

        StubSortedMap(V[] values, int size) {
            this.values = values;
            this.size = size;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public Set<Map.Entry<Long, V>> entrySet() {
            return new AbstractSet<Map.Entry<Long, V>>() {
                @Override
                public Iterator<java.util.Map.Entry<Long, V>> iterator() {
                    return new DenseEntryIterator<>(0, values.length, values);
                }

                @Override
                public int size() {
                    return StubSortedMap.this.size();
                }
            };
        }

        @Override
        public Comparator<? super Long> comparator() {
            return null; // natural order
        }

        @Override
        public SortedMap<Long, V> subMap(Long fromKey, Long toKey) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SortedMap<Long, V> headMap(Long toKey) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SortedMap<Long, V> tailMap(Long fromKey) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long firstKey() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long lastKey() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns {@code true} if the property key is a valid index.
     * 
     * @param propertyKey
     *            the property key
     * @return {@code true} if the property key is a valid index
     */
    public static boolean isIndex(long propertyKey) {
        return 0 <= propertyKey && propertyKey < MAX_LENGTH;
    }

    /**
     * Returns {@code true} if the property key is a valid index.
     * 
     * @param propertyKey
     *            the property key
     * @return {@code true} if the property key is a valid index
     */
    public static boolean isIndex(String propertyKey) {
        if (propertyKey.isEmpty() || propertyKey.charAt(0) < '0' || propertyKey.charAt(0) > '9') {
            return false;
        }
        return isIndex(Strings.toIndex(propertyKey));
    }

    /**
     * If {@code s} is an integer indexed property key less than {@code 2}<span><sup>{@code 53} </sup></span>{@code -1},
     * its integer value is returned. Otherwise {@code -1} is returned.
     * 
     * @param propertyKey
     *            the property key
     * @return the integer index or {@code -1}
     */
    public static long toIndex(String propertyKey) {
        return Strings.toIndex(propertyKey);
    }

    /**
     * Returns the length.
     * 
     * @return the length
     */
    public long getLength() {
        return length;
    }

    /**
     * Returns {@code true} if the property key was found.
     * 
     * @param propertyKey
     *            the property key
     * @return {@code true} if property key was found
     */
    public boolean containsKey(long propertyKey) {
        assert isIndex(propertyKey) : "invalid index: " + propertyKey;
        if (0 <= propertyKey && propertyKey < length) {
            return elements.has(propertyKey);
        }
        return false;
    }

    /**
     * Returns the mapped value or {@code null} if not found.
     * 
     * @param propertyKey
     *            the property key
     * @return the mapped value or {@code null} if not found
     */
    public VALUE get(long propertyKey) {
        assert isIndex(propertyKey) : "invalid index: " + propertyKey;
        if (0 <= propertyKey && propertyKey < length) {
            return elements.get(propertyKey);
        }
        return null;
    }

    /**
     * Sets the property key to the new value.
     * 
     * @param propertyKey
     *            the property key
     * @param value
     *            the new property value
     */
    public void put(long propertyKey, VALUE value) {
        assert isIndex(propertyKey) : "invalid index: " + propertyKey;
        if (propertyKey <= MAX_DENSE_LENGTH) {
            smallPut((int) propertyKey, value);
        } else {
            largePut(propertyKey, value);
        }
    }

    private static <VALUE> Elements<VALUE> createElements(int propertyKey) {
        if (propertyKey < MIN_SPARSE_LENGTH) {
            return new DenseElements<>(propertyKey);
        } else {
            return new SparseElements<>();
        }
    }

    private void smallPut(int propertyKey, VALUE value) {
        assert propertyKey <= (int) MAX_DENSE_LENGTH : "index too large: " + propertyKey;

        Elements<VALUE> elements = this.elements;
        if (elements == EMPTY_ELEMENTS) {
            this.elements = elements = createElements(propertyKey);
        }
        if (propertyKey < length) {
            // Switch to dense representation?
            int count = elements.count(), capacity = elements.capacity();
            if (capacity == 0 && count * 2 > length) {
                this.elements = elements = elements.toDense();
            }
        } else {
            // Switch to sparse representation?
            int newLength = propertyKey + 1;
            int count = elements.count(), capacity = newLength;
            if (capacity > MIN_SPARSE_LENGTH && count * SPARSE_DENSE_RATIO < capacity) {
                this.elements = elements = elements.toSparseOrShrink();
            }
            length = Math.max(length, newLength);
        }
        elements.put(propertyKey, value);
    }

    private void largePut(long propertyKey, VALUE value) {
        assert propertyKey > MAX_DENSE_LENGTH : "index too small: " + propertyKey;

        // Require sparse representation
        Elements<VALUE> elements = this.elements = this.elements.toSparse();
        elements.put(propertyKey, value);
        length = Math.max(length, propertyKey + 1);
    }

    /**
     * Deletes the property key.
     * 
     * @param propertyKey
     *            the property key
     */
    public void remove(long propertyKey) {
        assert isIndex(propertyKey) : "invalid index: " + propertyKey;
        if (0 <= propertyKey && propertyKey < length) {
            elements.delete(propertyKey);
            updateLength();
        }
    }

    /**
     * Deletes the property key. Does not perform any representation clean-up.
     * 
     * @param propertyKey
     *            the property key
     * @see #updateLength()
     */
    public void removeUnchecked(long propertyKey) {
        assert isIndex(propertyKey) : "invalid index: " + propertyKey;
        if (0 <= propertyKey && propertyKey < length) {
            elements.delete(propertyKey);
        }
    }

    /**
     * Updates the length and adjusts the internal representation.
     */
    public void updateLength() {
        // Switch to sparse representation?
        Elements<VALUE> elements = this.elements;
        int count = elements.count(), capacity = elements.capacity();
        if (capacity > MIN_SPARSE_LENGTH && count * SPARSE_DENSE_RATIO < capacity) {
            this.elements = elements = elements.toSparseOrShrink();
        } else {
            long length = elements.length();
            if (capacity == 0 && count * 2 > length) {
                this.elements = elements = elements.toDense();
            }
        }
        // TODO: add explicit length logic
        this.length = elements.length();
    }

    /**
     * Returns {@code true} if the map uses the sparse elements representation.
     * 
     * @return {@code true} if the map is sparse
     */
    public boolean isSparse() {
        return elements instanceof SparseElements;
    }

    /**
     * Returns {@code true} if the map has holes.
     * 
     * @return {@code true} if the map has holes
     */
    public boolean hasHoles() {
        return length != elements.count();
    }

    /**
     * Returns {@code true} if the map is empty.
     * 
     * @return {@code true} if the map is empty
     */
    public boolean isEmpty() {
        return elements.count() == 0;
    }

    /**
     * Returns the number of mappings in this map.
     * 
     * @return the number of mappings
     */
    public int size() {
        return elements.count();
    }

    /**
     * Returns the indexed keys as strings.
     * 
     * @return the indexed keys as strings
     */
    public List<String> keys() {
        return elements.keys();
    }

    /**
     * Returns the indexed keys as strings.
     * 
     * @param from
     *            from index (inclusive)
     * @param to
     *            to index (exclusive)
     * @return the indexed keys as strings
     */
    public List<String> keys(long from, long to) {
        if (from < 0 || to < 0 || from > to) {
            throw new IndexOutOfBoundsException();
        }
        return elements.keys(from, to);
    }

    /**
     * Returns the indexed keys.
     * 
     * @return the indexed keys
     */
    public long[] indices() {
        return elements.indices();
    }

    /**
     * Returns the indexed keys over the requested range.
     * 
     * @param from
     *            from index (inclusive)
     * @param to
     *            to index (exclusive)
     * @return the indexed keys
     */
    public long[] indices(long from, long to) {
        return elements.indices(from, to);
    }

    /**
     * Returns an ascending iterator over the complete range.
     * 
     * @return the range iterator
     */
    @Override
    public Iterator<Map.Entry<Long, VALUE>> iterator() {
        return elements.iterator();
    }

    /**
     * Returns an ascending iterator over the complete range.
     * 
     * @return the range iterator
     */
    public Iterator<Long> keysIterator() {
        return elements.keysIterator();
    }

    /**
     * Returns an ascending iterator over the complete range.
     * 
     * @return the range iterator
     */
    public Iterator<VALUE> valuesIterator() {
        return elements.valuesIterator();
    }

    /**
     * Returns an ascending iterator over the requested range.
     * 
     * @param from
     *            from index (inclusive)
     * @param to
     *            to index (exclusive)
     * @return the range iterator
     */
    public Iterator<Map.Entry<Long, VALUE>> ascendingIterator(long from, long to) {
        if (from < 0 || to < 0 || from > to) {
            throw new IndexOutOfBoundsException();
        }
        return elements.ascendingIterator(from, to);
    }

    /**
     * Returns a descending iterator over the requested range.
     * 
     * @param from
     *            from index (inclusive)
     * @param to
     *            to index (exclusive)
     * @return the range iterator
     */
    public Iterator<Map.Entry<Long, VALUE>> descendingIterator(long from, long to) {
        if (from < 0 || to < 0 || from > to) {
            throw new IndexOutOfBoundsException();
        }
        return elements.descendingIterator(from, to);
    }

    @Override
    public String toString() {
        return String.format("{length=%d, elements=%s}", length, elements);
    }
}
