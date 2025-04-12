package com.teamname.controller;

import com.teamname.api.OpenAIService;
import com.teamname.datastructures.Stack;
import com.teamname.datastructures.HashMap;
import com.teamname.datastructures.PriorityQueue;
import com.teamname.datastructures.BinarySearchTree;
import com.teamname.algorithms.sorting.QuickSort;
import com.teamname.algorithms.utils.AlgorithmAnalyzer;
import com.teamname.gui.ChatUI;
import com.teamname.model.ConversationEntry;
import com.teamname.model.ConversationManager;
import com.teamname.service.VoiceService;

import javax.swing.*;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class ChatController implements VoiceService.SpeechListener {
    // Services
    private OpenAIService openAIService;
    
    // Data Structures for the application
    private Stack<String> conversationHistory; // Legacy - keeping for backward compatibility
    private HashMap<String, String> responseCache;
    private PriorityQueue<Message> messageQueue;
    private BinarySearchTree<Topic> topicTree;
    
    // New conversation manager
    private ConversationManager conversationManager;
    
    // UI reference
    private ChatUI ui;
    
    // Speech synthesis
    private Voice voice;
    private boolean isSpeaking = false;
    
    // Message priorities
    public static final int PRIORITY_HIGH = 1;
    public static final int PRIORITY_MEDIUM = 2;
    public static final int PRIORITY_LOW = 3;
    
    public ChatController() {
        // Initialize services
        String apiKey = System.getenv("PERPLEXITY_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("No API key found. Using simulated responses.");
        } else {
            System.out.println("API key found. Length: " + apiKey.length());
        }
        openAIService = new OpenAIService(apiKey);
        
        // Initialize data structures
        conversationHistory = new Stack<>(); // Keep for backward compatibility
        responseCache = new HashMap<>();
        messageQueue = new PriorityQueue<>();
        topicTree = new BinarySearchTree<>();
        
        // Initialize conversation manager
        conversationManager = new ConversationManager();
        
        // Initialize the voice
        initializeVoice();
        
        // Initialize common topics in BST
        initializeTopicTree();
    }
    
    public ChatController(ConversationManager manager) {
        this(); // call the default constructor to init everything
        this.conversationManager = manager;
    }
   
    private void initializeVoice() {
        try {
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            VoiceManager voiceManager = VoiceManager.getInstance();
            voice = voiceManager.getVoice("kevin16");
            if (voice != null) {
                voice.allocate();
            } else {
                System.err.println("Cannot find a voice named kevin16. Speech will be disabled.");
            }
        } catch (Exception e) {
            System.err.println("Error initializing voice: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void setUI(ChatUI ui) {
        this.ui = ui;
        if (ui != null && ui.getVoiceService() != null) {
            ui.getVoiceService().setSpeechListener(this);
        }
    }
    
    // Implement the SpeechListener interface
    @Override
    public void speechStopped() {
        SwingUtilities.invokeLater(() -> {
            isSpeaking = false;
            if (ui != null) {
                ui.setSpeechInProgress(false);
            }
        });
    }
    
    public void handleUserMessage(String userInput, int priority) {
        String priorityLabel = getPriorityLabel(priority);
        
        // Add to conversation history - demonstrates Stack usage
        conversationHistory.push("User: " + userInput + priorityLabel);
        
        // Create conversation entry and add to conversation manager
        ConversationEntry entry = new ConversationEntry(
            ConversationEntry.Speaker.USER,
            userInput,
            new Date(),
            priority
        );
        conversationManager.addEntryToCurrentConversation(entry);
        
        // Update the output area with user input
        ui.appendToOutput("User: " + userInput + priorityLabel + "\n\n");
        
        // Update sidebar with latest conversation titles
        ui.updateConversationSidebar(conversationManager.getAllConversations(), 
                                   conversationManager.getCurrentConversationIndex());
        
        // Create Message with timestamp and priority
        Message message = new Message(userInput, new Date(), priority);
        
        // Add message to priority queue - demonstrates PriorityQueue usage
        messageQueue.enqueue(message);
        
        // Process all messages in the queue according to priority
        processMessageQueue();
    }
    
    private void processMessageQueue() {
        // Process all messages in the queue based on priority
        while (!messageQueue.isEmpty()) {
            Message message = messageQueue.dequeueHighestPriority();
            processMessage(message);
        }
    }
    
    private void processMessage(Message message) {
        String userInput = message.getText();
        
        // Categorize message in topic tree - demonstrates BST usage
        categorizeMessage(userInput);
        
        // Check cache first - demonstrates HashMap usage
        if (responseCache.containsKey(userInput)) {
            String cachedResponse = responseCache.get(userInput);
            displayAIResponse(cachedResponse + " [From Cache]");
            return;
        }
        
        try {
            // Show loading indicator
            ui.appendToOutput("AI: Thinking...\n");
            
            // Call the AI service in a separate thread to keep UI responsive
            new Thread(() -> {
                try {
                    // Call the Perplexity API
                    String response = openAIService.generateText(userInput);
                    
                    // Cache the response - demonstrates HashMap usage
                    responseCache.put(userInput, response);
                    
                    // Remove loading indicator and display the response
                    SwingUtilities.invokeLater(() -> {
                        // Remove the "thinking" text
                        String currentText = ui.getOutputText();
                        ui.setOutputText(currentText.replace("AI: Thinking...\n", ""));
                        
                        // Display the response
                        displayAIResponse(response);
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        // Remove the "thinking" text
                        String currentText = ui.getOutputText();
                        ui.setOutputText(currentText.replace("AI: Thinking...\n", ""));
                        
                        // Use fallback if API fails
                        String fallbackResponse = openAIService.generateJavaTutorialResponse(userInput);
                        displayAIResponse(fallbackResponse);
                    });
                }
            }).start();
        } catch (Exception e) {
            displayAIResponse("Error: " + e.getMessage());
        }
    }
    
    private void displayAIResponse(String response) {
        // Add to conversation history (legacy Stack)
        conversationHistory.push("AI: " + response);

        // Create entry for ConversationManager
        ConversationEntry entry = new ConversationEntry(
            ConversationEntry.Speaker.AI,
            response,
            new Date(),
            0 // Priority not used for AI responses
        );

        // Add to active conversation
        conversationManager.addEntryToCurrentConversation(entry);

        // Save updated conversation to DB
        conversationManager.saveCurrentConversation();

        // Display in the UI
        ui.appendToOutput("AI: " + response + "\n\n");

        // Update sidebar
        ui.updateConversationSidebar(
            conversationManager.getAllConversations(),
            conversationManager.getCurrentConversationIndex()
        );

        // Speak if voice output is enabled
        if (ui.isVoiceOutputEnabled()) {
            String speechText = extractPlainTextForSpeech(response);
            speakText(speechText);
        }
    }
    
    // Helper method to extract plain text from response (skipping code blocks)
    private String extractPlainTextForSpeech(String response) {
        // Skip code blocks for speech
        if (response.contains("```")) {
            StringBuilder speechText = new StringBuilder();
            int startIndex = 0;
            int codeBlockStart = response.indexOf("```");
            
            while (codeBlockStart != -1) {
                // Add text before code block
                speechText.append(response.substring(startIndex, codeBlockStart));
                
                // Find end of code block
                int codeBlockEnd = response.indexOf("```", codeBlockStart + 3);
                if (codeBlockEnd == -1) break;
                
                // Skip the code block and add a message
                speechText.append(" [Code example omitted for speech] ");
                
                // Update start index for next iteration
                startIndex = codeBlockEnd + 3;
                codeBlockStart = response.indexOf("```", startIndex);
            }
            
            // Add remaining text after last code block
            if (startIndex < response.length()) {
                speechText.append(response.substring(startIndex));
            }
            
            return speechText.toString();
        }
        
        return response;
    }
    
    // Speak text using FreeTTS
    public void speakText(String text) {
        if (voice == null) {
            System.err.println("Voice not initialized. Cannot speak.");
            return;
        }
        
        // Set speaking status - enables stop button
        isSpeaking = true;
        ui.setSpeechInProgress(true);
        
        // Speak in a separate thread to not block UI
        new Thread(() -> {
            try {
                if (ui != null && ui.getVoiceService() != null) {
                    
                    ui.getVoiceService().speak(text);
                } else {
                    voice.speak(text);
                }
                
                // Update UI after speech completes
                SwingUtilities.invokeLater(() -> {
                    isSpeaking = false;
                    ui.setSpeechInProgress(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                
                // Update UI in case of error
                SwingUtilities.invokeLater(() -> {
                    isSpeaking = false;
                    ui.setSpeechInProgress(false);
                });
            }
        }).start();
    }
    
    // Stop speaking
    public void stopSpeech() {
        if (isSpeaking) {
            System.out.println("🔈 Controller requesting speech stop...");
            
            // Update UI immediately
            isSpeaking = false;
            ui.setSpeechInProgress(false);
            
            try {
                // Method 1: Try to stop via VoiceService
                if (ui != null && ui.getVoiceService() != null) {
                    System.out.println("🔈 Calling VoiceService.stopSpeaking()");
                    ui.getVoiceService().stopSpeaking();
                } 
                // Method 2: Try to stop voice directly as a fallback
                else if (voice != null) {
                    System.out.println("🔈 Attempting to stop voice directly");
                    try {
                        // Get and cancel the audio player
                        if (voice.getAudioPlayer() != null) {
                            voice.getAudioPlayer().cancel();
                        }
                        // Deallocate and reallocate as a last resort
                        voice.deallocate();
                        voice.allocate();
                    } catch (Exception e) {
                        System.err.println("Error stopping voice: " + e.getMessage());
                    }
                } else {
                    System.err.println("🔈 Cannot stop speech - voice and service are null");
                }
            } catch (Exception e) {
                System.err.println("Error stopping speech: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public void clearConversation() {
        // Clear legacy stack
        conversationHistory = new Stack<>();
        
        // Create a new conversation
        conversationManager.createNewConversation("New Chat");
        
        // Update the sidebar
        ui.updateConversationSidebar(conversationManager.getAllConversations(), 
                                   conversationManager.getCurrentConversationIndex());
    }
    
    public void createNewConversation() {
        // Create a new conversation
        conversationManager.createNewConversation("New Chat");
        
        // Clear current display
        ui.clearOutputDisplay();
        
        // Update the sidebar
        ui.updateConversationSidebar(conversationManager.getAllConversations(), 
                                   conversationManager.getCurrentConversationIndex());
    }
    
    public void switchConversation(int index) {
        // Switch to the selected conversation
        ConversationManager.Conversation selectedConversation = 
            conversationManager.switchConversation(index);
        
        if (selectedConversation != null) {
            // Clear the current display
            ui.clearOutputDisplay();
            
            // Display all messages from the selected conversation
            List<ConversationEntry> entries = selectedConversation.getEntriesChronological();
            for (ConversationEntry entry : entries) {
                ui.appendToOutput(entry.toString() + "\n\n");
            }
            
            // Update the sidebar
            ui.updateConversationSidebar(conversationManager.getAllConversations(), 
                                       conversationManager.getCurrentConversationIndex());
        }
    }
    
    public void removeConversation(int index) {
        
        if (conversationManager.getAllConversations().size() <= 1) {
            JOptionPane.showMessageDialog(ui.getFrame(), 
                "Cannot delete the only conversation. Create a new one first.", 
                "Cannot Delete", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Confirm deletion
        int choice = JOptionPane.showConfirmDialog(ui.getFrame(), 
            "Are you sure you want to delete this conversation?", 
            "Confirm Deletion", 
            JOptionPane.YES_NO_OPTION);
            
        if (choice == JOptionPane.YES_OPTION) {
            // Remove the conversation
            conversationManager.removeConversation(index);
            
            // Switch to current conversation
            switchConversation(conversationManager.getCurrentConversationIndex());
        }
    }
    
    // The following methods are kept for backward compatibility
    public void displayHistory() {
        // Create a temporary stack to reverse the order
        Stack<String> tempStack = new Stack<>();
        Stack<String> originalStack = new Stack<>();
        
        // Copy to temporary stack and preserve original
        while (!conversationHistory.isEmpty()) {
            String message = conversationHistory.pop();
            tempStack.push(message);
            originalStack.push(message);
        }
        
        // Restore original stack
        while (!originalStack.isEmpty()) {
            conversationHistory.push(originalStack.pop());
        }
        
        // Display in chronological order
        StringBuilder history = new StringBuilder("Conversation History (from oldest to newest):\n\n");
        while (!tempStack.isEmpty()) {
            String message = tempStack.pop();
            history.append(message).append("\n\n");
        }
        
        // Show in a dialog with scrolling
        JTextArea textArea = new JTextArea(history.toString());
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        JOptionPane.showMessageDialog(ui.getFrame(), scrollPane, "Conversation History", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void searchTopics() {
        String searchTopic = JOptionPane.showInputDialog(ui.getFrame(), "Enter a programming topic to search for:");
        if (searchTopic != null && !searchTopic.isEmpty()) {
            // Convert to lowercase for case-insensitive search
            String lowerCaseTopic = searchTopic.toLowerCase();
            
            // First check if topic exists in BST - THIS SATISFIES BST REQUIREMENT
            boolean topicExistsInBST = topicTree.search(new Topic(lowerCaseTopic, ""));
            
            // Search through conversation history for this topic
            Stack<String> tempStack = new Stack<>();
            Stack<String> originalStack = new Stack<>();
            StringBuilder relevantMessages = new StringBuilder();
            
            // Copy conversations and preserve original stack
            while (!conversationHistory.isEmpty()) {
                String message = conversationHistory.pop();
                originalStack.push(message);
                
                // Check if message contains the topic
                if (message.toLowerCase().contains(lowerCaseTopic)) {
                    relevantMessages.append(message).append("\n\n");
                    
                    // If topic wasn't in BST, add it now - THIS UPDATES THE BST
                    if (!topicExistsInBST) {
                        topicTree.insert(new Topic(lowerCaseTopic, message));
                        topicExistsInBST = true; // Update flag
                    }
                }
            }
            
            // Restore original stack
            while (!originalStack.isEmpty()) {
                conversationHistory.push(originalStack.pop());
            }
            
            // Display BST search result and relevant messages
            if (topicExistsInBST) {
                StringBuilder resultMessage = new StringBuilder();
                resultMessage.append("Topic '").append(searchTopic).append("' found in the Binary Search Tree!\n\n");
                
                if (relevantMessages.length() > 0) {
                    resultMessage.append("Here are the relevant conversations:\n\n");
                    resultMessage.append(relevantMessages);
                } else {
                    resultMessage.append("However, no specific conversations were found with this exact term.\n");
                    resultMessage.append("This might happen if the topic is similar to others in the BST.");
                }
                
                JTextArea textArea = new JTextArea(resultMessage.toString());
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(600, 400));
                
                JOptionPane.showMessageDialog(ui.getFrame(), scrollPane, 
                    "BST Search Results for '" + searchTopic + "'", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Topic not found in BST
                JOptionPane.showMessageDialog(ui.getFrame(), 
                    "Topic '" + searchTopic + "' not found in the Binary Search Tree.\n" +
                    "Try asking a question that includes the word '" + searchTopic + "'!");
                    
                // Auto-populate the input field with a suggestion
                ui.setInputText("Tell me about " + searchTopic);
            }
        }
    }
    
    public void sortHistory() {
        if (conversationHistory.isEmpty()) {
            JOptionPane.showMessageDialog(ui.getFrame(), "No conversation history to sort.");
            return;
        }
        
        // Transfer stack to array for sorting
        List<String> historyList = new ArrayList<>();
        Stack<String> tempStack = new Stack<>();
        
        // Copy to temporary stack and preserve original
        while (!conversationHistory.isEmpty()) {
            String message = conversationHistory.pop();
            historyList.add(message);
            tempStack.push(message);
        }
        
        // Restore original stack
        while (!tempStack.isEmpty()) {
            conversationHistory.push(tempStack.pop());
        }
        
        // Convert list to array for QuickSort
        String[] historyArray = historyList.toArray(new String[0]);
        
        // Use AlgorithmAnalyzer to measure sorting time
        long sortingTime = AlgorithmAnalyzer.measureExecutionTime(
            arr -> QuickSort.sort(historyArray), 
            historyArray
        );
        
        // Display sorted history
        StringBuilder sortedHistory = new StringBuilder("Sorted Conversation History (alphabetically):\n\n");
        sortedHistory.append("Sorting completed in ").append(sortingTime).append(" ms\n\n");
        
        for (String message : historyArray) {
            sortedHistory.append(message).append("\n\n");
        }
        
        // Show in a dialog with scrolling
        JTextArea textArea = new JTextArea(sortedHistory.toString());
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        JOptionPane.showMessageDialog(ui.getFrame(), scrollPane, "Sorted Conversation History", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void downloadHistory() {
        // Get the current conversation
        ConversationManager.Conversation currentConversation = conversationManager.getCurrentConversation();
        List<ConversationEntry> entries = currentConversation.getEntriesChronological();
        
        // Check if there are any entries to download
        if (entries.isEmpty()) {
            JOptionPane.showMessageDialog(ui.getFrame(), 
                "No conversation history to download.", 
                "Empty History", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            // Let user choose where to save the file
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Conversation History");
            
            // Set default filename with date and conversation title
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
            String defaultFileName = "conversation_" + 
                                    currentConversation.getTitle().replaceAll("\\W+", "_") + "_" + 
                                    dateFormat.format(new Date()) + ".pdf";
            fileChooser.setSelectedFile(new File(defaultFileName));
            
            // Set file filter for PDF
            FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Files", "pdf");
            fileChooser.setFileFilter(filter);
            
            int userSelection = fileChooser.showSaveDialog(ui.getFrame());
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                // Add .pdf extension if not present
                if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
                }
                
                // Create PDF document
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                document.open();
                
                // Add title
                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD);
                document.add(new Paragraph("Conversation: " + currentConversation.getTitle(), titleFont));
                document.add(new Paragraph(" ")); // Empty line
                
                // Add messages to PDF in chronological order
                for (ConversationEntry entry : entries) {
                    // Format message with different fonts for user and AI
                    if (entry.getSpeaker() == ConversationEntry.Speaker.USER) {
                        com.itextpdf.text.Font userFont = new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
                        document.add(new Paragraph("User: " + entry.getMessage(), userFont));
                    } else {
                        com.itextpdf.text.Font aiFont = new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.NORMAL);
                        document.add(new Paragraph("AI: " + entry.getMessage(), aiFont));
                    }
                    
                    document.add(new Paragraph(" ")); // Empty line between messages
                }
                
                // Close document
                document.close();
                
                JOptionPane.showMessageDialog(ui.getFrame(), 
                    "Conversation history saved to: " + fileToSave.getAbsolutePath(), 
                    "Download Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ui.getFrame(), 
                "Error saving conversation history: " + ex.getMessage(), 
                "Download Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String getPriorityLabel(int priority) {
        switch (priority) {
            case PRIORITY_HIGH:
                return " [High Priority]";
            case PRIORITY_LOW:
                return " [Low Priority]";
            default:
                return " [Medium Priority]";
        }
    }
    
    private void categorizeMessage(String message) {
        String lowerMessage = message.toLowerCase();
        
        // Common programming topics to check for
        String[] topics = {
            "java", "python", "algorithm", "data structure", "loop", "variable", 
            "function", "method", "class", "object", "inheritance", "polymorphism", 
            "encapsulation", "interface", "exception", "array", "list", "map", 
            "set", "stack", "queue", "tree", "graph", "recursion", "sorting"
        };
        
        for (String topic : topics) {
            if (lowerMessage.contains(topic)) {
                // Add topic to BST
                topicTree.insert(new Topic(topic, message));
                break; // Add the first matching topic
            }
        }
    }
    
    private void initializeTopicTree() {
        String[] commonTopics = {
            "java", "python", "algorithm", "data structure", "loop", "variable", 
            "function", "method", "class", "object", "inheritance", "polymorphism", 
            "encapsulation", "interface", "exception", "array", "list", "map", 
            "hashmap", "set", "stack", "queue", "tree", "graph", "recursion", "sorting"
        };
        
        for (String topic : commonTopics) {
            topicTree.insert(new Topic(topic, ""));
        }
    }
    
    public void generateConversationSummary() {
        // Collect conversation history
        StringBuilder conversationText = new StringBuilder();
        
        // Get current conversation
        ConversationManager.Conversation currentConversation = conversationManager.getCurrentConversation();
        List<ConversationEntry> entries = currentConversation.getEntriesChronological();
        
        // Build conversation text
        for (ConversationEntry entry : entries) {
            conversationText.append(entry.toString()).append("\n");
        }
        
        try {
            // Generate summary using OpenAI service
            String summary = openAIService.generateConversationSummary(
                conversationText.toString()
            );
            
            // Display summary in a dialog
            JDialog summaryDialog = new JDialog(ui.getFrame(), "Conversation Summary", true);
            JTextArea summaryArea = new JTextArea(summary);
            summaryArea.setEditable(false);
            summaryArea.setLineWrap(true);
            summaryArea.setWrapStyleWord(true);
            
            JScrollPane scrollPane = new JScrollPane(summaryArea);
            scrollPane.setPreferredSize(new Dimension(400, 200));
            
            summaryDialog.getContentPane().add(scrollPane);
            summaryDialog.pack();
            summaryDialog.setLocationRelativeTo(ui.getFrame());
            summaryDialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ui.getFrame(), 
                "Error generating summary: " + e.getMessage(), 
                "Summary Error", 
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    // Inner class for messages with priority
    public static class Message implements Comparable<Message> {
        private final String text;
        private final Date timestamp;
        private final int priority; // Lower number = higher priority
        
        public Message(String text, Date timestamp, int priority) {
            this.text = text;
            this.timestamp = timestamp;
            this.priority = priority;
        }
        
        public String getText() {
            return text;
        }
        
        public Date getTimestamp() {
            return timestamp;
        }
        
        public int getPriority() {
            return priority;
        }
        
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
    }
    
    // Inner class for topics in the BST
    public static class Topic implements Comparable<Topic> {
        private final String name;
        private final String context;
        
        public Topic(String name, String context) {
            this.name = name; 
            this.context = context;
        }
        
        public String getName() {
            return name;
        }
        
        public String getContext() {
            return context;
        }
        
        @Override
        public int compareTo(Topic other) {
            return this.name.compareTo(other.name);
        }
    }
    
    // Method to get output text - needed for UI interaction
    public String getOutputText() {
        return ui.getOutputText();
    }
    
    // Getter for conversation manager
    public ConversationManager getConversationManager() {
        return conversationManager;
    }
}