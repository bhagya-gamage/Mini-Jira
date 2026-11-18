package com.minijira.controller;

import com.minijira.dto.report.*;
import com.minijira.repository.TaskRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Reports")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
public class ReportController {
    private final TaskRepository tasks;

    @GetMapping("/tasks-per-developer")
    public List<DeveloperTaskCount> perDeveloper() { return tasks.tasksPerDeveloper(); }

    @GetMapping("/overdue-by-project")
    public List<OverdueCount> overdue() { return tasks.overduePerProject(LocalDate.now()); }

    @GetMapping("/completion-rate")
    public List<CompletionRate> completion() { return tasks.completionRate(); }
}
