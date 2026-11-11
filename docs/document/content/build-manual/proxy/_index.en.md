+++
title = "Proxy"
weight = 2
pre = "<b>10.2. </b>"
+++

Use JDK 21 or later and run the following command from the source repository root.

## Packaging

```bash
./mvnw -B -pl distribution/proxy -am \
  '-P!dev,release,<profiles>' \
  -Dmaven.test.skip=true package
```

## Profile Selection

Replace `<profiles>` with the required profiles, separated by commas.

### L1 Kernel Layer

| Capability      | Profile                         | Included implementation                                             |
|-----------------|---------------------------------|---------------------------------------------------------------------|
| Metadata        | `mode-repo-memory`              | Memory metadata storage for Standalone mode                         |
| Metadata        | `mode-repo-jdbc`                | JDBC metadata storage for Standalone mode                           |
| Metadata        | `mode-repo-zookeeper`           | ZooKeeper metadata storage and registry center for Cluster mode     |
| Metadata        | `mode-repo-etcd`                | etcd metadata storage and registry center for Cluster mode          |
| Authority       | `authority-simple`              | Simple authority implementation                                     |
| Authority       | `authority-database`            | Database authority implementation                                   |
| Transaction     | `transaction-atomikos`          | Atomikos XA transaction manager                                     |
| Transaction     | `transaction-narayana`          | Narayana XA transaction manager                                     |
| Transaction     | `transaction-seata`             | Seata AT transaction implementation                                 |
| SQL Federation  | `sql-federation-calcite`        | CALCITE provider and all supported SQL Federation database dialects |
| SQL translation | `sql-translator-native`         | Native SQL translation provider                                     |
| Time service    | `feature-database-time-service` | Database time service                                               |

### L2 Feature Layer

| Feature              | Profile                       |
|----------------------|-------------------------------|
| Data sharding        | `feature-sharding`            |
| Broadcast tables     | `feature-broadcast`           |
| Read/write splitting | `feature-readwrite-splitting` |
| Data encryption      | `feature-encrypt`             |
| Data masking         | `feature-mask`                |
| Shadow database      | `feature-shadow`              |

### L3 Database Support and Adaptation

#### Database Support and Included Adaptations

| Database   | Profile         | JDBC Driver                     |
|------------|-----------------|---------------------------------|
| MySQL      | `db-mysql`      | Add separately under `ext-lib/` |
| MariaDB    | `db-mariadb`    | Add separately under `ext-lib/` |
| PostgreSQL | `db-postgresql` | Includes `postgresql`           |
| openGauss  | `db-opengauss`  | Includes `opengauss-jdbc`       |
| Oracle     | `db-oracle`     | Add separately under `ext-lib/` |
| SQL Server | `db-sqlserver`  | Add separately under `ext-lib/` |
| Firebird   | `db-firebird`   | Includes `jaybird`              |
| Hive       | `db-hive`       | Add separately under `ext-lib/` |
| Presto     | `db-presto`     | Includes `presto-jdbc`          |
| ClickHouse | `db-clickhouse` | Includes `clickhouse-jdbc`      |
| Doris      | `db-doris`      | Add separately under `ext-lib/` |

#### Database Adaptations Requiring Explicit Selection

| Adapted capability | Profile                             | Modules included together                                     |
|--------------------|-------------------------------------|---------------------------------------------------------------|
| L1 SQL Federation  | `sql-federation-calcite-mysql`      | CALCITE provider and MySQL SQL Federation dialect             |
| L1 SQL Federation  | `sql-federation-calcite-mariadb`    | CALCITE provider and MySQL SQL Federation dialect for MariaDB |
| L1 SQL Federation  | `sql-federation-calcite-postgresql` | CALCITE provider and PostgreSQL SQL Federation dialect        |
| L1 SQL Federation  | `sql-federation-calcite-opengauss`  | CALCITE provider and openGauss SQL Federation dialect         |
| L1 SQL Federation  | `sql-federation-calcite-oracle`     | CALCITE provider and Oracle SQL Federation dialect            |
| L1 SQL Federation  | `sql-federation-calcite-sqlserver`  | CALCITE provider and SQL Server SQL Federation dialect        |
| L2 data sharding   | `feature-sharding-mysql`            | Sharding features and MySQL sharding adaptation               |

### Dependency Sets

| Profile       | Purpose                                                                                                                                      |
|---------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| `default-dep` | Default dependency set                                                                                                                       |
| `all`         | All dependencies, adding more databases and optional implementations such as Narayana, Seata, SQL translation, and the database time service |
| `dev`         | Proxy bootstrap's development dependency set; deactivate with `!dev` when packaging                                                          |

#### default-dep Dependencies

| Group                   | Corresponding profiles                                                                                                            |
|-------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| Metadata                | `mode-repo-memory`, `mode-repo-jdbc`, `mode-repo-zookeeper`, `mode-repo-etcd`                                                     |
| Authority               | `authority-simple`, `authority-database`                                                                                          |
| Transaction             | `transaction-atomikos`                                                                                                            |
| SQL Federation          | `sql-federation-calcite`                                                                                                          |
| Features and adaptation | `feature-sharding-mysql`, `feature-broadcast`, `feature-readwrite-splitting`, `feature-encrypt`, `feature-mask`, `feature-shadow` |
| Databases               | `db-mysql`, `db-postgresql`, `db-opengauss`, `db-oracle`, `db-sqlserver`                                                          |

## Docker Image

| Profile  | Purpose                                                                       |
|----------|-------------------------------------------------------------------------------|
| `docker` | Append this profile to `<profiles>` to build a Docker image; requires Docker. |

## Artifacts

`distribution/proxy/target/apache-shardingsphere-<version>-shardingsphere-proxy-bin.tar.gz`

`<version>` is the project version of the source being built.

The Proxy archive's `lib` directory contains runtime dependencies, selected Hive and Seata AT modules are placed in `opt-lib`, and `ext-lib` is for user-supplied dependencies.
