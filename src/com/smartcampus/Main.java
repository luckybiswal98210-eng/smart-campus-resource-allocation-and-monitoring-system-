package com.smartcampus;

import com.smartcampus.config.DatabaseConnection;
import com.smartcampus.ui.LoginFrame;
import com.smartcampus.ui.UITheme;

import javax.swing.SwingUtilities;

/**
 * Entry point for Smart Campus Resource Allocation and Monitoring System
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("   SMART CAMPUS RESOURCE ALLOCATION & MONITORING SYSTEM (Java SE/JDBC) ");
        System.out.println("======================================================================");

        // Apply modern UI styling
        UITheme.applyTheme();

        // Initialize Database connection in background/startup
        try {
            System.out.println("🔌 Connecting to database...");
            DatabaseConnection.getConnection();
            System.out.println(" Database initialized successfully. Mode: " + DatabaseConnection.getConnectionMode());
        } catch (Exception e) {
            System.err.println("Database initialization notice: " + e.getMessage());
        }

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
