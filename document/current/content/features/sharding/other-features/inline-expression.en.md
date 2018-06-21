+++
toc = true
title = "Inline Expression"
weight = 1
+++

## Motivation

The motivation of use inline expression is make configuration simplification and unification.

In complicated sharding rule configuration, with more and more data nodes, lots of duplicated configuration will difficult to maintain. Use inline expression can simplify them.

For regular sharding algorithm, use java code to implement them can not management together. Use inline expression can save all configuration at same place.

## Usage

Use `${ expression }` or`$->{ expression }` on your configuration. It can support data node and sharding algorithm configuration.

## Syntax

`${begin..end}` means range 

`${[unit1, unit2, unit_x]}` means enumeration

If there are more than one inline expression continuously, it means all expression will cartesian each other.
For example:

```groovy
${['online', 'offline']}_table${1..3}
```

Will be：

```
online_table1, online_table2, online_table3, offline_table1, offline_table2, offline_table3
```

### Data Node Configuration

For uniform distribution, database schema are: 

```
db0
  ├── t_order0 
  └── t_order1 
db1
  ├── t_order0 
  └── t_order1
```

Use inline expression can simplify to: 

```
db${0..1}.t_order${0..1}
```

Or

```
db$->{0..1}.t_order$->{0..1}
```

For user-defined distribution, database schema are: 

```
db0
  ├── t_order0 
  └── t_order1 
db1
  ├── t_order2
  ├── t_order3
  └── t_order4
```

Use inline expression can simplify to: 

```
db0.t_order${0..1},db1.t_order${2..4}
```

Or

```
db0.t_order$->{0..1},db1.t_order$->{2..4}
```

### Sharding Algorithm Configuration

For algorithm use single sharding column and use `=` or `IN` in SQL only, can use inline expression to instead of java class. 

Inline expression is a segment of `groovy` codes. It should return actual data source name or table names during run algorithm.
For example, sharding for 10 data source by mantissa of id, if mantissa is zero should route to ds0, if mantissa is one should route to ds1, and so on, inline expression should be:

```groovy 
ds${id % 10}
```

Or

```groovy 
ds$->{id % 10}
```
