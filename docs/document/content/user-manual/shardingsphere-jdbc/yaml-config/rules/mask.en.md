+++
title = "Data Masking"
weight = 6
+++

## Background

The YAML configuration approach to data masking is highly readable, with the YAML format enabling a quick understanding of dependencies between mask rules.
Based on the YAML configuration, ShardingSphere automatically completes the creation of `ShardingSphereDataSource` objects, reducing unnecessary coding efforts for users.

## Parameters

```yaml
rules:
- !MASK
  tables:
    <table_name> (+): # Mask table name
      columns:
        <column_name> (+): # Mask logic column name
          maskAlgorithm: # Mask algorithm name

  # Mask algorithm configuration
  maskAlgorithms:
    <mask_algorithm_name> (+): # Mask algorithm name
      type: # Mask algorithm type
      props: # Mask algorithm properties
      # ...
```

Please refer to [Built-in Mask Algorithm List](/en/user-manual/common-config/builtin-algorithm/mask) for more details about type of algorithm.

## Procedure

1. Configure data masking rules in the YAML file, including data sources, mask rules, global attributes, and other configuration items.
2. Using the `createDataSource` of calling the `YamlShardingSphereDataSourceFactory` object to create `ShardingSphereDataSource` based on the configuration information in the YAML file.

## Sample

The data masking YAML configurations are as follows:

```yaml
dataSources:
  unique_ds:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    standardJdbcUrl: jdbc:mysql://localhost:3306/demo_ds?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    username: root
    password:

rules:
- !MASK
  tables:
    t_user:
      columns:
        password:
          maskAlgorithm: md5_mask
        email:
          maskAlgorithm: mask_before_special_chars_mask
        telephone:
          maskAlgorithm: keep_first_n_last_m_mask

  maskAlgorithms:
    md5_mask:
      type: MD5
    mask_before_special_chars_mask:
      type: MASK_BEFORE_SPECIAL_CHARS
      props:
        special-chars: '@'
        replace-char: '*'
    keep_first_n_last_m_mask:
      type: KEEP_FIRST_N_LAST_M
      props:
        first-n: 3
        last-m: 4
        replace-char: '*'
```

Read the YAML configuration to create a data source according to the `createDataSource` method of `YamlShardingSphereDataSourceFactory`.

```java
YamlShardingSphereDataSourceFactory.createDataSource(getFile());
```

## Related References

- [Core Feature: Data Masking](/en/features/mask/)
- [Developer Guide: Data Masking](/en/dev-manual/mask/)
