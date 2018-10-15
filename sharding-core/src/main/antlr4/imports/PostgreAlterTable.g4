grammar PostgreAlterTable;

import PostgreKeyword, DataType, Keyword, PostgreBase, BaseRule, Symbol;

alterTable
    : alterTableNameWithAsterisk (alterTableActions | renameColumn | renameConstraint)
    | alterTableNameExists renameTable
    ;

alterTableNameWithAsterisk
    : ALTER TABLE (IF EXISTS)? ONLY? tableName ASTERISK?
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
    | (DISABLE |ENABLE) TRIGGER (triggerName | ALL | USER )?
    | ENABLE (REPLICA | ALWAYS) TRIGGER triggerName
    | (DISABLE | ENABLE) RULE rewriteRuleName
    | ENABLE (REPLICA | ALWAYS) RULE rewriteRuleName
    | (DISABLE | ENABLE | (NO? FORCE)) ROW LEVEL SECURITY
    | CLUSTER ON indexName
    | SET WITHOUT CLUSTER
    | SET (WITH | WITHOUT) OIDS
    | SET TABLESPACE tablespaceName
    | SET (LOGGED | UNLOGGED)
    | SET LEFT_PAREN storageParameterWithValue (COMMA storageParameterWithValue)* RIGHT_PAREN
    | RESET LEFT_PAREN storageParameter (COMMA storageParameter)* RIGHT_PAREN
    | INHERIT tableName
    | NO INHERIT tableName
    | OF typeName
    | NOT OF
    | OWNER TO (ownerName | CURRENT_USER | SESSION_USER)
    | REPLICA IDENTITY (DEFAULT | (USING INDEX indexName) | FULL | NOTHING)
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
   | alterColumn SET DEFAULT expr
   | alterColumn DROP DEFAULT
   | alterColumn (SET | DROP) NOT NULL
   | alterColumn ADD GENERATED (ALWAYS | (BY DEFAULT)) AS IDENTITY (LEFT_PAREN sequenceOptions RIGHT_PAREN)?
   | alterColumn  alterColumnSetOption alterColumnSetOption*
   | alterColumn DROP IDENTITY (IF EXISTS)?
   | alterColumn SET STATISTICS NUMBER
   | alterColumn SET LEFT_PAREN attributeOptions RIGHT_PAREN
   | alterColumn RESET LEFT_PAREN attributeOptions RIGHT_PAREN
   | alterColumn SET STORAGE (PLAIN | EXTERNAL | EXTENDED | MAIN)
    ;
     
alterColumn     
    : ALTER COLUMN? columnName
    ;

alterColumnSetOption
    : SET GENERATED (ALWAYS | BY DEFAULT)
    | SET sequenceOption
    | RESTART (WITH? NUMBER)?
    ;

attributeOptions
    : attributeOption (COMMA attributeOption)*
    ;

attributeOption
    : ID EQ_OR_ASSIGN simpleExpr
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

storageParameterWithValue
    : storageParameter EQ_OR_ASSIGN simpleExpr
    ;

storageParameter
    : ID
    ;

alterTableNameExists
    : alterTableOp (IF EXISTS)? tableName
    ;

renameTable
    : RENAME TO tableName
    ;
