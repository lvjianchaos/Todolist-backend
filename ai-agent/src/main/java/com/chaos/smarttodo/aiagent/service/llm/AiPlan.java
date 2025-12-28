package com.chaos.smarttodo.aiagent.service.llm;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AiPlan {

    /**
     * create_task | update_task | query_tasks | unknown
     */
    private String intent;

    /**
     * Used by create_task / update_task
     */
    private TaskDraft task;

    /**
     * Used by query_tasks
     */
    private QueryDraft query;

    @Data
    public static class TaskDraft {
        private String name;
        private String content;
        private LocalDate startedAt;
        private LocalDate dueAt;
        private Integer priority;
        private Integer status;
    }

    @Data
    public static class QueryDraft {
        /**
         * dueAt | startedAt
         */
        private String dateField;

        private LocalDate from;
        private LocalDate to;
    }
}
