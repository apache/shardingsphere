+++
title = "Atomikos Transaction"
weight = 4
+++

## Background

Apache ShardingSphere provides XA transactions, and the default XA transaction manager is Atomikos.

## Procedure

1. Configure the transaction type
2. Configure Atomikos

## Sample

### Configure the transaction type

Yaml:

```yaml
transaction:
  defaultType: XA
  providerType: Atomikos 
```

### Configure Atomikos

Atomikos configuration items can be customized by adding `jta.properties` to the project's classpath.

See [Atomikos's official documentation](https://www.atomikos.com/Documentation/JtaProperties) for more details.

### Data Recovery

`xa_tx.log` is generated in the `logs` directory of the project. This is the log required for recovering XA crash. Do not delete it. 
