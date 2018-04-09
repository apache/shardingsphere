+++
toc = true
title = "背景说明"
weight = 3
+++


## 数据库模式

本文档中提供了两个数据源db0和db1，每个数据源之中包含了两组表t_order_0和t_order_1，t_order_item_0和t_order_item_1。这两组表的建表语句为：

```sql
CREATE TABLE IF NOT EXISTS t_order_x (
  order_id INT NOT NULL,
  user_id  INT NOT NULL,
  PRIMARY KEY (order_id)
);
CREATE TABLE IF NOT EXISTS t_order_item_x (
  item_id  INT NOT NULL,
  order_id INT NOT NULL,
  user_id  INT NOT NULL,
  PRIMARY KEY (item_id)
);
```

## 逻辑表与实际表映射关系

### 均匀分布

数据表在每个数据源内呈现均匀分布的态势：

```
db0
  ├── t_order_0 
  └── t_order_1 
db1
  ├── t_order_0 
  └── t_order_1
```

Sharding-JDBC可以支持多种规则配置的方式，以下以最为简单和通用的yaml举例。

真实数据节点的配置如下：

```yaml
    t_order:
        actualDataNodes: db0.t_order_0, db0.t_order_1, db1.t_order_0, db1.t_order_1
```

也可以通过inline表达式简化配置：

```yaml
    t_order:
        actualDataNodes: db${0..1}.t_order_${0..1}
```

### 自定义分布

数据表呈现有特定规则的分布：

```
db0
  ├── t_order_0 
  └── t_order_1 
db1
  ├── t_order_2
  ├── t_order_3
  └── t_order_4
```

表规则可以指定每张表在数据源中的分布情况：

```yaml
    t_order:
        actualDataNodes: db0.t_order_0, db0.t_order_1, db1.t_order_2, db1.t_order_3, db1.t_order_4
```

同样可以通过inline表达式简化配置：

```yaml
    t_order:
        actualDataNodes: db0.t_order_${0..1},db1.t_order_${2..4}
```

### 本教程采用的数据分布例子：

```
db0
  ├── t_order_0               user_id为偶数   order_id为偶数
  ├── t_order_1               user_id为偶数   order_id为奇数
  ├── t_order_item_0          user_id为偶数   order_id为偶数
  └── t_order_item_1          user_id为偶数   order_id为奇数
db1
  ├── t_order_0               user_id为奇数   order_id为偶数
  ├── t_order_1               user_id为奇数   order_id为奇数
  ├── t_order_item_0          user_id为奇数   order_id为偶数
  └── t_order_item_1          user_id为奇数   order_id为奇数
```

其中原始SQL中的t_order就是 __逻辑表__，而转换后的db0.t_order_0就是 __实际表__。
