package me.leafs.opponentchat.utils.server;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnabledServer {
    @Getter private final Pattern ipPattern;
    @Getter private final List<Pattern> chatPatterns;

    public EnabledServer(String ipRegex) {
        ipPattern = Pattern.compile(ipRegex, Pattern.CASE_INSENSITIVE);
        chatPatterns = new ArrayList<>();
    }

    public void add(String chatPattern) {
        chatPatterns.add(Pattern.compile(chatPattern, Pattern.CASE_INSENSITIVE));
    }

    public String getChatUserDisplay(String chatMessage) {
        // find a pattern that works
        for (Pattern pattern : chatPatterns) {
            Matcher matcher = pattern.matcher(chatMessage);
            // this is not the matcher you are looking for!
            if (!matcher.matches()) {
                continue;
            }

            try {
                // try to return the group named "name"
                return matcher.group("name");
            } catch (IllegalArgumentException e) {
                break; // break to return null
            }
        }

        return null;
    }
}
