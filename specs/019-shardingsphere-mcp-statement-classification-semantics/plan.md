# Implementation Plan: ShardingSphere MCP Statement Classification Semantics

**Branch**: `no-branch-switch-requested` | **Date**: 2026-04-12 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/019-shardingsphere-mcp-statement-classification-semantics/spec.md)
**Input**: Feature specification from `/specs/019-shardingsphere-mcp-statement-classification-semantics/spec.md`

## Summary

本特性把 `execute_query` 里的“语句是什么类型”重新拆成一套诚实的产品语义：

- `WITH` 不再被当成 `QUERY`
- `statement class` 负责副作用和治理语义
- `statement type` 负责具体动作说明
- `result kind` 负责返回形状
- data-modifying CTE 可以被表达成 `DML + result_set`

这会让 capability gate、audit 和执行链
都回到基于真实副作用做决策，而不是基于字符串前缀。

## Technical Context

**Language/Version**: Java 17 in the MCP subchain  
**Primary Dependencies**: repository-owned MCP core runtime, ShardingSphere parser assets, JDBC execution pipeline  
**Storage**: in-memory metadata catalog and execution responses  
**Testing**: statement classifier tests, facade tests, JDBC executor tests, response-payload tests, doc/contract alignment review  
**Target Platform**: ShardingSphere MCP standalone runtime on embedded Tomcat / STDIO  
**Project Type**: Java monorepo subproject under `mcp/core`, plus docs/specs follow-up  
**Constraints**: no branch switch; keep current branch; no V1 SQL-surface expansion; no string-prefix-only fix; schema semantics out of scope  
**Scale/Scope**: classifier, facade, statement executor/response, audit marker, docs/specs alignment

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Simplicity**: PASS  
  本轮不引入新的 SQL 类别，只把已有产品语义拆分到正确维度，
  符合 [CODE_OF_CONDUCT.md](/Users/zhangliang/IdeaProjects/shardingsphere/CODE_OF_CONDUCT.md#L8)
  到 [CODE_OF_CONDUCT.md](/Users/zhangliang/IdeaProjects/shardingsphere/CODE_OF_CONDUCT.md#L14)
  对简洁、一致和可读性的要求。
- **Gate 2 - Smallest safe change**: PASS  
  只修正 statement classification contract，
  不同时夹带 auth、schema 或 transport 改造。
- **Gate 3 - Honest contract**: PASS  
  通过拆分 `statement_class / statement_type / result_kind`
  提升契约诚实度，而不是继续靠文案模糊描述规避问题。
- **Gate 4 - Parser-first semantics**: PASS  
  放弃 `WITH -> QUERY` 的 prefix 规则，转向可验证的语义分析。
- **Gate 5 - Verification path exists**: PASS  
  分类器、facade、response payload 和文档合同都有 scoped verification 路径。

## Hard Constraint Checklist

- 不切换分支，只在当前 `001-shardingsphere-mcp` 上梳理和后续实现
- `WITH` 不得继续作为默认 `QUERY` 别名
- 不新增 V1 允许的 SQL 类型
- `SupportedMCPStatement` 现有类别优先复用
- 必须拆分 `statement class` 与 `result kind`
- 必须能覆盖 SQL Server CTE-prefixed DML
- 必须能覆盖 PostgreSQL / openGauss data-modifying CTE
- 不能用 prefix-only 规则作为最终方案
- 顶层 spec、PRD、technical design、detailed design 和 tool contract 必须对齐

## Project Structure

### Documentation (this feature)

```text
specs/019-shardingsphere-mcp-statement-classification-semantics/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── execute-query-classification-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/StatementClassifier.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/ClassificationResult.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPSQLExecutionFacade.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPJdbcStatementExecutor.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/response/SQLExecutionResponse.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/audit/AuditRecorder.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/execute/StatementClassifierTest.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPSQLExecutionFacadeTest.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPJdbcStatementExecutorTest.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/protocol/SQLExecutionResponseTest.java
docs/mcp/ShardingSphere-MCP-PRD.md
docs/mcp/ShardingSphere-MCP-Technical-Design.md
docs/mcp/ShardingSphere-MCP-Detailed-Design.md
specs/001-shardingsphere-mcp/spec.md
specs/001-shardingsphere-mcp/data-model.md
specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md
```

**Structure Decision**: 优先在现有分类器和响应模型上做最小必要收敛，
不新增模块；
若 parser 接入需要辅助类，也应局限在 `mcp/core` 的 execute 子域。

## Design Decisions

### 1. 用“真实副作用”定义 `statement class`

- capability gate、audit 和执行分支都应该先看语义副作用
- 只要任一 CTE 或主语句存在数据修改，整条语句就不再是纯 `QUERY`

### 2. `statement type` 保留具体动作语义

- 继续保留 `SELECT`、`UPDATE`、`MERGE` 这类具体表达
- 不让 `statement type` 去承担治理职责

### 3. `result kind` 只描述返回形状

- 允许 `DML + result_set`
- 不再把 `QUERY -> result_set`、`DML -> update_count` 当成不可打破的固定映射

### 4. 推荐在成功 payload 中增加 `statement_class`

- 这是对现有返回的补充，不是替换
- 可显著降低 Agent 把 `statement_type=SELECT` 误读为只读查询的风险

### 5. 分类器应采用 parser/AST 级语义识别

- 简单扫描首关键字不够
- 简单扫描 `WITH` 之后的主语句关键字也不够
- 需要识别 data-modifying CTE 这类“外层查、内部写”的语句

## Branch Checklist

1. `plain_with_select_remains_query`  
   Planned verification: `StatementClassifierTest`
2. `sqlserver_cte_prefixed_dml_is_classified_as_dml`  
   Planned verification: `StatementClassifierTest`
3. `data_modifying_cte_select_is_governed_as_dml`  
   Planned verification: `StatementClassifierTest` and `MCPSQLExecutionFacadeTest`
4. `dml_can_return_result_set_without_losing_write_semantics`  
   Planned verification: `MCPJdbcStatementExecutorTest` and `SQLExecutionResponseTest`
5. `audit_and_capability_gate_consume_statement_class`  
   Planned verification: `MCPSQLExecutionFacadeTest`

## Implementation Strategy

1. 先在 `StatementClassifier` 层明确新的语义输出：
   至少区分 `statement class` 与 `statement type`，
   并识别 data-modifying CTE。
2. 调整 `ClassificationResult`，
   让 facade 和 executor 能拿到完整语义，而不是只拿首关键字推导。
3. 调整 `MCPSQLExecutionFacade`，
   让 capability gate 和 audit marker 消费语义分类结果。
4. 调整 `MCPJdbcStatementExecutor` 与 `SQLExecutionResponse`，
   让返回形状不再被 `statement class` 单向绑死。
5. 如有需要，为成功 payload 增加 `statement_class` 字段。
6. 更新顶层 spec、PRD、technical design、detailed design 与 tool contract，
   统一说明 `WITH` 的产品语义。
7. 用 SQL Server 与 PostgreSQL / openGauss 反例补齐测试。

## Validation Strategy

- **Classifier verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=StatementClassifierTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Facade and audit verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPSQLExecutionFacadeTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Executor / response verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPJdbcStatementExecutorTest,SQLExecutionResponseTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Scoped style checks**
```bash
./mvnw -pl mcp/core -am -Pcheck -DskipITs -DskipTests \
  checkstyle:check spotless:check
```

## Rollout Notes

- 这不是在“支持更多 SQL”，而是在“把已经允许的 SQL 解释正确”。
- 对外最重要的变化不是语法兼容，而是契约更诚实。
- 如果后续决定不对 payload 增加 `statement_class`，
  需要明确记录替代方案如何避免 Agent 误判只读与写操作。
