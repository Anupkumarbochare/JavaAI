package com.teamname.datastructures;

import java.util.Comparator;

/**
 * Custom Priority Queue implementation
 * Satisfies one of the "Queues, Deques, Priority Queue" requirements
 */
public class PriorityQueue<T> {
    private static final int DEFAULT_CAPACITY = 11;
    private Object[] queue;
    private int size;
    private final Comparator<? super T> comparator;

    public PriorityQueue() {
        this(DEFAULT_CAPACITY, null);
    }

    public PriorityQueue(int initialCapacity, Comparator<? super T> comparator) {
        queue = new Object[initialCapacity];
        this.comparator = comparator;
    }

    public void enqueue(T item) {
        if (size >= queue.length - 1) {
            grow();
        }
        
        int i = size;
        size++;
        if (i == 0) {
            queue[0] = item;
        } else {
            siftUp(i, item);
        }
    }

    @SuppressWarnings("unchecked")
    public T dequeueHighestPriority() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }
        
        T result = (T) queue[0];
        T x = (T) queue[--size];
        queue[size] = null;
        
        if (size != 0) {
            siftDown(0, x);
        }
        
        return result;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }
        return (T) queue[0];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    private void siftUp(int k, T x) {
        if (comparator != null) {
            siftUpUsingComparator(k, x);
        } else {
            siftUpComparable(k, x);
        }
    }

    @SuppressWarnings("unchecked")
    private void siftUpComparable(int k, T x) {
        Comparable<? super T> key = (Comparable<? super T>) x;
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = queue[parent];
            if (key.compareTo((T) e) >= 0) {
                break;
            }
            queue[k] = e;
            k = parent;
        }
        queue[k] = key;
    }

    @SuppressWarnings("unchecked")
    private void siftUpUsingComparator(int k, T x) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = queue[parent];
            if (comparator.compare(x, (T) e) >= 0) {
                break;
            }
            queue[k] = e;
            k = parent;
        }
        queue[k] = x;
    }

    @SuppressWarnings("unchecked")
    private void siftDown(int k, T x) {
        if (comparator != null) {
            siftDownUsingComparator(k, x);
        } else {
            siftDownComparable(k, x);
        }
    }

    @SuppressWarnings("unchecked")
    private void siftDownComparable(int k, T x) {
        Comparable<? super T> key = (Comparable<? super T>) x;
        int half = size >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            Object c = queue[child];
            int right = child + 1;
            if (right < size && ((Comparable<? super T>) c).compareTo((T) queue[right]) > 0) {
                c = queue[child = right];
            }
            if (key.compareTo((T) c) <= 0) {
                break;
            }
            queue[k] = c;
            k = child;
        }
        queue[k] = key;
    }

    @SuppressWarnings("unchecked")
    private void siftDownUsingComparator(int k, T x) {
        int half = size >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            Object c = queue[child];
            int right = child + 1;
            if (right < size && comparator.compare((T) c, (T) queue[right]) > 0) {
                c = queue[child = right];
            }
            if (comparator.compare(x, (T) c) <= 0) {
                break;
            }
            queue[k] = c;
            k = child;
        }
        queue[k] = x;
    }

    private void grow() {
        int oldCapacity = queue.length;
        int newCapacity = oldCapacity + ((oldCapacity < 64) ? (oldCapacity + 2) : (oldCapacity >> 1));
        Object[] newQueue = new Object[newCapacity];
        System.arraycopy(queue, 0, newQueue, 0, size);
        queue = newQueue;
    }
}