package com.chaos.smattodo.task.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatchTaskReqDTO {

    private String name;

    private String content;

    private LocalDate startedAt;

    private LocalDate dueAt;

    /**
     * 0-待办, 1-完成, 2-过期
     */
    private Integer status;

    /**
     * 0-无, 1-低, 2-中, 3-高
     */
    private Integer priority;

    // --------------------
    // 字段是否出现在 JSON 中（用于区分未传 vs 传了 null）
    // --------------------

    @JsonIgnore
    private boolean contentSet;

    public void setContent(String content) {
        this.contentSet = true;
        this.content = content;
    }

    @JsonIgnore
    private boolean startedAtSet;

    public void setStartedAt(LocalDate startedAt) {
        this.startedAtSet = true;
        this.startedAt = startedAt;
    }

    @JsonIgnore
    private boolean dueAtSet;

    public void setDueAt(LocalDate dueAt) {
        this.dueAtSet = true;
        this.dueAt = dueAt;
    }
}
