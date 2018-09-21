grammar PostgreCreateTable;
import PostgreKeyword, DataType, Keyword, PostgreBase, BaseRule, Symbol;

createTable:
    createBasicTable
    |createTypeTable
    |createTableForPartition
    ;

createBasicTable:
    createTableHeader
    createDefinitions
    inheritClause?
    partitionClause?
    tableWithClause?
    commitClause?
    tableSpaceClause?
    ;

createTableHeader:
    CREATE ((GLOBAL | LOCAL)? (TEMPORARY | TEMP) | UNLOGGED)? TABLE (IF NOT EXISTS)? tableName
    ;

createDefinitions:
   LEFT_PAREN (columnDefinition (COMMA columnDefinition)*)? RIGHT_PAREN
   ;

columnDefinition:
     (columnName dataType collateClause? columnConstraint*)
    | tableConstraint
    | LIKE tableName likeOption*
     ;

likeOption:
    (INCLUDING | EXCLUDING )
    (COMMENTS | CONSTRAINTS | DEFAULTS | IDENTITY | INDEXES | STATISTICS | STORAGE | ALL)
    ;

inheritClause:
    INHERITS LEFT_PAREN tableName (COMMA tableName)* RIGHT_PAREN
    ;

partitionClause:
    PARTITION BY (RANGE | LIST) LEFT_PAREN partitionClauseParam (COMMA partitionClauseParam)* RIGHT_PAREN
    ;

partitionClauseParam:
    (columnName | simpleExpr | LEFT_PAREN simpleExpr RIGHT_PAREN) collateClause? opclass?
    ;

tableWithClause:
    withStorageParameters
    |(WITH OIDS)
    |(WITHOUT OIDS)
    ;

commitClause:
    ON COMMIT (PRESERVE ROWS | DELETE ROWS | DROP)
    ;

tableSpaceClause:
    TABLESPACE tablespaceName
    ;

createTypeTable:
    createTableHeader
    typeNameClause
    createDefinition1s?
    partitionClause?
    tableWithClause?
    commitClause?
    tableSpaceClause?
    ;

typeNameClause:
    OF typeName
    ;

createDefinition1s:
    LEFT_PAREN createDefinition1 (COMMA createDefinition1)* RIGHT_PAREN
    ;

createDefinition1:
    (columnName (WITH OPTIONS )? columnConstraint*)
    | tableConstraint
    ;

createTableForPartition:
    createTableHeader
    partitionOfParent
    createDefinition1s?
    forValuesParition
    partitionClause?
    tableWithClause?
    commitClause?
    tableSpaceClause?
    ;

partitionOfParent:
    PARTITION OF tableName
    ;