+++
title = "Distributed Transaction"
weight = 3
+++

ShardingSphere-Proxy supports LOCAL, XA, BASE transactions, LOCAL transaction is default value, it is original transaction of relational database.

## XA transaction

Default XA transaction manager of ShardingSphere is Atomikos. Users can customize Atomikos configuration items through adding `jta.properties` in conf catalog of ShardingSphere-Proxy. Please refer to [Official Documents](https://www.atomikos.com/Documentation/JtaProperties) of Atomikos for detailed configurations.

* Use Narayana XA Transaction Manager。

1. Copy the jar file required by Narayana to `conf/lib`. The reference package is as follows:

```xml
<propeties>
    <narayana.version>5.9.1.Final</narayana.version>
    <jboss-transaction-spi.version>7.6.0.Final</jboss-transaction-spi.version>
    <jboss-logging.version>3.2.1.Final</jboss-logging.version>
</propeties>
<dependency>
    <groupId>org.jboss.narayana.jta</groupId>
    <artifactId>jta</artifactId>
    <version>${narayana.version}</version>
</dependency>
<dependency>
    <groupId>org.jboss.narayana.jts</groupId>
    <artifactId>narayana-jts-integration</artifactId>
    <version>${narayana.version}</version>
</dependency>
<dependency>
    <groupId>org.jboss</groupId>
    <artifactId>jboss-transaction-spi</artifactId>
    <version>${jboss-transaction-spi.version}</version>
</dependency>
<dependency>
    <groupId>org.jboss.logging</groupId>
    <artifactId>jboss-logging</artifactId>
    <version>${jboss-logging.version}</version>
</dependency>
```

2. Configure `transaction-manager-type` in `conf/server.yaml`：

```yaml
props:
  transaction-manager-type: narayana
```

3. Add `jbossts-properties.xml` to customize Narayana configuration.
The order of path loading is` user.dir (pwd)` > `user.home` > `java.home` > `classpath`.
Please refer to [Narayana official documentation](https://narayana.io/documentation/index.html) for more details.

## BASE Transaction

Since we have not packed the BASE implementation jar into ShardingSphere-Proxy, you should copy relevant jar which implement `ShardingTransactionManager` SPI to `conf/lib`, then switch the transaction type to `BASE`.
