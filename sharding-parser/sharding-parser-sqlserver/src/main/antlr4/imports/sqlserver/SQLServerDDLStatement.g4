grammar SQLServerDDLStatement;

import SQLServerKeyword, Keyword, Symbol, SQLServerBase, BaseRule, DataType;

createIndex
    : CREATE UNIQUE? (CLUSTERED | NONCLUSTERED)? INDEX indexName ON tableName columnList
    ;

alterIndex
    : ALTER INDEX (indexName | ALL) ON tableName
    ;

dropIndex
    : DROP INDEX (IF EXISTS)? indexName ON tableName
    ;

createTable
    : createTableHeader createTableBody
    ;

alterTable
    : alterTableOp
    (
        alterColumn
        | addColumn
        | alterDrop
        | alterCheckConstraint
        | alterTrigger
        | alterSwitch
        | alterSet
        | alterTableTableOption
        | REBUILD
    )
    ;

truncateTable
    : TRUNCATE TABLE tableName
    ;

dropTable
    : DROP TABLE (IF EXISTS)? tableNames
    ;

createTableHeader
    : CREATE TABLE tableName
    ;

createTableBody
    : (AS FILETABLE)? LP_ createTableDefinition (COMMA_ createTableDefinition)* (COMMA_ periodClause)? RP_(ON (schemaName LP_ columnName RP_ | ignoredIdentifier_ | STRING_))?
    (TEXTIMAGE_ON (ignoredIdentifier_ | STRING_))? ((FILESTREAM_ON (schemaName) | ignoredIdentifier_ STRING_))? (WITH LP_ tableOption (COMMA_ tableOption)* RP_)?
    ;

createTableDefinition
    : columnDefinition | computedColumnDefinition | columnSetDefinition | tableConstraint | tableIndex
    ;

periodClause
    : PERIOD FOR SYSTEM_TIME LP_ columnName COMMA_ columnName RP_
    ;

tableIndex
    : INDEX indexName
    (
        (CLUSTERED | NONCLUSTERED)? columnList
        | CLUSTERED COLUMNSTORE
        | NONCLUSTERED? (COLUMNSTORE columnList | hashWithBucket) 
        | CLUSTERED COLUMNSTORE (WITH LP_ COMPRESSION_DELAY EQ_ (NUMBER_ MINUTES?) RP_)?
    )
    (WHERE expr)?
    (WITH LP_ indexOption (COMMA_ indexOption)* RP_)? indexOnClause?
    (FILESTREAM_ON (ignoredIdentifier_ | schemaName | STRING_))?
    ;

tableOption
    : DATA_COMPRESSION EQ_ (NONE | ROW | PAGE) (ON PARTITIONS LP_ partitionExpressions RP_)?
    | FILETABLE_DIRECTORY EQ_ ignoredIdentifier_ 
    | FILETABLE_COLLATE_FILENAME EQ_ (collationName | DATABASE_DEAULT)
    | FILETABLE_PRIMARY_KEY_CONSTRAINT_NAME EQ_ constraintName
    | FILETABLE_STREAMID_UNIQUE_CONSTRAINT_NAME EQ_ constraintName
    | FILETABLE_FULLPATH_UNIQUE_CONSTRAINT_NAME EQ_ constraintName
    | SYSTEM_VERSIONING EQ_ ON (LP_ HISTORY_TABLE EQ_ tableName (COMMA_ DATA_CONSISTENCY_CHECK EQ_ (ON | OFF))? RP_)?
    | REMOTE_DATA_ARCHIVE EQ_ (ON (LP_ tableStretchOptions (COMMA_ tableStretchOptions)* RP_)? | OFF LP_ MIGRATION_STATE EQ_ PAUSED RP_)
    | tableOptOption
    | distributionOption
    | dataWareHouseTableOption
    ;

tableOptOption
    : (MEMORY_OPTIMIZED EQ_ ON) | (DURABILITY EQ_ (SCHEMA_ONLY | SCHEMA_AND_DATA)) | (SYSTEM_VERSIONING EQ_ ON (LP_ HISTORY_TABLE EQ_ tableName (COMMA_ DATA_CONSISTENCY_CHECK EQ_ (ON | OFF))? RP_)?)
    ;

distributionOption
    : DISTRIBUTION EQ_ (HASH LP_ columnName RP_ | ROUND_ROBIN | REPLICATE) 
    ;

dataWareHouseTableOption
    : CLUSTERED COLUMNSTORE INDEX | HEAP | dataWareHousePartitionOption
    ;

dataWareHousePartitionOption
    : (PARTITION LP_ columnName RANGE (LEFT | RIGHT)? FOR VALUES LP_ simpleExpr (COMMA_ simpleExpr)* RP_ RP_)
    ;

tableStretchOptions 
    : (FILTER_PREDICATE EQ_ (NULL | functionCall) COMMA_)? MIGRATION_STATE EQ_ (OUTBOUND | INBOUND | PAUSED)
    ;

columnDefinition
    : columnName dataType columnDefinitionOption* (columnConstraint(COMMA_ columnConstraint)*)? columnIndex?
    ;

columnDefinitionOption
    : FILESTREAM
    | COLLATE collationName
    | SPARSE
    | MASKED WITH LP_ FUNCTION EQ_ STRING_ RP_
    | (CONSTRAINT constraintName)? DEFAULT expr
    | IDENTITY (LP_ NUMBER_ COMMA_ NUMBER_ RP_)?
    | NOT FOR REPLICATION
    | GENERATED ALWAYS AS ROW (START | END) HIDDEN_?
    | NOT? NULL
    | ROWGUIDCOL 
    | ENCRYPTED WITH LP_ COLUMN_ENCRYPTION_KEY EQ_ ignoredIdentifier_ COMMA_ ENCRYPTION_TYPE EQ_ (DETERMINISTIC | RANDOMIZED) COMMA_ ALGORITHM EQ_ STRING_ RP_
    | columnConstraint (COMMA_ columnConstraint)*
    | columnIndex
    ;

columnConstraint
    : (CONSTRAINT constraintName)? (primaryKeyConstraint | columnForeignKeyConstraint | checkConstraint)
    ;

primaryKeyConstraint
    : (primaryKey | UNIQUE) (diskTablePrimaryKeyConstraintOption | memoryTablePrimaryKeyConstraintOption)
    ;

diskTablePrimaryKeyConstraintOption
    : (CLUSTERED | NONCLUSTERED)? primaryKeyWithClause? primaryKeyOnClause?
    ;

columnForeignKeyConstraint
    : (FOREIGN KEY)? REFERENCES tableName LP_ columnName RP_ foreignKeyOnAction*
    ;

foreignKeyOnAction
    : ON (DELETE | UPDATE) foreignKeyOn | NOT FOR REPLICATION
    ;

foreignKeyOn
    : NO ACTION | CASCADE | SET (NULL | DEFAULT)
    ;

memoryTablePrimaryKeyConstraintOption
    : CLUSTERED withBucket?
    ;

hashWithBucket
    : HASH columnList withBucket
    ;

withBucket
    : WITH LP_ BUCKET_COUNT EQ_ NUMBER_ RP_
    ;

primaryKeyWithClause
    : WITH (FILLFACTOR EQ_ NUMBER_ | LP_ indexOption (COMMA_ indexOption)* RP_)
    ;

primaryKeyOnClause
    : onSchemaColumn | onFileGroup | onString
    ;

onSchemaColumn
    : ON schemaName LP_ columnName RP_
    ;

onFileGroup
    : ON ignoredIdentifier_
    ;

onString
    : ON STRING_
    ;

checkConstraint
    : CHECK(NOT FOR REPLICATION)? LP_ expr RP_
    ;

columnIndex
    : INDEX indexName (CLUSTERED | NONCLUSTERED)? (WITH LP_ indexOption (COMMA_ indexOption)* RP_)? indexOnClause? (FILESTREAM_ON (ignoredIdentifier_ | schemaName | STRING_))?
    ;

indexOnClause
    : onSchemaColumn | onFileGroup | onDefault
    ;

onDefault
    : ON DEFAULT
    ;

tableConstraint 
    : (CONSTRAINT constraintName)? (tablePrimaryConstraint | tableForeignKeyConstraint | checkConstraint)
    ;

tablePrimaryConstraint
    : primaryKeyUnique (diskTablePrimaryConstraintOption | memoryTablePrimaryConstraintOption)
    ;

primaryKeyUnique
    : primaryKey | UNIQUE
    ;

diskTablePrimaryConstraintOption
    : (CLUSTERED | NONCLUSTERED)? columnList primaryKeyWithClause? primaryKeyOnClause?
    ;

memoryTablePrimaryConstraintOption
    : NONCLUSTERED (columnList | hashWithBucket)
    ;

tableForeignKeyConstraint
    : (FOREIGN KEY)? columnList REFERENCES tableName columnList foreignKeyOnAction*
    ;

computedColumnDefinition
    : columnName AS expr (PERSISTED(NOT NULL)?)? columnConstraint?
    ;

columnSetDefinition 
    : ignoredIdentifier_ ID COLUMN_SET FOR ALL_SPARSE_COLUMNS
    ;

alterTableOp
    : ALTER TABLE tableName
    ;

alterColumn
    : modifyColumn
    ;

modifyColumn
    : alterColumnOp dataType (COLLATE collationName)? (NULL | NOT NULL)? SPARSE?
    ;

alterColumnOp
    : ALTER COLUMN columnName
    ;

addColumn
    : (WITH (CHECK | NOCHECK))? ADD (alterColumnAddOption (COMMA_ alterColumnAddOption)* | (columnNameGeneratedClause COMMA_ periodClause| periodClause COMMA_ columnNameGeneratedClause))
    ;

alterColumnAddOption
    : columnDefinition
    | computedColumnDefinition
    | columnSetDefinition
    | tableConstraint
    | alterTableTableIndex
    | constraintForColumn
    ;

constraintForColumn
    : (CONSTRAINT constraintName)? DEFAULT simpleExpr FOR columnName
    ;

columnNameWithSortsWithParen
    : LP_ columnNameWithSort (COMMA_ columnNameWithSort)* RP_
    ;

columnNameWithSort
    : columnName (ASC | DESC)?
    ;

columnNameGeneratedClause
    : columnNameGenerated DEFAULT simpleExpr (WITH VALUES)? COMMA_ columnNameGenerated
    ;

columnNameGenerated
    : columnName dataTypeName_ GENERATED ALWAYS AS ROW (START | END)? HIDDEN_? (NOT NULL)? (CONSTRAINT constraintName)?
    ;

alterDrop
    : DROP 
    (
        alterTableDropConstraint
        | dropColumn
        | dropIndexDef
        | PERIOD FOR SYSTEM_TIME
    )
    ;

alterTableDropConstraint
    : CONSTRAINT? (IF EXISTS)? dropConstraintName (COMMA_ dropConstraintName)*
    ;

dropConstraintName
    : constraintName dropConstraintWithClause?
    ;

dropConstraintWithClause
    : WITH LP_ dropConstraintOption (COMMA_ dropConstraintOption)* RP_
    ;

dropConstraintOption
    : (MAXDOP EQ_ NUMBER_ | ONLINE EQ_ (ON | OFF) | MOVE TO (schemaName LP_ columnName RP_ | ignoredIdentifier_ | STRING_))
    ;

dropColumn
    : COLUMN (IF EXISTS)? columnNames
    ;

dropIndexDef
    : INDEX (IF EXISTS)? indexName (COMMA_ indexName)*
    ;

alterCheckConstraint 
    : WITH? (CHECK | NOCHECK) CONSTRAINT (ALL | (constraintName (COMMA_ constraintName)*))
    ;

alterTrigger 
    : (ENABLE| DISABLE) TRIGGER (ALL | (ignoredIdentifier_ (COMMA_ ignoredIdentifier_)*))
    ;

alterSwitch
    : SWITCH (PARTITION expr)? TO tableName (PARTITION expr)? (WITH LP_ lowPriorityLockWait RP_)?
    ;

alterSet
    : SET LP_ (setFileStreamClause | setSystemVersionClause) RP_ 
    ;

setFileStreamClause
    : FILESTREAM_ON EQ_ (schemaName | ignoredIdentifier_ | STRING_)
    ;

setSystemVersionClause
    : SYSTEM_VERSIONING EQ_ (OFF | alterSetOnClause)
    ;

alterSetOnClause
    : ON
    (
        LP_ (HISTORY_TABLE EQ_ tableName)?
        (COMMA_? DATA_CONSISTENCY_CHECK EQ_ (ON | OFF))?
        (COMMA_? HISTORY_RETENTION_PERIOD EQ_ (INFINITE | (NUMBER_ (DAY | DAYS | WEEK | WEEKS | MONTH | MONTHS | YEAR | YEARS))))?
        RP_
    )?
    ;

alterTableTableIndex
    : indexWithName (indexNonClusterClause | indexClusterClause)
    ;

indexWithName
    : INDEX indexName
    ;

indexNonClusterClause
    : NONCLUSTERED (hashWithBucket | (columnNameWithSortsWithParen alterTableIndexOnClause?)) 
    ;

alterTableIndexOnClause
    : ON ignoredIdentifier_ | DEFAULT
    ;

indexClusterClause
    : CLUSTERED COLUMNSTORE (WITH COMPRESSION_DELAY EQ_ NUMBER_ MINUTES?)? indexOnClause?
    ;

alterTableTableOption
    : SET LP_ LOCK_ESCALATION EQ_ (AUTO | TABLE | DISABLE) RP_
    | MEMORY_OPTIMIZED EQ_ ON
    | DURABILITY EQ_ (SCHEMA_ONLY | SCHEMA_AND_DATA) 
    | SYSTEM_VERSIONING EQ_ ON (LP_ HISTORY_TABLE EQ_ tableName (COMMA_ DATA_CONSISTENCY_CHECK EQ_ (ON | OFF))? RP_)?
    ;
