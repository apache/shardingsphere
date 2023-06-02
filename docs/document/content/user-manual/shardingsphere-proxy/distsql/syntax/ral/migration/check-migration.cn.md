+++
title = "CHECK MIGRATION BY"
weight = 10

+++

### 描述

`CHECK MIGRATION BY` 语法用于校验数据迁移作业中的数据一致性。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowMigrationList ::=
  'CHECK' 'MIGRATION' migrationJobId 'BY' 'TYPE' '(' 'NAME' '=' migrationCheckAlgorithmType ')'

migrationJobId ::=
  string

migrationCheckAlgorithmType ::=
  string
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `migrationJobId` 需要通过 [SHOW MIGRATION LIST](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-list/) 语法查询获得

- `migrationCheckAlgorithmType` 需要通过 [SHOW MIGRATION CHECK ALGORITHMS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-check-algorithm/) 语法查询获得

### 示例

- 校验数据迁移作业中数据一致性

```sql
CHECK MIGRATION 'j01016e501b498ed1bdb2c373a2e85e2529a6' BY TYPE (NAME='CRC32_MATCH');
```

### 保留字

`CHECK`、`MIGRATION`、`BY`、`TYPE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW MIGRATION LIST](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-list/)
- [SHOW MIGRATION CHECK ALGORITHMS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-check-algorithm/)