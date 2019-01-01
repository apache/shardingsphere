grammar MySQLAlterTable;

import MySQLKeyword, Keyword, MySQLTableBase, MySQLBase, BaseRule, DataType, Symbol;

alterTable
    : ALTER TABLE tableName alterSpecifications?
    ;
    
alterSpecifications
    : alterSpecification (COMMA alterSpecification)*
    ;
    
alterSpecification
    : tableOptions
    | addColumn
    | addIndex
    | addConstraint
    | ALGORITHM EQ_? (DEFAULT | INPLACE | COPY)
    | ALTER COLUMN? columnName (SET DEFAULT | DROP DEFAULT)
    | changeColumn
    | DEFAULT? characterSet collateClause?
    | CONVERT TO characterSet collateClause?
    | (DISABLE | ENABLE) KEYS
    | (DISCARD | IMPORT_) TABLESPACE
    | dropColumn
    | dropIndexDef
    | dropPrimaryKey
    | DROP FOREIGN KEY fkSymbol
    | FORCE
    | LOCK EQ_? (DEFAULT | NONE | SHARED | EXCLUSIVE)
    | modifyColumn
    | ORDER BY columnName (COMMA columnName)*
    | renameIndex
    | renameTable
    | (WITHOUT | WITH) VALIDATION
    | ADD PARTITION partitionDefinitions
    | DROP PARTITION partitionNames
    | DISCARD PARTITION (partitionNames | ALL) TABLESPACE
    | IMPORT_ PARTITION (partitionNames | ALL) TABLESPACE
    | TRUNCATE PARTITION (partitionNames | ALL)
    | COALESCE PARTITION NUMBER
    | REORGANIZE PARTITION partitionNames INTO partitionDefinitions
    | EXCHANGE PARTITION partitionName WITH TABLE tableName ((WITH | WITHOUT) VALIDATION)?
    | ANALYZE PARTITION (partitionNames | ALL)
    | CHECK PARTITION (partitionNames | ALL)
    | OPTIMIZE PARTITION (partitionNames | ALL)
    | REBUILD PARTITION (partitionNames | ALL)
    | REPAIR PARTITION (partitionNames | ALL)
    | REMOVE PARTITIONING
    | UPGRADE PARTITIONING
    ;
    
singleColumn
    : columnDefinition firstOrAfterColumn?
    ;
    
firstOrAfterColumn
    : FIRST | AFTER columnName
    ;
    
multiColumn
    : LP_ columnDefinition (COMMA columnDefinition)* RP_
    ;
    
addConstraint
    : ADD constraintDefinition
    ;
    
addIndex
    : ADD indexDefinition
    ;
    
addColumn
    : ADD COLUMN? (singleColumn | multiColumn)
    ;
    
changeColumn
    : changeColumnOp columnName columnDefinition firstOrAfterColumn?
    ;
    
changeColumnOp
    : CHANGE COLUMN?
    ;
    
dropColumn:
    DROP COLUMN? columnName
    ;

dropIndexDef
    : DROP indexAndKey indexName
    ;
    
dropPrimaryKey
    : DROP primaryKey
    ;
    
fkSymbol
    : ID
    ;
    
modifyColumn
    : MODIFY COLUMN? columnDefinition firstOrAfterColumn?
    ;
    
renameIndex
    : RENAME indexAndKey indexName TO indexName
    ;
    
renameTable
    : RENAME (TO | AS)? tableName
    ;
    
partitionNames
    : partitionName (COMMA partitionName)*
    ;
