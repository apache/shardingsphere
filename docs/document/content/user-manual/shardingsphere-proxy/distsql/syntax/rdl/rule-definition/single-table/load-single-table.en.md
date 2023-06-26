+++
title = "LOAD SINGLE TABLE"
weight = 1
+++

## Description

The `LOAD SINGLE TABLE` syntax is used to load single table from storage unit.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
loadSingleTable ::=
  'LOAD' 'SINGLE' 'TABLE' tableDefinition

tableDefinition ::=
  tableIdentifier (',' tableIdentifier)*

tableIdentifier ::=
  '*.*' | '*.*.*' | storageUnitName '.*' | storageUnitName '.*.*' | storageUnitName '.' schemaName '.*' | storageUnitName '.' tableName | storageUnitName '.' schemaName '.' tableName

storageUnitName ::=
  identifier

schemaName ::=
  identifier

tableName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- support specifying schemaName in PostgreSQL and OpenGauss protocols


### Example

- Load specified single table

```sql
LOAD SINGLE TABLE ds_0.t_single;
```

- Load all single tables in the specified storage unit

```sql
LOAD SINGLE TABLE ds_0.*;
```

- Load all single tables

```sql
LOAD SINGLE TABLE *.*;
```

### Reserved word

`LOAD`, `SINGLE`, `TABLE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
