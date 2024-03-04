+++
title = "分布式事务"
weight = 4
+++

## 背景信息

ShardingSphere 提供了三种模式的分布式事务 `LOCAL`, `XA`, `BASE`。

## 参数解释

```yaml
transaction:
  defaultType: # 事务模式，可选值 LOCAL/XA/BASE
  providerType: # 指定模式下的具体实现
```

## 操作步骤

### 使用 LOCAL 模式

global.yaml 配置文件内容如下：

```yaml
transaction:
  defaultType: LOCAL
```

### 使用 XA 模式

global.yaml 配置文件内容如下：

```yaml
transaction:
  defaultType: XA
  providerType: Narayana/Atomikos 
```
手动添加 Narayana 相关依赖：

```
jta-5.12.7.Final.jar
arjuna-5.12.7.Final.jar
common-5.12.7.Final.jar
jboss-connector-api_1.7_spec-1.0.0.Final.jar
jboss-logging-3.2.1.Final.jar
jboss-transaction-api_1.2_spec-1.0.0.Alpha3.jar
jboss-transaction-spi-7.6.1.Final.jar
narayana-jts-integration-5.12.7.Final.jar
shardingsphere-transaction-xa-narayana-x.x.x-SNAPSHOT.jar
```

### 使用 BASE 模式

global.yaml 配置文件内容如下：

```yaml
transaction:
  defaultType: BASE
  providerType: Seata 
```

搭建 Seata Server，添加相关配置文件，和 Seata 依赖，具体步骤参考 [ShardingSphere 集成 Seata 柔性事务](https://community.sphere-ex.com/t/topic/404)
