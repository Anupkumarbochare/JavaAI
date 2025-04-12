package com.teamname.gui;

import com.teamname.controller.ChatController;
import com.teamname.service.VoiceService;
import com.teamname.model.ConversationManager;
import com.teamname.model.ConversationManager.Conversation;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;


import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;


import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

public class ChatUI {

    private static final Color PRIMARY_COLOR = new Color(64, 66, 88);      // Dark slate (header/main elements)
    private static final Color SECONDARY_COLOR = new Color(85, 90, 125);    // Medium slate (buttons)
    private static final Color ACCENT_COLOR = new Color(125, 135, 179);     // Light slate (highlights)
    private static final Color BG_COLOR = new Color(249, 250, 252);         // Almost white background
    private static final Color SIDEBAR_COLOR = new Color(242, 243, 245);    // Light gray for sidebar
    private static final Color USER_MSG_COLOR = new Color(241, 243, 248);   // Light blue-gray
    private static final Color AI_MSG_COLOR = new Color(252, 252, 252);     // White
    private static final Color TEXT_COLOR = new Color(52, 53, 65);          // Dark gray
    private static final Color BORDER_COLOR = new Color(222, 222, 222);     // Light gray border
    private static final Color CODE_BG_COLOR = new Color(245, 247, 249);    // Light gray for code
    private static final Color BUTTON_HOVER_COLOR = new Color(103, 109, 152); // Darker button color
    
    // Font constants
    private static final Font MAIN_FONT = new Font("Inter", Font.PLAIN, 14);
    private static final Font BOLD_FONT = new Font("Inter", Font.BOLD, 14);
    private static final Font HEADER_FONT = new Font("Inter", Font.BOLD, 16);
    private static final Font SIDEBAR_FONT = new Font("Inter", Font.BOLD, 13);
    private static final Font INPUT_FONT = new Font("Inter", Font.PLAIN, 15);
    private static final Font CODE_FONT = new Font("JetBrains Mono", Font.PLAIN, 13);

    private JFrame frame;
    private JTextPane outputPane;
    private JTextArea inputArea;
    private JButton sendButton;
    private JButton clearButton;
    private JButton showHistoryButton;
    private JButton searchTopicsButton;
    private JButton sortHistoryButton;
    private JButton downloadHistoryButton;
    private JButton summarizeButton;
    private JComboBox<String> prioritySelector;
    
    // Voice-related fields
    private JToggleButton voiceInputButton;
    private JToggleButton voiceOutputButton;
    private JButton stopSpeechButton;
    private VoiceService voiceService;
    private ChatController controller;
    
    // Sidebar components
    private JPanel sidebarPanel;
    private JButton newChatButton;
    private JPanel conversationListPanel;
    
    public ChatUI(ChatController controller) {
        this.controller = controller;
        initialize();
    }
    
    private void initialize() {

        try {

            try {
                Class.forName("com.formdev.flatlaf.FlatLightLaf");
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            } catch (ClassNotFoundException e) {
                // Fall back to default look 
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
            
            // Install custom UI defaults
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("TextField.margin", new Insets(6, 6, 6, 6));
            UIManager.put("TextArea.margin", new Insets(6, 6, 6, 6));
            UIManager.put("Button.margin", new Insets(8, 14, 8, 14));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create the frame with modern dimensions
        frame = new JFrame("AI Programming Tutor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.getContentPane().setBackground(BG_COLOR);
        
        // Create components
        setupComponents();
        
        // Layout components
        setupLayout();
        
        // Add event handlers
        setupEventHandlers();
        
        // Center on screen
        frame.setLocationRelativeTo(null);
        
        // Display the frame
        frame.setVisible(true);
        stopSpeechButton = new JButton("🛑 Stop Speech");

        stopSpeechButton.addActionListener(e -> {
            System.out.println("🔘 Stop button clicked");
            voiceService.stopSpeaking();
        });

    }
    
    private void setupComponents() {
        // Initialize styled document for output pane
        outputPane = new JTextPane();
        outputPane.setEditable(false);
        outputPane.setBackground(BG_COLOR);
        outputPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        outputPane.setFont(MAIN_FONT);
        
        // Set custom StyledDocument for output pane
        StyledDocument doc = outputPane.getStyledDocument();
        
        // Create styles
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        
        Style regularStyle = doc.addStyle("regular", defaultStyle);
        StyleConstants.setFontFamily(regularStyle, MAIN_FONT.getFamily());
        StyleConstants.setFontSize(regularStyle, MAIN_FONT.getSize());
        StyleConstants.setForeground(regularStyle, TEXT_COLOR);
        
        Style titleStyle = doc.addStyle("title", regularStyle);
        StyleConstants.setFontSize(titleStyle, HEADER_FONT.getSize());
        StyleConstants.setBold(titleStyle, true);
        StyleConstants.setForeground(titleStyle, PRIMARY_COLOR);
        
        Style userStyle = doc.addStyle("user", regularStyle);
        StyleConstants.setBold(userStyle, true);
        StyleConstants.setForeground(userStyle, PRIMARY_COLOR);
        
        Style aiStyle = doc.addStyle("ai", regularStyle);
        StyleConstants.setBold(aiStyle, true);
        StyleConstants.setForeground(aiStyle, SECONDARY_COLOR);
        
        // Add panel styles for messages
        Style userPanelStyle = doc.addStyle("user_panel", null);
        StyleConstants.setBackground(userPanelStyle, USER_MSG_COLOR);
        StyleConstants.setSpaceAbove(userPanelStyle, 10);
        StyleConstants.setSpaceBelow(userPanelStyle, 10);
        
        Style aiPanelStyle = doc.addStyle("ai_panel", null);
        StyleConstants.setBackground(aiPanelStyle, AI_MSG_COLOR);
        StyleConstants.setSpaceAbove(aiPanelStyle, 10);
        StyleConstants.setSpaceBelow(aiPanelStyle, 10);
     
        // Voice-related buttons 
        voiceInputButton = createToggleButton("Voice Input", FontIcon.of(FontAwesomeSolid.MICROPHONE, 16, Color.WHITE));
        voiceOutputButton = createToggleButton("Voice Output", FontIcon.of(FontAwesomeSolid.VOLUME_UP, 16, Color.WHITE));

        // Add stop speech button
        stopSpeechButton = createStyledButton("Stop", FontIcon.of(FontAwesomeSolid.STOP, 16, Color.WHITE), new Color(220, 53, 69), Color.WHITE);
        stopSpeechButton.setEnabled(false); // Initially disabled until speech starts
        stopSpeechButton.setToolTipText("Stop the current text-to-speech output");

        // Initialize voice service
        voiceService = new VoiceService();
        
        // Add code style
        Style codeStyle = doc.addStyle("code", regularStyle);
        StyleConstants.setFontFamily(codeStyle, CODE_FONT.getFamily());
        StyleConstants.setFontSize(codeStyle, CODE_FONT.getSize());
        
        // Modern input area
        inputArea = new JTextArea(4, 40);
        inputArea.setFont(INPUT_FONT);
        inputArea.setBackground(Color.WHITE);
        inputArea.setForeground(TEXT_COLOR);
        inputArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(12, 12, 12, 12),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        
        // Create sidebar components
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(SIDEBAR_COLOR);
        sidebarPanel.setPreferredSize(new Dimension(250, frame.getHeight()));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        
        newChatButton = createStyledButton("+ New Chat", FontIcon.of(FontAwesomeSolid.PLUS, 14, Color.WHITE), PRIMARY_COLOR, Color.WHITE);
        newChatButton.setPreferredSize(new Dimension(220, 40));
        newChatButton.setFont(BOLD_FONT);
        
        conversationListPanel = new JPanel();
        conversationListPanel.setBackground(SIDEBAR_COLOR);
        conversationListPanel.setLayout(new BoxLayout(conversationListPanel, BoxLayout.Y_AXIS));
        
        // Create modern buttons with FontIcon
        sendButton = createIconButton(FontIcon.of(FontAwesomeSolid.PAPER_PLANE, 16, Color.WHITE), SECONDARY_COLOR);
        clearButton = createStyledButton("Clear", FontIcon.of(FontAwesomeSolid.TRASH_ALT, 14, Color.WHITE), ACCENT_COLOR, Color.WHITE);
        showHistoryButton = createStyledButton("History", FontIcon.of(FontAwesomeSolid.HISTORY, 14, Color.WHITE), SECONDARY_COLOR, Color.WHITE);
        searchTopicsButton = createStyledButton("Search", FontIcon.of(FontAwesomeSolid.SEARCH, 14, Color.WHITE), SECONDARY_COLOR, Color.WHITE);
        sortHistoryButton = createStyledButton("Sort", FontIcon.of(FontAwesomeSolid.SORT, 14, Color.WHITE), SECONDARY_COLOR, Color.WHITE);
        downloadHistoryButton = createStyledButton("Export PDF", FontIcon.of(FontAwesomeSolid.FILE_PDF, 14, Color.WHITE), SECONDARY_COLOR, Color.WHITE);
        summarizeButton = createStyledButton("Summarize", FontIcon.of(FontAwesomeSolid.COMPRESS_ALT, 14, Color.WHITE), SECONDARY_COLOR, Color.WHITE);
        
        // Modern priority selector
        String[] priorities = {"High Priority", "Medium Priority", "Low Priority"};
        prioritySelector = new JComboBox<>(priorities);
        prioritySelector.setSelectedIndex(1); // Default to medium priority
        prioritySelector.setFont(MAIN_FONT);
        prioritySelector.setBackground(Color.WHITE);
        prioritySelector.setForeground(TEXT_COLOR);
        prioritySelector.setRenderer(new ModernComboBoxRenderer());
        
        // Add welcome message with styles
        addWelcomeMessage();
        
        // Initialize conversation sidebar with the current conversations
        if (controller != null && controller.getConversationManager() != null) {
            updateConversationSidebar(
                controller.getConversationManager().getAllConversations(),
                controller.getConversationManager().getCurrentConversationIndex()
            );
        }
    }
    
    private class ModernComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            
            if (isSelected) {
                label.setBackground(SECONDARY_COLOR);
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(TEXT_COLOR);
            }
            
            return label;
        }
    }
    
    private JToggleButton createToggleButton(String text, Icon icon) {
        JToggleButton button = new JToggleButton(text);
        if (icon != null) button.setIcon(icon);
        button.setFont(MAIN_FONT);
        button.setBackground(BG_COLOR);
        button.setForeground(PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!button.isSelected()) {
                    button.setBackground(new Color(240, 240, 240));
                }
            }
            public void mouseExited(MouseEvent e) {
                if (!button.isSelected()) {
                    button.setBackground(BG_COLOR);
                }
            }
        });
        
        return button;
    }
    
    private JButton createIconButton(Icon icon, Color bgColor) {
        JButton button = new JButton();
        if (icon != null) button.setIcon(icon);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(40, 40));
        button.setMargin(new Insets(0, 0, 0, 0));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_HOVER_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private JButton createStyledButton(String text, Icon icon, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        if (icon != null) button.setIcon(icon);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(MAIN_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void addWelcomeMessage() {
        try {
            StyledDocument doc = outputPane.getStyledDocument();
            doc.insertString(doc.getLength(), "Welcome to the AI Programming Tutor\n", doc.getStyle("title"));
            doc.insertString(doc.getLength(), "I'm here to help you learn programming concepts.\n\n", doc.getStyle("regular"));
            doc.insertString(doc.getLength(), "Key features available:\n", doc.getStyle("regular"));
            doc.insertString(doc.getLength(), "• Conversation history management using Stack\n", doc.getStyle("regular"));
            doc.insertString(doc.getLength(), "• Response caching with HashMap\n", doc.getStyle("regular"));
            doc.insertString(doc.getLength(), "• Message priority handling via Priority Queue\n", doc.getStyle("regular"));
            doc.insertString(doc.getLength(), "• Topic organization with Binary Search Tree\n", doc.getStyle("regular"));
            doc.insertString(doc.getLength(), "• Conversation sorting using QuickSort\n\n", doc.getStyle("regular"));
            doc.insertString(doc.getLength(), "Ask me about programming topics or try out the different features using the buttons below!\n\n", doc.getStyle("regular"));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private void setupLayout() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_COLOR);
        
        // Setup sidebar layout
        setupSidebarLayout();
        
        // Create chat main area
        JPanel chatPanel = new JPanel(new BorderLayout(0, 0));
        chatPanel.setBackground(BG_COLOR);
        
        // Create a custom scroll pane for the output
        JScrollPane outputScrollPane = new JScrollPane(outputPane);
        outputScrollPane.setBorder(BorderFactory.createEmptyBorder());
        outputScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Scrollbars
        outputScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        outputScrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        
        // Create header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        headerPanel.setPreferredSize(new Dimension(chatPanel.getWidth(), 60));
        
        JLabel headerLabel = new JLabel("AI Programming Tutor");
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(Color.WHITE);
        
        JPanel headerButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerButtonPanel.setBackground(PRIMARY_COLOR);
        
        // Add header buttons
        headerButtonPanel.add(clearButton);
        headerButtonPanel.add(downloadHistoryButton);
        headerButtonPanel.add(summarizeButton);
        
        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(headerButtonPanel, BorderLayout.EAST);
        
        // Create input area with rounded borders
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BG_COLOR);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Create a rounded border panel for input
        JPanel inputBorderPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        inputBorderPanel.setOpaque(false);
        inputBorderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        inputBorderPanel.setBackground(Color.WHITE);
        
        // Create a scrollable input area
        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        inputScrollPane.setBorder(BorderFactory.createEmptyBorder());
        inputScrollPane.setOpaque(false);
        inputScrollPane.getViewport().setOpaque(false);
        
        // Customize scrollbars
        inputScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        inputScrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        
        // Create a panel for input accessories (voice buttons, priority)
        JPanel inputAccessoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        inputAccessoryPanel.setOpaque(false);
        inputAccessoryPanel.add(voiceInputButton);
        inputAccessoryPanel.add(voiceOutputButton);
        inputAccessoryPanel.add(stopSpeechButton);
        
        JPanel rightAccessoryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightAccessoryPanel.setOpaque(false);
        
        JLabel priorityLabel = new JLabel("Priority:");
        priorityLabel.setFont(MAIN_FONT);
        priorityLabel.setForeground(TEXT_COLOR);
        
        rightAccessoryPanel.add(priorityLabel);
        rightAccessoryPanel.add(prioritySelector);
        
        // Add input area and send button to border panel
        inputBorderPanel.add(inputScrollPane, BorderLayout.CENTER);
        inputBorderPanel.add(sendButton, BorderLayout.EAST);
        
        // Add everything to input panel
        inputPanel.add(inputAccessoryPanel, BorderLayout.NORTH);
        inputPanel.add(inputBorderPanel, BorderLayout.CENTER);
        inputPanel.add(rightAccessoryPanel, BorderLayout.SOUTH);
        
        // Add all panels to chat panel
        chatPanel.add(headerPanel, BorderLayout.NORTH);
        chatPanel.add(outputScrollPane, BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        
        // Add sidebar and chat panel to main panel
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(chatPanel, BorderLayout.CENTER);
        
        // Add main panel to frame
        frame.setContentPane(mainPanel);
    }
    
    private void setupSidebarLayout() {
        sidebarPanel.setLayout(new BorderLayout());
        
        // Top panel for new chat button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        topPanel.setBackground(SIDEBAR_COLOR);
        topPanel.add(newChatButton);
        
        // Bottom panel for other sidebar buttons
        JPanel bottomButtonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        bottomButtonPanel.setBackground(SIDEBAR_COLOR);
        bottomButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        
        bottomButtonPanel.add(showHistoryButton);
        bottomButtonPanel.add(searchTopicsButton);
        bottomButtonPanel.add(sortHistoryButton);
        
        // Create a scroll pane for conversation list
        JScrollPane conversationScrollPane = new JScrollPane(conversationListPanel);
        conversationScrollPane.setBorder(BorderFactory.createEmptyBorder());
        conversationScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        conversationScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        conversationScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        
        // Add panels to sidebar
        sidebarPanel.add(topPanel, BorderLayout.NORTH);
        sidebarPanel.add(conversationScrollPane, BorderLayout.CENTER);
        sidebarPanel.add(bottomButtonPanel, BorderLayout.SOUTH);
    }
    
    private class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = ACCENT_COLOR;
            this.trackColor = BG_COLOR;
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }
        
        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
        
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.translate(thumbBounds.x, thumbBounds.y);
            g2.setColor(thumbColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, thumbBounds.width, thumbBounds.height, 8, 8));
            g2.dispose();
        }
    }
    
    private void setupEventHandlers() {
        sendButton.addActionListener(e -> {
            String userInput = inputArea.getText().trim();
            if (!userInput.isEmpty()) {
                int priority = getPriorityFromSelector();
                controller.handleUserMessage(userInput, priority);
                inputArea.setText("");
            }
        });
        
        clearButton.addActionListener(e -> {
            inputArea.setText("");
            outputPane.setText("");
            addWelcomeMessage();
            controller.clearConversation();
        });
        
        showHistoryButton.addActionListener(e -> controller.displayHistory());
        
        searchTopicsButton.addActionListener(e -> controller.searchTopics());
        
        sortHistoryButton.addActionListener(e -> controller.sortHistory());
        
        downloadHistoryButton.addActionListener(e -> controller.downloadHistory());
        
        // Summarize conversation button action
        summarizeButton.addActionListener(e -> controller.generateConversationSummary());
        
        // New chat button action
        newChatButton.addActionListener(e -> {
            controller.createNewConversation();
        });
        
        // Handle Enter key in input area
        inputArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER && !evt.isShiftDown()) {
                    evt.consume();
                    sendButton.doClick();
                }
            }
        });
        
        voiceInputButton.addActionListener(e -> {
            if (voiceInputButton.isSelected()) {
                if (voiceService.isSTTAvailable()) {
                    voiceInputButton.setText("Listening...");
                    voiceInputButton.setBackground(new Color(255, 100, 100));
                    voiceService.startListening(recognizedText -> {
                        SwingUtilities.invokeLater(() -> {
                        	inputArea.append(recognizedText + " ");
                            // Optionally send the message automatically
                            // sendButton.doClick();
                        });
                    });
                } else {
                    JOptionPane.showMessageDialog(frame, 
                        "Speech recognition is not available on this system.", 
                        "Feature Unavailable", 
                        JOptionPane.WARNING_MESSAGE);
                    voiceInputButton.setSelected(false);
                }
            } else {
                voiceInputButton.setText("Voice Input");
                voiceInputButton.setBackground(BG_COLOR);
                voiceService.stopListening();
            }
        });

        voiceOutputButton.addActionListener(e -> {
            if (voiceOutputButton.isSelected()) {
                if (voiceService.isTTSAvailable()) {
                    voiceOutputButton.setText("Voice Output ON");
                    voiceOutputButton.setBackground(new Color(100, 255, 100));
                } else {
                    JOptionPane.showMessageDialog(frame, 
                        "Text-to-speech is not available on this system.", 
                        "Feature Unavailable", 
                        JOptionPane.WARNING_MESSAGE);
                    voiceOutputButton.setSelected(false);
                }
            } else {
                voiceOutputButton.setText("Voice Output");
                voiceOutputButton.setBackground(BG_COLOR);
            }
        });
        
        // Add stop speech button handler
        stopSpeechButton.addActionListener(e -> {
            if (voiceService != null) {
                voiceService.stopSpeaking();
                stopSpeechButton.setEnabled(false);
            }
        });
    }
    
    public boolean isVoiceOutputEnabled() {
        return voiceOutputButton.isSelected();
    }

    public void speakText(String text) {
        if (voiceService != null && isVoiceOutputEnabled()) {
            stopSpeechButton.setEnabled(true); // Enable stop button when speech starts
            voiceService.speak(text);
            
            // Start a background thread to monitor speaking status
            new Thread(() -> {
                try {
                    // Check every 500ms if speaking is finished
                    while (voiceService.isSpeaking()) {
                        Thread.sleep(500);
                    }
                    // Disable stop button when speaking stops
                    SwingUtilities.invokeLater(() -> stopSpeechButton.setEnabled(false));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    
    public void setSpeechInProgress(boolean inProgress) {
        stopSpeechButton.setEnabled(inProgress);
    }
    
    
    public void dispose() {
        if (voiceService != null) {
            voiceService.close();
        }
    }
    
    private int getPriorityFromSelector() {
        switch (prioritySelector.getSelectedIndex()) {
            case 0: return 1; // High
            case 2: return 3; // Low
            default: return 2; // Medium
        }
    }
    
    public void appendToOutput(String text) {
        try {
            StyledDocument doc = outputPane.getStyledDocument();
            if (text.startsWith("User: ")) {
                
                appendStyledMessage(doc, text, "user", USER_MSG_COLOR);
            } else if (text.startsWith("AI: ")) {
                
                appendStyledMessage(doc, text, "ai", AI_MSG_COLOR);
            } else {
                
                doc.insertString(doc.getLength(), text, doc.getStyle("regular"));
            }
            
            // Scroll to the bottom
            outputPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private void appendStyledMessage(StyledDocument doc, String text, String stylePrefix, Color bgColor) 
            throws BadLocationException {
        // Split the text into prefix (User:/AI:) and content
        int colonIndex = text.indexOf(":");
        if (colonIndex != -1) {
            String prefix = text.substring(0, colonIndex + 1);
            String content = text.substring(colonIndex + 1);
            
            // Insert a spacer before each message
            doc.insertString(doc.getLength(), "\n", doc.getStyle("regular"));
            
            // Create a message panel with rounded borders
            JPanel messagePanel = new JPanel(new BorderLayout());
            messagePanel.setBackground(bgColor);
            
            // Insert the message prefix with avatar-like icon
            JPanel avatarPanel = new JPanel(new BorderLayout());
            avatarPanel.setOpaque(false);
            avatarPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            
            // Create avatar icon
            JLabel avatarLabel = new JLabel();
            if (stylePrefix.equals("user")) {
                avatarLabel.setText("👤"); // User icon
            } else {
                avatarLabel.setText("🤖"); // AI icon
            }
            avatarLabel.setFont(new Font(avatarLabel.getFont().getName(), Font.PLAIN, 24));
            avatarPanel.add(avatarLabel, BorderLayout.CENTER);
            
            // Insert the prefix with style
            int startPos = doc.getLength();
            
            // Add prefix with styling
            doc.insertString(doc.getLength(), prefix + " ", doc.getStyle(stylePrefix));
            
            // Style the entire paragraph
            Style panelStyle = doc.getStyle(stylePrefix + "_panel");
            doc.setParagraphAttributes(startPos, prefix.length() + 1, panelStyle, false);
            
            // Check if the content contains code blocks (text between ```java and ```)
            if (content.contains("```java") && content.contains("```")) {
                // Process content with code blocks
                processContentWithCodeBlocks(doc, content, panelStyle, bgColor);
            } else {
                // Insert regular content with panel style
                startPos = doc.getLength();
                doc.insertString(doc.getLength(), content + "\n\n", doc.getStyle("regular"));
                doc.setParagraphAttributes(startPos, content.length() + 2, panelStyle, false);
            }
        } else {
            // Fallback if the text doesn't have the expected format
            doc.insertString(doc.getLength(), text + "\n", doc.getStyle("regular"));
        }
    }

    private void processContentWithCodeBlocks(StyledDocument doc, String content, Style panelStyle, Color bgColor) 
            throws BadLocationException {
        int codeStart = content.indexOf("```java");
        
        // Handle text before code block
        if (codeStart > 0) {
            String beforeCode = content.substring(0, codeStart);
            int startPos = doc.getLength();
            doc.insertString(doc.getLength(), beforeCode, doc.getStyle("regular"));
            doc.setParagraphAttributes(startPos, beforeCode.length(), panelStyle, false);
        }
        
        // Handle code block
        int codeContentStart = codeStart + 8; // "```java" length
        int codeEnd = content.indexOf("```", codeContentStart);
        
        if (codeEnd != -1) {
            String codeContent = content.substring(codeContentStart, codeEnd).trim();
            
            try {
                // Create a syntax highlighted code component
                RSyntaxTextArea codeArea = new RSyntaxTextArea(codeContent);
                codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
                codeArea.setCodeFoldingEnabled(true);
                codeArea.setHighlightCurrentLine(false);
                codeArea.setEditable(false);
                codeArea.setBackground(CODE_BG_COLOR);
                codeArea.setFont(CODE_FONT);
                
                // Try to apply a theme if possible
                try {
                    Theme theme = Theme.load(getClass().getResourceAsStream(
                            "/org/fife/ui/rsyntaxtextarea/themes/default.xml"));
                    theme.apply(codeArea);
                } catch (Exception e) {
                    // If theme loading fails, continue with default styling
                    System.out.println("Theme loading failed: " + e.getMessage());
                }
                
                // Create a scrollable panel for the code with rounded borders
                RTextScrollPane scrollPane = new RTextScrollPane(codeArea);
                scrollPane.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
                scrollPane.setPreferredSize(new Dimension(Math.min(outputPane.getWidth() - 60, 800), 
                                                          Math.min(codeContent.split("\n").length * 18 + 30, 300)));
                
                scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
                scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
                
                // Add some spacing before the code block
                doc.insertString(doc.getLength(), "\n", doc.getStyle("regular"));
                
                // Insert the component into the document
                StyleConstants.setComponent(doc.getStyle("code"), scrollPane);
                doc.insertString(doc.getLength(), " ", doc.getStyle("code"));
                doc.insertString(doc.getLength(), "\n", doc.getStyle("regular"));
            } catch (Exception e) {
                // Fallback if RSyntaxTextArea fails
                System.out.println("RSyntaxTextArea error: " + e.getMessage());
                e.printStackTrace();
                
                // Insert the code as plain text with a different background
                Style codeBlockStyle = doc.addStyle("code_block", doc.getStyle("regular"));
                StyleConstants.setFontFamily(codeBlockStyle, CODE_FONT.getFamily());
                StyleConstants.setFontSize(codeBlockStyle, CODE_FONT.getSize());
                StyleConstants.setBackground(codeBlockStyle, CODE_BG_COLOR);
                StyleConstants.setForeground(codeBlockStyle, new Color(0, 0, 150));
                
                JPanel codePanel = new JPanel(new BorderLayout());
                codePanel.setBackground(CODE_BG_COLOR);
                codePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                
                // Add some spacing before the code block
                doc.insertString(doc.getLength(), "\n", doc.getStyle("regular"));
                
                int startPos = doc.getLength();
                doc.insertString(doc.getLength(), codeContent + "\n", codeBlockStyle);
                doc.setParagraphAttributes(startPos, codeContent.length() + 1, codeBlockStyle, false);
            }
            
            // Handle text after code block
            if (codeEnd + 3 < content.length()) {
                String afterCode = content.substring(codeEnd + 3);
                int startPos = doc.getLength();
                doc.insertString(doc.getLength(), afterCode + "\n\n", doc.getStyle("regular"));
                doc.setParagraphAttributes(startPos, afterCode.length() + 2, panelStyle, false);
            } else {
                // Add extra spacing after the code block if there's no text
                doc.insertString(doc.getLength(), "\n", doc.getStyle("regular"));
            }
        }
    }
    
    public void setOutputText(String text) {
        outputPane.setText("");
        appendToOutput(text);
    }
    
    public String getOutputText() {
        return outputPane.getText();
    }
    
    public JFrame getFrame() {
        return frame;
    }
    
    public void setInputText(String text) {
        inputArea.setText(text);
    }
 
    
    public VoiceService getVoiceService() {
        return voiceService;
    }
    
    /**
     * Updates the conversation sidebar with current conversation list
     * @param conversations List of all conversations
     * @param currentIndex Index of currently active conversation
     */
    public void updateConversationSidebar(List<Conversation> conversations, int currentIndex) {
        // Clear existing conversation list
        conversationListPanel.removeAll();
        
        // Add each conversation as a sidebar item
        for (int i = 0; i < conversations.size(); i++) {
            Conversation conv = conversations.get(i);
            
            // Get a derived title if available (using first user message)
            String derivedTitle = conv.getDerivedTitle();
            if (derivedTitle.equals("New Chat") && !conv.getEntries().isEmpty()) {
                derivedTitle = conv.getDerivedTitle();
            }
            
            // Format conversation timestamp
            String timeInfo = getTimeFormatted(conv.getTimestamp());
            
            // Create and add conversation item
            JPanel item = createConversationItem(derivedTitle, timeInfo, i, i == currentIndex);
            conversationListPanel.add(item);
        }
        
        // Update the UI
        conversationListPanel.revalidate();
        conversationListPanel.repaint();
    }
    
    /**
     * Creates a conversation item for the sidebar
     * @param title Title of the conversation
     * @param time Time information
     * @param index Index in the conversation list
     * @param isActive Whether this is the active conversation
     * @return Panel representing the conversation
     */
    private JPanel createConversationItem(String title, String time, int index, boolean isActive) {
        JPanel item = new JPanel(new BorderLayout(5, 5));
        item.setName("conversation-" + index);
        
        // Set the background color based on whether this is the active conversation
        Color bgColor = isActive ? new Color(220, 220, 235) : SIDEBAR_COLOR;
        item.setBackground(bgColor);
        
        // Add border
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(12, 10, 12, 10)
        ));
        item.setMaximumSize(new Dimension(250, 70));
        
        // Create title and time labels
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(SIDEBAR_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font(SIDEBAR_FONT.getName(), Font.PLAIN, 11));
        timeLabel.setForeground(new Color(120, 120, 120));
        
        // Create delete button
        JLabel deleteButton = new JLabel(FontIcon.of(FontAwesomeSolid.TIMES, 14, new Color(150, 150, 150)));
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.setToolTipText("Delete conversation");
        deleteButton.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        
        // Add action for delete button
        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.removeConversation(index);
                e.consume();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                deleteButton.setIcon(FontIcon.of(FontAwesomeSolid.TIMES, 14, new Color(220, 53, 69)));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                deleteButton.setIcon(FontIcon.of(FontAwesomeSolid.TIMES, 14, new Color(150, 150, 150)));
            }
        });
        
        // Create a panel for the title with the delete button
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(deleteButton, BorderLayout.EAST);
        
        // Add components to the item
        item.add(titlePanel, BorderLayout.CENTER);
        item.add(timeLabel, BorderLayout.SOUTH);
        
        // Add hover effect and click action
        final Color originalBg = bgColor;
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isActive) {
                    item.setBackground(new Color(230, 230, 240));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (!isActive) {
                    item.setBackground(originalBg);
                }
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                // Skip if clicking the delete button
                Component c = SwingUtilities.getDeepestComponentAt(item, e.getX(), e.getY());
                if (c == deleteButton) {
                    return;
                }
                
                // Switch to this conversation
                controller.switchConversation(index);
            }
        });
        
        return item;
    }
    
    /**
     * Formats a timestamp into a readable relative time
     * @param timestamp The timestamp to format
     * @return A string representing the relative time
     */
    private String getTimeFormatted(Date timestamp) {
        // Get current date
        Date now = new Date();
        long diffInMillies = now.getTime() - timestamp.getTime();
        long diffInMinutes = diffInMillies / (60 * 1000);
        long diffInHours = diffInMillies / (60 * 60 * 1000);
        long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);
        
        if (diffInMinutes < 1) {
            return "just now";
        } else if (diffInMinutes < 60) {
            return diffInMinutes + " min ago";
        } else if (diffInHours < 24) {
            return diffInHours + " hours ago";
        } else if (diffInDays < 7) {
            return diffInDays == 1 ? "Yesterday" : diffInDays + " days ago";
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");
            return dateFormat.format(timestamp);
        }
    }
    
    /**
     * Clear the output display
     */
    public void clearOutputDisplay() {
        outputPane.setText("");
        addWelcomeMessage();
    }
}