+++
pre = "<b>3.9.2. </b>"
title = "SPI List"
weight = 2
chapter = true
+++

## SQL Passing

### SQLParserConfiguration

| *SPI Name*                    | *Descript ion*                                        |
| ----------------------------- | ----------------------------------------------------- |
| SQLParserConfiguration        | Regulate for SQL parser ANTLR G4 file and AST visitor |

| *Implementation Class*        | *Description*                                         |
| ----------------------------- | ----------------------------------------------------- |
| MySQLParserConfiguration      | Based on MySQL's SQL parser                           |
| PostgreSQLParserConfiguration | Based on PostgreSQL's SQL parser                      |
| SQLServerParserConfiguration  | Based on SQLServer's SQL parser                       |
| OracleParserConfiguration     | Based on Oracle's SQL parser                          |
| SQL92ParserConfiguration      | Based on SQL92's SQL parser                           |

## Database Protocol

### DatabaseProtocolFrontendEngine

| *SPI Name*                       | *Description*                                                                   |
| -------------------------------- | ------------------------------------------------------------------------------- |
| DatabaseProtocolFrontendEngine   | Regulate parse and adapter protocol of database access for ShardingSphere-Proxy |

| *Implementation Class*           | *Description*                                                                   |
| -------------------------------- | ------------------------------------------------------------------------------- |
| MySQLProtocolFrontendEngine      | Base on MySQL database protocol                                                 |
| PostgreSQLProtocolFrontendEngine | Base on postgreSQL database protocol                                            |

## Configuration

### ShardingSphereRuleBuilder

| *SPI Name*                | *Description*                                                                       |
| ------------------------- | ----------------------------------------------------------------------------------- |
| ShardingSphereRuleBuilder | Used to convert user configurations to rule objects                                 |

| *Implementation Class*    | *Description*                                                                       |
| ------------------------- | ----------------------------------------------------------------------------------- |
| ShardingRuleBuilder       | Used to convert user sharding configurations to sharding rule objects               |
| MasterSlaveRuleBuilder    | Used to convert user master-slave configurations to master-slave rule objects       |
| ReplicaRuleBuilder        | Used to convert user multi replica configurations to multi replica rule objects     |
| EncryptRuleBuilder        | Used to convert user encryption configurations to encryption rule objects           |
| ShadowRuleBuilder         | Used to convert user shadow database configurations to shadow database rule objects |

### YamlRuleConfigurationSwapper

| *SPI Name*                              | *Description*                                                                                |
| --------------------------------------- | -------------------------------------------------------------------------------------------- |
| YamlRuleConfigurationSwapper            | Used to convert YAML configuration to standard user configuration                            |

| *Implementation Class*                  | *Description*                                                                                |
| --------------------------------------- | -------------------------------------------------------------------------------------------- |
| ShardingRuleConfigurationYamlSwapper    | Used to convert YAML sharding configuration to standard sharding configuration               |
| MasterSlaveRuleConfigurationYamlSwapper | Used to convert YAML master-slave configuration to standard master-slave configuration       |
| ReplicaRuleConfigurationYamlSwapper     | Used to convert YAML multi replica configuration to standard multi replica configuration     |
| EncryptRuleConfigurationYamlSwapper     | Used to convert YAML encryption configuration to standard encryption configuration           |
| ShadowRuleConfigurationYamlSwapper      | Used to convert YAML shadow database configuration to standard shadow database configuration |

### ShardingSphereYamlConstruct

| *SPI Name*                                     | *Description*                                                |
| ---------------------------------------------- | ------------------------------------------------------------ |
| ShardingSphereYamlConstruct                    | Used to convert customized objects and YAML to each other    |

| *Implementation Class*                         | *Description*                                                |
| ---------------------------------------------- | ------------------------------------------------------------ |
| NoneShardingStrategyConfigurationYamlConstruct | Used to convert non sharding strategy and YAML to each other |

## Kernel

### RuleMetaDataLoader

| *SPI Name*             | *Description*                           |
| ---------------------- | --------------------------------------- |
| RuleMetaDataLoader     | Used to initialize meta data            |

| *Implementation Class* | *Description*                           |
| ---------------------- | --------------------------------------- |
| ShardingMetaDataLoader | Used to initialize sharding meta data   |
| EncryptMetaDataLoader  | Used to initialize encryption meta data |

### RuleMetaDataDecorator

| *SPI Name*                | *Description*                        |
| ------------------------- | ------------------------------------ |
| RuleMetaDataDecorator     | Used to update meta data             |

| *Implementation Class*    | *Description*                        |
| ------------------------- | ------------------------------------ |
| ShardingMetaDataDecorator | Used to update sharding meta data    |
| EncryptMetaDataDecorator  | Used to update encryption meta data  |

### RouteDecorator

| *SPI Name*                | *Description*                                   |
| ------------------------- | ----------------------------------------------- |
| RouteDecorator            | Used to process routing results                 |

| *Implementation Class*    | *Description*                                   |
| ------------------------- | ----------------------------------------------- |
| ShardingRouteDecorator    | Used to process sharding routing results        |
| MasterSlaveRouteDecorator | Used to process master-slave routing results    |
| ReplicaRouteDecorator     | Used to process multi replica routing results   |
| ShadowRouteDecorator      | Used to process shadow database routing results |

### SQLRewriteContextDecorator

| *SPI Name*                         | *Description*                                  |
| ---------------------------------- | ---------------------------------------------- |
| SQLRewriteContextDecorator         | Used to process SQL rewrite results            |

| *SPI Name*                         | *Description*                                  |
| ---------------------------------- | ---------------------------------------------- |
| ShardingSQLRewriteContextDecorator | Used to process sharding SQL rewrite results   |
| EncryptSQLRewriteContextDecorator  | Used to process encryption SQL rewrite results |
| ShadowSQLRewriteContextDecorator   | Used to process shadow SQL rewrite results     |

### ExecuteGroupDecorator

| *SPI Name*                   | *Description*                          |
| ---------------------------- | -------------------------------------- |
| ExecuteGroupDecorator        | Used by update data nodes group result |

| *Implementation Class*       | *Description*                          |
| ---------------------------- | -------------------------------------- |
| ReplicaExecuteGroupDecorator | Used by multi replica data nodes group |

### SQLExecutionHook

| *SPI Name*                    | *Description*                      |
| ----------------------------- | ---------------------------------- |
| SQLExecutionHook              | Hook of SQL execution              |

| *Implementation Class*        | *Description*                      |
| ----------------------------- | ---------------------------------- |
| TransactionalSQLExecutionHook | Transaction hook of SQL execution  |
| OpenTracingSQLExecutionHook   | Open tracing hook of SQL execution |

### ResultProcessEngine

| *SPI Name*                   | *Description*                                         |
| ---------------------------- | ----------------------------------------------------- |
| ResultProcessEngine          | Used by merge engine to process result set            |

| *Implementation Class*       | *Description*                                         |
| ---------------------------- | ----------------------------------------------------- |
| ShardingResultMergerEngine   | Used by merge engine to process sharding result set   |
| EncryptResultDecoratorEngine | Used by merge engine to process encryption result set |

## Data Sharding

### ShardingAlgorithm

| *SPI Name*                      | *Description*                          |
| ------------------------------- | -------------------------------------- |
| ShardingAlgorithm               | Sharding algorithm                     |

| *Implementation Class*          | *Description*                          |
| ------------------------------- | -------------------------------------- |
| InlineShardingAlgorithm         | Inline sharding algorithm              |
| ModuloShardingAlgorithm         | Modulo sharding algorithm              |
| HashShardingAlgorithm           | Hash sharding algorithm                |
| DatetimeShardingAlgorithm       | Datetime sharding algorithm            |
| CustomDateTimeShardingAlgorithm | Customized datetime sharding algorithm |
| StandardRangeShardingAlgorithm  | Range sharding algorithm               |
| CustomRangeShardingAlgorithm    | Customized range sharding algorithm    |

### KeyGenerateAlgorithm

| *SPI Name*                    | *Description*                    |
| ----------------------------- | -------------------------------- |
| KeyGenerateAlgorithm          | Key generate algorithm           |

| *Implementation Class*        | *Description*                    |
| ----------------------------- | -------------------------------- |
| SnowflakeKeyGenerateAlgorithm | Snowflake key generate algorithm |
| UUIDKeyGenerateAlgorithm      | UUID key generate algorithm      |

### TimeService

| *SPI Name*                  | *Description*                                                |
| --------------------------- | ------------------------------------------------------------ |
| TimeService                 | Use current time for routing                                 |

| *Implementation Class*      | *Description*                                                |
| --------------------------- | ------------------------------------------------------------ |
| DefaultTimeService          | Get the current time from the application system for routing |
| DatabaseTimeServiceDelegate | Get the current time from the database for routing           |

### DatabaseSQLEntry

| *SPI Name*                 | *Description*                           |
| -------------------------- | --------------------------------------- |
| DatabaseSQLEntry           | Database dialect for get current time   |

| *Implementation Class*     | *Description*                           |
| -------------------------- | --------------------------------------- |
| MySQLDatabaseSQLEntry      | MySQL dialect for get current time      |
| PostgreSQLDatabaseSQLEntry | PostgreSQL dialect for get current time |
| OracleDatabaseSQLEntry     | Oracle dialect for get current time     |
| SQLServerDatabaseSQLEntry  | SQLServer dialect for get current time  |

## Read-write splitting

### MasterSlaveLoadBalanceAlgorithm

| *SPI Name*                                | *Description*                                         |
| ----------------------------------------- | ----------------------------------------------------- |
| MasterSlaveLoadBalanceAlgorithm           | Load balance algorithm of slave databases             |

| *Implementation Class*                    | *Description*                                         |
| ----------------------------------------- | ----------------------------------------------------- |
| RoundRobinMasterSlaveLoadBalanceAlgorithm | Round robin load balance algorithm of slave databases |
| RandomMasterSlaveLoadBalanceAlgorithm     | Random load balance algorithm of slave databases      |

## Data encryption

### Encryptor

| *SPI Name*             | *Description*              |
| ---------------------- | -------------------------- |
| Encryptor              | Data encrypt algorithm     |

| *Implementation Class* | *Description*              |
| ---------------------- | -------------------------- |
| MD5Encryptor           | MD5 data encrypt algorithm |
| AESEncryptor           | AES data encrypt algorithm |
| RC4Encryptor           | Rc4 data encrypt algorithm |

### QueryAssistedEncryptor

| *SPI Name*             | *Description*                                              |
| ---------------------- | ---------------------------------------------------------- |
| QueryAssistedEncryptor | Data encrypt algorithm which include query assisted column |

| *Implementation Class* | *Description*                                              |
| ---------------------- | ---------------------------------------------------------- |
| None                   |                                                            |

## Distributed Transaction

### ShardingTransactionManager

| *SPI Name*                        | *Description*                         |
| --------------------------------- | ------------------------------------- |
| ShardingTransactionManager        | Distributed transaction manager       |

| *Implementation Class*            | *Description*                         |
| --------------------------------- | ------------------------------------- |
| XAShardingTransactionManager      | XA distributed transaction manager    |
| SeataATShardingTransactionManager | Seata distributed transaction manager |

### XATransactionManager

| *SPI Name*                   | *Description*                                        |
| ---------------------------- | ---------------------------------------------------- |
| XATransactionManager         | XA distributed transaction manager                   |

| *Implementation Class*       | *Description*                                        |
| ---------------------------- | ---------------------------------------------------- |
| AtomikosTransactionManager   | XA distributed transaction manager based on Atomikos |
| NarayanaXATransactionManager | XA distributed transaction manager based on Narayana |
| BitronixXATransactionManager | XA distributed transaction manager based on Bitronix |

### XADataSourceDefinition

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

### DataSourcePropertyProvider

| *SPI Name*                 | *Description*                                       |
| -------------------------- | --------------------------------------------------- |
| DataSourcePropertyProvider | Used to get standard properties of data source pool |

| *Implementation Class*     | *Description*                                       |
| -------------------------- | --------------------------------------------------- |
| HikariCPPropertyProvider   | Used to get standard properties of HikariCP         |

