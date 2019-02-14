grammar OracleDDLStatement;

import OracleKeyword, Keyword, OracleBase, BaseRule, DataType, Symbol;

createIndex
    : CREATE (UNIQUE | BITMAP)? INDEX indexName ON (tableIndexClause | bitmapJoinIndexClause)
    ;
    
alterIndex
    : ALTER INDEX indexName (RENAME TO indexName)?
    ;
    
dropIndex
    : DROP INDEX indexName
    ;
    
createTable
    : CREATE (GLOBAL TEMPORARY)? TABLE tableName relationalTable
    ;
    
alterTable
    : ALTER TABLE tableName (alterTableProperties | columnClauses | constraintClauses | alterExternalTable)?
    ;
    
dropTable
    : DROP TABLE tableName
    ;
    
truncateTable
    : TRUNCATE TABLE tableName
    ;
    
tableIndexClause
    : tableName alias? LP_ indexExprSort (COMMA_ indexExprSort)* RP_
    ;
    
indexExprSort
    : indexExpr (ASC | DESC)?
    ;
    
indexExpr
    : columnName | expr 
    ;
    
tablespaceClauseWithParen
    : LP_ tablespaceClause RP_
    ;
    
tablespaceClause
    : TABLESPACE tablespaceName
    ;
    
domainIndexClause
    : indexTypeName
    ;
    
bitmapJoinIndexClause
    : tableName LP_ columnSortClause(COMMA_ columnSortClause)* RP_ FROM tableAndAlias (COMMA_ tableAndAlias)* WHERE expr
    ;
    
relationalTable
    : (LP_ relationalProperties RP_)? (ON COMMIT (DELETE | PRESERVE) ROWS)? tableProperties
    ;
    
relationalProperties
    : relationalProperty (COMMA_ relationalProperty)*
    ;
    
relationalProperty
    : columnDefinition | virtualColumnDefinition | outOfLineConstraint | outOfLineRefConstraint
    ;
    
tableProperties
    : columnProperties? (AS unionSelect)?
    ;
    
unionSelect
    : matchNone
    ;
    
alterTableProperties
    : renameTable | REKEY encryptionSpec
    ;
    
renameTable
    : RENAME TO tableName
    ;
    
columnClauses
    : opColumnClause+ | renameColumn
    ;
    
opColumnClause
    : addColumn | modifyColumn | dropColumnClause
    ;
    
addColumn
    : ADD columnOrVirtualDefinitions columnProperties?
    ;
    
columnOrVirtualDefinitions
    : LP_ columnOrVirtualDefinition (COMMA_ columnOrVirtualDefinition)* RP_ | columnOrVirtualDefinition
    ;
    
columnOrVirtualDefinition
    : columnDefinition | virtualColumnDefinition
    ;
    
modifyColumn
    : MODIFY (LP_? modifyColProperties (COMMA_ modifyColProperties)* RP_? | modifyColSubstitutable)
    ;
    
modifyColProperties
    : columnName dataType? (DEFAULT expr)? (ENCRYPT encryptionSpec | DECRYPT)? inlineConstraint* 
    ;
    
modifyColSubstitutable
    : COLUMN columnName NOT? SUBSTITUTABLE AT ALL LEVELS FORCE?
    ;

dropColumnClause
    : SET UNUSED columnOrColumnList cascadeOrInvalidate* | dropColumn
    ;
    
dropColumn
    : DROP columnOrColumnList cascadeOrInvalidate* checkpointNumber?
    ;
    
columnOrColumnList
    : COLUMN columnName | LP_ columnName (COMMA_ columnName)* RP_
    ;
    
cascadeOrInvalidate
    : CASCADE CONSTRAINTS | INVALIDATE
    ;
    
checkpointNumber
    : CHECKPOINT NUMBER_
    ;
    
renameColumn
    : RENAME COLUMN columnName TO columnName
    ;
    
constraintClauses
    : addConstraint | modifyConstraintClause | renameConstraintClause | dropConstraintClause+
    ;
    
addConstraint
    : ADD (outOfLineConstraint+ | outOfLineRefConstraint)
    ;
    
modifyConstraintClause
    : MODIFY constraintOption constraintState+ CASCADE?
    ;
    
constraintWithName
    : CONSTRAINT constraintName
    ;
    
constraintOption
    : constraintWithName | constraintPrimaryOrUnique
    ;
    
constraintPrimaryOrUnique
    : primaryKey | UNIQUE columnList
    ;
    
renameConstraintClause
    : RENAME constraintWithName TO constraintName
    ;
    
dropConstraintClause
    : DROP
    (
    constraintPrimaryOrUnique CASCADE? ((KEEP | DROP) INDEX)? | (CONSTRAINT constraintName CASCADE?)
    ) 
    ;
    
alterExternalTable
    : (addColumn | modifyColumn | dropColumn)+
    ;
    
columnDefinition
    : columnName dataType SORT? (DEFAULT (ON NULL)? expr | identityClause)? (ENCRYPT encryptionSpec)? (inlineConstraint+ | inlineRefConstraint)?
    ;
    
identityClause
    : GENERATED (ALWAYS | BY DEFAULT (ON NULL)?) AS IDENTITY LP_? (identityOptions+)? RP_?
    ;
    
identityOptions
    : START WITH (NUMBER_ | LIMIT VALUE)
    | INCREMENT BY NUMBER_
    | MAXVALUE NUMBER_
    | NOMAXVALUE
    | MINVALUE NUMBER_
    | NOMINVALUE
    | CYCLE
    | NOCYCLE
    | CACHE NUMBER_
    | NOCACHE
    | ORDER
    | NOORDER
    ;
    
virtualColumnDefinition
    : columnName dataType? (GENERATED ALWAYS)? AS LP_ expr RP_ VIRTUAL? inlineConstraint*
    ;
    
inlineConstraint
    : (CONSTRAINT constraintName)? (NOT? NULL | UNIQUE | primaryKey | referencesClause | CHECK LP_ expr RP_) constraintState*
    ;
    
referencesClause
    : REFERENCES tableName columnList? (ON DELETE (CASCADE | SET NULL))?
    ;
    
constraintState
    : notDeferrable 
    | initiallyClause 
    | RELY 
    | NORELY 
    | usingIndexClause 
    | ENABLE 
    | DISABLE 
    | VALIDATE 
    | NOVALIDATE 
    | exceptionsClause
    ;
    
notDeferrable
    : NOT? DEFERRABLE
    ;
    
initiallyClause
    : INITIALLY (IMMEDIATE | DEFERRED)
    ;
    
exceptionsClause
    : EXCEPTIONS INTO
    ;
    
usingIndexClause
    : USING INDEX (indexName | LP_ createIndex RP_)?
    ;
    
inlineRefConstraint
    : SCOPE IS tableName | WITH ROWID | (CONSTRAINT constraintName)? referencesClause constraintState*
    ;
    
outOfLineConstraint
    : (CONSTRAINT constraintName)?
    (
    	UNIQUE columnList
        | primaryKey columnList 
        | FOREIGN KEY columnList referencesClause
        | CHECK LP_ expr RP_
    ) 
    constraintState*
    ;
    
outOfLineRefConstraint
    : SCOPE FOR LP_ lobItem RP_ IS tableName
    | REF LP_ lobItem RP_ WITH ROWID
    | (CONSTRAINT constraintName)? FOREIGN KEY lobItemList referencesClause constraintState*
    ;
    
encryptionSpec
    : (USING STRING_)? (IDENTIFIED BY STRING_)? STRING_? (NO? SALT)?
    ;
    
objectProperties
    : objectProperty (COMMA_ objectProperty)*
    ;
    
objectProperty
    : (columnName | attributeName) (DEFAULT expr)? (inlineConstraint* | inlineRefConstraint?) | outOfLineConstraint | outOfLineRefConstraint
    ;
    
columnProperties
    : columnProperty+
    ;
    
columnProperty
    : objectTypeColProperties
    ;
    
objectTypeColProperties
    : COLUMN columnName substitutableColumnClause
    ;
    
substitutableColumnClause
    : ELEMENT? IS OF TYPE? LP_ ONLY? typeName RP_
    | NOT? SUBSTITUTABLE AT ALL LEVELS
    ;
