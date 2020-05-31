+++
pre = "<b>5.7. </b>"
title = "Distributed Transaction"
weight = 7
chapter = true
+++

## ShardingTransactionManager

| *SPI Name*                        | *Description*                         |
| --------------------------------- | ------------------------------------- |
| ShardingTransactionManager        | Distributed transaction manager       |

| *Implementation Class*            | *Description*                         |
| --------------------------------- | ------------------------------------- |
| XAShardingTransactionManager      | XA distributed transaction manager    |
| SeataATShardingTransactionManager | Seata distributed transaction manager |

## XATransactionManager

| *SPI Name*                   | *Description*                                        |
| ---------------------------- | ---------------------------------------------------- |
| XATransactionManager         | XA distributed transaction manager                   |

| *Implementation Class*       | *Description*                                        |
| ---------------------------- | ---------------------------------------------------- |
| AtomikosTransactionManager   | XA distributed transaction manager based on Atomikos |
| NarayanaXATransactionManager | XA distributed transaction manager based on Narayana |
| BitronixXATransactionManager | XA distributed transaction manager based on Bitronix |

## XADataSourceDefinition

| *SPI Name*                       | *Description*                                                           |
| -------------------------------- | ----------------------------------------------------------------------- |
| XADataSourceDefinition           | Auto convert Non XA data source to XA data source                       |

| *Implementation Class*           | *Description*                                                           |
| -------------------------------- | ----------------------------------------------------------------------- |
| MySQLXADataSourceDefinition      | Auto convert Non XA MySQL data source to XA MySQL data source           |
| MariaDBXADataSourceDefinition    | Auto convert Non XA MariaDB data source to XA MariaDB data source       |
| PostgreSQLXADataSourceDefinition | Auto convert Non XA PostgreSQL data source to XA PostgreSQL data source |
| OracleXADataSourceDefinition     | Auto convert Non XA Oracle data source to XA Oracle data source         |
| SQLServerXADataSourceDefinition  | Auto convert Non XA SQLServer data source to XA SQLServer data source   |
| H2XADataSourceDefinition         | Auto convert Non XA H2 data source to XA H2 data source                 |

## DataSourcePropertyProvider

| *SPI Name*                 | *Description*                                       |
| -------------------------- | --------------------------------------------------- |
| DataSourcePropertyProvider | Used to get standard properties of data source pool |

| *Implementation Class*     | *Description*                                       |
| -------------------------- | --------------------------------------------------- |
| HikariCPPropertyProvider   | Used to get standard properties of HikariCP         |
