# ShardingSphere MCP

ShardingSphere MCP 为 Apache ShardingSphere 提供独立运行的 Model Context Protocol runtime。
这份文档按第一次上手成功为目标编排：先构建发行包，再启动内置的 demo runtime，初始化一个会话，并通过 HTTP 验证 discovery 和 query 行为。
其他说明会补充如何把 demo runtime 替换成真实 JDBC 部署。

## Quick Start

### 前置条件

- `JAVA_HOME` 或 `PATH` 中可用的 JDK 21
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

```bat
cd /d "%DIST_DIR%"
bin\start.bat
```

说明：

- `bin/start.sh` 和 `bin\start.bat` 都会以前台方式运行。请保持这个终端不退出，并在第二个终端执行下面的 `curl` 命令。
- 发行包运行时默认使用 `conf/mcp-http.yaml`，同时内置 `conf/mcp-stdio.yaml`，并读取 `conf/logback.xml` 作为日志配置。
- 启用 HTTP 时，默认端点是 `http://127.0.0.1:18088/mcp`。
- 日志会写到 `logs/` 目录。
- `conf/mcp-http.yaml` 采用严格 schema：`transport.type`、`transport.http.bindHost`、`transport.http.port`、
  `transport.http.endpointPath`，以及每个 runtime database 的全部字段都只能使用受支持字段名。
- MCP YAML 值均为显式配置。JDBC 凭证等部署相关敏感配置应写入受保护的自定义配置文件，再通过 `SHARDINGSPHERE_MCP_CONFIG` 或启动脚本参数选择该文件。
- 每个进程必须且只能通过 `transport.type` 选择一种 transport。发行包内置示例配置默认选择 Streamable HTTP。
- `bin/start.sh` 和 `bin\start.bat` 启动前都会校验配置文件、运行时依赖和 Java 环境，并自动创建 `data/`、`logs/`、`plugins/` 目录，然后切到发行包根目录启动，确保相对路径可用。
- 如果启动成功，进程会保持前台运行；如果立刻退出，优先查看终端报错和 `logs/mcp.log`。
- 内置 demo runtime 会暴露两个逻辑库 `orders` 和 `billing`，底层使用发行包自带的 H2 驱动以及 `data/` 下的种子数据。

发行包内置示例配置如下：

```yaml
transport:
  type: STREAMABLE_HTTP

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
  --data '{"jsonrpc":"2.0","id":"resource-1","method":"resources/read","params":{"uri":"shardingsphere://databases/orders/schemas/public/tables"}}'
```

预期结果：

- 响应类型是 `text/event-stream`。
- JSON 负载位于 `data:` 行，其中会包含 `orders`、`order_items`、`active_orders`。

说明：

- metadata 的 list / detail / capability discovery 统一走 `resources/read`。
- 当前 public tools 包括 `database_gateway_search_metadata`、`database_gateway_execute_query`、`database_gateway_execute_update`、`database_gateway_plan_encrypt_rule`、`database_gateway_plan_mask_rule`、`database_gateway_apply_workflow` 和 `database_gateway_validate_workflow`。
- `database_gateway_execute_query` 只接受分类器批准的 `SELECT` 和 `EXPLAIN ANALYZE`，并拒绝已知有副作用的查询形态；
  DML、DDL、DCL、事务控制、savepoint 以及其他支持的有副作用 SQL 要使用 `database_gateway_execute_update`。
- SQL 执行分类会识别限定名和带引号对象名，但用途仅限安全分类与跨 schema 防护；
  这不代表 workflow 规划输入支持这些写法，也不代表 MCP 内置通用 SQL 表达式解析器。
- `database_gateway_execute_query.max_rows` 省略或传 `0` 时使用服务端默认值 `100`；显式传 `1` 到 `5000` 用于限制返回行数。
- 加密与脱敏 workflow 面向由 ShardingSphere-Proxy 暴露的逻辑库；下文会单独说明这部分的前置条件和使用方式。
- `database_gateway_search_metadata.object_types` 只接受 `database`、`schema`、`table`、`view`、`column`、`index`、`sequence`。

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"tool-1","method":"tools/call","params":{"name":"database_gateway_search_metadata","arguments":{"database":"orders","query":"order","object_types":["table","view"]}}}'
```

预期结果：

- 响应类型是 `text/event-stream`。
- JSON 负载位于 `data:` 行，其中会包含命中的 `orders`、`order_items` 或 `active_orders`。

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"tool-2","method":"tools/call","params":{"name":"database_gateway_execute_query","arguments":{"database":"orders","schema":"public","sql":"SELECT status FROM orders ORDER BY order_id","max_rows":10}}}'
```

预期结果：

- 响应类型是 `text/event-stream`。
- JSON 负载位于 `data:` 行，其中 `result_kind` 为 `result_set`。
- 同一负载还会包含 `statement_class = query`、`statement_type = SELECT`，以及 `columns`、`rows`。

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
- 该 payload 由 descriptor catalog 生成，会包含 `resources`、`resourceTemplates`、`tools`、`prompts`、`completionTargets`、`resourceNavigation`、
  `protocolAvailability` 和确定性的 `fingerprints`。

### 基于 Descriptor 的 Discovery

MCP tools、resources、prompts 与 completions 的模型可见元数据来自 `META-INF/shardingsphere-mcp/mcp-descriptors` 下的 YAML descriptors。
`resources/list`、`resources/templates/list`、`tools/list`、`prompts/list`、`completion/complete` 和聚合资源 `shardingsphere://capabilities` 都使用同一份 descriptor 来源。
ShardingSphere 专用 resource 语义通过 `shardingSphereMetadata` 编写，在运行时保留 typed metadata，并通过 resource 与 resource template 的 `_meta` 暴露给 MCP client。

Descriptor 必须说明模型该如何使用这个 surface，而不是只重复 URI 或 tool 名：

- Resource descriptor 包含 URI template 参数含义、逻辑对象与物理对象范围、MIME type、title、description、annotations 和关系元数据。
- Tool descriptor 包含 input field 描述、已知 object key 的结构化 schema、output schema、MCP annotations、相关 resources、后续 tools 和副作用说明。
- Prompt descriptor 暴露元数据检查、安全 SQL 执行、加密规划、脱敏规划和 workflow 恢复等任务指南。
- Completion target 为运行时名称、算法和当前 session 内 workflow plan_id 提供 descriptor 驱动的补全建议。
- Completion 响应会返回缺失上下文、候选数量和确定性排序原因等诊断信息。
- `resourceNavigation` 说明轻量级的公开下一跳，例如 database 到 schema、table 到 column、algorithm 到规划工具，
  以及 workflow plan 到 apply 或 validation 工具。
- `shardingsphere://runtime` 暴露轻量运行时状态，`shardingsphere://workflows/{plan_id}` 支持按 plan_id 回读 workflow plan。
- `fingerprints` 记录 descriptor、prompt、navigation 和模型可见 schema 的确定性哈希，让测试产物能证明模型使用的是哪一版 MCP surface。
- item-list 响应总会包含 `items` 和 `count`。大结果 payload 在需要收窄后续查询时使用 typed `truncated`、`total_count`、`returned_count` 和 `large_result_guidance` 字段。
  resource read 还会包含 `self_uri`，并在适用时包含 typed `parent_resource` 或 typed `next_resources`。
- Workflow tool 响应包含 `missing_required_inputs`、`clarification_questions`、`resources_to_read` 和 `next_actions`，
  让模型可以按结构化提示继续补问、预览、执行或校验。
- 可恢复错误 payload 保留 `message`，并为缺失参数、不支持的 tool/resource、非法枚举、workflow 状态错误以及 SQL tool 选错场景增加 `recovery` 提示。
  JSON-RPC 数字错误码是 MCP 协议错误契约。

Descriptor annotations 遵循 MCP `2025-11-25` schema，并且属于开发者维护的协议 surface 元数据，不是终端用户运行时配置：

- Resource annotations 是可选的。需要时只能使用 `audience`、`priority` 和 `lastModified`；没有字段需要表达时应省略整个 `annotations` map。
- Resource `audience` 只能使用 MCP role `user` 或 `assistant`；`priority` 必须是有限数值并处于 `0.0` 到 `1.0`；`lastModified` 必须包含 ISO 8601 UTC 标记或 offset。
- Tool annotations 使用 MCP `ToolAnnotations`。MCP 的有效默认值是 `readOnlyHint=false`、`destructiveHint=true`、`idempotentHint=false` 和 `openWorldHint=true`。
- ShardingSphere public tool descriptor 仍然必须在 YAML 中显式声明这四个 boolean hints，这样 primitive defaults 生效前，reviewer 能直接看到安全决策。
- Tool annotations 只作为客户端提示，不替代运行时校验、SQL 安全检查、用户审批或服务端授权。
- MCP `icons` 和 `Tool.execution` 是官方 `2025-11-25` descriptor 字段，但 MCP Java SDK `1.1.2` 尚未在 `Resource`、`ResourceTemplate` 或 `Tool` 中暴露 `icons`，也尚未在 `Tool` 中暴露 `execution`；
  在 SDK 边界支持前，它们属于后续范围。

### 读取 `shardingsphere://databases/orders/schemas/public/sequences`

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"resource-2","method":"resources/read","params":{"uri":"shardingsphere://databases/orders/schemas/public/sequences"}}'
```

预期结果：

- 响应类型是 `text/event-stream`。
- 当目标数据库声明支持 `SEQUENCE` 时，`data:` 行中会包含 `order_seq` 之类的 sequence metadata。

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

发行包现在已经内置了 `conf/mcp-stdio.yaml`，可以直接使用这个配置启动：

```bash
bin/start.sh conf/mcp-stdio.yaml
```

```bat
bin\start.bat conf\mcp-stdio.yaml
```

说明：

- 进程仍然会以前台方式运行。
- `transport.type` 必须是 `STREAMABLE_HTTP` 或 `STDIO`。
- `transport.http` 只在 `transport.type` 为 `STREAMABLE_HTTP` 时合法；`STDIO` 模式应省略它。
- 默认 `conf/logback.xml` 会把控制台日志写到 stderr，并把文件日志写到 `logs/mcp.log`，这样 stdout 可以专门用于 MCP 协议消息。
- STDIO 模式面向 MCP client，不是给人工手输请求的交互式 Shell。推荐在 MCP client 配置里把它作为子进程启动。

参考：

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/stdio/StdioMCPServer.java`
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/stdio/StdioTransportIntegrationTest.java`

## Client 配置与排障

- 通用 STDIO client 配置形态：

  ```json
  {
    "mcpServers": {
      "shardingsphere": {
        "command": "/path/to/apache-shardingsphere-mcp/bin/start.sh",
        "args": ["conf/mcp-stdio.yaml"]
      }
    }
  }
  ```

- 通用 HTTP client 配置形态：

  ```json
  {
    "mcpServers": {
      "shardingsphere-http": {
        "url": "http://127.0.0.1:18088/mcp"
      }
    }
  }
  ```

- 启动时会通过已配置的 logger 输出一条简短诊断日志：配置文件路径、日志路径、runtime database 数量、当前 transport 和 endpoint。
- Client 应先使用官方 MCP discovery 方法（`tools/list`、`resources/list`、`resources/templates/list`、`prompts/list`、
  `completion/complete`），再按需读取 `shardingsphere://capabilities` 作为领域目录。
- 当前内置 HTTP runtime 不提供授权。远程部署应放在受信网络、反向代理或网关后面，由外层处理认证和授权。
- 如果启动提示 runtime database 缺失或数量为 0，修正 `runtimeDatabases`；MCP resources 暴露的是 ShardingSphere 逻辑库，
  不是物理存储单元。
- 如果 `shardingsphere://runtime` 返回 `server_status=configuration_required`，先配置至少一个 `runtimeDatabases` 条目，再做 metadata discovery 或 SQL execution。
- 如果 JDBC metadata 或 SQL 执行因为驱动失败，请把目标 JDBC driver jar 放入 `plugins/`，或加入嵌入式运行时 classpath。
- STDIO 模式下 stdout 只用于 MCP 协议帧，诊断信息应写到 stderr 或 `logs/mcp.log`。
- Workflow tools 规划的是 ShardingSphere 逻辑规则变更；apply 前先 preview，并确认副作用仍然需要执行。

## Runtime 说明

- 发行包里的 `conf/mcp-http.yaml` 现在默认内置一段 demo 多数据库 JDBC `runtimeDatabases` 配置，所以第一次启动就能验证逻辑库发现和真实 query 执行。
- 如果要接真实部署，请把 `runtimeDatabases` 段替换成你自己的逻辑库映射和 JDBC 连接属性。每个逻辑库条目都需要显式声明所需的 runtime 字段；schema 发现改为依赖 JDBC metadata，`runtime.*` alias 已不再支持。
- 对支持 JDBC 4 自动注册的驱动，`driverClassName` 可以不写；只有目标驱动需要显式覆盖时再配置。
- 发行包默认把 MCP 官方基线 jar，包括 encrypt 和 mask feature，放在 `lib/` 下。
- 如果目标数据库驱动或额外的 MCP feature jar 没有随发行包提供，请先把对应 jar 放到 `plugins/`，再执行 `bin/start.sh` 或 `bin\start.bat`。
- 如果你不是用发行包，而是直接嵌入 `shardingsphere-mcp-bootstrap`，那就需要把所需 feature jar 显式加入运行时 classpath。
- 每个 runtime 进程必须且只能通过 `transport.type` 选择一种 transport。
- 如果只需要本地 HTTP 调试，设置 `transport.type: STREAMABLE_HTTP`。
- 如果要给本地 MCP client 走 stdio，设置 `transport.type: STDIO`，并省略 `transport.http`。
- Streamable HTTP 模式下 `transport.http` 可以省略。缺省 `bindHost`、`port`、`endpointPath` 分别是 `127.0.0.1`、`18088` 和 `/mcp`。
- `transport.http.bindHost` 表示 HTTP 服务监听在哪个地址上：`127.0.0.1`、`localhost`、`::1` 只面向本机；`0.0.0.0` 或指定内网 IP 会面向对应网络接口。
- loopback HTTP 绑定下，如果请求带 `Origin`，它也必须是 loopback；格式非法或远程 origin 会返回 `403`。
- 非 loopback HTTP 绑定下，缺失 `Origin` 的非浏览器请求会被接受，但任何显式 `Origin` 都会返回 `403`。
- 内置 HTTP runtime 当前不带授权。对外暴露场景应放在受信网络、上游网关或反向代理后面。
- 如果要使用自定义配置文件启动，Unix-like 系统可以执行 `bin/start.sh /path/to/mcp-http.yaml`，Windows 可以执行 `bin\start.bat path\to\mcp-http.yaml`。
- 如果要调整 JVM 参数，可以使用 `JAVA_OPTS`，例如 Unix-like 系统执行 `JAVA_OPTS="-Xms256m -Xmx256m" bin/start.sh`，Windows 执行 `set "JAVA_OPTS=-Xms256m -Xmx256m" && bin\start.bat`。

## Feature SPI 结构

当前 MCP 子链路按 `api + support + features + core + bootstrap` 分层组织：

- `mcp/api`
  - 定义 public tool / resource handler 契约、共享 descriptor、协议 response 与 MCP 协议异常
- `mcp/support`
  - 为 MCP core 与可插拔 feature 提供 database metadata、execution、capability、workflow context、model、facade、SPI 与复用 helper
- `mcp/features/encrypt`
  - 提供 encrypt MCP tools、resources 与 workflow 实现
- `mcp/features/mask`
  - 提供 mask MCP tools、resources 与 workflow 实现
- `mcp/core`
  - 提供 capability、metadata、session、execute-query 与通用 runtime 能力
- `mcp/bootstrap`
  - 通过 MCP Java SDK 汇总各 feature 贡献的能力，并暴露 HTTP / STDIO transport

encrypt 和 mask 在 MCP 里不是 bootstrap 内硬编码的特殊功能，而是通过 MCP handler provider SPI 贡献到 MCP runtime 的可插拔 feature。

## 如何新增一个 MCP Feature

如果你要在现有 encrypt / mask 之外再新增一个 feature，建议保持下面这条最小路径：

- 在 `mcp/features/<feature>` 新建模块，依赖 `mcp/api`，database metadata、execution 或 workflow handler 依赖 `mcp/support`，只有需要 service 级 handler context 时才依赖 `mcp/core`，再加必要的领域模块；不要依赖 `mcp/bootstrap`
- 如果是新增 feature 模块，还要把它接入构建与运行时 classpath：先加入 `mcp/features/pom.xml`，然后按目标形态接入运行时
- 如果要作为官方默认能力随发行包提供，就把它加入 `distribution/mcp/pom.xml`
- 如果要保持可选插件，就在构建完成后把对应 jar 放到 `plugins/`，再启动 runtime
- 对外新增 tool 时，实现 `MCPToolHandler<T extends MCPHandlerContext>` 并声明所需 context type 与 canonical tool name，同时在 `META-INF/shardingsphere-mcp/mcp-descriptors` 下添加 descriptor
- 对外新增 resource 时，实现 `MCPResourceHandler<T extends MCPHandlerContext>` 并声明所需 context type 与 canonical URI template，同时在 `META-INF/shardingsphere-mcp/mcp-descriptors` 下添加 descriptor
- 运行时代码需要 handler 对应 descriptor 时，使用 canonical tool name 或 resource URI template 通过 `MCPDescriptorCatalogIndex` 从 catalog 解析；
  不要在 handler 内重复维护 descriptor 字段
- service 级 handler 使用 `MCPServiceHandlerContext`，database metadata 或 execution handler 使用 `MCPDatabaseHandlerContext`，workflow handler 使用 `MCPWorkflowHandlerContext`
- 实现一个 `MCPHandlerProvider`，通过 `getToolHandlers()` 和 `getResourceHandlers()` 返回该 feature 自己暴露的 handlers
- 如果 feature 拥有 workflow definitions，就在同一个 provider 上实现 `MCPWorkflowDefinitionProvider`
- 在 `src/main/resources/META-INF/services/` 下注册 `org.apache.shardingsphere.mcp.api.MCPHandlerProvider`
- feature URI 统一使用 `shardingsphere://features/<feature>/...` 命名空间，避免和公共 metadata path 混用
- `mcp/core` 会通过 `ShardingSphereServiceLoader` 自动发现 handler provider，展开并校验其中的 handlers，`mcp/bootstrap` 只负责把最终结果发布到协议层
- tool 名和 resource URI pattern 必须在全局范围内保持唯一；重复 handler 与重复 descriptor 都会在启动期校验时被拒绝

encrypt 和 mask 模块本身就是最直接的参考实现。

## 基于 ShardingSphere-Proxy 的加密与脱敏 Workflow

这组 workflow tool 让 MCP client 可以通过自然语言或结构化参数，对某个逻辑表的某一列规划、执行并校验加密或脱敏规则。
它的目标不是自己实现加密，而是帮助大模型把用户意图转换成 ShardingSphere-Proxy 可执行的 DDL、DistSQL 与校验动作。
这些 tools 与 resources 由 encrypt / mask feature 通过 SPI 注册进入 MCP，bootstrap 只负责汇总并发布到协议层。
如果只需要面向本次评分闭环的精简流程，请看 `.specify/specs/020-mcp-encrypt-mask-scoped-100/quickstart.md`。

### 前置条件

- 当前 V1 只支持 `ShardingSphere-Proxy`。
- MCP runtime 应通过 JDBC 连接到 `ShardingSphere-Proxy` 的逻辑库，而不是直接连接底层存储库。
- Quick Start 中的 demo H2 runtime 适合验证 metadata discovery 和 query；如果要使用加密与脱敏 workflow，请把 `runtimeDatabases` 改成指向 Proxy 的逻辑库。
- 当前 workflow 只处理规则、元数据和 SQL 可执行性校验，不处理存量数据迁移或回填。

### 相关 tools 与 resources

加密 feature 对外暴露 1 个 planning tool：

- `database_gateway_plan_encrypt_rule`
  - 识别加密意图，补全缺失参数，生成派生列方案、DDL、DistSQL、索引计划和校验策略。

脱敏 feature 对外暴露 1 个 planning tool：

- `database_gateway_plan_mask_rule`
  - 识别脱敏意图，补全缺失参数，生成 DistSQL 和校验策略。

workflow runtime 额外提供 2 个 encrypt / mask 共用的 generic tools：

- `database_gateway_apply_workflow`
  - 按当前 `plan_id` 执行生成的 artifacts，或在 `manual-only` 模式下导出人工执行包。
- `database_gateway_validate_workflow`
  - 按 workflow 自身的校验层级检查当前 plan 的最终结果。

Workflow 同时补充了以下 feature resources：

- `shardingsphere://features/encrypt/algorithms`
- `shardingsphere://features/encrypt/databases/{database}/rules`
- `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules`
- `shardingsphere://features/mask/algorithms`
- `shardingsphere://features/mask/databases/{database}/rules`
- `shardingsphere://features/mask/databases/{database}/tables/{table}/rules`

其中 `features/*/algorithms` 会展示当前 Proxy 可见的算法插件，方便大模型基于实际运行时集合做算法推荐。

### 使用前先记住这几条

- 整个 workflow 必须复用同一个 `MCP-Session-Id`。`plan`、`apply`、`validate` 如果切换到别的 session，后续调用会因为 plan 归属不一致而失败。
- 第一次调用 `database_gateway_plan_encrypt_rule` 或 `database_gateway_plan_mask_rule` 时不需要 `plan_id`；只要拿到 `plan_id`，后续所有补问、继续规划、执行和校验都继续使用这个 `plan_id`，但只在当前 feature 的 workflow 内复用。
- `database_gateway_plan_encrypt_rule` 和 `database_gateway_plan_mask_rule` 只负责规划，不会执行任何 DDL 或 DistSQL；真正执行统一通过 `database_gateway_apply_workflow`。
- `database_gateway_validate_workflow` 只做校验，不会修改规则，也不会补执行遗漏步骤。
- 用户始终面向逻辑库、逻辑表、逻辑列发起请求。对加密场景，MCP 可能会自动规划并创建物理派生列，但对用户暴露的目标仍然是 Proxy 里的逻辑对象。
- `schema` 是可选的；如果 Proxy 逻辑库下只有一个 schema，MCP 会自动补齐；如果无法唯一定位，MCP 会明确返回 `请明确 schema。`。
- `delivery_mode` 只影响客户端如何组织对话和展示步骤，不影响最终生成的 artifacts；真正影响执行行为的是 `execution_mode`。
- 敏感算法属性在 `plan` 和 `apply` 的返回中都只会以脱敏形式展示，不会明文回显。
- MCP form elicitation 只用于 STDIO 下的非敏感补问。
  Streamable HTTP 会返回结构化 fallback，因为当前版本还没有把表单回复强绑定到远程用户身份。
  当前版本不实现 URL mode；如果客户端不能自动续跑，响应会明确带上 `elicitation_support` 和 `fallback_reason`。
  如果补问带有 `secret: true`、`input_type: "secret"`，或者字段名包含 password、token、key、secret、credential，
  请保留返回的 `plan_id`，通过 secret manager、受保护环境变量或运维控制通道取得该值，再用同一个 planner 继续。
- 下面所有 `curl` 示例都默认你已经完成了 MCP `initialize`，并持有同一个 `SESSION_ID` 和对应的 `PROTOCOL_VERSION`。

### 推荐操作顺序

如果你想把一次加密或脱敏 workflow 稳定跑通，直接按下面顺序调用即可：

1. 先调用对应 feature 的 planner：加密用 `database_gateway_plan_encrypt_rule`，脱敏用 `database_gateway_plan_mask_rule`，不要直接从 `apply` 开始。
2. 如果返回 `status = clarifying`，读取 `clarification_questions`，按其中的 `field` 补齐非敏感参数；
   敏感参数必须通过上面的安全通道取得，然后带上同一个 `plan_id` 再次调用同一个 `plan_*_rule`。
3. 如果返回 `status = planned`，重点 review `derived_column_plan`、`ddl_artifacts`、`distsql_artifacts`、`index_plan`。
4. 调用 `database_gateway_apply_workflow` 并显式设置 `execution_mode=preview`，先查看 artifacts 和副作用范围，不改变运行时状态。
5. 确认预览结果后，再用 `execution_mode=review-then-execute` 调用 `database_gateway_apply_workflow`，或者用 `manual-only` 导出人工执行包。
6. 如果 `apply` 返回 `awaiting-manual-execution`，先把 `manual_artifact_package` 里的 SQL / DistSQL 在 Proxy 上手工执行完，再进入下一步。
7. 调用 `database_gateway_validate_workflow`，确认返回中的校验层级都通过。
8. 如果 `validate` 失败，优先看 `issues` 和 `mismatches`，修复后再继续补执行或重新规划。

### 整体交互方式

`database_gateway_plan_encrypt_rule` 和 `database_gateway_plan_mask_rule` 每次都会返回全局步骤列表，并告诉客户端当前走到哪一步。默认步骤如下：

1. 确认 database、table、column 和目标生命周期
2. 检查现有规则、插件和逻辑元数据
3. 澄清缺失需求并推荐算法
4. 收集算法属性并生成派生列命名方案
5. 生成 DDL、DistSQL 和索引 artifacts
6. Review artifacts 并选择执行模式
7. 执行或导出 artifacts
8. 校验并汇总结果

常见状态包括：

- `clarifying`
  - 需要继续补充信息，例如 logical database、算法类型或密钥属性。
- `planned`
  - artifacts 已生成，可以进入 apply。
- `completed`
  - `database_gateway_apply_workflow` 已执行完成。
- `awaiting-manual-execution`
  - 选择了 `manual-only`，系统只导出了 artifacts，没有自动执行。
- `validated`
  - `database_gateway_validate_workflow` 已通过。

另外：

- `delivery_mode` 支持 `all-at-once` 和 `step-by-step`
  - MCP 会把选定模式写回 plan 响应，便于 client 决定一次展示完整计划还是按步骤组织对话。
- 规划阶段的 `execution_mode` 支持 `review-then-execute` 和 `manual-only`，表示最终 apply 的偏好。
- `database_gateway_apply_workflow` 必须显式传入 `execution_mode`
  - 先用 `preview` 预览，再在确认后使用 `review-then-execute`，或者用 `manual-only` 只导出人工执行包。

### 看到不同状态时，下一步该做什么

- `clarifying`
  - 说明信息还不够。读取 `clarification_questions`，按其中的 `field` 补齐非敏感参数；
    敏感参数通过安全通道取得后，再带上同一个 `plan_id` 继续调用对应的 `plan_*_rule`。
- `planned`
  - 说明执行包已经生成好了。此时不要再补问，应该 review artifacts，然后进入 `database_gateway_apply_workflow`。
- `completed`
  - 说明自动执行已经结束。下一步就是 `database_gateway_validate_workflow`。
- `awaiting-manual-execution`
  - 说明你选择了 `manual-only`。下一步不是重新 `apply`，而是先手工执行返回的 artifacts，然后再 `validate`。
- `validated`
  - 说明本次 workflow 已经闭环完成。
- `failed`
  - 说明当前阶段没有通过。先看 `issues` 和 `mismatches`，判断是缺信息、SQL 执行失败、规则状态不一致，还是逻辑元数据校验失败，再决定是重新 `plan`、补 `apply`，还是修正环境后重新 `validate`。

### 最常看的返回字段

`database_gateway_plan_encrypt_rule` 和 `database_gateway_plan_mask_rule` 返回里，最值得优先看的字段是：

- `plan_id`
  - 本次 workflow 的唯一标识，后续所有补问、执行、校验都依赖它。
- `status`
  - 决定你下一步是继续补问、进入执行，还是先排错。
- `clarification_questions`
  - typed 补问信息；按每个 `field` 补参数，`display_message` 只用于展示给用户。
- `algorithm_recommendations`
  - 当自然语言没有给全算法信息时，这里会返回推荐算法池。
- `derived_column_plan`
  - 只对加密最关键，会告诉你最终采用的 `*_cipher`、`*_assisted_query`、`*_like_query` 命名。
- `ddl_artifacts`
  - 物理列 DDL，例如 `ALTER TABLE ... ADD COLUMN ...`；加密可能生成，脱敏通常不会生成。
- `distsql_artifacts`
  - 最终提交给 Proxy 的 `CREATE/ALTER/DROP ENCRYPT RULE` 或 `MASK RULE`。
- `index_plan`
  - 只对加密出现，且仅在等值查询或模糊查询需要派生索引时返回。

`database_gateway_apply_workflow` 返回里，最值得优先看的字段是：

- `status`
- `issues`
- `step_results`
- `executed_ddl`
- `executed_distsql`
- `skipped_artifacts`
- `manual_artifact_package`

`database_gateway_validate_workflow` 返回里，最值得优先看的字段是：

- `status`
- `overall_status`
- `issues`
- `mismatches`
- `ddl_validation`
- `rule_validation`
- `logical_metadata_validation`
- `sql_executability_validation`

### 加密 workflow

#### 最小可用输入

加密 `create` / `alter` 场景，推荐至少提供下面这些信息：

- `database`
- `table`
- `column`
- `natural_language_intent`
  - 或者在调用方已经明确生命周期时，显式提供 `operation_type=create|alter`
- `algorithm_type`
  - 如果自然语言已经足够明确，可以先不传，让 MCP 推荐；如果你已经确定算法，建议直接传
- `primary_algorithm_properties`
  - 例如 `AES` 需要 `aes-key-value`
- `schema`
  - 多 schema 逻辑库里建议显式传，避免歧义

#### 典型输入

可以直接给自然语言，也可以同时补充结构化参数。下面这个例子会直接规划一个可逆加密流程：

```bash
AES_KEY_VALUE="${SHARDINGSPHERE_AES_KEY_VALUE}"

curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{
    "jsonrpc":"2.0",
    "id":"encrypt-plan-1",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_plan_encrypt_rule",
      "arguments":{
        "database":"logic_db",
        "table":"orders",
        "column":"status",
        "natural_language_intent":"给 status 做可逆加密，不需要等值，不需要模糊",
        "algorithm_type":"AES",
        "primary_algorithm_properties":{"aes-key-value":"'"${AES_KEY_VALUE}"'"}
      }
    }
  }'
```

预期结果：

- 返回 `plan_id`，后续 `apply` 和 `validate` 都使用这个标识继续。
- 返回 `status = planned`。
- `derived_column_plan` 会给出实际使用的派生列名，例如 `status_cipher`。
- `ddl_artifacts`、`distsql_artifacts`、`index_plan` 会给出本次执行计划。
- `masked_property_preview` 和 `distsql_artifacts` 中的敏感属性会按脱敏形式展示，不会回显明文密钥。

#### 完整操作示例：从自然语言到生效

下面这个例子演示一个真正完整的加密链路：第一次调用故意不传密钥，让 MCP 先进入 `clarifying`，然后用同一个 `plan_id` 继续补齐。

第 1 步：先让 MCP 识别需求并推荐算法

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{
    "jsonrpc":"2.0",
    "id":"encrypt-plan-clarifying-1",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_plan_encrypt_rule",
      "arguments":{
        "database":"logic_db",
        "table":"orders",
        "column":"status",
        "natural_language_intent":"给 status 做可逆加密，需要等值，不需要模糊"
      }
    }
  }'
```

典型响应片段如下：

```json
{
  "plan_id": "plan-xxx",
  "status": "clarifying",
  "clarification_questions": [
    {
      "field": "primary_algorithm_properties.aes-key-value",
      "input_type": "secret",
      "secret": true,
      "message": "Sensitive input must be provided through configured secure channels before continuing the same planner."
    }
  ],
  "elicitation_support": {
    "form_mode": true,
    "url_mode": false,
    "selected_interaction": "url_fallback"
  },
  "fallback_reason": "sensitive_form_blocked",
  "algorithm_recommendations": [
    {"algorithm_role": "primary", "algorithm_type": "AES"},
    {"algorithm_role": "assisted_query", "algorithm_type": "MD5"}
  ]
}
```

这个带有 secret 的补问只会作为工具结构化内容返回，不会转换成 MCP form elicitation。
请保留 `plan_id`，通过 secret manager、受保护环境变量或运维控制通道取得密钥后，再继续调用 planner。

实际操作时，可以先把上一步返回的 `plan_id` 暂存为 shell 变量：

```bash
PLAN_ID='plan-xxx'
AES_KEY_VALUE="${SHARDINGSPHERE_AES_KEY_VALUE}"
```

这里的含义非常直接：

- 当前不要 `apply`
- 继续使用同一个 `plan_id`
- 只在客户端已经通过安全通道取得密钥后，再补齐缺失的 `aes-key-value`

第 2 步：继续同一个 plan，补齐缺失属性

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{
    "jsonrpc":"2.0",
    "id":"encrypt-plan-clarifying-2",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_plan_encrypt_rule",
      "arguments":{
        "plan_id":"'"${PLAN_ID}"'",
        "primary_algorithm_properties":{"aes-key-value":"'"${AES_KEY_VALUE}"'"}
      }
    }
  }'
```

典型响应片段如下：

```json
{
  "plan_id": "plan-xxx",
  "status": "planned",
  "current_step": "review",
  "derived_column_plan": {
    "cipher_column_name": "status_cipher",
    "assisted_query_column_name": "status_assisted_query",
    "assisted_query_column_required": true,
    "like_query_column_required": false
  },
  "ddl_artifacts": [
    {"sql": "ALTER TABLE orders ADD COLUMN status_cipher VARCHAR(...) ..."}
  ],
  "index_plan": [
    {"sql": "CREATE INDEX idx_orders_status_assisted_query ON orders (status_assisted_query)"}
  ],
  "distsql_artifacts": [
    {"sql": "ALTER ENCRYPT RULE orders ..."}
  ]
}
```

走到这里时，说明已经可以执行了。你需要 review 的重点是：

- 派生列名是否符合预期
- 是否需要辅助查询列和索引
- DistSQL 中的算法类型和逻辑列映射是否正确

第 3 步：执行已确认的 plan

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-apply-complete-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"database_gateway_apply_workflow\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\",\"execution_mode\":\"review-then-execute\"}}}"
```

典型响应片段如下：

```json
{
  "status": "completed",
  "step_results": [
    {"artifact_type": "add-column", "status": "passed"},
    {"artifact_type": "create-index", "status": "passed"},
    {"artifact_type": "rule_distsql", "status": "passed"}
  ],
  "executed_ddl": ["ALTER TABLE ...", "CREATE INDEX ..."],
  "executed_distsql": ["ALTER ENCRYPT RULE ..."]
}
```

第 4 步：校验最终结果

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-validate-complete-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"database_gateway_validate_workflow\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
```

典型响应片段如下：

```json
{
  "status": "validated",
  "overall_status": "passed",
  "ddl_validation": {"status": "passed"},
  "rule_validation": {"status": "passed"},
  "logical_metadata_validation": {"status": "passed"},
  "sql_executability_validation": {"status": "passed"}
}
```

只要这 4 层都 `passed`，这次加密 workflow 就算真正完成。

#### 缺少算法或属性时

如果自然语言没有说清算法，或者像 `AES` 这样的算法缺少 `aes-key-value` 之类的必填属性，`database_gateway_plan_encrypt_rule` 会返回：

- `status = clarifying`
- `clarification_questions`
- `algorithm_recommendations`
- `property_requirements`

此时应带上同一个 `plan_id` 再次调用 `database_gateway_plan_encrypt_rule`，把缺失的非敏感参数补齐，而不是重新开一个计划。
对于敏感字段，必须先通过 secret manager、受保护环境变量或运维控制通道取得值，再继续。

#### 默认派生列规则

- 默认会为加密列生成 `*_cipher` 派生列。
- 如果自然语言表达了等值查询能力，会额外生成 `*_assisted_query` 和相应索引计划。
- 如果自然语言表达了模糊查询能力，会额外生成 `*_like_query`。
- 如果默认列名冲突，系统会自动追加数字后缀，并把最终命名写回 `derived_column_plan`。

#### 执行与校验

规划阶段默认的最终模式是 `review-then-execute`，但 `database_gateway_apply_workflow` 必须先 `preview`，真实副作用执行必须显式传入 `execution_mode=review-then-execute`。
先预览：

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-apply-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"database_gateway_apply_workflow\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\",\"execution_mode\":\"preview\"}}}"
```

确认预览结果后，执行已确认的 artifacts：

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-apply-2\",\"method\":\"tools/call\",\"params\":{\"name\":\"database_gateway_apply_workflow\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\",\"execution_mode\":\"review-then-execute\"}}}"
```

如果只想让 MCP 生成 SQL 和 DistSQL，不自动执行，可以改为：

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-apply-3\",\"method\":\"tools/call\",\"params\":{\"name\":\"database_gateway_apply_workflow\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\",\"execution_mode\":\"manual-only\"}}}"
```

`manual-only` 会返回 `manual_artifact_package`，里面包含：

- `ddl_artifacts`
- `index_plan`
- `distsql_artifacts`

如果要分步执行，可以把 `approved_steps` 作为执行过滤器，只允许 `ddl`、`index_ddl` 或 `rule_distsql`。
它不是 approval token；应在 review preview 后，从 `preview_artifacts[].approval_step` 复制取值。
未知值会被拒绝，不会静默跳过。
这类分步执行主要用于 review 或灰度流程；如果只执行了一部分，`database_gateway_validate_workflow` 很可能会先失败，直到剩余步骤也补执行完成。

执行完成后，建议立刻调用：

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-validate-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"database_gateway_validate_workflow\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
```

校验会覆盖 4 层：

- `ddl_validation`
- `rule_validation`
- `logical_metadata_validation`
- `sql_executability_validation`

#### 删除加密规则

V1 已支持 `encrypt drop`，但它是“只删规则、不做物理清理”的 workflow：

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{
    "jsonrpc":"2.0",
    "id":"encrypt-plan-drop-1",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_plan_encrypt_rule",
      "arguments":{
        "database":"logic_db",
        "table":"orders",
        "column":"status",
        "operation_type":"drop"
      }
    }
  }'
```

预期结果：

- 返回 `status = planned`
- `missing_required_inputs` 为空，因为 `drop` 不需要再追问可逆、等值、LIKE 这些能力
- `distsql_artifacts` 中会生成 `DROP ENCRYPT RULE` 或 `ALTER ENCRYPT RULE`
- 响应里会带 warning，明确提醒物理派生列和索引仍需人工清理

如果同一张表还有其他加密列，系统会生成保留 sibling rules 的 `ALTER ENCRYPT RULE`；只有当目标表不再剩余任何 encrypt 列时，才会生成 `DROP ENCRYPT RULE`。

### 脱敏 workflow

#### 最小可用输入

脱敏 `create` / `alter` 场景，推荐至少提供：

- `database`
- `table`
- `column`
- `natural_language_intent`
  - 或者在调用方已经明确生命周期时，显式提供 `operation_type=create|alter`
- `operation_type`
- `algorithm_type`
- `primary_algorithm_properties`
- `schema`
  - 多 schema 逻辑库里建议显式传，避免歧义

脱敏 `drop` 场景最小输入则是：

- `database`
- `table`
- `column`
- `operation_type=drop`

#### 创建或修改脱敏规则

脱敏同样支持自然语言和结构化参数混合输入。下面这个例子直接创建一条 mask 规则：

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{
    "jsonrpc":"2.0",
    "id":"mask-plan-1",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_plan_mask_rule",
      "arguments":{
        "database":"logic_db",
        "table":"orders",
        "column":"phone",
        "operation_type":"create",
        "algorithm_type":"KEEP_FIRST_N_LAST_M",
        "primary_algorithm_properties":{"first-n":"3","last-m":"4","replace-char":"*"}
      }
    }
  }'
```

预期结果：

- 返回 `status = planned`
- `distsql_artifacts` 中会生成 `CREATE MASK RULE` 或 `ALTER MASK RULE`
- 脱敏规则不会生成物理派生列，因此不会有 encrypt 那类派生列 DDL

如果自然语言表达类似“把 phone 当作手机号做脱敏，保留前 3 后 4”，系统也会尝试推荐算法；如果缺少必要属性，同样会先进入 `clarifying`。

#### 完整操作示例：从自然语言到生效

下面这个例子演示一个完整的脱敏链路。第一次调用只给自然语言，不直接给结构化属性，让 MCP 先推荐算法并继续补问。

第 1 步：先规划

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{
    "jsonrpc":"2.0",
    "id":"mask-plan-clarifying-1",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_plan_mask_rule",
      "arguments":{
        "database":"logic_db",
        "table":"orders",
        "column":"phone",
        "natural_language_intent":"把 phone 当作手机号做脱敏，保留前3后4"
      }
    }
  }'
```

典型响应片段如下：

```json
{
  "plan_id": "plan-yyy",
  "status": "clarifying",
  "clarification_questions": [
    {"field": "primary_algorithm_properties.from-x", "input_type": "string", "secret": false, "display_message": "请提供属性 `from-x`。"},
    {"field": "primary_algorithm_properties.to-y", "input_type": "string", "secret": false, "display_message": "请提供属性 `to-y`。"}
  ],
  "algorithm_recommendations": [
    {"algorithm_role": "primary", "algorithm_type": "MASK_FROM_X_TO_Y"}
  ]
}
```

实际操作时，可以先把上一步返回的 `plan_id` 暂存为 shell 变量：

```bash
PLAN_ID='plan-yyy'
```

第 2 步：继续同一个 plan，补齐属性

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{
    "jsonrpc":"2.0",
    "id":"mask-plan-clarifying-2",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_plan_mask_rule",
      "arguments":{
        "plan_id":"'"${PLAN_ID}"'",
        "primary_algorithm_properties":{"from-x":"4","to-y":"7"}
      }
    }
  }'
```

典型响应片段如下：

```json
{
  "status": "planned",
  "distsql_artifacts": [
    {"sql": "CREATE MASK RULE orders ..."}
  ],
  "ddl_artifacts": [],
  "index_plan": []
}
```

这一步有两个非常重要的判断点：

- 脱敏不会生成物理派生列，所以 `ddl_artifacts` 正常情况下应该为空
- 你真正需要 review 的核心是 `distsql_artifacts`

第 3 步：执行已确认的脱敏规则

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"mask-apply-complete-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"database_gateway_apply_workflow\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\",\"execution_mode\":\"review-then-execute\"}}}"
```

第 4 步：校验脱敏规则

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"mask-validate-complete-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"database_gateway_validate_workflow\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
```

只要 `rule_validation`、`logical_metadata_validation` 和 `sql_executability_validation` 都通过，这次脱敏 workflow 就可以认为生效了。

#### 删除脱敏规则

V1 支持 `mask drop`。示例：

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{
    "jsonrpc":"2.0",
    "id":"mask-plan-2",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_plan_mask_rule",
      "arguments":{
        "database":"logic_db",
        "table":"orders",
        "column":"phone",
        "operation_type":"drop"
      }
    }
  }'
```

如果同一张表还有其他脱敏列，系统会生成保留 sibling rules 的 `ALTER MASK RULE`；只有当目标表不再剩余任何 mask 列时，才会生成 `DROP MASK RULE`。

### 查看规则与算法池

查看某个逻辑库的加密规则：

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"resource-encrypt-1","method":"resources/read","params":{"uri":"shardingsphere://features/encrypt/databases/logic_db/tables/orders/rules"}}'
```

查看可推荐的加密算法与脱敏算法：

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"resource-plugin-1","method":"resources/read","params":{"uri":"shardingsphere://features/encrypt/algorithms"}}'
```

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"resource-plugin-2","method":"resources/read","params":{"uri":"shardingsphere://features/mask/algorithms"}}'
```

### 当前能力边界

- 只支持 `ShardingSphere-Proxy`
- 加密支持 `create`、`alter` 和 `drop`
- 脱敏支持 `create`、`alter` 和 `drop`
- `encrypt drop` 只删除规则；物理派生列和索引仍需人工清理
- 不处理存量数据迁移或回填
- 不提供自动回滚能力
- 不持久化 SQL 执行 trace
- workflow 与生成 DDL/DistSQL 的规划输入只接受标准未加引号的逻辑 identifier

## Registry 与 OCI 发布

- 官方 MCP Registry 元数据位于 `mcp/server.json`。
- 对外发布的 server name 是 `io.github.apache/shardingsphere-mcp`。
- 首个公开 package 形态是 GHCR 上的 OCI image：`ghcr.io/apache/shardingsphere-mcp:<version>`。
- release workflow 会在发布到官方 MCP Registry 之前，把 `mcp/server.json` 里的版本更新为 GitHub release 版本。

### 以 stdio 模式运行已发布镜像

```bash
docker run --rm -i \
  -e SHARDINGSPHERE_MCP_TRANSPORT=stdio \
  ghcr.io/apache/shardingsphere-mcp:5.5.4
```

### 以 HTTP 模式运行已发布镜像

```bash
docker run --rm -p 18088:18088 \
  ghcr.io/apache/shardingsphere-mcp:5.5.4
```

### 使用自定义 runtime 配置运行已发布镜像

```bash
docker run --rm -i \
  -e SHARDINGSPHERE_MCP_TRANSPORT=stdio \
  -e SHARDINGSPHERE_MCP_CONFIG=/opt/shardingsphere-mcp/conf/custom-mcp-stdio.yaml \
  -v /path/to/mcp-stdio.yaml:/opt/shardingsphere-mcp/conf/custom-mcp-stdio.yaml:ro \
  -v /path/to/plugins:/opt/shardingsphere-mcp/plugins:ro \
  ghcr.io/apache/shardingsphere-mcp:5.5.4
```

说明：

- `SHARDINGSPHERE_MCP_TRANSPORT=stdio` 会切到发行包内置的 `conf/mcp-stdio.yaml`。
- 如果不设置 `SHARDINGSPHERE_MCP_TRANSPORT`，Docker image 会保持现有的 HTTP 默认启动方式。
- 如果容器内需要指定自定义配置文件，请先挂载 YAML 文件，再设置 `SHARDINGSPHERE_MCP_CONFIG=/opt/shardingsphere-mcp/conf/your-config.yaml`。
- HTTP 模式也使用同一个 `SHARDINGSPHERE_MCP_CONFIG` 模式；保留端口映射，并挂载启用 HTTP 的 YAML 文件。
- `.github/workflows/mcp-build.yml` 会先发布 GHCR image，再执行 `mcp-publisher publish`。

## 开发参考

- `test/e2e/mcp` 现在也包含一条真实模型驱动的 MCP smoke：
  - 默认模型栈：Docker-owned `llama.cpp` server + `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`
  - runtime 覆盖：file-backed H2 runtime，加上一条 Testcontainers 拉起的 MySQL runtime
  - runtime 形态：测试会在进程内拉起 production bootstrap HTTP 和 STDIO runtime
  - 最终判定：结构化 JSON 和 MCP tool trace
- 在做定向本地复现前，先把模块依赖装到本地仓库一次：

```bash
./mvnw -pl test/e2e/mcp -am install -DskipTests -DskipITs -Dspotless.skip=true -B -ntp
```

- 构建本地 score-closing LLM runtime image 前，先检查 Docker 磁盘占用：

```bash
docker system df
```

- 只校验本机架构选择，不下载模型：

```bash
sh test/e2e/mcp/src/test/resources/docker/llm-runtime/build-local.sh --dry-run
```

- 在 Maven 启动 LLM lane 前，先构建本地 score-closing LLM runtime image：

```bash
sh test/e2e/mcp/src/test/resources/docker/llm-runtime/build-local.sh
```

- 本地复现这条 LLM smoke：

```bash
./mvnw -pl test/e2e/mcp -Pllm-e2e test -DskipITs -Dspotless.skip=true \
  -Dtest=LLMSmokeE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=true
```

- Score-closing LLM lane 会通过 Testcontainers 启动本地 `apache/shardingsphere-mcp-llm-runtime:local` image。Maven 不下载模型；Docker build 会预打包固定 GGUF 文件，并用 `ADD --checksum` 校验。

- 本地复现 LLM usability lane：

```bash
./mvnw -pl test/e2e/mcp -Pllm-e2e test -DskipITs -Dspotless.skip=true \
  -Dtest=LLMUsabilitySuiteE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=true
```

- 仅本地调试时，可以用 `-Dmcp.llm.runtime-mode=external-debug -Dmcp.llm.base-url=http://127.0.0.1:8080/v1`
  连接已经运行的 OpenAI-compatible endpoint。External debug endpoint 不能作为 score-closing evidence。
- LLM artifact 会落到 `test/e2e/mcp/target/llm-e2e/`。
- 对应的 GitHub Actions 入口是 `.github/workflows/mcp-llm-e2e.yml` 和 `.github/workflows/mcp-llm-usability-e2e.yml`。
  它们会在 PR 改到 MCP module 路径或对应 workflow 文件自身时运行，也保留 `workflow_dispatch` 和工作日 schedule。
  不要把这两条检查配置成 branch protection 或 ruleset 的 required check；失败应该可见，但未跑完的 LLM lane 不应该单独卡住 merge。
  如果超大 PR 因 GitHub path filter 限制漏触发，就用 `workflow_dispatch` 手动补 score evidence。
- 本地 Docker 清理先用 `docker system df` 看占用，再用 `docker image prune` 或 `docker builder prune` 清 dangling image 和 build cache。
  不要把 volume prune 放进默认清理流程；volume 可能包含本地数据库状态，必须单独显式确认。
- `mcp/api`：public tool / resource handler 契约、共享 descriptor、协议 response 与 MCP 协议异常
- `mcp/support`：为 MCP core 与可插拔 feature 提供 database metadata、execution、capability、workflow context、model、facade、SPI 与复用 helper
- `mcp/features/encrypt`：encrypt tools、resources、planning / apply / validation 与算法可见性装配
- `mcp/features/mask`：mask tools、resources、planning / apply / validation 与算法可见性装配
- `mcp/core`：handler 发现、registry、request scope 实现、session、SQL execution trace 创建、execute-query runtime service 聚合，以及 JDBC runtime 配置模型、metadata 发现、`DatabaseRuntime` 装配与 JDBC-backed runtime context factory
- `mcp/bootstrap`：基于 MCP Java SDK 的 bootstrap、HTTP / STDIO transport、顶层配置加载、feature SPI 汇总与生命周期管理
- `distribution/mcp`：独立打包、启动脚本、配置、Dockerfile
- `test/e2e/mcp`：端到端契约验证

如果要做本地调试或更完整的语义验证，优先参考 `mcp/bootstrap` 下的集成测试和 `test/e2e/mcp` 下的 E2E 用例。
