-- Run as the app schema user AFTER the app has started once (so tables exist)
-- and after registering a few users + creating a few projects.

-- 1) Seed 50,000 tasks so index differences become visible
DECLARE
  TYPE t_ids IS TABLE OF NUMBER;
  u t_ids; p t_ids;
BEGIN
  SELECT id BULK COLLECT INTO u FROM users;
  SELECT id BULK COLLECT INTO p FROM projects;
  FOR i IN 1..50000 LOOP
    INSERT INTO tasks (title, status, priority, due_date, project_id, assignee_id, created_at)
    VALUES ('Seed task ' || i,
            CASE MOD(i,3) WHEN 0 THEN 'TODO' WHEN 1 THEN 'IN_PROGRESS' ELSE 'DONE' END,
            'MEDIUM', TRUNC(SYSDATE) + MOD(i,120) - 60,
            p(MOD(i, p.COUNT) + 1), u(MOD(i, u.COUNT) + 1), SYSTIMESTAMP);
  END LOOP;
  COMMIT;
END;
/
EXEC DBMS_STATS.GATHER_TABLE_STATS(USER, 'TASKS');

-- 2) BEFORE: drop the indexes Hibernate created, look at the plan (expect TABLE ACCESS FULL)
DROP INDEX idx_tasks_assignee;
DROP INDEX idx_tasks_status;
DROP INDEX idx_tasks_due_date;

SET TIMING ON
EXPLAIN PLAN FOR
  SELECT * FROM tasks WHERE assignee_id = (SELECT MIN(id) FROM users) AND status = 'DONE';
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);

EXPLAIN PLAN FOR
  SELECT * FROM tasks WHERE due_date < TRUNC(SYSDATE) AND status <> 'DONE';
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);

-- 3) AFTER: recreate indexes (a composite one helps the common board filter)
CREATE INDEX idx_tasks_assignee ON tasks(assignee_id);
CREATE INDEX idx_tasks_status   ON tasks(status);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_assignee_status ON tasks(assignee_id, status);
EXEC DBMS_STATS.GATHER_TABLE_STATS(USER, 'TASKS');

EXPLAIN PLAN FOR
  SELECT * FROM tasks WHERE assignee_id = (SELECT MIN(id) FROM users) AND status = 'DONE';
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);   -- expect INDEX RANGE SCAN
