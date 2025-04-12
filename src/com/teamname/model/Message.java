package com.teamname.model;

import java.util.Date;

/**
 * Model class representing a message with priority information
 */
public class Message implements Comparable<Message> {
    private final String text;
    private final Date timestamp;
    private final int priority; // Lower number = higher priority
    
    /**
     * Constructor for Message
     * @param text The message content
     * @param timestamp When the message was created
     * @param priority Priority level (1=High, 2=Medium, 3=Low)
     */
    public Message(String text, Date timestamp, int priority) {
        this.text = text;
        this.timestamp = timestamp;
        this.priority = priority;
    }
    
    /**
     * Get the message text
     * @return The message content
     */
    public String getText() {
        return text;
    }
    
    /**
     * Get the message timestamp
     * @return When the message was created
     */
    public Date getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get the message priority
     * @return Priority value (lower = higher priority)
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * Compare messages for priority queue ordering
     * Messages are compared first by priority, then by timestamp
     */
    @Override
    public int compareTo(Message other) {
        // First compare by priority (lower number = higher priority)
        int priorityCompare = Integer.compare(this.priority, other.priority);
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        // If same priority, compare by timestamp (older first)
        return this.timestamp.compareTo(other.timestamp);
    }
    
    /**
     * String representation of message
     */
    @Override
    public String toString() {
        String priorityStr;
        switch (priority) {
            case 1: priorityStr = "High"; break;
            case 3: priorityStr = "Low"; break;
            default: priorityStr = "Medium"; break;
        }
        return text + " [" + priorityStr + " Priority]";
    }
}