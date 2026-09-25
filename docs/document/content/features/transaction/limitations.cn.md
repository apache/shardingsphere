+++
title = "使用限制"
weight = 2
+++

虽然 Apache ShardingSphere 希望能够完全兼容所有的分布式事务场景，并在性能上达到最优，但在 CAP 定理所指导下，分布式事务必然有所取舍。
Apache ShardingSphere 希望能够将分布式事务的选择权交给使用者，在不同的场景使用最适合的分布式事务解决方案。

## LOCAL 事务

### 不支持项

* 不支持因网络、硬件异常导致的跨库事务。例如：同一事务中，跨两个库更新，更新完毕后、未提交之前，第一个库宕机，则只有第二个库数据提交，且无法回滚。
* ShardingSphere-Proxy 使用 PostgreSQL 时，事务中仅支持 `CREATE TABLE`、`ALTER TABLE`、`DROP TABLE` 和 `RENAME TABLE`，其元数据刷新延迟到事务结束后执行。
  其余会改变元数据的 DDL 语句均不支持，包括 schema、视图和索引语句。ShardingSphere-Proxy 使用 openGauss 时，事务中不支持任何会改变元数据的 DDL 语句。
  不支持的语句会在执行前以 SQLSTATE `0A000` 拒绝，避免事务回滚后存储数据库与 ShardingSphere 元数据不一致。
* 同一事务中，后续语句无法引用该事务内新建的表，因为该表的元数据只在事务结束时才生效。
  例如 `BEGIN; CREATE TABLE t (...); ALTER TABLE t ...;` 会在第二条语句上失败。
* 上述表 DDL 支持仅在语句路由到的每个存储单元都将 DDL 纳入事务时生效。
  PostgreSQL 协议的会话若路由到隐式提交 DDL 的存储库（如 MySQL），仍保留该限制，因为存储库会立即提交语句，而 ShardingSphere 元数据仍处于延迟刷新状态。

## XA 事务

### 不支持项

* 服务宕机后，在其它机器上恢复提交/回滚中的数据；
* MySQL 事务块内，SQL 执行出现异常，执行 `Commit`，数据保持一致；
* 配置 XA 事务后，存储单元名称最大长度不超过45个字符。
* PostgreSQL 和 openGauss 的元数据变更 DDL 支持范围与限制，与 LOCAL 事务中所述一致。

## BASE 事务

### 不支持项

* 不支持隔离级别。
* PostgreSQL 和 openGauss 在事务中不支持会改变元数据的 DDL 语句。
  LOCAL 和 XA 事务所支持的表 DDL，在 BASE 事务中不支持。
