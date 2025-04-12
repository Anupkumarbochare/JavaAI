package com.teamname.model;

import java.util.Date;

/**
 * Model class representing an entry in the conversation history
 */
public class ConversationEntry {
    public enum Speaker {
        USER, AI
    }
    
    private final Speaker speaker;
    private final String message;
    private final Date timestamp;
    private final int priority;  // Only applicable for user messages
    
    /**
     * Constructor for ConversationEntry
     * @param speaker Who sent the message (USER or AI)
     * @param message The message content
     * @param timestamp When the message was sent
     * @param priority Priority level (for user messages)
     */
    public ConversationEntry(Speaker speaker, String message, Date timestamp, int priority) {
        this.speaker = speaker;
        this.message = message;
        this.timestamp = timestamp;
        this.priority = priority;
    }
    
    /**
     * Get the speaker
     * @return Who sent the message
     */
    public Speaker getSpeaker() {
        return speaker;
    }
    
    /**
     * Get the message
     * @return The message content
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Get the timestamp
     * @return When the message was sent
     */
    public Date getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get the priority
     * @return Priority level (only applicable for user messages)
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * String representation of the conversation entry
     */
    @Override
    public String toString() {
        String priorityStr = "";
        if (speaker == Speaker.USER && priority > 0) {
            switch (priority) {
                case 1: priorityStr = " [High Priority]"; break;
                case 2: priorityStr = " [Medium Priority]"; break;
                case 3: priorityStr = " [Low Priority]"; break;
            }
        }
        
        return (speaker == Speaker.USER ? "User: " : "AI: ") + message + priorityStr;
    }
}