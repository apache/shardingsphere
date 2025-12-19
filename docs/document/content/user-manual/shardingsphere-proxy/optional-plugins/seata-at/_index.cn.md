+++
title = "Seata AT 模式事务"
weight = 6
+++

## 背景信息

ShardingSphere Proxy 或 GraalVM Native Image 形态的 ShardingSphere Proxy Native 默认情况下不提供对 Seata 的 AT 模式的支持。 
两者对 Seata 的 AT 模式的支持均位于可选模块中。

本节依然受到 ShardingSphere JDBC 一侧的 [Seata 事务](/cn/user-manual/shardingsphere-jdbc/special-api/transaction/seata) 的已记录内容的限制，
但有所不同，

1. 若用户采用混合部署架构使用 ShardingSphere JDBC，此情景不直接与 ShardingSphere Proxy 交互，因此与本文无关。
   本文仅讨论业务项目不使用 ShardingSphere JDBC 的场景
2. Seata Client 只存在于 ShardingSphere Proxy 中，业务项目并不需要依赖 Seata Client
3. 业务项目的 R2DBC DataSource 可以正常连接至开启 Seata 集成的 ShardingSphere Proxy
4. 对于开启 Seata 集成的 ShardingSphere Proxy，无法通过建立 `跨服务的事务传播` 的操作，
   传播事务到其他使用 Seata 集成的 ShardingSphere Proxy 实例或其他使用 Seata 集成的微服务。用户如果有这种需求，应考虑为 ShardingSphere 提交 PR
5. ShardingSphere JDBC 对 Seata 的 TCC 模式建立的假设，在 ShardingSphere Proxy 上失效

下文以使用 Seata Client 2.5.0 的 ShardingSphere Proxy 为例讨论。

## 操作步骤

1. 确认 Seata Client 的 JAR 和依赖列表
2. 启动 Seata Server
3. 为所涉及的真实数据库创建 `undo_log` 表
4. 创建包含 Seata Client 和 Seata 集成模块的 ShardingSphere Proxy
5. 为 ShardingSphere Proxy 添加 Seata 配置

## 配置示例

### 确认 Seata Client 的 JAR 和依赖列表

`OpenJDK` 和 `Maven` 可以通过 `sdkman/sdkman-cli` 或 `version-fox/vfox` 安装。
对于已安装 `OpenJDK 23` 和 `Maven 3.9.11` 的 `Ubuntu 24.04.3` 或 `Windows 11 Home 24H2`，可以以如下命令确认 Seata Client 的所有 `compile` scope 的依赖，

1. 如果使用 Bash，

```shell
mvn dependency:get "-Dartifact=org.apache.seata:seata-all:2.5.0"
mvn -f "${HOME}/.m2/repository/org/apache/seata/seata-all/2.5.0/seata-all-2.5.0.pom" dependency:tree | grep -v ':provided' | grep -v ':runtime'
```

2. 如果使用 PowerShell 7，

```shell
mvn dependency:get "-Dartifact=org.apache.seata:seata-all:2.5.0"
mvn -f "${HOME}/.m2/repository/org/apache/seata/seata-all/2.5.0/seata-all-2.5.0.pom" dependency:tree | Where-Object { $_ -notmatch ':provided' -and $_ -notmatch ':runtime' }
```

与 `org.apache.shardingsphere:shardingsphere-proxy-distribution` 的 `pom.xml` 对比，不难发现有差异列表为，

```
org.apache.seata:seata-all:jar:2.5.0
org.springframework:spring-context:jar:5.3.39
org.springframework:spring-expression:jar:5.3.39
org.springframework:spring-core:jar:5.3.39
org.springframework:spring-jcl:jar:5.3.39
org.springframework:spring-beans:jar:5.3.39
org.springframework:spring-aop:jar:5.3.39
org.springframework:spring-webmvc:jar:5.3.39
org.springframework:spring-web:jar:5.3.39
org.springframework:spring-tx:jar:5.3.39
io.netty:netty-all:jar:4.1.101.Final
io.netty:netty-codec-dns:jar:4.1.101.Final
io.netty:netty-codec-haproxy:jar:4.1.101.Final
io.netty:netty-codec-memcache:jar:4.1.101.Final
io.netty:netty-codec-mqtt:jar:4.1.101.Final
io.netty:netty-codec-redis:jar:4.1.101.Final
io.netty:netty-codec-smtp:jar:4.1.101.Final
io.netty:netty-codec-stomp:jar:4.1.101.Final
io.netty:netty-codec-xml:jar:4.1.101.Final
io.netty:netty-handler-ssl-ocsp:jar:4.1.101.Final
io.netty:netty-resolver-dns:jar:4.1.101.Final
io.netty:netty-transport-rxtx:jar:4.1.101.Final
io.netty:netty-transport-sctp:jar:4.1.101.Final
io.netty:netty-transport-udt:jar:4.1.101.Final
io.netty:netty-transport-classes-kqueue:jar:4.1.101.Final
io.netty:netty-resolver-dns-classes-macos:jar:4.1.101.Final
org.antlr:antlr4:jar:4.8
org.antlr:antlr-runtime:jar:3.5.2
org.antlr:ST4:jar:4.3
org.abego.treelayout:org.abego.treelayout.core:jar:1.0.3
org.glassfish:javax.json:jar:1.0.4
com.ibm.icu:icu4j:jar:61.1
com.alibaba:fastjson:jar:1.2.83
com.alibaba:druid:jar:1.2.20
com.typesafe:config:jar:1.2.1
commons-pool:commons-pool:jar:1.6
org.apache.dubbo.extensions:dubbo-filter-seata:jar:1.0.2
aopalliance:aopalliance:jar:1.0
```

显然，用户始终应该避免关注过时的 `org.antlr:antlr4-runtime:4.8`。此列表将用于重新创建 ShardingSphere Proxy 的 Docker Image。

此外，用户始终可以通过修改 ShardingSphere 的源代码来将 Seata Client 添加进本地构建的 ShardingSphere Proxy 的 Docker Image 中。

### 启动 Seata Server，Postgres Server 和 ShardingSphere Proxy

编写 Docker Compose 文件来启动 Seata Server 和 Postgres Server。

```yaml
services:
   postgres:
      image: postgres:17.5-bookworm
      environment:
         POSTGRES_PASSWORD: example
      volumes:
         - ./docker-entrypoint-initdb.d:/docker-entrypoint-initdb.d
   apache-seata-server:
      image: apache/seata-server:2.5.0
      healthcheck:
        test: [ "CMD", "sh", "-c", "curl -s apache-seata-server:8091/health | grep -q '\"ok\"'" ]
   shardingsphere-proxy-custom:
      image: example/shardingsphere-proxy-custom:latest
      pull_policy: build
      build:
         context: .
         dockerfile_inline: |
            FROM apache/shardingsphere-proxy:latest
            RUN wget https://repo1.maven.org/maven2/org/apache/shardingsphere/shardingsphere-transaction-base-seata-at/5.5.2/shardingsphere-transaction-base-seata-at-5.5.2.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/org/apache/seata/seata-all/2.5.0/seata-all-2.5.0.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/org/springframework/spring-context/5.3.39/spring-context-5.3.39.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/org/springframework/spring-expression/5.3.39/spring-expression-5.3.39.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/org/springframework/spring-core/5.3.39/spring-core-5.3.39.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/org/springframework/spring-jcl/5.3.39/spring-jcl-5.3.39.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/org/springframework/spring-beans/5.3.39/spring-beans-5.3.39.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/org/springframework/spring-aop/5.3.39/spring-aop-5.3.39.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/org/springframework/spring-webmvc/5.3.39/spring-webmvc-5.3.39.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/org/springframework/spring-web/5.3.39/spring-web-5.3.39.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/org/springframework/spring-tx/5.3.39/spring-tx-5.3.39.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/io/netty/netty-all/4.1.101.Final/netty-all-4.1.101.Final.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/io/netty/netty-codec-dns/4.1.101.Final/netty-codec-dns-4.1.101.Final.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/io/netty/netty-codec-haproxy/4.1.101.Final/netty-codec-haproxy-4.1.101.Final.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/io/netty/netty-codec-memcache/4.1.101.Final/netty-codec-memcache-4.1.101.Final.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/io/netty/netty-codec-mqtt/4.1.101.Final/netty-codec-mqtt-4.1.101.Final.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/io/netty/netty-codec-redis/4.1.101.Final/netty-codec-redis-4.1.101.Final.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/io/netty/netty-codec-smtp/4.1.101.Final/netty-codec-smtp-4.1.101.Final.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/io/netty/netty-codec-stomp/4.1.101.Final/netty-codec-stomp-4.1.101.Final.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/io/netty/netty-codec-xml/4.1.101.Final/netty-codec-xml-4.1.101.Final.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/io/netty/netty-handler-ssl-ocsp/4.1.101.Final/netty-handler-ssl-ocsp-4.1.101.Final.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/io/netty/netty-resolver-dns/4.1.101.Final/netty-resolver-dns-4.1.101.Final.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/io/netty/netty-transport-rxtx/4.1.101.Final/netty-transport-rxtx-4.1.101.Final.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/io/netty/netty-transport-sctp/4.1.101.Final/netty-transport-sctp-4.1.101.Final.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/io/netty/netty-transport-udt/4.1.101.Final/netty-transport-udt-4.1.101.Final.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/io/netty/netty-transport-classes-kqueue/4.1.101.Final/netty-transport-classes-kqueue-4.1.101.Final.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/io/netty/netty-resolver-dns-classes-macos/4.1.101.Final/netty-resolver-dns-classes-macos-4.1.101.Final.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/org/antlr/antlr4/4.8/antlr4-4.8.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/org/antlr/antlr-runtime/3.5.2/antlr-runtime-3.5.2.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/org/antlr/ST4/4.3/ST4-4.3.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/org/abego/treelayout/org.abego.treelayout.core/1.0.3/org.abego.treelayout.core-1.0.3.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/org/glassfish/javax.json/1.0.4/javax.json-1.0.4.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/com/ibm/icu/icu4j/61.1/icu4j-61.1.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/com/alibaba/fastjson/1.2.83/fastjson-1.2.83.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/com/alibaba/druid/1.2.20/druid-1.2.20.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo.akka.io/maven/com/typesafe/config/1.2.1/config-1.2.1.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/commons-pool/commons-pool/1.6/commons-pool-1.6.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/org/apache/dubbo/extensions/dubbo-filter-seata/1.0.2/dubbo-filter-seata-1.0.2.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
            RUN wget https://repo1.maven.org/maven2/aopalliance/aopalliance/1.0/aopalliance-1.0.jar --directory-prefix=/opt/shardingsphere-proxy/ext-lib
      volumes:
         - ./conf:/opt/shardingsphere-proxy/conf
      ports:
         - "3308:3308"
      environment:
         PORT: 3308
      depends_on:
         apache-seata-server:
            condition: service_healthy
```

此外，用户始终可以提前利用 Dockerfile 构建 ShardingSphere Proxy 的 Docker Image，而不是在 Docker Compose 中动态构建 Docker Image。

`./conf` 文件夹包含文件 `global.yaml`，内容如下，

```yaml
authority:
  users:
    - user: root@%
      password: root
      admin: true
  privilege:
    type: ALL_PERMITTED
transaction:
  defaultType: BASE
  providerType: Seata
props:
  proxy-frontend-database-protocol-type: PostgreSQL
```

`./conf` 文件夹包含文件 `file.conf`，内容如下，

```
service {
   vgroupMapping.default_tx_group = "default"
   default.grouplist = "apache-seata-server:8091"
}
```

`./conf` 文件夹包含文件 `registry.conf`，内容如下，

```
registry {
  type = "file"
  file {
    name = "file.conf"
  }
}
config {
  type = "file"
  file {
    name = "file.conf"
  }
}
```

`./conf` 文件夹包含文件 `seata.conf`，内容如下，

```
client {
    application.id = test
    transaction.service.group = default_tx_group
}
```

`./docker-entrypoint-initdb.d` 文件夹包含文件 `init.sh`，内容如下，

```shell
#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
  CREATE DATABASE demo_ds_0;
  CREATE DATABASE demo_ds_1;
  CREATE DATABASE demo_ds_2;
EOSQL

for i in "demo_ds_0" "demo_ds_1" "demo_ds_2"
do
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$i" <<-EOSQL
  CREATE TABLE IF NOT EXISTS public.undo_log
  (
      id            SERIAL       NOT NULL,
      branch_id     BIGINT       NOT NULL,
      xid           VARCHAR(128) NOT NULL,
      context       VARCHAR(128) NOT NULL,
      rollback_info BYTEA        NOT NULL,
      log_status    INT          NOT NULL,
      log_created   TIMESTAMP(0) NOT NULL,
      log_modified  TIMESTAMP(0) NOT NULL,
      CONSTRAINT pk_undo_log PRIMARY KEY (id),
      CONSTRAINT ux_undo_log UNIQUE (xid, branch_id)
      );
  CREATE INDEX ix_log_created ON undo_log(log_created);
  COMMENT ON TABLE public.undo_log IS 'AT transaction mode undo table';
  COMMENT ON COLUMN public.undo_log.branch_id IS 'branch transaction id';
  COMMENT ON COLUMN public.undo_log.xid IS 'global transaction id';
  COMMENT ON COLUMN public.undo_log.context IS 'undo_log context,such as serialization';
  COMMENT ON COLUMN public.undo_log.rollback_info IS 'rollback info';
  COMMENT ON COLUMN public.undo_log.log_status IS '0:normal status,1:defense status';
  COMMENT ON COLUMN public.undo_log.log_created IS 'create datetime';
  COMMENT ON COLUMN public.undo_log.log_modified IS 'modify datetime';
  CREATE SEQUENCE IF NOT EXISTS undo_log_id_seq INCREMENT BY 1 MINVALUE 1 ;

  CREATE TABLE IF NOT EXISTS t_order (
      order_id BIGSERIAL NOT NULL PRIMARY KEY,
      order_type INTEGER,
      user_id INTEGER NOT NULL,
      address_id BIGINT NOT NULL,
      status VARCHAR(50)
  );
EOSQL
done
```

### 创建 ShardingSphere 虚拟库

通过第三方工具在 ShardingSphere Proxy 内创建 ShardingSphere 虚拟库。
以 DBeaver Community 为例，若使用 Ubuntu 22.04.4，可通过 Snapcraft 快速安装，

```shell
sudo apt update && sudo apt upgrade -y
sudo snap install dbeaver-ce
snap run dbeaver-ce
```

在 DBeaver Community 内，使用 `jdbc:postgresql://127.0.0.1:3308/postgres` 的 `standardJdbcUrl` 连接至 ShardingSphere Proxy，
username 和 password 均为 `root`。所需的 JDBC Driver 与 ShardingSphere Proxy 设置的 `proxy-frontend-database-protocol-type` 对应。
执行如下 SQL，

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE sharding_db;
```

在 DBeaver Community 内，使用 `jdbc:postgresql://127.0.0.1:3308/sharding_db` 的 `standardJdbcUrl` 连接至 ShardingSphere Proxy，
username 和 password 均为 `root`。
执行如下 SQL，

```sql
-- noinspection SqlNoDataSourceInspectionForFile
REGISTER STORAGE UNIT ds_0 (
  URL="jdbc:postgresql://postgres:5432/demo_ds_0",
  USER="postgres",
  PASSWORD="example"
),ds_1 (
  URL="jdbc:postgresql://postgres:5432/demo_ds_1",
  USER="postgres",
  PASSWORD="example"
),ds_2 (
  URL="jdbc:postgresql://postgres:5432/demo_ds_2",
  USER="postgres",
  PASSWORD="example"
);
         
CREATE DEFAULT SHARDING DATABASE STRATEGY (
  TYPE="standard", 
  SHARDING_COLUMN=user_id, 
  SHARDING_ALGORITHM(
    TYPE(
      NAME=INLINE, 
      PROPERTIES(
        "algorithm-expression"="ds_${user_id % 2}"
      )
    )
  )
);
       
CREATE SHARDING TABLE RULE t_order (
  DATANODES("ds_$->{0..2}.t_order"),
  KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="SNOWFLAKE"))
);
```

### 在业务项目引入 Postgres JDBC Driver

在业务项目引入 Postgres JDBC Driver。所需的 JDBC Driver 与 ShardingSphere Proxy 设置的 `proxy-frontend-database-protocol-type` 对应。

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.8</version>
</dependency>
```

### 享受集成

通过 Postgres JDBC Driver 创建 ShardingSphere 的数据源以享受集成。

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
@SuppressWarnings("SqlNoDataSourceInspection")
public class ExampleUtils {
    void test() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://127.0.0.1:3308/sharding_db");
        config.setDriverClassName("org.postgresql.Driver");
        try (HikariDataSource dataSource = new HikariDataSource(config);
             Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (1, 1, 1, 'INSERT_TEST')");
            statement.executeQuery("SELECT * FROM t_order");
            statement.execute("DELETE FROM t_order WHERE user_id=1");
        }
    }
}
```
