grammar MySQLTableBase;

import MySQLKeyword, Keyword, MySQLBase, BaseRule, DataType, Symbol;

columnDefinition
    : columnName dataType (dataTypeOption* | dataTypeGenerated?)
    ;
    
dataType
    : typeName dataTypeLength? characterSet? collateClause? UNSIGNED? ZEROFILL?
    | typeName (LP_ STRING_ (COMMA_ STRING_)* RP_ characterSet? collateClause?)
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
    : NULL | NOT NULL | UNIQUE KEY? | primaryKey | COMMENT STRING_
    ;
    
defaultValue
    : NULL
    | NUMBER_
    | STRING_
    | currentTimestampType (ON UPDATE currentTimestampType)?
    | ON UPDATE currentTimestampType
    ;
    
currentTimestampType
    : (CURRENT_TIMESTAMP | LOCALTIME | LOCALTIMESTAMP | NOW | NUMBER_) dataTypeLength?
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
    : tableOption (COMMA_? tableOption)*
    ;
    
tableOption
    : AUTO_INCREMENT EQ_? NUMBER_
    | AVG_ROW_LENGTH EQ_? NUMBER_
    | DEFAULT? (characterSet | collateClause)
    | CHECKSUM EQ_? NUMBER_
    | COMMENT EQ_? STRING_
    | COMPRESSION EQ_? STRING_
    | CONNECTION EQ_? STRING_
    | (DATA | INDEX) DIRECTORY EQ_? STRING_
    | DELAY_KEY_WRITE EQ_? NUMBER_
    | ENCRYPTION EQ_? STRING_
    | ENGINE EQ_? engineName
    | INSERT_METHOD EQ_? (NO | FIRST | LAST)
    | KEY_BLOCK_SIZE EQ_? NUMBER_
    | MAX_ROWS EQ_? NUMBER_
    | MIN_ROWS EQ_? NUMBER_
    | PACK_KEYS EQ_? (NUMBER_ | DEFAULT)
    | PASSWORD EQ_? STRING_
    | ROW_FORMAT EQ_? (DEFAULT | DYNAMIC | FIXED | COMPRESSED | REDUNDANT | COMPACT)
    | STATS_AUTO_RECALC EQ_? (DEFAULT | NUMBER_)
    | STATS_PERSISTENT EQ_? (DEFAULT | NUMBER_)
    | STATS_SAMPLE_PAGES EQ_? NUMBER_
    | TABLESPACE tablespaceName (STORAGE (DISK | MEMORY | DEFAULT))?
    | UNION EQ_? tableList
    ;
    
engineName
    : ID | MEMORY
    ;
    
partitionOptions
    : PARTITION BY (linearPartition | rangeOrListPartition) (PARTITIONS NUMBER_)?
    (SUBPARTITION BY linearPartition (SUBPARTITIONS NUMBER_)? )? (LP_ partitionDefinitions RP_)?
    ;
    
linearPartition
    : LINEAR? (HASH (yearFunctionExpr | expr) | KEY (ALGORITHM EQ_ NUMBER_)? columnNamesWithParen)
    ;
    
yearFunctionExpr
    : LP_ YEAR expr RP_
    ;
    
rangeOrListPartition
    : (RANGE | LIST) (expr | COLUMNS columnNamesWithParen)
    ;
    
partitionDefinitions
    : partitionDefinition (COMMA_ partitionDefinition)*
    ;
    
partitionDefinition
    : PARTITION partitionName
    (VALUES (lessThanPartition | IN assignmentValueList))?
    partitionDefinitionOption*
    (LP_ subpartitionDefinition (COMMA_ subpartitionDefinition)* RP_)?
    ;
    
partitionDefinitionOption
    : STORAGE? ENGINE EQ_? engineName
    | COMMENT EQ_? STRING_
    | DATA DIRECTORY EQ_? STRING_
    | INDEX DIRECTORY EQ_? STRING_
    | MAX_ROWS EQ_? NUMBER_
    | MIN_ROWS EQ_? NUMBER_
    | TABLESPACE EQ_? tablespaceName
    ;
    
lessThanPartition
    : LESS THAN (LP_ (expr | assignmentValues) RP_ | MAXVALUE)
    ;
    
subpartitionDefinition
    : SUBPARTITION partitionName partitionDefinitionOption*
    ;
