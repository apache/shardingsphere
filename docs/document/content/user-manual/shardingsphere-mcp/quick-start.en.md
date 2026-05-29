+++
title = "Quick Start"
weight = 1
+++

This page shows how to build ShardingSphere-MCP from source, configure an existing ShardingSphere-Proxy logical database, and verify metadata reads and read-only SQL queries over HTTP.
The examples assume a logical database named `logic_db` with an `orders` table. Replace them with your own logical database and table names.

## Prerequisites

- JDK 21 available from `JAVA_HOME` or `PATH`.
- A ShardingSphere-Proxy logical database reachable through JDBC.
- `curl` for HTTP requests.
- A terminal that supports sh/bash syntax.

## Build the distribution

Run from the repository root:

```bash
./mvnw -pl distribution/mcp -am -DskipTests package
```

Enter the distribution directory:

```bash
cd distribution/mcp/target/apache-shardingsphere-mcp-*
```

Expected result:

- The current directory contains `bin/`, `conf/`, and `lib/`.

## Configure the database

Edit `conf/mcp-http.yaml` and point `runtimeDatabases` to an existing ShardingSphere-Proxy logical database:

```yaml
runtimeDatabases:
  logic_db:
    databaseType: MySQL
    jdbcUrl: "jdbc:mysql://127.0.0.1:3307/logic_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    username: "root"
    password: "root"
    driverClassName: "com.mysql.cj.jdbc.Driver"
```

If the target database driver is not packaged, copy the corresponding JDBC driver jar to `plugins/` before startup.

## Start the HTTP MCP Server

```bash
bin/start.sh > logs/mcp-http.log 2>&1 &
MCP_PID=$!
```

The default configuration file is `conf/mcp-http.yaml`, and the default endpoint is `http://127.0.0.1:18088/mcp`.
The startup script runs in the foreground by default. The quick start backgrounds it through the shell so the same terminal can continue running `curl`.
Keep it in the foreground when running under containers, systemd, or another process manager.

## Initialize an MCP session

```bash
curl -i -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  --data '{"jsonrpc":"2.0","id":"init-1","method":"initialize","params":{"capabilities":{},"clientInfo":{"name":"curl-demo","version":"1.0.0"}}}'
```

Expected result:

- The response headers include `MCP-Session-Id`.
- The response headers include `MCP-Protocol-Version`.
- Later HTTP requests must include both response headers.

Copy the values from the response headers and set them in the current terminal:

```bash
export SESSION_ID="<MCP-Session-Id value>"
export PROTOCOL_VERSION="<MCP-Protocol-Version value>"
```

## Read a metadata resource

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"resource-1","method":"resources/read","params":{"uri":"shardingsphere://databases"}}'
```

Expected result:

- The response type is `text/event-stream`.
- The JSON payload is in the `data:` line.
- The payload contains `logic_db`.

## Search metadata

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"tool-1","method":"tools/call","params":{"name":"database_gateway_search_metadata","arguments":{"database":"logic_db","query":"order","object_types":["table","view"]}}}'
```

Expected result:

- The JSON payload contains matched tables or views.
- Each result item may include resource hints for follow-up reads.

## Execute a read-only query

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

Expected result:

- `result_kind` is `result_set`.
- `statement_class` is `query`.
- The payload contains `columns`, `rows`, or `row_objects`.

## Close the session and stop the server

```bash
curl -sS -D - -o /dev/null \
  -X DELETE http://127.0.0.1:18088/mcp \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}"
kill "${MCP_PID}"
```

Expected result:

- The HTTP status code is `200`.
