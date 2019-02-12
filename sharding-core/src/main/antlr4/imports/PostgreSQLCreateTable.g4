grammar PostgreSQLCreateTable;

import PostgreSQLKeyword, DataType, Keyword, PostgreSQLBase, BaseRule, Symbol;

createTable
    : createTableHeader createDefinitions inheritClause?
    ;
    
createTableHeader
    : CREATE ((GLOBAL | LOCAL)? (TEMPORARY | TEMP) | UNLOGGED)? TABLE (IF NOT EXISTS)? tableName
    ;
    
createDefinitions
    : LP_ (createDefinition (COMMA_ createDefinition)*)? RP_
    ;
    
createDefinition
    : columnDefinition | tableConstraint | LIKE tableName likeOption*
    ;
    
likeOption
    : (INCLUDING | EXCLUDING) (COMMENTS | CONSTRAINTS | DEFAULTS | IDENTITY | INDEXES | STATISTICS | STORAGE | ALL)
    ;
    
inheritClause
    : INHERITS LP_ tableName (COMMA_ tableName)* RP_
    ;
