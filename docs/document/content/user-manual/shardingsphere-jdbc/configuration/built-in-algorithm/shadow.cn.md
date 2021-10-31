+++
title = "影子算法"
weight = 5
+++

## 列影子算法

### 列值匹配影子算法

类型：COLUMN_VALUE_MATCH

可配置属性：

| *属性名称*      | *数据类型* | *说明*  |
| -------------- | --------- | ------- |
| column         | String    | 影子列 |
| operation      | String    | SQL 操作类型（INSERT, UPDATE, DELETE, SELECT） |
| value          | String    | 影子列匹配的值 |

### 列正则表达式匹配影子算法

类型：COLUMN_REGEX_MATCH

可配置属性：

| *属性名称*      | *数据类型* | *说明*  |
| -------------- | --------- | ------- |
| column         | String    | 匹配列 |
| operation      | String    | SQL操作类型（INSERT, UPDATE, DELETE, SELECT） |
| regex          | String    | 影子列匹配正则表达式 |

## 注解影子算法

### 简单SQL注解匹配影子算法

类型：SIMPLE_NOTE

可配置属性：

> 至少配置一组任意的键值对。比如：foo:bar

| *属性名称*        | *数据类型*  | *说明*     |
| --------------  | ---------  | --------- |
| foo             | String     | bar       |
