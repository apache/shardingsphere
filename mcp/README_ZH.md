# ShardingSphere MCP

ShardingSphere MCP 为 Apache ShardingSphere 提供独立运行的 Model Context Protocol runtime。
这份文档按第一次上手成功为目标编排：先构建发行包，再启动内置的 demo runtime，初始化一个会话，并通过 HTTP 验证 discovery 和 query 行为。
其他说明会补充如何把 demo runtime 替换成真实 JDBC 部署。

## Quick Start

### 前置条件

- `JAVA_HOME` 或 `PATH` 中可用的 JDK 17
- 仓库根目录下的 Maven Wrapper
- 包含 `curl`、`find`、`mktemp`、`sed`、`tr` 的类 Unix Shell

### 1. 构建独立发行包

```bash
./mvnw -pl distribution/mcp -am -DskipTests package
```

解析发行包目录：

```bash
DIST_DIR=$(find distribution/mcp/target -maxdepth 1 -type d -name 'apache-shardingsphere-mcp-*' | sed -n '1p')
echo "${DIST_DIR}"
```

预期结果：

- 命令会打印出一个非空的发行包目录。
- 该目录下包含 `bin/`、`conf/` 和 `lib/`。

### 2. 启动 MCP runtime

```bash
cd "${DIST_DIR}"
bin/start.sh
```

说明：

- `bin/start.sh` 会以前台方式运行。请保持这个终端不退出，并在第二个终端执行下面的 `curl` 命令。
- 发行包运行时会读取 `conf/mcp.yaml` 和 `conf/logback.xml`。
- 启用 HTTP 时，默认端点是 `http://127.0.0.1:18088/mcp`。
- 日志会写到 `logs/` 目录。
- `conf/mcp.yaml` 现在是严格 schema：`transport.http.enabled`、`transport.http.bindHost`、`transport.http.port`、`transport.http.endpointPath`、`transport.stdio.enabled`，以及每个 runtime database 的全部字段都必须显式声明。
- 每个进程必须且只能启用一种 transport。发行包内置示例配置默认只启用 HTTP。
- `bin/start.sh` 启动前会校验配置文件、运行时依赖和 Java 环境，并自动创建 `data/`、`logs/`、`ext-lib/` 目录，然后切到发行包根目录启动，确保相对路径可用。
- 如果启动成功，进程会保持前台运行；如果立刻退出，优先查看终端报错和 `logs/mcp.log`。
- 内置 demo runtime 会暴露两个逻辑库 `orders` 和 `billing`，底层使用发行包自带的 H2 驱动以及 `data/` 下的种子数据。

发行包内置示例配置如下：

```yaml
transport:
  http:
    enabled: true
    bindHost: 127.0.0.1
    port: 18088
    endpointPath: /mcp
  stdio:
    enabled: false

runtimeDatabases:
  orders:
    databaseType: H2
    jdbcUrl: "jdbc:h2:file:./data/mcp-demo-orders;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'conf/demo-h2.sql'"
    username: ""
    password: ""
    driverClassName: org.h2.Driver
  billing:
    databaseType: H2
    jdbcUrl: "jdbc:h2:file:./data/mcp-demo-billing;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'conf/demo-h2.sql'"
    username: ""
    password: ""
    driverClassName: org.h2.Driver
```

### 3. 初始化一个 MCP 会话

在第二个终端执行：

```bash
INIT_HEADERS=$(mktemp)
curl -sS -D "${INIT_HEADERS}" -o /dev/null http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  --data '{"jsonrpc":"2.0","id":"init-1","method":"initialize","params":{"capabilities":{},"clientInfo":{"name":"curl-demo","version":"1.0.0"}}}'
SESSION_ID=$(sed -n 's/^[Mm][Cc][Pp]-[Ss]ession-[Ii][Dd]: //p' "${INIT_HEADERS}" | tr -d '\r')
PROTOCOL_VERSION=$(sed -n 's/^[Mm][Cc][Pp]-[Pp]rotocol-[Vv]ersion: //p' "${INIT_HEADERS}" | tr -d '\r')
rm -f "${INIT_HEADERS}"
printf 'SESSION_ID=%s\nPROTOCOL_VERSION=%s\n' "${SESSION_ID}" "${PROTOCOL_VERSION}"
```

预期结果：

- 命令会打印出一个非空的会话 ID，以及一个非空的协议版本。
- initialize 响应会协商协议版本，并通过 `MCP-Protocol-Version` 响应头返回。

### 4. 验证 discovery 和 query

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"tool-1","method":"tools/call","params":{"name":"list_tables","arguments":{"database":"orders","schema":"public"}}}'
```

预期结果：

- 响应类型是 `text/event-stream`。
- JSON 负载位于 `data:` 行，其中会包含 `orders`、`order_items`、`active_orders`。

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"tool-2","method":"tools/call","params":{"name":"execute_query","arguments":{"database":"orders","schema":"public","sql":"SELECT status FROM orders ORDER BY order_id","max_rows":10}}}'
```

预期结果：

- 响应类型是 `text/event-stream`。
- JSON 负载位于 `data:` 行，其中 `result_kind` 为 `result_set`。

完成后，可使用下方的 DELETE 示例关闭会话。

## 附加 HTTP 验证

### 读取 `shardingsphere://capabilities`

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"resource-1","method":"resources/read","params":{"uri":"shardingsphere://capabilities"}}'
```

预期结果：

- 响应类型是 `text/event-stream`。
- `data:` 行中会包含 `shardingsphere://capabilities` 对应的 resource 内容。

### 可选：打开 SSE 流

只有在你想直接观察长连接事件流时才需要这一步：

```bash
curl -N http://127.0.0.1:18088/mcp \
  -H 'Accept: text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}"
```

说明：

- 这个命令会一直阻塞，直到你用 `Ctrl+C` 手动停止。
- 建议单独开一个终端观察，不要和其他 follow-up 请求混用。

### 关闭会话

```bash
curl -sS -D - -o /dev/null \
  -X DELETE http://127.0.0.1:18088/mcp \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}"
```

预期结果：

- 响应状态码是 `200`。

## 使用 STDIO

STDIO 现在实现为真实的 MCP stdio transport，适用于由本地 MCP client 拉起 ShardingSphere MCP 子进程的场景。
只有在客户端会通过进程 `stdin` / `stdout` 传输 MCP 协议消息时，才应启用 STDIO。

### 只启用 STDIO 启动

如果你想在关闭 HTTP 的情况下验证发行包，可以创建一个专用配置文件，例如 `conf/mcp-stdio.yaml`：

```yaml
transport:
  http:
    enabled: false
    bindHost: 127.0.0.1
    port: 18088
    endpointPath: /mcp
  stdio:
    enabled: true
```

然后使用这个配置文件启动：

```bash
bin/start.sh conf/mcp-stdio.yaml
```

说明：

- 进程仍然会以前台方式运行。
- 如果 `transport.http.enabled` 和 `transport.stdio.enabled` 同时为 `false`，启动会因 "Exactly one transport must be explicitly enabled. Set either `transport.http.enabled` or `transport.stdio.enabled` to true." 失败。
- 如果两个 transport 同时启用，启动会因 "HTTP and STDIO transports cannot be enabled at the same time. Choose exactly one transport." 失败。
- 默认 `conf/logback.xml` 会把控制台日志写到 stderr，并把文件日志写到 `logs/mcp.log`，这样 stdout 可以专门用于 MCP 协议消息。
- STDIO 模式面向 MCP client，不是给人工手输请求的交互式 Shell。推荐在 MCP client 配置里把它作为子进程启动。

参考：

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/stdio/StdioTransportMCPServer.java`
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/stdio/StdioTransportIntegrationTest.java`

## Runtime 说明

- 发行包里的 `conf/mcp.yaml` 现在默认内置一段 demo 多数据库 JDBC `runtimeDatabases` 配置，所以第一次启动就能验证逻辑库发现和真实 query 执行。
- 如果要接真实部署，请把 `runtimeDatabases` 段替换成你自己的逻辑库映射和 JDBC 连接属性。每个逻辑库条目都需要显式声明所需的 runtime 字段；schema 发现改为依赖 JDBC metadata，legacy `runtime.*` alias 已不再支持。
- 对支持 JDBC 4 自动注册的驱动，`driverClassName` 可以不写；只有目标驱动需要显式覆盖时再配置。
- 如果目标数据库的驱动没有随发行包提供，请先把对应 jar 放到 `ext-lib/`，再执行 `bin/start.sh`。
- 每个 runtime 进程必须且只能启用一种 transport。
- 如果只需要本地 HTTP 调试，保留 `transport.http.enabled: true`，并把 `transport.stdio.enabled` 设为 `false`。
- 如果要给本地 MCP client 走 stdio，保留 `transport.http.enabled: false`，并把 `transport.stdio.enabled` 设为 `true`。
- 如果 HTTP 端点要暴露到 localhost 之外，建议放在受信网络、上游网关或反向代理之后。
- 如果要使用自定义配置文件启动，可以执行 `bin/start.sh /path/to/mcp.yaml`。
- 如果要调整 JVM 参数，可以使用 `JAVA_OPTS`，例如 `JAVA_OPTS="-Xms256m -Xmx256m" bin/start.sh`。

## 开发参考

- `mcp/core`：capability、metadata、session、audit、execute-query 契约、runtime service 聚合，以及 JDBC runtime 配置模型、metadata 发现、`DatabaseRuntime` 装配与 JDBC-backed runtime context factory
- `mcp/bootstrap`：基于 MCP Java SDK 的 bootstrap、HTTP / STDIO transport、顶层配置加载与生命周期管理
- `distribution/mcp`：独立打包、启动脚本、配置、Dockerfile
- `test/e2e/mcp`：端到端契约验证

如果要做本地调试或更完整的语义验证，优先参考 `mcp/bootstrap` 下的集成测试和 `test/e2e/mcp` 下的 E2E 用例。
