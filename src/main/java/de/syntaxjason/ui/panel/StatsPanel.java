package de.syntaxjason.ui.panel;

import de.syntaxjason.model.EventType;
import de.syntaxjason.model.Session;
import de.syntaxjason.service.session.ISessionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class StatsPanel extends JPanel {
    private final ISessionService sessionService;
    private JPanel statsContainer;

    public StatsPanel(ISessionService sessionService) {
        this.sessionService = sessionService;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(30, 30, 30));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("STATISTIKEN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        statsContainer = new JPanel();
        statsContainer.setLayout(new BoxLayout(statsContainer, BoxLayout.Y_AXIS));
        statsContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(statsContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        refreshStats();
    }

    public void refreshStats() {
        statsContainer.removeAll();

        Session currentSession = sessionService.getCurrentSession();

        if (currentSession == null) {
            addNoSessionMessage();
            statsContainer.revalidate();
            statsContainer.repaint();
            return;
        }

        statsContainer.add(createOverviewSection(currentSession));
        statsContainer.add(Box.createVerticalStrut(20));
        statsContainer.add(createEventBreakdownSection(currentSession));
        statsContainer.add(Box.createVerticalStrut(20));
        statsContainer.add(createAllTimeStatsSection());

        statsContainer.revalidate();
        statsContainer.repaint();
    }

    private void addNoSessionMessage() {
        JLabel noSessionLabel = new JLabel("Keine aktive Session");
        noSessionLabel.setForeground(new Color(150, 150, 160));
        noSessionLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        noSessionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsContainer.add(noSessionLabel);
    }

    private JPanel createOverviewSection(Session session) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JLabel sectionTitle = new JLabel("AKTUELLE SESSION");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(new Color(150, 150, 160));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(15));

        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        cardsPanel.setOpaque(false);
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

        cardsPanel.add(createStatCard("Session Name", session.getName(), "◧"));
        cardsPanel.add(createStatCard("Gesamt Events", String.valueOf(session.getTotalEvents()), "◉"));
        cardsPanel.add(createStatCard("Hinzugefügte Zeit", session.getTotalMinutesAdded() + " Min", "⏱"));
        cardsPanel.add(createStatCard("Dauer", session.getDurationMinutes() + " Min", "◷"));

        section.add(cardsPanel);

        return section;
    }

    private JPanel createEventBreakdownSection(Session session) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        JLabel sectionTitle = new JLabel("EVENT BREAKDOWN");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(new Color(150, 150, 160));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(15));

        for (EventType type : EventType.values()) {
            int count = session.getEventCounts().getOrDefault(type, 0);
            int minutes = session.getTotalMinutesAddedMap().getOrDefault(type, 0);
            section.add(createEventBar(type, count, minutes, session.getTotalEvents()));
            section.add(Box.createVerticalStrut(10));
        }

        return section;
    }

    private JPanel createAllTimeStatsSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JLabel sectionTitle = new JLabel("ALL-TIME STATISTIKEN");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(new Color(150, 150, 160));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(15));

        List<Session> allSessions = sessionService.getAllSessions();

        int totalSessions = allSessions.size();
        int totalEvents = allSessions.stream().mapToInt(Session::getTotalEvents).sum();
        int totalMinutes = allSessions.stream().mapToInt(Session::getTotalMinutesAdded).sum();

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        cardsPanel.add(createStatCard("Sessions", String.valueOf(totalSessions), "◧"));
        cardsPanel.add(createStatCard("Events", String.valueOf(totalEvents), "◉"));
        cardsPanel.add(createStatCard("Minuten", String.valueOf(totalMinutes), "⏱"));

        section.add(cardsPanel);

        return section;
    }

    private JPanel createStatCard(String label, String value, String icon) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(45, 45, 50),
                        0, getHeight(), new Color(35, 35, 40)
                );

                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2d.setColor(new Color(100, 65, 165, 40));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

                g2d.dispose();
            }
        };

        card.setLayout(new BorderLayout(10, 10));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Dialog", Font.BOLD, 28));
        iconLabel.setForeground(new Color(100, 65, 165));
        iconLabel.setPreferredSize(new Dimension(40, 40));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        labelComponent.setForeground(new Color(150, 150, 160));
        labelComponent.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueComponent.setForeground(Color.WHITE);
        valueComponent.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(labelComponent);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(valueComponent);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createEventBar(EventType type, int count, int minutes, int totalEvents) {
        JPanel bar = new JPanel(new BorderLayout(10, 5));
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        bar.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel nameLabel = new JLabel(type.getDisplayName());
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setPreferredSize(new Dimension(100, 20));

        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setOpaque(false);

        JProgressBar progressBar = new JProgressBar(0, Math.max(1, totalEvents));
        progressBar.setValue(count);
        progressBar.setPreferredSize(new Dimension(0, 20));
        progressBar.setStringPainted(false);
        progressBar.setBackground(new Color(40, 40, 45));
        progressBar.setForeground(getEventColor(type));
        progressBar.setBorderPainted(false);

        JLabel statsLabel = new JLabel(String.format("%dx (%d Min)", count, minutes));
        statsLabel.setForeground(new Color(150, 150, 160));
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statsLabel.setPreferredSize(new Dimension(120, 20));
        statsLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        progressPanel.add(progressBar, BorderLayout.CENTER);

        bar.add(nameLabel, BorderLayout.WEST);
        bar.add(progressPanel, BorderLayout.CENTER);
        bar.add(statsLabel, BorderLayout.EAST);

        return bar;
    }

    private Color getEventColor(EventType type) {
        return switch (type) {
            case FOLLOWER -> new Color(100, 150, 255);
            case RAID -> new Color(255, 150, 100);
            case SUB -> new Color(150, 100, 255);
            case BITS -> new Color(255, 215, 0);
            case SUBGIFT -> new Color(200, 100, 200);
        };
    }
}
