# Implementation Plan: ShardingSphere MCP Runtime Configuration Simplification

**Branch**: `005-shardingsphere-mcp-runtime-config-simplification` | **Date**: 2026-03-24 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/005-shardingsphere-mcp-runtime-config-simplification/spec.md)
**Input**: Feature specification from `/specs/005-shardingsphere-mcp-runtime-config-simplification/spec.md`

## Summary

本 follow-up 收敛 MCP direct JDBC runtime 的 operator-facing 配置模型：
保留 `runtime` 顶级命名空间，让 `runtime.databases` 成为 single-db 与
multi-db 的统一 canonical 入口，把 shared defaults 改名为
`runtime.databaseDefaults`，移除 schema 的外部配置面，弱化 `driverClassName`
为 optional override，并把 `supportsCrossSchemaSql` 与
`supportsExplainAnalyze` 从常规 YAML 配置移到系统自动 capability 推导。

## Technical Context

**Language/Version**: Java 17 in the MCP subchain  
**Primary Dependencies**: JDBC, repository-managed capability registry, YAML
swappers, packaged distribution `lib/` and `ext-lib/` classpath loading  
**Storage**: `distribution/mcp/conf/mcp.yaml`, in-memory runtime topology,
in-memory metadata catalog, in-memory derived capability facts  
**Testing**: JUnit 5, YAML/config loader tests, bootstrap integration tests,
core capability/execution tests, scoped Maven verification  
**Target Platform**: Standalone MCP distribution on Linux or macOS with direct
JDBC connectivity to supported databases  
**Project Type**: Java monorepo subproject with packaged distribution and
dedicated bootstrap/core test modules  
**Constraints**: Keep MCP V1 public contract unchanged; keep the smallest safe
change; maintain backward-compatible parsing for legacy runtime aliases during
one migration window; avoid requiring operator capability booleans  
**Scale/Scope**: YAML contract, config normalization, runtime launch path,
driver loading behavior, capability derivation, docs, and regression coverage

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Smallest safe change**: PASS  
  变更聚焦 direct runtime 配置收敛，不改 public MCP contract。
- **Gate 2 - Readability and simplicity**: PASS  
  目标是把 single-db 与 multi-db 统一到一套 runtime topology 写法，减少 operator 心智负担。
- **Gate 3 - Runtime behavior remains traceable**: PASS  
  capability derivation、legacy alias canonicalization 与 driver override 都会留下明确规则和诊断。
- **Gate 4 - Verification path exists**: PASS  
  可通过 YAML swappers、config loader、bootstrap launch、capability 和 execution tests 覆盖主要风险。
- **Gate 5 - Compatibility risk is bounded**: PASS  
  保留 legacy aliases 的兼容期，但 canonical 文档与默认配置只展示新结构。

## Project Structure

### Documentation (this feature)

```text
specs/005-shardingsphere-mcp-runtime-config-simplification/
├── spec.md
├── research.md
├── data-model.md
├── plan.md
├── tasks.md
└── quickstart.md
```

### Source Code (repository root)

```text
distribution/mcp/src/main/resources/conf/mcp.yaml
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/loader/MCPConfigurationLoader.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/RuntimeDatabaseConfiguration.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlRuntimeConfiguration.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlRuntimeDatabaseConfiguration.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlMCPLaunchConfigurationSwapper.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlRuntimeConfigurationSwapper.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlRuntimeDatabaseConfigurationSwapper.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/DatabaseConnectionConfiguration.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/DatabaseRuntimeFactory.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/JdbcConnectionFactory.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/JdbcMetadataLoader.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/MCPLaunchRuntimeLoader.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/DatabaseCapabilityAssembler.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/DatabaseCapabilityRegistry.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/ExecuteQueryFacade.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/RuntimeDatabaseDescriptor.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/
mcp/core/src/test/java/org/apache/shardingsphere/mcp/
test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/
mcp/README.md
mcp/README_ZH.md
docs/mcp/ShardingSphere-MCP-Technical-Design.md
docs/mcp/ShardingSphere-MCP-Detailed-Design.md
```

**Structure Decision**: 不新增模块，继续在 `mcp/bootstrap`、`mcp/core`、
`distribution/mcp`、`test/e2e/mcp` 和运维文档内完成配置契约收敛。

## Design Decisions

### 1. 保留 `runtime` 顶级命名空间

- `runtime` 继续和 `transport` 并列。
- 不把 `databases` 与 shared defaults 提升到顶级。

### 2. `runtime.databases` 成为 direct runtime 唯一 canonical 入口

- single-db 与 multi-db 统一使用 logical database 拓扑。
- `runtime.props` 只在兼容期作为 migration alias 进入 canonicalization。

### 3. `runtime.databaseDefaults` 替代 `runtime.defaults`

- shared defaults 改为更可读的名字。
- `schemaPattern` 与 `defaultSchema` 从 operator-facing YAML 中移除。
- schema 范围与默认 schema 改由 JDBC metadata 自动发现。

### 4. `driverClassName` 只保留为 optional override

- 默认路径依赖 classpath 与 JDBC auto-loading。
- 显式填写错误时仍然 fail fast。

### 5. capability booleans 改为自动推导

- `supportsCrossSchemaSql` 与 `supportsExplainAnalyze` 从 operator-facing
  YAML 中移除。
- 系统根据 database type、database version 与 runtime metadata 推导。

### 6. legacy aliases 兼容但显式诊断

- `runtime.props`、`runtime.defaults` 和 legacy capability booleans
  在兼容期内可读。
- canonical key 与 legacy alias 混用时必须 fail fast。

## Branch Checklist

在实现前固定关键分支与计划测试，避免配置收敛 follow-up 出现覆盖盲区：

1. `canonical_single_database_config`
   Planned test: canonical one-database YAML loads and starts without `runtime.props`
2. `canonical_database_defaults_inheritance`
   Planned test: `runtime.databaseDefaults` connection defaults are inherited deterministically
3. `legacy_props_alias`
   Planned test: legacy `runtime.props` loads as one canonical database binding with diagnostics
4. `optional_driver_autodiscovery`
   Planned test: omitted `driverClassName` succeeds when driver is on classpath
5. `invalid_explicit_driver`
   Planned test: explicit bad `driverClassName` fails fast with clear diagnostic
6. `derived_explain_analyze_capability`
   Planned test: `EXPLAIN ANALYZE` gating follows derived capability, not operator booleans
7. `legacy_capability_boolean_deprecation`
   Planned test: legacy booleans are accepted only as migration shim with warnings
8. `canonical_legacy_conflict_validation`
   Planned test: mixed canonical and legacy runtime keys fail fast with targeted diagnostics

## Implementation Strategy

1. 重定义 YAML 配置模型，让 canonical direct runtime 只围绕
   `runtime.databaseDefaults` 和 `runtime.databases`。
2. 在 YAML swappers 和 config loader 中完成 legacy alias 的 canonicalization
   与 conflict validation。
3. 收敛 runtime launch path，使 default launch path 以 canonical database
   topology 为中心，而不是继续依赖 `runtime.props`。
4. 把 `driverClassName` 变成 optional override，并把 capability booleans
   迁移到自动 derivation。
5. 更新 packaged config、README、设计文档和 regression tests。

## Validation Strategy

- **Config canonicalization and loader coverage**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPConfigurationLoaderTest,YamlRuntimeConfigurationSwapperTest,YamlRuntimeDatabaseConfigurationSwapperTest,YamlMCPLaunchConfigurationSwapperTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```
- **Default launch path and migration alias coverage**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPBootstrapTest,MCPLaunchRuntimeLoaderTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```
- **Capability derivation and execution gating**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=DatabaseCapabilityAssemblerTest,ExecuteQueryFacadeTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```
- **Production runtime E2E verification**
```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=ProductionMetadataDiscoveryE2ETest,ProductionMultiDatabaseE2ETest,ProductionExecuteQueryE2ETest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```
- **Scoped style checks for touched modules**
```bash
./mvnw -pl mcp/core,mcp/bootstrap,distribution/mcp,test/e2e/mcp -am \
  checkstyle:check -Pcheck -DskipITs -Dspotless.skip=true
```

## Rollout Notes

- 这是 direct runtime 配置契约收敛，不是 MCP 协议重写。
- `005` 继续继承 `001`、`002` 和 `004` 已定义的 MCP domain 与 capability contracts，
  本 follow-up 只补充 direct runtime 输入规范化与迁移行为。
- canonical 结构生效后，默认发行包与 README 只展示新写法。
- legacy aliases 兼容期结束后，应通过新的 follow-up 删除兼容分支，而不是长期并存。
