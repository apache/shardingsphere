# Roadmap

## Database
- [x] MySQL
- [x] Oracle
- [x] SQLServer
- [x] PostgreSQL

## SQL
- [x] DQL
    - [x] Simple
    - [x] JOIN
    - [x] BETWEEN
    - [x] IN
    - [x] ORDER BY
    - [x] GROUP BY
    - [x] Aggregation
    - [x] LIMIT, rownum, TOP
    - [x] Simple Sub Query
    - [ ] DISTINCT
    - [ ] HAVING
    - [ ] OR
    - [ ] UNION, UNION ALL
    - [ ] Calculate Expression, eg: SUM(pv) / COUNT(uv)
    - [ ] Complicated Sub Query
    - [ ] SQL Hint
- [x] DML
    - [x] INSERT INTO
    - [x] INSERT SET
    - [x] UPDATE
    - [x] DELETE
    - [ ] INSERT INTO VALUES (xxx), (xxx)
    - [ ] UPDATE Multiple Tables
    - [ ] DELETE Multiple Tables
- [x] DDL
    - [x] CREATE TABLE
    - [x] ALTER
    - [x] DROP
    - [x] TRUNCATE
    - [ ] CREATE VIEW
    - [ ] CREATE INDEX
    - [ ] CREATE OR REPLACE

## Configuration
- [x] Java API
- [x] Spring namespace
- [x] YAML
- [x] Independent Read Write Split
- [ ] Improve Binding Table
- [x] Configuration Center
- [x] Dynamic Configuration

## SQL Parser
- [x] Lexer
- [x] Parser
- [ ] Multiple SQL Parser
- [ ] Duplicate Parentheses

## SQL Rewrite
- [x] Correct Rewrite
- [x] Optimize Rewrite

## Route
- [x] Hint
- [x] Simple
- [x] Cartesian

## Merge
- [x] Streaming
- [x] Memory
- [x] Decorator

## Sharding
- [x] Database
- [x] Table
- [x] Default Data Source

## Read Write Split
- [x] Read Write Split
- [x] Consistent with Same Thread
- [x] Force Hint Master Database

## Distribute Primary Key
- [x] JDBC
- [x] Strategy API
- [x] Snowflake

## BASE Transaction
- [x] BED
- [ ] TCC

## Orchestration
- [ ] Health Check
- [ ] Switch Data Source
- [ ] Flow Limit

## Operator
- [ ] Dictionary Broadcast
- [ ] Dynamic Scale Out
