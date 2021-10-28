+++
title = "数据节点"
weight = 2
+++

数据分片的最小单元，由数据源名称和真实表组成。
例：`ds_0.t_order_0`。

逻辑表与真实表的映射关系，可分为均匀分布和自定义分布两种形式。

## 均匀分布

指数据表在每个数据源内呈现均匀分布的态势，
例如：

```
db0
  ├── t_order0
  └── t_order1
db1
  ├── t_order0
  └── t_order1
```

数据节点的配置如下：

```
db0.t_order0, db0.t_order1, db1.t_order0, db1.t_order1
```

## 自定义分布

指数据表呈现有特定规则的分布，
例如：

```
db0
  ├── t_order0
  └── t_order1
db1
  ├── t_order2
  ├── t_order3
  └── t_order4
```

数据节点的配置如下：

```
db0.t_order0, db0.t_order1, db1.t_order2, db1.t_order3, db1.t_order4
```
