+++
title = "Proxy"
weight = 2
pre = "<b>10.2. </b>"
+++

使用 JDK 21 或更高版本，在源码仓库根目录执行以下命令。

## 打包

```bash
./mvnw -B -pl distribution/proxy -am \
  '-P!dev,release,<profiles>' \
  -Dmaven.test.skip=true package
```

## Profile 选择

将 `<profiles>` 替换为所需 profile，多个值用逗号分隔。

### L1 内核层

| 能力     | Profile                         | 包含的实现                             |
|--------|---------------------------------|-----------------------------------|
| 元数据    | `mode-repo-memory`              | Standalone 模式的 Memory 元数据存储       |
| 元数据    | `mode-repo-jdbc`                | Standalone 模式的 JDBC 元数据存储         |
| 元数据    | `mode-repo-zookeeper`           | Cluster 模式的 ZooKeeper 元数据存储与注册中心  |
| 元数据    | `mode-repo-etcd`                | Cluster 模式的 etcd 元数据存储与注册中心       |
| 权限     | `authority-simple`              | Simple 权限实现                       |
| 权限     | `authority-database`            | Database 权限实现                     |
| 事务     | `transaction-atomikos`          | Atomikos XA 事务管理器                 |
| 事务     | `transaction-narayana`          | Narayana XA 事务管理器                 |
| 事务     | `transaction-seata`             | Seata AT 事务实现                     |
| 联邦查询   | `sql-federation-calcite`        | CALCITE provider 和全部受支持的联邦查询数据库方言 |
| SQL 翻译 | `sql-translator-native`         | Native SQL 翻译 provider            |
| 时间服务   | `feature-database-time-service` | 数据库时间服务                           |

### L2 功能层

| 功能   | Profile                       |
|------|-------------------------------|
| 数据分片 | `feature-sharding`            |
| 广播表  | `feature-broadcast`           |
| 读写分离 | `feature-readwrite-splitting` |
| 数据加密 | `feature-encrypt`             |
| 数据脱敏 | `feature-mask`                |
| 影子库  | `feature-shadow`              |

### L3 数据库支持与适配

#### 数据库支持与随带适配

| 数据库        | Profile         | JDBC 驱动              |
|------------|-----------------|----------------------|
| MySQL      | `db-mysql`      | 需自行添加到 `ext-lib/`    |
| MariaDB    | `db-mariadb`    | 需自行添加到 `ext-lib/`    |
| PostgreSQL | `db-postgresql` | 随带 `postgresql`      |
| openGauss  | `db-opengauss`  | 随带 `opengauss-jdbc`  |
| Oracle     | `db-oracle`     | 需自行添加到 `ext-lib/`    |
| SQL Server | `db-sqlserver`  | 需自行添加到 `ext-lib/`    |
| Firebird   | `db-firebird`   | 随带 `jaybird`         |
| Hive       | `db-hive`       | 需自行添加到 `ext-lib/`    |
| Presto     | `db-presto`     | 随带 `presto-jdbc`     |
| ClickHouse | `db-clickhouse` | 随带 `clickhouse-jdbc` |
| Doris      | `db-doris`      | 需自行添加到 `ext-lib/`    |

#### 需要独立选择的数据库适配

| 适配的能力   | Profile                             | 同时包含的模块                                      |
|---------|-------------------------------------|----------------------------------------------|
| L1 联邦查询 | `sql-federation-calcite-mysql`      | CALCITE provider 与 MySQL 联邦查询方言              |
| L1 联邦查询 | `sql-federation-calcite-mariadb`    | CALCITE provider 与 MySQL 联邦查询方言，供 MariaDB 使用 |
| L1 联邦查询 | `sql-federation-calcite-postgresql` | CALCITE provider 与 PostgreSQL 联邦查询方言         |
| L1 联邦查询 | `sql-federation-calcite-opengauss`  | CALCITE provider 与 openGauss 联邦查询方言          |
| L1 联邦查询 | `sql-federation-calcite-oracle`     | CALCITE provider 与 Oracle 联邦查询方言             |
| L1 联邦查询 | `sql-federation-calcite-sqlserver`  | CALCITE provider 与 SQL Server 联邦查询方言         |
| L2 数据分片 | `feature-sharding-mysql`            | 分片功能与 MySQL 分片适配                             |

### 依赖组合

| Profile       | 用途                                                   |
|---------------|------------------------------------------------------|
| `default-dep` | 默认依赖组合                                               |
| `all`         | 全量依赖组合，加入更多数据库和可选实现，例如 Narayana、Seata、SQL 翻译与数据库时间服务 |
| `dev`         | Proxy bootstrap 的开发依赖组合，打包时用 `!dev` 禁用               |

#### default-dep 依赖组成

| 分组    | 对应 Profile                                                                                                                   |
|-------|------------------------------------------------------------------------------------------------------------------------------|
| 元数据   | `mode-repo-memory`、`mode-repo-jdbc`、`mode-repo-zookeeper`、`mode-repo-etcd`                                                   |
| 权限    | `authority-simple`、`authority-database`                                                                                      |
| 事务    | `transaction-atomikos`                                                                                                       |
| 联邦查询  | `sql-federation-calcite`                                                                                                     |
| 功能与适配 | `feature-sharding-mysql`、`feature-broadcast`、`feature-readwrite-splitting`、`feature-encrypt`、`feature-mask`、`feature-shadow` |
| 数据库   | `db-mysql`、`db-postgresql`、`db-opengauss`、`db-oracle`、`db-sqlserver`                                                         |

## Docker 镜像

| Profile  | 用途                                                   |
|----------|------------------------------------------------------|
| `docker` | 在 `<profiles>` 中追加此 profile 以构建 Docker 镜像，需要 Docker。 |

## 制品

`distribution/proxy/target/apache-shardingsphere-<version>-shardingsphere-proxy-bin.tar.gz`

`<version>` 为所构建源码的项目版本。

Proxy 发行包的 `lib` 包含运行时依赖，所选 Hive、Seata AT 模块位于 `opt-lib`，`ext-lib` 用于用户补充依赖。
