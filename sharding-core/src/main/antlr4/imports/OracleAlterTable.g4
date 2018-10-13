grammar OracleAlterTable;

import OracleKeyword, DataType, Keyword,OracleCreateIndex, OracleTableBase,OracleBase,BaseRule,Symbol;

alterTable
    : ALTER TABLE tableName
     ( alterTableProperties
     | columnClauses
     | constraintClauses
     | alterExternalTable
    )?
    ;
    
alterTableProperties
    : renameTable
    | REKEY encryptionSpec
    ;

renameTable:
    RENAME TO tableName
    ;

columnClauses
    : opColumnClause+
    | renameColumn
    ;

opColumnClause
    : addColumn
    | modifyColumn
    | dropColumnClause
    ;
    
addColumn
    : ADD columnOrVirtualDefinitions columnProperties?
    ;

columnOrVirtualDefinitions
    : LEFT_PAREN columnOrVirtualDefinition
        (COMMA columnOrVirtualDefinition)* 
      RIGHT_PAREN
    | columnOrVirtualDefinition
    ;
    
columnOrVirtualDefinition:
    columnDefinition 
    | virtualColumnDefinition
    ;
    
modifyColumn
    : MODIFY 
    ( 
        LEFT_PAREN? modifyColProperties (COMMA modifyColProperties)* RIGHT_PAREN?
       | modifyColSubstitutable
    )
    ;
    
modifyColProperties
    : columnName dataType?
    (DEFAULT expr)?
    (ENCRYPT encryptionSpec | DECRYPT)?
    inlineConstraint* 
    ;
    
modifyColSubstitutable
    : COLUMN columnName
    NOT? SUBSTITUTABLE AT ALL LEVELS FORCE?
    ;
    
dropColumnClause
    : SET UNUSED columnOrColumnList cascadeOrInvalidate*
    | dropColumn
    ;

dropColumn
    : DROP columnOrColumnList cascadeOrInvalidate* checkpointNumber?
	;
	
columnOrColumnList
    : COLUMN columnName
    | LEFT_PAREN columnName ( COMMA columnName )* RIGHT_PAREN
    ;

cascadeOrInvalidate
    : CASCADE CONSTRAINTS
    | INVALIDATE
    ;

checkpointNumber
    : CHECKPOINT NUMBER
    ;
    
renameColumn
    : RENAME COLUMN columnName TO columnName
    ;
    
constraintClauses
    : addConstraintClause
    | modifyConstraintClause
    | renameConstraintClause
    | dropConstraintClause+
    ;
    
addConstraintClause
    : ADD (outOfLineConstraint+ | outOfLineRefConstraint)
    ;

modifyConstraintClause
    : MODIFY constraintOption constraintState CASCADE?
    ;

constraintWithName
    : CONSTRAINT constraintName
    ;    
    
constraintOption
    : constraintWithName
    | constraintPrimaryOrUnique
    ;
 
constraintPrimaryOrUnique
    : primaryKey
    | UNIQUE columnList
    ;
        
renameConstraintClause
    : RENAME constraintWithName TO constraintName
    ;
   
dropConstraintClause
    : DROP
    (
        (constraintPrimaryOrUnique CASCADE? (( KEEP | DROP) INDEX)?)
       | (CONSTRAINT constraintName ( CASCADE )?)
    ) 
    ;

alterExternalTable
    : ( addColumn
    | modifyColumn
    | dropColumn
    )+
    ;
