+++
title = "Quick Start"
weight = 1
+++

This page shows how to build ShardingSphere-MCP from source, connect it to a user-prepared ShardingSphere-Proxy logical database, and verify basic database tasks through natural language in an AI application.

## Prerequisites

- JDK 21 available from `JAVA_HOME` or `PATH`.
- A ShardingSphere-Proxy logical database reachable through JDBC.
- An AI application, IDE extension, or agent platform that supports MCP.

## Build the Distribution

Run the following command from the repository root:

```bash
./mvnw -pl distribution/mcp -am -DskipTests package
```

Enter the distribution directory:

```bash
cd distribution/mcp/target/apache-shardingsphere-mcp-${version}
```

Expected result:

- The current directory contains `bin/`, `conf/`, and `lib/`.
- Replace `${version}` with the built distribution version, such as `5.5.4-SNAPSHOT`.

## Configure the Database

Edit `conf/mcp-http.yaml` and point `runtimeDatabases` to an existing ShardingSphere-Proxy logical database:

```yaml
runtimeDatabases:
  "logic_db":
    jdbcUrl: "jdbc:mysql://127.0.0.1:3307/logic_db"
    username: "root"
    password: ""
    driverClassName: "com.mysql.cj.jdbc.Driver"
```

Adjust `logic_db`, `127.0.0.1`, `3307`, `root`, and the empty password according to the actual ShardingSphere-Proxy connection information.
The MCP Server resolves the database type from `jdbcUrl`.
If the target database driver is not provided with the distribution, put the corresponding JDBC driver jar under `plugins/` before startup.

## Start the HTTP MCP Server

Unix-like systems:

```bash
bin/start.sh > logs/mcp-http.log 2>&1 &
```

Windows:

```bat
start "ShardingSphere MCP" cmd /c "bin\start.bat > logs\mcp-http.log 2>&1"
```

The default configuration file is `conf/mcp-http.yaml`, and the default endpoint is `http://127.0.0.1:18088/mcp`.

## Connect an AI Application

Choose an MCP-capable AI application, IDE extension, or agent platform, and configure the HTTP MCP Server address started in the previous step.

Typical client configuration examples:

- [Codex](../client-integration/codex/)
- [Claude Code](../client-integration/claude-code/)

For other clients, follow their own documentation and use the ShardingSphere-MCP address: `http://127.0.0.1:18088/mcp`.

## Verify through Natural Language

After configuration, enter the following tasks in the AI application to verify that ShardingSphere-MCP can access the target logical database:

- "Show tables in `logic_db`."
- "Show columns and indexes for `orders`."
- "Query the first 10 rows from `orders`."

If the application returns the logical database, table structure, or query results, the MCP Server can access the target ShardingSphere-Proxy logical database through the AI application.
For deployment choices, health checks, and basic observability entrypoints, see [Deployment](../deployment/).
If the AI application cannot connect or cannot see the logical database, see [Troubleshooting](../troubleshooting/).
