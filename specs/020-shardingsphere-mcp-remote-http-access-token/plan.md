# Implementation Plan: ShardingSphere MCP Remote HTTP Access Token

**Branch**: `no-branch-switch-requested` | **Date**: 2026-04-13 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/020-shardingsphere-mcp-remote-http-access-token/spec.md)
**Input**: Feature specification from `/specs/020-shardingsphere-mcp-remote-http-access-token/spec.md`

## Summary

本特性为 MCP 的 Streamable HTTP transport
补齐一层最小内建 admission gate：

- remote 暴露必须显式配置共享访问 token
- 配置了 token 的 runtime，所有 HTTP 请求都要带 Bearer token
- 认证失败统一在 transport 层返回 `401`
- 认证校验先于 session / protocol 校验
- loopback 默认本地调试体验继续保持

这不是引入完整认证授权系统，
而是在不扩散到 `mcp/core` 的前提下，
最小代价关闭匿名 remote HTTP。

## Technical Context

**Language/Version**: Java 17 in the MCP bootstrap subchain  
**Primary Dependencies**: embedded Tomcat, MCP Java SDK servlet transport, existing YAML config swapper pipeline  
**Storage**: in-memory runtime configuration only  
**Testing**: configuration loader tests, YAML swapper tests, request validator tests, Streamable HTTP integration tests, doc/contract alignment review  
**Target Platform**: ShardingSphere MCP standalone runtime on embedded Tomcat  
**Project Type**: Java monorepo subproject under `mcp/bootstrap`, plus docs/specs follow-up  
**Constraints**: no branch switch; smallest code change; no user system; no token issuance flow; no SQL/capability/schema behavior changes  
**Scale/Scope**: HTTP transport config, servlet admission check, focused bootstrap tests, README/spec alignment

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Simplicity**: PASS  
  采用单个共享 token 作为最小 admission gate，
  不引入用户、角色或 token 生命周期系统，
  符合 [CODE_OF_CONDUCT.md](/Users/zhangliang/IdeaProjects/shardingsphere/CODE_OF_CONDUCT.md#L8)
  到 [CODE_OF_CONDUCT.md](/Users/zhangliang/IdeaProjects/shardingsphere/CODE_OF_CONDUCT.md#L14)
  的简洁与一致性原则。
- **Gate 2 - Smallest safe change**: PASS  
  改动面限制在 `mcp/bootstrap` transport boundary，
  不进入 `mcp/core` 执行链。
- **Gate 3 - Honest product boundary**: PASS  
  明确声明这是 shared bearer token admission gate，
  不是完整 authn/authz 方案。
- **Gate 4 - Backward compatibility for local mode**: PASS  
  保持 loopback 默认无 token 的本地调试体验。
- **Gate 5 - Verification path exists**: PASS  
  配置加载、validator 与 HTTP integration 都有 scoped verification 路径。

## Hard Constraint Checklist

- 不切换分支，只在当前分支整理并后续实现
- 只做 remote HTTP 最小内建 admission gate
- 不引入用户、角色、权限体系
- 不引入登录、签发、刷新、过期、吊销机制
- 不改 `mcp/core` 的 tool contract、capability 或 SQL 执行链
- 非 loopback 暴露必须要求 `accessToken`
- loopback 默认无 token 仍可用
- 配置了 token 的 runtime，所有 HTTP 请求都必须带 Bearer token
- 认证校验优先于 session / protocol 校验
- README、top-level spec 与 streamable HTTP contract 必须对齐

## Project Structure

### Documentation (this feature)

```text
specs/020-shardingsphere-mcp-remote-http-access-token/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── streamable-http-access-token-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/HttpTransportConfiguration.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlHttpTransportConfiguration.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapper.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServlet.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/LoopbackOriginSecurityValidator.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPRequestValidator.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/AccessTokenSecurityValidator.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/loader/MCPConfigurationLoaderTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapperTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlMCPTransportConfigurationSwapperTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPRequestValidatorTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/LoopbackOriginSecurityValidatorTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/AccessTokenSecurityValidatorTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/AbstractStreamableHttpIT.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpTransportIT.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMetadataDiscoveryIT.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpExecuteQueryIT.java
mcp/README.md
mcp/README_ZH.md
specs/001-shardingsphere-mcp/spec.md
specs/001-shardingsphere-mcp/contracts/streamable-http-contract.md
```

**Structure Decision**: 不新增模块；
在现有 `HttpTransportConfiguration -> servlet security validation`
链路上增加一个 token-based validator，
并复用现有 bootstrap 配置与 HTTP 集成测试框架。

## Design Decisions

### 1. `accessToken` 是 deployment secret，不是 login token

- 由部署方生成并配置
- 启动时读取
- 每次请求比对
- 不存在“登录后签发”流程

### 2. remote 必填，local 可选

- 非 loopback + `allowRemoteAccess=true` 时必须配置 token
- loopback 默认不要求 token
- loopback 若显式配置 token，则按统一规则启用认证

### 3. admission gate 覆盖整个 HTTP surface

- `initialize`
- `tools/call`
- `resources/read`
- `DELETE` / close session

都要经过同一 token 校验。

### 4. token 校验先于 session / protocol 校验

- 未认证请求不应先拿到 session / protocol 反馈
- 这样可以减少信息泄露并统一边界顺序

### 5. 保留外部边界建议

- 内建 token 只解决“匿名接入”
- TLS、反向代理、trusted network、限流等仍由外围承接

## Branch Checklist

1. `remote_bind_requires_access_token`  
   Planned verification: `MCPConfigurationLoaderTest`
2. `configured_token_requires_authorization_header_for_initialize`  
   Planned verification: `StreamableHttpTransportIT`
3. `configured_token_blocks_follow_up_requests_without_leaking_session_state`  
   Planned verification: `StreamableHttpTransportIT` and request validator tests
4. `loopback_without_token_remains_usable`  
   Planned verification: existing HTTP integration tests
5. `loopback_origin_guard_is_preserved`  
   Planned verification: `LoopbackOriginSecurityValidatorTest` and HTTP integration tests

## Implementation Strategy

1. 扩展 `HttpTransportConfiguration` 与 `YamlHttpTransportConfiguration`，
   新增 `accessToken` 字段。
2. 在 `YamlHttpTransportConfigurationSwapper` 中补齐：
   - YAML <-> object 映射
   - remote 场景下 token 必填校验
3. 新增独立的 `AccessTokenSecurityValidator`，
   只负责 `Authorization: Bearer <token>` admission check。
4. 在 `StreamableHttpMCPServlet` 中组合：
   - token validator
   - loopback `Origin` validator
   并调整顺序，使认证早于 session / protocol 校验。
5. 保持 `StreamableHttpMCPRequestValidator` 只处理 session / protocol 语义，
   不把 token 逻辑塞进去。
6. 更新 bootstrap 层 focused tests：
   - loader / swapper
   - validator
   - integration tests
7. 更新 README 与 `001` 顶层 contract，
   把 remote exposure 的产品定位改成
   “external controls + built-in shared bearer token admission gate”。

## Validation Strategy

- **Configuration verification**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPConfigurationLoaderTest,YamlHttpTransportConfigurationSwapperTest,YamlMCPTransportConfigurationSwapperTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Validator verification**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=LoopbackOriginSecurityValidatorTest,StreamableHttpMCPRequestValidatorTest,AccessTokenSecurityValidatorTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **HTTP integration verification**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=StreamableHttpTransportIT,StreamableHttpMetadataDiscoveryIT,StreamableHttpExecuteQueryIT test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Scoped style checks**
```bash
./mvnw -pl mcp/bootstrap -am -Pcheck -DskipITs -DskipTests \
  checkstyle:check spotless:check
```

## Rollout Notes

- 这不是“把 remote HTTP 变成公网级完整安全服务”。
- 这只是把 “anonymous remote access” 收紧成 “shared-secret gated access”。
- 如果后续要支持多身份、RBAC 或 OAuth，
  应开新的 follow-up feature，
  不应把本轮最小 admission gate 演进成半套 auth platform。
