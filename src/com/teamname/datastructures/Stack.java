package com.teamname.datastructures;

/**
 * Custom generic Stack implementation using an array
 * Satisfies the "Stacks" requirement from the first list
 */
public class Stack<T> {
    private Object[] elements;
    private int size;
    private static final int DEFAULT_CAPACITY = 10;
    
    public Stack() {
        elements = new Object[DEFAULT_CAPACITY];
        size = 0;
    }
    
    /**
     * Pushes an item onto the top of this stack
     * @param item the item to be pushed onto this stack
     */
    public void push(T item) {
        ensureCapacity();
        elements[size++] = item;
    }
    
    /**
     * Removes the object at the top of this stack and returns it
     * @return the object at the top of this stack
     * @throws EmptyStackException if this stack is empty
     */
    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException("Stack is empty");
        }
        T item = (T) elements[--size];
        elements[size] = null; // Help garbage collection
        return item;
    }
    
    /**
     * Looks at the object at the top of this stack without removing it
     * @return the object at the top of this stack
     * @throws EmptyStackException if this stack is empty
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException("Stack is empty");
        }
        return (T) elements[size - 1];
    }
    
    /**
     * Tests if this stack is empty
     * @return true if and only if this stack contains no items; false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Returns the number of items in this stack
     * @return the number of items in this stack
     */
    public int size() {
        return size;
    }
    
    /**
     * Ensures capacity for adding more elements
     */
    private void ensureCapacity() {
        if (size == elements.length) {
            Object[] newElements = new Object[elements.length * 2];
            System.arraycopy(elements, 0, newElements, 0, size);
            elements = newElements;
        }
    }
    
    /**
     * Custom EmptyStackException
     */
    public static class EmptyStackException extends RuntimeException {
        public EmptyStackException(String message) {
            super(message);
        }
    }
}