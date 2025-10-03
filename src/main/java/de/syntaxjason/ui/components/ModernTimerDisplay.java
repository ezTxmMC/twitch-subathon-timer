package de.syntaxjason.ui.components;

import de.syntaxjason.manager.ITimerManager;
import de.syntaxjason.service.timer.ITimerService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Duration;

public class ModernTimerDisplay extends JPanel {
    private final ITimerService timerService;
    private final ITimerManager timerManager;
    private JLabel timeLabel;
    private JLabel titleLabel;
    private JProgressBar progressBar;
    private JButton pauseButton;
    private JLabel pauseIndicatorLabel;

    public ModernTimerDisplay(ITimerService timerService, ITimerManager timerManager) {
        this.timerService = timerService;
        this.timerManager = timerManager;
        initializeUI();
        updateTime(timerService.getRemainingTime());
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 15));
        setBackground(new Color(40, 40, 45));
        setBorder(new EmptyBorder(40, 40, 40, 40));
        setPreferredSize(new Dimension(0, 280));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        titleLabel = new JLabel("VERBLEIBENDE ZEIT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(150, 150, 160));

        pauseButton = createPauseButton();

        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(pauseButton, BorderLayout.EAST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        timeLabel = new JLabel("00:00:00", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 96));
        timeLabel.setForeground(Color.WHITE);

        pauseIndicatorLabel = new JLabel("", SwingConstants.CENTER);
        pauseIndicatorLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pauseIndicatorLabel.setForeground(new Color(255, 180, 80));
        pauseIndicatorLabel.setVisible(false);

        centerPanel.add(timeLabel, BorderLayout.CENTER);
        centerPanel.add(pauseIndicatorLabel, BorderLayout.SOUTH);

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(0, 8));
        progressBar.setStringPainted(false);
        progressBar.setBackground(new Color(30, 30, 35));
        progressBar.setForeground(new Color(100, 65, 165));
        progressBar.setBorderPainted(false);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);

        timerManager.registerPauseListener(this::updatePauseState);
    }

    private JButton createPauseButton() {
        JButton button = new JButton("⏸");
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        button.setPreferredSize(new Dimension(60, 40));
        button.setBackground(new Color(100, 65, 165));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText("Timer pausieren/fortsetzen");

        button.addActionListener(e -> togglePause());

        return button;
    }

    private void togglePause() {
        if (timerManager.isPaused()) {
            timerManager.resume();
            return;
        }

        timerManager.pause();
    }

    private void updatePauseState(boolean isPaused) {
        if (isPaused) {
            pauseButton.setText("▶");
            pauseButton.setBackground(new Color(80, 150, 80));
            pauseIndicatorLabel.setText("PAUSIERT");
            pauseIndicatorLabel.setVisible(true);
            return;
        }

        pauseButton.setText("⏸");
        pauseButton.setBackground(new Color(100, 65, 165));
        pauseIndicatorLabel.setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(50, 50, 55),
                0, getHeight(), new Color(35, 35, 40)
        );

        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

        g2d.setColor(new Color(100, 65, 165, 30));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

        g2d.dispose();
    }

    public void updateTime(Duration duration) {
        if (duration == null) {
            timeLabel.setText("00:00:00");
            progressBar.setValue(0);
            return;
        }

        long totalSeconds = duration.getSeconds();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timeLabel.setText(timeText);

        updateTimeColor(duration);
        updateProgressBar(duration);
    }

    private void updateTimeColor(Duration duration) {
        if (timerManager.isPaused()) {
            timeLabel.setForeground(new Color(255, 180, 80));
            return;
        }

        if (duration.isZero() || duration.isNegative()) {
            timeLabel.setForeground(new Color(220, 80, 80));
            return;
        }

        if (duration.toMinutes() < 10) {
            timeLabel.setForeground(new Color(255, 180, 80));
            return;
        }

        timeLabel.setForeground(Color.WHITE);
    }

    private void updateProgressBar(Duration duration) {
        long totalMinutes = 240;
        long remainingMinutes = duration.toMinutes();
        int progress = (int) ((remainingMinutes * 100) / totalMinutes);
        progressBar.setValue(Math.max(0, Math.min(100, progress)));
    }
}
