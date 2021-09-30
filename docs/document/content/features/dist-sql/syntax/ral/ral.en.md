+++
title = "RAL"
weight = 1
+++

## Definition

RAL (Resource & Rule Administration Language) responsible for the added-on feature of hint, transaction type switch, scaling, sharding execute planning and so on.
## Usage

## Hint

| Statement                                          | Function                                                                                                     | Example                                           |
|:---------------------------------------------------|:-------------------------------------------------------------------------------------------------------------|:----------------------------------------------|
|set readwrite_splitting hint source = [auto / write]| For current connection, set readwrite splitting routing strategy (automatic or forced to write data source)  | set readwrite_splitting hint source = write   |  
|set sharding hint database_value = yy               | For current connection, set sharding value for database sharding only, yy: sharding value                    | set sharding hint database_value = 100        |  
|add sharding hint database_value xx= yy             | For current connection, add sharding value for table, xx: logic table, yy: database sharding value           | add sharding hint database_value t_order= 100 |  
|add sharding hint table_value xx = yy               | For current connection, add sharding value for table, xx: logic table, yy: table sharding value              | add sharding hint table_value t_order = 100   |  
|clear hint                                          | For current connection, clear all hint settings                                                              | clear hint                                    |  
|clear [sharding hint / readwrite_splitting hint]    | For current connection, clear hint settings of sharding or readwrite splitting                               | clear readwrite_splitting hint                |  
|show [sharding / readwrite_splitting] hint status   | For current connection, query hint settings of sharding or readwrite splitting                               | show readwrite_splitting hint status          |  

## Scaling

| Statement                                          | Function                                                           | Example                                         |
|:---------------------------------------------------|:-------------------------------------------------------------------|:-----------------------------------------------|
|show scaling list                                   | Query running list                                                 | show scaling list                              |  
|show scaling status xx                              | Query scaling status, xx：jobId                                     | show scaling status 1234                       |  
|start scaling xx                                    | Start scaling, xx：jobId                                            | start scaling 1234                             |  
|stop scaling xx                                     | Stop scaling, xx：jobId                                             | stop scaling 1234                              |  
|drop scaling xx                                     | Drop scaling, xx：jobId                                             | drop scaling 1234                              |  
|reset scaling xx                                    | reset progress, xx：jobId                                           | reset scaling 1234                             |  
|check scaling xx                                    | Data consistency check, xx：jobId                                   | check scaling 1234                             |  
|show scaling check algorithms                       | Show available consistency check algorithms                         | show scaling check algorithms                  |  
|stop scaling source writing xx                      | The source ShardingSphere data source is discontinued, xx：jobId     | stop scaling source writing 1234               |  
|checkout scaling xx                                 | Switch to target ShardingSphere data source, xx：jobId               | checkout scaling 1234                          |  


## Circuit Breaker

| Statement                                                         | Function                            | Example                                       |
|:------------------------------------------------------------------|:------------------------------------|:----------------------------------------------|
|[enable / disable] readwrite_splitting read xxx [from schema]      | Enable or disable read data source  | enable readwrite_splitting read xxx           |  
|[enable / disable] instance IP=xxx, PORT=xxx                       | Enable or disable proxy instance    | disable instance IP=127.0.0.1, PORT=3307      |  

## Other

| Statement                                          | Function                                                                             | Example                                       |
|:---------------------------------------------------|:-------------------------------------------------------------------------------------|:----------------------------------------------|
|set variable transaction_type = xx                  | Modify transaction_type of the current connection, supports LOCAL, XA, BASE          | set variable transaction_type = XA            |  
|show variable transaction_type                      | Query the transaction type of the current connection                                 | show variable transaction_type                |  
|show variable cached_connections                    | Query the number of cached physical database connections in the current connection   | show variable cached_connections              |  
|preview SQL                                         | Preview the actual SQLs                                                              | preview select * from t_order                 |  

## Notice

ShardingSphere-Proxy does not support hint by default, to support it, set `proxy-hint-enabled` to true in `conf/server.yaml`.
