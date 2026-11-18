+++
title = "MCP"
weight = 5
pre = "<b>10.5. </b>"
+++

使用 JDK 21 或更高版本，在源码仓库根目录执行以下命令。

## 打包

```bash
./mvnw -B -pl distribution/mcp -am \
  -Prelease,default-dep \
  -Dmaven.test.skip=true package
```

启用 `release` profile 后，`package` 会生成二进制发行包和 SHA-512 校验文件。
未启用 `release` 时，普通编译、测试和安装不会生成发行包。

## Profile 选择

`release` 控制发行打包，数据库依赖由 `default-dep`、`all` 或 `db-*` profile 选择。
显式启用 `release` 会使同一 POM 中 `activeByDefault` 的 `default-dep` 不再自动激活，因此默认发行构建需使用 `-Prelease,default-dep`。
定制数据库时，将其替换为 `-Prelease,db-postgresql`、其他已有 `db-*` 组合或 `-Prelease,all`。

### L3 数据库连接器

| 数据库        | Profile         | JDBC 驱动              |
|------------|-----------------|----------------------|
| MySQL      | `db-mysql`      | 需自行添加到 `plugins/`    |
| MariaDB    | `db-mariadb`    | 需自行添加到 `plugins/`    |
| PostgreSQL | `db-postgresql` | 随带 `postgresql`      |
| openGauss  | `db-opengauss`  | 随带 `opengauss-jdbc`  |
| Oracle     | `db-oracle`     | 需自行添加到 `plugins/`    |
| SQL Server | `db-sqlserver`  | 需自行添加到 `plugins/`    |
| Firebird   | `db-firebird`   | 需自行添加到 `plugins/`    |
| Hive       | `db-hive`       | 需自行添加到 `plugins/`    |
| Presto     | `db-presto`     | 随带 `presto-jdbc`     |
| ClickHouse | `db-clickhouse` | 随带 `clickhouse-jdbc` |

`all` 和 `db-firebird` profile 保留 Firebird 连接器，但不随包提供 Jaybird JDBC 驱动，需自行添加到 `plugins/`。

`all` 保留 Presto、ClickHouse 连接器，但不随带这两个 JDBC 驱动；如需随包提供驱动，可组合 `-Prelease,all,db-presto,db-clickhouse`，也可将驱动自行放入 `plugins/`。

### 依赖组合

| Profile       | 用途                                                                             |
|---------------|--------------------------------------------------------------------------------|
| `default-dep` | 包含 `db-mysql`、`db-postgresql`、`db-oracle`、`db-sqlserver`、`db-opengauss` 数据库连接器 |
| `all`         | 加入更多数据库连接器                                                                     |

## Docker 镜像

镜像通过 `distribution/mcp/Dockerfile` 构建，以 `distribution/mcp/target` 为构建上下文，需先生成 MCP 二进制发行包。
Dockerfile 在构建镜像时解压发行包。

## 制品

- 二进制发行包：`distribution/mcp/target/apache-shardingsphere-<version>-shardingsphere-mcp-bin.tar.gz`
- SHA-512 校验文件：与发行包同名，追加 `.sha512`。

将 `<version>` 和 `${version}` 替换为所构建源码的项目版本。

本地运行前先解压发行包：

```bash
tar -xzf distribution/mcp/target/apache-shardingsphere-${version}-shardingsphere-mcp-bin.tar.gz -C distribution/mcp/target
```

解压后的 `apache-shardingsphere-mcp-<version>/` 目录包含 `bin`、`conf`、`lib`、`plugins` 和 `logs`。

归档保留运行目录的名称、内容和启动脚本权限，并作为 `shardingsphere-mcp-bin` 附属制品参与 Maven `install`。
项目正式发布使用 `-Prelease,all`；默认构建和镜像仍使用 `-Prelease,default-dep`。
普通 `release package` 不要求签名密钥，正式发布的 `.asc` 签名由已有 `apache-release` 流程在 `verify` 阶段生成。
