package com.teamname.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class OpenAIService {
    // Perplexity API uses OpenAI-compatible endpoints
    private static final String API_URL = "https://api.perplexity.ai/chat/completions";
    private static final String SUMMARY_API_URL = "https://api.perplexity.ai/chat/completions";
    
    private final String apiKey;
    private final HttpClient httpClient;
    private final Gson gson;
    private boolean useLocalOnly = false; // Option to bypass API calls
    
    public OpenAIService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.gson = new Gson();
    }
    
    public String generateText(String prompt) throws Exception {
        // If local-only mode is enabled or no API key, use local responses
        if (useLocalOnly || apiKey == null || apiKey.isEmpty()) {
            return generateJavaTutorialResponse(prompt);
        }
        
        // Create messages array with system and user messages
        JsonArray messagesArray = new JsonArray();
        
        // Add system message (instructions for the AI)
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "You are a witty, slightly sarcastic Java programming tutor. You're enthusiastic about Java and programming, but have a sense of humor. Keep your responses focused exclusively on Java, conversational in tone, and avoid formal academic style. Use analogies, jokes, and personal touches to make Java concepts clear.");
        messagesArray.add(systemMessage);
        
        // Add user message
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messagesArray.add(userMessage);
        
        // Create the complete request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "sonar-pro"); // Using Sonar Pro model
        requestBody.add("messages", messagesArray);
        requestBody.addProperty("max_tokens", 1000);
        requestBody.addProperty("temperature", 0.8); // Higher temperature for more creativity
        
        // Build HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
        
        try {
            // Send request and get response
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            // Check if request was successful
            if (response.statusCode() != 200) {
                System.err.println("Error response: " + response.body());
                useLocalOnly = true; // Switch to local-only mode for future requests
                return generateJavaTutorialResponse(prompt);
            }
            
            // Parse response
            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
            
            // Extract the message content
            String content = jsonResponse
                .getAsJsonArray("choices")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("message")
                .get("content")
                .getAsString();
            
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return generateJavaTutorialResponse(prompt);
        }
    }
    
    /**
     * Generate a summary of the conversation
     * @param conversationHistory String containing the full conversation history
     * @return Summarized conversation text
     */
    public String generateConversationSummary(String conversationHistory) throws Exception {
        // If no API key is provided, return a simple summary
        if (apiKey == null || apiKey.isEmpty()) {
            return generateLocalSummary(conversationHistory);
        }
        
        // Prepare the summary request
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "sonar-pro");
        
        // Create messages array for summary request
        JsonArray messagesArray = new JsonArray();
        
        // System message to guide summarization
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", 
            "You are a professional summarizer. Generate a concise, clear summary " +
            "of the following conversation. Highlight key topics discussed, " +
            "main points, and any significant insights. Keep the summary " +
            "between 3-5 sentences. Use a professional and objective tone.");
        
        // User message with conversation history
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", 
            "Please summarize the following conversation:\n\n" + 
            conversationHistory);
        
        messagesArray.add(systemMessage);
        messagesArray.add(userMessage);
        
        requestBody.add("messages", messagesArray);
        requestBody.addProperty("max_tokens", 300);
        requestBody.addProperty("temperature", 0.7);
        
        // Build HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUMMARY_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
        
        try {
            // Send request and get response
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            // Check if request was successful
            if (response.statusCode() != 200) {
                System.err.println("Summary API error: " + response.body());
                return generateLocalSummary(conversationHistory);
            }
            
            // Parse response
            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
            
            // Extract summary content
            String summary = jsonResponse
                .getAsJsonArray("choices")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("message")
                .get("content")
                .getAsString();
            
            return summary;
        } catch (Exception e) {
            e.printStackTrace();
            return generateLocalSummary(conversationHistory);
        }
    }
    
    /**
     * Generate a local summary when API is unavailable
     * @param conversationHistory Full conversation text
     * @return A basic summary of the conversation
     */
    private String generateLocalSummary(String conversationHistory) {
        // Basic local summarization strategy
        String[] messages = conversationHistory.split("\n");
        
        // If very few messages, return the entire history
        if (messages.length <= 3) {
            return "Conversation was too short to summarize.\n\n" + conversationHistory;
        }
        
        // Extract first and last few messages
        StringBuilder summary = new StringBuilder("Conversation Summary:\n\n");
        
        // Add context from first message
        summary.append("Started with: ")
               .append(messages[0].length() > 100 ? 
                       messages[0].substring(0, 100) + "..." : 
                       messages[0])
               .append("\n\n");
        
        // Add most recent messages
        summary.append("Recent Discussion Highlights:\n");
        for (int i = Math.max(0, messages.length - 3); i < messages.length; i++) {
            summary.append("- ").append(messages[i]).append("\n");
        }
        
        return summary.toString();
    }
    
    // Enhanced Java-specific educational responses with personality
    public String generateJavaTutorialResponse(String prompt) {
        // Convert prompt to lowercase for easier matching
        String lowercasePrompt = prompt.toLowerCase();
        
        // Check for general greetings
        if (lowercasePrompt.contains("hi") || lowercasePrompt.contains("hello") || 
            lowercasePrompt.contains("hey") || lowercasePrompt.equals("hi") || 
            lowercasePrompt.equals("hello") || lowercasePrompt.equals("hey")) {
            return "Hey there, Java enthusiast! Ready to dive into the wonderful world of curly braces and semicolons? What Java topic can I help you untangle today? I promise I'll try to make it less painful than debugging a NullPointerException.";
        }
        
        // Check for "how are you" type questions
        if (lowercasePrompt.contains("how are you") || lowercasePrompt.contains("how's it going")) {
            return "I'm running at optimal performance today, thanks for asking! Not a single OutOfMemoryError in sight. But enough about my garbage collection—let's talk about Java! What can I help you with? Class hierarchies? Lambda expressions? Or perhaps you'd like to hear my rant about why the substring() method is secretly plotting world domination?";
        }
        
        // Check for "what is java" type questions
        if ((lowercasePrompt.contains("what") || lowercasePrompt.contains("tell")) && 
            lowercasePrompt.contains("java") && !lowercasePrompt.contains("what is java")) {
            return "Ah, Java—the language that promised 'write once, run anywhere' and delivered 'debug everywhere.' But seriously, Java is a robust, object-oriented programming language created by James Gosling at Sun Microsystems (now owned by Oracle) in 1995. It runs on billions of devices worldwide and has survived more tech industry shifts than a cockroach survives apocalypses. What specific aspect of Java would you like me to explain in excruciating detail while making terrible programming puns?";
        }
        
        // Check for "teach me java" type requests
        if (lowercasePrompt.contains("teach") && lowercasePrompt.contains("java")) {
            return "Oh, you want me to teach you Java? Brave soul! I admire your courage. Let's start at the beginning—no, not with 'Hello World,' that's too cliché. Let's start with the fact that Java is like that friend who insists everything must be properly labeled and put in its right place.\n\n" +
                   "Here's your first lesson: In Java, everything lives in classes. It's like an obsessively organized closet where every sock needs its own drawer. Want to print something? You need a class. Want to add two numbers? Class. Want to question your life choices as a programmer? Definitely need a class for that.\n\n" +
                   "Let's write something simple:\n\n" +
                   "```java\npublic class WhyAmIDoingThis {\n    public static void main(String[] args) {\n        System.out.println(\"Because Java pays the bills!\");\n    }\n}\n```\n\n" +
                   "That's your first program! Notice how we needed a whole ceremony just to print one line? Welcome to Java—where verbosity is not a bug, it's a feature! What specific part of Java would you like to learn about next? Variables? Methods? Or should we dive into the existential crisis that is exception handling?";
        }
        
        // Add more specialized responses for Java topics
        if (lowercasePrompt.contains("encapsulation")) {
            return "Ah, encapsulation—Java's way of saying 'mind your own business!' It's essentially the programming equivalent of those annoying childproof caps on medicine bottles.\n\n" +
                   "Encapsulation is all about bundling your data (fields) with the methods that work on that data, then slapping a big 'PRIVATE' sign on the data so nobody can mess with it directly. Instead, they have to use your getters and setters—the bouncers of your class that decide who gets in and who stays out.\n\n" +
                   "Here's a quick example:\n\n" +
                   "```java\npublic class BankAccount {\n    private double balance; // Private data—no touching!\n    \n    // Getter—the polite way to ask about someone's balance\n    public double getBalance() {\n        return balance;\n    }\n    \n    // Setter with validation—like a bouncer checking IDs\n    public void deposit(double amount) {\n        if (amount > 0) {\n            balance += amount;\n        } else {\n            System.out.println(\"Nice try, but no.\");\n        }\n    }\n}\n```\n\n" +
                   "Why bother with all this? Because if you let everyone directly access your fields, next thing you know, someone's setting your bank balance to negative infinity, and then you're explaining to your cat why dinner is canceled. Encapsulation prevents that kind of chaos. It's not just good practice—it's sanity preservation!";
        } 
        else if (lowercasePrompt.contains("inheritance")) {
            return "Inheritance in Java—or as I like to call it, 'Object-Oriented Family Drama.' It's where a child class inherits traits from a parent class, just like how you inherited your mom's nose and your dad's inability to fold a map.\n\n" +
                   "Here's the family dynamic in code:\n\n" +
                   "```java\n// The parent class—think of it as the family patriarch\npublic class Vehicle {\n    protected int wheels;\n    \n    public void honk() {\n        System.out.println(\"BEEP BEEP! Get out of my way!\");\n    }\n}\n\n// The child class—rebellious but still respects the family name\npublic class Car extends Vehicle {\n    private String model;\n    \n    public Car() {\n        this.wheels = 4; // Thanks for the wheels, dad!\n    }\n    \n    public void drift() {\n        System.out.println(\"Tokyo Drift: Java Edition\");\n    }\n}\n```\n\n" +
                   "The beauty here is that a Car can honk() because it inherited that method from Vehicle. It's like getting your parents' Netflix subscription—why create your own when you can just inherit theirs? And unlike real life, in Java you can only inherit from one parent class. Java is strictly a monogamous family affair, though interfaces let you get around this a bit (but that's a story for another time).";
        }
        else if (lowercasePrompt.contains("polymorphism")) {
            return "Polymorphism—the fancy Greek word that makes Java developers sound smart at parties. It basically means 'many forms,' kind of like how water can be liquid, solid, or gas, or how I can be helpful, sarcastic, or completely confused by your code.\n\n" +
                   "In Java terms, it's when different objects respond to the same method call in different ways. It's like yelling 'Hey!' in a room full of people named 'Hey'—they'll all respond, but each in their own unique way.\n\n" +
                   "Here's polymorphism showing off:\n\n" +
                   "```java\n// The zoo of objects\nclass Animal {\n    public void makeSound() {\n        System.out.println(\"*Generic animal noise*\");\n    }\n}\n\nclass Dog extends Animal {\n    @Override\n    public void makeSound() {\n        System.out.println(\"Woof! (Translation: Where's my Java treat?)\");\n    }\n}\n\nclass Cat extends Animal {\n    @Override\n    public void makeSound() {\n        System.out.println(\"Meow! (Translation: Java is inferior to Scratch)\");\n    }\n}\n\n// Using polymorphism\nAnimal myPet = new Dog();\nmyPet.makeSound(); // Outputs: Woof! (not the generic sound)\n```\n\n" +
                   "The real magic is that myPet is declared as an Animal but behaves like a Dog. It's like how I'm technically an AI but sometimes behave like a sleep-deprived Java programmer who's had too much coffee. That's polymorphism for you—full of surprises!";
        }
        else if (lowercasePrompt.contains("interface")) {
            return "Interfaces in Java are like New Year's resolutions—they declare a bunch of methods you promise to implement, but don't actually implement them. It's the ultimate 'I have a great idea, YOU do the work' approach to programming.\n\n" +
                   "Here's an interface in action:\n\n" +
                   "```java\n// The interface—full of promises\npublic interface Programmer {\n    void writeCode();\n    void debugCode();\n    void drinkCoffee();\n    void explainWhyProjectIsDelayed();\n}\n\n// The implementation—where promises meet reality\npublic class JavaDeveloper implements Programmer {\n    private int coffeeLevel = 0;\n    \n    @Override\n    public void writeCode() {\n        System.out.println(\"public static void main(String[] args)... *sigh*\");\n    }\n    \n    @Override\n    public void debugCode() {\n        System.out.println(\"It works on my machine!\");\n    }\n    \n    @Override\n    public void drinkCoffee() {\n        coffeeLevel++;\n        System.out.println(\"Coffee level: \" + coffeeLevel);\n    }\n    \n    @Override\n    public void explainWhyProjectIsDelayed() {\n        System.out.println(\"Well, you see, we had an unexpected NullPointerException...\");\n    }\n}\n```\n\n" +
                   "The best part about interfaces? Unlike real life, you can implement multiple interfaces in Java. It's like being able to sign up for gym membership, piano lessons, AND a Spanish course, all without the guilt of abandoning them all by February. Impressive, right?";
        }
        else if (lowercasePrompt.contains("what is java")) {
            return "Ah, Java! Not the coffee, not the island, but the programming language that's been fueling developer nightmares—I mean, dreams—since 1995!\n\n" +
                   "Java is an object-oriented programming language created by James Gosling at Sun Microsystems (now owned by Oracle). It was designed with the mission statement of 'Write Once, Run Anywhere,' which is developer speak for 'Debug Everywhere.'\n\n" +
                   "Here's what makes Java special:\n" +
                   "- It's object-oriented, which means everything is neatly wrapped in classes like presents nobody wants to open because the wrapping is too pretty\n" +
                   "- It runs on a Java Virtual Machine (JVM), allowing it to work on any device that has a JVM (hence the 'run anywhere' part)\n" +
                   "- It has automatic memory management (garbage collection), saving you from the existential crisis of manual memory allocation\n" +
                   "- It's strongly typed, meaning it will absolutely refuse to add a string to an integer without explicit permission, like a stubborn grammar teacher\n\n" +
                   "Java powers everything from Android apps to enterprise systems to those annoying update prompts that pop up at the worst possible times. It's like the cockroach of programming languages—it's survived everything and will probably outlive us all.\n\n" +
                   "Want to see what Java looks like in the wild? Here's a small taste:\n\n" +
                   "```java\npublic class ThisIsJava {\n    public static void main(String[] args) {\n        System.out.println(\"Yes, I really need this many brackets.\");\n    }\n}\n```\n\n" +
                   "Fascinating creature, isn't it? What else would you like to know about this resilient species of programming language?";
        }
        else {
            // Generic Java learning response
            return "Ah, another Java question! I live for these moments. Java—where semicolons rule and curly braces multiply like rabbits.\n\n" +
                   "Java is that reliable friend who's been around since 1995 but still knows all the cool modern tricks. Object-oriented to its core, Java loves to organize everything into classes, enforce strong typing (it's very judgmental about your variable types), and it refuses to let you get away with memory leaks thanks to its garbage collector (the unsung hero of programming).\n\n" +
                   "If you're trying to learn Java, you're in for a treat (and by 'treat,' I mean countless hours debugging NullPointerExceptions). But don't worry, I'm here to make this journey less painful! What specific Java concept can I illuminate for you today? Encapsulation? Inheritance? Or perhaps you'd like to hear about the thrilling world of exception handling? I promise to keep it interesting—or at least more interesting than watching paint dry.";
        }
    }
}

