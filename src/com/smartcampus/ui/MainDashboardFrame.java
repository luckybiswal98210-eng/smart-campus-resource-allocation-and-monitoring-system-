package com.smartcampus.ui;

import com.smartcampus.config.DatabaseConnection;
import com.smartcampus.model.User;
import com.smartcampus.service.SensorSimulator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main application dashboard containing all campus management panels
 */
public class MainDashboardFrame extends JFrame {

    private final User currentUser;
    private final JLabel clockLabel = new JLabel();
    private final JLabel dbStatusLabel = new JLabel();

    private ResourcePanel resourcePanel;
    private AllocationPanel allocationPanel;
    private LiveMonitoringPanel liveMonitoringPanel;
    private MaintenanceAlertPanel alertPanel;
    private AnalyticsPanel analyticsPanel;

    public MainDashboardFrame(User user) {
        super("Smart Campus Resource Allocation & Monitoring System - " + user.getFullName());
        this.currentUser = user;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1020, 680));
        setLocationRelativeTo(null);

        initUI();

        // Graceful exit on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_LIGHT);

        // Header
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        // Brand on Left
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.setOpaque(false);
        JLabel title = new JLabel("🏫 SMART CAMPUS");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("| Resource Allocation & IoT Telemetry System");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(new Color(203, 213, 225)); // Slate 300

        brand.add(title);
        brand.add(sub);

        // Right Info Panel (Clock, DB Status, User Badge, Logout)
        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightInfo.setOpaque(false);

        clockLabel.setFont(UITheme.FONT_SMALL);
        clockLabel.setForeground(new Color(241, 245, 249));
        startClock();

        updateDbBadge();
        dbStatusLabel.setFont(UITheme.FONT_SMALL);
        dbStatusLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dbStatusLabel.setToolTipText("Click to view or edit MySQL connection settings");
        dbStatusLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new DatabaseConfigDialog(MainDashboardFrame.this).setVisible(true);
                updateDbBadge();
            }
        });

        JLabel userPill = new JLabel("👤 " + currentUser.getFullName() + " [" + currentUser.getRole() + "]");
        userPill.setFont(UITheme.FONT_BOLD);
        userPill.setForeground(UITheme.ACCENT);

        JButton logoutBtn = UITheme.createSecondaryButton("Logout");
        logoutBtn.setPreferredSize(new Dimension(85, 28));
        logoutBtn.addActionListener(e -> logout());

        rightInfo.add(clockLabel);
        rightInfo.add(dbStatusLabel);
        rightInfo.add(userPill);
        rightInfo.add(logoutBtn);

        header.add(brand, BorderLayout.WEST);
        header.add(rightInfo, BorderLayout.EAST);

        // Tabbed Pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.setBackground(Color.WHITE);

        analyticsPanel = new AnalyticsPanel(currentUser);
        resourcePanel = new ResourcePanel(currentUser);
        allocationPanel = new AllocationPanel(currentUser);
        liveMonitoringPanel = new LiveMonitoringPanel(currentUser);
        alertPanel = new MaintenanceAlertPanel(currentUser);

        tabs.addTab("📊 Overview & Analytics", analyticsPanel);
        tabs.addTab("🏛️ Campus Resources", resourcePanel);
        tabs.addTab("📅 Allocations & Bookings", allocationPanel);
        tabs.addTab("⚡ IoT Live Monitoring", liveMonitoringPanel);
        tabs.addTab("⚠️ Maintenance Tickets", alertPanel);

        // Refresh panel data on tab click
        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 0) analyticsPanel.refreshMetrics();
            else if (idx == 1) resourcePanel.loadData();
            else if (idx == 2) allocationPanel.loadData();
            else if (idx == 3) liveMonitoringPanel.loadTelemetryData();
            else if (idx == 4) alertPanel.loadData();
        });

        root.add(header, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);

        getContentPane().add(root);
    }

    private void updateDbBadge() {
        if (DatabaseConnection.isUsingMySQL()) {
            dbStatusLabel.setText("🟢 MySQL Server (Online)");
            dbStatusLabel.setForeground(new Color(134, 239, 172)); // Green
        } else {
            dbStatusLabel.setText("🟡 Local Offline Mode (SQLite)");
            dbStatusLabel.setForeground(new Color(253, 224, 71)); // Yellow
        }
    }

    private void startClock() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  HH:mm:ss");
        Timer timer = new Timer(1000, e -> clockLabel.setText(LocalDateTime.now().format(dtf)));
        timer.setInitialDelay(0);
        timer.start();
    }

    private void logout() {
        int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out?", 
                "Sign Out", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            SensorSimulator.getInstance().stop();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void confirmExit() {
        int opt = JOptionPane.showConfirmDialog(this, "Exit Smart Campus Application?", 
                "Confirm Exit", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            SensorSimulator.getInstance().stop();
            DatabaseConnection.close();
            System.exit(0);
        }
    }
}
