package com.smartcampus.ui;

import com.smartcampus.dao.MaintenanceAlertDAO;
import com.smartcampus.dao.ResourceDAO;
import com.smartcampus.model.MaintenanceAlert;
import com.smartcampus.model.Resource;
import com.smartcampus.model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel for managing hardware alerts and facility maintenance tickets
 */
public class MaintenanceAlertPanel extends JPanel {

    private final User currentUser;
    private final MaintenanceAlertDAO alertDAO = new MaintenanceAlertDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();

    private final DefaultTableModel tableModel;
    private final JTable alertTable;

    public MaintenanceAlertPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top Header / Bar
        JPanel topPanel = UITheme.createCardPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 5));

        JButton addTicketBtn = UITheme.createPrimaryButton("+ Report Issue");
        addTicketBtn.setPreferredSize(new Dimension(160, 34));
        addTicketBtn.addActionListener(e -> showReportDialog());

        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadData());

        topPanel.add(addTicketBtn);
        topPanel.add(refreshBtn);

        // Center Table
        String[] cols = {"Ticket ID", "Facility / Resource", "Issue Description", "Severity", "Reported Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        alertTable = new JTable(tableModel);
        UITheme.styleTable(alertTable);
        alertTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        alertTable.getColumnModel().getColumn(1).setPreferredWidth(210);
        alertTable.getColumnModel().getColumn(2).setPreferredWidth(320);
        alertTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        alertTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        alertTable.getColumnModel().getColumn(5).setPreferredWidth(110);

        JScrollPane scrollPane = new JScrollPane(alertTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        // Bottom Actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setOpaque(false);

        JButton resolveBtn = UITheme.createSecondaryButton("✅ Mark as Resolved");
        resolveBtn.addActionListener(e -> {
            int row = alertTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a ticket to resolve.");
                return;
            }
            int ticketId = (int) tableModel.getValueAt(row, 0);
            String status = (String) tableModel.getValueAt(row, 5);
            if ("RESOLVED".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "Ticket is already marked as resolved.");
                return;
            }

            if (alertDAO.resolveAlert(ticketId)) {
                JOptionPane.showMessageDialog(this, "Ticket #" + ticketId + " has been resolved!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to resolve ticket.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(resolveBtn);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadData();
    }

    public void loadData() {
        tableModel.setRowCount(0);
        List<MaintenanceAlert> list = alertDAO.getAllAlerts();
        for (MaintenanceAlert a : list) {
            tableModel.addRow(new Object[]{
                    a.getAlertId(),
                    a.getResourceName() != null ? a.getResourceName() : "Facility #" + a.getResourceId(),
                    a.getIssue(),
                    a.getSeverity(),
                    a.getReportedDate().toString(),
                    a.isResolved() ? "RESOLVED" : "PENDING ACTION"
            });
        }
    }

    private void showReportDialog() {
        JDialog dialog = new JDialog((JFrame) null, "Report Campus Equipment / Facility Issue", true);
        dialog.setSize(480, 360);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 14));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        List<Resource> resources = resourceDAO.getAllResources();
        JComboBox<Resource> resourceBox = new JComboBox<>(resources.toArray(new Resource[0]));
        JTextField issueField = new JTextField();
        JComboBox<String> severityBox = new JComboBox<>(new String[]{"LOW", "MEDIUM", "HIGH", "CRITICAL"});

        panel.add(new JLabel("Select Resource:"));
        panel.add(resourceBox);
        panel.add(new JLabel("Issue Description:"));
        panel.add(issueField);
        panel.add(new JLabel("Severity Level:"));
        panel.add(severityBox);
        panel.add(new JLabel("Reported By:"));
        panel.add(new JLabel(currentUser.getFullName()));

        JButton submitBtn = UITheme.createPrimaryButton("Submit Ticket");
        submitBtn.addActionListener(e -> {
            Resource selRes = (Resource) resourceBox.getSelectedItem();
            String issue = issueField.getText().trim();
            if (selRes == null || issue.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in the issue description.");
                return;
            }

            MaintenanceAlert alert = new MaintenanceAlert();
            alert.setResourceId(selRes.getResourceId());
            alert.setIssue(issue);
            alert.setSeverity((String) severityBox.getSelectedItem());
            alert.setReportedDate(Date.valueOf(LocalDate.now()));
            alert.setResolved(false);

            if (alertDAO.addAlert(alert)) {
                // If severity is critical, set resource to MAINTENANCE
                if ("CRITICAL".equalsIgnoreCase(alert.getSeverity())) {
                    resourceDAO.updateStatus(selRes.getResourceId(), "MAINTENANCE");
                }
                JOptionPane.showMessageDialog(dialog, "Maintenance ticket submitted successfully!");
                dialog.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to submit ticket.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(panel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(submitBtn);
        wrapper.add(btnPanel, BorderLayout.SOUTH);

        dialog.getContentPane().add(wrapper);
        dialog.setVisible(true);
    }
}
