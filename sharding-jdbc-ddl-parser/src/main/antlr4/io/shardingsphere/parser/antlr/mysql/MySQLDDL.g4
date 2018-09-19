grammar MySQLDDL;
import MySQLKeyword, DataType, Keyword, MySQLDQL, DQLBase,DDLBase, BaseRule,Symbol;

createIndex:
    CREATE (UNIQUE | FULLTEXT | SPATIAL)? INDEX indexName
    indexType?
    ON tableName keyParts
    indexOption?
    (algorithmOption | lockOption)*
    ;
 
dropIndex:
     dropIndexDef ON tableName
    (algorithmOption | lockOption)*
    ;

dropTable:
    DROP TEMPORARY? TABLE (IF EXISTS)?
    tableName (COMMA tableName)* 
    (RESTRICT | CASCADE)?
    ;
 
truncateTable:
     TRUNCATE TABLE? tableName
     ;
     

createTableOptions:
    createTableBasic
    |createTableSelect
    |createTableLike
    ;
    
createTableBasic:
    LEFT_PAREN createDefinitions RIGHT_PAREN
    tableOptions?
    partitionOptions?
    ;
    
createDefinitions:
    createDefinition (COMMA createDefinition)*
    ;
    
createDefinition:
    (columnNameAndDefinition)
    |(constraintDefinition|indexDefinition| checkExpr)
    ;
 
 checkExpr:
    CHECK exprWithParen
    ;
    
createTableSelect:
    (LEFT_PAREN createDefinitions RIGHT_PAREN)?
    tableOptions?
    partitionOptions??
    (IGNORE | REPLACE)?
    AS?
    unionSelect
    ;
    
createTableLike:
    (likeTable) 
    | LEFT_PAREN likeTable RIGHT_PAREN
    ;    

likeTable:
    LIKE tableName
    ;
    
alterSpecifications:
    alterSpecification (COMMA alterSpecification)*
    ;

alterSpecification:
    (tableOptions)
    | ADD COLUMN? (singleColumn | multiColumn)
    | ADD indexDefinition
    | ADD constraintDefinition
    | algorithmOption
    | ALTER COLUMN? columnName (SET DEFAULT | DROP DEFAULT)
    | changeColumn
    | DEFAULT? characterAndCollateWithEqual
    | CONVERT TO characterAndCollate
    | (DISABLE|ENABLE) KEYS
    | (DISCARD|IMPORT_) TABLESPACE
    | dropColumn
    | dropIndexDef
    | dropPrimaryKey
    | DROP FOREIGN KEY fkSymbol
    | FORCE
    | lockOption
    | modifyColumn
    | (ORDER BY columnName (COMMA columnName)* )+ 
    | renameIndex
    | renameTable
    | (WITHOUT|WITH) VALIDATION
    | ADD PARTITION partitionDefinitions
    | DROP PARTITION partitionNames
    | DISCARD PARTITION (partitionNames | ALL) TABLESPACE
    | IMPORT_ PARTITION (partitionNames | ALL) TABLESPACE
    | TRUNCATE PARTITION (partitionNames | ALL)
    | COALESCE PARTITION NUMBER
    | REORGANIZE PARTITION partitionNames INTO partitionDefinitions
    | EXCHANGE PARTITION partitionName WITH TABLE tableName ((WITH|WITHOUT) VALIDATION)?
    | ANALYZE PARTITION (partitionNames | ALL)
    | CHECK PARTITION (partitionNames | ALL)
    | OPTIMIZE PARTITION (partitionNames | ALL)
    | REBUILD PARTITION (partitionNames | ALL)
    | REPAIR PARTITION (partitionNames | ALL)
    | REMOVE PARTITIONING
    | UPGRADE PARTITIONING
    ;

changeColumn:
    changeColumnOp columnName columnName columnDefinition (FIRST|AFTER columnName)?
    ;
    
changeColumnOp:
    CHANGE COLUMN?
    ;
    
dropColumn:
    DROP COLUMN? columnName
    ;
    
dropIndexDef:
    DROP indexAndKey indexName
    ;
    
dropPrimaryKey:
    DROP PRIMARY KEY
    ;
    
renameIndex:
    RENAME indexAndKey indexName TO indexName
    ;

renameTable:    
    RENAME (TO|AS)? tableName
    ;

modifyColumn:    
    MODIFY COLUMN? columnNameAndDefinition (FIRST | AFTER columnName)?
    ;
    
algorithmOption:
    ALGORITHM EQ_OR_ASSIGN? (DEFAULT|INPLACE|COPY)
    ;

lockOption:
    LOCK EQ_OR_ASSIGN? (DEFAULT|NONE|SHARED|EXCLUSIVE)
    ;
    
indexDefinition:
    (((FULLTEXT | SPATIAL) indexAndKey?)|indexAndKey) indexDefOption
    ;
    
indexAndKey:
    INDEX|KEY
    ;

indexDefOption:
    indexName? indexType? keyParts indexOption?
    ;
    
singleColumn:
    columnNameAndDefinition (FIRST | AFTER columnName)?
    ;
    
multiColumn:
    LEFT_PAREN columnNameAndDefinition (COMMA columnNameAndDefinition)* RIGHT_PAREN
    ;

constraintDefinition:
    (CONSTRAINT symbol?)? (primaryKeyOption | uniqueOption | foreignKeyOption)
    ;
    
primaryKeyOption:    
    PRIMARY KEY indexType? keyParts indexOption?
    ;
    
uniqueOption:    
    UNIQUE (INDEX|KEY)? indexName? indexType? keyParts indexOption?
    ;
    
foreignKeyOption:    
    FOREIGN KEY indexName? LEFT_PAREN columnName (COMMA columnName)* RIGHT_PAREN  referenceDefinition
    ;
    
tableOptions:
    tableOption (COMMA? tableOption)*
    ; 

tableOption:
     AUTO_INCREMENT EQ_OR_ASSIGN? NUMBER
    |AVG_ROW_LENGTH EQ_OR_ASSIGN? NUMBER
    | DEFAULT? characterSetWithEqual
    | CHECKSUM EQ_OR_ASSIGN? NUMBER
    | DEFAULT? collateClauseWithEqual
    | COMMENT EQ_OR_ASSIGN? STRING
    | COMPRESSION EQ_OR_ASSIGN? (ZLIB|'LZ4'|NONE)
    | CONNECTION EQ_OR_ASSIGN? STRING
    | (DATA|INDEX) DIRECTORY EQ_OR_ASSIGN? STRING
    | DELAY_KEY_WRITE EQ_OR_ASSIGN? NUMBER
    | ENCRYPTION EQ_OR_ASSIGN? STRING
    | ENGINE EQ_OR_ASSIGN? engineName
    | INSERT_METHOD EQ_OR_ASSIGN? ( NO | FIRST | LAST )
    | KEY_BLOCK_SIZE EQ_OR_ASSIGN? NUMBER
    | MAX_ROWS EQ_OR_ASSIGN? NUMBER
    | MIN_ROWS EQ_OR_ASSIGN? NUMBER
    | PACK_KEYS EQ_OR_ASSIGN? (NUMBER | DEFAULT)
    | PASSWORD EQ_OR_ASSIGN? STRING
    | ROW_FORMAT EQ_OR_ASSIGN? (DEFAULT|DYNAMIC|FIXED|COMPRESSED|REDUNDANT|COMPACT)
    | STATS_AUTO_RECALC EQ_OR_ASSIGN? (DEFAULT|NUMBER)
    | STATS_PERSISTENT EQ_OR_ASSIGN? (DEFAULT|NUMBER)
    | STATS_SAMPLE_PAGES EQ_OR_ASSIGN? NUMBER
    | TABLESPACE tablespaceName (STORAGE (DISK|MEMORY|DEFAULT))?
    | UNION EQ_OR_ASSIGN? idList
    ; 

columnNameAndDefinition:
    columnName columnDefinition
    ;

columnDefinition:
    dataType (dataTypeOption | dataTypeGenerated)
    ;
    
dataType:
    BIT dataTypeLength?
    |((TINYINT | SMALLINT | MEDIUMINT | INT | INTEGER| BIGINT) dataTypeLength? (NOT? NULL)? AUTO_INCREMENT? numberTypeSuffix)
    |((REAL | DOUBLE | FLOAT | DECIMAL | NUMERIC) dataTypeLengthWithPrecision? (NOT? NULL)? AUTO_INCREMENT? numberTypeSuffix )
    |((DATE | TIME) (NOT? NULL)? (DEFAULT (STRING | NULL))?)
    |((timestampType | DATETIME) (NOT? NULL)?  (DEFAULT (currentTimestampType | NUMBER | STRING | NULL))? (ON UPDATE currentTimestampType)? )
    |(YEAR dataTypeLength? (NOT? NULL)?  (DEFAULT (NUMBER | STRING | NULL))?)
    |((CHAR | VARCHAR) dataTypeLength? (NOT? NULL)? characterSet? collateClause? (DEFAULT (STRING | NULL))?)
    |((BINARY | VARBINARY) dataTypeLength? (NOT? NULL)?  (DEFAULT NUMBER |STRING | NULL)? )
    |(TINYBLOB | BLOB | MEDIUMBLOB | LONGBLOB |JSON) (NOT? NULL)? 
    |((TINYTEXT | TEXT | MEDIUMTEXT | LONGTEXT ) (NOT? NULL)?  BINARY ? characterSet? collateClause? )
    |((ENUM | SET) (LEFT_PAREN STRING (COMMA STRING)* RIGHT_PAREN  (NOT? NULL)? (DEFAULT (STRING | NULL))? characterSet? collateClause?))
    ;
    
timestampType:
    TIMESTAMP dataTypeLength?
    ;
    
currentTimestampType:
    CURRENT_TIMESTAMP dataTypeLength?
    ;
    
dataTypeLength:
    (LEFT_PAREN NUMBER RIGHT_PAREN)
    ;

dataTypeLengthWithPrecision:
    (LEFT_PAREN NUMBER COMMA NUMBER RIGHT_PAREN)
    ;

numberTypeSuffix:
    UNSIGNED? ZEROFILL?  (DEFAULT (NUMBER |STRING | NULL))?
    ;
    
dataTypeOption:
    (UNIQUE KEY?)? 
    (PRIMARY? KEY)?
    (COMMENT STRING)?
    (COLUMN_FORMAT (FIXED|DYNAMIC|DEFAULT))?
    (STORAGE (DISK|MEMORY|DEFAULT))?
    (referenceDefinition)?
    ;
 
dataTypeGenerated:
    (GENERATED ALWAYS)? AS LEFT_PAREN expr RIGHT_PAREN
    (VIRTUAL | STORED)? (NOT NULL | NULL)?
    (UNIQUE (KEY)?)? ((PRIMARY)? KEY)?
    (COMMENT STRING)?
    ;
    
referenceDefinition:
    REFERENCES tableName keyParts
    (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)?
    (((ON UPDATE referenceOption)? (ON DELETE referenceOption)?)
          |((ON DELETE referenceOption)? (ON UPDATE referenceOption)?)
    )
    ;
    
referenceOption:
    (RESTRICT | CASCADE | SET NULL | NO ACTION | SET DEFAULT)
    ;
        
symbol:
    ID
    ;
    
fkSymbol:
    ID
    ;
    
keyParts:
    LEFT_PAREN keyPart (COMMA keyPart)* RIGHT_PAREN
    ;
     
defaultValue:
    NULL
    |simpleExpr
    ;
    
keyPart:
    columnName (LEFT_PAREN NUMBER RIGHT_PAREN)? (ASC | DESC)?
    ;

indexType:
    USING (BTREE | HASH)
    ;
    
indexOption:
    KEY_BLOCK_SIZE EQ_OR_ASSIGN? value
    | indexType
    | WITH PARSER parserName
    | COMMENT STRING
    ;

engineName:
    ID
    |MEMORY
    ;

partitionNames:
    partitionName (COMMA partitionName)*
    ;

partitionOptions:
    PARTITION BY (linearPartition | rangeOrListPartition)
    (PARTITIONS NUMBER)?
    (SUBPARTITION BY linearPartition (SUBPARTITIONS NUMBER)? )?
    partitionDefinitions?
    ;

//hash(YEAR(col)) YEAR is keyword which does not match expr
linearPartition:
     LINEAR? ((HASH (yearFunctionExpr | exprWithParen)) |keyColumnList)
     ;
 
 yearFunctionExpr:
     LEFT_PAREN YEAR exprWithParen RIGHT_PAREN
     ;
     
keyColumnList:
    KEY (ALGORITHM EQ_OR_ASSIGN NUMBER)? columnList
    ;
    
exprWithParen:
    LEFT_PAREN expr RIGHT_PAREN
    ;
      
rangeOrListPartition:
     (RANGE | LIST ) exprOrColumns
     ;

exprOrColumns:
    LEFT_PAREN expr RIGHT_PAREN | COLUMNS columnList  
    ;
           
partitionDefinitions:
    LEFT_PAREN partitionDefinition (COMMA partitionDefinition)* RIGHT_PAREN
    ;
    
partitionDefinition:
    PARTITION partitionName
    (VALUES (lessThanPartition|IN valueListWithParen))?
    (STORAGE? ENGINE EQ_OR_ASSIGN? engineName)?
    (COMMENT EQ_OR_ASSIGN? STRING )?
    (DATA DIRECTORY EQ_OR_ASSIGN? STRING)?
    (INDEX DIRECTORY EQ_OR_ASSIGN? STRING)?
    (MAX_ROWS EQ_OR_ASSIGN? NUMBER)?
    (MIN_ROWS EQ_OR_ASSIGN? NUMBER)?
    (TABLESPACE EQ_OR_ASSIGN? tablespaceName)?
    (subpartitionDefinition (COMMA subpartitionDefinition)*)?
    ;

lessThanPartition:
    LESS THAN ((LEFT_PAREN (expr | valueList) RIGHT_PAREN) | MAXVALUE)
    ;
    
subpartitionDefinition:
    SUBPARTITION partitionName
    ((STORAGE)? ENGINE EQ_OR_ASSIGN? engineName)?
    (COMMENT EQ_OR_ASSIGN? STRING )?
    (DATA DIRECTORY EQ_OR_ASSIGN? STRING)?
    (INDEX DIRECTORY EQ_OR_ASSIGN? STRING)?
    (MAX_ROWS EQ_OR_ASSIGN? NUMBER)?
    (MIN_ROWS EQ_OR_ASSIGN? NUMBER)?
    (TABLESPACE EQ_OR_ASSIGN? tablespaceName)?
    ;   

 value:
    DEFAULT|expr;

valueList:
     value (COMMA value)*
    ;
    
valueListWithParen:
    LEFT_PAREN valueList RIGHT_PAREN
    ;