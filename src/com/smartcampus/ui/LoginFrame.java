package com.smartcampus.ui;

import com.smartcampus.config.DatabaseConnection;
import com.smartcampus.dao.UserDAO;
import com.smartcampus.model.User;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Modern Login Screen with Role-based Fast Access and Database Indicators
 */
public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JLabel dbStatusLabel = new JLabel();
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        super("Smart Campus - System Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 540);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(UITheme.BG_LIGHT);
        main.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Brand Banner
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);

        JLabel logo = new JLabel("🏫 SMART CAMPUS");
        logo.setFont(UITheme.FONT_TITLE);
        logo.setForeground(UITheme.PRIMARY);
        logo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Resource Allocation & Monitoring System");
        subtitle.setFont(UITheme.FONT_SUBTITLE);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        brandPanel.add(logo);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(subtitle);

        // Login Card
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel loginTitle = new JLabel("Sign In to Portal");
        loginTitle.setFont(UITheme.FONT_SUBTITLE);
        loginTitle.setForeground(UITheme.PRIMARY);
        loginTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(UITheme.FONT_BOLD);
        userLabel.setAlignmentX(LEFT_ALIGNMENT);

        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        usernameField.setAlignmentX(LEFT_ALIGNMENT);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(UITheme.FONT_BOLD);
        passLabel.setAlignmentX(LEFT_ALIGNMENT);

        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        passwordField.setAlignmentX(LEFT_ALIGNMENT);

        JButton loginBtn = UITheme.createPrimaryButton("Login to Campus System");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginBtn.setAlignmentX(LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> performLogin());

        // Quick Role buttons for demo
        JLabel demoLabel = new JLabel("Quick Demo Logins:");
        demoLabel.setFont(UITheme.FONT_SMALL);
        demoLabel.setForeground(UITheme.TEXT_MUTED);
        demoLabel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel quickPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        quickPanel.setOpaque(false);
        quickPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnAdmin = createQuickButton("Admin", "admin", "admin123");
        JButton btnFaculty = createQuickButton("Faculty", "sharma", "sharma123");
        JButton btnStudent = createQuickButton("Student", "lucky", "lucky123");

        quickPanel.add(btnAdmin);
        quickPanel.add(btnFaculty);
        quickPanel.add(btnStudent);

        card.add(loginTitle);
        card.add(Box.createVerticalStrut(14));
        card.add(userLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(10));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(16));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(14));
        card.add(demoLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(quickPanel);

        // Database status bar
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        updateDbStatusText();
        dbStatusLabel.setFont(UITheme.FONT_SMALL);

        JButton dbBtn = new JButton("⚙️ Database Settings");
        dbBtn.setFont(UITheme.FONT_SMALL);
        dbBtn.setContentAreaFilled(false);
        dbBtn.setBorderPainted(false);
        dbBtn.setForeground(UITheme.ACCENT);
        dbBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dbBtn.addActionListener(e -> {
            new DatabaseConfigDialog(this).setVisible(true);
            updateDbStatusText();
        });

        footer.add(dbStatusLabel, BorderLayout.WEST);
        footer.add(dbBtn, BorderLayout.EAST);

        main.add(brandPanel);
        main.add(Box.createVerticalStrut(18));
        main.add(card);
        main.add(Box.createVerticalStrut(14));
        main.add(footer);

        getContentPane().add(main, BorderLayout.CENTER);
        
        // Enter key activates login
        getRootPane().setDefaultButton(loginBtn);
        
        // Default to admin
        usernameField.setText("admin");
        passwordField.setText("admin123");
    }

    private JButton createQuickButton(String title, String user, String pass) {
        JButton btn = UITheme.createSecondaryButton(title);
        btn.setFont(UITheme.FONT_SMALL);
        btn.addActionListener(e -> {
            usernameField.setText(user);
            passwordField.setText(pass);
        });
        return btn;
    }

    private void updateDbStatusText() {
        String mode = DatabaseConnection.getConnectionMode();
        if (DatabaseConnection.isUsingMySQL()) {
            dbStatusLabel.setText("🟢 " + mode);
            dbStatusLabel.setForeground(UITheme.SUCCESS);
        } else {
            dbStatusLabel.setText("🟡 " + mode);
            dbStatusLabel.setForeground(UITheme.WARNING);
        }
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", 
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = userDAO.authenticate(username, password);
        if (user != null) {
            dispose();
            MainDashboardFrame dashboard = new MainDashboardFrame(user);
            dashboard.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials! Check your username or password.", 
                    "Authentication Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
