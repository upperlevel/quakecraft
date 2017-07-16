package xyz.upperlevel.spigot.quakecraft.core;

import java.util.List;

public class StringUtil {

    private StringUtil() {
    }

    public static String toPhrase(List<String> words) {
        StringBuilder bdr = new StringBuilder();
        for (String word : words)
            bdr.append(" ")
                    .append(word);
        return bdr.substring(1);
    }
}
