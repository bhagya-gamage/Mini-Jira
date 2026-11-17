package com.minijira.service;

import com.minijira.dto.PagedResponse;
import com.minijira.dto.TaskDtos.*;
import com.minijira.exception.BadRequestException;
import com.minijira.exception.ResourceNotFoundException;
import com.minijira.model.*;
import com.minijira.repository.*;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {
    private final TaskRepository tasks;
    private final CommentRepository comments;
    private final ProjectMemberRepository members;
    private final UserRepository users;
    private final ProjectService projectService;

    public TaskDto create(Long projectId, TaskRequest r, User current) {
        Project p = projectService.find(projectId);
        projectService.assertAccess(p, current);
        Task t = new Task();
        t.setProject(p);
        apply(t, r);
        return TaskDto.from(tasks.save(t));
    }

    @Transactional(readOnly = true)
    public PagedResponse<TaskDto> list(Long projectId, TaskStatus status, Long assigneeId, String q,
                                       Pageable pageable, User current) {
        projectService.assertAccess(projectService.find(projectId), current);
        Specification<Task> spec = (root, query, cb) -> {
            // JOIN FETCH the assignee in the data query (not in the count query) to avoid N+1
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("assignee", JoinType.LEFT);
            }
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("project").get("id"), projectId));
            if (status != null) ps.add(cb.equal(root.get("status"), status));
            if (assigneeId != null) ps.add(cb.equal(root.get("assignee").get("id"), assigneeId));
            if (q != null && !q.isBlank())
                ps.add(cb.like(cb.lower(root.get("title")), "%" + q.trim().toLowerCase() + "%"));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return PagedResponse.of(tasks.findAll(spec, pageable), TaskDto::from);
    }

    @Transactional(readOnly = true)
    public TaskDto get(Long id, User current) {
        Task t = load(id);
        projectService.assertAccess(t.getProject(), current);
        return TaskDto.from(t);
    }

    public TaskDto update(Long id, TaskRequest r, User current) {
        Task t = load(id);
        projectService.assertAccess(t.getProject(), current);
        apply(t, r);
        return TaskDto.from(t);
    }

    public TaskDto updateStatus(Long id, TaskStatus status, User current) {
        Task t = load(id);
        projectService.assertAccess(t.getProject(), current);
        t.setStatus(status);
        return TaskDto.from(t);
    }

    public void delete(Long id, User current) {
        Task t = load(id);
        projectService.assertAccess(t.getProject(), current);
        comments.deleteByTaskId(id);
        tasks.delete(t);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> listComments(Long taskId, User current) {
        Task t = load(taskId);
        projectService.assertAccess(t.getProject(), current);
        return comments.findByTaskIdOrderByCreatedAtAsc(taskId).stream().map(CommentDto::from).toList();
    }

    public CommentDto addComment(Long taskId, CommentRequest r, User current) {
        Task t = load(taskId);
        projectService.assertAccess(t.getProject(), current);
        Comment c = new Comment();
        c.setTask(t);
        c.setAuthor(current);
        c.setContent(r.content());
        return CommentDto.from(comments.save(c));
    }

    private Task load(Long id) {
        return tasks.findDetailed(id).orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }

    private void apply(Task t, TaskRequest r) {
        t.setTitle(r.title());
        t.setDescription(r.description());
        if (r.priority() != null) t.setPriority(r.priority());
        t.setDueDate(r.dueDate());
        if (r.assigneeId() == null) {
            t.setAssignee(null);
        } else {
            User a = users.findById(r.assigneeId()).orElseThrow(() -> new ResourceNotFoundException("User", r.assigneeId()));
            if (!members.existsByProjectIdAndUserId(t.getProject().getId(), a.getId()))
                throw new BadRequestException("Assignee must be a member of the project");
            t.setAssignee(a);
        }
    }
}
