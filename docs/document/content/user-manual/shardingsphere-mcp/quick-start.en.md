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
  "<logic-database>":
    databaseType: MySQL
    jdbcUrl: "jdbc:mysql://<proxy-host>:<proxy-port>/<logic-database>"
    username: "<proxy-username>"
    password: "<proxy-password>"
    driverClassName: "com.mysql.cj.jdbc.Driver"
```

Replace `<logic-database>`, `<proxy-host>`, `<proxy-port>`, `<proxy-username>`, and `<proxy-password>` with the actual ShardingSphere-Proxy connection information.
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

Configure the HTTP MCP Server address in the AI application, IDE extension, or agent platform:

```json
{
  "mcpServers": {
    "shardingsphere": {
      "url": "http://127.0.0.1:18088/mcp"
    }
  }
}
```

Configuration file locations and field names may differ between AI applications. Follow the documentation of the client you use.
For more HTTP and STDIO options, see [Client Integration](../client-integration/).

## Verify through Natural Language

After configuration, enter the following tasks in the AI application to verify that ShardingSphere-MCP can access the target logical database:

- "Show tables in `<logic-database>`."
- "Show columns and indexes for `<table-name>`."
- "Query the first 10 rows from `<table-name>`."

If the application returns the logical database, table structure, or query results, the MCP Server can access the target ShardingSphere-Proxy logical database through the AI application.
If the AI application cannot connect or cannot see the logical database, see [Troubleshooting](../troubleshooting/).
