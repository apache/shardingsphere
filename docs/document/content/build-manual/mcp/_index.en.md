+++
title = "MCP"
weight = 5
pre = "<b>10.5. </b>"
+++

Use JDK 21 or later and run the following command from the source repository root.

## Packaging

```bash
./mvnw -B -pl distribution/mcp -am \
  '-P<profiles>' \
  -Dmaven.test.skip=true package
```

`package` generates a standalone runtime directory without a `release` profile.

## Profile Selection

Replace `<profiles>` with the required profiles, separated by commas.

### L3 Database Connectors

| Database   | Profile         | JDBC Driver                     |
|------------|-----------------|---------------------------------|
| MySQL      | `db-mysql`      | Add separately under `plugins/` |
| MariaDB    | `db-mariadb`    | Add separately under `plugins/` |
| PostgreSQL | `db-postgresql` | Includes `postgresql`           |
| openGauss  | `db-opengauss`  | Includes `opengauss-jdbc`       |
| Oracle     | `db-oracle`     | Add separately under `plugins/` |
| SQL Server | `db-sqlserver`  | Add separately under `plugins/` |
| Firebird   | `db-firebird`   | Includes `jaybird`              |
| Hive       | `db-hive`       | Add separately under `plugins/` |
| Presto     | `db-presto`     | Includes `presto-jdbc`          |
| ClickHouse | `db-clickhouse` | Includes `clickhouse-jdbc`      |

### Dependency Sets

| Profile       | Purpose                                                                                                       |
|---------------|---------------------------------------------------------------------------------------------------------------|
| `default-dep` | Includes the `db-mysql`, `db-postgresql`, `db-oracle`, `db-sqlserver`, and `db-opengauss` database connectors |
| `all`         | Adds more database connectors                                                                                 |

## Docker Image

Build images with `distribution/mcp/Dockerfile`, using `distribution/mcp/target` as the build context after generating the MCP runtime directory.

## Artifacts

`distribution/mcp/target/apache-shardingsphere-mcp-<version>/`

`<version>` is the project version of the source being built.

The runtime directory contains `bin`, `conf`, `lib`, `plugins`, and `logs`.
