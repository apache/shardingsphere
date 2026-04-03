# Implementation Plan: ShardingSphere MCP Generic URI Template Dispatch and Executing Resource Handlers

**Branch**: `no-branch-switch-requested` | **Date**: 2026-04-03 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/010-shardingsphere-mcp-generic-uri-dispatch/spec.md)
**Input**: Feature specification from `/specs/010-shardingsphere-mcp-generic-uri-dispatch/spec.md`

## Summary

本特性在 `mcp/core` 中抽出通用 URI template 基础设施，
并把 resource dispatch 从 “controller -> dispatcher -> plan -> controller 执行”
收敛为 “controller -> dispatcher -> handler 执行 -> payload mapping”。

这轮不改 `resources/list` / `resources/read` 的 public contract，
不引入 Jersey，不要求 tool 立即迁移到 URI dispatch。

## Technical Context

**Language/Version**: Java 17 in the MCP subchain  
**Primary Dependencies**: repository-owned MCP core runtime, ShardingSphere SPI, no third-party URI SDK  
**Storage**: in-memory SPI registry, runtime metadata snapshots, capability builder, metadata reader  
**Testing**: URI template unit tests, resource handler tests, controller regression, bootstrap compatibility checks  
**Target Platform**: ShardingSphere MCP standalone runtime on embedded Tomcat / STDIO  
**Project Type**: Java monorepo subproject under `mcp/core` and `mcp/bootstrap`  
**Constraints**: no public contract drift; no Jersey; keep `Map<String, Object>` controller return type;
future tool URI reuse should be possible without copying template parsing  
**Scale/Scope**: `mcp/core` resource dispatch chain, new `mcp/core` URI package, targeted controller/bootstrap regression

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Smallest safe change**: PASS  
  本轮只收敛 URI template 和 resource dispatch 执行边界，不改 public resource contract 和 tool surface。
- **Gate 2 - Readability and simplicity**: PASS  
  删除 `ResourceQueryPlan` 这一中间跳板，让 handler 名字和职责一致。
- **Gate 3 - Clear abstraction levels**: PASS  
  通用 URI template 留在 neutral core 包；resource handler 真正执行；controller 只做入口和 payload 映射。
- **Gate 4 - Verification path exists**: PASS  
  可以通过 URI template 测试、handler dedicated tests、controller regression 和 bootstrap compatibility 验证。
- **Gate 5 - Compatibility risk is bounded**: PASS  
  `resources/list` / `resources/read`、错误码和 payload 形状保持不变，风险主要局限于 internal dispatch 链。

## Project Structure

### Documentation (this feature)

```text
specs/010-shardingsphere-mcp-generic-uri-dispatch/
├── spec.md
└── plan.md
```

### Source Code (repository root)

```text
mcp/core/src/main/java/org/apache/shardingsphere/mcp/uri/UriTemplate.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/uri/UriTemplateMatch.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/MCPResourceController.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/MetadataResourceReader.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/MetadataResourceQuery.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/MetadataResourceResult.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/ResourceHandlerContext.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/ResourceHandlerResult.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/dispatch/ResourceDispatcher.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/dispatch/ResourceHandler.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/dispatch/ResourceHandlerRegistry.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/dispatch/ResourceHandlerMapping.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/dispatch/ResourceHandlerExecution.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/dispatch/ResourceHandlerRegistration.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/dispatch/handler/*.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/uri/*.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/*.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/dispatch/*.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/*.java
```

**Structure Decision**: 不新增模块；在 `mcp/core` 中新增最小通用 URI 包，
resource 继续消费该包，bootstrap 只保留对 `MCPResourceController` 的调用。

## Design Decisions

### 1. 通用 URI template 以对象形式存在，而不是再做一层 framework

- 新增 `UriTemplate`
- 新增 `UriTemplateMatch`
- `UriTemplate` 负责：
  - 模板合法性校验
  - strict full match
  - 变量提取
  - overlap 判断

这样 resource 与未来 tool URI surface 都只依赖同一套 core 对象，不依赖第三方 SDK。

### 2. `ResourceDispatcher` 只做 dispatch，不再做执行计划解释

- `dispatch(resourceUri)` 改为返回 `Optional<ResourceHandlerExecution>`
- `ResourceHandlerExecution` 包含：
  - matched handler
  - matched `UriTemplateMatch`

dispatch 层只负责“谁处理”，不再决定“怎么处理”。

### 3. `ResourceHandler` 成为真正执行型 handler

- `handle(...)` 不再返回 `ResourceQueryPlan`
- 改为接收：
  - `ResourceHandlerContext`
  - `UriTemplateMatch`
- 直接返回 `ResourceHandlerResult`

这让 16 个 handler 的名字与职责对齐，也让 controller 不再承担 domain execution branching。

### 4. `ResourceHandlerContext` 承接执行依赖

- context 负责把以下执行依赖集中交给 handler：
  - `MCPRuntimeContext`
  - capability builder 访问
  - `MetadataResourceReader`
  - database metadata snapshots

这样 handler 不需要自己 new reader，也不需要到处感知 controller 内部状态。

### 5. `ResourceHandlerResult` 统一 domain 结果，而不是泄漏 payload map

- `serviceCapability`
- `databaseCapability`
- `metadataResult`
- `error`

controller 仍然是 payload map 的唯一出入口。

### 6. registry 在启动期完成 template 编译和 overlap 检测

- 每个 handler 注册时编译出 `UriTemplate`
- registry 存 `ResourceHandlerRegistration`
- 初始化时校验：
  - duplicate template
  - overlap template

运行时不再依赖 ambiguity 分支作为主防线。

### 7. `ResourceQueryPlan` 从主链路退出

- 删除该类型或把它从 resource read 主路径完全移除
- controller 不再 switch `SERVICE_CAPABILITIES / DATABASE_CAPABILITIES / METADATA`
- 这些分支迁移到对应 handler 内部

## Branch Checklist

1. `generic_uri_template_matches_and_extracts_variables`
   Planned verification: strict match、变量提取、非法输入和重复变量名都由 `UriTemplate` 测试覆盖
2. `registry_rejects_duplicate_and_overlap_templates`
   Planned verification: handler registry 在启动期拦截重复模板和字面量/变量重叠模板
3. `resource_dispatch_returns_handler_execution_instead_of_plan`
   Planned verification: dispatcher 只返回 execution，主链路不再出现 `ResourceQueryPlan`
4. `resource_handlers_execute_directly`
   Planned verification: 16 个 handler dedicated tests 直接断言 `ResourceHandlerResult`
5. `controller_only_maps_to_payload`
   Planned verification: controller regression 确认 unsupported URI 和各类 handler result 都正确映射为 payload
6. `bootstrap_contract_remains_unchanged`
   Planned verification: bootstrap 侧 resource specification / integration regression 保持兼容

## Implementation Strategy

1. 先在 `mcp/core` 新增通用 `UriTemplate` / `UriTemplateMatch`，锁定 strict match 与 overlap 语义。
2. 把 `ResourceHandlerRegistry` 改成编译并持有 `ResourceHandlerRegistration`。
3. 把 `ResourceDispatcher` / `ResourceHandlerMapping` 改成只返回 `ResourceHandlerExecution`。
4. 引入 `ResourceHandlerContext` 和 `ResourceHandlerResult`。
5. 逐个迁移 16 个 handler，让它们直接执行 capability / metadata 读取。
6. 简化 `MCPResourceController`，删除 `ResourceQueryPlan` 主链路。
7. 跑 core 与 bootstrap 的 scoped tests，确认 public contract 零损失。

## Validation Strategy

- **URI template coverage**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=UriTemplateTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Resource dispatch and handler coverage**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPResourceControllerTest,ResourceHandlerRegistryTest,ResourceHandlerMappingTest,ResourceHandlerTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Bootstrap compatibility**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPResourceSpecificationFactoryTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Scoped style checks**
```bash
./mvnw -pl mcp/core,mcp/bootstrap -am -Pcheck -DskipITs -DskipTests \
  checkstyle:check spotless:check
```

## Rollout Notes

- 这轮的重点是把 resource dispatch 执行边界拉直，不是做新的 protocol 设计。
- 如果后续 tool 也需要 URI surface，应直接复用 `mcp/uri`，而不是再复制一份 resource-style matcher。
- 如果未来确实需要完整 RFC 6570 能力，应另开 follow-up；本轮只保留当前 MCP resource 所需的 strict subset。
