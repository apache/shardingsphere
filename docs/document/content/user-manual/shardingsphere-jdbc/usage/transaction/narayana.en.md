+++
title = "Narayana Transaction"
weight = 5
+++

## Import Maven Dependency

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

<!-- Import if using XA transaction -->
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

## Customize Configuration Items

Add `jbossts-properties.xml` in classpath of the application to customize Narayana configuration.

Please refer to [Narayana official documentation](https://narayana.io/documentation/index.html) for more details.

## Configure Transaction Manager Type

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
