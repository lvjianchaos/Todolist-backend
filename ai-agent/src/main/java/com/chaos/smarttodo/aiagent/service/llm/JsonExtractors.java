package com.chaos.smarttodo.aiagent.service.llm;

/**
 * Utilities to extract a JSON object from LLM output.
 */
public final class JsonExtractors {

    private JsonExtractors() {
    }

    /**
     * Extract the first top-level JSON object from a string.
     * Handles outputs like:
     * - ```json\n{ ... }\n```
     * - some text { ... } some text
     */
    public static String extractFirstJsonObject(String text) {
        if (text == null) {
            return null;
        }
        String s = stripCodeFences(text).trim();

        int start = s.indexOf('{');
        if (start < 0) {
            return null;
        }

        int depth = 0;
        boolean inString = false;
        boolean escaping = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);

            if (inString) {
                if (escaping) {
                    escaping = false;
                    continue;
                }
                if (c == '\\') {
                    escaping = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            if (c == '"') {
                inString = true;
                continue;
            }

            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return s.substring(start, i + 1);
                }
            }
        }

        return null;
    }

    private static String stripCodeFences(String s) {
        String t = s.trim();
        if (t.startsWith("```")) {
            // remove first fence line
            int firstNewline = t.indexOf('\n');
            if (firstNewline > 0) {
                t = t.substring(firstNewline + 1);
            }
            // remove last fence
            int lastFence = t.lastIndexOf("```");
            if (lastFence >= 0) {
                t = t.substring(0, lastFence);
            }
        }
        return t;
    }
}
