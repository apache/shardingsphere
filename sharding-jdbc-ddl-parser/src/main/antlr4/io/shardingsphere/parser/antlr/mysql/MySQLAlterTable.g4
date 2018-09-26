grammar MySQLAlterTable;
import MySQLKeyword, DataType, Keyword, BaseRule,MySQLTableBase, MySQLBase,Symbol;

alterTable:
     ALTER TABLE tableName alterSpecifications? partitionOptions?
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
    | (ORDER BY columnName (COMMA columnName)*)+ 
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
    changeColumnOp columnName columnDefinition (FIRST|AFTER columnName)?
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
    DROP primaryKey
    ;
    
renameIndex:
    RENAME indexAndKey indexName TO indexName
    ;

renameTable:    
    RENAME (TO|AS)? tableName
    ;

partitionNames:
    partitionName (COMMA partitionName)*
    ;    

modifyColumn:    
    MODIFY COLUMN? columnDefinition (FIRST | AFTER columnName)?
    ;
    
algorithmOption:
    ALGORITHM EQ_OR_ASSIGN? (DEFAULT|INPLACE|COPY)
    ;

lockOption:
    LOCK EQ_OR_ASSIGN? (DEFAULT|NONE|SHARED|EXCLUSIVE)
    ;
    
singleColumn:
    columnDefinition (FIRST | AFTER columnName)?
    ;
    
multiColumn:
    LEFT_PAREN columnDefinition (COMMA columnDefinition)* RIGHT_PAREN
    ;




 
