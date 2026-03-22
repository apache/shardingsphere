# Contract: Transport Launch Profiles

## Purpose

定义 ShardingSphere MCP 在 transport 默认值重排后的启动组合、默认行为与不变量，供实现、测试与运维文档共享同一基线。

## Supported Profiles

### 1. `stdio only` (default)

- `transport.stdio.enabled = true`
- `transport.http.enabled = false`
- 启动结果：
  - STDIO runtime 启动
  - 不启动 HTTP listener

### 2. `http only`

- `transport.stdio.enabled = false`
- `transport.http.enabled = true`
- 启动结果：
  - 启动 `/mcp` Streamable HTTP endpoint
  - 不要求 STDIO runtime 对外提供默认入口

### 3. `dual enabled`

- `transport.stdio.enabled = true`
- `transport.http.enabled = true`
- 启动结果：
  - 两条 transport 都可使用
  - 共享同一套 runtime wiring、tool registry 与 session manager assembly

## Invalid Profile

### 4. `both false`

- `transport.stdio.enabled = false`
- `transport.http.enabled = false`
- 启动结果：
  - 启动立即失败
  - 必须输出明确诊断，说明至少启用一个 transport

## Invariants

- HTTP transport 继续固定为 MCP `Streamable HTTP`
- `/mcp` endpoint path 不因默认值切换而改变
- HTTP 的 `MCP-Session-Id`、protocol version、SSE 与 `DELETE /mcp` 语义保持不变
- STDIO 的 `initializeSession()`、tool 调用与 session close API surface 保持不变
- transport 默认值调整不改变生产 runtime provider 的装配边界

## Operator Guidance

- 本地 host/IDE/CLI 集成：优先使用默认 `stdio only`
- 远程 host、网关、反向代理接入：显式启用 `http only`
- 本地联调、兼容多 host 或做迁移验证：使用 `dual enabled`
