# ShardingSphere MCP 接入本地 Codex 指南

本文面向已经完成 ShardingSphere MCP 构建的开发者，目标是让本地 Codex 通过 ShardingSphere MCP 访问你本机可达的数据库。

推荐优先使用 `stdio` 模式接入 Codex。这个模式最适合本地开发，因为由 Codex 直接拉起 MCP 子进程，不需要你额外维护 HTTP 服务生命周期。

## 1. 接入拓扑

```text
Codex -> ShardingSphere MCP -> JDBC Driver -> Local Database
```

其中：

- Codex 负责发起 MCP tool 调用
- ShardingSphere MCP 负责 metadata discovery、capability 查询和 SQL 执行
- JDBC Driver 负责连到你的本地数据库

## 2. 前置条件

- JDK 17
- 仓库根目录下可用的 Maven Wrapper
- 你的数据库能够从当前机器访问
- 对应数据库 JDBC driver 可用

如果目标数据库驱动或额外的 MCP feature jar 没有被发行包自带，请把对应 jar 放到发行包目录的 `plugins/` 下。

## 3. 先构建发行包

在仓库根目录执行：

```bash
./mvnw -pl distribution/mcp -am -DskipTests package
DIST_DIR=$(find distribution/mcp/target -maxdepth 1 -type d -name 'apache-shardingsphere-mcp-*' | sed -n '1p')
echo "${DIST_DIR}"
```

后续命令都基于 `${DIST_DIR}` 这个发行包目录。

不要直接把源码树里的 `distribution/mcp/src/main/bin/start.sh` 当成长期运行入口；推荐始终使用发行包里的 `bin/start.sh`。

## 4. 配置真实数据库

发行包默认 `conf/mcp.yaml` 使用的是 H2 demo 数据库。接你自己的数据库时，需要替换 `runtimeDatabases`，并为 Codex 单独准备一个 `stdio` 配置文件，例如 `${DIST_DIR}/conf/mcp-codex-stdio.yaml`。

如果你要在 Codex 里使用 encrypt / mask workflow，`runtimeDatabases` 指向的应当是 `ShardingSphere-Proxy` 暴露出来的逻辑库，而不是底层存储库。
这类 workflow 会生成并执行 Proxy 可理解的 DDL、DistSQL 和校验动作，不会直接面向底层物理库做规则维护。

### Proxy 逻辑库示例

如果你的 `ShardingSphere-Proxy` 通过 MySQL 协议监听在 `127.0.0.1:3307`，并暴露逻辑库 `proxy_db`，可以这样配置：

```yaml
transport:
  http:
    enabled: false
    bindHost: 127.0.0.1
    allowRemoteAccess: false
    port: 18088
    endpointPath: /mcp
  stdio:
    enabled: true

runtimeDatabases:
  proxy_db:
    databaseType: MySQL
    jdbcUrl: "jdbc:mysql://127.0.0.1:3307/proxy_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8"
    username: "root"
    password: "root"
    driverClassName: com.mysql.cj.jdbc.Driver
```

这里的 `proxy_db` 是 Proxy 暴露出来的逻辑库名，不是底层存储库名。
如果逻辑库下存在多个 schema，后续调用 encrypt / mask workflow 时，建议显式传 `schema`。

### 单库模板

```yaml
transport:
  http:
    enabled: false
    bindHost: 127.0.0.1
    allowRemoteAccess: false
    port: 18088
    endpointPath: /mcp
  stdio:
    enabled: true

runtimeDatabases:
  local_db:
    databaseType: MySQL
    jdbcUrl: "jdbc:mysql://127.0.0.1:3306/demo_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8"
    username: "root"
    password: "root"
    driverClassName: com.mysql.cj.jdbc.Driver
```

### PostgreSQL 示例

```yaml
transport:
  http:
    enabled: false
    bindHost: 127.0.0.1
    allowRemoteAccess: false
    port: 18088
    endpointPath: /mcp
  stdio:
    enabled: true

runtimeDatabases:
  local_db:
    databaseType: PostgreSQL
    jdbcUrl: "jdbc:postgresql://127.0.0.1:5432/demo_db"
    username: "postgres"
    password: "postgres"
    driverClassName: org.postgresql.Driver
```

### 多库模板

如果你希望一个 MCP 实例同时暴露多个逻辑库，可以这样配置：

```yaml
transport:
  http:
    enabled: false
    bindHost: 127.0.0.1
    allowRemoteAccess: false
    port: 18088
    endpointPath: /mcp
  stdio:
    enabled: true

runtimeDatabases:
  app_db:
    databaseType: MySQL
    jdbcUrl: "jdbc:mysql://127.0.0.1:3306/app_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8"
    username: "root"
    password: "root"
    driverClassName: com.mysql.cj.jdbc.Driver
  analytics_db:
    databaseType: PostgreSQL
    jdbcUrl: "jdbc:postgresql://127.0.0.1:5432/analytics_db"
    username: "postgres"
    password: "postgres"
    driverClassName: org.postgresql.Driver
```

说明：

- `local_db`、`app_db`、`analytics_db` 是逻辑库名，Codex 后续会用这个名字调用工具
- `databaseType` 需要和目标数据库类型匹配
- 对 JDBC 4 自动注册驱动，`driverClassName` 可省略；不确定时建议显式填写
- 每个进程必须且只能启用一种 transport

## 5. 推荐方式：用 STDIO 接入 Codex

### 5.1 用命令注册 MCP server

执行：

```bash
codex mcp add shardingsphere-mcp-local \
  --env JAVA_HOME="${JAVA_HOME}" \
  -- "${DIST_DIR}/bin/start.sh" "${DIST_DIR}/conf/mcp-codex-stdio.yaml"
```

注册完成后，可以检查：

```bash
codex mcp list
codex mcp get shardingsphere-mcp-local
```

### 5.2 对应的手写配置

上面的命令本质上会把配置写入 `~/.codex/config.toml`，结构如下：

```toml
[mcp_servers.shardingsphere-mcp-local]
command = "/absolute/path/to/apache-shardingsphere-mcp-<version>/bin/start.sh"
args = ["/absolute/path/to/apache-shardingsphere-mcp-<version>/conf/mcp-codex-stdio.yaml"]

[mcp_servers.shardingsphere-mcp-local.env]
JAVA_HOME = "/absolute/path/to/jdk-17"
```

如果你更喜欢手工维护 `~/.codex/config.toml`，可以直接按这个结构写。

### 5.3 重启或新开 Codex 会话

注册完成后，建议重启 Codex，或者至少新开一个会话，让新的 MCP server 配置生效。

## 6. 可选方式：用 HTTP 接入 Codex

如果你希望把 MCP runtime 独立启动出来做联调，可以改用 HTTP。

### 6.1 准备 HTTP 配置

例如 `${DIST_DIR}/conf/mcp-codex-http.yaml`：

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
  local_db:
    databaseType: MySQL
    jdbcUrl: "jdbc:mysql://127.0.0.1:3306/demo_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8"
    username: "root"
    password: "root"
    driverClassName: com.mysql.cj.jdbc.Driver
```

### 6.2 HTTP 鉴权与远程暴露约束

如果你只在本机给 Codex 使用 HTTP，保持 `bindHost: 127.0.0.1` 且 `allowRemoteAccess: false` 即可。

如果 HTTP 需要暴露到非 loopback 地址，必须同时满足下面 3 个条件：

- 把 `transport.http.bindHost` 改成非 loopback 地址
- 显式设置 `transport.http.allowRemoteAccess: true`
- 配置非空的 `transport.http.accessToken`

例如：

```yaml
transport:
  http:
    enabled: true
    bindHost: 0.0.0.0
    allowRemoteAccess: true
    accessToken: "replace-with-strong-token"
    port: 18088
    endpointPath: /mcp
  stdio:
    enabled: false
```

后续所有 HTTP 请求都需要携带：

```text
Authorization: Bearer <token>
```

### 6.3 启动 MCP runtime

```bash
"${DIST_DIR}/bin/start.sh" "${DIST_DIR}/conf/mcp-codex-http.yaml"
```

### 6.4 把 HTTP server 注册到 Codex

```bash
codex mcp add shardingsphere-mcp-http --url http://127.0.0.1:18088/mcp
```

对应的 `~/.codex/config.toml` 结构如下：

```toml
[mcp_servers.shardingsphere-mcp-http]
url = "http://127.0.0.1:18088/mcp"
```

上面的 `codex mcp add ... --url ...` 示例默认面向本机、无 token 的 HTTP 调试场景。
如果你启用了 `transport.http.accessToken`，更适合用 `curl` 或通用 MCP HTTP client 做协议联调；本地 Codex 仍建议优先使用 `stdio`。

这个模式更适合单独排查日志、抓包和做协议联调；日常本地使用仍建议优先 `stdio`。

## 7. Codex 里怎么用

ShardingSphere MCP 当前暴露的核心 tool 和 resource 包括：

- tool: `search_metadata`
- tool: `execute_query`
- tool: `plan_encrypt_rule`
- tool: `apply_encrypt_rule`
- tool: `validate_encrypt_rule`
- tool: `plan_mask_rule`
- tool: `apply_mask_rule`
- tool: `validate_mask_rule`
- resource: `shardingsphere://capabilities`
- resource: `shardingsphere://databases`
- resource template: `shardingsphere://databases/{database}/schemas/{schema}/tables`
- resource template: `shardingsphere://databases/{database}/schemas/{schema}/views`
- resource template: `shardingsphere://features/encrypt/algorithms`
- resource template: `shardingsphere://features/encrypt/databases/{database}/rules`
- resource template: `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules`
- resource template: `shardingsphere://features/mask/algorithms`
- resource template: `shardingsphere://features/mask/databases/{database}/rules`
- resource template: `shardingsphere://features/mask/databases/{database}/tables/{table}/rules`

你可以直接在 Codex 里这样提问：

- `读取 shardingsphere-mcp-local 里的所有逻辑库资源`
- `读取 local_db 的 public schema 下表资源`
- `搜索 local_db 里名称包含 order 的表和视图`
- `执行 SQL: SELECT * FROM t_order ORDER BY order_id DESC LIMIT 10`
- `先读取 local_db 的 capability resource，再帮我查最近 10 条订单`
- `帮我给 proxy_db.customer_profiles.phone 规划一条可逆加密规则，并告诉我下一步该调用哪个 tool`
- `读取 proxy_db 的 encrypt algorithms resource，然后帮我为 customer_profiles.phone 选择合适算法`
- `帮我把 proxy_db.customer_profiles.id_card 的脱敏 workflow 跑完整，但先只导出人工执行包`

如果你配置了多库，也可以明确指定逻辑库名，例如：

- `查询 analytics_db.public.events 最近 20 条数据`

### 7.1 在 Codex 里使用 Encrypt / Mask Workflow

如果你打算在 Codex 里直接操作加密或脱敏规则，建议按下面顺序使用：

1. 先调用对应 feature 的 planner：
   - 加密使用 `plan_encrypt_rule`
   - 脱敏使用 `plan_mask_rule`
2. 如果返回 `status = clarifying`，读取 `pending_questions`，并带上同一个 `plan_id` 继续调用同一个 `plan_*_rule`
3. 如果返回 `status = planned`，review artifacts，再调用对应的 `apply_*_rule`
4. 如果要人工执行，给 `apply_*_rule` 传 `execution_mode = manual-only`
5. 最后调用对应的 `validate_*_rule`

使用时务必记住：

- 整个 workflow 必须复用同一个 `MCP-Session-Id`
- 第一次 `plan_*_rule` 返回的 `plan_id`，后续整个 workflow 都要继续使用
- `manual-only` 只导出执行包，不会自动执行 SQL 或 DistSQL
- encrypt / mask workflow 当前只适用于 Proxy 逻辑库，不适合直接连到底层物理库

### 7.2 常见返回状态与下一步动作

- `clarifying`
  - 继续读取 `pending_questions`，带同一个 `plan_id` 补问，不要新开 plan
- `planned`
  - 先 review 生成的 artifacts，再进入 `apply_*_rule`
- `awaiting-manual-execution`
  - 先执行 `manual_artifact_package` 里的 SQL 或 DistSQL，再进入 `validate_*_rule`
- `failed`
  - 优先看 `issues` 和 `mismatches`，判断是环境问题、规则状态不一致，还是前置执行没完成

### 7.3 一个最小的 Codex 提问方式

如果你已经把 Proxy 逻辑库配置成了 `proxy_db`，可以直接这样让 Codex 驱动 workflow：

- `读取 proxy_db 的 encrypt algorithms resource，然后为 customer_profiles.phone 规划加密规则`
- `继续刚才的 encrypt plan，如果还缺参数就先补问，不要新开 plan`
- `把刚才的 encrypt plan 改成 manual-only，只导出执行包`
- `在我执行完返回的 SQL 和 DistSQL 之后，再帮我 validate`
- `为 proxy_db.customer_profiles.id_card 规划 mask 规则，并完整执行到 validate`

## 8. 建议的使用边界

- 本地联调阶段，优先给 MCP 使用只读数据库账号
- 需要执行 DDL、DML 时，再切换成具备写权限的账号
- 如果 HTTP 暴露到 `127.0.0.1` 之外，需要把 `bindHost` 改为非 loopback 地址并设置 `allowRemoteAccess: true`，同时建议放在受信网络或反向代理后面
- 不要同时启用 HTTP 和 STDIO

## 9. 常见问题

### 9.1 Codex 已注册，但调用不到

先检查：

```bash
codex mcp list
codex mcp get shardingsphere-mcp-local
```

如果配置存在但会话里还不可见，重启 Codex 或新开会话。

### 9.2 启动时报 JDBC driver 缺失

把对应 jar 放到：

```text
${DIST_DIR}/plugins/
```

然后重新启动 MCP runtime。

### 9.3 启动时报 transport 冲突

确保只有一种 transport 被启用：

- `stdio` 给 Codex 子进程接入
- `http` 给独立服务接入

### 9.4 启动后还是连到 H2 demo

说明你还在用默认 `conf/mcp.yaml`，或者没有把 Codex 指向你新建的配置文件。

## 10. 推荐落地顺序

1. 构建发行包
2. 准备 `mcp-codex-stdio.yaml`
3. 把目标数据库 driver 放到 `plugins/`
4. 用 `codex mcp add ... -- "${DIST_DIR}/bin/start.sh" ...` 注册
5. 重启 Codex 或新开会话
6. 先读取 `shardingsphere://databases` 和目标 table/view resource 验证元数据
7. 再用 `search_metadata` 和 `execute_query` 验证搜索与查询
8. 如果要接 Proxy 逻辑库，再用 `plan_*_rule -> apply_*_rule -> validate_*_rule` 验证 encrypt / mask workflow

如果你只是想本地把它跑通，`stdio + 单库只读账号` 是最省事、最稳的组合。
