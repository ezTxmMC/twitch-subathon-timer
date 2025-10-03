package de.syntaxjason.util;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(\\d+)\\s*([dhms])",
            Pattern.CASE_INSENSITIVE
    );

    public static Duration parseTimeString(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            throw new IllegalArgumentException("Zeit-String darf nicht leer sein");
        }

        Duration totalDuration = Duration.ZERO;
        Matcher matcher = TIME_PATTERN.matcher(timeString.toLowerCase());

        boolean found = false;
        while (matcher.find()) {
            found = true;
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            Duration duration = switch (unit) {
                case "d" -> Duration.ofDays(value);
                case "h" -> Duration.ofHours(value);
                case "m" -> Duration.ofMinutes(value);
                case "s" -> Duration.ofSeconds(value);
                default -> Duration.ZERO;
            };

            totalDuration = totalDuration.plus(duration);
        }

        if (!found) {
            throw new IllegalArgumentException(
                    "Ungültiges Zeitformat. Verwende: 5d 3h 30m 15s"
            );
        }

        return totalDuration;
    }

    public static String formatDuration(Duration duration) {
        if (duration.isZero() || duration.isNegative()) {
            return "0s";
        }

        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append("d ");
        }

        if (hours > 0) {
            sb.append(hours).append("h ");
        }

        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }

        if (seconds > 0) {
            sb.append(seconds).append("s");
        }

        return sb.toString().trim();
    }
}
