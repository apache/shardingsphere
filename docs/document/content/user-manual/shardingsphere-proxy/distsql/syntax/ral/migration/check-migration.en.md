+++
title = "CHECK MIGRATION"
weight = 10
+++

### Description

The `CHECK MIGRATION` syntax is used to check data consistency in migration job.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CheckMigration ::=
  'CHECK' 'MIGRATION' migrationJobId ('BY' algorithmDefinition)?

migrationJobId ::=
  integer | identifier | string

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' migrationCheckAlgorithmType (',' propertiesDefinition)? ')'

migrationCheckAlgorithmType ::=
  string

propertiesDefinition ::=
  'PROPERTIES' '(' (key '=' value (',' key '=' value)*)? ')'

key ::=
  string

value ::=
  literal
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `migrationJobId` needs to be obtained through [SHOW MIGRATION LIST](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-list/) syntax query

- When the `BY` clause is specified, `migrationCheckAlgorithmType` needs to be obtained through [SHOW MIGRATION CHECK ALGORITHMS](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-check-algorithm/) syntax query

### Example

- Check data consistency in migration job

```sql
CHECK MIGRATION 'j01016e501b498ed1bdb2c373a2e85e2529a6' BY TYPE (NAME='CRC32_MATCH');
```

### Reserved word

`CHECK`, `MIGRATION`, `BY`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW MIGRATION LIST](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-list/)
- [SHOW MIGRATION CHECK ALGORITHMS](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-check-algorithm/)
