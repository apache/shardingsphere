# Implementation Plan: ShardingSphere MCP Runtime Configuration Simplification

**Branch**: `005-shardingsphere-mcp-runtime-config-simplification` | **Date**: 2026-03-24 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/005-shardingsphere-mcp-runtime-config-simplification/spec.md)
**Input**: Feature specification from `/specs/005-shardingsphere-mcp-runtime-config-simplification/spec.md`

## Summary

本 follow-up 收敛 MCP direct JDBC runtime 的 operator-facing 配置模型：
让顶级 `runtimeDatabases` 成为 single-db 与 multi-db 的统一 canonical 入口，
删除 `runtime` 包裹层和 shared defaults 模型，移除 schema 的外部配置面，弱化
`driverClassName` 为 optional override，并把 `supportsCrossSchemaSql` 与
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
change; prefer one-to-one YAML/runtime mapping; avoid requiring operator
capability booleans  
**Scale/Scope**: YAML contract, config validation, runtime launch path, driver
loading behavior, capability derivation, docs, and regression coverage

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Smallest safe change**: PASS  
  变更聚焦 direct runtime 配置收敛，不改 public MCP contract。
- **Gate 2 - Readability and simplicity**: PASS  
  目标是让 YAML 与运行时对象一一对应，减少 operator 心智负担。
- **Gate 3 - Runtime behavior remains traceable**: PASS  
  capability derivation、legacy key rejection 与 driver override 都有明确规则和诊断。
- **Gate 4 - Verification path exists**: PASS  
  可通过 launch-level swapper、config loader、bootstrap launch、capability 和 execution tests 覆盖主要风险。
- **Gate 5 - Compatibility risk is bounded**: PASS  
  旧 `runtime.*` keys 不再被加载，但保留显式迁移提示。

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
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/MCPLaunchConfiguration.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/loader/MCPConfigurationLoader.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/RuntimeDatabaseConfiguration.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlMCPLaunchConfiguration.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlRuntimeDatabaseConfiguration.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlMCPLaunchConfigurationSwapper.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlRuntimeDatabaseConfigurationSwapper.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/lifecycle/MCPRuntimeLauncher.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/DatabaseConnectionConfiguration.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/DatabaseRuntimeFactory.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/JdbcConnectionFactory.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/JdbcMetadataLoader.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/DatabaseCapabilityAssembler.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/DatabaseCapabilityRegistry.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/ExecuteQueryFacade.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/DatabaseMetadataSnapshot.java
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

### 1. 顶级 canonical runtime 入口是 `runtimeDatabases`

- `runtimeDatabases` 直接和 `transport` 并列。
- 不再保留 `runtime` 包裹层。

### 2. `runtimeDatabases` 同时覆盖 single-db 与 multi-db

- 单库就是只含一个 logical database entry 的 map。
- 不再保留 `runtime.props` 作为兼容加载路径。

### 3. 删除 shared defaults 模型

- 不再保留 `runtime.databaseDefaults` 或其他 shared-defaults canonical 写法。
- 每个 logical database entry 都显式声明自己的运行时输入。
- `schemaPattern` 从 operator-facing YAML 中移除。

### 4. `driverClassName` 只保留为 optional override

- 默认路径依赖 classpath 与 JDBC auto-loading。
- 显式填写错误时仍然 fail fast。

### 5. capability booleans 改为自动推导

- `supportsCrossSchemaSql` 与 `supportsExplainAnalyze` 从 operator-facing
  YAML 中移除。
- 系统根据 database type、database version 与 runtime metadata 推导。

### 6. legacy keys 只保留显式拒绝诊断

- `runtime.props`、`runtime.defaults`、`runtime.databaseDefaults`、
  `runtime.databases` 和 legacy capability booleans 在校验阶段被拒绝。
- 错误消息必须指向 `runtimeDatabases` 现行写法。

## Branch Checklist

在实现前固定关键分支与计划测试，避免配置收敛 follow-up 出现覆盖盲区：

1. `canonical_single_database_config`
   Planned test: one-database `runtimeDatabases` YAML loads and starts successfully
2. `canonical_multi_database_config`
   Planned test: two-database `runtimeDatabases` YAML loads and starts successfully
3. `legacy_runtime_key_rejection`
   Planned test: `runtime.props` / `runtime.databases` / `runtime.databaseDefaults` all fail with targeted diagnostics
4. `optional_driver_autodiscovery`
   Planned test: omitted `driverClassName` succeeds when driver is on classpath
5. `invalid_explicit_driver`
   Planned test: explicit bad `driverClassName` fails fast with clear diagnostic
6. `derived_explain_analyze_capability`
   Planned test: `EXPLAIN ANALYZE` gating follows derived capability, not operator booleans
7. `legacy_capability_boolean_rejection`
   Planned test: legacy booleans are rejected with migration guidance
8. `canonical_legacy_conflict_validation`
   Planned test: mixed canonical and legacy runtime keys fail fast with targeted diagnostics

## Implementation Strategy

1. 重定义 launch-level YAML 配置模型，让 canonical direct runtime 只围绕
   `runtimeDatabases`。
2. 在 YAML swappers 和 config loader 中完成 legacy runtime keys 的 validation
   and rejection。
3. 收敛 runtime launch path，使 default launch path 以 `runtimeDatabases`
   为中心，而不是继续依赖旧 `runtime.*` 结构。
4. 把 `driverClassName` 变成 optional override，并把 capability booleans
   迁移到自动 derivation。
5. 更新 packaged config、README、设计文档和 regression tests。

## Validation Strategy

- **Config and launch-level coverage**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPConfigurationLoaderTest,YamlRuntimeDatabaseConfigurationSwapperTest,YamlMCPLaunchConfigurationSwapperTest,ProductionRuntimeLauncherTest,MCPBootstrapTest test \
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
  -Dtest=ProductionRuntimeSmokeE2ETest,ProductionMultiDatabaseE2ETest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```
- **Scoped style checks for touched modules**
```bash
./mvnw -pl mcp/bootstrap,test/e2e/mcp -Pcheck -DskipITs -DskipTests checkstyle:check spotless:check
```

## Rollout Notes

- 这是 direct runtime 配置契约收敛，不是 MCP 协议重写。
- `005` 继续继承 `001`、`002` 和 `004` 已定义的 MCP domain 与 capability contracts，
  本 follow-up 只补充 direct runtime 输入规范化、legacy rejection 与文档统一。
- canonical 结构生效后，默认发行包与 README 只展示 `runtimeDatabases`。
- 如果未来需要重新引入兼容迁移窗口，应通过新的 follow-up 明确设计，而不是在当前实现里保留隐式兼容。
