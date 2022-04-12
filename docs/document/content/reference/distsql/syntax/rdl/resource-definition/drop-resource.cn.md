+++
title = "DROP RESOURCE"
weight = 3
+++

### 描述

`DROP RESOURCE` 语法用于从当前逻辑库中移除资源。 

### 语法
```SQL
DropResource ::=
  'DROP' 'RESOURCE' ( 'IF' 'EXISTS' )? dataSourceName  ( ',' dataSourceName )* ( 'IGNORE' 'SINGLE' 'TABLES' )?
```

 ### 补充说明

- `DROP RESOURCE` 只会移除 Proxy 中的资源，不会删除与资源对应的真实数据源
- 无法移除已经被规则使用的资源
- 移除的资源中包含单表时，需要添加 `IGNORE SINGLE TABLES` 忽略单表移除资源
- `IF EXISTS` 关键字用于判断移除的资源是否存在，资源存在时才执行移除操作

 ### 示例
- 移除资源
```SQL
DROP RESOURCE ds_0;
```

- 移除多个资源
```SQL
DROP RESOURCE ds_1, ds_2;
```

- 移除包含单表的资源
```SQL
DROP RESOURCE ds_3 IGNORE SINGLE TABLES;
```

- 如果资源存在则移除
```SQL
DROP RESOURCE IF EXISTS ds_4;
```

### 保留字

    DROP、 RESOURCE、 IF、 EXISTS、 IGNORE、 SINGLE、 TABLES

 ### 相关链接
- [保留字](/cn/reference/distsql/syntax/reserved-word/)