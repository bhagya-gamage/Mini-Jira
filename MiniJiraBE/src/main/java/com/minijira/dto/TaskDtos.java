package com.minijira.dto;

import com.minijira.model.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class TaskDtos {
    private TaskDtos() {}

    public record TaskRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 2000) String description,
            Priority priority,
            LocalDate dueDate,
            Long assigneeId) {}

    public record StatusRequest(@NotNull TaskStatus status) {}

    public record CommentRequest(@NotBlank @Size(max = 2000) String content) {}

    public record TaskDto(Long id, String title, String description, TaskStatus status, Priority priority,
                          LocalDate dueDate, Long projectId, Long assigneeId, String assigneeName,
                          LocalDateTime createdAt) {
        public static TaskDto from(Task t) {
            User a = t.getAssignee();
            return new TaskDto(t.getId(), t.getTitle(), t.getDescription(), t.getStatus(), t.getPriority(),
                    t.getDueDate(), t.getProject().getId(),
                    a == null ? null : a.getId(), a == null ? null : a.getFullName(), t.getCreatedAt());
        }
    }

    public record CommentDto(Long id, String content, Long authorId, String authorName, LocalDateTime createdAt) {
        public static CommentDto from(Comment c) {
            return new CommentDto(c.getId(), c.getContent(), c.getAuthor().getId(),
                    c.getAuthor().getFullName(), c.getCreatedAt());
        }
    }
}
