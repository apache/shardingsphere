+++
title = "ALTER SHARDING TABLE REFERENCE RULE"
weight = 13
+++

## 描述

`ALTER SHARDING TABLE REFERENCE RULE` 语法用于修改分片表关联关系。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
AlterShardingTableReferenceRule ::=
  'ALTER' 'SHARDING' 'TABLE' 'REFERENCE' 'RULE'  referenceRelationshipDefinition  (',' referenceRelationshipDefinition)*

referenceRelationshipDefinition ::=
  ruleName '(' tableName (',' tableName)* ')'

ruleName ::=
  identifier

tableName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 一张分片表只能具有一个关联关系；
- 关联的分片表应分布在相同的存储单元，并且分片个数相同。例如 `ds_${0..1}.t_order_${0..1}` 与 `ds_${0..1}.t_order_item_${0..1}`；
- 关联的分片表应使用一致的分片算法。例如 `t_order_${order_id % 2}` 与 `t_order_item_${order_item_id % 2}`；

### 示例

#### 1.修改关联关系

```sql
ALTER SHARDING TABLE REFERENCE RULE ref_0 (t_order,t_order_item);
```

#### 2.修改多个关联关系

```sql
ALTER SHARDING TABLE REFERENCE RULE ref_0 (t_order,t_order_item), ref_1 (t_product,t_product_item);
```

### 保留字

`ALTER`、`SHARDING`、`TABLE`、`REFERENCE`、`RULE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [CREATE SHARDING TABLE RULE](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/sharding/create-sharding-table-rule/)
