+++
toc = true
title = "Inline Expression"
weight = 1
+++

## Motivation

Configuration simplicity and unity are two main problems that inline expression wants to solve.

In complex data sharding rules, with the increase of data nodes, a large number of repeated configurations make it difficult to maintain. 
Inline expressions can simplify data node configuration work.

Realizing common sharding algorithms with Java codes is not helpful to the unified management of configurations. 
Writing sharding algorithms with inline expressions enables rule configurations to be stored together, making it easier to browse and store them.

## Syntax Explanation

The use of inline expressions is really direct. 
Users only need to use `${ expression }` or `$->{ expression }` to identify them. 
The configurations of data nodes and sharding algorithms are available for now. 
The content of inline expressions uses Groovy syntax, which can support all kinds of operations, including inline expression. For example:

`${begin..end}` means range

`${[unit1, unit2, unit_x]}` means enumeration

If there appears many continuous `${ expression }` or `$->{ expression }` in the inline expression, 
according to each sub inline expression result, the ultimate result of the whole expression will be in cartesian combination.

For example, the following inline expression:

```groovy
${['online', 'offline']}_table${1..3}
```

Will be parsed as:

```
online_table1, online_table2, online_table3, offline_table1, offline_table2, offline_table3
```

## Data Node Configuration

For evenly distributed data nodes, if the data structure is as follow:

```
db0
  ├── t_order0 
  └── t_order1 
db1
  ├── t_order0 
  └── t_order1
```

It can be simplified by inline expression as:

```
db${0..1}.t_order${0..1}
```

Or

```
db$->{0..1}.t_order$->{0..1}
```

For self-defined data nodes, if the data structure is:

```
db0
  ├── t_order0 
  └── t_order1 
db1
  ├── t_order2
  ├── t_order3
  └── t_order4
```

It can be simplified by inline expression as:

```
db0.t_order${0..1},db1.t_order${2..4}
```

Or

```
db0.t_order$->{0..1},db1.t_order$->{2..4}
```

For data nodes with prefixes, inline expression can also be used to configure them flexibly, if the data structure is:

```
db0
  ├── t_order_00
  ├── t_order_01
  ├── t_order_02
  ├── t_order_03
  ├── t_order_04
  ├── t_order_05
  ├── t_order_06
  ├── t_order_07
  ├── t_order_08
  ├── t_order_09
  ├── t_order_10
  ├── t_order_11
  ├── t_order_12
  ├── t_order_13
  ├── t_order_14
  ├── t_order_15
  ├── t_order_16
  ├── t_order_17
  ├── t_order_18
  ├── t_order_19
  └── t_order_20
db1
  ├── t_order_00
  ├── t_order_01
  ├── t_order_02
  ├── t_order_03
  ├── t_order_04
  ├── t_order_05
  ├── t_order_06
  ├── t_order_07
  ├── t_order_08
  ├── t_order_09
  ├── t_order_10
  ├── t_order_11
  ├── t_order_12
  ├── t_order_13
  ├── t_order_14
  ├── t_order_15
  ├── t_order_16
  ├── t_order_17
  ├── t_order_18
  ├── t_order_19
  └── t_order_20
```

Users can use separate configuration method to configure data nodes with prefixes, then data nodes without prefixes, and automatically combine them with the cartesian product feature of inline expressions. 
The example above can be simplified by inline expression as:

```groovy
db${0..1}.t_order_0${0..9}, db${0..1}.t_order_${10..20}
```

Or

```groovy
db->${0..1}.t_order_0$->{0..9}, db$->{0..1}.t_order_$->{10..20}
```

## Sharding Algorithm Configuration

For single sharding SQL that uses `=` and `IN`, inline expression can be used to replace codes in configuration.

Inline expression is a piece of Groovy code in essence, which can return the corresponding real data source or table name according to the computation method of sharding keys.

For example, sharding keys with the last number 0 are routed to the data source with the suffix of 0, those with the last number 1 are routed to the data source with the suffix of 1, the rest goes on in a similar way. 
The inline expression used to indicate sharding algorithm is:

```groovy
ds${id % 10}
```

Or

```groovy
ds$->{id % 10}
```
