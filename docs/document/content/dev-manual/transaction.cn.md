+++
pre = "<b>5.8. </b>"
title = "分布式事务"
weight = 8
chapter = true
+++

## ShardingTransactionManager

| *SPI 名称*                         | *详细说明*                 |
| --------------------------------- | -------------------------- |
| ShardingTransactionManager        | 分布式事务管理器             |

| *已知实现类*                       | *详细说明*                  |
| --------------------------------- | -------------------------- |
| XAShardingTransactionManager      | 基于 XA 的分布式事务管理器    |
| SeataATShardingTransactionManager | 基于 Seata 的分布式事务管理器 |

## XATransactionManager

| *SPI 名称*                   | *详细说明*                         |
| ---------------------------- | --------------------------------- |
| XATransactionManager         | XA分布式事务管理器                  |

| *已知实现类*                  | *详细说明*                         |
| ---------------------------- | --------------------------------- |
| AtomikosTransactionManager   | 基于 Atomikos 的 XA 分布式事务管理器 |
| NarayanaXATransactionManager | 基于 Narayana 的 XA 分布式事务管理器 |
| BitronixXATransactionManager | 基于 Bitronix 的 XA 分布式事务管理器 |

## XADataSourceDefinition

| *SPI 名称*                        | *详细说明*                                                |
| -------------------------------- | --------------------------------------------------------- |
| XADataSourceDefinition           | 非 XA 数据源自动转化为 XA 数据源                             |

| *已知实现类*                      | *详细说明*                                                 |
| -------------------------------- | --------------------------------------------------------- |
| MySQLXADataSourceDefinition      | 非 XA 的 MySQL 数据源自动转化为 XA 的 MySQL 数据源           |
| MariaDBXADataSourceDefinition    | 非 XA 的 MariaDB 数据源自动转化为 XA 的 MariaDB 数据源       |
| PostgreSQLXADataSourceDefinition | 非 XA 的 PostgreSQL 数据源自动转化为 XA 的 PostgreSQL 数据源 |
| OracleXADataSourceDefinition     | 非 XA 的 Oracle 数据源自动转化为 XA 的 Oracle 数据源         |
| SQLServerXADataSourceDefinition  | 非 XA 的 SQLServer 数据源自动转化为 XA 的 SQLServer 数据源   |
| H2XADataSourceDefinition         | 非 XA 的 H2 数据源自动转化为 XA 的 H2 数据源                 |

## DataSourcePropertyProvider

| *SPI 名称*                  | *详细说明*                       |
| -------------------------- | ------------------------------- |
| DataSourcePropertyProvider | 用于获取数据源连接池的标准属性      |

| *已知实现类*                | *详细说明*                       |
| -------------------------- | ------------------------------- |
| HikariCPPropertyProvider   | 用于获取 HikariCP 连接池的标准属性 |
