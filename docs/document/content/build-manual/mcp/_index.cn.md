+++
title = "MCP"
weight = 5
pre = "<b>10.5. </b>"
+++

使用 JDK 21 或更高版本，在源码仓库根目录执行以下命令。

## 打包

```bash
./mvnw -B -pl distribution/mcp -am \
  '-P<profiles>' \
  -Dmaven.test.skip=true package
```

`package` 生成独立运行目录，无需 `release` profile。

## Profile 选择

将 `<profiles>` 替换为所需 profile，多个值用逗号分隔。

### L3 数据库连接器

| 数据库        | Profile         | JDBC 驱动              |
|------------|-----------------|----------------------|
| MySQL      | `db-mysql`      | 需自行添加到 `plugins/`    |
| MariaDB    | `db-mariadb`    | 需自行添加到 `plugins/`    |
| PostgreSQL | `db-postgresql` | 随带 `postgresql`      |
| openGauss  | `db-opengauss`  | 随带 `opengauss-jdbc`  |
| Oracle     | `db-oracle`     | 需自行添加到 `plugins/`    |
| SQL Server | `db-sqlserver`  | 需自行添加到 `plugins/`    |
| Firebird   | `db-firebird`   | 随带 `jaybird`         |
| Hive       | `db-hive`       | 需自行添加到 `plugins/`    |
| Presto     | `db-presto`     | 随带 `presto-jdbc`     |
| ClickHouse | `db-clickhouse` | 随带 `clickhouse-jdbc` |

### 依赖组合

| Profile       | 用途                                                                             |
|---------------|--------------------------------------------------------------------------------|
| `default-dep` | 包含 `db-mysql`、`db-postgresql`、`db-oracle`、`db-sqlserver`、`db-opengauss` 数据库连接器 |
| `all`         | 加入更多数据库连接器                                                                     |

## Docker 镜像

镜像通过 `distribution/mcp/Dockerfile` 构建，以 `distribution/mcp/target` 为构建上下文，需先生成 MCP 运行目录。

## 制品

`distribution/mcp/target/apache-shardingsphere-mcp-<version>/`

`<version>` 为所构建源码的项目版本。

运行目录包含 `bin`、`conf`、`lib`、`plugins` 和 `logs`。
