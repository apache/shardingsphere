+++
title = "Narayana 事务"
weight = 5
+++

## 引入 Maven 依赖

```xml
<propeties>
        <narayana.version>5.9.1.Final</narayana.version>
        <jboss-transaction-spi.version>7.6.0.Final</jboss-transaction-spi.version>
        <jboss-logging.version>3.2.1.Final</jboss-logging.version>
</propeties>

<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- 使用 XA 事务时，需要引入此模块 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-xa-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<dependency>
      <groupId>org.apache.shardingsphere</groupId>
      <artifactId>shardingsphere-transaction-xa-narayana</artifactId>
      <version>${shardingsphere.version}</version>
</dependency>
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

## 定制化配置项

可以通过在项目的 classpath 中添加 `jbossts-properties.xml` 来定制化 Narayana 配置项。

详情请参见[Narayana官方文档](https://narayana.io/documentation/index.html)。

## 设置 XA 事务管理类型

Yaml:

```yaml
props:
  transaction-manager-type: Narayana
```

SpringBoot:

```yaml
spring:
  shardingsphere:
    props:
      transaction-manager-type: Narayana
```

Spring Namespace:

```xml
<shardingsphere:data-source id="xxx" data-source-names="xxx" rule-refs="xxx">
    <props>
        <prop key="transaction-manager-type">Narayana</prop>
    </props>
</shardingsphere:data-source>
```
