grammar MySQLBase;

import MySQLKeyword, Keyword, BaseRule, DataType, Symbol;

alias
    : ID | PASSWORD | STRING
    ;
    
tableName
    : ID | ID DOT_ASTERISK | ASTERISK
    ;
    
characterSet
    : (CHARACTER | CHAR) SET EQ_? charsetName | CHARSET EQ_? charsetName
    ;
    
charsetName
    : ID | BINARY
    ;
    
collateClause
    : COLLATE EQ_? collationName
    ;
    
keyPartsWithParen
    : LP_ keyParts RP_
    ;
    
keyParts
    : keyPart (COMMA keyPart)*
    ;
    
keyPart
    : columnName (LP_ NUMBER RP_)? (ASC | DESC)?
    ;
    
symbol
    : ID
    ;
    
indexType
    : USING (BTREE | HASH)
    ;
    
indexAndKey
    : INDEX | KEY
    ;
    
indexOption
    : KEY_BLOCK_SIZE EQ_? value | indexType | WITH PARSER parserName | COMMENT STRING
    ;
    
valueListWithParen
    : LP_ valueList RP_
    ;
    
valueList
    : value (COMMA value)*
    ;
    
value
    : DEFAULT | MAXVALUE | expr | exprsWithParen
    ;
    
functionCall
    : (ID | DATE) LP_ distinct? (exprs | ASTERISK)? RP_
    | groupConcat
    | windowFunction
    ;
    
groupConcat
    : GROUP_CONCAT LP_ distinct? (exprs | ASTERISK)? (orderByClause SEPARATOR expr) RP_
    ;
    
windowFunction
    : ID exprsWithParen overClause
    ;
overClause
    : OVER LP_ windowSpec RP_ 
    | OVER ID
    ;
    
windowSpec
    : ID? windowPartitionClause? orderByClause? frameClause?
    ;
    
windowPartitionClause
    : PARTITION BY exprs
    ;
    
frameClause
    : frameUnits frameExtent
    ;
    
frameUnits
    : ROWS | RANGE
    ;
    
frameExtent
    : frameStart | frameBetween
    ;
    
frameStart
    : CURRENT ROW
    | UNBOUNDED PRECEDING
    | UNBOUNDED FOLLOWING
    | expr PRECEDING
    | expr FOLLOWING
    ;
    
frameBetween
    : BETWEEN frameStart AND frameEnd
    ;
    
frameEnd
    : frameStart
    ;
    
variable
    : (AT_ AT_)? (GLOBAL | PERSIST | PERSIST_ONLY | SESSION)? DOT? ID
    ;
