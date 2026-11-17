package com.minijira.service;

import com.minijira.dto.ProjectDtos.*;
import com.minijira.exception.BadRequestException;
import com.minijira.exception.ResourceNotFoundException;
import com.minijira.model.*;
import com.minijira.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {
    private final ProjectRepository projects;
    private final ProjectMemberRepository members;
    private final UserRepository users;

    public ProjectDto create(ProjectRequest r, User owner) {
        Project p = new Project();
        p.setName(r.name());
        p.setDescription(r.description());
        p.setOwner(owner);
        projects.save(p);
        addMemberInternal(p, owner);
        return ProjectDto.from(p);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> list(User u) {
        List<Project> list = u.getRole() == Role.ADMIN ? projects.findAllWithOwner() : projects.findByMember(u.getId());
        return list.stream().map(ProjectDto::from).toList();
    }

    @Transactional(readOnly = true)
    public ProjectDto get(Long id, User u) {
        Project p = find(id);
        assertAccess(p, u);
        return ProjectDto.from(p);
    }

    public MemberDto addMember(Long projectId, Long userId, User current) {
        Project p = find(projectId);
        assertAccess(p, current);
        User target = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (members.existsByProjectIdAndUserId(projectId, userId))
            throw new BadRequestException("User is already a member of this project");
        return MemberDto.from(addMemberInternal(p, target));
    }

    @Transactional(readOnly = true)
    public List<MemberDto> listMembers(Long projectId, User current) {
        assertAccess(find(projectId), current);
        return members.findByProjectWithUser(projectId).stream().map(MemberDto::from).toList();
    }

    // ---- helpers used by TaskService ----
    public Project find(Long id) {
        return projects.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    public void assertAccess(Project p, User u) {
        if (u.getRole() == Role.ADMIN) return;
        if (!members.existsByProjectIdAndUserId(p.getId(), u.getId()))
            throw new AccessDeniedException("Not a member of this project");
    }

    private ProjectMember addMemberInternal(Project p, User u) {
        ProjectMember m = new ProjectMember();
        m.setProject(p);
        m.setUser(u);
        return members.save(m);
    }
}
