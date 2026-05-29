+++
title = "快速开始"
weight = 1
+++

本页演示如何从源码构建 ShardingSphere-MCP，连接一个用户已准备好的 ShardingSphere-Proxy 逻辑库，并通过 HTTP 验证元数据读取和只读 SQL 查询。

## 前置条件

- `JAVA_HOME` 或 `PATH` 中可用的 JDK 21。
- 一个可通过 JDBC 访问的 ShardingSphere-Proxy 逻辑库。
- `curl`，用于发送 HTTP 请求。

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
  "<logic-database>":
    databaseType: MySQL
    jdbcUrl: "jdbc:mysql://<proxy-host>:<proxy-port>/<logic-database>"
    username: "<proxy-username>"
    password: "<proxy-password>"
    driverClassName: "com.mysql.cj.jdbc.Driver"
```

将 `<logic-database>`、`<proxy-host>`、`<proxy-port>`、`<proxy-username>` 和 `<proxy-password>` 替换为 ShardingSphere-Proxy 的实际连接信息。
如果目标数据库驱动没有随发行包提供，请在启动前把对应 JDBC 驱动 jar 放入 `plugins/`。

## 启动 HTTP MCP Server

Unix-like 系统：

```bash
bin/start.sh > logs/mcp-http.log 2>&1 &
MCP_PID=$!
```

Windows：

```bat
start "ShardingSphere MCP" cmd /c "bin\start.bat > logs\mcp-http.log 2>&1"
```

默认配置文件是 `conf/mcp-http.yaml`，默认端点是 `http://127.0.0.1:18088/mcp`。
Unix-like 示例会在当前终端后台启动 MCP Server，并把进程号保存到 `MCP_PID`，方便最后停止服务。

## 初始化 MCP 会话

```bash
curl -i -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  --data '{"jsonrpc":"2.0","id":"init-1","method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{},"clientInfo":{"name":"curl-client","version":"1.0.0"}}}'
```

预期结果：

- 响应头包含 `MCP-Session-Id`。
- 响应头包含 `MCP-Protocol-Version`。

通知服务端客户端已完成初始化：

```bash
curl -i -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'MCP-Session-Id: <MCP-Session-Id value>' \
  -H 'MCP-Protocol-Version: <MCP-Protocol-Version value>' \
  --data '{"jsonrpc":"2.0","method":"notifications/initialized","params":{}}'
```

预期结果：

- HTTP 状态码是 `202`。
- 后续 HTTP 请求必须携带这两个响应头。

## 读取元数据资源

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'MCP-Session-Id: <MCP-Session-Id value>' \
  -H 'MCP-Protocol-Version: <MCP-Protocol-Version value>' \
  --data '{"jsonrpc":"2.0","id":"resource-1","method":"resources/read","params":{"uri":"shardingsphere://databases"}}'
```

预期结果：

- 响应类型是 `text/event-stream`。
- JSON 负载位于 `data:` 行。
- 返回内容包含 `<logic-database>` 对应的逻辑库名称。

## 搜索元数据

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'MCP-Session-Id: <MCP-Session-Id value>' \
  -H 'MCP-Protocol-Version: <MCP-Protocol-Version value>' \
  --data '{
    "jsonrpc":"2.0",
    "id":"tool-1",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_search_metadata",
      "arguments":{
        "database":"<logic-database>",
        "query":"<metadata-keyword>",
        "object_types":["table","view"]
      }
    }
  }'
```

预期结果：

- JSON 负载包含匹配到的表或视图。
- 结果项可包含后续读取用的资源提示。

## 执行只读查询

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'MCP-Session-Id: <MCP-Session-Id value>' \
  -H 'MCP-Protocol-Version: <MCP-Protocol-Version value>' \
  --data '{
    "jsonrpc":"2.0",
    "id":"tool-2",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_execute_query",
      "arguments":{
        "database":"<logic-database>",
        "sql":"SELECT * FROM <table-name> LIMIT 10",
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

Unix-like 系统：

```bash
curl -sS -D - -o /dev/null \
  -X DELETE http://127.0.0.1:18088/mcp \
  -H 'MCP-Session-Id: <MCP-Session-Id value>' \
  -H 'MCP-Protocol-Version: <MCP-Protocol-Version value>'
kill "${MCP_PID}"
```

Windows：

```bat
curl -sS -D - -o NUL ^
  -X DELETE http://127.0.0.1:18088/mcp ^
  -H "MCP-Session-Id: <MCP-Session-Id value>" ^
  -H "MCP-Protocol-Version: <MCP-Protocol-Version value>"
```

然后在 `ShardingSphere MCP` 启动窗口按 `Ctrl+C`，或直接关闭该窗口，停止 MCP Server 进程。

预期结果：

- HTTP 状态码是 `200`。
