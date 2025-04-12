package com.teamname.service;

import com.teamname.model.ConversationEntry;
import com.teamname.model.ConversationManager.Conversation;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseService {
    private static final String DB_URL = "jdbc:sqlite:chat_history.db";

    public DatabaseService() {
        createTables();
    }

    public void createTables() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            System.out.println("Creating tables if they don't exist...");

            // Main tables
            stmt.executeUpdate("""
            	    CREATE TABLE IF NOT EXISTS conversations (
            	        id INTEGER PRIMARY KEY AUTOINCREMENT,
            	        title TEXT UNIQUE,
            	        timestamp INTEGER
            	    );
            	""");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS conversation_entries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    speaker TEXT NOT NULL,
                    message TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    priority INTEGER DEFAULT 0,
                    FOREIGN KEY (title) REFERENCES conversations(title)
                );
            """);

            // Index for speed
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_title ON conversation_entries(title);");

            System.out.println("Tables created successfully");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteConversation(String title) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String deleteEntriesSQL = "DELETE FROM conversation_entries WHERE title = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteEntriesSQL)) {
                stmt.setString(1, title);
                int rowsDeleted = stmt.executeUpdate();
                System.out.println("Deleted " + rowsDeleted + " entries for conversation: " + title);
            }

            String deleteConversationSQL = "DELETE FROM conversations WHERE title = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteConversationSQL)) {
                stmt.setString(1, title);
                int rowsDeleted = stmt.executeUpdate();
                System.out.println("Deleted conversation: " + title);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting conversation '" + title + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveConversation(Conversation conversation) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String title = conversation.getTitle();
            long timestamp = conversation.getTimestamp().getTime();

            conn.setAutoCommit(false);
            try {
                String upsertConversationSQL = "INSERT OR REPLACE INTO conversations (title, timestamp) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(upsertConversationSQL)) {
                    stmt.setString(1, title);
                    stmt.setLong(2, timestamp);
                    stmt.executeUpdate();
                    System.out.println("💾 Saved metadata: " + title);
                }

                List<ConversationEntry> entries = conversation.getEntriesChronological();
                if (!entries.isEmpty()) {
                    String checkExistSQL = """
                        SELECT COUNT(*) FROM conversation_entries 
                        WHERE title = ? AND speaker = ? AND message = ? AND timestamp = ?
                    """;
                    String insertSQL = """
                        INSERT INTO conversation_entries (title, speaker, message, timestamp, priority) 
                        VALUES (?, ?, ?, ?, ?)
                    """;

                    try (PreparedStatement checkStmt = conn.prepareStatement(checkExistSQL);
                         PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                        for (ConversationEntry entry : entries) {
                            checkStmt.setString(1, title);
                            checkStmt.setString(2, entry.getSpeaker().name());
                            checkStmt.setString(3, entry.getMessage());
                            checkStmt.setLong(4, entry.getTimestamp().getTime());

                            ResultSet rs = checkStmt.executeQuery();
                            if (rs.next() && rs.getInt(1) == 0) {
                                insertStmt.setString(1, title);
                                insertStmt.setString(2, entry.getSpeaker().name());
                                insertStmt.setString(3, entry.getMessage());
                                insertStmt.setLong(4, entry.getTimestamp().getTime());
                                insertStmt.setInt(5, entry.getPriority());
                                insertStmt.addBatch();
                            }
                        }

                        int[] results = insertStmt.executeBatch();
                        System.out.println("✅ Appended " + results.length + " entries to: " + title);
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error saving conversation: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public List<ConversationEntry> loadConversation(String title) {
        List<ConversationEntry> entries = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String selectSQL = "SELECT speaker, message, timestamp, priority FROM conversation_entries WHERE title = ? ORDER BY timestamp";
            try (PreparedStatement stmt = conn.prepareStatement(selectSQL)) {
                stmt.setString(1, title);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String speaker = rs.getString("speaker");
                    String message = rs.getString("message");
                    long timestamp = rs.getLong("timestamp");
                    int priority = rs.getInt("priority");

                    try {
                        entries.add(new ConversationEntry(
                                ConversationEntry.Speaker.valueOf(speaker),
                                message,
                                new Date(timestamp),
                                priority
                        ));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Skipping unknown speaker: " + speaker);
                    }
                }

                System.out.println("Loaded " + entries.size() + " entries for conversation: " + title);
            }
        } catch (SQLException e) {
            System.err.println("Error loading conversation: " + e.getMessage());
            e.printStackTrace();
        }

        return entries;
    }

    public List<String> getAllConversationTitles() {
        List<String> titles = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Use DISTINCT to avoid duplicate logs
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT title FROM conversations ORDER BY timestamp DESC");

            while (rs.next()) {
                String title = rs.getString("title");
                titles.add(title);
                System.out.println("✅ Found unique conversation title: " + title);
            }

            System.out.println("Loaded " + titles.size() + " distinct conversation titles from conversations table");
        } catch (SQLException e) {
            System.err.println("Error getting conversation titles: " + e.getMessage());
            e.printStackTrace();
        }

        return titles;
    }
}
