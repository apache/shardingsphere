+++
title = "影子算法"
weight = 6
+++

## 背景信息

影子库功能对执行的 SQL 语句进行影子判定。影子判定支持两种类型算法，用户可根据实际业务需求选择一种或者组合使用。

## 参数解释

### 列影子算法

#### 列值匹配算法

类型：VALUE_MATCH

| *属性名称*    | *数据类型* | *说明*                                     |
|-----------|--------|------------------------------------------|
| column    | String | 影子列                                      |
| operation | String | SQL 操作类型（INSERT, UPDATE, DELETE, SELECT) |
| value     | String | 影子列匹配的值                                  |

####  列正则表达式匹配算法

类型：REGEX_MATCH

| *属性名称*    | *数据类型* | *说明*                                     |
|-----------|--------|------------------------------------------|
| column    | String | 匹配列                                      |
| operation | String | SQL 操作类型(INSERT, UPDATE, DELETE, SELECT) |
| regex     | String | 影子列匹配正则表达式                               |

### Hint 影子算法

####  SQL HINT 影子算法

类型：SQL_HINT
```sql
/* SHARDINGSPHERE_HINT: SHADOW=true */
```

## 配置示例

- Java API

```java
public final class ShadowConfiguration {
    // ...
    
    private AlgorithmConfiguration createShadowAlgorithmConfiguration() {
        Properties userIdInsertProps = new Properties();
        userIdInsertProps.setProperty("operation", "insert");
        userIdInsertProps.setProperty("column", "user_id");
        userIdInsertProps.setProperty("value", "1");
        return new AlgorithmConfiguration("VALUE_MATCH", userIdInsertProps);
    }
    
    // ...
}
```

- YAML:

```yaml
shadowAlgorithms:
  user-id-insert-algorithm:
    type: VALUE_MATCH
    props:
      column: user_id
      operation: insert
      value: 1
```
