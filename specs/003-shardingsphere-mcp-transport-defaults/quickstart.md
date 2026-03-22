# Quickstart: ShardingSphere MCP Transport Default Realignment

## 1. Goal

验证“默认 `stdio only`、HTTP opt-in、双 transport 保留、`both false` fail-fast”四个核心行为。

## 2. Default packaged profile

检查发行配置：

```yaml
transport:
  http:
    enabled: false
  stdio:
    enabled: true
```

预期：

- 默认启动时只启用 STDIO
- 默认启动时不创建 `/mcp` HTTP listener
- 本地 host 通过子进程方式接入

## 3. Verify default STDIO launch

使用本地 MCP host 或现有 STDIO integration path 启动发行包对应 runtime。

最小预期：

1. `initializeSession()` 可创建会话
2. 至少一个 metadata tool 可调用成功
3. `closeSession()` 后会话被正确关闭
4. 未显式启用 HTTP 时，不要求 `/mcp` 可访问

## 4. Verify HTTP opt-in launch

将配置切换为：

```yaml
transport:
  http:
    enabled: true
  stdio:
    enabled: false
```

最小预期：

1. `POST /mcp` 可以完成 `initialize`
2. follow-up 请求继续要求 `MCP-Session-Id`
3. `GET /mcp` 与 `DELETE /mcp` 继续工作

## 5. Verify dual enabled launch

将配置切换为：

```yaml
transport:
  http:
    enabled: true
  stdio:
    enabled: true
```

最小预期：

1. STDIO 本地会话可用
2. `/mcp` HTTP endpoint 可用
3. 启动日志清晰显示两条 transport 都已启用

## 6. Verify fail-fast branch

将配置切换为：

```yaml
transport:
  http:
    enabled: false
  stdio:
    enabled: false
```

最小预期：

1. 启动失败
2. 日志直接提示至少启用一个 transport

## 7. Reviewer checklist

- 默认值是否已改为 `stdio only`
- HTTP contract 是否保持不变
- 双开是否仍合法
- `both false` 是否仍 fail-fast
- README / 技术设计 / quickstart 是否描述一致
