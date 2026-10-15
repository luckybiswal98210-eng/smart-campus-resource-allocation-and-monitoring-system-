package com.smartcampus.ui;

import com.smartcampus.config.DatabaseConnection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * Modern modal dialog for configuring MySQL JDBC connection credentials
 */
public class DatabaseConfigDialog extends JDialog {

    private final JTextField hostField = new JTextField(DatabaseConnection.getDbHost(), 15);
    private final JTextField portField = new JTextField(DatabaseConnection.getDbPort(), 6);
    private final JTextField nameField = new JTextField(DatabaseConnection.getDbName(), 15);
    private final JTextField userField = new JTextField(DatabaseConnection.getDbUser(), 15);
    private final JPasswordField passField = new JPasswordField(DatabaseConnection.getDbPassword(), 15);
    private final JLabel statusLabel = new JLabel("Current Mode: " + DatabaseConnection.getConnectionMode());

    public DatabaseConfigDialog(JFrame parent) {
        super(parent, "Database Configuration (MySQL / JDBC)", true);
        setSize(480, 420);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        content.setBackground(Color.WHITE);

        JLabel title = new JLabel("MySQL Database Settings");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.PRIMARY);

        JLabel subtitle = new JLabel("Configure MySQL Server credentials or use offline mode.");
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        statusLabel.setFont(UITheme.FONT_BOLD);
        statusLabel.setForeground(DatabaseConnection.isUsingMySQL() ? UITheme.SUCCESS : UITheme.WARNING);

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        form.add(new JLabel("MySQL Host:"));
        form.add(hostField);

        form.add(new JLabel("Port:"));
        form.add(portField);

        form.add(new JLabel("Database Name:"));
        form.add(nameField);

        form.add(new JLabel("Username:"));
        form.add(userField);

        form.add(new JLabel("Password:"));
        form.add(passField);

        // Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(Color.WHITE);

        JButton testBtn = UITheme.createSecondaryButton("Test Connection");
        testBtn.addActionListener(e -> {
            String host = hostField.getText().trim();
            String port = portField.getText().trim();
            String name = nameField.getText().trim();
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());

            boolean ok = DatabaseConnection.testMySqlConnection(host, port, name, user, pass);
            if (ok) {
                JOptionPane.showMessageDialog(this, " Successfully connected to MySQL Server (" + host + ":" + port + ")!", 
                        "Connection Succeeded", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "❌ Could not connect to MySQL Server.\nPlease verify if MySQL service is started and password is correct.", 
                        "Connection Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton saveBtn = UITheme.createPrimaryButton("Save & Connect");
        saveBtn.addActionListener(e -> {
            String host = hostField.getText().trim();
            String port = portField.getText().trim();
            String name = nameField.getText().trim();
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());

            try {
                DatabaseConnection.reconnect(host, port, name, user, pass);
                JOptionPane.showMessageDialog(this, "Connected! Mode: " + DatabaseConnection.getConnectionMode(), 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UITheme.createSecondaryButton("Close");
        cancelBtn.addActionListener(e -> dispose());

        actionPanel.add(testBtn);
        actionPanel.add(saveBtn);
        actionPanel.add(cancelBtn);

        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(10));
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(form);
        content.add(Box.createVerticalStrut(15));
        content.add(actionPanel);

        getContentPane().add(content, BorderLayout.CENTER);
    }
}
