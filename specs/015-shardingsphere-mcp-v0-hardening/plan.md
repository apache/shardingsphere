# Implementation Plan: ShardingSphere MCP V0 Surface Hardening

**Branch**: `no-branch-switch-requested` | **Date**: 2026-04-07 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/015-shardingsphere-mcp-v0-hardening/spec.md)
**Input**: Feature specification from `/specs/015-shardingsphere-mcp-v0-hardening/spec.md`

## Summary

本轮 V0 不扩功能，只做提交前必须立即收口的四件事：

- public surface 改成 resource-only 真口径
- 修 `execute_query` 截断语义
- 收紧 tool 参数校验
- 收紧 HTTP 默认安全边界，非 loopback 绑定必须显式声明远程访问意图

不会切换分支，不恢复旧 tools，不引入新的安全配置开关。

## Technical Context

**Language/Version**: Java 17 in the MCP subchain  
**Primary Dependencies**: repository-owned MCP core runtime, MCP Java SDK integration, ShardingSphere YAML config swapper  
**Storage**: in-memory metadata/session state  
**Testing**: scoped `mcp/core` and `mcp/bootstrap` unit/integration tests plus README/contract review  
**Target Platform**: ShardingSphere MCP standalone runtime on embedded Tomcat / STDIO  
**Project Type**: Java monorepo subproject under `mcp/core`, `mcp/bootstrap`, `distribution/mcp`, and `specs/`  
**Constraints**: no branch switch; no old tool revival; no unrelated cleanup; no new remote HTTP escape hatch; docs/contract and implementation must stay traceable  
**Scale/Scope**: two core behavior fixes, one bootstrap config hardening, public docs/contract alignment

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Smallest safe change**: PASS  
  本轮只修 correctness、validation、default security baseline 与 contract truthfulness，不扩大到 metadata model 或 remote platformization。
- **Gate 2 - Explicit governance and security**: PASS  
  默认 loopback 边界、显式 remote intent、resource-only public surface 与 invalid-request rules 都被显式记录。
- **Gate 3 - Testable delivery**: PASS  
  四个 V0 问题都有对应 scoped verification。
- **Gate 4 - Traceable contracts**: PASS  
  README、quickstart、domain contract 与实现会同步收口到同一 public surface。
- **Gate 5 - Quality gates**: PASS  
  `mcp/core` 与 `mcp/bootstrap` 都有 scoped test/style 命令。

## Hard Constraint Checklist

- 不切换分支，只在当前工作树完成 Speckit 设计和实现
- 不恢复已删除 metadata tools
- 必须把 public surface 文档收口到 resource-only
- 必须修正 `execute_query` 的 `truncated` 语义
- 必须让 required string args 的空白值返回 `invalid_request`
- 必须让非法 `object_types` 显式失败，不能静默扩大查询范围
- 必须让 HTTP `bindHost` 默认只接受 loopback
- 非 loopback `bindHost` 必须通过 `allowRemoteAccess: true` 显式声明远程访问意图
- 必须用 scoped tests 和 style checks 验证 touched modules

## Project Structure

### Documentation (this feature)

```text
specs/015-shardingsphere-mcp-v0-hardening/
├── plan.md
├── spec.md
└── tasks.md
```

### Source Code (repository root)

```text
mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/MCPJdbcStatementExecutor.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolController.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/request/MCPToolArguments.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapper.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/execute/MCPJdbcStatementExecutorTest.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/MCPToolControllerTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapperTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/loader/MCPConfigurationLoaderTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/LoopbackOriginSecurityValidatorTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/ProductionExecuteQueryIntegrationTest.java
specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md
specs/001-shardingsphere-mcp/quickstart.md
mcp/README_ZH.md
mcp/README.md
```

**Structure Decision**: 不新增模块；V0 只在现有 `core`、`bootstrap`、`specs/001` 与 README 上做最小安全修改。

## Design Decisions

### 1. metadata public surface 只保留 resource-only 叙述

- `search_metadata` 继续保留为唯一 metadata search tool
- `execute_query` 继续保留为唯一 SQL execution tool
- `describe_table`、`describe_view`、`get_capabilities` 统一改为 resource 说明，不再在 public tools 中声明

### 2. 用 “多取一行” 修正截断判断

- `Statement#setMaxRows(...)` 在有 `max_rows` 时设置为 `max_rows + 1`
- 结果集组装仍只返回前 `max_rows` 行
- 如果额外探测到第 `max_rows + 1` 行，则标记 `truncated=true`

这样既保留 JDBC 侧的结果上限，也修复当前永远感知不到 overflow 的问题。

### 3. required string 参数统一做 blank 校验

- `MCPToolController` 继续负责 descriptor-level required validation
- 对 required `STRING` 字段，除了 key 存在，还必须非 blank
- 这样 `database=""`、`sql="   "`、`query="   "` 都会在入口层稳定返回 `invalid_request`

### 4. 非法 `object_types` 不再静默降级

- `search_metadata` 的 `object_types` 收紧为当前 public search surface 支持的六类：
  `database`、`schema`、`table`、`view`、`column`、`index`
- `MCPToolArguments` 解析时一旦发现超出该白名单的值，立即抛 `MCPInvalidRequestException`
- 保持合法过滤语义不变；不在本轮改变 index unsupported 的现有行为

### 5. HTTP 配置层要求显式允许非 loopback `bindHost`

- `YamlHttpTransportConfigurationSwapper` 在 `swapToObject(...)` 阶段校验 `bindHost`
- 默认只允许 `127.0.0.1`、`localhost`、`::1`
- 如果 `bindHost` 是非 loopback，必须设置 `allowRemoteAccess: true`
- 这样默认发行包、配置文件和 runtime 行为都明确落在本地调试边界内

## Branch Checklist

1. `resource_only_contract_is_truthful`
   Planned verification: README、quickstart、domain contract review 不再声明旧 metadata tools
2. `execute_query_truncation_is_detectable`
   Planned verification: `MCPJdbcStatementExecutorTest` 和 `ProductionExecuteQueryIntegrationTest`
3. `required_string_args_reject_blank_values`
   Planned verification: `MCPToolControllerTest`
4. `invalid_object_types_fail_instead_of_broadening_search`
   Planned verification: `MCPToolControllerTest` 与 `SearchMetadataToolServiceTest`
5. `http_bind_host_is_loopback_only`
   Planned verification: `YamlHttpTransportConfigurationSwapperTest`、`MCPConfigurationLoaderTest`

## Implementation Strategy

1. 先落 Speckit 文档，冻结本轮 V0 范围与验证分支。
2. 更新 `specs/001` contract/quickstart 与 README，先把对外口径收口到 resource-only。
3. 修改 `MCPJdbcStatementExecutor`，用 “多取一行” 修正 `truncated` 语义，并补核心与 HTTP 集成测试。
4. 修改 `MCPToolController` 与 `MCPToolArguments`，收紧 required string / invalid `object_types` 校验，并补 dedicated tests。
5. 修改 `YamlHttpTransportConfigurationSwapper`，让非 loopback `bindHost` 必须显式设置 `allowRemoteAccess: true`，同步配置装配测试与说明。
6. 跑 scoped tests、package、style checks，最后回填 Speckit 状态。

## Validation Strategy

- **Core correctness and validation**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPJdbcStatementExecutorTest,MCPToolControllerTest,SearchMetadataToolServiceTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Bootstrap config and HTTP regression**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=YamlHttpTransportConfigurationSwapperTest,MCPConfigurationLoaderTest,LoopbackOriginSecurityValidatorTest,ProductionExecuteQueryIntegrationTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Packaged distribution smoke**
```bash
./mvnw -pl distribution/mcp -am -DskipTests package
```

- **Scoped style checks**
```bash
./mvnw -pl mcp/core,mcp/bootstrap -am -Pcheck -DskipITs -DskipTests \
  checkstyle:check spotless:check
```

- **Static grep verification**
```bash
rg -n "list_databases|list_schemas|list_tables|list_views|list_columns|list_indexes|describe_table|describe_view|get_capabilities" \
  /Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md \
  /Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/quickstart.md \
  /Users/zhangliang/IdeaProjects/shardingsphere/mcp/README_ZH.md \
  /Users/zhangliang/IdeaProjects/shardingsphere/mcp/README.md
```

## Rollout Notes

- 本轮先把 V0 口径、正确性和默认安全边界收紧，不代表 remote HTTP 已经产品化。
- 如果后续仍需要远程 HTTP 暴露，应另开 follow-up 设计显式 auth/access policy，而不是在本轮偷偷放开。
