package com.chaos.smarttodo.aiagent.service.nlp;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A tiny deterministic parser so we can already execute "create task" without LLM.
 *
 * Supported examples:
 * - 我计划明天到后天完成计算机导论大作业
 */
@Component
public class SimpleChineseTaskParser {

    private static final Pattern PLAN_RANGE = Pattern.compile("我计划(?<start>明天|后天|今天)(到|至)(?<end>明天|后天|今天)完成(?<name>.+)");
    private static final Pattern PLAN_SINGLE = Pattern.compile("我计划(?<day>明天|后天|今天)完成(?<name>.+)");

    public ParseResult parse(String input) {
        if (input == null) {
            return null;
        }
        String text = input.trim();

        Matcher m1 = PLAN_RANGE.matcher(text);
        if (m1.matches()) {
            LocalDate startedAt = resolveDay(m1.group("start"));
            LocalDate dueAt = resolveDay(m1.group("end"));
            String name = m1.group("name").trim();
            return new ParseResult(true, name, input, startedAt, dueAt);
        }

        Matcher m2 = PLAN_SINGLE.matcher(text);
        if (m2.matches()) {
            LocalDate startedAt = resolveDay(m2.group("day"));
            String name = m2.group("name").trim();
            return new ParseResult(true, name, input, startedAt, startedAt);
        }

        return new ParseResult(false, null, input, null, null);
    }

    private LocalDate resolveDay(String token) {
        LocalDate today = LocalDate.now();
        return switch (token) {
            case "今天" -> today;
            case "明天" -> today.plusDays(1);
            case "后天" -> today.plusDays(2);
            default -> null;
        };
    }

    @Data
    public static class ParseResult {
        private final boolean matched;
        private final String taskName;
        private final String content;
        private final LocalDate startedAt;
        private final LocalDate dueAt;
    }
}

