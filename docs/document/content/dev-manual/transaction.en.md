+++
pre = "<b>6.10. </b>"
title = "Distributed Transaction"
weight = 10
chapter = true
+++

## ShardingSphereTransactionManager

| *SPI Name*                              | *Description*                         |
| --------------------------------------- | ------------------------------------- |
| ShardingSphereTransactionManager        | Distributed transaction manager       |

| *Implementation Class*                  | *Description*                         |
| --------------------------------------- | ------------------------------------- |
| XAShardingSphereTransactionManager      | XA distributed transaction manager    |
| SeataATShardingSphereTransactionManager | Seata distributed transaction manager |

## XATransactionManagerProvider

| *SPI Name*                           | *Description*                                        |
| ------------------------------------ | ---------------------------------------------------- |
| XATransactionManagerProvider         | XA distributed transaction manager                   |

| *Implementation Class*               | *Description*                                        |
| ------------------------------------ | ---------------------------------------------------- |
| AtomikosTransactionManagerProvider   | XA distributed transaction manager based on Atomikos |
| NarayanaXATransactionManagerProvider | XA distributed transaction manager based on Narayana |
| BitronixXATransactionManagerProvider | XA distributed transaction manager based on Bitronix |

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
