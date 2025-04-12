package com.teamname.model;

import com.teamname.datastructures.Stack;
import com.teamname.service.DatabaseService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Manages multiple conversations and persistent storage.
 */
public class ConversationManager {
    private List<Conversation> conversations;
    private int currentConversationIndex;
    private final DatabaseService db;

    public ConversationManager() {
        conversations = new ArrayList<>();
        db = new DatabaseService();

        // Load from DB on startup
        List<String> savedTitles = db.getAllConversationTitles();
        if (!savedTitles.isEmpty()) {
            for (String title : savedTitles) {
                List<ConversationEntry> entries = db.loadConversation(title);
                if (!entries.isEmpty()) {
                    Conversation conv = new Conversation(title, entries.get(0).getTimestamp());
                    for (ConversationEntry entry : entries) {
                        conv.addEntry(entry);
                    }
                    conversations.add(conv);
                }
            }
            currentConversationIndex = 0;
        } else {
            conversations.add(new Conversation("New Chat", new Date()));
            currentConversationIndex = 0;
        }
    }

    public Conversation getCurrentConversation() {
        return conversations.get(currentConversationIndex);
    }

    public Conversation createNewConversation(String baseTitle) {
        int count = 0;
        String generatedTitle = baseTitle;

        boolean titleExists = true;
        while (titleExists) {
            String finalTitle = generatedTitle; // Effectively final for lambda
            titleExists = conversations.stream().anyMatch(c -> c.getTitle().equals(finalTitle));

            if (titleExists) {
                count++;
                generatedTitle = baseTitle + " " + count;
            }
        }

        Conversation newConversation = new Conversation(generatedTitle, new Date());
        conversations.add(newConversation);
        currentConversationIndex = conversations.size() - 1;
        db.saveConversation(newConversation);
        return newConversation;
    }





    public Conversation switchConversation(int index) {
        if (index >= 0 && index < conversations.size()) {
            currentConversationIndex = index;
            return getCurrentConversation();
        }
        return null;
    }

    public List<Conversation> getAllConversations() {
        return conversations;
    }

    public void addEntryToCurrentConversation(ConversationEntry entry) {
        getCurrentConversation().addEntry(entry);
        db.saveConversation(getCurrentConversation());
    }

    public void saveAllConversations() {
        for (Conversation conversation : conversations) {
            db.saveConversation(conversation);
        }
    }

    public void removeConversation(int index) {
        if (index >= 0 && index < conversations.size()) {
            String titleToRemove = conversations.get(index).getTitle();
            conversations.remove(index);
            db.deleteConversation(titleToRemove);
            if (currentConversationIndex >= conversations.size()) {
                currentConversationIndex = conversations.size() - 1;
            }
            if (conversations.isEmpty()) {
                conversations.add(new Conversation("New Chat", new Date()));
                currentConversationIndex = 0;
            }
        }
    }

    public int getCurrentConversationIndex() {
        return currentConversationIndex;
    }

    public void saveCurrentConversation() {
        db.saveConversation(getCurrentConversation());
    }

    public void removeConversation(String title) {
        conversations.removeIf(conv -> conv.getTitle().equals(title));
        db.deleteConversation(title);
        if (conversations.isEmpty()) {
            conversations.add(new Conversation("New Chat", new Date()));
            currentConversationIndex = 0;
        } else {
            currentConversationIndex = Math.max(0, currentConversationIndex - 1);
        }
    }

    public static String getFormattedTitle(Conversation conversation) {
        String title = conversation.getTitle();
        Date timestamp = conversation.getTimestamp();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");
        Date now = new Date();
        long diff = now.getTime() - timestamp.getTime();
        long mins = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);

        if (mins < 60) return title + " (" + mins + " min ago)";
        else if (hours < 24) return title + " (" + hours + " hrs ago)";
        else if (days < 7) return title + " (" + days + " days ago)";
        else return title + " (" + dateFormat.format(timestamp) + ")";
    }

    public static class Conversation {
        private String title;
        private Date timestamp;
        private final Stack<ConversationEntry> entries;

        public Conversation(String title, Date timestamp) {
            this.title = title;
            this.timestamp = timestamp;
            this.entries = new Stack<>();
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public Stack<ConversationEntry> getEntries() {
            return entries;
        }

        public void addEntry(ConversationEntry entry) {
            entries.push(entry);
            timestamp = new Date();
        }

        public String getDerivedTitle() {
            Stack<ConversationEntry> temp = new Stack<>();
            Stack<ConversationEntry> restore = new Stack<>();
            String firstUserMessage = null;

            while (!entries.isEmpty()) {
                ConversationEntry e = entries.pop();
                restore.push(e);
                if (e.getSpeaker() == ConversationEntry.Speaker.USER && firstUserMessage == null) {
                    firstUserMessage = e.getMessage();
                }
            }

            while (!restore.isEmpty()) {
                entries.push(restore.pop());
            }

            if (firstUserMessage != null && firstUserMessage.length() > 30)
                return firstUserMessage.substring(0, 27) + "...";
            else return firstUserMessage != null ? firstUserMessage : title;
        }

        public List<ConversationEntry> getEntriesChronological() {
            List<ConversationEntry> chronological = new ArrayList<>();
            Stack<ConversationEntry> temp = new Stack<>();
            Stack<ConversationEntry> restore = new Stack<>();

            while (!entries.isEmpty()) {
                ConversationEntry e = entries.pop();
                temp.push(e);
                restore.push(e);
            }

            while (!restore.isEmpty()) {
                entries.push(restore.pop());
            }

            while (!temp.isEmpty()) {
                chronological.add(temp.pop());
            }

            return chronological;
        }
    }
}
