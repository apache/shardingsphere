grammar MySQLTableBase;

import MySQLKeyword, Keyword, MySQLBase, BaseRule, DataType, Symbol;

columnDefinition
    : columnName dataType (dataTypeOption* | dataTypeGenerated)?
    ;

dataType
    : typeName dataTypeLength? characterSet? collateClause? UNSIGNED? ZEROFILL?
    | typeName (LEFT_PAREN STRING (COMMA STRING)* RIGHT_PAREN characterSet? collateClause?)
    ;
 
 typeName
    : DOUBLE PRECISION
    | ID
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
    : nullNotnull
    | UNIQUE KEY?
    | primaryKey
    | COMMENT STRING
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
    : REFERENCES tableName keyPartsWithParen
    (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)?
    referenceType*
    ;

referenceType
    : ON UPDATE referenceOption
    | ON DELETE referenceOption
    ;

referenceOption
    : RESTRICT
    | CASCADE
    | SET NULL
    | NO ACTION
    | SET DEFAULT
    ;

dataTypeGenerated
    : (GENERATED ALWAYS)? AS expr
    (VIRTUAL | STORED)?
    dataTypeGeneratedOption*
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
    : AUTO_INCREMENT EQ_OR_ASSIGN? NUMBER
    | AVG_ROW_LENGTH EQ_OR_ASSIGN? NUMBER
    | DEFAULT? (characterSet | collateClause)
    | CHECKSUM EQ_OR_ASSIGN? NUMBER
    | COMMENT EQ_OR_ASSIGN? STRING
    | COMPRESSION EQ_OR_ASSIGN? STRING
    | CONNECTION EQ_OR_ASSIGN? STRING
    | (DATA | INDEX) DIRECTORY EQ_OR_ASSIGN? STRING
    | DELAY_KEY_WRITE EQ_OR_ASSIGN? NUMBER
    | ENCRYPTION EQ_OR_ASSIGN? STRING
    | ENGINE EQ_OR_ASSIGN? engineName
    | INSERT_METHOD EQ_OR_ASSIGN? (NO | FIRST | LAST)
    | KEY_BLOCK_SIZE EQ_OR_ASSIGN? NUMBER
    | MAX_ROWS EQ_OR_ASSIGN? NUMBER
    | MIN_ROWS EQ_OR_ASSIGN? NUMBER
    | PACK_KEYS EQ_OR_ASSIGN? (NUMBER | DEFAULT)
    | PASSWORD EQ_OR_ASSIGN? STRING
    | ROW_FORMAT EQ_OR_ASSIGN? (DEFAULT | DYNAMIC | FIXED | COMPRESSED | REDUNDANT | COMPACT)
    | STATS_AUTO_RECALC EQ_OR_ASSIGN? (DEFAULT | NUMBER)
    | STATS_PERSISTENT EQ_OR_ASSIGN? (DEFAULT | NUMBER)
    | STATS_SAMPLE_PAGES EQ_OR_ASSIGN? NUMBER
    | TABLESPACE tablespaceName (STORAGE (DISK | MEMORY | DEFAULT))?
    | UNION EQ_OR_ASSIGN? tableNamesWithParen
    ;

engineName
    : ID
    | MEMORY
    ;

partitionOptions
    : PARTITION BY (linearPartition | rangeOrListPartition)
    (PARTITIONS NUMBER)?
    (SUBPARTITION BY linearPartition (SUBPARTITIONS NUMBER)? )?
    (LEFT_PAREN partitionDefinitions RIGHT_PAREN)?
    ;

//hash(YEAR(col)) YEAR is keyword which does not match expr
linearPartition
    : LINEAR? (HASH (yearFunctionExpr | expr) | KEY (ALGORITHM EQ_OR_ASSIGN NUMBER)? columnNamesWithParen)
    ;

yearFunctionExpr
    : LEFT_PAREN YEAR expr RIGHT_PAREN
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
    (LEFT_PAREN subpartitionDefinition (COMMA subpartitionDefinition)* RIGHT_PAREN)?
    ;

partitionDefinitionOption
    : (STORAGE)? ENGINE EQ_OR_ASSIGN? engineName
    | COMMENT EQ_OR_ASSIGN? STRING
    | DATA DIRECTORY EQ_OR_ASSIGN? STRING
    | INDEX DIRECTORY EQ_OR_ASSIGN? STRING
    | MAX_ROWS EQ_OR_ASSIGN? NUMBER
    | MIN_ROWS EQ_OR_ASSIGN? NUMBER
    | TABLESPACE EQ_OR_ASSIGN? tablespaceName
    ;

lessThanPartition
    : LESS THAN (LEFT_PAREN (expr | valueList) RIGHT_PAREN | MAXVALUE)
    ;

subpartitionDefinition
    : SUBPARTITION partitionName
    partitionDefinitionOption*
    ;