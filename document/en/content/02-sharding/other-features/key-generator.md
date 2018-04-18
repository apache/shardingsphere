+++
toc = true
title = "Distributed sequence"
weight = 2
+++

## Background

In traditional database software, the technology of primary key generation is mostly required. For MySQL, it is difficult to generate the global unique ID for different actual tables after Sharding.
A simple solution is to generate the global unique ID by setting the rule of global ID generation, but this requires additional maintenance and also restricts the extensibility of the framework.
There are a number of third-party solutions that can solve this problem, such as UUID which relies on specific algorithms to generate global unique primary keys, or other ID generation services. If Sharding-JDBC is strongly dependent on any of the above solutions, its growth will be limited.
For those reasons, we finally decided to detach the underlying implementation of ID generation from Sharding-JDBC and use the JDBC interface to access to the generated IDs.

## Usage

It includes:
1. Configure the auto-generating method for primary key.
2. Obtain the generated keys

### Configure the auto-generating method for primary key

Configure the auto-generating method for primary key:

```java
TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
tableRuleConfig.setLogicTable("t_order");
tableRuleConfig.setKeyGeneratorColumnName("order_id");
```

Configure the implementation class of the ID generator, and the class needs to implement the interface of io.shardingjdbc.core.keygen.KeyGenerator.

Configure the global ID generator(com.xx.xx.KeyGenerator):

```java
ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
shardingRuleConfig.setDefaultKeyGeneratorClass("com.xx.xx.KeyGenerator");
```

Sometimes if you want the ID generator of some tables are different from the global ID generator, you can use com.xx.xx.OtherKeyGenerator to generate ID. 
For example, you can use com.xx.xx.OtherKeyGenerator to generate ID for t_order_item table, but com.xx.xx.KeyGenerator for t_order table.

```java
TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
tableRuleConfig.setLogicTable("t_order");
tableRuleConfig.setKeyGeneratorColumnName("order_id");
tableRuleConfig.setKeyGeneratorClass("com.xx.xx.OtherKeyGenerator");
```

### Obtain the generated keys

To Obtain the generated keys by using the API provided by JDBC. For Statement, you can call```statement.execute("INSERT ...", Statement.RETURN_GENERATED_KEYS)```
to 通知需要返回的生成的键值。For PreparedStatement, the command is ```connection.prepareStatement("INSERT ...", Statement.RETURN_GENERATED_KEYS)```.

To call ```statement.getGeneratedKeys()```to get the ResultSet of the value。

### Other framework configurations

Learn about the configurations of Spring，YAML，MyBatis and JPA(Hibernate), please refer to [The project examples](https://github.com/shardingjdbc/sharding-jdbc/tree/master/sharding-jdbc-example)。

# The default distributed ID generator

Its class name: io.shardingjdbc.core.keygen.DefaultKeyGenerator

This class uses snowflake algorithm to generate 64-bit and long-type primary key which should be save in a NUM column whose length is >= 64 bits, such as BIGINT in MySQL.

Its binary representation consists of four parts: 1 bit for symbol-bit (0), 41 bits for time-bit, 10 bits for process-bit and 12 bits for sequence-bit.

### Time-bit(41 bits)

The number of milliseconds from 00:00 on Nov 1, 2016 to the present and the high limit of year is 2156.

### Process-bit(10 bits)

This flag is unique in the Java process, and you should ensure that every process ID is different in distributed applications. The default value is 0, and can be configured by calling `DefaultKeyGenerator.setWorkerId("xxxx")`.

### Sequence-bit(12 bits)

It is used to generate different IDs in one millisecond. If the amount of generated IDs in this millisecond is more than 4096(2 to the power 12), the generator will not generate ID until the next millisecond.

### Summary

In consider of the generating method of primary key, generated ID is always sequential in product environment, which provides efficient insertion for index columns, e.g. the primary key in MySQL's Innodb storage engine.
More details on the guarantee of primary key uniqueness are as follows:
First, different process numbers, different IDs. 
Second, Time-bit and Sequence-bit are used to ensure to generate unique ID in the same process.
At last, the number of the time is monotonically increasing, and time synchronously mechanism among servers.
