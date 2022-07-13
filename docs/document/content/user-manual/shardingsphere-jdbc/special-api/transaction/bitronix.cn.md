+++
title = "Bitronix 事务"
weight = 6
+++

## 背景信息

Apache ShardingSphere 提供 XA 事务，集成了 Bitronix 的实现。

## 前提条件

引入 Maven 依赖

```xml
<properties>
    <btm.version>2.1.3</btm.version>
</properties>

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
    <artifactId>shardingsphere-transaction-xa-bitronix</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<dependency>
    <groupId>org.codehaus.btm</groupId>
    <artifactId>btm</artifactId>
    <version>${btm.version}</version>
</dependency>
```

## 操作步骤

1. 配置 XA 事务类型
2. 配置 Bitronix

## 配置示例

### 配置 XA 事务类型

Yaml:

```yaml
- !TRANSACTION
  defaultType: XA
  providerType: Bitronix
```

SpringBoot:

```yaml
spring:
  shardingsphere:
    props:
      xa-transaction-manager-type: Bitronix
```

Spring Namespace:

```xml
<shardingsphere:data-source id="xxx" data-source-names="xxx" rule-refs="xxx">
    <props>
        <prop key="xa-transaction-manager-type">Bitronix</prop>
    </props>
</shardingsphere:data-source>
```

### 配置 Bitronix （可省略）

详情请参见 [Bitronix 官方文档](https://github.com/bitronix/btm/wiki) 。
