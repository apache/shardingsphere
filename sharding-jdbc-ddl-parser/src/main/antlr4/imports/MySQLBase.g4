grammar MySQLBase;

import MySQLKeyword, Keyword, BaseRule, DataType, Symbol;

characterSet
    : (CHARACTER | CHAR) SET EQ_OR_ASSIGN? charsetName
    | CHARSET EQ_OR_ASSIGN? charsetName
    ;

charsetName
    : ID
    | BINARY
    ;

collateClause
    : COLLATE EQ_OR_ASSIGN? collationName
    ;

keyPartsWithParen
    : LEFT_PAREN keyParts RIGHT_PAREN
    ;

keyParts
    : keyPart (COMMA keyPart)*
    ;

keyPart
    : columnName (LEFT_PAREN NUMBER RIGHT_PAREN)? (ASC | DESC)?
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
    : KEY_BLOCK_SIZE EQ_OR_ASSIGN? value
    | indexType
    | WITH PARSER parserName
    | COMMENT STRING
    ;

valueListWithParen
    : LEFT_PAREN valueList RIGHT_PAREN
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