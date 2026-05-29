+++
title = "快速开始"
weight = 1
+++

本页演示如何从源码构建 ShardingSphere-MCP，配置一个已有的 ShardingSphere-Proxy 逻辑库，并通过 HTTP 验证元数据读取和只读 SQL 查询。
示例假设逻辑库名为 `logic_db`，其中存在 `orders` 表；实际使用时请替换为自己的逻辑库和表名。

## 前置条件

- `JAVA_HOME` 或 `PATH` 中可用的 JDK 21。
- 一个可通过 JDBC 访问的 ShardingSphere-Proxy 逻辑库。
- `curl`，用于发送 HTTP 请求。
- 支持 sh/bash 语法的终端。

## 构建发行包

在仓库根目录执行：

```bash
./mvnw -pl distribution/mcp -am -DskipTests package
```

进入发行包目录：

```bash
cd distribution/mcp/target/apache-shardingsphere-mcp-*
```

预期结果：

- 当前目录包含 `bin/`、`conf/`、`lib/`。

## 配置数据库

编辑 `conf/mcp-http.yaml`，将 `runtimeDatabases` 指向已有的 ShardingSphere-Proxy 逻辑库：

```yaml
runtimeDatabases:
  logic_db:
    databaseType: MySQL
    jdbcUrl: "jdbc:mysql://127.0.0.1:3307/logic_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    username: "root"
    password: "root"
    driverClassName: "com.mysql.cj.jdbc.Driver"
```

如果目标数据库驱动没有随发行包提供，请在启动前把对应 JDBC 驱动 jar 放入 `plugins/`。

## 启动 HTTP MCP Server

```bash
bin/start.sh > logs/mcp-http.log 2>&1 &
MCP_PID=$!
```

默认配置文件是 `conf/mcp-http.yaml`，默认端点是 `http://127.0.0.1:18088/mcp`。
启动脚本默认以前台方式运行；快速开始通过 shell 将其放到后台，便于在同一个终端继续执行 `curl`。
容器、systemd 或其他进程管理器场景建议保持前台运行。

## 初始化 MCP 会话

```bash
curl -i -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  --data '{"jsonrpc":"2.0","id":"init-1","method":"initialize","params":{"capabilities":{},"clientInfo":{"name":"curl-demo","version":"1.0.0"}}}'
```

预期结果：

- 响应头包含 `MCP-Session-Id`。
- 响应头包含 `MCP-Protocol-Version`。
- 后续 HTTP 请求必须携带这两个响应头。

从响应头复制取值，并在当前终端设置变量：

```bash
export SESSION_ID="<MCP-Session-Id value>"
export PROTOCOL_VERSION="<MCP-Protocol-Version value>"
```

## 读取元数据资源

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"resource-1","method":"resources/read","params":{"uri":"shardingsphere://databases"}}'
```

预期结果：

- 响应类型是 `text/event-stream`。
- JSON 负载位于 `data:` 行。
- 返回内容包含 `logic_db`。

## 搜索元数据

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"tool-1","method":"tools/call","params":{"name":"database_gateway_search_metadata","arguments":{"database":"logic_db","query":"order","object_types":["table","view"]}}}'
```

预期结果：

- JSON 负载包含匹配到的表或视图。
- 结果项可包含后续读取用的资源提示。

## 执行只读查询

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{
    "jsonrpc":"2.0",
    "id":"tool-2",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_execute_query",
      "arguments":{
        "database":"logic_db",
        "sql":"SELECT status FROM orders ORDER BY order_id",
        "max_rows":10
      }
    }
  }'
```

预期结果：

- `result_kind` 为 `result_set`。
- `statement_class` 为 `query`。
- 负载包含 `columns`、`rows` 或 `row_objects`。

## 关闭会话并停止服务

```bash
curl -sS -D - -o /dev/null \
  -X DELETE http://127.0.0.1:18088/mcp \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}"
kill "${MCP_PID}"
```

预期结果：

- HTTP 状态码是 `200`。
