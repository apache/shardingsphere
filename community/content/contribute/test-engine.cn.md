+++
toc = true
title = "测试引擎"
weight = 8
+++

ShardingSphere提供了完善的测试引擎。它以XML方式定义SQL，每条SQL由SQL解析单元测试引擎和整合测试引擎驱动，每个引擎分别为H2、MySQL、PostgreSQL、SQLServer和Oracle数据库运行测试用例。

SQL解析单元测试全面覆盖SQL占位符和字面量维度。整合测试进一步拆分为策略和JDBC两个维度，策略维度包括分库分表、仅分表、仅分库、读写分离等策略，JDBC维度包括Statement、PreparedStatement。

因此，1条SQL会驱动5种数据库的解析 * 2种参数传递类型 + 5种数据库 * 5种分片策略 * 2种JDBC运行方式 = 60个测试用例，以达到ShardingSphere对于高质量的追求。

# 整合测试

## 测试环境

整合测试由于涉及到真实数据库环境，需要先完成以下准备工作并测试：

1. 在准备测试的数据库上运行`resources/integrate/schema/manual_schema_create.sql`创建数据库(MySQL、PostgreSQL、SQLServer)及Schema（仅Oracle）。

1. 修改`sharding-jdbc/src/test/resources/integrate/env.properties中的databases`，指定需要测试的数据库。

1. 运行`AllIntegrateTests`，检查测试结果。

## 注意事项

1. 如需测试Oracle，请在pom.xml中增加Oracle驱动依赖。

1. 为了保证测试数据的完整性，整合测试中的分库分表采用了10库10表的方式，因此运行测试用例的时间会比较长。

# SQL解析引擎测试

## 测试环境

SQL解析引擎测试是基于SQL本身的解析，因此无需连接数据库，直接运行`AllParsingTests`即可。
