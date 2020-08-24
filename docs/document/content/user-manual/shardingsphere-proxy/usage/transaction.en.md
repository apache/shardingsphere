+++
title = "Distributed Transaction"
weight = 3
+++

ShardingSphere-Proxy supports LOCAL, XA, BASE transactions, LOCAL transaction is default value, it is original transaction of relational database.

## XA transaction

Default XA transaction manager of ShardingSphere is Atomikos. Users can customize Atomikos configuration items through adding `jta.properties` in conf catalog of ShardingSphere-Proxy. Please refer to [Official Documents](https://www.atomikos.com/Documentation/JtaProperties) of Atomikos for detailed configurations.

## BASE Transaction

Since we have not packed the BASE implementation jar into ShardingSphere-Proxy, you should copy relevant jar which implement `ShardingTransactionManager` SPI to `conf/lib`, then switch the transaction type to `BASE`.
