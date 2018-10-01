grammar MySQLBase;

import MySQLKeyword,Keyword,Symbol,DataType, BaseRule;

symbol:
    ID
    ;

fkSymbol:
    ID
    ;  
 
indexAndKey:
    INDEX|KEY
    ;
      
characterAndCollate:
    characterSet collateClause?
    ;
    
characterSet:
    (CHARACTER | CHAR) SET charsetName
    |CHARSET EQ_OR_ASSIGN? charsetName
    ;
    
charsetName:
    ID
    |BINARY
    ;

collateClause:
    COLLATE ID
    ;

characterAndCollateWithEqual:
    characterSetWithEqual collateClauseWithEqual?
    ;
    
characterSetWithEqual:
    ((CHARACTER | CHAR) SET EQ_OR_ASSIGN? charsetName)
    |CHARSET EQ_OR_ASSIGN? charsetName
    ;
    
collateClauseWithEqual:
    COLLATE EQ_OR_ASSIGN? ID
    ; 
    
indexType:
    USING (BTREE | HASH)
    ;
    
keyParts:
    LEFT_PAREN keyPart (COMMA keyPart)* RIGHT_PAREN
    ;
    
keyPart:
    columnName (LEFT_PAREN NUMBER RIGHT_PAREN)? (ASC | DESC)?
    ;
    
 indexOption:
    KEY_BLOCK_SIZE EQ_OR_ASSIGN? value
    | indexType
    | WITH PARSER parserName
    | COMMENT STRING
    ;

 value:
    DEFAULT
    |expr
    |exprsWithParen
    ;

valueList:
     value (COMMA value)*
    ;

valueListWithParen:
    LEFT_PAREN valueList RIGHT_PAREN
    ;
    
algorithmOption:
    ALGORITHM EQ_OR_ASSIGN? (DEFAULT|INPLACE|COPY)
    ;

lockOption:
    LOCK EQ_OR_ASSIGN? (DEFAULT|NONE|SHARED|EXCLUSIVE)
    ;