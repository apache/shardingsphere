+++
title = "Configuration"
weight = 3
+++

ShardingSphere-MCP uses YAML files to configure the transport and the databases that the MCP Server can connect to.
The packaged distribution reads `conf/mcp-http.yaml` by default and also ships `conf/mcp-stdio.yaml`.

## Transport configuration

Each MCP Server process must select exactly one transport.

HTTP example:

```yaml
transport:
  type: STREAMABLE_HTTP
  http:
    bindHost: 127.0.0.1
    port: 18088
    endpointPath: /mcp
```

STDIO example:

```yaml
transport:
  type: STDIO
```

| Configuration item | Description |
| --- | --- |
| `transport.type` | Transport type. Supported values are `STREAMABLE_HTTP` and `STDIO`. |
| `transport.http` | HTTP transport configuration, used only when `transport.type` is `STREAMABLE_HTTP`. |
| `transport.http.bindHost` | HTTP bind host. Defaults to `127.0.0.1`. Loopback values allow local access only. `0.0.0.0` or an intranet IP allows access through that interface. |
| `transport.http.port` | HTTP bind port. The default value is `18088`. |
| `transport.http.endpointPath` | HTTP endpoint path. The default value is `/mcp`. |

## Database configuration

`runtimeDatabases` defines the databases that the MCP Server can connect to and expose to users.
Each entry key is the database name that users reference in natural-language tasks. It usually maps to a logical database exposed by ShardingSphere-Proxy.

```yaml
runtimeDatabases:
  "<logic-database>":
    databaseType: MySQL
    jdbcUrl: "jdbc:mysql://<proxy-host>:<proxy-port>/<logic-database>"
    username: "<proxy-username>"
    password: "<proxy-password>"
    driverClassName: "com.mysql.cj.jdbc.Driver"
```

| *Name* | *Description* |
| --- | --- |
| `databaseType` (+) | Database protocol or dialect type of the connection endpoint, such as `MySQL` or `PostgreSQL`. It affects metadata recognition and SQL capability judgment; it does not mean the endpoint is necessarily a physical database or ShardingSphere-Proxy. |
| `jdbcUrl` (+) | JDBC URL used by the MCP Server to connect to the runtime database. Point it to a Proxy logical database when using ShardingSphere rule capabilities. |
| `username` (+) | Username for the runtime database, usually the ShardingSphere-Proxy logical database username. |
| `password` (?) | Password for the runtime database. |
| `driverClassName` (+) | JDBC driver class name, such as `com.mysql.cj.jdbc.Driver` for the MySQL driver. |

Legend:

- (+) means required.
- (?) means optional.

Notes:

- When the target is ShardingSphere-Proxy, users see ShardingSphere logical databases, not physical storage units.
- When the target is a physical database, users see metadata from that JDBC target, not ShardingSphere rule state.
- Schema, table, view, index, and sequence metadata depends on target JDBC metadata. Proxy-visible metadata and physical database metadata may differ.
- If the target JDBC driver is not packaged, copy the driver jar under `plugins/`.

## Choose a Connection Target

`runtimeDatabases` can use any reachable JDBC URL. The database objects users can see and the governance tasks they can perform depend on the connection target.

### Connecting to a ShardingSphere-Proxy logical database

Connect to a ShardingSphere-Proxy logical database when ShardingSphere rule state, data encryption, data masking, or rule change capabilities are required.

Users see the logical databases, tables, and columns exposed by Proxy.
Proxy-visible metadata may differ from the complete physical database structure. Plans involving physical columns, indexes, or rule changes should be reviewed before execution.

### Connecting to a physical database

Connect directly to a physical database when only ordinary metadata inspection, object search, or controlled queries are needed.

Users see metadata from the target database itself, not ShardingSphere rule state.
Tasks that depend on ShardingSphere rules, such as data encryption and data masking, do not apply to direct physical database connections.

For natural-language tasks supported by each connection target, see [Capability Catalog](../capabilities/).

## Plugin directory

The packaged distribution keeps MCP Server dependencies and built-in MCP feature plugin jars under `lib/`.
If your target database driver or an extra MCP feature plugin jar is not packaged, copy it under the distribution `plugins/` directory before starting the MCP Server.

## Custom configuration file

Unix-like systems:

```bash
bin/start.sh /path/to/mcp-http.yaml
```

Windows:

```bat
bin\start.bat path\to\mcp-http.yaml
```

For Docker, set `SHARDINGSPHERE_MCP_CONFIG` to an absolute config path inside the container.
