+++
title = "Atomikos Transaction"
weight = 4
+++

The default XA transaction manager of Apache ShardingSphere is Atomikos.

## Data Recovery

`xa_tx.log` generated in the project `logs` folder is necessary for the recovery when XA crashes. Please keep it.

## Update Configuration

Developer can add `jta.properties` in classpath of the application to customize Atomikos configuration. 
For detailed configuration rules.

Please refer to [Atomikos official documentation](https://www.atomikos.com/Documentation/JtaProperties) for more details.
