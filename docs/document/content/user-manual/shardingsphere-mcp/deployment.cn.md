+++
title = "部署说明"
weight = 6
+++

ShardingSphere-MCP 可以通过源码构建的独立发行包运行，也可以通过官方 OCI 镜像运行。

## 独立发行包

构建发行包：

```bash
./mvnw -pl distribution/mcp -am -DskipTests package
```

发行包目录包含：

- `bin/`：启动脚本。
- `conf/`：默认配置和日志配置。
- `lib/`：MCP Server 依赖和内置 MCP 功能插件。
- `plugins/`：外部 JDBC 驱动或额外 MCP 功能插件 jar。
- `logs/`：运行日志。

## OCI 镜像

官方 MCP Registry 元数据位于 `mcp/server.json`。
公开 server name 是 `io.github.apache/shardingsphere-mcp`。
OCI image 形态是：

```text
ghcr.io/apache/shardingsphere-mcp:<version>
```

以 HTTP 模式运行：

```bash
docker run --rm -p 18088:18088 ghcr.io/apache/shardingsphere-mcp:${latest.release.version}
```

以 STDIO 模式运行：

```bash
docker run --rm -i \
  -e SHARDINGSPHERE_MCP_TRANSPORT=stdio \
  ghcr.io/apache/shardingsphere-mcp:${latest.release.version}
```

使用自定义配置文件：

```bash
docker run --rm -p 18088:18088 \
  -e SHARDINGSPHERE_MCP_CONFIG=/opt/shardingsphere-mcp/conf/custom-mcp-http.yaml \
  -v /path/to/mcp-http.yaml:/opt/shardingsphere-mcp/conf/custom-mcp-http.yaml:ro \
  -v /path/to/plugins:/opt/shardingsphere-mcp/plugins:ro \
  ghcr.io/apache/shardingsphere-mcp:${latest.release.version}
```

配置文件中的 `runtimeDatabases` 需要指向用户已准备好的 ShardingSphere-Proxy 逻辑库。

## 安全部署建议

内置 HTTP Server 不提供认证和授权。
如果需要远程访问，应放在受信网络、反向代理或网关后面，由外层组件处理：

- TLS 终止。
- 身份认证。
- 授权策略。
- 网络访问控制。
- 审计日志。

HTTP 绑定建议：

- 本地调试使用 `127.0.0.1`。
- 容器或内网部署使用受控网络接口。
- 面向远程客户端暴露时，避免直接裸露 MCP Server。

## 日志

- HTTP 模式：查看启动终端和 `logs/mcp.log`。
- STDIO 模式：stdout 专用于 MCP 协议帧，诊断信息查看 stderr 或 `logs/mcp.log`。
