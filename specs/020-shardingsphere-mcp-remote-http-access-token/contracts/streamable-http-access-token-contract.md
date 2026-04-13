# Contract: Streamable HTTP Access Token Admission Gate

## Purpose

定义 ShardingSphere MCP Streamable HTTP runtime
在 remote exposure 场景下的最小内建 admission gate，
并明确 `allowRemoteAccess` 与 `accessToken` 的职责边界。

## Configuration Surface

```text
transport.http.enabled
transport.http.bindHost
transport.http.allowRemoteAccess
transport.http.accessToken
transport.http.port
transport.http.endpointPath
```

## Configuration Contract

### Rule 1: `allowRemoteAccess` 只表达远程暴露意图

- `allowRemoteAccess = true` 表示允许 HTTP 服务绑定到非 loopback 地址
- 它不等价于认证、授权或访问保护能力

### Rule 2: non-loopback remote exposure 必须配置 `accessToken`

- 当 `bindHost` 不是 `127.0.0.1`、`localhost` 或 `::1` 时：
  - `allowRemoteAccess` 必须为 `true`
  - `accessToken` 必须为非空、非空白字符串

### Rule 3: loopback 场景下 `accessToken` 可选

- loopback 场景未配置 token 时，保留现有本地调试体验
- loopback 场景若显式配置 token，则请求 admission gate 也必须启用

## Request Contract

### Rule 4: token 载体使用 `Authorization: Bearer <token>`

- 当 runtime 配置了 `accessToken` 时，
  initialize、resources/read、tools/call 和 DELETE
  都必须携带 Bearer token

### Rule 5: token admission check 先于 session / protocol check

- 未认证请求不能先获取：
  - session 是否存在
  - protocol 是否匹配
  - 其它运行时状态信息

### Rule 6: 认证失败统一返回 `401 Unauthorized`

- 触发条件包括：
  - 缺失 `Authorization`
  - 非 `Bearer` 方案
  - token 为空
  - token 不匹配

服务端响应应使用最小披露消息，例如：

```json
{
  "message": "Unauthorized."
}
```

## Boundary Contract

- 内建 `accessToken` 是 deployment-level shared secret
- 它不是 login token
- 它不是 session token
- 它不是按用户区分身份的凭证
- 它不替代 trusted network、gateway、reverse proxy 或 TLS

## Interaction with Existing Local Boundary

- loopback `Origin` 校验继续保留
- token 校验与 loopback `Origin` 校验都属于 transport boundary
- token 校验不能替代 loopback `Origin` 校验

## Normative Examples

### Example A: remote startup without token

```yaml
transport:
  http:
    enabled: true
    bindHost: 0.0.0.0
    allowRemoteAccess: true
    port: 18088
    endpointPath: /mcp
```

Expected result:

- configuration load fails

### Example B: authenticated initialize

```http
POST /mcp
Authorization: Bearer foo_token
Content-Type: application/json
```

Expected result:

- request enters initialize flow

### Example C: missing token

```http
POST /mcp
Content-Type: application/json
```

Expected result:

- `401 Unauthorized`

## Reviewer Checklist

- `transport.http.accessToken` 是否已进入 YAML 与 runtime config 模型
- remote bind 是否已要求 token 必填
- token 校验是否先于 session / protocol 校验
- initialize 与 follow-up request 是否都要求 token
- loopback 无 token 本地体验是否保持兼容
- README 与 top-level spec 是否已明确 “shared bearer token + external controls” 的组合定位
