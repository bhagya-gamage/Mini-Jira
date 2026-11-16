package com.minijira.repository;

import com.minijira.dto.report.*;
import com.minijira.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    @Query("select t from Task t left join fetch t.assignee join fetch t.project where t.id = :id")
    Optional<Task> findDetailed(@Param("id") Long id);

    @Query("select new com.minijira.dto.report.DeveloperTaskCount(a.fullName, count(t)) " +
            "from Task t join t.assignee a group by a.id, a.fullName order by count(t) desc")
    List<DeveloperTaskCount> tasksPerDeveloper();

    @Query("select new com.minijira.dto.report.OverdueCount(p.name, count(t)) " +
            "from Task t join t.project p " +
            "where t.dueDate < :today and t.status <> com.minijira.model.TaskStatus.DONE " +
            "group by p.id, p.name order by count(t) desc")
    List<OverdueCount> overduePerProject(@Param("today") LocalDate today);

    @Query("select new com.minijira.dto.report.CompletionRate(p.name, count(t), " +
            "sum(case when t.status = com.minijira.model.TaskStatus.DONE then 1 else 0 end)) " +
            "from Task t join t.project p group by p.id, p.name order by p.name")
    List<CompletionRate> completionRate();
}
