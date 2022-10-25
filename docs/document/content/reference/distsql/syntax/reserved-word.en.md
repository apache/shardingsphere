+++
title = "Reserved word"
weight = 3
+++

## RDL

### Basic Reserved Words

`CREATE`, `ALTER`, `DROP`, `TABLE`, `RULE`, `TYPE`, `NAME`, `PROPERTIES`

### Resource Definition

`ADD`, `RESOURCE`, `IF`, `EXISTS`, `HOST`, `PORT`, `DB`, `USER`, `PASSWORD`, `URL`
, `IGNORE`, `SINGLE`, `TABLES`

### Rule Definition

#### SHARDING

`DEFAULT`, `SHARDING`, `BROADCAST`, `BINDING`, `DATABASE`, `STRATEGY`, `RULES`, `ALGORITHM`
, `DATANODES`, `DATABASE_STRATEGY`, `TABLE_STRATEGY`, `KEY_GENERATE_STRATEGY`, `RESOURCES`, `SHARDING_COLUMN`, `KEY`
, `GENERATOR`, `SHARDING_COLUMNS`, `KEY_GENERATOR`, `SHARDING_ALGORITHM`, `COLUMN`, `AUDIT_STRATEGY`
, `AUDITORS`, `ALLOW_HINT_DISABLE`

#### Single Table

`SHARDING`, `SINGLE`, `RESOURCE`

#### Readwrite Splitting

`READWRITE_SPLITTING`, `WRITE_RESOURCE`, `READ_RESOURCES`, `AUTO_AWARE_RESOURCE`
, `WRITE_DATA_SOURCE_QUERY_ENABLED`

#### Encrypt

`ENCRYPT`, `COLUMNS`, `CIPHER`, `PLAIN`, `QUERY_WITH_CIPHER_COLUMN`

#### Database Discovery

`DB_DISCOVERY`, `RESOURCES`, `HEARTBEAT`

#### Shadow

`SHADOW`, `DEFAULT`, `ALGORITHM`, `SOURCE`, `SHADOW`

## RQL

### Basic Reserved Words

`SHOW`, `DEFAULT`, `RULE`, `RULES`, `TABLE`, `DATABASE`, `FROM`

### Resource Definition

`RESOURCES`, `UNUSED`, `USED`

### Rule Definition

#### SHARDING

`UNUSED`, `SHARDING`, `ALGORITHMS`

#### Single Table

`SINGLE`, `STORAGE`, `UNIT`

## Supplement

- The above reserved words are not case-sensitive