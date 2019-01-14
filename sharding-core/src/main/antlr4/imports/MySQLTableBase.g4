grammar MySQLTableBase;

import MySQLKeyword, Keyword, MySQLBase, BaseRule, DataType, Symbol;

columnDefinition
    : columnName dataType (dataTypeOption* | dataTypeGenerated)?
    ;
    
dataType
    : typeName dataTypeLength? characterSet? collateClause? UNSIGNED? ZEROFILL?
    | typeName (LP_ STRING (COMMA STRING)* RP_ characterSet? collateClause?)
    ;
    
typeName
    : DOUBLE PRECISION | ID
    ;
    
dataTypeOption
    : dataTypeGeneratedOption
    | DEFAULT? defaultValue
    | AUTO_INCREMENT
    | COLUMN_FORMAT (FIXED | DYNAMIC | DEFAULT)
    | STORAGE (DISK | MEMORY | DEFAULT)
    | referenceDefinition
    ;
    
dataTypeGeneratedOption
    : NULL | NOT NULL | UNIQUE KEY? | primaryKey | COMMENT STRING
    ;
    
defaultValue
    : NULL
    | NUMBER
    | STRING
    | currentTimestampType (ON UPDATE currentTimestampType)?
    | ON UPDATE currentTimestampType
    ;
    
currentTimestampType
    : (CURRENT_TIMESTAMP | LOCALTIME | LOCALTIMESTAMP | NOW | NUMBER) dataTypeLength?
    ;
    
referenceDefinition
    : REFERENCES tableName keyPartsWithParen (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)? referenceType*
    ;
    
referenceType
    : ON (UPDATE | DELETE) referenceOption
    ;
    
referenceOption
    : RESTRICT | CASCADE | SET NULL | NO ACTION | SET DEFAULT
    ;
    
dataTypeGenerated
    : (GENERATED ALWAYS)? AS expr (VIRTUAL | STORED)? dataTypeGeneratedOption*
    ;
    
constraintDefinition
    : (CONSTRAINT symbol?)? (primaryKeyOption | uniqueOption | foreignKeyOption)
    ;
    
primaryKeyOption
    : primaryKey indexType? columnList indexOption*
    ;
    
uniqueOption
    : UNIQUE indexAndKey? indexName? indexType? keyPartsWithParen indexOption*
    ;
    
foreignKeyOption
    : FOREIGN KEY indexName? columnNamesWithParen referenceDefinition
    ;
    
indexDefinition
    : (FULLTEXT | SPATIAL)? indexAndKey? indexName? indexType? keyPartsWithParen indexOption*
    ;
    
tableOptions
    : tableOption (COMMA? tableOption)*
    ;
    
tableOption
    : AUTO_INCREMENT EQ_? NUMBER
    | AVG_ROW_LENGTH EQ_? NUMBER
    | DEFAULT? (characterSet | collateClause)
    | CHECKSUM EQ_? NUMBER
    | COMMENT EQ_? STRING
    | COMPRESSION EQ_? STRING
    | CONNECTION EQ_? STRING
    | (DATA | INDEX) DIRECTORY EQ_? STRING
    | DELAY_KEY_WRITE EQ_? NUMBER
    | ENCRYPTION EQ_? STRING
    | ENGINE EQ_? engineName
    | INSERT_METHOD EQ_? (NO | FIRST | LAST)
    | KEY_BLOCK_SIZE EQ_? NUMBER
    | MAX_ROWS EQ_? NUMBER
    | MIN_ROWS EQ_? NUMBER
    | PACK_KEYS EQ_? (NUMBER | DEFAULT)
    | PASSWORD EQ_? STRING
    | ROW_FORMAT EQ_? (DEFAULT | DYNAMIC | FIXED | COMPRESSED | REDUNDANT | COMPACT)
    | STATS_AUTO_RECALC EQ_? (DEFAULT | NUMBER)
    | STATS_PERSISTENT EQ_? (DEFAULT | NUMBER)
    | STATS_SAMPLE_PAGES EQ_? NUMBER
    | TABLESPACE tablespaceName (STORAGE (DISK | MEMORY | DEFAULT))?
    | UNION EQ_? tableList
    ;
    
engineName
    : ID | MEMORY
    ;
    
partitionOptions
    : PARTITION BY (linearPartition | rangeOrListPartition) (PARTITIONS NUMBER)?
    (SUBPARTITION BY linearPartition (SUBPARTITIONS NUMBER)? )? (LP_ partitionDefinitions RP_)?
    ;
    
linearPartition
    : LINEAR? (HASH (yearFunctionExpr | expr) | KEY (ALGORITHM EQ_ NUMBER)? columnNamesWithParen)
    ;
    
yearFunctionExpr
    : LP_ YEAR expr RP_
    ;
    
rangeOrListPartition
    : (RANGE | LIST) (expr | COLUMNS columnNamesWithParen)
    ;
    
partitionDefinitions
    : partitionDefinition (COMMA partitionDefinition)*
    ;
    
partitionDefinition
    : PARTITION partitionName
    (VALUES (lessThanPartition | IN valueListWithParen))?
    partitionDefinitionOption*
    (LP_ subpartitionDefinition (COMMA subpartitionDefinition)* RP_)?
    ;
    
partitionDefinitionOption
    : STORAGE? ENGINE EQ_? engineName
    | COMMENT EQ_? STRING
    | DATA DIRECTORY EQ_? STRING
    | INDEX DIRECTORY EQ_? STRING
    | MAX_ROWS EQ_? NUMBER
    | MIN_ROWS EQ_? NUMBER
    | TABLESPACE EQ_? tablespaceName
    ;
    
lessThanPartition
    : LESS THAN (LP_ (expr | valueList) RP_ | MAXVALUE)
    ;
    
subpartitionDefinition
    : SUBPARTITION partitionName partitionDefinitionOption*
    ;
