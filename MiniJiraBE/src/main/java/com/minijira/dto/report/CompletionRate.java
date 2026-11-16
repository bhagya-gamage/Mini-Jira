package com.minijira.dto.report;

public record CompletionRate(String project, long totalTasks, long doneTasks, double completionPercent) {
    public CompletionRate(String project, Long total, Long done) {
        this(project, total, done == null ? 0 : done,
                total == 0 ? 0 : Math.round(((done == null ? 0 : done) * 1000.0) / total) / 10.0);
    }
}
