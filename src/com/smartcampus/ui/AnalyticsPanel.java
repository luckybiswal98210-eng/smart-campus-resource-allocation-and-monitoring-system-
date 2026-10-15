package com.smartcampus.ui;

import com.smartcampus.dao.AllocationDAO;
import com.smartcampus.dao.MaintenanceAlertDAO;
import com.smartcampus.dao.ResourceDAO;
import com.smartcampus.dao.TelemetryDAO;
import com.smartcampus.model.Allocation;
import com.smartcampus.model.Resource;
import com.smartcampus.model.User;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analytics and Campus Reporting Panel with Visual Charts & Metrics
 */
public class AnalyticsPanel extends JPanel {

    private final User currentUser;
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final AllocationDAO allocationDAO = new AllocationDAO();
    private final TelemetryDAO telemetryDAO = new TelemetryDAO();
    private final MaintenanceAlertDAO alertDAO = new MaintenanceAlertDAO();

    private final JPanel statsContent = new JPanel();

    public AnalyticsPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        statsContent.setLayout(new BoxLayout(statsContent, BoxLayout.Y_AXIS));
        statsContent.setOpaque(false);

        JScrollPane scroll = new JScrollPane(statsContent);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        // Bottom Export Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);

        JButton exportBtn = UITheme.createPrimaryButton("📄 Export Campus Audit Report");
        exportBtn.setPreferredSize(new Dimension(230, 36));
        exportBtn.addActionListener(e -> exportReport());

        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh Analytics");
        refreshBtn.addActionListener(e -> refreshMetrics());

        bottomBar.add(exportBtn);
        bottomBar.add(refreshBtn);

        add(scroll, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        refreshMetrics();
    }

    public void refreshMetrics() {
        statsContent.removeAll();

        List<Resource> resources = resourceDAO.getAllResources();
        List<Allocation> allocations = allocationDAO.getAllAllocations();
        int pendingAlerts = alertDAO.getActiveAlertCount();
        double currentPower = telemetryDAO.getTotalCurrentPowerDraw();
        int currentOccupancy = telemetryDAO.getTotalCurrentOccupancy();

        // 1. Top KPI Cards
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        kpiGrid.setOpaque(false);

        int totalRes = resources.size();
        long availableCount = resources.stream().filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus())).count();
        long allocatedCount = resources.stream().filter(r -> "ALLOCATED".equalsIgnoreCase(r.getStatus())).count();

        kpiGrid.add(createKpiCard("Total Campus Assets", String.valueOf(totalRes), "Active Resources", UITheme.PRIMARY));
        kpiGrid.add(createKpiCard("Available for Booking", String.valueOf(availableCount), "Ready for use", UITheme.SUCCESS));
        kpiGrid.add(createKpiCard("Currently Allocated", String.valueOf(allocatedCount), "In session", UITheme.ACCENT));
        kpiGrid.add(createKpiCard("Maintenance Alerts", String.valueOf(pendingAlerts), "Requires check", pendingAlerts > 0 ? UITheme.WARNING : UITheme.SUCCESS));

        statsContent.add(kpiGrid);
        statsContent.add(Box.createVerticalStrut(20));

        // 2. Resource Category Breakdown & Utilization Cards
        JPanel splitGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        splitGrid.setOpaque(false);

        // Category breakdown card
        JPanel categoryCard = UITheme.createCardPanel();
        categoryCard.setLayout(new BoxLayout(categoryCard, BoxLayout.Y_AXIS));

        JLabel catTitle = new JLabel("Facility Inventory by Category");
        catTitle.setFont(UITheme.FONT_SUBTITLE);
        catTitle.setForeground(UITheme.PRIMARY);
        categoryCard.add(catTitle);
        categoryCard.add(Box.createVerticalStrut(12));

        Map<String, Integer> catCounts = new HashMap<>();
        for (Resource r : resources) {
            catCounts.put(r.getCategory(), catCounts.getOrDefault(r.getCategory(), 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : catCounts.entrySet()) {
            categoryCard.add(createProgressRow(entry.getKey(), entry.getValue(), totalRes, UITheme.ACCENT));
            categoryCard.add(Box.createVerticalStrut(8));
        }

        // Energy & Smart Automation Summary Card
        JPanel energyCard = UITheme.createCardPanel();
        energyCard.setLayout(new BoxLayout(energyCard, BoxLayout.Y_AXIS));

        JLabel energyTitle = new JLabel("Energy & Sustainability Footprint");
        energyTitle.setFont(UITheme.FONT_SUBTITLE);
        energyTitle.setForeground(UITheme.PRIMARY);
        energyCard.add(energyTitle);
        energyCard.add(Box.createVerticalStrut(12));

        JLabel pLabel = new JLabel("• Current Real-Time Power Draw: " + currentPower + " kW");
        pLabel.setFont(UITheme.FONT_BODY);
        JLabel oLabel = new JLabel("• Live Active Occupancy: " + currentOccupancy + " Persons");
        oLabel.setFont(UITheme.FONT_BODY);
        JLabel aLabel = new JLabel("• Total Bookings Handled: " + allocations.size() + " reservation slots");
        aLabel.setFont(UITheme.FONT_BODY);
        JLabel eLabel = new JLabel("• Automated Eco-Policy: Active (HVAC auto-regulates based on occupancy)");
        eLabel.setFont(UITheme.FONT_BODY);

        energyCard.add(pLabel);
        energyCard.add(Box.createVerticalStrut(6));
        energyCard.add(oLabel);
        energyCard.add(Box.createVerticalStrut(6));
        energyCard.add(aLabel);
        energyCard.add(Box.createVerticalStrut(6));
        energyCard.add(eLabel);

        splitGrid.add(categoryCard);
        splitGrid.add(energyCard);

        statsContent.add(splitGrid);
        statsContent.revalidate();
        statsContent.repaint();
    }

    private JPanel createKpiCard(String title, String val, String subtitle, Color color) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        JLabel valLbl = new JLabel(val);
        valLbl.setFont(UITheme.FONT_METRIC);
        valLbl.setForeground(color);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(UITheme.FONT_SMALL);
        subLbl.setForeground(UITheme.TEXT_MUTED);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valLbl);
        card.add(Box.createVerticalStrut(2));
        card.add(subLbl);
        return card;
    }

    private JPanel createProgressRow(String label, int val, int total, Color color) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        int percent = (total > 0) ? (val * 100 / total) : 0;
        JLabel nameLbl = new JLabel(label + " (" + val + ")");
        nameLbl.setFont(UITheme.FONT_SMALL);
        nameLbl.setPreferredSize(new Dimension(180, 20));

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(percent);
        bar.setForeground(color);
        bar.setStringPainted(true);
        bar.setString(percent + "%");
        bar.setPreferredSize(new Dimension(150, 18));

        row.add(nameLbl, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        return row;
    }

    private void exportReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Smart_Campus_Audit_Report.txt"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("======================================================================\n");
                writer.write("SMART CAMPUS RESOURCE ALLOCATION & MONITORING AUDIT REPORT\n");
                writer.write("Generated at: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
                writer.write("Generated by: " + currentUser.getFullName() + " (" + currentUser.getRole() + ")\n");
                writer.write("======================================================================\n\n");

                List<Resource> resources = resourceDAO.getAllResources();
                writer.write("1. CAMPUS RESOURCE INVENTORY (Total: " + resources.size() + "):\n");
                for (Resource r : resources) {
                    writer.write(String.format("  - [%d] %-35s | %-16s | %-10s | Cap: %d | Power: %.1f kW\n",
                            r.getResourceId(), r.getName(), r.getCategory(), r.getStatus(), r.getCapacity(), r.getPowerRatingKw()));
                }

                writer.write("\n2. ALLOCATIONS & RESERVATIONS:\n");
                List<Allocation> allocs = allocationDAO.getAllAllocations();
                for (Allocation a : allocs) {
                    writer.write(String.format("  - Booking #%d: Resource %s by %s on %s [%s - %s] -> %s\n",
                            a.getAllocationId(), a.getResourceName(), a.getUserName(), a.getBookingDate(), 
                            a.getStartTime(), a.getEndTime(), a.getStatus()));
                }

                writer.write("\n3. REAL-TIME ENERGY TELEMETRY:\n");
                writer.write("  - Total Active Power Draw: " + telemetryDAO.getTotalCurrentPowerDraw() + " kW\n");
                writer.write("  - Total Campus Occupancy: " + telemetryDAO.getTotalCurrentOccupancy() + " Persons\n");
                writer.write("  - Pending Maintenance Alerts: " + alertDAO.getActiveAlertCount() + "\n\n");
                writer.write("======================================================================\n");
                writer.write("END OF SMART CAMPUS REPORT\n");

                JOptionPane.showMessageDialog(this, "Report exported successfully to:\n" + file.getAbsolutePath(), 
                        "Report Generated", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to write report file: " + ex.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
