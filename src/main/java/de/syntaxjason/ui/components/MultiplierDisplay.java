package de.syntaxjason.ui.components;

import de.syntaxjason.model.ActiveMultiplier;
import de.syntaxjason.service.multiplier.IMultiplierService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MultiplierDisplay extends JPanel {
    private final IMultiplierService multiplierService;
    private JLabel multiplierLabel;
    private JLabel statusLabel;
    private JLabel timeLabel;

    public MultiplierDisplay(IMultiplierService multiplierService) {
        this.multiplierService = multiplierService;
        initializeUI();
        updateMultiplier();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 10));
        setBackground(new Color(40, 40, 45));
        setBorder(new EmptyBorder(30, 30, 30, 30));
        setPreferredSize(new Dimension(280, 0));

        JLabel titleLabel = new JLabel("MULTIPLIER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(150, 150, 160));

        multiplierLabel = new JLabel("1.0x", SwingConstants.CENTER);
        multiplierLabel.setFont(new Font("Segoe UI", Font.BOLD, 52));
        multiplierLabel.setForeground(new Color(100, 200, 100));

        statusLabel = new JLabel("Inaktiv", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(120, 120, 130));

        timeLabel = new JLabel("", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        timeLabel.setForeground(new Color(100, 65, 165));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        multiplierLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(multiplierLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(statusLabel);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(timeLabel);

        add(titleLabel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean hasMultiplier = multiplierService.hasActiveMultiplier();

        paintBackground(g2d, hasMultiplier);
        paintBorder(g2d, hasMultiplier);

        g2d.dispose();
    }

    private void paintBackground(Graphics2D g2d, boolean hasMultiplier) {
        if (hasMultiplier) {
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(100, 65, 165, 40),
                    0, getHeight(), new Color(35, 35, 40)
            );
            g2d.setPaint(gradient);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            return;
        }

        g2d.setColor(new Color(40, 40, 45));
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
    }

    private void paintBorder(Graphics2D g2d, boolean hasMultiplier) {
        if (hasMultiplier) {
            g2d.setColor(new Color(100, 65, 165, 100));
        }

        if (!hasMultiplier) {
            g2d.setColor(new Color(60, 60, 65, 50));
        }

        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
    }

    public void updateMultiplier() {
        double multiplier = multiplierService.getCurrentMultiplier();
        ActiveMultiplier active = multiplierService.getActiveMultiplier();

        multiplierLabel.setText(String.format("%.1fx", multiplier));

        if (active != null) {
            updateActiveState(active);
            repaint();
            return;
        }

        updateInactiveState();
        repaint();
    }

    private void updateActiveState(ActiveMultiplier active) {
        multiplierLabel.setForeground(new Color(100, 200, 100));
        statusLabel.setText("Aktiv von " + active.getActivatedBy());
        statusLabel.setForeground(new Color(150, 200, 150));

        long remaining = active.getRemainingMinutes();
        timeLabel.setText(String.format("%d Min verbleibend", remaining));
        timeLabel.setVisible(true);
    }

    private void updateInactiveState() {
        multiplierLabel.setForeground(new Color(120, 120, 130));
        statusLabel.setText("Kein Multiplier aktiv");
        statusLabel.setForeground(new Color(120, 120, 130));
        timeLabel.setVisible(false);
    }
}
