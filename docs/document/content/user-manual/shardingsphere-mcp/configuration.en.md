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
| `transport.http.bindHost` | HTTP bind host. The default value is `127.0.0.1`. |
| `transport.http.port` | HTTP bind port. The default value is `18088`. |
| `transport.http.endpointPath` | HTTP endpoint path. The default value is `/mcp`. |

| Bind address | Description |
| --- | --- |
| `127.0.0.1`, `localhost`, `::1` | Allows local access only. |
| `0.0.0.0` or a specific intranet IP | Allows access through the matching network interface. |

## Database configuration

`runtimeDatabases` defines the databases that the MCP Server can connect to and expose through MCP.
Each entry key is the database name used in MCP calls. It usually maps to a logical database exposed by ShardingSphere-Proxy.

```yaml
runtimeDatabases:
  "<logic-database>":
    databaseType: MySQL
    jdbcUrl: "jdbc:mysql://<proxy-host>:<proxy-port>/<logic-database>"
    username: "<proxy-username>"
    password: "<proxy-password>"
    driverClassName: "com.mysql.cj.jdbc.Driver"
```

| Field | Required | Description |
| --- | --- | --- |
| `databaseType` | Yes | Database type, such as `MySQL` or `PostgreSQL`. |
| `jdbcUrl` | Yes | JDBC URL used by the MCP Server to connect to the logical database. |
| `username` | Yes | Username for the ShardingSphere-Proxy logical database. |
| `password` | No | Password for the ShardingSphere-Proxy logical database. Omit it or use an empty string `""` for a no-password account. |
| `driverClassName` | Yes | JDBC driver class name, such as `com.mysql.cj.jdbc.Driver` for the MySQL driver. |

Notes:

- MCP resources expose ShardingSphere logical databases, not physical storage units.
- Schema, table, view, index, and sequence metadata depends on target JDBC metadata.
- If the target JDBC driver is not packaged, copy the driver jar under `plugins/`.

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
