grammar MySQLBase;

import MySQLKeyword, Keyword, Symbol, BaseRule, DataType;

alias
    : ID | PASSWORD | STRING_
    ;

tableName
    : ID | ID DOT_ASTERISK_ | ASTERISK_
    ;

indexOption
    : KEY_BLOCK_SIZE EQ_? assignmentValue | indexType | WITH PARSER ignoredIdentifier_ | COMMENT STRING_
    ;

indexType
    : USING (BTREE | HASH)
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
    : (ID | DATE) LP_ distinct? (exprs | ASTERISK_)? RP_ | groupConcat | windowFunction
    ;

groupConcat
    : GROUP_CONCAT LP_ distinct? (exprs | ASTERISK_)? (orderByClause (SEPARATOR expr)?)? RP_
    ;

windowFunction
    : ID exprList overClause
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

rangeClause
    : rangeItem_ (COMMA_ rangeItem_)* | rangeItem_ OFFSET rangeItem_
    ;

rangeItem_
    : number | question
    ;

partitionClause 
    : PARTITION columnNames
    ;
