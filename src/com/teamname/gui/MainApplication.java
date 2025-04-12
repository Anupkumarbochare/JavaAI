package com.teamname.gui;

import com.teamname.controller.ChatController;
import com.teamname.model.ConversationManager;
import com.teamname.service.DatabaseService;

import javax.swing.SwingUtilities;

public class MainApplication {

	public static void main(String[] args) {
	    System.setProperty("awt.useSystemAAFontSettings", "on");
	    System.setProperty("swing.aatext", "true");

	    SwingUtilities.invokeLater(() -> {
	        try {
	            ChatController controller = new ChatController();
	            ChatUI ui = new ChatUI(controller);
	            controller.setUI(ui);

	            //  Save all conversations on exit
	            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
	                if (controller.getConversationManager() != null) {
	                    controller.getConversationManager().saveAllConversations();
	                    System.out.println("✅ All conversations saved on exit.");
	                }
	            }));

	            System.out.println("✅ Application started successfully");
	        } catch (Exception e) {
	            System.err.println("Error starting application: " + e.getMessage());
	            e.printStackTrace();
	        }
	    });
	}}
