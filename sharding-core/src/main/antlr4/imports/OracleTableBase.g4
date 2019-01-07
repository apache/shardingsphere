grammar OracleTableBase;

import OracleKeyword, Keyword, Symbol, OracleBase, BaseRule, DataType;

columnDefinition
    : columnName dataType SORT?
    (DEFAULT (ON NULL)? expr | identityClause)?
    (ENCRYPT encryptionSpec)?
    (inlineConstraint+ | inlineRefConstraint)?
    ;
    
identityClause
    : GENERATED (ALWAYS | BY DEFAULT (ON NULL)?) AS IDENTITY LP_? (identityOptions+)? RP_?
    ;
    
identityOptions
    : START WITH (NUMBER | LIMIT VALUE)
    | INCREMENT BY NUMBER
    | MAXVALUE NUMBER
    | NOMAXVALUE
    | MINVALUE NUMBER
    | NOMINVALUE
    | CYCLE
    | NOCYCLE
    | CACHE NUMBER
    | NOCACHE
    | ORDER
    | NOORDER
    ;
    
virtualColumnDefinition
    : columnName dataType? (GENERATED ALWAYS)? AS LP_ expr RP_ VIRTUAL? inlineConstraint*
    ;
    
inlineConstraint
    : (CONSTRAINT constraintName)?
    (NOT? NULL | UNIQUE | primaryKey | referencesClause | CHECK LP_ expr RP_)
    constraintState*
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
    
initiallyClause:
    INITIALLY ( IMMEDIATE | DEFERRED )
    ;
    
exceptionsClause
    : EXCEPTIONS INTO
    ;
    
usingIndexClause
    : USING INDEX
    (indexName
    | (LP_ createIndex RP_) 
    )?
    ;
    
createIndex
    : matchNone
    ;
    
inlineRefConstraint
    : SCOPE IS tableName
    | WITH ROWID
    | (CONSTRAINT constraintName)? referencesClause constraintState*
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
    : (USING STRING)?
    (IDENTIFIED BY STRING)?
    STRING? (NO? SALT)?
    ;
    
objectProperties
    : objectProperty (COMMA objectProperty)*
    ;
    
objectProperty
    : (columnName | attributeName ) (DEFAULT expr)? (inlineConstraint* | inlineRefConstraint?)
    | outOfLineConstraint 
    | outOfLineRefConstraint
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
