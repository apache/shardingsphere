grammar SQLServerCreateTable;
import SQLServerKeyword, DataType, Keyword, SQLServerBase,BaseRule,Symbol;

createTable:
	createTableHeader createTableBody
	;

createTableHeader:
	CREATE TABLE tableName
	;
	
createTableBody:
	 (AS FILETABLE)?  
    LEFT_PAREN 
    		createTableDefinition (COMMA createTableDefinition)*
    		(COMMA periodClause)?  
    RIGHT_PAREN  
    (ON ( schemaName LEFT_PAREN  columnName RIGHT_PAREN   
           | fileGroup   
           | STRING ) )?   
    (TEXTIMAGE_ON (fileGroup | STRING) )?   
    	((FILESTREAM_ON (schemaName) 
           | fileGroup   
           | STRING) )?  
    (WITH LEFT_PAREN  tableOption (COMMA tableOption)*  RIGHT_PAREN)?  
  	;
 
periodClause:
 	PERIOD FOR SYSTEM_TIME LEFT_PAREN  columnName   
    COMMA columnName RIGHT_PAREN
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
	

columnConstraint :   
	(CONSTRAINT constraintName )?   
     (	primaryKeyConstraint
	    | columnForeignKeyConstraint 
	  	| checkConstraint
  	)
	;

primaryKeyConstraint:
	(PRIMARY KEY | UNIQUE) 
	(diskTablePrimaryKeyConstraintOption | memoryTablePrimaryKeyConstraintOption)
	;

diskTablePrimaryKeyConstraintOption:
	(CLUSTERED | NONCLUSTERED)? 
	primaryKeyWithClause?
	primaryKeyOnClause?
	;	

memoryTablePrimaryKeyConstraintOption:
	CLUSTERED withBucket?
	;
	
hashWithBucket:
	HASH columnList withBucket
	;
	
withBucket:
	WITH LEFT_PAREN BUCKET_COUNT EQ_OR_ASSIGN NUMBER RIGHT_PAREN
	;
	
primaryKeyWithClause:
	 WITH 
	((FILLFACTOR EQ_OR_ASSIGN NUMBER)    
     | (LEFT_PAREN  indexOption (COMMA indexOption)* RIGHT_PAREN) 
     )
	;

primaryKeyOnClause:
	ON (
		(schemaName LEFT_PAREN  columnName RIGHT_PAREN)   
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
    		(schemaName LEFT_PAREN columnName RIGHT_PAREN)  
         | fileGroup  
         | DEFAULT   
         )  
    )?   
    ( FILESTREAM_ON ( fileGroup | schemaName | STRING ) )?  
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
    (diskTablePrimaryConstraintOption | memoryTablePrimaryConstraintOption)
	;

primaryKeyUnique:
	(PRIMARY KEY) 
	| UNIQUE
	;

diskTablePrimaryConstraintOption: 	
	( CLUSTERED | NONCLUSTERED )?   
    columnNameWithSortsWithParen
    primaryKeyWithClause?
    primaryKeyOnClause?
    ;

memoryTablePrimaryConstraintOption: 	
	NONCLUSTERED
	(columnNameWithSortsWithParen 
	       | hashWithBucket)
	;
	
	
tableIndex: 
	(  
	    (  
	      INDEX indexName 
	      	(
	      		((CLUSTERED | NONCLUSTERED )? columnNameWithSortsWithParen)  
	    		| (CLUSTERED COLUMNSTORE)  
	    		| (( NONCLUSTERED )? ((COLUMNSTORE columnList) | hashWithBucket)) 
  				| (CLUSTERED COLUMNSTORE (WITH LEFT_PAREN  COMPRESSION_DELAY EQ_OR_ASSIGN (NUMBER MINUTES?) RIGHT_PAREN)?) 
	    	)
	    ) 
	    (WHERE expr)?
	    (WITH LEFT_PAREN indexOption ( COMMA indexOption)* RIGHT_PAREN)?   
	    ( ON 
	    	( 
	    		(schemaName LEFT_PAREN columnName RIGHT_PAREN)  
	         	| groupName  
	         	| DEFAULT   
	         )  
	    )?   
	    ( FILESTREAM_ON ( groupName | schemaName | STRING ))?  
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
	|distributionOption
	|dataWareHouseTableOption     
	;

tableOptOption :  
    (MEMORY_OPTIMIZED EQ_OR_ASSIGN ON)   
  | (DURABILITY EQ_OR_ASSIGN (SCHEMA_ONLY | SCHEMA_AND_DATA)) 
  | (SYSTEM_VERSIONING EQ_OR_ASSIGN ON ( LEFT_PAREN  HISTORY_TABLE EQ_OR_ASSIGN tableName  
        (COMMA DATA_CONSISTENCY_CHECK EQ_OR_ASSIGN ( ON | OFF ) )? RIGHT_PAREN )?)   
  ;

distributionOption : 	
     DISTRIBUTION EQ_OR_ASSIGN 
     (
     	(HASH LEFT_PAREN columnName RIGHT_PAREN)
      	| ROUND_ROBIN 
      	| REPLICATE
      ) 
	; 
	
dataWareHouseTableOption:
    (CLUSTERED COLUMNSTORE INDEX)
    | HEAP   
    | dataWareHousePartitionOption
    ;
 
 dataWareHousePartitionOption:
 	(PARTITION LEFT_PAREN columnName  RANGE (LEFT | RIGHT)?  
        FOR VALUES LEFT_PAREN  simpleExpr (COMMA simpleExpr)* RIGHT_PAREN  RIGHT_PAREN)
 	; 
 	
tableStretchOptions:  
     ( FILTER_PREDICATE EQ_OR_ASSIGN ( NULL | functionCall ) COMMA )?  
       MIGRATION_STATE EQ_OR_ASSIGN ( OUTBOUND | INBOUND | PAUSED )  
	;
	



