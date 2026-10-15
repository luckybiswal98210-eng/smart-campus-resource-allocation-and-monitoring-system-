package com.smartcampus.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * UI Theme and styling utilities for modern, sleek Java Swing interface
 */
public class UITheme {

    // Color Palette
    public static final Color PRIMARY = new Color(30, 41, 59);        // Slate 800
    public static final Color PRIMARY_DARK = new Color(15, 23, 42);   // Slate 900
    public static final Color ACCENT = new Color(14, 165, 233);       // Sky 500
    public static final Color ACCENT_HOVER = new Color(2, 132, 199);  // Sky 600
    
    public static final Color BG_LIGHT = new Color(248, 250, 252);    // Slate 50
    public static final Color CARD_BG = new Color(255, 255, 255);
    public static final Color TEXT_MAIN = new Color(15, 23, 42);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);  // Slate 500
    public static final Color BORDER = new Color(226, 232, 240);      // Slate 200

    public static final Color SUCCESS = new Color(16, 185, 129);      // Emerald 500
    public static final Color SUCCESS_BG = new Color(236, 253, 245);
    public static final Color WARNING = new Color(245, 158, 11);      // Amber 500
    public static final Color WARNING_BG = new Color(254, 243, 199);
    public static final Color DANGER = new Color(239, 68, 68);        // Rose 500
    public static final Color DANGER_BG = new Color(254, 242, 242);
    public static final Color INFO = new Color(59, 130, 246);         // Blue 500
    public static final Color INFO_BG = new Color(239, 246, 255);

    // Fonts
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_METRIC = new Font("SansSerif", Font.BOLD, 26);

    public static void applyTheme() {
        try {
            // Attempt FlatLaf modern Light theme
            Class<?> flatLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            UIManager.setLookAndFeel((javax.swing.LookAndFeel) flatLafClass.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            try {
                // Fallback to System Look & Feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Global defaults
        UIManager.put("Button.font", FONT_BOLD);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.font", FONT_BOLD);
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(ACCENT_HOVER.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(ACCENT_HOVER);
                } else {
                    g2.setColor(ACCENT);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 36));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(TEXT_MAIN);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(DANGER);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        return panel;
    }

    public static JLabel createStatusBadge(String status) {
        JLabel badge = new JLabel(status, JLabel.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg;
                Color fg;
                if ("AVAILABLE".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
                    bg = SUCCESS_BG;
                    fg = SUCCESS;
                } else if ("ALLOCATED".equalsIgnoreCase(status) || "ACTIVE".equalsIgnoreCase(status) || "HIGH".equalsIgnoreCase(status) || "CRITICAL".equalsIgnoreCase(status)) {
                    bg = DANGER_BG;
                    fg = DANGER;
                } else {
                    bg = WARNING_BG;
                    fg = WARNING;
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(fg);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(FONT_SMALL);
        if ("AVAILABLE".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
            badge.setForeground(SUCCESS);
        } else if ("ALLOCATED".equalsIgnoreCase(status) || "ACTIVE".equalsIgnoreCase(status) || "HIGH".equalsIgnoreCase(status) || "CRITICAL".equalsIgnoreCase(status)) {
            badge.setForeground(DANGER);
        } else {
            badge.setForeground(WARNING);
        }
        badge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        return badge;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setFont(FONT_BODY);
        table.setSelectionBackground(new Color(224, 242, 254)); // Sky 100
        table.setSelectionForeground(TEXT_MAIN);
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));
        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));

        // Center render for status columns
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }
}
