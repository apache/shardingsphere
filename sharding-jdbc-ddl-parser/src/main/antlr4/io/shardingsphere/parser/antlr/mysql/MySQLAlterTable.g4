grammar MySQLAlterTable;
import MySQLKeyword, DataType, Keyword, BaseRule,MySQLTableBase, MySQLBase,Symbol;

alterTable:
     ALTER TABLE tableName alterSpecifications?
    ;

alterSpecifications:
    alterSpecification (COMMA alterSpecification)*
    ;

alterSpecification:
    (tableOptions)
    | ADD COLUMN? (singleColumn | multiColumn)
    | ADD indexDefinition
    | addConstraint
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

singleColumn:
    columnDefinition firstOrAfterColumn?
    ;

multiColumn:
    LEFT_PAREN columnDefinition (COMMA columnDefinition)* RIGHT_PAREN
    ;

addConstraint:
    ADD constraintDefinition
    ;

changeColumn:
    changeColumnOp columnName columnDefinition firstOrAfterColumn?
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

modifyColumn:
    MODIFY COLUMN? columnDefinition firstOrAfterColumn?
    ;

firstOrAfterColumn:
	FIRST
	|AFTER columnName
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