grammar PostgreAlterTable;

import PostgreKeyword, DataType, Keyword, PostgreBase, BaseRule, Symbol;

alterTable
    : alterTableNameWithAsterisk(alterTableActions| renameColumn | renameConstraint)
    | alterTableNameExists renameTable
    ;

alterTableNameWithAsterisk
    : alterTableOp (IF EXISTS)? ONLY? tableName ASTERISK?
    ;

alterTableOp
    : ALTER TABLE
    ;

alterTableActions
    : alterTableAction (COMMA alterTableAction)*
    ;

alterTableAction
    : addColumn
    | dropColumn
    | modifyColumn
    | alterTableAddConstraint
    | ALTER CONSTRAINT constraintName constraintOptionalParam
    | VALIDATE CONSTRAINT constraintName
    | DROP CONSTRAINT (IF EXISTS)? constraintName (RESTRICT | CASCADE)?
    | INHERIT tableName
    | NO INHERIT tableName
    | REPLICA IDENTITY (DEFAULT | USING INDEX indexName | FULL | NOTHING)
    ;

tableConstraintUsingIndex
    : (CONSTRAINT constraintName)?
    (UNIQUE | primaryKey) USING INDEX indexName
    constraintOptionalParam
    ;

 constraintOptionalParam
    : (NOT? DEFERRABLE)? (INITIALLY (DEFERRED |IMMEDIATE))?
    ;

addColumn
    : ADD COLUMN? (IF NOT EXISTS )? columnDefinition
    ;
    
dropColumn
    : DROP COLUMN? (IF EXISTS)? columnName (RESTRICT | CASCADE)?
    ;
     
modifyColumn
    : alterColumn (SET DATA)? TYPE dataType collateClause? (USING simpleExpr)?
    ;
     
alterColumn     
    : ALTER COLUMN? columnName
    ;
     
alterTableAddConstraint       
    : ADD tableConstraint (NOT VALID)?
    | ADD tableConstraintUsingIndex
    ;
    
renameColumn
    : RENAME COLUMN? columnName TO columnName
    ;

renameConstraint
    : RENAME CONSTRAINT constraintName TO constraintName
    ;

alterTableNameExists
    : alterTableOp (IF EXISTS)? tableName
    ;

renameTable
    : RENAME TO tableName
    ;
