+++
title = "配置说明"
weight = 2
+++

ShardingSphere-MCP 使用 YAML 文件配置 transport 和运行时逻辑库。
发行包默认读取 `conf/mcp-http.yaml`，也内置 `conf/mcp-stdio.yaml`。

## Transport 配置

每个 MCP runtime 进程必须且只能选择一种 transport。

HTTP 示例：

```yaml
transport:
  type: STREAMABLE_HTTP
  http:
    bindHost: 127.0.0.1
    port: 18088
    endpointPath: /mcp
```

STDIO 示例：

```yaml
transport:
  type: STDIO
```

说明：

- `transport.type` 支持 `STREAMABLE_HTTP` 和 `STDIO`。
- `transport.http` 只在 `transport.type` 为 `STREAMABLE_HTTP` 时合法。
- HTTP 模式下 `transport.http` 可以省略，默认 `bindHost`、`port`、`endpointPath` 分别是 `127.0.0.1`、`18088`、`/mcp`。
- `127.0.0.1`、`localhost`、`::1` 只面向本机。
- `0.0.0.0` 或指定内网 IP 会面向对应网络接口。

## Runtime Databases

`runtimeDatabases` 定义 MCP runtime 可见的 ShardingSphere 逻辑库。
每个条目的 key 是 MCP 对外暴露的逻辑库名称。

```yaml
runtimeDatabases:
  logic_db:
    databaseType: MySQL
    jdbcUrl: "jdbc:mysql://127.0.0.1:3307/logic_db"
    username: "root"
    password: "root"
    driverClassName: "com.mysql.cj.jdbc.Driver"
```

字段说明：

- `databaseType`：必填，声明数据库类型，例如 `MySQL`、`PostgreSQL`、`H2`。
- `jdbcUrl`：必填，MCP runtime 连接逻辑库的 JDBC URL。
- `username`：必填字段；无用户名时写空字符串 `""`。
- `password`：必填字段；无密码时写空字符串 `""`。
- `driverClassName`：必填字段；如果 JDBC 4 驱动可自动注册且不需要显式覆盖，写空字符串 `""`。

注意事项：

- MCP resources 暴露的是 ShardingSphere 逻辑库，不是底层物理存储单元。
- 加密和脱敏插件的 workflow 应连接 ShardingSphere-Proxy 暴露的逻辑库。
- Schema、table、view、index 和 sequence metadata 依赖目标 JDBC metadata。
- 非 H2 数据库通常需要把目标 JDBC driver jar 放入 `plugins/`。

## 插件目录

发行包默认把官方 MCP 基线 jar 放入 `lib/`，包括 Encrypt 和 Mask feature。
如果目标数据库驱动或额外 MCP feature jar 没有随发行包提供，请放入发行包根目录下的 `plugins/`，再启动 runtime。

## 自定义配置文件

Unix-like 系统：

```bash
bin/start.sh /path/to/mcp-http.yaml
```

Windows：

```bat
bin\start.bat path\to\mcp-http.yaml
```

Docker 中可以通过 `SHARDINGSPHERE_MCP_CONFIG` 指定容器内的配置文件路径。

## JVM 参数

Unix-like 系统：

```bash
JAVA_OPTS="-Xms256m -Xmx256m" bin/start.sh
```

Windows：

```bat
set "JAVA_OPTS=-Xms256m -Xmx256m" && bin\start.bat
```
