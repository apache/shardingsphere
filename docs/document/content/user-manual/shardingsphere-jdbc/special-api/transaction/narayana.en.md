+++
title = "Narayana Transaction"
weight = 5
+++

## Background

Apache ShardingSphere provides XA transactions that integrate with the Narayana implementation.

## Prerequisites

Introducing Maven dependency

```xml
<properties>
    <narayana.version>5.12.7.Final</narayana.version>
    <jboss-transaction-spi.version>7.6.1.Final</jboss-transaction-spi.version>
    <jboss-logging.version>3.2.1.Final</jboss-logging.version>
</properties>

<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- This module is required when using XA transactions -->
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
## Procedure
1. Configure Narayana
2. Set the XA transaction type

## Sample

### Configure Narayana

Narayana configuration items can be customized by adding `jbossts-properties.xml` to the project's classpath.

See [Narayana's Official Documentation](https://narayana.io/documentation/) for more details.

For the minimum configuration of `jbossts-properties.xml`,
ShardingSphere requires that Narayana's `CoreEnvironmentBean.nodeIdentifier` property be defined.
If Narayana 's object store is not shared between different Narayana instances, you can set this value to `1`.
A possible `jbossts-properties.xml` configuration is as follows,

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
    <entry key="CoreEnvironmentBean.nodeIdentifier">1</entry>
</properties>
```

In certain cases, you may not want to use XML files,
then you need to manually set `CoreEnvironmentBean.nodeIdentifier` in the bootstrap class of your own Java project.
You can refer to the following methods to call Narayana Java API.

```java
import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;
import com.arjuna.ats.arjuna.common.arjPropertyManager;

public class ExampleUtils {
    public void initNarayanaInstance() {
        try {
            arjPropertyManager.getCoreEnvironmentBean().setNodeIdentifier("1");
        } catch (CoreEnvironmentBeanException e) {
            throw new RuntimeException(e);
        }
    }
}
```

### Set the XA transaction type

Yaml:

```yaml
transaction:
  defaultType: XA
  providerType: Narayana
```
