+++
title = "Quick Start"
weight = 1
+++

This section uses the packaged H2 demo runtime to verify the ShardingSphere-MCP HTTP transport, metadata discovery, and read-only SQL query behavior.

## Prerequisites

- JDK 21 available from `JAVA_HOME` or `PATH`.
- Maven Wrapper from the repository root.
- A Unix-like shell with `curl`, `find`, `mktemp`, `sed`, and `tr`.

## Build the distribution

```bash
./mvnw -pl distribution/mcp -am -DskipTests package
```

Resolve the distribution directory:

```bash
DIST_DIR=$(find distribution/mcp/target -maxdepth 1 -type d -name 'apache-shardingsphere-mcp-*' | sed -n '1p')
echo "${DIST_DIR}"
```

Expected result:

- The command prints a non-empty distribution directory.
- The directory contains `bin/`, `conf/`, and `lib/`.

## Start the HTTP runtime

```bash
cd "${DIST_DIR}"
bin/start.sh
```

On Windows:

```bat
cd /d "%DIST_DIR%"
bin\start.bat
```

The default configuration file is `conf/mcp-http.yaml`, and the default endpoint is `http://127.0.0.1:18088/mcp`.
The process runs in the foreground and writes logs under `logs/`.
Keep this terminal open, and run the following `curl` commands in a second terminal.

The packaged demo runtime exposes two logical databases: `orders` and `billing`.
They use the packaged H2 driver and seed data under `data/`.

## Initialize an MCP session

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

Expected result:

- `SESSION_ID` is not empty.
- `PROTOCOL_VERSION` is not empty.
- Later HTTP requests must include both headers.

## Read a metadata resource

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"resource-1","method":"resources/read","params":{"uri":"shardingsphere://databases/orders/schemas/public/tables"}}'
```

Expected result:

- The response type is `text/event-stream`.
- The JSON payload is in the `data:` line.
- The payload contains `orders`, `order_items`, and `active_orders`.

## Search metadata

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"tool-1","method":"tools/call","params":{"name":"database_gateway_search_metadata","arguments":{"database":"orders","query":"order","object_types":["table","view"]}}}'
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
        "database":"orders",
        "schema":"public",
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

## Close the session

```bash
curl -sS -D - -o /dev/null \
  -X DELETE http://127.0.0.1:18088/mcp \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}"
```

Expected result:

- The HTTP status code is `200`.
