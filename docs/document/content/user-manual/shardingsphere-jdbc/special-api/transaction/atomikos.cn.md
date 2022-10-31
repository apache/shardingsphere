+++
title = "Atomikos 事务"
weight = 4
+++

## 背景信息

Apache ShardingSphere 提供 XA 事务，默认的 XA 事务实现为 Atomikos。
## 操作步骤

1. 配置事务类型
2. 配置 Atomikos

## 配置示例

### 配置事务类型

Yaml:

```yaml
- !TRANSACTION
  defaultType: XA
  providerType: Atomikos 
```

SpringBoot:

```yaml
spring:
  shardingsphere:
    props:
      xa-transaction-manager-type: Atomikos
```

Spring Namespace:

```xml
<shardingsphere:data-source id="xxx" data-source-names="xxx" rule-refs="xxx">
    <props>
        <prop key="xa-transaction-manager-type">Atomikos</prop>
    </props>
</shardingsphere:data-source>
```
### 配置 Atomikos

可以通过在项目的 classpath 中添加 `jta.properties` 来定制化 Atomikos 配置项。

详情请参见 [Atomikos 官方文档](https://www.atomikos.com/Documentation/JtaProperties) 。

### 数据恢复

在项目的 `logs` 目录中会生成 `xa_tx.log`, 这是 XA 崩溃恢复时所需的日志，请勿删除。
