package com.minijira.repository;

import com.minijira.model.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    @Query("select pm from ProjectMember pm join fetch pm.user where pm.project.id = :pid order by pm.joinedAt")
    List<ProjectMember> findByProjectWithUser(@Param("pid") Long projectId);
}
