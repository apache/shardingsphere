grammar OracleTableBase;

import OracleKeyword,Keyword,Symbol,OracleBase,BaseRule,DataType;

columnDefinition
    : columnName dataType SORT?
    (DEFAULT (ON NULL)? expr | identityClause)?
    (ENCRYPT encryptionSpec)?
    ( 
       inlineConstraint+ 
      | inlineRefConstraint
    )?
    ;

identityClause
    : GENERATED (ALWAYS | BY DEFAULT (ON NULL)?) AS IDENTITY LEFT_PAREN? (identityOptions+)? RIGHT_PAREN?
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
    : columnName dataType? (GENERATED ALWAYS)? AS LEFT_PAREN expr RIGHT_PAREN 
    VIRTUAL? inlineConstraint*
    ;

inlineConstraint
    : (CONSTRAINT constraintName)?
    ( 
    	  NOT? NULL
        | UNIQUE
        | primaryKey
        | referencesClause
        | CHECK LEFT_PAREN expr RIGHT_PAREN
    )
    constraintState?
    ;
    
referencesClause
    : REFERENCES tableName columnList?
    (ON DELETE (CASCADE | SET NULL))?
    ;
    
constraintState:
    (
        notDeferrable
        | initiallyClause
        | (RELY | NORELY)
        | usingIndexClause
        | (ENABLE | DISABLE)
        | (VALIDATE | NOVALIDATE)
        | exceptionsClause
    )+
    ;

notDeferrable:
    NOT? DEFERRABLE
    ;
    
initiallyClause:
    INITIALLY ( IMMEDIATE | DEFERRED )
    ;

exceptionsClause:
    EXCEPTIONS INTO  
    ;
        
usingIndexClause
    : USING INDEX
    (  indexName
    | (LEFT_PAREN createIndex RIGHT_PAREN) 
    )?
    ;
    
createIndex
    : matchNone
    ;
    
inlineRefConstraint
    : SCOPE IS tableName
    | WITH ROWID
    | (CONSTRAINT constraintName)? referencesClause constraintState?
    ;

outOfLineConstraint
    : (CONSTRAINT constraintName)?
    (
    	UNIQUE columnList
        | primaryKey columnList 
        | FOREIGN KEY columnList referencesClause
        | CHECK LEFT_PAREN expr RIGHT_PAREN
    ) 
    constraintState?
    ;
    
outOfLineRefConstraint
    : SCOPE FOR LEFT_PAREN lobItem RIGHT_PAREN IS  tableName
    | REF LEFT_PAREN lobItem RIGHT_PAREN WITH ROWID
    | (CONSTRAINT constraintName)? FOREIGN KEY lobItemList referencesClause constraintState?
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
    : ELEMENT? IS OF TYPE? LEFT_PAREN ONLY? typeName RIGHT_PAREN
    | NOT? SUBSTITUTABLE AT ALL LEVELS
    ;
