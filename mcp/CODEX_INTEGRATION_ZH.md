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

如果目标数据库驱动没有被发行包自带，请把 driver jar 放到发行包目录的 `ext-lib/` 下。

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

### 单库模板

```yaml
transport:
  http:
    enabled: false
    bindHost: 127.0.0.1
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

### 6.2 启动 MCP runtime

```bash
"${DIST_DIR}/bin/start.sh" "${DIST_DIR}/conf/mcp-codex-http.yaml"
```

### 6.3 把 HTTP server 注册到 Codex

```bash
codex mcp add shardingsphere-mcp-http --url http://127.0.0.1:18088/mcp
```

对应的 `~/.codex/config.toml` 结构如下：

```toml
[mcp_servers.shardingsphere-mcp-http]
url = "http://127.0.0.1:18088/mcp"
```

这个模式更适合单独排查日志、抓包和做协议联调；日常本地使用仍建议优先 `stdio`。

## 7. Codex 里怎么用

ShardingSphere MCP 当前暴露的核心工具包括：

- `list_databases`
- `list_schemas`
- `list_tables`
- `list_views`
- `list_columns`
- `list_indexes`
- `search_metadata`
- `describe_table`
- `describe_view`
- `get_capabilities`
- `execute_query`

你可以直接在 Codex 里这样提问：

- `列出 shardingsphere-mcp-local 里所有逻辑库`
- `查看 local_db 的 public schema 下有哪些表`
- `描述 local_db.public.t_order 的表结构`
- `执行 SQL: SELECT * FROM t_order ORDER BY order_id DESC LIMIT 10`
- `先检查 local_db 支持哪些能力，再帮我查最近 10 条订单`

如果你配置了多库，也可以明确指定逻辑库名，例如：

- `查询 analytics_db.public.events 最近 20 条数据`

## 8. 建议的使用边界

- 本地联调阶段，优先给 MCP 使用只读数据库账号
- 需要执行 DDL、DML 时，再切换成具备写权限的账号
- 如果 HTTP 暴露到 `127.0.0.1` 之外，建议放在受信网络或反向代理后面
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
${DIST_DIR}/ext-lib/
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
3. 把目标数据库 driver 放到 `ext-lib/`
4. 用 `codex mcp add ... -- "${DIST_DIR}/bin/start.sh" ...` 注册
5. 重启 Codex 或新开会话
6. 先用 `list_databases`、`describe_table` 验证元数据
7. 再用 `execute_query` 验证查询

如果你只是想本地把它跑通，`stdio + 单库只读账号` 是最省事、最稳的组合。
