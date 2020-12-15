+++
title = "分布式事务"
weight = 3
+++

ShardingSphere-Proxy 接入的分布式事务 API 同 ShardingSphere-JDBC 保持一致，支持 LOCAL，XA，BASE 类型的事务。

## XA 事务

* ShardingSphere-Proxy 原生支持 XA 事务，默认的事务管理器为 Atomikos。
可以通过在 ShardingSphere-Proxy 的 `conf` 目录中添加 `jta.properties` 来定制化 Atomikos 配置项。
具体的配置规则请参考 Atomikos 的[官方文档](https://www.atomikos.com/Documentation/JtaProperties)。

* 使用 Narayana事务管理器，需要参考以下步骤。

1. 将 Narayana 所需 jar 拷贝至 `/lib` 目录。参考如下：

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
 
2. 在 `conf/server.yaml` 中加入如下配置：

```yaml
props:
   transaction-manager-type: narayana
```

3. 新增 `jbossts-properties.xml` 文件来定制化 Narayana 配置项，它的加载路径顺序：`user.dir (pwd)` > `user.home` > `java.home` > `classpath`。
详情请参见[Narayana官方文档](https://narayana.io/documentation/index.html)。


## BASE 事务

BASE 目前没有集成至 ShardingSphere-Proxy 的二进制发布包中，使用时需要将实现了 `ShardingTransactionManager` SPI 的 jar 拷贝至 `conf/lib` 目录，然后切换事务类型为 BASE。
