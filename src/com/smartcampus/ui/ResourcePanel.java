package com.smartcampus.ui;

import com.smartcampus.dao.ResourceDAO;
import com.smartcampus.model.Resource;
import com.smartcampus.model.User;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

/**
 * Panel for managing campus resources (Classrooms, Labs, Halls, Equipment)
 */
public class ResourcePanel extends JPanel {

    private final User currentUser;
    private final ResourceDAO resourceDAO = new ResourceDAO();

    private final DefaultTableModel tableModel;
    private final JTable resourceTable;
    private final JTextField searchField = new JTextField(14);
    private final JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"ALL", "SMART_CLASSROOM", "LABORATORY", "SEMINAR_HALL", "EQUIPMENT"});
    private final JComboBox<String> statusCombo = new JComboBox<>(new String[]{"ALL", "AVAILABLE", "ALLOCATED", "MAINTENANCE"});

    public ResourcePanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top Filter Bar
        JPanel topPanel = UITheme.createCardPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));

        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(new JLabel("Category:"));
        topPanel.add(categoryCombo);
        topPanel.add(new JLabel("Status:"));
        topPanel.add(statusCombo);

        JButton filterBtn = UITheme.createPrimaryButton("Filter / Search");
        filterBtn.setPreferredSize(new Dimension(130, 32));
        filterBtn.addActionListener(e -> loadData());

        JButton resetBtn = UITheme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            categoryCombo.setSelectedIndex(0);
            statusCombo.setSelectedIndex(0);
            loadData();
        });

        topPanel.add(filterBtn);
        topPanel.add(resetBtn);

        // Center Table
        String[] columns = {"ID", "Resource Name", "Category", "Building", "Floor / Room", "Capacity", "Power (kW)", "Status", "Features"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        resourceTable = new JTable(tableModel);
        UITheme.styleTable(resourceTable);
        resourceTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        resourceTable.getColumnModel().getColumn(1).setPreferredWidth(210);
        resourceTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        resourceTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        resourceTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        resourceTable.getColumnModel().getColumn(5).setPreferredWidth(70);
        resourceTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        resourceTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        resourceTable.getColumnModel().getColumn(8).setPreferredWidth(220);

        JScrollPane scrollPane = new JScrollPane(resourceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        // Bottom Action Bar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setOpaque(false);

        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadData());
        bottomPanel.add(refreshBtn);

        if (currentUser.isAdmin()) {
            JButton addBtn = UITheme.createPrimaryButton("+ Add Resource");
            addBtn.addActionListener(e -> showAddEditDialog(null));

            JButton editBtn = UITheme.createSecondaryButton("✏️ Edit Selected");
            editBtn.addActionListener(e -> {
                int row = resourceTable.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "Please select a resource to edit.");
                    return;
                }
                int id = (int) tableModel.getValueAt(row, 0);
                Resource res = resourceDAO.getResourceById(id);
                if (res != null) showAddEditDialog(res);
            });

            JButton deleteBtn = UITheme.createDangerButton("🗑️ Delete");
            deleteBtn.addActionListener(e -> {
                int row = resourceTable.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "Please select a resource to delete.");
                    return;
                }
                int id = (int) tableModel.getValueAt(row, 0);
                String name = (String) tableModel.getValueAt(row, 1);
                int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete resource:\n" + name + "?", 
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    if (resourceDAO.deleteResource(id)) {
                        JOptionPane.showMessageDialog(this, "Resource deleted successfully.");
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete resource.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            bottomPanel.add(addBtn);
            bottomPanel.add(editBtn);
            bottomPanel.add(deleteBtn);
        }

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadData();
    }

    public void loadData() {
        tableModel.setRowCount(0);
        String keyword = searchField.getText().trim();
        String cat = (String) categoryCombo.getSelectedItem();
        String stat = (String) statusCombo.getSelectedItem();

        List<Resource> list = resourceDAO.searchResources(keyword, cat, stat);
        for (Resource r : list) {
            tableModel.addRow(new Object[]{
                    r.getResourceId(),
                    r.getName(),
                    r.getCategory(),
                    r.getBuilding(),
                    r.getFloorRoom(),
                    r.getCapacity(),
                    r.getPowerRatingKw() + " kW",
                    r.getStatus(),
                    r.getFeatures()
            });
        }
    }

    private void showAddEditDialog(Resource existing) {
        boolean isEdit = (existing != null);
        JDialog dialog = new JDialog((JFrame) null, isEdit ? "Edit Campus Resource" : "Add New Campus Resource", true);
        dialog.setSize(500, 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JTextField nameField = new JTextField(isEdit ? existing.getName() : "");
        JComboBox<String> catBox = new JComboBox<>(new String[]{"SMART_CLASSROOM", "LABORATORY", "SEMINAR_HALL", "EQUIPMENT"});
        if (isEdit) catBox.setSelectedItem(existing.getCategory());

        JTextField buildingField = new JTextField(isEdit ? existing.getBuilding() : "");
        JTextField roomField = new JTextField(isEdit ? existing.getFloorRoom() : "");
        JSpinner capSpinner = new JSpinner(new SpinnerNumberModel(isEdit ? existing.getCapacity() : 50, 1, 1000, 5));
        JSpinner powerSpinner = new JSpinner(new SpinnerNumberModel(isEdit ? existing.getPowerRatingKw() : 5.0, 0.1, 100.0, 0.5));
        JComboBox<String> statBox = new JComboBox<>(new String[]{"AVAILABLE", "ALLOCATED", "MAINTENANCE"});
        if (isEdit) statBox.setSelectedItem(existing.getStatus());
        JTextField featuresField = new JTextField(isEdit ? existing.getFeatures() : "Smart Board, High-speed Wi-Fi, HVAC");

        panel.add(new JLabel("Resource Name:")); panel.add(nameField);
        panel.add(new JLabel("Category:")); panel.add(catBox);
        panel.add(new JLabel("Building:")); panel.add(buildingField);
        panel.add(new JLabel("Floor / Room:")); panel.add(roomField);
        panel.add(new JLabel("Capacity:")); panel.add(capSpinner);
        panel.add(new JLabel("Power Rating (kW):")); panel.add(powerSpinner);
        panel.add(new JLabel("Initial Status:")); panel.add(statBox);
        panel.add(new JLabel("Features:")); panel.add(featuresField);

        JButton saveBtn = UITheme.createPrimaryButton(isEdit ? "Update" : "Save Resource");
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid resource name.");
                return;
            }

            Resource res = isEdit ? existing : new Resource();
            res.setName(name);
            res.setCategory((String) catBox.getSelectedItem());
            res.setBuilding(buildingField.getText().trim());
            res.setFloorRoom(roomField.getText().trim());
            res.setCapacity((int) capSpinner.getValue());
            res.setPowerRatingKw((double) powerSpinner.getValue());
            res.setStatus((String) statBox.getSelectedItem());
            res.setFeatures(featuresField.getText().trim());

            boolean success = isEdit ? resourceDAO.updateResource(res) : resourceDAO.addResource(res);
            if (success) {
                JOptionPane.showMessageDialog(dialog, "Resource saved successfully!");
                dialog.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to save resource.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(panel, BorderLayout.CENTER);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(saveBtn);
        wrapper.add(btnPanel, BorderLayout.SOUTH);

        dialog.getContentPane().add(wrapper);
        dialog.setVisible(true);
    }
}
