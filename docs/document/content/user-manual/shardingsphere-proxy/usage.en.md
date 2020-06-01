+++
pre = "<b>4.2.1. </b>"
title = "Usage"
weight = 1
+++

## Proxy Initialization

1. Download the latest version of ShardingSphere-Proxy.
2. If users use docker, they can implement `docker pull shardingsphere/shardingsphere-proxy` to get the clone. Please refer to [Docker Clone](/en/user-manual/shardingsphere-proxy/docker/) for more details.
3. After the decompression, revise `conf/server.yaml` and documents begin with `config-` prefix, `conf/config-xxx.yaml` for example, to configure sharding rules and read-write split rules. Please refer to [Configuration Manual](/en/user-manual/shardingsphere-proxy/configuration/) for the configuration method.
4. Please run `bin/start.sh` for Linux operating system; run `bin/start.bat` for Windows operating system to start ShardingSphere-Proxy. To configure start port and document location, please refer to [Quick Start](/en/quick-start/shardingsphere-proxy-quick-start/).
5. Use any PostgreSQL server client end to connect, such as `psql -U root -h 127.0.0.1 -p 3307`.

## Use of Registry Center

If users want to use the database orchestration function of ShardingSphere-Proxy, they need to implement instance disabling and slave database disabling functions in the registry center. Please refer to [Available Registry Centers](/en/features/orchestration/supported-registry-repo/) for more details.

### Zookeeper

1. ShardingSphere-Proxy has provided the registry center solution of Zookeeper in default. Users only need to follow [Configuration Rules](/en/user-manual/shardingsphere-proxy/configuration/) to set the registry center and use it.

### Other Third Party Registry Center

1. Delete`shardingsphere-orchestration-reg-zookeeper-curator-${shardingsphere.version}.jar` under the lib catalog of ShardingSphere-Proxy.
2. Use SPI methods in logic coding and put the generated jar package to the lib catalog of ShardingSphere-Proxy.
3. Follow [Configuration Rules](/en/user-manual/shardingsphere-proxy/configuration/) to set the registry center and use it.

### Use of user-defined sharding algorithm class

TODO

## Distributed Transactions
ShardingSphere-Proxy supports LOCAL, XA, BASE transactions, LOCAL transaction is default value, it is original transaction of relational database.

### XA transaction

Default XA transaction manager of ShardingSphere is Atomikos. Users can customize Atomikos configuration items through adding `jta.properties` in conf catelog of ShardingSphere-Proxy. Please refer to [Official Documents](https://www.atomikos.com/Documentation/JtaProperties) of Atomikos for detailed configurations.

### BASE Transaction

Since we have not pack the BASE implementation jar into ShardingSphere-Proxy, you should copy relevant jar which implement `ShardingTransactionManager` SPI to `conf/lib`, then switch the transaction type
 to `BASE`.
 
## SCTL (ShardingSphere-Proxy control language)

SCTL supports modify and query the state of Sharing-Proxy at runtime. The current supported syntax is:

| statement                               | function                                                                                                             | example                                        |
|:----------------------------------------|:---------------------------------------------------------------------------------------------------------------------|:-----------------------------------------------|
|sctl:set transaction_type=XX             | Modify transaction_type of the current TCP connection, supports LOCAL, XA, BASE                                      | sctl:set transaction_type=XA                   |
|sctl:show transaction_type               | Query the transaction type of the current TCP connection                                                             | sctl:show transaction_type                     |
|sctl:show cached_connections             | Query the number of cached physical database connections in the current TCP connection                               | sctl:show cached_connections                   |
|sctl:explain SQL                         | View the execution plan for logical SQL.                                                                             | sctl:explain select * from t_order             |
|sctl:hint set MASTER_ONLY=true           | For current TCP connection, set database operation force route to master database only or not                        | sctl:hint set MASTER_ONLY=true                 |
|sctl:hint set DatabaseShardingValue=yy   | For current TCP connection, set sharding value for database sharding only, yy: sharding value                        | sctl:hint set DatabaseShardingValue=100        |
|sctl:hint addDatabaseShardingValue xx=yy | For current TCP connection, add sharding value for database, xx: logic table, yy: sharding value                     | sctl:hint addDatabaseShardingValue t_order=100 |
|sctl:hint addTableShardingValue xx=yy    | For current TCP connection, add sharding value for table, xx: logic table, yy: sharding value                        | sctl:hint addTableShardingValue t_order=100    |
|sctl:hint clear                          | For current TCP connection, clear all hint settings                                                                  | sctl:hint clear                                |
|sctl:hint show status                    | For current TCP connection, query hint status, master_only:true/false, sharding_type:databases_only/databases_tables | sctl:hint show status                          |
|sctl:hint show table status              | For current TCP connection, query sharding values of logic tables                                                    | sctl:hint show table status                    |

ShardingSphere-Proxy does not support hint by default, to support it, set the `props` property `proxy.hint.enabled` to true in conf/server.yaml.In ShardingSphere-Proxy. In ShardingSphere-Proxy, the generic of HintShardingAlgorithm can only be a String type.

## Notices

1. ShardingSphere-Proxy uses 3307 port in default. Users can start the script parameter as the start port number, like `bin/start.sh 3308`.
2. ShardingSphere-Proxy uses `conf/server.yaml` to configure the registry center, authentication information and public properties.
3. ShardingSphere-Proxy supports multi-logic data source, with each yaml configuration document named by `config-` prefix as a logic data source.
