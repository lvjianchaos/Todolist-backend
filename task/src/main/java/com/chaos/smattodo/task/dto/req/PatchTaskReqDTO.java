package com.chaos.smattodo.task.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
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

    @JsonSetter(value = "content", nulls = Nulls.SET)
    public void setContent(String content) {
        this.contentSet = true;
        this.content = content;
    }

    @JsonIgnore
    private boolean startedAtSet;

    @JsonSetter(value = "startedAt", nulls = Nulls.SET)
    public void setStartedAt(LocalDate startedAt) {
        this.startedAtSet = true;
        this.startedAt = startedAt;
    }

    @JsonIgnore
    private boolean dueAtSet;

    @JsonSetter(value = "dueAt", nulls = Nulls.SET)
    public void setDueAt(LocalDate dueAt) {
        this.dueAtSet = true;
        this.dueAt = dueAt;
    }

    // 显式暴露 getter，避免 Lombok/IDE 对 boolean 命名推导差异导致 service 侧读不到
    public boolean isContentSet() {
        return contentSet;
    }

    public boolean isStartedAtSet() {
        return startedAtSet;
    }

    public boolean isDueAtSet() {
        return dueAtSet;
    }
}
