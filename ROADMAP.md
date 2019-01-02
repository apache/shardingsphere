# Roadmap

## Sharding-JDBC

### JDBC
- [x] Data Source
- [x] Connection
- [x] Connection Metadata
- [x] Statement
- [x] Prepared Statement
- [x] Result Set
- [ ] Result Set Metadata

### Database
- [x] MySQL
- [x] Oracle
- [x] SQLServer
- [x] PostgreSQL

### Configuration
- [x] Java API
- [x] YAML
- [x] Spring Namespace
- [x] Spring Boot Starter
- [x] Inline expression

## Sharding-Proxy

### Database
- [x] MySQL
  - [x] Handshake Packet
  - [x] OK Packet
  - [x] ERR Packet
  - [x] EOF Packet
  - [x] COM_QUIT Packet
  - [x] COM_INIT_DB Packet
  - [x] COM_QUERY Packet
  - [x] COM_FIELD_LIST Packet
  - [x] COM_STMT_PREPARE
  - [x] COM_STMT_EXECUTE
  - [x] COM_STMT_CLOSE
  - [x] COM_STMT_RESET
- [ ] Oracle
- [ ] SQLServer
- [ ] PostgreSQL

### Configuration
- [x] YAML
- [x] Inline expression

## Sharding-Sidecar
- [ ] TODO

## Kernel

### SQL
- [x] DQL
    - [x] Simple DQL
    - [x] JOIN
    - [x] BETWEEN
    - [x] IN
    - [x] ORDER BY
    - [x] GROUP BY
    - [x] Aggregation Functions
    - [x] LIMIT, rownum, TOP
    - [x] Simple Sub Query
    - [x] OR
    - [x] DISTINCT
    - [ ] HAVING
    - [ ] UNION, UNION ALL
    - [ ] Calculate Expression, eg: SUM(pv) / COUNT(uv)
    - [ ] Complicated Sub Query
    - [ ] SQL Hint
- [x] DML
    - [x] INSERT INTO
    - [x] INSERT SET
    - [x] UPDATE
    - [x] DELETE
    - [x] INSERT INTO VALUES (xxx), (xxx)
    - [ ] UPDATE Multiple Tables
    - [ ] DELETE Multiple Tables
- [x] DDL
    - [x] CREATE TABLE
    - [x] ALTER
    - [x] DROP
    - [x] TRUNCATE
    - [ ] CREATE VIEW
    - [x] CREATE INDEX
    - [ ] CREATE OR REPLACE
- [x] TCL
    - [x] SET
    - [x] COMMIT
    - [x] ROLLBACK
    - [x] SAVEPONIT
    - [x] BEGIN
- [x] MySQL database administrator command
    - [x] USE
    - [x] SHOW DATABASES
    - [x] SHOW TABLES
    - [x] DESCRIBE & DESC

### SQL Parse
- [x] Lexer
- [x] Standard Parser
- [ ] Multiple SQL Parser
- [ ] Duplicate Parentheses

### SQL Rewrite
- [x] LIMIT Offset Rewrite
- [x] AVG To SUM/COUNT Rewrite
- [x] ORDER BY Derived Columns Rewrite
- [x] GROUP BY Derived Columns Rewrite
- [x] INSERT Derived Primary Key Rewrite
- [x] GROUP BY Only Optimized Rewrite

### Route
- [x] Standard Router
- [x] Cartesian Router
- [x] Unicast Router
- [x] Broadcast Router
- [x] Hint Router

### Merge
- [x] Streaming Merger
- [x] Memory Merger
- [x] Decorator Merger
- [x] Metadata Merger

### Sharding
- [x] Databases
- [x] Tables
- [x] Default Data Source
- [x] Broadcast Tables

### Read-Write Split
- [x] Read Write Split
- [x] Consistent with Same Thread
- [x] Force Hint Master Database
- [x] Multiple Slaves Replica
- [ ] Multiple Masters Replica

### Distribute Sequence
- [x] Strategy API
- [x] Snowflake algorithm

## Orchestration

### Registry Center
- [x] Zookeeper
- [x] Etcd
- [ ] Eureka

### Dynamic configuration
- [x] Dynamic Data Source
- [x] Dynamic Sharding Strategy

### Government
- [x] Circuit breaker
- [x] Enable/Disable Data Source
- [ ] Health Check
- [ ] LoadBalance
- [ ] Flow Limit
- [ ] Failover

### APM
- [x] Tracing Collector
- [x] Open Tracing

## BASE Transaction

### Best Efforts Delivery
- [x] Post DML event
- [x] Transaction Journal Storage
- [x] Synchronized Delivery
- [x] Asynchronized Delivery

### Try Confirm Cancel
- [x] Post DML event
- [ ] Transaction Journal Storage
- [ ] Try
- [ ] Confirm
- [ ] Cancel

## Operation Tool

### Auto Scale Out
- [ ] Bin Log Parse
- [ ] Redo Log Parse
- [ ] Data Migration

### Administrator Console
- [ ] Configuration Panel
- [ ] Runtime Control Panel
- [ ] Tracing Panel
- [ ] Metrics Panel
- [ ] RBAC
