+++
title = "Distributed Transaction"
weight = 4
+++

## Background

ShardingSphere provides three modes for distributed transactions `LOCAL`, `XA`, `BASE`.

## Parameters

```yaml
transaction:
  defaultType: # Transaction mode, optional value LOCAL/XA/BASE
  providerType: # Specific implementation of the mode
```

## Procedure

### Use LOCAL Mode

The content of the global.yaml configuration file is as follows:

```yaml
transaction:
  defaultType: LOCAL
```

### Use XA Mode

The content of the global.yaml configuration file is as follows:

```yaml
transaction:
  defaultType: XA
  providerType: Narayana/Atomikos 
```
To manually add Narayana-related dependencies:

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

### Use BASE Mode

The content of the global.yaml configuration file is as follows:

```yaml
transaction:
  defaultType: BASE
  providerType: Seata 
```

Build a Seata Server, add relevant configuration files and Seata dependencies, see [ShardingSphere Integrates Seata Flexible Transactions](https://community.sphere-ex.com/t/topic/404)