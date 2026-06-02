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

Run in HTTP mode:

```bash
docker run --rm -p 18088:18088 ghcr.io/apache/shardingsphere-mcp:${latest.release.version}
```

Run in STDIO mode:

```bash
docker run --rm -i \
  -e SHARDINGSPHERE_MCP_TRANSPORT=stdio \
  ghcr.io/apache/shardingsphere-mcp:${latest.release.version}
```

Use a custom configuration file:

```bash
docker run --rm -p 18088:18088 \
  -e SHARDINGSPHERE_MCP_CONFIG=/opt/shardingsphere-mcp/conf/custom-mcp-http.yaml \
  -v /path/to/mcp-http.yaml:/opt/shardingsphere-mcp/conf/custom-mcp-http.yaml:ro \
  -v /path/to/plugins:/opt/shardingsphere-mcp/plugins:ro \
  ghcr.io/apache/shardingsphere-mcp:${latest.release.version}
```

Configure `runtimeDatabases` according to the target capability boundary:

- Point it to a ShardingSphere-Proxy logical database when using ShardingSphere rule capabilities or feature plugin workflows.
- Point it to any reachable JDBC database only for general JDBC metadata, metadata search, and controlled SQL capabilities.

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

## Logs

- HTTP mode: inspect the startup terminal and `logs/mcp.log`.
- STDIO mode: stdout is reserved for MCP protocol frames; inspect stderr or `logs/mcp.log` for diagnostics.
