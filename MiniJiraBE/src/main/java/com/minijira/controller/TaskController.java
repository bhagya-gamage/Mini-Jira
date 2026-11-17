package com.minijira.controller;

import com.minijira.dto.PagedResponse;
import com.minijira.dto.TaskDtos.*;
import com.minijira.model.TaskStatus;
import com.minijira.model.User;
import com.minijira.service.TaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tasks")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService service;

    @PostMapping("/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto create(@PathVariable Long projectId, @Valid @RequestBody TaskRequest r, @AuthenticationPrincipal User u) {
        return service.create(projectId, r, u);
    }

    @GetMapping("/projects/{projectId}/tasks")
    public PagedResponse<TaskDto> list(@PathVariable Long projectId,
                                       @RequestParam(required = false) TaskStatus status,
                                       @RequestParam(required = false) Long assigneeId,
                                       @RequestParam(required = false) String q,
                                       @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
                                       @AuthenticationPrincipal User u) {
        return service.list(projectId, status, assigneeId, q, pageable, u);
    }

    @GetMapping("/tasks/{id}")
    public TaskDto get(@PathVariable Long id, @AuthenticationPrincipal User u) { return service.get(id, u); }

    @PutMapping("/tasks/{id}")
    public TaskDto update(@PathVariable Long id, @Valid @RequestBody TaskRequest r, @AuthenticationPrincipal User u) {
        return service.update(id, r, u);
    }

    @PatchMapping("/tasks/{id}/status")
    public TaskDto status(@PathVariable Long id, @Valid @RequestBody StatusRequest r, @AuthenticationPrincipal User u) {
        return service.updateStatus(id, r.status(), u);
    }

    @DeleteMapping("/tasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User u) {
        service.delete(id, u);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tasks/{id}/comments")
    public List<CommentDto> comments(@PathVariable Long id, @AuthenticationPrincipal User u) {
        return service.listComments(id, u);
    }

    @PostMapping("/tasks/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest r, @AuthenticationPrincipal User u) {
        return service.addComment(id, r, u);
    }
}
