package com.smartcampus.ui;

import com.smartcampus.dao.AllocationDAO;
import com.smartcampus.dao.ResourceDAO;
import com.smartcampus.model.Allocation;
import com.smartcampus.model.Resource;
import com.smartcampus.model.User;
import com.smartcampus.service.AllocationService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for managing room and equipment bookings with conflict prevention
 */
public class AllocationPanel extends JPanel {

    private final User currentUser;
    private final AllocationDAO allocationDAO = new AllocationDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final AllocationService allocationService = new AllocationService();

    private final DefaultTableModel tableModel;
    private final JTable allocationTable;
    private final JCheckBox myBookingsCheck = new JCheckBox("Show only my reservations");

    public AllocationPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top Filter Bar
        JPanel topPanel = UITheme.createCardPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 5));

        myBookingsCheck.setFont(UITheme.FONT_BOLD);
        myBookingsCheck.setOpaque(false);
        myBookingsCheck.addActionListener(e -> loadData());

        JButton newBookingBtn = UITheme.createPrimaryButton("+ Request New Allocation");
        newBookingBtn.setPreferredSize(new Dimension(190, 34));
        newBookingBtn.addActionListener(e -> showBookingDialog());

        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadData());

        topPanel.add(newBookingBtn);
        topPanel.add(myBookingsCheck);
        topPanel.add(refreshBtn);

        // Center Table
        String[] cols = {"ID", "Resource Name", "Category", "Requested By", "Purpose", "Date", "Start Time", "End Time", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        allocationTable = new JTable(tableModel);
        UITheme.styleTable(allocationTable);
        allocationTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        allocationTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        allocationTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        allocationTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        allocationTable.getColumnModel().getColumn(4).setPreferredWidth(210);
        allocationTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        allocationTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        allocationTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        allocationTable.getColumnModel().getColumn(8).setPreferredWidth(95);

        JScrollPane scrollPane = new JScrollPane(allocationTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        // Bottom Actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setOpaque(false);

        JButton releaseBtn = UITheme.createSecondaryButton("✅ Release / Mark Completed");
        releaseBtn.addActionListener(e -> {
            int row = allocationTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select an allocation to release.");
                return;
            }
            int allocId = (int) tableModel.getValueAt(row, 0);
            String status = (String) tableModel.getValueAt(row, 8);
            if ("COMPLETED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "This allocation is already " + status + ".");
                return;
            }
            // Find resource id
            int resourceId = findResourceIdForAllocation(allocId);
            if (allocationService.releaseResource(allocId, resourceId)) {
                JOptionPane.showMessageDialog(this, "Resource successfully released and marked Available!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update allocation.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UITheme.createDangerButton("❌ Cancel Reservation");
        cancelBtn.addActionListener(e -> {
            int row = allocationTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select an allocation to cancel.");
                return;
            }
            int allocId = (int) tableModel.getValueAt(row, 0);
            int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel this booking?", 
                    "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                int resourceId = findResourceIdForAllocation(allocId);
                if (allocationService.cancelAllocation(allocId, resourceId)) {
                    JOptionPane.showMessageDialog(this, "Allocation cancelled.");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to cancel allocation.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        bottomPanel.add(releaseBtn);
        bottomPanel.add(cancelBtn);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadData();
    }

    public void loadData() {
        tableModel.setRowCount(0);
        List<Allocation> list;
        if (myBookingsCheck.isSelected()) {
            list = allocationDAO.getUserAllocations(currentUser.getUserId());
        } else {
            list = allocationDAO.getAllAllocations();
        }

        for (Allocation a : list) {
            tableModel.addRow(new Object[]{
                    a.getAllocationId(),
                    a.getResourceName() != null ? a.getResourceName() : "Resource #" + a.getResourceId(),
                    a.getResourceCategory() != null ? a.getResourceCategory() : "General",
                    a.getUserName() != null ? a.getUserName() : "User #" + a.getUserId(),
                    a.getPurpose(),
                    a.getBookingDate().toString(),
                    a.getStartTime().toString(),
                    a.getEndTime().toString(),
                    a.getStatus()
            });
        }
    }

    private int findResourceIdForAllocation(int allocId) {
        List<Allocation> all = allocationDAO.getAllAllocations();
        for (Allocation a : all) {
            if (a.getAllocationId() == allocId) return a.getResourceId();
        }
        return 0;
    }

    private void showBookingDialog() {
        JDialog dialog = new JDialog((JFrame) null, "New Campus Resource Allocation", true);
        dialog.setSize(520, 420);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        List<Resource> resources = resourceDAO.getAllResources();
        JComboBox<Resource> resourceBox = new JComboBox<>(resources.toArray(new Resource[0]));

        LocalDate today = LocalDate.now();
        JTextField dateField = new JTextField(today.toString());
        JTextField startTimeField = new JTextField("09:00:00");
        JTextField endTimeField = new JTextField("11:00:00");
        JTextField purposeField = new JTextField("Academic Lecture / Lab Practice");

        panel.add(new JLabel("Select Resource:"));
        panel.add(resourceBox);
        panel.add(new JLabel("Booking Date (YYYY-MM-DD):"));
        panel.add(dateField);
        panel.add(new JLabel("Start Time (HH:MM:SS):"));
        panel.add(startTimeField);
        panel.add(new JLabel("End Time (HH:MM:SS):"));
        panel.add(endTimeField);
        panel.add(new JLabel("Purpose / Event:"));
        panel.add(purposeField);
        panel.add(new JLabel("Requested By:"));
        panel.add(new JLabel(currentUser.getFullName() + " (" + currentUser.getRole() + ")"));

        JButton submitBtn = UITheme.createPrimaryButton("Confirm & Allocate");
        submitBtn.addActionListener(e -> {
            Resource selRes = (Resource) resourceBox.getSelectedItem();
            if (selRes == null) {
                JOptionPane.showMessageDialog(dialog, "Please choose a valid resource.");
                return;
            }

            Date bDate;
            Time sTime;
            Time eTime;
            try {
                bDate = Date.valueOf(dateField.getText().trim());
                sTime = Time.valueOf(startTimeField.getText().trim());
                eTime = Time.valueOf(endTimeField.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid Date or Time format!\nPlease use YYYY-MM-DD and HH:MM:SS (e.g. 10:00:00).", 
                        "Format Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            AllocationService.AllocationResult res = allocationService.requestAllocation(
                    selRes.getResourceId(),
                    currentUser.getUserId(),
                    purposeField.getText().trim(),
                    bDate,
                    sTime,
                    eTime
            );

            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(dialog, res.getMessage(), "Allocation Approved", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dialog, res.getMessage(), "Allocation Conflict / Error", JOptionPane.ERROR_MESSAGE);
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
