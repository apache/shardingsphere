+++
title = "Narayana 事务"
weight = 5
+++

## 背景信息

Apache ShardingSphere 提供 XA 事务，集成了 Narayana 的实现。

## 前提条件

引入 Maven 依赖

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
## 操作步骤

1. 配置 Narayana
2. 设置 XA 事务类型

## 配置示例

### 配置 Narayana

可以通过在项目的 classpath 中添加 `jbossts-properties.xml` 来定制化 Narayana 配置项。

详情请参见 [Narayana 官方文档](https://narayana.io/documentation/) 。

对于 `jbossts-properties.xml` 的最小配置，ShardingSphere 要求定义 Narayana 的 `CoreEnvironmentBean.nodeIdentifier` 属性。
如果 Narayana 的 object store 并非在不同的 Narayana 实例之间共享，你可以将此值设置为 `1`。一个可能的 `jbossts-properties.xml` 配置如下，

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
    <entry key="CoreEnvironmentBean.nodeIdentifier">1</entry>
</properties>
```

在特定情况下，你可能不会希望使用 XML 文件，那你需要在自有 Java 项目的启动类手动设置 `CoreEnvironmentBean.nodeIdentifier`。
可参考如下方法调用 Narayana Java API。

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

### 设置 XA 事务类型

Yaml:

```yaml
transaction:
  defaultType: XA
  providerType: Narayana
```
