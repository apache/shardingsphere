# ShardingSphere MCP SQL Execution Guardrails

## 1. Purpose

This note records the implemented review contract for the SQL execution guardrails in PR 38541.

The immediate goal is to prevent the same categories of issues from reappearing after each local fix:

- `database_gateway_execute_query` transaction compatibility.
- Read-only query admission and known side-effecting `SELECT` constructs.
- Logical database boundary enforcement when cross-schema SQL is not supported.
- SQL target collection for aliases, object lists, nested queries, DDL modifiers, DDL reference/destination objects, and DCL wildcard scopes.

## 2. Source-Driven Facts

- MCP `ToolAnnotations` are hints, not security guarantees. `readOnlyHint=true` says the tool does not modify its environment.
  Clients must not use annotations from untrusted servers as a security boundary.
  Source: https://modelcontextprotocol.io/specification/2025-11-25/schema
- JDBC `Connection.setReadOnly(true)` is a driver optimization hint and cannot be called during a transaction.
  Source: https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/Connection.html#setReadOnly(boolean)
- MySQL executable comments are parsed and executed by MySQL Server, so `/*! ... */` cannot be treated as an ordinary comment by the MCP classifier.
  Source: https://dev.mysql.com/doc/refman/8.4/en/comments.html
- MySQL `table_references` supports aliases, comma-separated table lists, partition clauses, index hints, joined tables, and derived tables.
  Source: https://dev.mysql.com/doc/refman/8.4/en/join.html
- MySQL multi-table `DELETE` can name delete targets before `FROM`, and table aliases must be handled in the table reference section.
  Source: https://dev.mysql.com/doc/refman/8.4/en/delete.html
- MySQL `ALTER TABLE ... RENAME` and `RENAME TABLE` can move a table to another database.
  Source: https://dev.mysql.com/doc/refman/8.4/en/rename-table.html
- PostgreSQL `CREATE INDEX` allows `CONCURRENTLY` and `IF NOT EXISTS` before the index name; `DROP INDEX` allows `CONCURRENTLY` and `IF EXISTS`.
  Sources: https://www.postgresql.org/docs/current/sql-createindex.html and https://www.postgresql.org/docs/current/sql-dropindex.html
- PostgreSQL `ALTER TABLE` supports `IF EXISTS`, `ONLY`, `RENAME TO`, and `SET SCHEMA`.
  Source: https://www.postgresql.org/docs/current/sql-altertable.html
- PostgreSQL `nextval` advances a sequence, and `setval` changes sequence state. Those calls are side effects even inside a `SELECT`.
  Source: https://www.postgresql.org/docs/current/functions-sequence.html
- PostgreSQL advisory lock functions acquire or release session-level or transaction-level locks.
  Source: https://www.postgresql.org/docs/current/functions-admin.html
- MySQL `GET_LOCK` acquires named locks that are not released by transaction commit or rollback; `RELEASE_LOCK` and `RELEASE_ALL_LOCKS` release locks.
  Source: https://dev.mysql.com/doc/refman/8.0/en/locking-functions.html
- MySQL user variables are session-specific, and MySQL still supports assignment outside `SET` with `:=` for backward compatibility.
  Source: https://dev.mysql.com/doc/refman/8.4/en/user-variables.html
- MySQL `LAST_INSERT_ID(expr)` remembers `expr` as the next value returned by `LAST_INSERT_ID()`.
  Source: https://dev.mysql.com/doc/refman/8.4/en/information-functions.html
- SQL Server `NEXT VALUE FOR` generates a sequence number.
  Source: https://learn.microsoft.com/en-us/sql/t-sql/functions/next-value-for-transact-sql
- Oracle `sequence.NEXTVAL` increments the sequence and returns the next value.
  Source: https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Sequence-Pseudocolumns.html

## 3. Execution Contract

### 3.1 Query Tool Contract

`database_gateway_execute_query` should accept only statements classified as:

- `QUERY`
- `EXPLAIN_ANALYZE` whose analyzed statement class is `QUERY`

It must reject statements or expressions that are syntactically a query but have known side effects:

- Locking reads such as `SELECT ... FOR UPDATE` and `LOCK IN SHARE MODE`.
- MySQL executable comments, because they can hide clauses from a normal comment skipper.
- Sequence advancement or sequence state mutation: PostgreSQL `nextval` / `setval`, SQL Server `NEXT VALUE FOR`, Oracle or compatible `sequence.NEXTVAL`.
- User-level or advisory locking functions: MySQL `GET_LOCK`, `RELEASE_LOCK`, `RELEASE_ALL_LOCKS`; PostgreSQL `pg_advisory_*lock*` and unlock variants.
- Session-state mutation: PostgreSQL `set_config`, MySQL user variable assignment with `:=`, MySQL `LAST_INSERT_ID(expr)`.
- PostgreSQL administrative functions that mutate replication slots, WAL, server processes, or configuration state. The denylist must be explicit,
  and tests should cover at least `pg_replication_slot_advance`, `pg_logical_slot_get_changes`, `pg_logical_emit_message`, `pg_switch_wal`,
  `pg_reload_conf`, `pg_cancel_backend`, and `pg_terminate_backend`.

Arbitrary database UDFs cannot be proven read-only from SQL text alone. The implementation must either document this residual limitation or use a much stricter allowlist model for functions.

### 3.2 Transaction Compatibility Contract

When the session already has an active transaction for the same logical database:

- `database_gateway_execute_query` should execute against the transaction-bound connection.
- It must not call `Connection.setReadOnly(true)` because JDBC disallows calling it during a transaction.
- It must not close, commit, or roll back the transaction-bound connection.
- It should still apply normal statement limits such as `max_rows` and `timeout_ms`.

When there is no active transaction:

- A read-only query may keep the current best-effort protection: set connection read-only, disable auto-commit if needed, execute, roll back, restore auto-commit, restore read-only state, then close.

### 3.3 Logical Database Boundary Contract

When `MCPDatabaseCapability.isSupportsCrossSchemaSql()` is `false`, execution must reject any statement that references or moves work outside the requested logical database.

The target resolver must collect:

- Direct DML and DDL targets.
- Source objects from `FROM`, `JOIN`, `USING`, `LIKE`, `ON`, `REFERENCES`, `INHERITS`, `INHERIT`, and nested subqueries.
- All CTE object references, including unused CTEs.
- Object lists with aliases, `AS`, partition clauses, index hints, and comma-separated entries.
- MySQL multi-table `DELETE` targets before `FROM`.
- PostgreSQL and MySQL DDL names after optional modifiers such as `CONCURRENTLY`, `IF EXISTS`, `IF NOT EXISTS`, and `ONLY`.
- Destination objects for boundary-moving DDL, such as `ALTER TABLE ... RENAME TO other_db.table` or `SET SCHEMA`.
- DCL wildcard scopes such as `*.*`, because they are global rather than database-local.
- Qualified function names and DDL object types such as `other_db.refresh_orders()`, `CREATE TYPE other_db.type_name`, and trigger/policy source tables.

Single-identifier `CREATE`, `ALTER`, and `DROP` for `DATABASE` or `SCHEMA` must be checked as logical-boundary objects, even when they do not contain a dot.

## 4. MCP Design Check

The MCP design should preserve the two-tool split:

- `database_gateway_execute_query` remains the inspection-oriented tool.
- `database_gateway_execute_update` remains the explicit preview/execute tool for DML, DDL, DCL, transaction control, savepoints, and side-effecting `EXPLAIN ANALYZE`.

Tool descriptions and recovery messages must not overstate the guarantee. They should say the query tool accepts only classifier-approved query forms
and rejects known side-effecting SQL forms. They should not claim authentication, authorization, or a universal database-level read-only guarantee.

`readOnlyHint=true` is acceptable only if the project accepts the residual UDF limitation as a documented trade-off. If the project requires
annotation-level strictness, the safer design is to change the annotation to `readOnlyHint=false` or introduce a restrictive function allowlist.

## 5. Test Mapping

The implementation should add or update focused tests for:

- Active transaction read-only query uses the transaction connection and does not call `setReadOnly`, `rollback`, `commit`, or `close`.
- Non-transaction read-only query still restores connection state and rolls back best-effort execution.
- Alias and object list references: `FROM logic_db.t a, other_db.u b`, `UPDATE logic_db.t a, other_db.u b`.
- Partition and index hint tails: `FROM logic_db.t PARTITION (p0), other_db.u`.
- MySQL multi-table `DELETE`: `DELETE other_db.t FROM logic_db.t JOIN ...` and `DELETE logic_db.t, other_db.u FROM ...`.
- DDL modifiers: `CREATE INDEX CONCURRENTLY IF NOT EXISTS other_db.idx ON logic_db.t`, `DROP INDEX CONCURRENTLY IF EXISTS other_db.idx`, `ALTER TABLE IF EXISTS other_db.t`.
- Boundary-moving DDL: `ALTER TABLE logic_db.t RENAME TO other_db.t2`.
- DDL reference objects: `REFERENCES other_db.t`, `INHERITS (other_db.t)`, `INHERIT other_db.t`, `CREATE TYPE other_db.type_name`,
  and `CREATE TRIGGER ... ON other_db.t`.
- `CREATE/DROP/ALTER DATABASE other_db` and `CREATE/DROP/ALTER SCHEMA other_db`.
- Unused CTE references: `WITH unused AS (SELECT * FROM other_db.t) SELECT * FROM logic_db.t`.
- MySQL executable comments hiding joins or locking reads.
- Side-effecting query forms: `nextval`, `setval`, `NEXT VALUE FOR`, `sequence.NEXTVAL`, `GET_LOCK`, `RELEASE_LOCK`, `pg_advisory_lock`,
  `set_config`, selected PostgreSQL administrative functions, MySQL `@v :=`, and `LAST_INSERT_ID(expr)`.
- DCL global wildcard rejection when cross-schema SQL is disabled.

## 6. Decision

The implemented path is Option A:

- Keep the query tool usable.
- Reject known side-effecting constructs.
- Document that arbitrary UDFs and view definitions cannot be proven read-only from SQL text alone.

This follows the transaction compatibility priority: active-transaction query execution uses the transaction connection and skips JDBC connection-level
read-only mode.
