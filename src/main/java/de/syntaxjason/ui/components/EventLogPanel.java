package de.syntaxjason.ui.components;

import de.syntaxjason.model.EventType;
import de.syntaxjason.model.TimerEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventLogPanel extends JPanel {
    private final JPanel eventsContainer;
    private final List<EventCard> eventCards;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public EventLogPanel() {
        this.eventCards = new ArrayList<>();
        this.eventsContainer = new JPanel();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(40, 40, 45));
        setBorder(BorderFactory.createEmptyBorder());

        JLabel titleLabel = new JLabel("EVENT LOG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(150, 150, 160));
        titleLabel.setBorder(new EmptyBorder(20, 20, 15, 20));

        eventsContainer.setLayout(new BoxLayout(eventsContainer, BoxLayout.Y_AXIS));
        eventsContainer.setBackground(new Color(30, 30, 30));
        eventsContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(eventsContainer);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

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
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

        g2d.setColor(new Color(100, 65, 165, 30));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

        g2d.dispose();
    }

    public void addEvent(TimerEvent event) {
        if (event == null) {
            return;
        }

        EventCard card = new EventCard(event, timeFormatter);
        eventCards.add(0, card);

        eventsContainer.add(card, 0);
        eventsContainer.add(Box.createVerticalStrut(8), 1);

        if (eventCards.size() > 50) {
            removeOldestEvent();
        }

        eventsContainer.revalidate();
        eventsContainer.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalScrollBar = ((JScrollPane) getComponent(1)).getVerticalScrollBar();
            verticalScrollBar.setValue(0);
        });
    }

    private void removeOldestEvent() {
        int lastIndex = eventsContainer.getComponentCount() - 1;

        if (lastIndex >= 0) {
            eventsContainer.remove(lastIndex);
        }

        if (lastIndex - 1 >= 0) {
            eventsContainer.remove(lastIndex - 1);
        }

        if (!eventCards.isEmpty()) {
            eventCards.remove(eventCards.size() - 1);
        }
    }

    private static class EventCard extends JPanel {
        private final TimerEvent event;
        private final DateTimeFormatter timeFormatter;

        private static final Color FOLLOWER_COLOR = new Color(100, 150, 255);
        private static final Color RAID_COLOR = new Color(255, 150, 100);
        private static final Color SUB_COLOR = new Color(150, 100, 255);
        private static final Color BITS_COLOR = new Color(255, 215, 0);
        private static final Color SUBGIFT_COLOR = new Color(200, 100, 200);

        public EventCard(TimerEvent event, DateTimeFormatter timeFormatter) {
            this.event = event;
            this.timeFormatter = timeFormatter;
            initializeCard();
        }

        private void initializeCard() {
            setLayout(new BorderLayout(15, 0));
            setBackground(new Color(45, 45, 50));
            setBorder(new EmptyBorder(12, 15, 12, 15));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

            JPanel leftPanel = createLeftPanel();
            JPanel rightPanel = createRightPanel();

            add(leftPanel, BorderLayout.WEST);
            add(rightPanel, BorderLayout.CENTER);
        }

        private JPanel createLeftPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(false);
            panel.setPreferredSize(new Dimension(60, 50));

            JLabel iconLabel = new JLabel(getEventIcon(), SwingConstants.CENTER);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
            iconLabel.setForeground(getEventColor());

            panel.add(iconLabel, BorderLayout.CENTER);
            return panel;
        }

        private JPanel createRightPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(false);

            JLabel titleLabel = new JLabel(event.getEventType().getDisplayName() + " von " + event.getUsername() + " @ " + event.getChannelName());
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            String details = buildDetailsText();
            JLabel detailsLabel = new JLabel(details);
            detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            detailsLabel.setForeground(new Color(150, 150, 160));
            detailsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            panel.add(titleLabel);
            panel.add(Box.createVerticalStrut(4));
            panel.add(detailsLabel);

            return panel;
        }

        private String buildDetailsText() {
            String time = event.getTimestamp().format(timeFormatter);
            String minutes = String.format("+%d Min", event.getMinutesAdded());

            if (event.getDetails().isEmpty()) {
                return String.format("%s • %s", time, minutes);
            }

            return String.format("%s • %s • %s", time, minutes, event.getDetails());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(new Color(45, 45, 50));
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

            Color eventColor = getEventColor();
            g2d.setColor(new Color(eventColor.getRed(), eventColor.getGreen(), eventColor.getBlue(), 30));
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

            g2d.setColor(new Color(eventColor.getRed(), eventColor.getGreen(), eventColor.getBlue(), 80));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

            g2d.dispose();
        }

        private String getEventIcon() {
            return switch (event.getEventType()) {
                case FOLLOWER -> "👤";
                case RAID -> "⚔️";
                case SUB -> "⭐";
                case BITS -> "💎";
                case SUBGIFT -> "🎁";
            };
        }

        private Color getEventColor() {
            return switch (event.getEventType()) {
                case FOLLOWER -> FOLLOWER_COLOR;
                case RAID -> RAID_COLOR;
                case SUB -> SUB_COLOR;
                case BITS -> BITS_COLOR;
                case SUBGIFT -> SUBGIFT_COLOR;
            };
        }
    }
}
