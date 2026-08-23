# Issue #27152 剩余实现计划：Oracle ALTER TABLE EXCHANGE PARTITION + 测试用例补齐

## 目标（完整关闭 issue #27152 的剩余缺口）
1. Oracle `ALTER TABLE t EXCHANGE PARTITION p WITH TABLE t2 [(INCLUDING|EXCLUDING) INDEXES] [(WITH|WITHOUT) VALIDATION]` 语法支持 + 语义建模
2. 三组 Oracle parser 测试用例：alter-table exchange partition、insert dblink、lock table

## 非目标
- 不改 MySQL/Doris/Hive 的 EXCHANGE PARTITION（保持其现状纯语法覆盖）
- 不为 INCLUDING/EXCLUDING INDEXES、WITH/WITHOUT VALIDATION、updateIndexClauses、parallelClause 建模（仅语法解析）
- 不做 CASCADE 及多级分区高级子句
- 不做任何远程 GitHub 写操作（issue 回复/关单/PR 由用户执行，最后提供 commit message 与回复草稿）
- 不改 binder/rewriter/routing 下游（无消费需求）

## Architecture Gate 报告（语义建模触及共享模型）
- Owner：`parser/sql/statement/core` 的 `AlterTableStatement`（全方言共享）与 `segment/ddl/partition/` 目录（现有 4 个分区 segment 全在 core，即便仅单方言消费，放 core 对齐先例）
- 兼容性：纯增量。新增 segment 类 + `AlterTableStatement` 加 `@Singular("exchangePartitionDefinition") Collection<ExchangePartitionDefinitionSegment>` 字段（该类为 Lombok `@Builder` 私有构造，public builder API 向后兼容）
- 复用：`PartitionSegment`、`SimpleTableSegment`、`AlterDefinitionSegment`、`ExpectedPartition`、`PartitionAssert`、`AbstractExpectedSQLSegment`
- 其余 10 个 `alterTablePartitioning` 分支的 visitor 行为保持不变（仍不产生 segment）

## 语法设计（对齐 Oracle 官方语法与仓库现有分支模式）
```antlr
alterTablePartitioning
    : ...(现有 11 分支)
    | exchangePartitionTable
    ;

exchangePartitionTable
    : EXCHANGE (partitionExtendedName | subpartitionExtendedName) WITH TABLE tableName
      ((INCLUDING | EXCLUDING) INDEXES)? ((WITH | WITHOUT) VALIDATION)? (updateIndexClauses parallelClause?)?
    ;
```
所需关键字（EXCHANGE/INCLUDING/EXCLUDING/VALIDATION/WITHOUT）均已在 `OracleKeyword.g4` 定义且为非保留字；`partitionExtendedName`、`subpartitionExtendedName`、`updateIndexClauses`、`parallelClause` 规则已存在。生成代码不入库，Maven 构建时自动重新生成。

## 改动文件清单（13 个）
生产（4）：
1. `parser/sql/engine/dialect/oracle/src/main/antlr4/imports/oracle/DDLStatement.g4` — 上述语法（~6 行）
2. `parser/sql/statement/core/.../segment/ddl/partition/ExchangePartitionDefinitionSegment.java` — 新建（startIndex/stopIndex/partition(可空)/exchangeTable，实现 AlterDefinitionSegment，~40 行）
3. `parser/sql/statement/core/.../statement/type/ddl/table/AlterTableStatement.java` — +1 字段
4. `parser/sql/engine/dialect/oracle/.../type/OracleDDLStatementVisitor.java` — `visitAlterDefinitionClause` 增加对 `alterTablePartitioning` 的访问（仅 exchange 分支产生 segment）；新增 `visitExchangePartitionTable` 与 `visitPartitionExtendedName`（`PARTITION name` → PartitionSegment；`PARTITION FOR (...)` → partition 为 null；SUBPARTITION 形式同样 partition 为 null）

测试（9）：
5. `test/it/parser/src/main/resources/sql/supported/ddl/alter-table.xml` — +3 用例：basic、`PARTITION FOR (TO_DATE(...))` + INCLUDING INDEXES WITHOUT VALIDATION、EXCLUDING INDEXES WITH VALIDATION
6. `test/it/parser/src/main/resources/case/ddl/alter-table.xml` — +3 期望（`<table>` + `<exchange-partition>` 子树：partition + exchange-table）
7. `test/it/parser/.../jaxb/segment/impl/definition/ExpectedExchangePartitionDefinition.java` — 新建（照 `ExpectedAddPartitionDefinition` 模式：partition + exchange-table 子节点）
8. `test/it/parser/.../AlterTableStatementTestCase.java` — +`@XmlElement(name = "exchange-partition")`
9. `test/it/parser/.../asserts/.../AlterTableStatementAssert.java` — +断言方法（复用 PartitionAssert/TableAssert）
10. `test/it/parser/src/main/resources/sql/supported/dml/insert.xml` — +1：`INSERT INTO remote_table@link SELECT * FROM local_table`（issue 原文）
11. `test/it/parser/src/main/resources/case/dml/insert.xml` — +1 期望（table name=remote_table、stop-index 覆盖 `remote_table@link`，照 `select_with_table_dblink_oracle`/`insert_with_select_subquery` 先例）
12. `test/it/parser/src/main/resources/sql/supported/lcl/lock.xml` — +1：`LOCK TABLE interval_sales PARTITION FOR (TO_DATE('01-JUN-2007','dd-MON-yyyy')) IN SHARE MODE`（issue 原文；Oracle `visitLock` 返回空 tables）
13. `test/it/parser/src/main/resources/case/tcl/lock.xml` — +1 空 `<lock/>` 期望（照 `lock_instance_for_backup` 先例）

## 实施步骤
1. 记录 `git status --short` 基线（现有 3 个未跟踪 `__pycache__` 目录保持不动）
2. 改 g4 语法 → 编译验证重新生成无冲突
3. 新建 core segment + AlterTableStatement 字段
4. Oracle visitor 三处改动
5. test/it/parser 的 JAXB + assert 扩展
6. 三组测试用例 XML（start/stop-index 按资源文件原文逐一计数）
7. 分层验证（见下）
8. Completion Loop：task-delta 审计、spotless/checkstyle、正确性自审（含 start/stop-index 复核）

## 验证命令（按 token-efficiency 契约用 log-file wrapper，自底向上）
1. `./mvnw -pl parser/sql/statement/core -DskipTests -Dspotless.skip=true install`
2. `./mvnw -pl parser/sql/engine/dialect/oracle -DskipITs -Dspotless.skip=true test`
3. `./mvnw -pl parser/sql/engine/dialect/oracle -DskipTests install`（供 IT 取到含新语法的构件；若 IT 仍拉到旧 SNAPSHOT 则改用 `-am` 并记录理由=reactor freshness）
4. `./mvnw -pl test/it/parser -Dtest=org.apache.shardingsphere.test.it.sql.parser.oracle.InternalOracleParserIT -Dsurefire.failIfNoSpecifiedTests=false test`
5. `./mvnw spotless:apply -Pcheck -T1C` → `./mvnw checkstyle:check -Pcheck -T1C`

## 规模与风险
- 预估新增 ~180 行（生产 ~90 / 测试 ~90），远低于 10,000 行任务上限
- 风险 1：test/it/parser 取 parser SNAPSHOT 的方式（本地仓库 vs reactor）——步骤 3/4 编码时按实际裁剪
- 风险 2：`PARTITION FOR` 形式 partition 为 null 的断言——期望节点省略 `<partition>` 子节点即可表达
- 完成后提供 commit message（`Support ALTER TABLE EXCHANGE PARTITION parsing in Oracle (#27152)` 方向）与 issue 回复草稿，Git 提交与远程操作由用户执行