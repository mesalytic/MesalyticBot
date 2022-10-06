package org.virep.jdabot.utils;

import com.vdurmont.emoji.EmojiParser;

import java.util.ArrayList;
import java.util.List;

/**
 * CODE IS FROM https://github.com/DuncteBot/SkyBot/blob/main/src/main/java/ml/duncte123/skybot/utils/TwemojiParser.java
 */
public class Twemoji extends EmojiParser {
    private static final String BASE_URL = "https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/";

    public static String parseOne(String text) {
        final List<UnicodeCandidate> emojis = getUnicodeCandidates(stripVariants(text));

        if  (!emojis.isEmpty()) {
            final String iconId = grabTheRightIcon(emojis.get(0).getEmoji().getUnicode());

            return BASE_URL + iconId + ".png";
        }

        return null;
    }

    // for future use
    public static List<String> parseAll(String text) {
        final List<UnicodeCandidate> emojis = getUnicodeCandidates(stripVariants(text));

        if (emojis.isEmpty()) {
            return null;
        }

        final List<String> urls = new ArrayList<>();

        // Kinda copied from EmojiParser but it does not have the variants on it
        for (final UnicodeCandidate emoji : emojis) {
            final String iconId = grabTheRightIcon(emoji.getEmoji().getUnicode());
            final String iconUrl = BASE_URL + iconId + ".png";

            urls.add(iconUrl);
        }

        return urls;
    }

    private static String toCodePoint(String unicodeSurrogates) {
        final List<String> codes = new ArrayList<>();

        int charAt;
        int someValue = 0; // what is for?
        int index = 0;

        while (index < unicodeSurrogates.length()) {
            charAt = unicodeSurrogates.charAt(index++);

            if (someValue == 0) {
                if (0xD800 <= charAt && charAt <= 0xDBFF) {
                    someValue = charAt;
                } else {
                    codes.add(Integer.toString(charAt, 16));
                }
            } else {
                final int calculation = 0x10000 + ((someValue - 0xD800) << 10) + (charAt - 0xDC00);

                codes.add(Integer.toString(calculation, 16));
                someValue = 0;
            }
        }

        return String.join("-", codes);
    }

    public static String stripVariants(String rawText) {
        // if variant is present as \uFE0F
        return rawText.indexOf('\u200D') < 0 ? rawText.replace("\uFE0F", "") : rawText;
    }

    private static String grabTheRightIcon(String rawText) {
        return toCodePoint(stripVariants(rawText));
    }
}
