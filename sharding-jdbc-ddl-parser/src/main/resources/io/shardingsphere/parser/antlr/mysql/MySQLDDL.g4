grammar MySQLDDL;
import MySQLBase,MySQLKeyword,DDLBase,SQLBase,Keyword,Symbol;
@header{
	package io.shardingsphere.parser.antlr.mysql;
}


alterSpecifications:
	alterSpecification (COMMA alterSpecification)*
	;

alterSpecification:
  	tableOptions
  	| ADD COLUMN? (singleColumn | multiColumn)
	| (ADD (INDEX|KEY) indexName? indexType? keyParts indexOption?)+
	| (ADD (CONSTRAINT symbol?)? (primaryKeyOption | uniqueOption | foreignKeyOption))+
	| (ADD FULLTEXT (INDEX|KEY)? indexName? indexType? keyParts indexOption?)+
	| (ADD SPATIAL (INDEX|KEY)? indexName? indexType? keyParts indexOption?)+
	| ALGORITHM EQ_OR_ASSIGN? (DEFAULT|INPLACE|COPY)
	| ALTER COLUMN? columnName (SET DEFAULT | DROP DEFAULT)
	| CHANGE COLUMN? columnName columnName columnDefinition (FIRST|AFTER columnName)?
	| DEFAULT? CHARACTER SET EQ_OR_ASSIGN? charsetName (COLLATE EQ_OR_ASSIGN? collationName)?
	| CONVERT TO CHARACTER SET charsetName (COLLATE collationName)?
	| (DISABLE|ENABLE) KEYS
	| (DISCARD|IMPORT_) TABLESPACE
	| DROP COLUMN? columnName
	| DROP (INDEX|KEY) indexName
	| DROP PRIMARY KEY
	| DROP FOREIGN KEY fkSymbol
	| FORCE
	| LOCK EQ_OR_ASSIGN? (DEFAULT|NONE|SHARED|EXCLUSIVE)
	| MODIFY COLUMN? columnName columnDefinition (FIRST | AFTER columnName)?
	| (ORDER BY columnName (COMMA columnName)* )+ 
	| RENAME (INDEX|KEY) indexName TO indexName
	| RENAME (TO|AS)? tableName
	| (WITHOUT|WITH) VALIDATION
	| ADD PARTITION partitionDefinitions
	| DROP PARTITION partitionNames
	| DISCARD PARTITION (partitionNames | ALL) TABLESPACE
	| IMPORT_ PARTITION (partitionNames | ALL) TABLESPACE
	| TRUNCATE PARTITION (partitionNames | ALL)
	| COALESCE PARTITION INT
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
	
singleColumn:
	columnNameAndDefinition (FIRST | AFTER columnName)?
	;
	
multiColumn:
	LEFT_PAREN columnNameAndDefinition (COMMA columnNameAndDefinition)* RIGHT_PAREN
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
    tableOption (COMMA tableOption)*
    ; 

tableOption:
 	AUTO_INCREMENT EQ_OR_ASSIGN? INT
	| AVG_ROW_LENGTH EQ_OR_ASSIGN? INT
	| DEFAULT? CHARACTER SET EQ_OR_ASSIGN? charsetName
	| CHECKSUM EQ_OR_ASSIGN? INT
	| DEFAULT? COLLATE EQ_OR_ASSIGN? collationName
	| COMMENT EQ_OR_ASSIGN? STRING
	| COMPRESSION EQ_OR_ASSIGN? (ZLIB|'LZ4'|NONE)
	| CONNECTION EQ_OR_ASSIGN? STRING
	| (DATA|INDEX) DIRECTORY EQ_OR_ASSIGN? STRING
	| DELAY_KEY_WRITE EQ_OR_ASSIGN? INT
	| ENCRYPTION EQ_OR_ASSIGN? STRING
	| ENGINE EQ_OR_ASSIGN? engineName
	| INSERT_METHOD EQ_OR_ASSIGN? ( NO | FIRST | LAST )
	| KEY_BLOCK_SIZE EQ_OR_ASSIGN? INT
	| MAX_ROWS EQ_OR_ASSIGN? INT
	| MIN_ROWS EQ_OR_ASSIGN? INT
	| PACK_KEYS EQ_OR_ASSIGN? (INT | DEFAULT)
	| PASSWORD EQ_OR_ASSIGN? STRING
	| ROW_FORMAT EQ_OR_ASSIGN? (DEFAULT|DYNAMIC|FIXED|COMPRESSED|REDUNDANT|COMPACT)
	| STATS_AUTO_RECALC EQ_OR_ASSIGN? (DEFAULT|INT)
	| STATS_PERSISTENT EQ_OR_ASSIGN? (DEFAULT|INT)
	| STATS_SAMPLE_PAGES EQ_OR_ASSIGN? INT
	| TABLESPACE tablespaceName (STORAGE (DISK|MEMORY|DEFAULT))?
	| UNION EQ_OR_ASSIGN?
	; 

columnNameAndDefinition:
	columnName columnDefinition
	;

columnDefinition:
	dataType (dataTypeOption | dataTypeGenerated)
	;
	
dataType:
	simpleExpr
	;
	
dataTypeOption:
	(NOT? NULL)? 
	(DEFAULT defaultValue)? 
	AUTO_INCREMENT? 
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
      (ON DELETE referenceOption)?
      (ON UPDATE referenceOption)?
	;
	
referenceOption:
    RESTRICT | CASCADE | SET NULL | NO ACTION | SET DEFAULT
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
charsetName:
	;
	
defaultValue:
	;
		
keyPart:
    columnName (LEFT_PAREN NUMBER RIGHT_PAREN)? (ASC | DESC)?
	;

indexName:
	ID
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
	
parserName:
	ID
	;

engineName:
	ID
	;

partitionNames:
	partitionName (COMMA partitionName)*
	;

partitionName:
	ID
	;

partitionOptions:
	PARTITION BY (linearPartition | rangeOrListPartition)
    (PARTITIONS INT)?
    (SUBPARTITION BY linearPartition (SUBPARTITIONS INT)? )?
  	partitionDefinitions?
   	;
        
linearPartition:
 	LINEAR? (HASH exprWithParen |keyColumnList)
 	;
 
keyColumnList:
	KEY (ALGORITHM EQ_OR_ASSIGN INT)? columnList
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
    (MAX_ROWS EQ_OR_ASSIGN? INT)?
    (MIN_ROWS EQ_OR_ASSIGN? INT)?
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
        (MAX_ROWS EQ_OR_ASSIGN? INT)?
        (MIN_ROWS EQ_OR_ASSIGN? INT)?
        (TABLESPACE EQ_OR_ASSIGN? tablespaceName)?
	;

tablespaceName:
	ID
	;

collationName:
	ID
	;	







     
