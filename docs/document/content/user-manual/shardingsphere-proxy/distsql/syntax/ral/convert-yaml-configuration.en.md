+++
title = "CONVERT YAML CONFIGURATION"
weight = 15
+++

### Description

The `CONVERT YAML CONFIGURATION` syntax is used to convert `YAML` configuration to DistSQL RDL statements.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
convertYamlConfiguration ::=
  'CONVERT' 'YAML' 'CONFIGURATION' 'FROM' 'FILE' filePath

filePath ::=
  string
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- The `CONVERT YAML CONFIGURATION` syntax only reads the YAML file and converts the configuration into DistSQL statements without affecting the current metadata;
- When `dataSources` in YAML is empty, `rules` conversion will not be performed.

### Example

```sql
mysql> CONVERT YAML CONFIGURATION FROM FILE '/xxx/config_sharding_db.yaml';
+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| dist_sql                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| CREATE DATABASE sharding_db;
USE sharding_db;

REGISTER STORAGE UNIT ds_0 (
URL='jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true',
USER='root',
PASSWORD='123456',
PROPERTIES('maxPoolSize'='10')
), ds_1 (
URL='jdbc:mysql://127.0.0.1:3306/demo_ds_1?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true',
USER='root',
PASSWORD='123456',
PROPERTIES('maxPoolSize'='10')
);

CREATE SHARDING TABLE RULE t_order (
STORAGE_UNITS(ds_0,ds_1),
SHARDING_COLUMN=order_id,
TYPE(NAME='mod', PROPERTIES('sharding-count'='4')),
KEY_GENERATE_STRATEGY(COLUMN=order_id, TYPE(NAME='snowflake'))
);

|
+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
1 row in set (0.02 sec)
```

### Reserved word

`CONVERT`, `YAML`, `CONFIGURATION`, `FROM`, `FILE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
