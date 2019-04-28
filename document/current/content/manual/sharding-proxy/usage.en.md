+++
pre = "<b>4.2.1. </b>"
toc = true
title = "Manual"
weight = 1
+++

## Proxy Initialization

1. Download the latest version of Sharding-Proxy.
2. If users use docker, they can implement `docker pull shardingsphere/sharding-proxy` to get the clone. Please refer to [Docker Clone](/en/manual/sharding-proxy/docker/) for more details.
3. After the decompression, revise `conf/server.yaml` and documents begin with `config-` prefix, `conf/config-xxx.yaml` for example, to configure sharding rules and read-write split rules. Please refer to [Configuration Manual](/en/manual/sharding-proxy/configuration/) for the configuration method.
4. Please run `bin/start.sh` for Linux operating system; run `bin/start.bat` for Windows operating system to start Sharding-Proxy. To configure start port and document location, please refer to [Quick Start](/en/quick-start/sharding-proxy-quick-start/).
5. Use any PostgreSQL server client end to connect, such as `psql -U root -h 127.0.0.1 -p 3307`.

## Use of Registry Center

If users want to use the database orchestration function of Sharding-Proxy, they need to implement instance disabling and slave database disabling functions in the registry center. Please refer to [Available Registry Centers](/en/features/orchestration/supported-registry-repo/) for more details.

### Zookeeper

1. Sharding-Proxy has provided the registry center solution of Zookeeper in default. Users only need to follow [Configuration Rules](/en/manual/sharding-proxy/configuration/) to set the registry center and use it.

### Etcd

1. Delete `sharding-orchestration-reg-zookeeper-curator-${sharding-sphere.version}.jar` under the lib catalog of Sharding-Proxy.
2. Download the jar package of [latest stable version](http://central.maven.org/maven2/io/shardingsphere/sharding-orchestration-reg-etcd/) of Etcd solution under Maven repository.
3. Put the downloaded jar package to the lib catalog of Sharding-Proxy.
4. Follow [Configuration Rules](/en/manual/sharding-proxy/configuration/) to set the registry center and use it.

### Other Third Party Registry Center

1. Delete`sharding-orchestration-reg-zookeeper-curator-${sharding-sphere.version}.jar` under the lib catalog of Sharding-Proxy.
2. Use SPI methods in logic coding and put the generated jar package to the lib catalog of Sharding-Proxy.
3. Follow [Configuration Rules](/en/manual/sharding-proxy/configuration/) to set the registry center and use it.

## Distributed Transactions

Sharding-Proxy natively supports XA transactions and there is no need for extra configurations.

### Configure Default Transaction Type

Default transaction type can be configured in `server.yaml`, for example:

```yaml
proxy.transaction.type: XA
```

### Shift Running Transaction Type

#### Command Line

```shell
postgres=# sctl: set transantcion_type=XA
postgres=# sctl: show transaction_type
```

#### Native JDBC

If users use JDBC-Driver method to connect Sharding-Proxy, users can send SQL transaction shift type of `sctl:set transaction_type=XA` after connection.

#### Spring Note Method

```java
@ShardingTransactionType(TransactionType.LOCAL)
@Transactional
```

Or

```java
@ShardingTransactionType(TransactionType.XA)
@Transactional
```

To be noticed: `@ShardingTransactionType` needs to be used together with `@Transactional` of Spring, and then transactions will take effect.

#### SpringBootStarter Usage

Introduce Maven Dependency:

```xml
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-transaction-jdbc-spring-boot-starter</artifactId>
    <version>${shardingsphere-spi-impl.version}</version>
</dependency>

<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>${aspectjweaver.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context-support</artifactId>
    <version>${springframework.version}</version>
</dependency>

<aspectjweaver.version>1.8.9</aspectjweaver.version>
<springframework.version>[4.3.6.RELEASE,5.0.0.M1)</springframework.version>
```

#### Spring Namespace Usage

Introduce Maven Dependency:

```xml
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-transaction-jdbc-spring</artifactId>
    <version>${shardingsphere-spi-impl.version}</version>
</dependency>

<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>${aspectjweaver.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context-support</artifactId>
    <version>${springframework.version}</version>
</dependency>

<aspectjweaver.version>1.8.9</aspectjweaver.version>
<springframework.version>[4.3.6.RELEASE,5.0.0.M1)</springframework.version>
```

Load section configuration information:

```xml
<import resource="classpath:META-INF/shardingTransaction.xml"/>
```

### Atomikos Parameter Configuration

Default XA transaction manager of ShardingSphere is Atomikos. Users can customize Atomikos configuration items through adding `jta.properties` in conf catelog of Sharding-Proxy. Please refer to [Official Documents](https://www.atomikos.com/Documentation/JtaProperties) of Atomikos for detailed configurations.

### BASE Transaction

Pack `sharding-transaction-base-spi-impl` module in [shardingsphere-spi-impl](en/shardingsphere-spi-impl) project; copy relevant jar packages to lib; switch the transaction type to`BASE`. The configuration of`saga.properties` is the same as that of JDBC.

## Notices

1. Sharding-Proxy uses 3307 port in default. Users can start the script parameter as the start port number, like `bin/start.sh 3308`.
2. Sharding-Proxy uses `conf/server.yaml` to configure the registry center, authentication information and public properties.
3. Sharding-Proxy supports multi-logic data source, with each yaml configuration document named by `config-` prefix as a logic data source.