+++
title = "Configuration"
weight = 2
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

Notes:

- `transport.type` supports `STREAMABLE_HTTP` and `STDIO`.
- `transport.http` is valid only when `transport.type` is `STREAMABLE_HTTP`.
- In HTTP mode, `transport.http` can be omitted. The default `bindHost`, `port`, and `endpointPath` are `127.0.0.1`, `18088`, and `/mcp`.
- `127.0.0.1`, `localhost`, and `::1` are local-only bindings.
- `0.0.0.0` or a specific intranet IP exposes the matching network interface.

## Database configuration

`runtimeDatabases` defines the databases that the MCP Server can connect to and expose through MCP.
Each entry key is the database name used in MCP calls. It usually maps to a logical database exposed by ShardingSphere-Proxy.

```yaml
runtimeDatabases:
  logic_db:
    databaseType: MySQL
    jdbcUrl: "jdbc:mysql://127.0.0.1:3307/logic_db"
    username: "root"
    password: "root"
    driverClassName: "com.mysql.cj.jdbc.Driver"
```

Fields:

- `databaseType`: required database type, such as `MySQL` or `PostgreSQL`.
- `jdbcUrl`: required JDBC URL used by the MCP Server to connect to the database.
- `username`: required field; use an empty string `""` when no username is needed.
- `password`: required field; use an empty string `""` when no password is needed.
- `driverClassName`: required field; use an empty string `""` when a JDBC 4 driver auto-registers and no explicit override is needed.

Notes:

- MCP resources expose ShardingSphere logical databases, not physical storage units.
- Encrypt and Mask plugin workflows should connect to logical databases exposed by ShardingSphere-Proxy.
- Schema, table, view, index, and sequence metadata depends on target JDBC metadata.
- If the target JDBC driver is not packaged, copy the driver jar under `plugins/`.

## Plugin directory

The packaged distribution keeps MCP Server dependencies and official feature plugin jars under `lib/`, including Encrypt and Mask plugins.
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

## JVM options

Unix-like systems:

```bash
JAVA_OPTS="-Xms256m -Xmx256m" bin/start.sh
```

Windows:

```bat
set "JAVA_OPTS=-Xms256m -Xmx256m" && bin\start.bat
```
