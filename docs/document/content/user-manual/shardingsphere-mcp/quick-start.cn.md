+++
title = "快速开始"
weight = 1
+++

本节使用发行包内置的 H2 demo runtime 验证 ShardingSphere-MCP 的 HTTP transport、metadata discovery 和只读 SQL 查询。

## 前置条件

- `JAVA_HOME` 或 `PATH` 中可用的 JDK 21。
- 仓库根目录下的 Maven Wrapper。
- 类 Unix Shell，并包含 `curl`、`find`、`mktemp`、`sed` 和 `tr`。

## 构建发行包

```bash
./mvnw -pl distribution/mcp -am -DskipTests package
```

解析发行包目录：

```bash
DIST_DIR=$(find distribution/mcp/target -maxdepth 1 -type d -name 'apache-shardingsphere-mcp-*' | sed -n '1p')
echo "${DIST_DIR}"
```

预期结果：

- 命令打印一个非空发行包目录。
- 该目录包含 `bin/`、`conf/`、`lib/`。

## 启动 HTTP runtime

```bash
cd "${DIST_DIR}"
bin/start.sh
```

Windows 使用：

```bat
cd /d "%DIST_DIR%"
bin\start.bat
```

默认配置文件是 `conf/mcp-http.yaml`，默认端点是 `http://127.0.0.1:18088/mcp`。
进程以前台方式运行，日志写入 `logs/` 目录。
请保持当前终端不退出，并在第二个终端执行后续 `curl` 命令。

发行包内置 demo runtime 暴露两个逻辑库：`orders` 和 `billing`。
它们使用发行包自带 H2 驱动和 `data/` 下的种子数据。

## 初始化 MCP session

```bash
INIT_HEADERS=$(mktemp)
curl -sS -D "${INIT_HEADERS}" -o /dev/null http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  --data '{"jsonrpc":"2.0","id":"init-1","method":"initialize","params":{"capabilities":{},"clientInfo":{"name":"curl-demo","version":"1.0.0"}}}'
SESSION_ID=$(sed -n 's/^[Mm][Cc][Pp]-[Ss]ession-[Ii][Dd]: //p' "${INIT_HEADERS}" | tr -d '\r')
PROTOCOL_VERSION=$(sed -n 's/^[Mm][Cc][Pp]-[Pp]rotocol-[Vv]ersion: //p' "${INIT_HEADERS}" | tr -d '\r')
rm -f "${INIT_HEADERS}"
printf 'SESSION_ID=%s\nPROTOCOL_VERSION=%s\n' "${SESSION_ID}" "${PROTOCOL_VERSION}"
```

预期结果：

- `SESSION_ID` 非空。
- `PROTOCOL_VERSION` 非空。
- 后续 HTTP 请求必须携带这两个 header。

## 读取 metadata resource

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"resource-1","method":"resources/read","params":{"uri":"shardingsphere://databases/orders/schemas/public/tables"}}'
```

预期结果：

- 响应类型是 `text/event-stream`。
- JSON payload 位于 `data:` 行。
- 返回内容包含 `orders`、`order_items`、`active_orders`。

## 搜索 metadata

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"tool-1","method":"tools/call","params":{"name":"database_gateway_search_metadata","arguments":{"database":"orders","query":"order","object_types":["table","view"]}}}'
```

预期结果：

- JSON payload 包含匹配到的表或视图。
- 结果项会携带可继续读取的 resource hint。

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
        "database":"orders",
        "schema":"public",
        "sql":"SELECT status FROM orders ORDER BY order_id",
        "max_rows":10
      }
    }
  }'
```

预期结果：

- `result_kind` 为 `result_set`。
- `statement_class` 为 `query`。
- payload 包含 `columns`、`rows` 或 `row_objects`。

## 关闭 session

```bash
curl -sS -D - -o /dev/null \
  -X DELETE http://127.0.0.1:18088/mcp \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}"
```

预期结果：

- HTTP 状态码是 `200`。
