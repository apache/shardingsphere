+++
title = "Configuration"
weight = 3
+++

## Sharding Rule

The main entrance for Sharding rules includes the configurations of data source, tables, binding tables and replica query.

## Data Sources Configuration

Real data sources list.

## Tables Configuration

Configurations of logic table names, data node and table sharding rules.

## Data Node Configuration

It is used in the configurations of the mapping relationship between logic tables and actual tables and can be divided into two kinds: uniform distribution and user-defined distribution.

- Uniform distribution

It means that data tables are evenly distributed in each data source, for example: 

```
db0
  ├── t_order0 
  └── t_order1 
db1
  ├── t_order0 
  └── t_order1
```

So the data node configurations will be as follows:

```
db0.t_order0, db0.t_order1, db1.t_order0, db1.t_order1
```

- User-defined distribution

It means that data tables are distributed with certain rules, for example:

```
db0
  ├── t_order0 
  └── t_order1 
db1
  ├── t_order2
  ├── t_order3
  └── t_order4
```

So the data node configurations will be as follows:

```
db0.t_order0, db0.t_order1, db1.t_order2, db1.t_order3, db1.t_order4
```

## Sharding Strategy Configuration

There are two dimensions of sharding strategies, database sharding and table sharding.

- Database sharding strategy

`DatabaseShardingStrategy` is used to configure data in the targeted database.

- Table sharding strategy

`TableShardingStrategy` is used to configure data in the targeted table that exists in the database. 
So the table sharding strategy relies on the result of the database sharding strategy.

API of those two kinds of strategies are totally same.

## Auto-increment Key Generation Strategy

Replacing the original database auto-increment key with that generated in the server can make distributed key not repeat.