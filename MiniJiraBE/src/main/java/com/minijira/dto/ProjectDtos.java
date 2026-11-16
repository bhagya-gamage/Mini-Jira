package com.minijira.dto;

import com.minijira.model.Project;
import com.minijira.model.ProjectMember;
import com.minijira.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class ProjectDtos {
    private ProjectDtos() {}

    public record ProjectRequest(@NotBlank @Size(max = 120) String name, @Size(max = 1000) String description) {}

    public record AddMemberRequest(@NotNull Long userId) {}

    public record ProjectDto(Long id, String name, String description, Long ownerId, String ownerName, LocalDateTime createdAt) {
        public static ProjectDto from(Project p) {
            return new ProjectDto(p.getId(), p.getName(), p.getDescription(),
                    p.getOwner().getId(), p.getOwner().getFullName(), p.getCreatedAt());
        }
    }

    public record MemberDto(Long userId, String fullName, String email, Role role, LocalDateTime joinedAt) {
        public static MemberDto from(ProjectMember m) {
            return new MemberDto(m.getUser().getId(), m.getUser().getFullName(), m.getUser().getEmail(),
                    m.getUser().getRole(), m.getJoinedAt());
        }
    }
}
