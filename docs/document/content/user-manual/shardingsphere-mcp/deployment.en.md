+++
title = "Deployment"
weight = 6
+++

ShardingSphere-MCP can run from the standalone distribution built from source or from the official OCI image.

## Standalone distribution

Build the distribution:

```bash
./mvnw -pl distribution/mcp -am -DskipTests package
```

The distribution directory contains:

- `bin/`: startup scripts.
- `conf/`: default configuration and logging configuration.
- `lib/`: MCP Server dependencies and built-in MCP feature plugins.
- `plugins/`: external JDBC drivers or extra MCP feature plugin jars.
- `logs/`: runtime logs.

## OCI image

Official MCP Registry metadata lives in `mcp/server.json`.
The published server name is `io.github.apache/shardingsphere-mcp`.
The OCI image shape is:

```text
ghcr.io/apache/shardingsphere-mcp:<version>
```

Before using the OCI image, prepare a custom configuration file.
When HTTP mode runs in a container, `bindHost` should bind to a network interface that the container can expose, such as `0.0.0.0`:

```yaml
transport:
  type: STREAMABLE_HTTP
  http:
    bindHost: 0.0.0.0
    port: 18088
    endpointPath: /mcp

runtimeDatabases:
  "<logic-database>":
    databaseType: MySQL
    jdbcUrl: "jdbc:mysql://<proxy-host>:<proxy-port>/<logic-database>"
    username: "<proxy-username>"
    password: "<proxy-password>"
    driverClassName: "com.mysql.cj.jdbc.Driver"
```

Run in HTTP mode with a custom configuration file and plugin directory:

```bash
docker run --rm -p 18088:18088 \
  -e SHARDINGSPHERE_MCP_CONFIG=/opt/shardingsphere-mcp/conf/custom-mcp-http.yaml \
  -v /path/to/mcp-http.yaml:/opt/shardingsphere-mcp/conf/custom-mcp-http.yaml:ro \
  -v /path/to/plugins:/opt/shardingsphere-mcp/plugins:ro \
  ghcr.io/apache/shardingsphere-mcp:${latest.release.version}
```

When running in STDIO mode, set `transport.type` in the configuration file to `STDIO`:

```bash
docker run --rm -i \
  -e SHARDINGSPHERE_MCP_CONFIG=/opt/shardingsphere-mcp/conf/custom-mcp-stdio.yaml \
  -v /path/to/mcp-stdio.yaml:/opt/shardingsphere-mcp/conf/custom-mcp-stdio.yaml:ro \
  -v /path/to/plugins:/opt/shardingsphere-mcp/plugins:ro \
  ghcr.io/apache/shardingsphere-mcp:${latest.release.version}
```

Configure `runtimeDatabases` according to the target capability boundary:

- Point it to a ShardingSphere-Proxy logical database when using ShardingSphere rule capabilities or the rule change flow.
- Use a direct database connection only for metadata search, metadata inspection, and controlled SQL capabilities.

## Secure deployment

The built-in HTTP Server does not provide authentication or authorization.
For remote access, place it in a trusted network or behind a reverse proxy or gateway that handles:

- TLS termination.
- Authentication.
- Authorization policy.
- Network access control.
- Audit logs.

HTTP binding recommendations:

- Use `127.0.0.1` for local debugging.
- Use a controlled network interface for container or intranet deployments.
- Avoid exposing the MCP Server directly to remote clients.
- When sessions must be associated with external users or request sources, let a trusted gateway inject session attribution headers. Do not allow clients to forge these headers directly.

## Logs

- HTTP mode: inspect the startup terminal and `logs/mcp.log`.
- STDIO mode: do not use stdout as a log inspection entry; inspect stderr or `logs/mcp.log` for diagnostics.
