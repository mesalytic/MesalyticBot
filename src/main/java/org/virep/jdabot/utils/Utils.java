package org.virep.jdabot.utils;

import java.time.Duration;

public class Utils {
    public static String progressBar(long position, long duration) {
        var blocks = (int) ((float) position / duration * 15);
        var progressBuilder = new StringBuilder();
        for (var i = 0; i < 15; i++)
            progressBuilder.append(i == blocks ? "\uD83D\uDD18" : "\u25AC");
        return progressBuilder.append("\u25AC").append(" [**").append(formatTrackLength(position)).append("/").append(formatTrackLength(duration)).append("**]").toString();
    }

    public static String formatTrackLength(final long millis) {
        Duration duration = Duration.ofMillis(millis);
        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();
        int seconds = duration.toSecondsPart();
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static long lengthToMillis(final String length) {
        String[] splitLength = length.split(":");

        long millis = 0;
        if (splitLength.length == 3) {
            // h:m:s
            millis = (Integer.parseInt(splitLength[0]) * 3600000L) + (Integer.parseInt(splitLength[1]) * 60000L) + (Integer.parseInt(splitLength[2]) * 1000L);
        } else if (splitLength.length == 2) {
            millis = (Integer.parseInt(splitLength[0]) * 60000L) + (Integer.parseInt(splitLength[1]) * 1000L);
        }

        return millis;
    }

    public static String secondsToSeperatedTime(final int seconds) {
        Duration duration = Duration.ofSeconds(seconds);
        String response = "";

        int hour = duration.toHoursPart();
        int minute = duration.toMinutesPart();
        int second = duration.toSecondsPart();

        if (hour > 0) response += "%dh ".formatted(hour);
        if (minute > 0) response += "%dm ".formatted(minute);
        response += "%ds".formatted(second);

        return response;
    }
}
