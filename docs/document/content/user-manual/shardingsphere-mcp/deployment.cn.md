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

使用 OCI 镜像前，应先准备自定义配置文件。
HTTP 模式在容器中运行时，`bindHost` 应绑定到容器可暴露的网络接口，例如 `0.0.0.0`：

```yaml
transport:
  type: STREAMABLE_HTTP
  http:
    bindHost: 0.0.0.0
    port: 18088
    endpointPath: /mcp

runtimeDatabases:
  "<logic-database>":
    databaseType: MySQL
    jdbcUrl: "jdbc:mysql://<proxy-host>:<proxy-port>/<logic-database>"
    username: "<proxy-username>"
    password: "<proxy-password>"
    driverClassName: "com.mysql.cj.jdbc.Driver"
```

以 HTTP 模式运行，并挂载自定义配置文件和插件目录：

```bash
docker run --rm -p 18088:18088 \
  -e SHARDINGSPHERE_MCP_CONFIG=/opt/shardingsphere-mcp/conf/custom-mcp-http.yaml \
  -v /path/to/mcp-http.yaml:/opt/shardingsphere-mcp/conf/custom-mcp-http.yaml:ro \
  -v /path/to/plugins:/opt/shardingsphere-mcp/plugins:ro \
  ghcr.io/apache/shardingsphere-mcp:${latest.release.version}
```

以 STDIO 模式运行时，配置文件中的 `transport.type` 应为 `STDIO`：

```bash
docker run --rm -i \
  -e SHARDINGSPHERE_MCP_CONFIG=/opt/shardingsphere-mcp/conf/custom-mcp-stdio.yaml \
  -v /path/to/mcp-stdio.yaml:/opt/shardingsphere-mcp/conf/custom-mcp-stdio.yaml:ro \
  -v /path/to/plugins:/opt/shardingsphere-mcp/plugins:ro \
  ghcr.io/apache/shardingsphere-mcp:${latest.release.version}
```

根据目标能力边界配置 `runtimeDatabases`：

- 使用 ShardingSphere 规则能力或规则变更流程时，指向用户已准备好的 ShardingSphere-Proxy 逻辑库。
- 仅使用元数据搜索、元数据查看和受控 SQL 能力时，可以使用数据库直连。

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
- 需要把会话和外部用户或调用来源关联时，由受信网关注入会话归属请求头；不要允许客户端直接伪造这些请求头。

## 健康检查

部署完成后，建议按以下顺序确认 ShardingSphere-MCP 是否真正可用，而不只停留在“HTTP 端口可达”：

1. 服务进程与端点可达

   - HTTP 模式确认进程已启动、端口已监听，并且 `http://<bind-host>:<port><endpointPath>` 与客户端配置一致。
   - STDIO 模式确认由 AI 应用正确拉起 MCP 进程，不把标准输入输出当作交互式 shell 使用。

2. MCP 协议已就绪

   - 通过 AI 应用确认 MCP Server 已被识别，或按[自研集成附录](../developer-appendix/)中的协议调试示例完成 `initialize` 和能力读取。
   - 如果只能确认 HTTP 返回，但无法读取 capabilities、resources 或 tools，说明端点可达但协议尚未正确接通。

3. 运行时数据库已就绪

   - 读取 `shardingsphere://runtime`，确认 transport、runtime 数据库摘要和运行状态可见。
   - 调用 `database_gateway_validate_proxy_connectivity`，或在 AI 应用中执行“查看 `<logic-database>` 中有哪些表”这类最小任务，确认当前 `runtimeDatabases` 对应的逻辑库可用。
   - 仅有 MCP Server 进程启动并不表示目标运行时数据库已经可用；连接失败、权限不足或逻辑库不可见仍会阻断后续任务。

## 基础可观测入口

### 日志

- HTTP 模式：查看启动终端和 `logs/mcp.log`。
- STDIO 模式：不要把标准输出作为日志查看入口，诊断信息查看 stderr 或 `logs/mcp.log`。

### 运行时状态与保护信息

- `shardingsphere://runtime` 提供当前 transport、runtime 数据库摘要、运行状态和基础诊断信息。
- 运行时保护信息会反映查询行数限制、查询超时和会话级工具调用保护等边界。
- 当运行时数据库连接失败时，可结合错误分类和恢复建议定位问题；详细分类说明见[常见问题](../troubleshooting/)。

### 最小排障信息

需要向管理员或排障人员反馈问题时，至少收集以下信息：

- 启动命令或容器运行命令。
- MCP 配置文件摘要，注意移除密码、密钥和令牌。
- 传输方式、端点地址和 `runtimeDatabases` 中的目标逻辑库名称。
- AI 应用中的 MCP Server 配置摘要。
- 失败任务、错误分类和 `logs/mcp.log` 中相关日志片段。

进一步的症状定位、错误分类和运行时保护说明见[常见问题](../troubleshooting/)；需要直接调试 MCP 协议请求时，见[自研集成附录](../developer-appendix/)。
