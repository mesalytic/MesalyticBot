package org.virep.jdabot;

import java.time.Duration;

public class Utils {
    public static String progressBar(long position, long duration) {
        var blocks = (int) ((float) position / duration * 15);
        var progressBuilder = new StringBuilder();
        for (var i = 0; i < 15; i++)
            progressBuilder.append(i == blocks ? "\uD83D\uDD18" : "\u25AC");
        return progressBuilder.append("\u25AC").append(" [**").append(formatTrackLength(position)).append("/").append(formatTrackLength(duration)).append("**]").toString();
    }

    private static String formatTrackLength(final long millis) {
        Duration duration = Duration.ofMillis(millis);
        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();
        int seconds = duration.toSecondsPart();
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }
}
