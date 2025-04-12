package com.teamname.datastructures;

/**
 * Binary Search Tree implementation
 * Satisfies the "Binary Search Tree" requirement
 */
public class BinarySearchTree<T extends Comparable<T>> {
    private Node<T> root;
    private int size;
    
    public BinarySearchTree() {
        root = null;
        size = 0;
    }
    
    public void insert(T data) {
        root = insertRec(root, data);
        size++;
    }
    
    private Node<T> insertRec(Node<T> root, T data) {
        // If tree is empty, create new node
        if (root == null) {
            return new Node<>(data);
        }
        
        // Recursively insert data
        if (data.compareTo(root.data) < 0) {
            root.left = insertRec(root.left, data);
        } else if (data.compareTo(root.data) > 0) {
            root.right = insertRec(root.right, data);
        }
        
        return root;
    }
    
    public boolean search(T data) {
        return searchRec(root, data);
    }
    
    private boolean searchRec(Node<T> root, T data) {
        if (root == null) {
            return false;
        }
        
        if (data.compareTo(root.data) == 0) {
            return true;
        }
        
        if (data.compareTo(root.data) < 0) {
            return searchRec(root.left, data);
        } else {
            return searchRec(root.right, data);
        }
    }
    
    public void delete(T data) {
        root = deleteRec(root, data);
    }
    
    private Node<T> deleteRec(Node<T> root, T data) {
        if (root == null) {
            return null;
        }
        
        // Find the node to delete
        if (data.compareTo(root.data) < 0) {
            root.left = deleteRec(root.left, data);
        } else if (data.compareTo(root.data) > 0) {
            root.right = deleteRec(root.right, data);
        } else {
            // Node found, now delete it
            
            // Case 1: Leaf node
            if (root.left == null && root.right == null) {
                size--;
                return null;
            }
            
            // Case 2: Node with only one child
            if (root.left == null) {
                size--;
                return root.right;
            } else if (root.right == null) {
                size--;
                return root.left;
            }
            
            // Case 3: Node with two children
            // Find the inorder successor (smallest in the right subtree)
            root.data = minValue(root.right);
            
            // Delete the inorder successor
            root.right = deleteRec(root.right, root.data);
        }
        
        return root;
    }
    
    private T minValue(Node<T> root) {
        T minValue = root.data;
        while (root.left != null) {
            minValue = root.left.data;
            root = root.left;
        }
        return minValue;
    }
    
    public void inorderTraversal() {
        inorderRec(root);
        System.out.println();
    }
    
    private void inorderRec(Node<T> root) {
        if (root != null) {
            inorderRec(root.left);
            System.out.print(root.data + " ");
            inorderRec(root.right);
        }
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    private static class Node<T> {
        T data;
        Node<T> left, right;
        
        Node(T data) {
            this.data = data;
            left = right = null;
        }
    }
}