+++
title = "HiveServer2"
weight = 6
+++

## 背景信息

ShardingSphere 默认情况下不提供对 `org.apache.hive.jdbc.HiveDriver` 的 `driverClassName` 的支持。
ShardingSphere 对 HiveServer2 JDBC Driver 的支持位于可选模块中。

## 前提条件

要在 ShardingSphere 的配置文件为数据节点使用类似 `jdbc:hive2://localhost:10000/` 的 `jdbcUrl`，
可能的 Maven 依赖关系如下，

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-infra-database-hive</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-parser-sql-hive</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.hive</groupId>
        <artifactId>hive-jdbc</artifactId>
        <version>4.0.1</version>
    </dependency>
    <dependency>
        <groupId>org.apache.hive</groupId>
        <artifactId>hive-service</artifactId>
        <version>4.0.1</version>
    </dependency>
</dependencies>
```

### 可选的解决依赖冲突的捷径

直接使用 `org.apache.hive:hive-jdbc:4.0.1` 会导致大量的依赖冲突。 
如果用户不希望手动解决潜在的数千行的依赖冲突，可以使用 HiveServer2 JDBC Driver 的 Thin JAR 的第三方构建。
可能的配置例子如下，

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-infra-database-hive</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-parser-sql-hive</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>io.github.linghengqian</groupId>
        <artifactId>hive-server2-jdbc-driver-thin</artifactId>
        <version>1.5.0</version>
        <exclusions>
            <exclusion>
                <groupId>com.fasterxml.woodstox</groupId>
                <artifactId>woodstox-core</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
</dependencies>
```

## 配置示例

### 启动 HiveServer2

编写 Docker Compose 文件来启动 HiveServer2。

```yaml
services:
    hive-server2:
        image: apache/hive:4.0.1
        environment:
          SERVICE_NAME: hiveserver2
        ports:
          - "10000:10000"
        expose:
          - 10002
```

### 创建业务表

通过第三方工具在 HiveServer2 内创建业务库与业务表。
以 DBeaver Community 为例，若使用 Ubuntu 22.04.4，可通过 Snapcraft 快速安装，

```shell
sudo apt update && sudo apt upgrade -y
sudo snap install dbeaver-ce
snap run dbeaver-ce
```

在 DBeaver Community 内使用 `jdbc:hive2://localhost:10000/` 的 `jdbcUrl` 连接至 HiveServer2，`username` 和 `password` 留空。

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;
```

分别使用 `jdbc:hive2://localhost:10000/demo_ds_0` ，
`jdbc:hive2://localhost:10000/demo_ds_1` 和 `jdbc:hive2://localhost:10000/demo_ds_2` 的 `jdbcUrl` 连接至 HiveServer2 来执行如下 SQL，

```sql
-- noinspection SqlNoDataSourceInspectionForFile
set iceberg.mr.schema.auto.conversion=true;

CREATE TABLE IF NOT EXISTS t_order
(
    order_id   BIGINT,
    order_type INT,
    user_id    INT    NOT NULL,
    address_id BIGINT NOT NULL,
    status     VARCHAR(50),
    PRIMARY KEY (order_id) disable novalidate
) STORED BY ICEBERG STORED AS ORC TBLPROPERTIES ('format-version' = '2');

TRUNCATE TABLE t_order;
```

### 在业务项目创建 ShardingSphere 数据源

在业务项目引入`前提条件`涉及的依赖后，在业务项目的 classpath 上编写 ShardingSphere 数据源的配置文件`demo.yaml`，

```yaml
dataSources:
    ds_0:
        dataSourceClassName: com.zaxxer.hikari.HikariDataSource
        driverClassName: org.apache.hive.jdbc.HiveDriver
        jdbcUrl: jdbc:hive2://localhost:10000/demo_ds_0
    ds_1:
        dataSourceClassName: com.zaxxer.hikari.HikariDataSource
        driverClassName: org.apache.hive.jdbc.HiveDriver
        jdbcUrl: jdbc:hive2://localhost:10000/demo_ds_1
    ds_2:
        dataSourceClassName: com.zaxxer.hikari.HikariDataSource
        driverClassName: org.apache.hive.jdbc.HiveDriver
        jdbcUrl: jdbc:hive2://localhost:10000/demo_ds_2
rules:
- !SHARDING
    tables:
      t_order:
        actualDataNodes:
        keyGenerateStrategy:
          column: order_id
          keyGeneratorName: snowflake
    defaultDatabaseStrategy:
      standard:
        shardingColumn: user_id
        shardingAlgorithmName: inline
    shardingAlgorithms:
      inline:
        type: INLINE
        props:
          algorithm-expression: ds_${user_id % 2}
    keyGenerators:
      snowflake:
        type: SNOWFLAKE
```

### 享受集成

创建 ShardingSphere 的数据源以享受集成，

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
public class ExampleUtils {
    void test() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:shardingsphere:classpath:demo.yaml");
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        try (HikariDataSource dataSource = new HikariDataSource(config);
             Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (1, 1, 1, 'INSERT_TEST')");
            statement.executeQuery("SELECT * FROM t_order");
            statement.execute("DELETE FROM t_order WHERE order_id=1");
        }
    }
}
```

## 使用限制

### 版本限制

HiveServer2 `2.x` 和 HiveServer2 `3.x` 发行版的生命周期已经结束。
参考 https://lists.apache.org/thread/0mh4hvpllzv877bkx1f9srv1c3hlbtt9 和 https://lists.apache.org/thread/mpzrv7v1hqqo4cmp0zorswnbvd7ltmbp 。
ShardingSphere 仅针对 HiveServer2 `4.0.1` 进行集成测试。

### HiveServer2 JDBC Driver 的 Uber JAR 限制

受 https://issues.apache.org/jira/browse/HIVE-28445 影响，
用户不应该使用 `classifier` 为 `standalone` 的 `org.apache.hive:hive-jdbc:4.0.1`，以避免依赖冲突。

### 嵌入式 HiveServer2 限制

嵌入式 HiveServer2 不再被 Hive 社区认为是用户友好的，用户不应该尝试通过 ShardingSphere 的配置文件启动 嵌入式 HiveServer2。
用户总应该通过 HiveServer2 的 Docker Image `apache/hive:4.0.1` 启动 HiveServer2。
参考 https://issues.apache.org/jira/browse/HIVE-28418 。

### Hadoop 限制

用户仅可使用 Hadoop `3.3.6` 来作为 HiveServer2 JDBC Driver `4.0.1` 的底层 Hadoop 依赖。
HiveServer2 JDBC Driver `4.0.1` 不支持 Hadoop `3.4.1`，
参考 https://github.com/apache/hive/pull/5500 。

### 数据库连接池限制

由于 `org.apache.hive.jdbc.DatabaseMetaData` 未实现 `java.sql.DatabaseMetaData#getURL()`， 
ShardingSphere 在`org.apache.shardingsphere.infra.database.DatabaseTypeEngine#getStorageType(javax.sql.DataSource)`处做了模糊处理，
因此用户暂时仅可通过 `com.zaxxer.hikari.HikariDataSource` 的数据库连接池连接 HiveServer2。

若用户需要通过 `com.alibaba.druid.pool.DruidDataSource` 的数据库连接池连接 HiveServer2，
用户应当考虑在 Hive 的主分支实现 `java.sql.DatabaseMetaData#getURL()`，
而不是尝试修改 ShardingSphere 的内部类。

### SQL 限制

ShardingSphere JDBC DataSource 尚不支持执行 HiveServer2 的 `SET` 语句，`CREATE TABLE` 语句和 `TRUNCATE TABLE` 语句。

用户应考虑为 ShardingSphere 提交包含单元测试的 PR。

### jdbcURL 限制

对于 ShardingSphere 的配置文件，对 HiveServer2 的 jdbcURL 存在限制。引入前提，
HiveServer2 的 jdbcURL 格式为 `jdbc:hive2://<host1>:<port1>,<host2>:<port2>/dbName;initFile=<file>;sess_var_list?hive_conf_list#hive_var_list`。
ShardingSphere 当前对参数的解析仅支持以`jdbc:hive2://localhost:10000/demo_ds_1;initFile=/tmp/init.sql`为代表的`;hive_conf_list`部分。

若用户需使用`;sess_var_list`或`#hive_var_list`的 jdbcURL 参数，考虑为 ShardingSphere 提交包含单元测试的 PR。

### 在 ShardingSphere 数据源上使用 DML SQL 语句的前提条件

为了能够使用 `delete` 等 DML SQL 语句，当连接到 HiveServer2 时，用户应当考虑在 ShardingSphere JDBC 中仅使用支持 ACID 的表。
`apache/hive` 提供了多种事务解决方案。

第1种选择是使用 ACID 表，可能的建表流程如下。
由于其过时的基于目录的表格式，用户可能不得不在 DML 语句执行前后进行等待，以让 HiveServer2 完成低效的 DML 操作。

```sql
-- noinspection SqlNoDataSourceInspectionForFile
set metastore.compactor.initiator.on=true;
set metastore.compactor.cleaner.on=true;
set metastore.compactor.worker.threads=5;

set hive.support.concurrency=true;
set hive.exec.dynamic.partition.mode=nonstrict;
set hive.txn.manager=org.apache.hadoop.hive.ql.lockmgr.DbTxnManager;

CREATE TABLE IF NOT EXISTS t_order
(
    order_id   BIGINT,
    order_type INT,
    user_id    INT    NOT NULL,
    address_id BIGINT NOT NULL,
    status     VARCHAR(50),
    PRIMARY KEY (order_id) disable novalidate
) CLUSTERED BY (order_id) INTO 2 BUCKETS STORED AS ORC TBLPROPERTIES ('transactional' = 'true');
```

第2种选择是使用 Iceberg 表，可能的建表流程如下。Apache Iceberg 表格式有望在未来几年取代传统的 Hive 表格式， 
参考 https://blog.cloudera.com/from-hive-tables-to-iceberg-tables-hassle-free/ 。

```sql
-- noinspection SqlNoDataSourceInspectionForFile
set iceberg.mr.schema.auto.conversion=true;

CREATE TABLE IF NOT EXISTS t_order
(
    order_id   BIGINT,
    order_type INT,
    user_id    INT    NOT NULL,
    address_id BIGINT NOT NULL,
    status     VARCHAR(50),
    PRIMARY KEY (order_id) disable novalidate
) STORED BY ICEBERG STORED AS ORC TBLPROPERTIES ('format-version' = '2');
```

Iceberg 表格式支持的 Hive type 相对较少，设置`iceberg.mr.schema.auto.conversion`为`true`有助于缓解这一问题。

### 事务限制

HiveServer2 不支持 ShardingSphere 集成级别的本地事务，XA 事务或 Seata 的 AT 模式事务，
更多讨论位于 https://cwiki.apache.org/confluence/display/Hive/Hive+Transactions 。

### DBeaver Community 版本限制

当用户使用 DBeaver Community 连接至 HiveServer2 时，需确保 DBeaver Community 版本大于或等于 `24.2.5`。
参考 https://github.com/dbeaver/dbeaver/pull/35059 。
