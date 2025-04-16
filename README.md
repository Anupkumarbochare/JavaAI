# AI Programming Tutor

An intelligent desktop application that helps users learn programming concepts through conversation with an AI tutor. This application demonstrates various data structures and algorithms in a practical, interactive environment.

## 🌟 Features

- **Interactive Chat Interface**: Engage in conversations with an AI programming tutor
- **Voice Input/Output**: Speak to the application and hear responses
- **Conversation Management**: Save, load, and organize your learning sessions
- **Priority-based Message Handling**: Set priority for your questions
- **Code Syntax Highlighting**: View Java code examples with professional highlighting
- **Topic Search**: Search for specific programming topics in your conversation history
- **Conversation Export**: Download your conversations as PDF files
- **Conversation Summarization**: Get concise summaries of your learning sessions

## 📋 Data Structures Implementation

This application demonstrates the implementation of various data structures:

- **Stack**: Used for conversation history management
- **HashMap**: Implemented for response caching
- **Binary Search Tree (BST)**: Used for organizing and searching programming topics
- **Priority Queue**: Handles messages based on priority levels
- **QuickSort**: Used for sorting conversation history

## 🛠️ Technical Architecture

The application follows a well-structured MVC (Model-View-Controller) architecture:

```
com.teamname
├── algorithms
│   ├── sorting
│   │   └── QuickSort.java
│   └── utils
│       └── AlgorithmAnalyzer.java
├── api
│   └── OpenAIService.java
├── controller
│   └── ChatController.java
├── datastructures
│   ├── BinarySearchTree.java
│   ├── HashMap.java
│   ├── PriorityQueue.java
│   └── Stack.java
├── gui
│   ├── ChatUI.java
│   └── MainApplication.java
├── model
│   ├── ConversationEntry.java
│   ├── ConversationManager.java
│   ├── Message.java
│   └── Topic.java
└── service
    ├── DatabaseService.java
    └── VoiceService.java
```

## 🚀 Getting Started

### Prerequisites

- Java Development Kit (JDK) 11 or later
- Maven or Gradle (for dependency management)

### Dependencies

- Perplexity API (compatible with OpenAI API format) for AI responses
- iText for PDF generation
- FreeTTS for voice synthesis
- RSyntaxTextArea for code highlighting
- Ikonli for modern icons
- FlatLaf for modern UI look and feel
- SQLite JDBC for local database storage

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/ai-programming-tutor.git
   ```

2. Navigate to the project directory:
   ```bash
   cd ai-programming-tutor
   ```

3. Build the project:
   ```bash
   # With Maven
   mvn clean install
   
   # Or with Gradle
   gradle build
   ```

4. Run the application:
   ```bash
   java -jar target/ai-programming-tutor.jar
   ```

### API Key Configuration

The application uses Perplexity AI API for generating responses. To use this feature:

1. Get an API key from [Perplexity AI](https://www.perplexity.ai/)
2. Set the environment variable:
   ```bash
   export PERPLEXITY_API_KEY=your_api_key_here
   ```

## 📝 Usage Guide

1. **Starting a Conversation**: Type your programming question in the input field and press Enter or click the send button.

2. **Setting Priority**: Choose a priority level (High, Medium, Low) from the dropdown menu before sending your message.

3. **Using Voice Features**:
   - Click "Voice Input" to speak your questions
   - Toggle "Voice Output" to have the AI read responses aloud
   - Use "Stop Speech" to interrupt voice output

4. **Managing Conversations**:
   - Click "+ New Chat" to start a fresh conversation
   - Click on any conversation in the sidebar to switch between them
   - Use "×" to delete a conversation

5. **Using Tools**:
   - "Clear": Clear the current conversation
   - "Export PDF": Save the conversation as a PDF file
   - "Summarize": Generate a concise summary of the conversation
   - "History": View conversation history in chronological order
   - "Search": Search for programming topics in your conversations
   - "Sort": Sort your conversation history alphabetically

## 🧪 How Data Structures Are Used

- **Stack**: Used in `ChatController` to manage conversation history, allowing users to track the flow of conversation.
- **HashMap**: Implemented in `ChatController` for caching responses to frequently asked questions.
- **Binary Search Tree**: Used for organizing programming topics and enabling efficient search functionality.
- **Priority Queue**: Messages are processed based on priority, demonstrating how prioritization works in queues.
- **QuickSort**: Applied to sort conversation history alphabetically, showing efficient sorting algorithms.

## 📊 Performance Analysis

The `AlgorithmAnalyzer` class provides utilities to measure and compare algorithm performance:

- Execution time measurement in milliseconds
- Algorithm comparison with speedup ratio calculation
- Performance visualization for sorting algorithms

## 🤝 Contributing

Contributions are welcome! Here's how you can contribute:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add some amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Syntax highlighting powered by RSyntaxTextArea
- Modern UI components from FlatLaf
- Icons provided by Ikonli
- Text-to-Speech functionality via FreeTTS
