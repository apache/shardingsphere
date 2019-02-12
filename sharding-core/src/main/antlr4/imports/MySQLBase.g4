grammar MySQLBase;

import MySQLKeyword, Keyword, BaseRule, DataType, Symbol;

alias
    : ID | PASSWORD | STRING_
    ;
    
tableName
    : ID | ID DOT_ASTERISK_ | ASTERISK_
    ;

columnName
    : ID | ROW
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
    : keyPart (COMMA_ keyPart)*
    ;
    
keyPart
    : columnName (LP_ NUMBER_ RP_)? (ASC | DESC)?
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
    : KEY_BLOCK_SIZE EQ_? assignmentValue | indexType | WITH PARSER parserName | COMMENT STRING_
    ;
    
assignmentValueList
    : LP_ assignmentValues RP_
    ;
    
assignmentValues
    : assignmentValue (COMMA_ assignmentValue)*
    ;
    
assignmentValue
    : DEFAULT | MAXVALUE | expr
    ;
    
functionCall
    : (ID | DATE) LP_ distinct? (exprs | ASTERISK_)? RP_
    | groupConcat
    | windowFunction
    ;
    
groupConcat
    : GROUP_CONCAT LP_ distinct? (exprs | ASTERISK_)? (orderByClause (SEPARATOR expr)?)? RP_
    ;
    
windowFunction
    : ID exprsWithParen overClause
    ;
    
overClause
    : OVER LP_ windowSpec RP_ | OVER ID
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
    : (AT_ AT_)? (GLOBAL | PERSIST | PERSIST_ONLY | SESSION)? DOT_? ID
    ;

assignmentList
    : assignment (COMMA_ assignment)*
    ;

assignment
    : columnName EQ_ assignmentValue
    ;
    
fromClause
    : FROM tableReferences
    ;
    
tableReferences
    : matchNone
    ;
    
whereClause
    : WHERE expr
    ;
    
groupByClause 
    : GROUP BY orderByItem (COMMA_ orderByItem)* (WITH ROLLUP)?
    ;
    
havingClause
    : HAVING expr
    ;
    
limitClause
    : LIMIT rangeClause
    ;
    
partitionClause 
    : PARTITION columnList
    ;
    
    
asterisk
    : ASTERISK_
    ;