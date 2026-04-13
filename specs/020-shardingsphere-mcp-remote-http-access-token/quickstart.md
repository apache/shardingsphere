# Quickstart: ShardingSphere MCP Remote HTTP Access Token

## 1. loopback 本地调试，保持默认无 token

配置：

```yaml
transport:
  http:
    enabled: true
    bindHost: 127.0.0.1
    allowRemoteAccess: false
    port: 18088
    endpointPath: /mcp
```

期望行为：

- 本地 HTTP client 仍可直接 `initialize`
- 继续保留 loopback `Origin` 校验
- 不要求 `Authorization` 头

产品解释：

- 这是 V1 的默认 local-first 体验
- remote hardening 不应破坏本地调试

## 2. remote 暴露，显式配置共享 token

配置：

```yaml
transport:
  http:
    enabled: true
    bindHost: 0.0.0.0
    allowRemoteAccess: true
    accessToken: 7f1c0c6d9d7846f4a2c9b8e1a6f3c521
    port: 18088
    endpointPath: /mcp
```

期望行为：

- 服务可正常启动
- 所有 HTTP 请求都必须带：

```text
Authorization: Bearer 7f1c0c6d9d7846f4a2c9b8e1a6f3c521
```

产品解释：

- `allowRemoteAccess` 代表“允许暴露”
- `accessToken` 代表“允许谁接入”

## 3. remote 缺失 token，启动失败

配置：

```yaml
transport:
  http:
    enabled: true
    bindHost: 0.0.0.0
    allowRemoteAccess: true
    port: 18088
    endpointPath: /mcp
```

期望行为：

- 配置加载失败
- 系统不允许以“remote 已打开、admission gate 未配置”的状态启动

## 4. 请求未携带 token，返回 401

请求：

```http
POST /mcp
Content-Type: application/json

{"jsonrpc":"2.0","id":"init-1","method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"demo","version":"1.0.0"}}}
```

期望行为：

- 返回 `401 Unauthorized`

产品解释：

- 这说明请求甚至还没通过 transport admission gate
- 不是 session 或 protocol 的错误

## 5. token 正确后，才进入 MCP 现有流程

请求：

```http
POST /mcp
Authorization: Bearer 7f1c0c6d9d7846f4a2c9b8e1a6f3c521
Content-Type: application/json

{"jsonrpc":"2.0","id":"init-1","method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"demo","version":"1.0.0"}}}
```

期望行为：

- 请求先通过 token admission gate
- 再进入 initialize / session 创建流程
- 后续 `tools/call`、`resources/read`、`DELETE` 也继续要求 token

## 6. 这不是登录系统

调用方应这样理解：

- `accessToken` 是部署方预置的共享密钥
- 它不是登录后签发的 token
- 它不区分不同用户身份
- 它只表达“当前请求是否允许接入这个 HTTP runtime”
