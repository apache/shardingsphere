grammar MySQLBase;

import MySQLKeyword, Keyword, BaseRule, DataType, Symbol;

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
    : INDEX|KEY
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
