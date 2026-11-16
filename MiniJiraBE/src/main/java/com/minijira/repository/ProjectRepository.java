package com.minijira.repository;

import com.minijira.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("select p from Project p join fetch p.owner " +
            "where p.id in (select pm.project.id from ProjectMember pm where pm.user.id = :uid) order by p.createdAt desc")
    List<Project> findByMember(@Param("uid") Long uid);

    @Query("select p from Project p join fetch p.owner order by p.createdAt desc")
    List<Project> findAllWithOwner();
}
