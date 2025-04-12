package com.teamname.datastructures;

/**
 * Custom HashMap implementation
 * Satisfies the "Hashing" requirement
 */
public class HashMap<K, V> {
    private static final int DEFAULT_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    
    private Node<K, V>[] table;
    private int size;
    private final float loadFactor;
    private int threshold;
    
    @SuppressWarnings("unchecked")
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        this.table = (Node<K, V>[]) new Node[DEFAULT_CAPACITY];
        this.threshold = (int) (DEFAULT_CAPACITY * loadFactor);
    }
    
    public V put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        if (size >= threshold) {
            resize();
        }
        
        int hash = hash(key);
        int index = index(hash, table.length);
        
        // If slot is empty, create new node
        if (table[index] == null) {
            table[index] = new Node<>(hash, key, value, null);
            size++;
            return null;
        }
        
        // Handle collision via chaining
        Node<K, V> current = table[index];
        Node<K, V> prev = null;
        
        while (current != null) {
            if (current.hash == hash && 
                (current.key == key || current.key.equals(key))) {
                // Key already exists, update value
                V oldValue = current.value;
                current.value = value;
                return oldValue;
            }
            prev = current;
            current = current.next;
        }
        
        // Add new node to end of chain
        prev.next = new Node<>(hash, key, value, null);
        size++;
        return null;
    }
    
    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        int hash = hash(key);
        int index = index(hash, table.length);
        
        Node<K, V> current = table[index];
        while (current != null) {
            if (current.hash == hash && 
                (current.key == key || current.key.equals(key))) {
                return current.value;
            }
            current = current.next;
        }
        
        return null;
    }
    
    public boolean containsKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        int hash = hash(key);
        int index = index(hash, table.length);
        
        Node<K, V> current = table[index];
        while (current != null) {
            if (current.hash == hash && 
                (current.key == key || current.key.equals(key))) {
                return true;
            }
            current = current.next;
        }
        
        return false;
    }
    
    public V remove(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        int hash = hash(key);
        int index = index(hash, table.length);
        
        Node<K, V> current = table[index];
        Node<K, V> prev = null;
        
        while (current != null) {
            if (current.hash == hash && 
                (current.key == key || current.key.equals(key))) {
                // Remove node
                if (prev == null) {
                    table[index] = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
                return current.value;
            }
            prev = current;
            current = current.next;
        }
        
        return null;
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    @SuppressWarnings("unchecked")
    private void resize() {
        int newCapacity = table.length * 2;
        Node<K, V>[] newTable = (Node<K, V>[]) new Node[newCapacity];
        
        // Rehash all existing entries
        for (int i = 0; i < table.length; i++) {
            Node<K, V> current = table[i];
            while (current != null) {
                Node<K, V> next = current.next;
                
                // Calculate new index based on new capacity
                int index = index(current.hash, newCapacity);
                
                // Insert at beginning of list
                current.next = newTable[index];
                newTable[index] = current;
                
                current = next;
            }
        }
        
        table = newTable;
        threshold = (int) (newCapacity * loadFactor);
    }
    
    private int hash(K key) {
        int h = key.hashCode();
        // Apply additional bit spread to avoid clustering
        return h ^ (h >>> 16);
    }
    
    private int index(int hash, int length) {
        return hash & (length - 1);
    }
    
    private static class Node<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;
        
        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
}