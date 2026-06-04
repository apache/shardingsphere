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

### Trusted gateway and TLS termination example

The following example uses Nginx to illustrate the minimum layout in which the outer entry handles HTTPS while ShardingSphere-MCP continues to serve plain HTTP on a controlled network interface. Other reverse proxies, ingress controllers, ALBs, or API gateways can follow the same boundary.

ShardingSphere-MCP configuration example:

```yaml
transport:
  type: STREAMABLE_HTTP
  http:
    bindHost: 127.0.0.1
    port: 18088
    endpointPath: /mcp
    sessionAttributionSource:
      subjectHeader: X-ShardingSphere-MCP-Subject
      sourceHeader: X-ShardingSphere-MCP-Source
      attributeHeaderPrefix: X-ShardingSphere-MCP-Attribute-
```

Nginx example:

```nginx
server {
  listen 443 ssl http2;
  server_name mcp.example.com;

  ssl_certificate     /etc/nginx/certs/mcp.crt;
  ssl_certificate_key /etc/nginx/certs/mcp.key;

  location /mcp {
    proxy_pass http://127.0.0.1:18088/mcp;
    proxy_http_version 1.1;
    proxy_pass_request_headers off;

    proxy_set_header Host $host;
    proxy_set_header Content-Type $http_content_type;
    proxy_set_header Accept $http_accept;
    proxy_set_header MCP-Session-Id $http_mcp_session_id;
    proxy_set_header MCP-Protocol-Version $http_mcp_protocol_version;
    proxy_set_header X-Forwarded-Proto https;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  }
}
```

The important points of this layout are:

- TLS terminates at the trusted gateway, so the MCP process does not manage public certificates directly.
- ShardingSphere-MCP continues to bind to loopback or a controlled intranet interface instead of being exposed directly to the public network.
- Authentication, authorization, rate limiting, and network access control remain the responsibility of the outer gateway, not the built-in HTTP Server.

### Session attribution wiring example

When the outer gateway already identifies the caller, let it overwrite the session attribution headers before forwarding the request. The MCP Runtime then binds the resulting attribution to the session context. Keep the header names and prefix aligned with `transport.http.sessionAttributionSource` in the [Configuration](../configuration/) document.

Nginx example:

```nginx
location /mcp {
  proxy_pass http://127.0.0.1:18088/mcp;
  proxy_http_version 1.1;
  proxy_pass_request_headers off;

  proxy_set_header Host $host;
  proxy_set_header Content-Type $http_content_type;
  proxy_set_header Accept $http_accept;
  proxy_set_header MCP-Session-Id $http_mcp_session_id;
  proxy_set_header MCP-Protocol-Version $http_mcp_protocol_version;
  proxy_set_header X-Forwarded-Proto https;
  proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

  proxy_set_header X-ShardingSphere-MCP-Subject $remote_user;
  proxy_set_header X-ShardingSphere-MCP-Source gateway-nginx;
  proxy_set_header X-ShardingSphere-MCP-Attribute-Environment production;
}
```

Follow these rules when wiring attribution:

- Use an allow-list for proxied request headers, or strip/reject every client-supplied header that matches the configured attribution header names and prefix before injecting trusted values.
- `subjectHeader` represents the external subject, such as a trial user, a commercial customer identifier, or an internal caller identity.
- `sourceHeader` identifies the request source, such as `gateway-nginx`, `internal-alb`, or another trusted ingress name.
- `attributeHeaderPrefix` can carry a small amount of non-sensitive context such as environment, region, or integration channel; do not pass passwords, keys, or tokens through these headers.
- If the deployment does not need to bind request attribution into the session context, omit `sessionAttributionSource`.

After wiring the gateway, continue with the health checks below to confirm that:

- The gateway endpoint is reachable.
- The MCP protocol is ready.
- Runtime databases are ready.
- Later requests in the same session do not fail because the attribution headers change.

## Health Checks

After deployment, verify that ShardingSphere-MCP is truly usable instead of stopping at “the HTTP port is reachable”:

1. Service process and endpoint are reachable

   - In HTTP mode, confirm that the process has started, the port is listening, and `http://<bind-host>:<port><endpointPath>` matches the client configuration.
   - In STDIO mode, confirm that the AI application launches the MCP process correctly and does not treat stdin/stdout as an interactive shell.

2. MCP protocol is ready

   - Confirm from the AI application that the MCP Server is recognized, or follow the protocol debugging examples in the [Custom Integration Appendix](../developer-appendix/) to complete `initialize` and read capabilities.
   - If HTTP responses are reachable but capabilities, resources, or tools cannot be listed, the endpoint is reachable but the MCP protocol is not yet wired correctly.

3. Runtime databases are ready

   - Read `shardingsphere://runtime` and confirm that the transport, runtime database summary, and readiness details are visible.
   - Call `database_gateway_validate_proxy_connectivity`, or run a minimal task such as “Show tables in `<logic-database>`” from the AI application to confirm that the configured runtime database is usable.
   - A running MCP Server process alone does not mean that the target runtime database is ready. Connectivity failures, insufficient privileges, or invisible logical databases can still block tasks.

## Basic Observability Entrypoints

### Logs

- HTTP mode: inspect the startup terminal and `logs/mcp.log`.
- STDIO mode: do not use stdout as a log inspection entry; inspect stderr or `logs/mcp.log` for diagnostics.

### Runtime status and protection details

- `shardingsphere://runtime` exposes the current transport, runtime database summary, readiness details, and basic diagnostics.
- Runtime protection details show boundaries such as row limits, query timeout limits, and session-level tool-call protection.
- When a runtime database connection fails, use the returned failure category and recovery guidance to locate the issue. See [Troubleshooting](../troubleshooting/) for the full category list.

### Minimum troubleshooting evidence

When reporting an issue to an operator or troubleshooter, collect at least:

- The startup command or container run command.
- An MCP configuration summary, with passwords, keys, and tokens removed.
- The transport type, endpoint address, and target logical database names configured under `runtimeDatabases`.
- The MCP Server configuration summary from the AI application.
- The failed task, returned failure category, and the relevant excerpt from `logs/mcp.log`.

For symptom-oriented diagnosis, failure categories, and runtime protection guidance, see [Troubleshooting](../troubleshooting/). For direct MCP protocol debugging, see the [Custom Integration Appendix](../developer-appendix/).
