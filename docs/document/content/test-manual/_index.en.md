+++
pre = "<b>6. </b>"
title = "Test Manual"
weight = 6
chapter = true
+++

Apache ShardingSphere provides test engines for integration, module and performance.

## Integration Test

Provide point to point test which connect real ShardingSphere and database instances.

They define SQLs in XML files, engine run for each database independently.
All test engines designed to modify the configuration files to execute all assertions without any **Java code** modification.
It does not depend on any third-party environment, ShardingSphere-Proxy and database used for testing are provided by docker image.

## Module Test

Provide module test engine for complex modules.

They define SQLs in XML files, engine run for each database independently too
It includes SQL parser and SQL rewriter modules.

## Performance Test

Provide multiple performance test methods, includes Sysbench, JMH or TPCC and so on.

## Sysbench Test
