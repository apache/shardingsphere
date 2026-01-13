+++
title = "Seata AT Mode transactions"
weight = 6
+++

## Background Information

ShardingSphere Proxy or ShardingSphere Proxy Native in the form of GraalVM Native Image do not provide support for Seata's AT mode by default.
Both support Seata's AT mode in optional modules.

This section is still limited by the documented content of [Seata transaction](/en/user-manual/shardingsphere-jdbc/special-api/transaction/seata) on the ShardingSphere JDBC side,
but there are some differences,

1. If the user uses ShardingSphere JDBC in a hybrid deployment architecture, this scenario does not directly interact with ShardingSphere Proxy and is therefore not relevant to this article.
   This article only discusses the scenario where the business project does not use ShardingSphere JDBC
2. Seata Client only exists in ShardingSphere Proxy, and the business project does not need to rely on Seata Client
3. The R2DBC DataSource of the business project can be normally connected to the ShardingSphere Proxy with Seata integration turned on
4. For ShardingSphere Proxy with Seata integration turned on, it is not possible to establish a `transaction propagation across services` operation to propagate transactions to other ShardingSphere Proxy instances using Seata integration or other microservices using Seata integration.
If users have such needs, they should consider submitting a PR for ShardingSphere
5. The assumptions made by ShardingSphere JDBC on Seata's TCC mode are invalid on ShardingSphere Proxy

The following discussion takes ShardingSphere Proxy using Seata Client 2.5.0 as an example.

## Operation steps

1. Confirm the JAR and dependency list of Seata Client
2. Start Seata Server
3. Create the `undo_log` table for the real database involved
4. Create ShardingSphere Proxy containing Seata Client and Seata integration module
5. Add Seata configuration to ShardingSphere Proxy

## Configuration example

### Confirm the JAR and dependency list of Seata Client


`OpenJDK` and `Maven` can be installed via `sdkman/sdkman-cli` or `version-fox/vfox`.
For `Ubuntu 24.04.3` or `Windows 11 Home 24H2` with `OpenJDK 23` and `Maven 3.9.11` installed, user can confirm all `compile` scope dependencies of Seata Client with the following command,

1. If using Bash,

```shell
mvn dependency:get "-Dartifact=org.apache.seata:seata-all:2.5.0"
mvn -f "${HOME}/.m2/repository/org/apache/seata/seata-all/2.5.0/seata-all-2.5.0.pom" dependency:tree | grep -v ':provided' | grep -v ':runtime'
```

2. If using PowerShell 7,

```shell
mvn dependency:get "-Dartifact=org.apache.seata:seata-all:2.5.0"
mvn -f "${HOME}/.m2/repository/org/apache/seata/seata-all/2.5.0/seata-all-2.5.0.pom" dependency:tree | Where-Object { $_ -notmatch ':provided' -and $_ -notmatch ':runtime' }
```

Compared with the `pom.xml` of `org.apache.shardingsphere:shardingsphere-proxy-distribution`, 
it is not difficult to find the differences listed as follows:

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

Obviously, users should always avoid paying attention to the outdated `org.antlr:antlr4-runtime:4.8`.
This list will be used to recreate the Docker Image of ShardingSphere Proxy.

In addition, users can always add Seata Client to the locally built Docker Image of ShardingSphere Proxy by modifying the source code of ShardingSphere.

### Start Seata Server, Postgres Server and ShardingSphere Proxy

Write the Docker Compose file to start Seata Server and Postgres Server.

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

In addition, 
users can always build the Docker Image of ShardingSphere Proxy in advance using Dockerfile instead of dynamically building the Docker Image in Docker Compose.

The `./conf` folder contains the file `global.yaml` with the following content,

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

The `./conf` folder contains the file `file.conf` with the following content,

```
service {
   vgroupMapping.default_tx_group = "default"
   default.grouplist = "apache-seata-server:8091"
}
```

The `./conf` folder contains the file `registry.conf` with the following contents,

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

The `./conf` folder contains the file `seata.conf`, the content of which is as follows,

```
client {
    application.id = test
    transaction.service.group = default_tx_group
}
```

The `./docker-entrypoint-initdb.d` folder contains the file `init.sh` with the following content,

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

### Create ShardingSphere virtual database

Use third-party tools to create ShardingSphere virtual database in ShardingSphere Proxy. 
Taking DBeaver Community as an example, if you use Ubuntu 24.04, you can quickly install it through Snapcraft.

```shell
sudo apt update && sudo apt upgrade -y
sudo snap install dbeaver-ce --classic
snap run dbeaver-ce
```

In DBeaver Community, use the `standardJdbcUrl` of `jdbc:postgresql://127.0.0.1:3308/postgres` to connect to ShardingSphere Proxy, 
and the username and password are both `root`. 
The required JDBC Driver corresponds to the `proxy-frontend-database-protocol-type` set by ShardingSphere Proxy.
Execute the following SQL,

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE sharding_db;
```

In DBeaver Community, use the `standardJdbcUrl` of `jdbc:postgresql://127.0.0.1:3308/sharding_db` to connect to ShardingSphere Proxy, 
and the username and password are both `root`. Execute the following SQL,

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

### Introduce Postgres JDBC Driver in the business project

Introduce Postgres JDBC Driver in the business project. 
The required JDBC Driver corresponds to the `proxy-frontend-database-protocol-type` set by ShardingSphere Proxy.

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.8</version>
</dependency>
```

### Enjoy the integration

Create ShardingSphere's data source through Postgres JDBC Driver to enjoy the integration.

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
