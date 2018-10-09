grammar PostgreCreateTable;

import PostgreKeyword, DataType, Keyword, PostgreBase, BaseRule, Symbol;

createTable
    : createTableHeader
    createDefinitions
    inheritClause?
    ;

createTableHeader
    : CREATE ((GLOBAL | LOCAL)? (TEMPORARY | TEMP) | UNLOGGED)? TABLE (IF NOT EXISTS)? tableName
    ;

createDefinitions
    : LEFT_PAREN (createDefinition (COMMA createDefinition)*)? RIGHT_PAREN
    ;

createDefinition
    : columnDefinition
    | tableConstraint
    | LIKE tableName likeOption*
    ;
	
likeOption
    : (INCLUDING | EXCLUDING)
    (COMMENTS | CONSTRAINTS | DEFAULTS | IDENTITY | INDEXES | STATISTICS | STORAGE | ALL)
    ;

inheritClause
    : INHERITS LEFT_PAREN tableName (COMMA tableName)* RIGHT_PAREN
    ;