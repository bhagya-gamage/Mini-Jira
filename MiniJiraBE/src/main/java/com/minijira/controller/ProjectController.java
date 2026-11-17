package com.minijira.controller;

import com.minijira.dto.ProjectDtos.*;
import com.minijira.model.User;
import com.minijira.service.ProjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Projects")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
    public ProjectDto create(@Valid @RequestBody ProjectRequest r, @AuthenticationPrincipal User u) {
        return service.create(r, u);
    }

    @GetMapping
    public List<ProjectDto> list(@AuthenticationPrincipal User u) { return service.list(u); }

    @GetMapping("/{id}")
    public ProjectDto get(@PathVariable Long id, @AuthenticationPrincipal User u) { return service.get(id, u); }

    @PostMapping("/{id}/members")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
    public MemberDto addMember(@PathVariable Long id, @Valid @RequestBody AddMemberRequest r,
                               @AuthenticationPrincipal User u) {
        return service.addMember(id, r.userId(), u);
    }

    @GetMapping("/{id}/members")
    public List<MemberDto> members(@PathVariable Long id, @AuthenticationPrincipal User u) {
        return service.listMembers(id, u);
    }
}
