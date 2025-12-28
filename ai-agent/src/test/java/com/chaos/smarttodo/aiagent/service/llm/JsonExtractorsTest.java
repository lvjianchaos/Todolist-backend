package com.chaos.smarttodo.aiagent.service.llm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonExtractorsTest {

    @Test
    void shouldExtractJsonFromCodeFence() {
        String input = "```json\n{\"intent\":\"create_task\",\"task\":{\"name\":\"A\"}}\n```";
        assertThat(JsonExtractors.extractFirstJsonObject(input))
                .isEqualTo("{\"intent\":\"create_task\",\"task\":{\"name\":\"A\"}}"
                );
    }

    @Test
    void shouldExtractJsonFromMixedText() {
        String input = "好的，以下是结果：{\"intent\":\"unknown\",\"task\":null}谢谢";
        assertThat(JsonExtractors.extractFirstJsonObject(input))
                .isEqualTo("{\"intent\":\"unknown\",\"task\":null}");
    }
}

