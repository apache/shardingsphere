+++
toc = true
title = "Distributed sequence"
weight = 2
+++

## Motivation

In traditional database, primary key generation is mostly required. Every database has already support this feature, such as auto increment key on MySQL, sequence on Oracle, etc. 
It is difficult to generate the global unique ID for different actual tables after Sharding.
A simple solution is to generate the global unique ID by setting different initial value and increase step, but this requires additional maintenance. This solution make solution complicated. 

There are lots of third-party solutions, such as UUID or global ID generation services, every solutions has their suitable scenario. 
It is not flexibility if Sharding-Sphere dependent on any of the them.

For those reasons, Sharding-Sphere just provide interface and default implement, it can switch key generator strategy by end users. 

# Default distributed ID generator

Use snowflake algorithm to generate 64-bit primary key for long type.

Binary data contains 4 parts: 1 bit for symbol-bit(0), 41 bits for timestamp, 10 bits for worker id and 12 bits for sequence in millisecond.

For different process, snowflake guarantee generate different ID by worker ID; for same process, snowflake guarantee generate different ID by timestamp and sequence in millisecond.
Because of timestamp is monotonically increasing, generated key by snowflake is orderly which guarantee performance for indexed insert. 

Database should be save in a number column whose length is >= 64 bits, such as BIGINT in MySQL.

Class: `io.shardingsphere.core.keygen.DefaultKeyGenerator`

### Timestamp(41 bits)

The number of milliseconds from 00:00 on Nov 1, 2016 to the present and the high limit of year is 2156.

### Work id(10 bits)

This flag is unique in the Java process, and you should ensure that every process ID is different in distributed applications. The default value is 0, and can be configured by calling `DefaultKeyGenerator.setWorkerId("xxxx")`.

### Sequence(12 bits)

It is used to generate different IDs in one millisecond. If the amount of generated IDs in this millisecond is more than 4096(2 to the power 12), the generator will not generate ID until the next millisecond.
