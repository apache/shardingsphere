grammar MySQLBase;

import MySQLKeyword, Keyword, BaseRule, DataType, Symbol;

characterSet
    : (CHARACTER | CHAR) SET EQ_? charsetName
    | CHARSET EQ_? charsetName
    ;

charsetName
    : ID
    | BINARY
    ;

collateClause
    : COLLATE EQ_? collationName
    ;

keyPartsWithParen
    : LP_ keyParts RIGHT_PAREN
    ;

keyParts
    : keyPart (COMMA keyPart)*
    ;

keyPart
    : columnName (LP_ NUMBER RIGHT_PAREN)? (ASC | DESC)?
    ;

symbol
    : ID
    ;

indexType
    : USING (BTREE | HASH)
    ;

indexAndKey
    : INDEX|KEY
    ;

indexOption
    : KEY_BLOCK_SIZE EQ_? value
    | indexType
    | WITH PARSER parserName
    | COMMENT STRING
    ;

valueListWithParen
    : LP_ valueList RIGHT_PAREN
    ;

valueList
    : value (COMMA value)*
    ;

value
    : DEFAULT
    | MAXVALUE
    | expr
    | exprsWithParen
    ;