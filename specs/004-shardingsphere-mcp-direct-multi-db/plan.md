# Implementation Plan: ShardingSphere MCP Direct Multi-Database Runtime

**Branch**: `004-shardingsphere-mcp-direct-multi-db` | **Date**: 2026-03-23 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/004-shardingsphere-mcp-direct-multi-db/spec.md)
**Input**: Feature specification from `/specs/004-shardingsphere-mcp-direct-multi-db/spec.md`

## Summary

在保持 MCP V1 公共 resources、tools、结果模型与错误模型不变的前提下，
把 runtime 从“单一直连数据库”扩展为“显式 logical database 拓扑 + 多数据库直连”。
该 follow-up 采用 fail-fast 启动校验、运行后单库故障隔离、last-good metadata snapshot
保留和按数据库粒度刷新，目标是在不依赖 ShardingSphere 中间层的情况下，
让一个 MCP 服务实例直接处理多个独立数据库。

## Technical Context

**Language/Version**: Java 17 in the MCP subchain  
**Primary Dependencies**: MCP Java SDK, repository-managed Jackson 2.16.1,
JDBC drivers loaded through distribution `ext-lib/`, existing `mcp/core` and
`mcp/bootstrap` runtime wiring  
**Storage**: Packaged runtime topology configuration under
`distribution/mcp/conf/mcp.yaml`, in-memory session state, in-memory metadata snapshots per logical database  
**Testing**: JUnit 5, bootstrap integration tests, core unit tests, direct multi-database E2E tests, scoped Maven verification  
**Target Platform**: Standalone MCP distribution on Linux or macOS, local or trusted-network deployment, direct connectivity to multiple independent supported databases  
**Project Type**: Java monorepo subproject with standalone distribution and dedicated E2E module  
**Constraints**: Keep the MCP V1 public contract unchanged; keep single-database transaction
semantics; no cross-database federation; fail-fast startup for invalid topology; preserve
per-database runtime isolation after startup  
**Scale/Scope**: Config model, runtime topology loading, metadata snapshot management, execution routing, refresh isolation, diagnostics, docs, and E2E coverage for multiple logical databases

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Smallest safe change**: PASS  
  本次变更保持现有 MCP 公共契约不变，只扩展 runtime 装配和运行边界。
- **Gate 2 - Runtime boundaries and security are explicit**: PASS  
  启动 fail-fast、单库故障隔离、单库事务边界、last-good snapshot 和凭据脱敏都已进入 spec。
- **Gate 3 - Verification path exists**: PASS  
  通过 config tests、bootstrap integration、core unit tests 和 direct multi-database E2E 可覆盖主要风险。
- **Gate 4 - Traceable contracts remain intact**: PASS  
  `004` 只扩展 runtime topology，不重写 `001` 的公开 resources/tools/result/error 契约。
- **Gate 5 - Governance baseline is bounded**: PASS WITH NOTE  
  `.specify/memory/constitution.md` 中关于非默认 `mcp` profile 的旧表述与当前仓库现状不完全一致；
  本 feature 以现有 `001/002/003` 已落地的 MCP 子链路为基线，不再扩大这一差异的影响面。

## Project Structure

### Documentation (this feature)

```text
specs/004-shardingsphere-mcp-direct-multi-db/
├── spec.md
├── research.md
├── plan.md
├── tasks.md
├── quickstart.md
└── checklists/
    └── requirements.md
```

### Source Code (repository root)

```text
distribution/mcp/src/main/resources/conf/mcp.yaml
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/MCPConfigurationLoader.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/lifecycle/MCPRuntimeLauncher.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/MCPLaunchRuntimeLoader.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/MCPRuntimeProvider.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/DatabaseRuntimeFactory.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/JdbcMetadataLoader.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/ShardingSphereExecutionAdapter.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/MetadataRefreshCoordinator.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/MetadataResourceLoader.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/session/MCPSessionManager.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/session/TransactionCommandExecutor.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/
mcp/core/src/test/java/org/apache/shardingsphere/mcp/
test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/
mcp/README.md
mcp/README_ZH.md
docs/mcp/ShardingSphere-MCP-Technical-Design.md
docs/mcp/ShardingSphere-MCP-Detailed-Design.md
```

**Structure Decision**: 不新增模块，继续在现有 `mcp/core`、`mcp/bootstrap`、
`distribution/mcp` 和 `test/e2e/mcp` 内完成实现；主要变更集中在 runtime 拓扑、配置、
按库隔离的 metadata/execution 行为与文档。

## Design Decisions

### 1. 保持公共 MCP 契约不变

- `list_databases`、`list_schemas`、`execute_query` 等工具继续使用现有参数与结果模型。
- 多数据库能力通过已有 `database` route key 放大，而不是重新设计协议层。

### 2. 用显式 logical database 拓扑代替单库 runtime props

- runtime 需要表达多个 logical database 绑定。
- 绑定项必须显式命名、全局唯一，并允许各自拥有数据库类型、schema 语义和 capability 覆盖。

### 3. 启动 fail-fast，运行后单库故障隔离

- 启动前必须完成全部绑定校验与初次 metadata 装载。
- 服务启动成功后，单个 logical database 的运行时失败不拖垮其他已装载数据库。

### 4. 保留 last-good metadata snapshot

- 运行中后端短时不可用时，只读 discovery 继续返回最近一次成功 snapshot。
- 实时执行和需要 live backend 的操作返回 `unavailable`。

### 5. 刷新按数据库粒度进行

- DDL / DCL 只替换目标 logical database 的 metadata snapshot。
- 无关数据库的 snapshot 不参与本次刷新，不被整体重建。

### 6. 保持单数据库事务边界

- 活动事务继续绑定到一个 logical database。
- 不引入跨数据库事务、跨数据库 savepoint 或 SQL federation。

## Branch Checklist

在实现前先固定关键分支与计划测试，避免 direct multi-database follow-up 出现覆盖盲区：

1. `startup_valid_topology`
   Planned test: direct multi-database config loading + initial metadata discovery integration
2. `startup_invalid_topology`
   Planned test: duplicate logical database, unsupported database type, missing driver, initial metadata load failure
3. `runtime_single_database_outage`
   Planned test: one logical database becomes unavailable after startup while others keep serving
4. `targeted_refresh_only`
   Planned test: DDL / DCL on database A refreshes only A and preserves B snapshot
5. `transaction_conflict_across_databases`
   Planned test: active transaction on A then route to B returns conflict or transaction_state_error

## Implementation Strategy

1. 扩展 runtime 配置模型，让 `mcp.yaml` 可以表达多个 logical database 绑定。
2. 在 bootstrap 层引入 direct runtime topology 装载、统一启动校验和 fail-fast 诊断。
3. 把 metadata snapshot 与执行路由改造成按 logical database 组织和隔离。
4. 在运行时保留 last-good snapshot，并为单库故障映射统一 `unavailable` 语义。
5. 收敛 DDL / DCL refresh、README、技术设计和 E2E 验收路径。

## Validation Strategy

- **Config topology loading**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPConfigurationLoaderTest,MCPLaunchRuntimeLoaderTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```
- **Core direct multi-database routing**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=ShardingSphereExecutionAdapterTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```
- **Bootstrap discovery and execution integration**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=DirectMultiDatabaseDiscoveryIntegrationTest,DirectMultiDatabaseExecuteQueryIntegrationTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```
- **E2E direct multi-database verification**
```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=DirectMultiDatabaseDiscoveryE2ETest,DirectMultiDatabaseAvailabilityE2ETest,DirectMultiDatabaseRefreshIsolationE2ETest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```
- **Scoped style checks for touched modules**
```bash
./mvnw -pl mcp/core,mcp/bootstrap,distribution/mcp,test/e2e/mcp -am \
  checkstyle:check -Pcheck -DskipITs -Dspotless.skip=true
```

## Rollout Notes

- 这是 runtime topology 扩展，不是 MCP 协议重写。
- `001-shardingsphere-mcp` 继续作为公共契约基线，`004` 只负责 direct multi-database runtime 行为。
- 若后续需要 cross-database federation 或 partial-startup semantics，应视为新的 feature，而不是在 `004` 中隐式扩容。
