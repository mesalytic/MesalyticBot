package org.virep.jdabot.utils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Utils {
    private static final long SECOND = 1000;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;

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

        int day = (int)duration.toDaysPart();
        int hour = duration.toHoursPart();
        int minute = duration.toMinutesPart();
        int second = duration.toSecondsPart();

        if (day > 0) response += "%dd ".formatted(day);
        if (hour > 0) response += "%dh ".formatted(hour);
        if (minute > 0) response += "%dm ".formatted(minute);
        response += "%ds".formatted(second);

        return response;
    }

    public static String formatUptime(long uptime) {
        StringBuilder buf = new StringBuilder();
        if (uptime > DAY) {
            long days = (uptime - uptime % DAY) / DAY;
            buf.append(days);
            buf.append(" Days");
            uptime = uptime % DAY;
        }
        if (uptime > HOUR) {
            long hours = (uptime - uptime % HOUR) / HOUR;
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(hours);
            buf.append(" Hours");
            uptime = uptime % HOUR;
        }
        if (uptime > MINUTE) {
            long minutes = (uptime - uptime % MINUTE) / MINUTE;
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(minutes);
            buf.append(" Minutes");
            uptime = uptime % MINUTE;
        }
        if (uptime > SECOND) {
            long seconds = (uptime - uptime % SECOND) / SECOND;
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(seconds);
            buf.append(" Seconds");
        }

        return buf.toString();
    }

    public static int timeStringToSeconds(String input) {
        input = input.toLowerCase();
        input = input.replaceAll(" ", "");

        if (input.isEmpty()) return -1;
        int time = 0;
        StringBuilder number = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (isInt(String.valueOf(c))) {
                number.append(c);
                continue;
            }
            if (number.toString().isEmpty()) return -1;
            int add = Integer.parseInt(number.toString());
            switch (c) {
                case 'w':
                    add *= 7;
                case 'd':
                    add *= 24;
                case 'h':
                    add *= 60;
                case 'm':
                    add *= 60;
                case 's':
                    time += add;
                    number.setLength(0);
                    break;
                default:
                    return -1;
            }
        }
        return time;
    }

    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String badgesToEmote(String flags) {
        return flags
                .replace("boost", "<:boost:1013341121516159046>")
                .replace("Bug Hunter Level 1", "<:bughunter1:776631999959531570>")
                .replace("Bug Hunter Level 2", "<:bughunter2:776632040643624990>")
                .replace("Early Supporter", "<:earlysupporter:776627862597009408>")
                .replace("HypeSquad Events", "<:hypesquad:776630121985343499>")
                .replace("HypeSquad Balance", "<:balancehouse:776629582950432820>")
                .replace("HypeSquad Bravery", "<:braveryhouse:776629630408327189>")
                .replace("HypeSquad Brilliance", "<:brilliancehouse:776629668848861255>")
                .replace("Partenered Server Owner", "<:partnerowner:776628269356417036>")
                .replace("Discord Employee", "<:discordemployee:776633014502555678>")
                .replace("Early Verified Bot Developer", "<:verifieddev:776627149452083212>")
                .replace("Verified Bot", "<:verifiedbot:776626551025958912>")
                .replace("Discord Certified Moderator", "<:certifiedmod:1013331823868256297>");
    }

    public static <T> List<List<T>> getPages(Collection<T> collection, Integer pageSize) {
        if (collection == null) return Collections.emptyList();

        List<T> list = new ArrayList<T>(collection);
        if (pageSize == null || pageSize <= 0 || pageSize > list.size()) pageSize = list.size();

        int numPages = (int) Math.ceil((double) list.size() / (double) pageSize);
        List<List<T>> pages = new ArrayList<List<T>>(numPages);

        for (int pageNum = 0; pageNum < numPages; )
            pages.add(list.subList(pageNum * pageSize, Math.min(++pageNum * pageSize, list.size())));

        return pages;
    }
}
