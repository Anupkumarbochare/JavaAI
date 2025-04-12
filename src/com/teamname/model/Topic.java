package com.teamname.model;

/**
 * Model class representing a topic for the Binary Search Tree
 */
public class Topic implements Comparable<Topic> {
    private final String name;
    private final String context;
    
    /**
     * Constructor for Topic
     * @param name Topic name (e.g., "java", "algorithm")
     * @param context Optional context or description for the topic
     */
    public Topic(String name, String context) {
        this.name = name;
        this.context = context;
    }
    
    /**
     * Get the topic name
     * @return The topic name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the topic context
     * @return The topic context or description
     */
    public String getContext() {
        return context;
    }
    
    /**
     * Compare topics for Binary Search Tree ordering
     * Topics are compared by name (alphabetically)
     */
    @Override
    public int compareTo(Topic other) {
        return this.name.compareTo(other.name);
    }
    
    /**
     * Topics are considered equal if they have the same name
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Topic topic = (Topic) obj;
        return name.equals(topic.name);
    }
    
    /**
     * Hash code based on the topic name
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    /**
     * String representation of topic
     */
    @Override
    public String toString() {
        return "Topic: " + name;
    }
}