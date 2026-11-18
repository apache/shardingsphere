+++
title = "MCP"
weight = 5
pre = "<b>10.5. </b>"
+++

Use JDK 21 or later and run the following command from the source repository root.

## Packaging

```bash
./mvnw -B -pl distribution/mcp -am \
  -Prelease,default-dep \
  -Dmaven.test.skip=true package
```

With the `release` profile enabled, `package` generates a standalone runtime directory.
Ordinary compilation, testing, and installation do not assemble the distribution directory without `release`.

## Profile Selection

The `release` profile controls distribution assembly, while `default-dep`, `all`, or `db-*` profiles select database dependencies.
Explicitly enabling `release` disables automatic activation of the `activeByDefault` profile `default-dep` in the same POM, so the default distribution build must use `-Prelease,default-dep`.
For a custom database selection, replace it with `-Prelease,db-postgresql`, another existing `db-*` combination, or `-Prelease,all`.

### L3 Database Connectors

| Database   | Profile         | JDBC Driver                     |
|------------|-----------------|---------------------------------|
| MySQL      | `db-mysql`      | Add separately under `plugins/` |
| MariaDB    | `db-mariadb`    | Add separately under `plugins/` |
| PostgreSQL | `db-postgresql` | Includes `postgresql`           |
| openGauss  | `db-opengauss`  | Includes `opengauss-jdbc`       |
| Oracle     | `db-oracle`     | Add separately under `plugins/` |
| SQL Server | `db-sqlserver`  | Add separately under `plugins/` |
| Firebird   | `db-firebird`   | Add separately under `plugins/` |
| Hive       | `db-hive`       | Add separately under `plugins/` |
| Presto     | `db-presto`     | Includes `presto-jdbc`          |
| ClickHouse | `db-clickhouse` | Includes `clickhouse-jdbc`      |

The `all` and `db-firebird` profiles retain the Firebird connector but do not bundle the Jaybird JDBC driver; add the driver separately under `plugins/`.

The `all` profile retains the Presto and ClickHouse connectors without bundling their JDBC drivers; use `-Prelease,all,db-presto,db-clickhouse` to bundle those drivers, or add them separately under `plugins/`.

### Dependency Sets

| Profile       | Purpose                                                                                                       |
|---------------|---------------------------------------------------------------------------------------------------------------|
| `default-dep` | Includes the `db-mysql`, `db-postgresql`, `db-oracle`, `db-sqlserver`, and `db-opengauss` database connectors |
| `all`         | Adds more database connectors                                                                                 |

## Docker Image

Build images with `distribution/mcp/Dockerfile`, using `distribution/mcp/target` as the build context after generating the MCP runtime directory.

## Artifacts

- Runtime directory: `distribution/mcp/target/apache-shardingsphere-mcp-<version>/`
- Binary distribution: `distribution/mcp/target/apache-shardingsphere-<version>-shardingsphere-mcp-bin.tar.gz`
- SHA-512 checksum: the binary distribution filename followed by `.sha512`.

`<version>` is the project version of the source being built.

The runtime directory contains `bin`, `conf`, `lib`, `plugins`, and `logs`.

The archive preserves the runtime directory name, contents, and startup script permissions, and participates in Maven `install` as the `shardingsphere-mcp-bin` attached artifact.
The project release uses `-Prelease,all`; default builds and images continue to use `-Prelease,default-dep`.
Ordinary `release package` requires no signing key; the existing `apache-release` process generates `.asc` signatures during `verify` for the formal release.
