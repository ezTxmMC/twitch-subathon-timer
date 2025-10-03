package de.syntaxjason.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NavigationBar extends JPanel {
    private final List<Consumer<String>> navigationListeners = new ArrayList<>();
    private final List<NavigationButton> buttons = new ArrayList<>();

    public NavigationBar() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(25, 25, 28));
        setPreferredSize(new Dimension(80, 0));
        setBorder(new EmptyBorder(20, 0, 20, 0));

        addNavigationButton("\uD83D\uDDA5\uFE0F", "Dashboard", "dashboard", true);
        addNavigationButton("⚙\uFE0F", "Config", "config", false);
        addNavigationButton("\uD83D\uDCC8", "Stats", "stats", false);
        addNavigationButton("◧", "Sessions", "sessions", false);
        addNavigationButton("◫", "Backups", "backups", false);
        addNavigationButton("◎", "Live Session", "live", false);

        add(Box.createVerticalGlue());

        addNavigationButton("◉", "Info", "info", false);
    }

    private void addNavigationButton(String icon, String tooltip, String view, boolean selected) {
        NavigationButton button = new NavigationButton(icon, tooltip, view);
        button.setSelected(selected);

        button.addActionListener(e -> {
            buttons.forEach(btn -> btn.setSelected(false));
            button.setSelected(true);
            notifyNavigationListeners(view);
        });

        buttons.add(button);
        add(button);
        add(Box.createVerticalStrut(10));
    }

    public void addNavigationListener(Consumer<String> listener) {
        navigationListeners.add(listener);
    }

    private void notifyNavigationListeners(String view) {
        for (Consumer<String> listener : navigationListeners) {
            listener.accept(view);
        }
    }

    private static class NavigationButton extends JButton {
        private final String view;
        private boolean selected;

        private static final Color HOVER_COLOR = new Color(60, 60, 65);
        private static final Color SELECTED_COLOR = new Color(100, 65, 165);
        private static final Color DEFAULT_COLOR = new Color(25, 25, 28);

        public NavigationButton(String icon, String tooltip, String view) {
            super(icon);
            this.view = view;
            this.selected = false;

            setToolTipText(tooltip);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFont(new Font("Dialog", Font.BOLD, 32));
            setPreferredSize(new Dimension(80, 70));
            setMaximumSize(new Dimension(80, 70));
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    repaint();
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bgColor = calculateBackgroundColor();

            g2d.setColor(bgColor);
            g2d.fillRoundRect(10, 5, getWidth() - 20, getHeight() - 10, 15, 15);

            if (selected) {
                g2d.setColor(new Color(100, 65, 165));
                g2d.fillRoundRect(0, getHeight() / 2 - 20, 5, 40, 5, 5);
            }

            g2d.dispose();
            super.paintComponent(g);
        }

        private Color calculateBackgroundColor() {
            if (selected) {
                return SELECTED_COLOR;
            }

            if (getModel().isRollover()) {
                return HOVER_COLOR;
            }

            return DEFAULT_COLOR;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            repaint();
        }
    }
}
