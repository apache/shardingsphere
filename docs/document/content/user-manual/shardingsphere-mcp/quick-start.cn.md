+++
title = "快速开始"
weight = 1
+++

本页演示如何从源码构建 ShardingSphere-MCP，连接一个用户已准备好的 ShardingSphere-Proxy 逻辑库，并在 AI 应用中通过自然语言验证基础数据库任务。

## 前置条件

- `JAVA_HOME` 或 `PATH` 中可用的 JDK 21。
- 一个可通过 JDBC 访问的 ShardingSphere-Proxy 逻辑库。
- 一个支持 MCP 的 AI 应用、IDE 插件或 Agent 平台。

## 构建发行包

在仓库根目录执行：

```bash
./mvnw -pl distribution/mcp -am -DskipTests package
```

进入发行包目录：

```bash
cd distribution/mcp/target/apache-shardingsphere-mcp-${version}
```

预期结果：

- 当前目录包含 `bin/`、`conf/`、`lib/`。
- 将 `${version}` 替换为构建出的发行包版本，例如 `5.5.4-SNAPSHOT`。

## 配置数据库

编辑 `conf/mcp-http.yaml`，将 `runtimeDatabases` 指向已有的 ShardingSphere-Proxy 逻辑库：

```yaml
runtimeDatabases:
  "logic_db":
    jdbcUrl: "jdbc:mysql://127.0.0.1:3307/logic_db"
    username: "root"
    password: ""
    driverClassName: "com.mysql.cj.jdbc.Driver"
```

根据 ShardingSphere-Proxy 的实际连接信息调整 `logic_db`、`127.0.0.1`、`3307`、`root` 和空密码。
MCP Server 会从 `jdbcUrl` 解析数据库类型。
如果目标数据库驱动没有随发行包提供，请在启动前把对应 JDBC 驱动 jar 放入 `plugins/`。

## 启动 HTTP MCP Server

Unix-like 系统：

```bash
bin/start.sh > logs/mcp-http.log 2>&1 &
```

Windows：

```bat
start "ShardingSphere MCP" cmd /c "bin\start.bat > logs\mcp-http.log 2>&1"
```

默认配置文件是 `conf/mcp-http.yaml`，默认端点是 `http://127.0.0.1:18088/mcp`。

## 接入 AI 应用

选择一个支持 MCP 的 AI 应用、IDE 插件或 Agent 平台，并配置上一步启动的 HTTP MCP Server 地址。

典型客户端配置见：

- [Codex](../client-integration/codex/)
- [Claude Code](../client-integration/claude-code/)

其他客户端请按其自身文档配置 ShardingSphere-MCP 地址：`http://127.0.0.1:18088/mcp`。

## 通过自然语言验证

配置完成后，在 AI 应用中输入以下任务验证 ShardingSphere-MCP 是否可以访问目标逻辑库：

- “查看 `logic_db` 中有哪些表。”
- “查看 `orders` 的字段和索引。”
- “查询 `orders` 前 10 行。”

如果可以返回逻辑库、表结构或查询结果，说明 MCP Server 已经可以通过 AI 应用访问目标 ShardingSphere-Proxy 逻辑库。
进一步的部署方式、健康检查和基础可观测入口，请参考[部署说明](../deployment/)。
如果 AI 应用无法连接或看不到逻辑库，请查看[常见问题](../troubleshooting/)。
