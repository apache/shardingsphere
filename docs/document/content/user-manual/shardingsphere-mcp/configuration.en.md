+++
title = "Configuration"
weight = 3
+++

ShardingSphere-MCP uses YAML files to configure the transport and the databases that the MCP Server can connect to.
The packaged distribution reads `conf/mcp-http.yaml` by default and also ships `conf/mcp-stdio.yaml` and `conf/mcp-http-docker.yaml`.

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

| Configuration item            | Description                                                                                                                                         |
|-------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| `transport.type`              | Transport type. Supported values are `STREAMABLE_HTTP` and `STDIO`.                                                                                 |
| `transport.http`              | HTTP transport configuration, used only when `transport.type` is `STREAMABLE_HTTP`.                                                                 |
| `transport.http.bindHost`     | HTTP bind host. Defaults to `127.0.0.1`. Loopback values allow local access only. `0.0.0.0` or an intranet IP allows access through that interface. |
| `transport.http.port`         | HTTP bind port. The default value is `18088`.                                                                                                       |
| `transport.http.endpointPath` | HTTP endpoint path. The default value is `/mcp`.                                                                                                    |

### HTTP Session Attribution (Optional)

When ShardingSphere-MCP runs behind a trusted gateway or reverse proxy, the gateway can inject trusted headers to associate an MCP session with an external user or request source.
This configuration does not provide authentication or authorization. Authentication, authorization, and header injection should still be handled by the outer gateway.

```yaml
transport:
  type: STREAMABLE_HTTP
  http:
    sessionAttributionSource:
      subjectHeader: X-ShardingSphere-MCP-Subject
      sourceHeader: X-ShardingSphere-MCP-Source
      attributeHeaderPrefix: X-ShardingSphere-MCP-Attribute-
```

| Configuration item                                              | Description                                                                      |
|-----------------------------------------------------------------|----------------------------------------------------------------------------------|
| `transport.http.sessionAttributionSource`                       | HTTP session attribution source. When omitted, session attribution is not bound. |
| `transport.http.sessionAttributionSource.subjectHeader`         | Header name for the external user, tenant, or request subject.                   |
| `transport.http.sessionAttributionSource.sourceHeader`          | Header name for the request source.                                              |
| `transport.http.sessionAttributionSource.attributeHeaderPrefix` | Header prefix for custom attribution attributes.                                 |

Enable this only when clients cannot forge these headers directly.

## Database configuration

`runtimeDatabases` defines the databases that the MCP Server can connect to and expose to users. It may be omitted or empty when the server starts without database-backed capabilities.
Each entry key is the database name that users reference in natural-language tasks. It usually maps to a logical database exposed by ShardingSphere-Proxy.
The MCP Server resolves the database type from `jdbcUrl`; use a JDBC driver class that matches the configured URL.

```yaml
runtimeDatabases:
  "logic_db":
    jdbcUrl: "jdbc:mysql://127.0.0.1:3307/logic_db"
    username: "root"
    password: ""
    driverClassName: "com.mysql.cj.jdbc.Driver"
```

| *Name*                | *Description*                                                                                                                                                         |
|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `jdbcUrl` (+)         | JDBC URL used by the MCP Server to connect to the runtime database and resolve the database type. Use a Proxy logical database for ShardingSphere rule capabilities. |
| `username` (+)        | Username for the runtime database, usually the ShardingSphere-Proxy logical database username.                                                                        |
| `password` (?)        | Password for the runtime database.                                                                                                                                    |
| `driverClassName` (+) | JDBC driver class name, such as `com.mysql.cj.jdbc.Driver` for the MySQL driver.                                                                                      |

Legend:

- (+) means required.
- (?) means optional.

Notes:

- When the target is ShardingSphere-Proxy, users see ShardingSphere logical databases, not physical storage units.
- With a direct database connection, users see metadata from the target database itself, not ShardingSphere rule state.
- Schema, table, view, index, and sequence metadata depends on JDBC metadata from the connection target. Proxy-visible metadata and direct-connection metadata may differ.
- If the target JDBC driver is not packaged, copy the driver jar under `plugins/`.
- The sample values such as `logic_db` and `127.0.0.1:3307` are examples only. Runtime YAML files reject unresolved angle-bracket placeholder syntax.

## Secret Placeholders

Rule-change tools may require sensitive algorithm parameters such as keys, tokens, or replacement characters.
Do not put real sensitive values into model-visible tool input, ordinary documents, chat records, or logs.
Tool input can pass a secret placeholder object inside algorithm properties. The MCP Server only plans, previews, and generates safe manual execution packages; it does not read or resolve real values.
Planning, preview, and manual artifact packages return only neutral placeholders or `******`; they do not return `secret_ref` or real sensitive values.

Example reference object in algorithm properties:

```json
{
  "primary_algorithm_properties": {
    "aes-key-value": {
      "secret_ref": "placeholder://secret-value-1"
    }
  }
}
```

Notes:

- MCP Server only records the sensitive slot that requires manual replacement; it does not fetch real sensitive values from external systems.
- Automatic execution with sensitive placeholders stops before side effects and returns `secret_reference_manual_execution_required`.
- When using a manual execution package, operators replace neutral placeholders with real values outside MCP and the AI application, then execute the DistSQL or YAML.
- Documentation and examples use neutral placeholders only to avoid exposing real keys, paths, or internal system details to model context.

## Choose a Connection Target

`runtimeDatabases` can use any reachable JDBC URL. The database objects users can see and the governance tasks they can perform depend on the connection target.

### Connecting to a ShardingSphere-Proxy logical database

Connect to a ShardingSphere-Proxy logical database when ShardingSphere rule state, data encryption, data masking, or rule change capabilities are required.

Users see the logical databases, tables, and columns exposed by Proxy.
Proxy-visible metadata may differ from the complete physical database structure. Plans involving metadata interpretation or rule changes should be reviewed before execution.

### Direct database connection

A direct database connection means that ShardingSphere-MCP connects to a user-provided database service such as MySQL or PostgreSQL without going through ShardingSphere-Proxy.
Use it when only metadata inspection, object search, or controlled queries against an existing database are needed.

Users see metadata from the target database itself, not ShardingSphere rule state.
Tasks that depend on ShardingSphere rules, such as data encryption and data masking, do not apply to direct database connections.

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
