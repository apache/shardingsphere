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
- `conf/mcp.yaml` 现在对支持字段名采用严格 schema：`transport.http.enabled`、`transport.http.bindHost`、`transport.http.allowRemoteAccess`、`transport.http.accessToken`、`transport.http.port`、`transport.http.endpointPath`、`transport.stdio.enabled`，以及每个 runtime database 的全部字段都只能使用受支持字段名。
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
    allowRemoteAccess: false
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
  --data '{"jsonrpc":"2.0","id":"resource-1","method":"resources/read","params":{"uri":"shardingsphere://databases/orders/schemas/public/tables"}}'
```

预期结果：

- 响应类型是 `text/event-stream`。
- JSON 负载位于 `data:` 行，其中会包含 `orders`、`order_items`、`active_orders`。

说明：

- metadata 的 list / detail / capability discovery 统一走 `resources/read`。
- 当前 public tools 包括 `search_metadata`、`execute_query`、`plan_encrypt_rule`、`apply_encrypt_rule`、`validate_encrypt_rule`、`plan_mask_rule`、`apply_mask_rule` 和 `validate_mask_rule`。
- 加密与脱敏 workflow 面向由 ShardingSphere-Proxy 暴露的逻辑库；下文会单独说明这部分的前置条件和使用方式。
- `search_metadata.object_types` 只接受 `database`、`schema`、`table`、`view`、`column`、`index`、`sequence`。

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"tool-1","method":"tools/call","params":{"name":"search_metadata","arguments":{"database":"orders","query":"order","object_types":["table","view"]}}}'
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
  --data '{"jsonrpc":"2.0","id":"tool-2","method":"tools/call","params":{"name":"execute_query","arguments":{"database":"orders","schema":"public","sql":"SELECT status FROM orders ORDER BY order_id","max_rows":10}}}'
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

说明：

- 进程仍然会以前台方式运行。
- 如果 `transport.http.enabled` 和 `transport.stdio.enabled` 同时为 `false`，启动会因 "Exactly one transport must be explicitly enabled. Set either `transport.http.enabled` or `transport.stdio.enabled` to true." 失败。
- 如果两个 transport 同时启用，启动会因 "HTTP and STDIO transports cannot be enabled at the same time. Choose exactly one transport." 失败。
- 默认 `conf/logback.xml` 会把控制台日志写到 stderr，并把文件日志写到 `logs/mcp.log`，这样 stdout 可以专门用于 MCP 协议消息。
- STDIO 模式面向 MCP client，不是给人工手输请求的交互式 Shell。推荐在 MCP client 配置里把它作为子进程启动。

参考：

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/stdio/StdioMCPServer.java`
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/stdio/StdioTransportIntegrationTest.java`

## Runtime 说明

- 发行包里的 `conf/mcp.yaml` 现在默认内置一段 demo 多数据库 JDBC `runtimeDatabases` 配置，所以第一次启动就能验证逻辑库发现和真实 query 执行。
- 如果要接真实部署，请把 `runtimeDatabases` 段替换成你自己的逻辑库映射和 JDBC 连接属性。每个逻辑库条目都需要显式声明所需的 runtime 字段；schema 发现改为依赖 JDBC metadata，legacy `runtime.*` alias 已不再支持。
- 对支持 JDBC 4 自动注册的驱动，`driverClassName` 可以不写；只有目标驱动需要显式覆盖时再配置。
- 如果目标数据库的驱动没有随发行包提供，请先把对应 jar 放到 `ext-lib/`，再执行 `bin/start.sh`。
- 每个 runtime 进程必须且只能启用一种 transport。
- 如果只需要本地 HTTP 调试，保留 `transport.http.enabled: true`，并把 `transport.stdio.enabled` 设为 `false`。
- 如果要给本地 MCP client 走 stdio，保留 `transport.http.enabled: false`，并把 `transport.stdio.enabled` 设为 `true`。
- `transport.http.bindHost` 表示 HTTP 服务监听在哪个地址上：`127.0.0.1`、`localhost`、`::1` 只面向本机；`0.0.0.0` 或指定内网 IP 会面向对应网络接口。
- 非 loopback `bindHost` 必须显式设置 `transport.http.allowRemoteAccess: true`，否则启动失败；该字段只表达远程暴露意图。
- 配置了 `transport.http.accessToken` 后，所有 HTTP 请求都必须携带 `Authorization: Bearer <token>`。
- 非 loopback `bindHost` 还必须配置非空的 `transport.http.accessToken`，避免 remote HTTP 以匿名方式暴露。
- 内建 `accessToken` 是部署级共享密钥，不是登录态 token，也不是按用户划分的凭证。
- 即使启用了内建 `accessToken`，对外暴露场景仍建议放在受信网络、上游网关或反向代理后面。
- 如果要使用自定义配置文件启动，可以执行 `bin/start.sh /path/to/mcp.yaml`。
- 如果要调整 JVM 参数，可以使用 `JAVA_OPTS`，例如 `JAVA_OPTS="-Xms256m -Xmx256m" bin/start.sh`。

## Feature SPI 结构

当前 MCP 子链路按 `features + core + bootstrap` 分层组织：

- `mcp/features/spi`
  - 定义 feature SPI、workflow 公共模型、descriptor 与共享 issue / response 语义
- `mcp/features/encrypt`
  - 提供 encrypt MCP tools、resources 与 workflow 实现
- `mcp/features/mask`
  - 提供 mask MCP tools、resources 与 workflow 实现
- `mcp/core`
  - 提供 capability、metadata、session、execute-query 与通用 runtime 能力
- `mcp/bootstrap`
  - 通过 MCP Java SDK 汇总各 feature 注册结果，并暴露 HTTP / STDIO transport

encrypt 和 mask 在 MCP 里不是 bootstrap 内硬编码的特殊功能，而是通过 feature SPI 注册到 MCP runtime 的可插拔 feature。

## 如何新增一个 MCP Feature

如果你要在现有 encrypt / mask 之外再新增一个 feature，建议保持下面这条最小路径：

- 在 `mcp/features/<feature>` 新建模块，只依赖 `mcp/features/spi` 和必要的领域模块，不要依赖 `mcp/bootstrap`
- 如果是新增 feature 模块，还要把它接入构建与运行时 classpath：先加入 `mcp/features/pom.xml`，再让当前 runtime 入口模块（现在是 `mcp/bootstrap`）依赖它，这样打包后的发行物里才会真正带上对应 jar
- 对外新增 tool 时，实现 `ToolHandler`，并提供唯一的 `MCPToolDescriptor`
- 对外新增 resource 时，实现 `ResourceHandler`，并提供唯一的 URI pattern
- 在 `src/main/resources/META-INF/services/` 下分别注册 `org.apache.shardingsphere.mcp.tool.handler.ToolHandler` 和 `org.apache.shardingsphere.mcp.resource.handler.ResourceHandler`
- feature URI 统一使用 `shardingsphere://features/<feature>/...` 命名空间，避免和公共 metadata path 混用
- `mcp/core` 会通过 `ShardingSphereServiceLoader` 自动发现并校验这些 handler，`mcp/bootstrap` 只负责把最终结果发布到协议层
- tool 名和 resource URI pattern 必须在全局范围内保持唯一；重复注册会在启动期校验时被拒绝

encrypt 和 mask 模块本身就是最直接的参考实现。

## 基于 ShardingSphere-Proxy 的加密与脱敏 Workflow

这组 workflow tool 让 MCP client 可以通过自然语言或结构化参数，对某个逻辑表的某一列规划、执行并校验加密或脱敏规则。
它的目标不是自己实现加密，而是帮助大模型把用户意图转换成 ShardingSphere-Proxy 可执行的 DDL、DistSQL 与校验动作。
这些 tools 与 resources 由 encrypt / mask feature 通过 SPI 注册进入 MCP，bootstrap 只负责汇总并发布到协议层。

### 前置条件

- 当前 V1 只支持 `ShardingSphere-Proxy`。
- MCP runtime 应通过 JDBC 连接到 `ShardingSphere-Proxy` 的逻辑库，而不是直接连接底层存储库。
- Quick Start 中的 demo H2 runtime 适合验证 metadata discovery 和 query；如果要使用加密与脱敏 workflow，请把 `runtimeDatabases` 改成指向 Proxy 的逻辑库。
- 当前 workflow 只处理规则、元数据和 SQL 可执行性校验，不处理存量数据迁移或回填。

### 相关 tools 与 resources

加密 feature 对外暴露 3 个 tools：

- `plan_encrypt_rule`
  - 识别加密意图，补全缺失参数，生成派生列方案、DDL、DistSQL、索引计划和校验策略。
- `apply_encrypt_rule`
  - 执行加密 plan 生成的 artifacts，或在 `manual-only` 模式下导出人工执行包。
- `validate_encrypt_rule`
  - 从 DDL、规则状态、逻辑元数据和 SQL 可执行性 4 个层面校验加密结果。

脱敏 feature 对外暴露 3 个 tools：

- `plan_mask_rule`
  - 识别脱敏意图，补全缺失参数，生成 DistSQL 和校验策略。
- `apply_mask_rule`
  - 执行脱敏 plan 生成的 artifacts，或在 `manual-only` 模式下导出人工执行包。
- `validate_mask_rule`
  - 从规则状态、逻辑元数据和 SQL 可执行性层面校验脱敏结果。

Workflow 同时补充了以下 feature resources：

- `shardingsphere://features/encrypt/algorithms`
- `shardingsphere://features/encrypt/databases/{database}/rules`
- `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules`
- `shardingsphere://features/mask/algorithms`
- `shardingsphere://features/mask/databases/{database}/rules`
- `shardingsphere://features/mask/databases/{database}/tables/{table}/rules`

其中 `features/*/algorithms` 会同时展示内建算法和当前 Proxy 可见的自定义 SPI 算法，方便大模型做算法推荐。

### 使用前先记住这几条

- 整个 workflow 必须复用同一个 `MCP-Session-Id`。`plan`、`apply`、`validate` 如果切换到别的 session，后续调用会因为 plan 归属不一致而失败。
- 第一次调用 `plan_encrypt_rule` 或 `plan_mask_rule` 时不需要 `plan_id`；只要拿到 `plan_id`，后续所有补问、继续规划、执行和校验都继续使用这个 `plan_id`，但只在当前 feature 的 workflow 内复用。
- `plan_encrypt_rule` 和 `plan_mask_rule` 只负责规划，不会执行任何 DDL 或 DistSQL；真正执行发生在各自的 `apply_*_rule`。
- `validate_encrypt_rule` 和 `validate_mask_rule` 只做校验，不会修改规则，也不会补执行遗漏步骤。
- 用户始终面向逻辑库、逻辑表、逻辑列发起请求。对加密场景，MCP 可能会自动规划并创建物理派生列，但对用户暴露的目标仍然是 Proxy 里的逻辑对象。
- `schema` 是可选的；如果 Proxy 逻辑库下只有一个 schema，MCP 会自动补齐；如果无法唯一定位，MCP 会明确返回 `请明确 schema。`。
- `delivery_mode` 只影响客户端如何组织对话和展示步骤，不影响最终生成的 artifacts；真正影响执行行为的是 `execution_mode`。
- 敏感算法属性在 `plan` 和 `apply` 的返回中都只会以脱敏形式展示，不会明文回显。
- 下面所有 `curl` 示例都默认你已经完成了 MCP `initialize`，并持有同一个 `SESSION_ID` 和对应的 `PROTOCOL_VERSION`。

### 推荐操作顺序

如果你想把一次加密或脱敏 workflow 稳定跑通，直接按下面顺序调用即可：

1. 先调用对应 feature 的 planner：加密用 `plan_encrypt_rule`，脱敏用 `plan_mask_rule`，不要直接从 `apply` 开始。
2. 如果返回 `status = clarifying`，读取 `pending_questions`，带上同一个 `plan_id` 再次调用同一个 `plan_*_rule` 补齐缺失信息。
3. 如果返回 `status = planned`，重点 review `derived_column_plan`、`ddl_artifacts`、`distsql_artifacts`、`index_plan`。
4. 调用对应的 `apply_*_rule` 执行 artifacts；如果要人工执行，就把 `execution_mode` 设为 `manual-only`。
5. 如果 `apply` 返回 `awaiting-manual-execution`，先把 `manual_artifact_package` 里的 SQL / DistSQL 在 Proxy 上手工执行完，再进入下一步。
6. 调用对应的 `validate_*_rule`，确认返回中的校验层级都通过。
7. 如果 `validate` 失败，优先看 `issues` 和 `mismatches`，修复后再继续补执行或重新规划。

### 整体交互方式

`plan_encrypt_rule` 和 `plan_mask_rule` 每次都会返回全局步骤列表，并告诉客户端当前走到哪一步。默认步骤如下：

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
  - `apply_encrypt_rule` 或 `apply_mask_rule` 已执行完成。
- `awaiting-manual-execution`
  - 选择了 `manual-only`，系统只导出了 artifacts，没有自动执行。
- `validated`
  - `validate_encrypt_rule` 或 `validate_mask_rule` 已通过。

另外：

- `delivery_mode` 支持 `all-at-once` 和 `step-by-step`
  - MCP 会把选定模式写回 plan 响应，便于 client 决定一次展示完整计划还是按步骤组织对话。
- `execution_mode` 支持 `review-then-execute` 和 `manual-only`
  - 前者自动执行已生成的 artifacts，后者只导出人工执行包。

### 看到不同状态时，下一步该做什么

- `clarifying`
  - 说明信息还不够。直接读取 `pending_questions`，带上同一个 `plan_id` 继续调用对应的 `plan_*_rule`。
- `planned`
  - 说明执行包已经生成好了。此时不要再补问，应该 review artifacts，然后进入对应的 `apply_*_rule`。
- `completed`
  - 说明自动执行已经结束。下一步就是对应的 `validate_*_rule`。
- `awaiting-manual-execution`
  - 说明你选择了 `manual-only`。下一步不是重新 `apply`，而是先手工执行返回的 artifacts，然后再 `validate`。
- `validated`
  - 说明本次 workflow 已经闭环完成。
- `failed`
  - 说明当前阶段没有通过。先看 `issues` 和 `mismatches`，判断是缺信息、SQL 执行失败、规则状态不一致，还是逻辑元数据校验失败，再决定是重新 `plan`、补 `apply`，还是修正环境后重新 `validate`。

### 最常看的返回字段

`plan_encrypt_rule` 和 `plan_mask_rule` 返回里，最值得优先看的字段是：

- `plan_id`
  - 本次 workflow 的唯一标识，后续所有补问、执行、校验都依赖它。
- `status`
  - 决定你下一步是继续补问、进入执行，还是先排错。
- `pending_questions`
  - 只在 `clarifying` 阶段重点处理，里面就是 MCP 还缺的关键信息。
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

`apply_encrypt_rule` 和 `apply_mask_rule` 返回里，最值得优先看的字段是：

- `status`
- `issues`
- `step_results`
- `executed_ddl`
- `executed_distsql`
- `skipped_artifacts`
- `manual_artifact_package`

`validate_encrypt_rule` 和 `validate_mask_rule` 返回里，最值得优先看的字段是：

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
      "name":"plan_encrypt_rule",
      "arguments":{
        "database":"logic_db",
        "table":"orders",
        "column":"status",
        "natural_language_intent":"给 status 做可逆加密，不需要等值，不需要模糊",
        "algorithm_type":"AES",
        "primary_algorithm_properties":{"aes-key-value":"123456abc"}
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
      "name":"plan_encrypt_rule",
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
  "pending_questions": ["请提供属性 `aes-key-value`。"],
  "algorithm_recommendations": [
    {"algorithm_role": "primary", "algorithm_type": "AES"},
    {"algorithm_role": "assisted_query", "algorithm_type": "MD5"}
  ]
}
```

实际操作时，可以先把上一步返回的 `plan_id` 暂存为 shell 变量：

```bash
PLAN_ID='plan-xxx'
```

这里的含义非常直接：

- 当前不要 `apply`
- 继续使用同一个 `plan_id`
- 只需要把缺失的 `aes-key-value` 补回来即可

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
      "name":"plan_encrypt_rule",
      "arguments":{
        "plan_id":"'"${PLAN_ID}"'",
        "primary_algorithm_properties":{"aes-key-value":"123456abc"}
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

第 3 步：执行 plan

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-apply-complete-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"apply_encrypt_rule\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
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
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-validate-complete-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"validate_encrypt_rule\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
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

如果自然语言没有说清算法，或者像 `AES` 这样的算法缺少 `aes-key-value` 之类的必填属性，`plan_encrypt_rule` 会返回：

- `status = clarifying`
- `pending_questions`
- `algorithm_recommendations`
- `property_requirements`

此时应带上同一个 `plan_id` 再次调用 `plan_encrypt_rule`，把缺失参数补齐，而不是重新开一个计划。

#### 默认派生列规则

- 默认会为加密列生成 `*_cipher` 派生列。
- 如果自然语言表达了等值查询能力，会额外生成 `*_assisted_query` 和相应索引计划。
- 如果自然语言表达了模糊查询能力，会额外生成 `*_like_query`。
- 如果默认列名冲突，系统会自动追加数字后缀，并把最终命名写回 `derived_column_plan`。

#### 执行与校验

默认执行模式是 `review-then-execute`：

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-apply-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"apply_encrypt_rule\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
```

如果只想让 MCP 生成 SQL 和 DistSQL，不自动执行，可以改为：

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-apply-2\",\"method\":\"tools/call\",\"params\":{\"name\":\"apply_encrypt_rule\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\",\"execution_mode\":\"manual-only\"}}}"
```

`manual-only` 会返回 `manual_artifact_package`，里面包含：

- `ddl_artifacts`
- `index_plan`
- `distsql_artifacts`

如果要分步执行，可以通过 `approved_steps` 只执行一部分步骤，例如只执行 `ddl`、`index_ddl` 或 `rule_distsql`。
这类分步执行主要用于 review 或灰度流程；如果只执行了一部分，`validate_encrypt_rule` 很可能会先失败，直到剩余步骤也补执行完成。

执行完成后，建议立刻调用：

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-validate-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"validate_encrypt_rule\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
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
      "name":"plan_encrypt_rule",
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
- `pending_questions` 为空，因为 `drop` 不需要再追问可逆、等值、LIKE 这些能力
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
      "name":"plan_mask_rule",
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
      "name":"plan_mask_rule",
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
  "pending_questions": ["请提供属性 `from-x`。", "请提供属性 `to-y`。"],
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
      "name":"plan_mask_rule",
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

第 3 步：执行脱敏规则

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"mask-apply-complete-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"apply_mask_rule\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
```

第 4 步：校验脱敏规则

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"mask-validate-complete-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"validate_mask_rule\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
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
      "name":"plan_mask_rule",
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
- 不做审计落库
- V1 只支持未加引号的 SQL identifier

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

说明：

- `SHARDINGSPHERE_MCP_TRANSPORT=stdio` 会切到发行包内置的 `conf/mcp-stdio.yaml`。
- 如果不设置 `SHARDINGSPHERE_MCP_TRANSPORT`，Docker image 会保持现有的 HTTP 默认启动方式。
- 如果容器内需要指定自定义配置文件，可以设置 `SHARDINGSPHERE_MCP_CONFIG=/opt/shardingsphere-mcp/conf/your-config.yaml`。
- `.github/workflows/mcp-build.yml` 会先发布 GHCR image，再执行 `mcp-publisher publish`。

## 开发参考

- `test/e2e/mcp` 现在也包含一条真实模型驱动的 MCP smoke：
  - 默认模型栈：`Ollama + qwen3:1.7b`
  - runtime 覆盖：file-backed H2 runtime，加上一条 Testcontainers 拉起的 MySQL runtime
  - runtime 形态：测试会在进程内拉起 production bootstrap HTTP 和 STDIO runtime
  - 最终判定：结构化 JSON 和 MCP tool trace
- 在做定向本地复现前，先把模块依赖装到本地仓库一次：

```bash
./mvnw -pl test/e2e/mcp -am install -DskipTests -DskipITs -Dspotless.skip=true -B -ntp
```

- 本地复现这条 LLM smoke：

```bash
docker run -d --rm --name ollama-mcp-llm-e2e -p 11434:11434 ollama/ollama:latest
docker exec ollama-mcp-llm-e2e ollama pull qwen3:1.7b
MCP_LLM_E2E_ENABLED=true \
MCP_LLM_BASE_URL=http://127.0.0.1:11434/v1 \
MCP_LLM_MODEL=qwen3:1.7b \
./mvnw -pl test/e2e/mcp test -DskipITs -Dspotless.skip=true \
  -Dtest=LLMSmokeE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=true
```

- LLM usability 这条 lane 可以复用同一个 Ollama 进程：

```bash
MCP_LLM_E2E_ENABLED=true \
MCP_LLM_BASE_URL=http://127.0.0.1:11434/v1 \
MCP_LLM_MODEL=qwen3:1.7b \
./mvnw -pl test/e2e/mcp test -DskipITs -Dspotless.skip=true \
  -Dtest=LLMUsabilitySuiteE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=true
```

- LLM smoke 的 artifact 会落到 `test/e2e/mcp/target/llm-e2e/`。
- 对应的 GitHub Actions 入口是 `.github/workflows/mcp-llm-e2e.yml`，第一轮按 `workflow_dispatch` 和 nightly schedule 交付，不进 PR gate。
- `mcp/features/spi`：feature SPI、workflow 公共模型、descriptor、共享 issue / response 语义
- `mcp/features/encrypt`：encrypt tools、resources、planning / apply / validation 与算法可见性装配
- `mcp/features/mask`：mask tools、resources、planning / apply / validation 与算法可见性装配
- `mcp/core`：capability、metadata、session、audit、execute-query 契约、runtime service 聚合，以及 JDBC runtime 配置模型、metadata 发现、`DatabaseRuntime` 装配与 JDBC-backed runtime context factory
- `mcp/bootstrap`：基于 MCP Java SDK 的 bootstrap、HTTP / STDIO transport、顶层配置加载、feature SPI 汇总与生命周期管理
- `distribution/mcp`：独立打包、启动脚本、配置、Dockerfile
- `test/e2e/mcp`：端到端契约验证

如果要做本地调试或更完整的语义验证，优先参考 `mcp/bootstrap` 下的集成测试和 `test/e2e/mcp` 下的 E2E 用例。
