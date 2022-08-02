+++
title = "Shadow Algorithm"
weight = 6
+++

## Background

The shadow DB feature carries out shadow measurement to SQL statements executed. Shadow measurement supports two types of algorithms, and users can choose one or a combination of them based on actual business needs.

## Parameters

### Column-based shadow algorithm

#### Column value matching shadow algorithm

Type：VALUE_MATCH

| *Attribute Name* | *Data Type* | *Description*                                       |
| ---------------- | ----------- | --------------------------------------------------- |
| column           | String      | shadow column                                       |
| operation        | String      | SQL operation type (INSERT, UPDATE, DELETE, SELECT) |
| value            | String      | value matched by shadow column                      |

#### Column-based Regex matching algorithm

Type：REGEX_MATCH

| *Attribute Name* | *Data Type* | *Description*                                      |
| ---------------- | ----------- | -------------------------------------------------- |
| column           | String      | match a column                                     |
| operation        | String      | SQL operation type（INSERT, UPDATE, DELETE, SELECT）|
| regex            | String      | shadow column matching Regex                       |

### Hint-based shadow algorithm

#### Simple Hint matching shadow algorithm

Type：SIMPLE_HINT

| *Attribute Name* | *Data Type* | *Description* |
| ---------------- | ----------- | ------------- |
| foo              | String      | bar           |

## Configuration sample

- Java API

```java
public final class ShadowConfiguration {
    // ...
    
    private AlgorithmConfiguration createShadowAlgorithmConfiguration() {
        Properties userIdInsertProps = new Properties();
        userIdInsertProps.setProperty("operation", "insert");
        userIdInsertProps.setProperty("column", "user_id");
        userIdInsertProps.setProperty("value", "1");
        return new AlgorithmConfiguration("VALUE_MATCH", userIdInsertProps));
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

- Spring Boot Starter:

```properties
spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-insert-algorithm.type=VALUE_MATCH
spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-insert-algorithm.props.operation=insert
spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-insert-algorithm.props.column=user_id
spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-insert-algorithm.props.value=1
```

- Spring Namespace:

```xml
<shadow:shadow-algorithm id="user-id-insert-algorithm" type="VALUE_MATCH">
    <props>
        <prop key="operation">insert</prop>
        <prop key="column">user_id</prop>
        <prop key="value">1</prop>
    </props>
</shadow:shadow-algorithm>
```
