+++
title = "Quick Start"
weight = 1
+++

This page shows how to build ShardingSphere-MCP from source, connect to a ShardingSphere-Proxy logical database prepared by the user, and verify metadata reads and read-only SQL queries over HTTP.

## Prerequisites

- JDK 21 available from `JAVA_HOME` or `PATH`.
- A ShardingSphere-Proxy logical database reachable through JDBC.
- `curl` for HTTP requests.

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
  "<logic-database>":
    databaseType: MySQL
    jdbcUrl: "jdbc:mysql://<proxy-host>:<proxy-port>/<logic-database>"
    username: "<proxy-username>"
    password: "<proxy-password>"
    driverClassName: "com.mysql.cj.jdbc.Driver"
```

Replace `<logic-database>`, `<proxy-host>`, `<proxy-port>`, `<proxy-username>`, and `<proxy-password>` with the actual ShardingSphere-Proxy connection information.
If the target database driver is not packaged, copy the corresponding JDBC driver jar to `plugins/` before startup.

## Start the HTTP MCP Server

Unix-like systems:

```bash
bin/start.sh > logs/mcp-http.log 2>&1 &
MCP_PID=$!
```

Windows:

```bat
start "ShardingSphere MCP" cmd /c "bin\start.bat > logs\mcp-http.log 2>&1"
```

The default configuration file is `conf/mcp-http.yaml`, and the default endpoint is `http://127.0.0.1:18088/mcp`.
The Unix-like example starts the MCP Server in the background and stores the process id in `MCP_PID` so it can be stopped at the end.

## Initialize an MCP session

```bash
curl -i -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  --data '{"jsonrpc":"2.0","id":"init-1","method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{},"clientInfo":{"name":"curl-client","version":"1.0.0"}}}'
```

Expected result:

- The response headers include `MCP-Session-Id`.
- The response headers include `MCP-Protocol-Version`.

Notify the server that the client has completed initialization:

```bash
curl -i -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'MCP-Session-Id: <MCP-Session-Id value>' \
  -H 'MCP-Protocol-Version: <MCP-Protocol-Version value>' \
  --data '{"jsonrpc":"2.0","method":"notifications/initialized","params":{}}'
```

Expected result:

- The HTTP status code is `202`.
- Later HTTP requests must include both response headers.

## Read a metadata resource

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'MCP-Session-Id: <MCP-Session-Id value>' \
  -H 'MCP-Protocol-Version: <MCP-Protocol-Version value>' \
  --data '{"jsonrpc":"2.0","id":"resource-1","method":"resources/read","params":{"uri":"shardingsphere://databases"}}'
```

Expected result:

- The response type is `text/event-stream`.
- The JSON payload is in the `data:` line.
- The payload contains the logical database name that replaces `<logic-database>`.

## Search metadata

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

Expected result:

- The JSON payload contains matched tables or views.
- Each result item may include resource hints for follow-up reads.

## Execute a read-only query

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

Expected result:

- `result_kind` is `result_set`.
- `statement_class` is `query`.
- The payload contains `columns`, `rows`, or `row_objects`.

## Close the session and stop the server

Unix-like systems:

```bash
curl -sS -D - -o /dev/null \
  -X DELETE http://127.0.0.1:18088/mcp \
  -H 'MCP-Session-Id: <MCP-Session-Id value>' \
  -H 'MCP-Protocol-Version: <MCP-Protocol-Version value>'
kill "${MCP_PID}"
```

Windows:

```bat
curl -sS -D - -o NUL ^
  -X DELETE http://127.0.0.1:18088/mcp ^
  -H "MCP-Session-Id: <MCP-Session-Id value>" ^
  -H "MCP-Protocol-Version: <MCP-Protocol-Version value>"
```

Then press `Ctrl+C` in the `ShardingSphere MCP` startup window, or close that window, to stop the MCP Server process.

Expected result:

- The HTTP status code is `200`.
