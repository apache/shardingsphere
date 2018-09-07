grammar SQLServerDDL;
import SQLServerKeyword, DataType, Keyword, BaseRule,DDLBase,MySQLDQL,DQLBase,Symbol;

createTable:
	createTableOpWithName
	(
		createMemoryTable
		|createDiskTable
	)
	;

createTableOpWithName:
	CREATE TABLE tableName
	;
	
createDiskTable:
	 (AS FILETABLE)?  
    LEFT_PAREN 
    		createTableDefinition (COMMA createTableDefinition)*
    		periodClause?  
    RIGHT_PAREN  
    (ON ( schemeName LEFT_PAREN  columnName RIGHT_PAREN   
           | fileGroup   
           | STRING ) )?   
    (TEXTIMAGE_ON (fileGroup | STRING) )?   
    	((FILESTREAM_ON (schemeName) 
           | fileGroup   
           | STRING) )?  
    (WITH LEFT_PAREN  tableOption (COMMA tableOption)*  RIGHT_PAREN)?  
  	;
 
periodClause:
 	COMMA PERIOD FOR SYSTEM_TIME LEFT_PAREN  systemStartTimeColumnName   
    COMMA systemEndTimeColumnName RIGHT_PAREN
    ;
 
systemStartTimeColumnName:
	ID
	;
	
systemEndTimeColumnName:
	ID
	;

schemeName:
	ID
	;
	
fileGroup:
	ID
	;

groupName:
	ID
	;
	
constraintName:
	ID
	;

keyName:
	ID
	;
	
typeName:
	ID
	;
	
xmlSchemaCollection:
	ID
	;

columnSetName:
	ID
	;

directoryName:
	ID
	;
	
createTableDefinition:
	columnDefinition  
    | computedColumnDefinition    
    | columnSetDefinition   
    | tableConstraint   
    | tableIndex
    ;
       

columnDefinition:
	columnName dataType  
    columnDefinitionOption*
    (columnConstraint (COMMA columnConstraint)*)?
    columnIndex?
	;

columnDefinitionOption:
	FILESTREAM  
	|(COLLATE collationName )
	|SPARSE  
	|( MASKED WITH LEFT_PAREN  FUNCTION EQ_OR_ASSIGN STRING RIGHT_PAREN ) 
	|((CONSTRAINT constraintName)? DEFAULT expr)  
	|(IDENTITY (LEFT_PAREN  NUMBER COMMA NUMBER RIGHT_PAREN )? )
	|(NOT FOR REPLICATION)
	|(GENERATED ALWAYS AS ROW (START | END) HIDDEN_? )  
	|(NOT? NULL)
	|ROWGUIDCOL 
	|( ENCRYPTED WITH 
		 LEFT_PAREN  
			COLUMN_ENCRYPTION_KEY EQ_OR_ASSIGN keyName COMMA  
		  	ENCRYPTION_TYPE EQ_OR_ASSIGN ( DETERMINISTIC | RANDOMIZED ) COMMA   
		  	ALGORITHM EQ_OR_ASSIGN STRING 
		RIGHT_PAREN 
	)
	|(columnConstraint (COMMA columnConstraint)*)
	|columnIndex
    ;	
	
dataType: 
	typeName   
    (
    	LEFT_PAREN  
	    	(
	    		(NUMBER ( COMMA NUMBER )?)
	    		| MAX 
	    		|((CONTENT | DOCUMENT)? xmlSchemaCollection) 
	    	)
    	RIGHT_PAREN 
    )?   
	;
	
columnConstraint :   
	(CONSTRAINT constraintName )?   
     (	primaryKeyConstraint
	    | columnForeignKeyConstraint 
	  	| checkConstraint
  	)
	;

primaryKeyConstraint:
	(PRIMARY KEY | UNIQUE)  (CLUSTERED | NONCLUSTERED)? 
	primaryKeyWithClause?
	primaryKeyOnClause?
	;
	
primaryKeyWithClause:
	 WITH 
	((FILLFACTOR EQ_OR_ASSIGN NUMBER)    
     | (LEFT_PAREN  indexOption (COMMA indexOption)* RIGHT_PAREN) 
     )
	;

primaryKeyOnClause:
	ON (
		(schemeName LEFT_PAREN  columnName RIGHT_PAREN)   
         | fileGroup 
         | STRING 
        ) 
    ;

columnForeignKeyConstraint:
	(FOREIGN KEY)?  
    REFERENCES tableName LEFT_PAREN  columnName RIGHT_PAREN   
    foreignKeyOnAction
	;

tableForeignKeyConstraint:
	(FOREIGN KEY)? columnList
    REFERENCES tableName columnList  
    foreignKeyOnAction
	;
	
foreignKeyOnAction:
	 ( ON DELETE foreignKeyOn )?   
    ( ON UPDATE foreignKeyOn )?   
    ( NOT FOR REPLICATION )? 
    ;	

foreignKeyOn:
	NO ACTION 
	| CASCADE 
	| SET NULL 
	| SET DEFAULT 
	;
	
checkConstraint:
	CHECK(NOT FOR REPLICATION)? LEFT_PAREN  expr RIGHT_PAREN  
	;
	
columnIndex:   
 	INDEX indexName ( CLUSTERED | NONCLUSTERED )?  
    ( WITH LEFT_PAREN  indexOption (COMMA indexOption)*  RIGHT_PAREN )?  
    ( ON ( 
    		(schemeName LEFT_PAREN columnName RIGHT_PAREN)  
         | fileGroup  
         | DEFAULT   
         )  
    )?   
    ( FILESTREAM_ON ( fileGroup | schemeName | STRING ) )?  
	;
	
computedColumnDefinition :  
	columnName AS expr   
	(PERSISTED( NOT NULL )?)?  
	columnConstraint?   
	;
	
columnSetDefinition :  
	columnSetName ID COLUMN_SET FOR ALL_SPARSE_COLUMNS  
	;
	
tableConstraint:  
	(CONSTRAINT constraintName )?   
    (
    	tablePrimaryConstraint
    	| tableForeignKeyConstraint   
    	| checkConstraint 
    )
	;
	
tablePrimaryConstraint: 
	primaryKeyUnique   
    ( CLUSTERED | NONCLUSTERED )?   
    columnNameWithSortsWithParen
    primaryKeyWithClause?
    primaryKeyOnClause?
	;

primaryKeyUnique:
	(PRIMARY KEY) 
	| UNIQUE
	;
	
columnNameWithSortsWithParen:
	LEFT_PAREN columnNameWithSort (COMMA columnNameWithSort)* RIGHT_PAREN 
	;
		
columnNameWithSort:
	columnName ( ASC | DESC )?
	;
	
tableIndex: 
	(  
	    (  
	      INDEX indexName 
	      	(
	      		((CLUSTERED | NONCLUSTERED )? columnNameWithSortsWithParen)  
	    		| (CLUSTERED COLUMNSTORE)  
	    		| (( NONCLUSTERED )? COLUMNSTORE columnList) 
	    	)
	    ) 
	    (WHERE expr)?
	    (WITH LEFT_PAREN  indexOption ( COMMA indexOption)* RIGHT_PAREN)?   
	    ( ON 
	    	( 
	    		(schemeName LEFT_PAREN columnName RIGHT_PAREN)  
	         	| groupName  
	         	| DEFAULT   
	         )  
	    )?   
	    ( FILESTREAM_ON ( groupName | schemeName | STRING ) )?  
	)   
	;
	
tableOption:  
	(DATA_COMPRESSION EQ_OR_ASSIGN ( NONE | ROW | PAGE ) (ON PARTITIONS LEFT_PAREN  partitionExpressions RIGHT_PAREN )?) 
	|( FILETABLE_DIRECTORY EQ_OR_ASSIGN directoryName )  
	|( FILETABLE_COLLATE_FILENAME EQ_OR_ASSIGN ( collationName | DATABASE_DEAULT ) )
	|( FILETABLE_PRIMARY_KEY_CONSTRAINT_NAME EQ_OR_ASSIGN constraintName )
	|( FILETABLE_STREAMID_UNIQUE_CONSTRAINT_NAME  EQ_OR_ASSIGN constraintName )  
	|( FILETABLE_FULLPATH_UNIQUE_CONSTRAINT_NAME  EQ_OR_ASSIGN constraintName )  
	|( SYSTEM_VERSIONING EQ_OR_ASSIGN ON ( LEFT_PAREN  HISTORY_TABLE EQ_OR_ASSIGN tableName   
		(COMMA DATA_CONSISTENCY_CHECK EQ_OR_ASSIGN ( ON | OFF ) )? RIGHT_PAREN)? )  
	|( 
		REMOTE_DATA_ARCHIVE EQ_OR_ASSIGN   
		(   
	    (ON (LEFT_PAREN  tableStretchOptions (COMMA tableStretchOptions)* RIGHT_PAREN )?) 
	     | (OFF LEFT_PAREN  MIGRATION_STATE EQ_OR_ASSIGN PAUSED RIGHT_PAREN)  
		)   
	)
	|tableOptOption     
	;
	
tableStretchOptions:  
     ( FILTER_PREDICATE EQ_OR_ASSIGN ( NULL | functionCall ) COMMA )?  
       MIGRATION_STATE EQ_OR_ASSIGN ( OUTBOUND | INBOUND | PAUSED )  
	;
	
indexOption :     
   	(PAD_INDEX EQ_OR_ASSIGN ( ON | OFF ) ) 
	| (FILLFACTOR EQ_OR_ASSIGN NUMBER) 
	| (IGNORE_DUP_KEY EQ_OR_ASSIGN ( ON | OFF ))  
	| (STATISTICS_NORECOMPUTE EQ_OR_ASSIGN ( ON | OFF ))  
	| (STATISTICS_INCREMENTAL EQ_OR_ASSIGN ( ON | OFF ))  
	| (ALLOW_ROW_LOCKS EQ_OR_ASSIGN ( ON | OFF))  
	| (ALLOW_PAGE_LOCKS EQ_OR_ASSIGN( ON | OFF))   
	| (COMPRESSION_DELAY EQ_OR_ASSIGN NUMBER MINUTES?)
	| (DATA_COMPRESSION EQ_OR_ASSIGN ( NONE | ROW | PAGE | COLUMNSTORE | COLUMNSTORE_ARCHIVE )  
	   ( ON PARTITIONS LEFT_PAREN partitionExpressions  RIGHT_PAREN )?)
	;

partitionExpressions:
	partitionExpression (COMMA partitionExpression)*
	;

partitionExpression:
	NUMBER 
	|partitionRange
	;
	
partitionRange:   
	NUMBER TO NUMBER  
	;
	
createMemoryTable:	
    LEFT_PAREN  
		columnOption (COMMA columnOption)*
		periodClause?  
    RIGHT_PAREN 
    (WITH LEFT_PAREN tableOptOption ( COMMA tableOptOption)* RIGHT_PAREN)? 
	; 
	
columnOption:
	columnOptDefinition  
    | (tableOptConstraint (COMMA tableOptConstraint)*)?  
    | tableIndex?
    ;

columnOptDefinition :  
	columnName dataType  
   	columnOptDefinitionOpt*
	;
	
columnOptDefinitionOpt:
	(COLLATE collationName ) 
	| (GENERATED ALWAYS AS ROW (START | END) HIDDEN_? )
	| (NOT? NULL)  
	| ((CONSTRAINT constraintName )? DEFAULT expr )
	| ( IDENTITY ( LEFT_PAREN  NUMBER COMMA NUMBER RIGHT_PAREN ) ) 
	| columnOptConstraint
	| columnOptIndex
	;
	
columnOptConstraint :  
 	(CONSTRAINT constraintName )?  
	(
		primaryKeyOptConstaint
  		| foreignKeyOptConstaint
 		| (CHECK LEFT_PAREN  expr RIGHT_PAREN)  
	)  
;

primaryKeyOptConstaint:
	primaryKeyUnique   
   	( 
   		 (NONCLUSTERED  columnNameWithSortsWithParen)
        | (NONCLUSTERED HASH WITH LEFT_PAREN BUCKET_COUNT EQ_OR_ASSIGN NUMBER RIGHT_PAREN) 
    ) 
	;
	
foreignKeyOptConstaint:
	(FOREIGN KEY )? REFERENCES tableName ( LEFT_PAREN  columnName RIGHT_PAREN )?
	;
	
tableOptConstraint:  
	 ( CONSTRAINT constraintName )?  
	(    
	  	 (primaryKeyUnique 
	     (   
	       	(NONCLUSTERED columnNameWithSortsWithParen) 
	       | (NONCLUSTERED HASH columnList WITH LEFT_PAREN  BUCKET_COUNT EQ_OR_ASSIGN NUMBER RIGHT_PAREN)   
	      ))  
	    | (FOREIGN KEY columnList REFERENCES tableName columnList? ) 
	    | (CHECK LEFT_PAREN  expr RIGHT_PAREN )  
	) 
	;

columnOptIndex:
  	INDEX indexName  
	(NONCLUSTERED? (HASH WITH LEFT_PAREN BUCKET_COUNT EQ_OR_ASSIGN NUMBER RIGHT_PAREN)? )  
	;

tableOptIndex :  
	INDEX indexName  
	(   
		( NONCLUSTERED ? HASH columnList WITH LEFT_PAREN BUCKET_COUNT EQ_OR_ASSIGN NUMBER RIGHT_PAREN )   
  		| ( NONCLUSTERED ? columnNameWithSortsWithParen ( ON groupName | DEFAULT )? ) 
  		| (CLUSTERED COLUMNSTORE (WITH LEFT_PAREN  COMPRESSION_DELAY EQ_OR_ASSIGN (NUMBER MINUTES?) RIGHT_PAREN)? ( ON groupName | DEFAULT )?)  
	)
	;

tableOptOption :  
    (MEMORY_OPTIMIZED EQ_OR_ASSIGN ON)   
  | (DURABILITY EQ_OR_ASSIGN (SCHEMA_ONLY | SCHEMA_AND_DATA)) 
  | (SYSTEM_VERSIONING EQ_OR_ASSIGN ON ( LEFT_PAREN  HISTORY_TABLE EQ_OR_ASSIGN tableName  
        (COMMA DATA_CONSISTENCY_CHECK EQ_OR_ASSIGN ( ON | OFF ) )? RIGHT_PAREN )?)   
  ;

privateExprOfDb:
	windowedFunction
	|atTimeZoneExpr
	|castExpr
	|convertExpr
	;

atTimeZoneExpr:
	ID (WITH TIME ZONE)? STRING
	;
	
castExpr:
	CAST LEFT_PAREN expr AS dataType (LEFT_PAREN  NUMBER RIGHT_PAREN )? RIGHT_PAREN  
	;
	
convertExpr:
	CONVERT ( dataType (LEFT_PAREN  NUMBER RIGHT_PAREN )? COMMA expr (COMMA NUMBER)?)
	;
	
windowedFunction:
 	functionCall overClause
 	;
 
overClause:
	OVER 
		LEFT_PAREN     
	      partitionByClause?
	      orderByClause?  
	      rowRangeClause? 
	    RIGHT_PAREN 
	;
	
partitionByClause:  
	PARTITION BY expr (COMMA expr)*  
	;
	
orderByClause:   
	ORDER BY orderByExpr (COMMA orderByExpr)*
   ;
  
orderByExpr:
  	expr (COLLATE collationName)? (ASC | DESC)? 
	;
	
rowRangeClause:  
 	(ROWS | RANGE) windowFrameExtent
 	;

windowFrameExtent: 
	windowFramePreceding
  	| windowFrameBetween 
	; 
	
windowFrameBetween:
  	BETWEEN windowFrameBound AND windowFrameBound  
	;
	
windowFrameBound:  
	windowFramePreceding 
  	| windowFrameFollowing 
	;
	
windowFramePreceding:  
    (UNBOUNDED PRECEDING)  
  | NUMBER PRECEDING  
  | CURRENT ROW  
; 

windowFrameFollowing:
    UNBOUNDED FOLLOWING  
  | NUMBER FOLLOWING  
  | CURRENT ROW  
;
