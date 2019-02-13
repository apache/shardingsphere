grammar BaseRule;

import DataType, Keyword, Symbol;

ID 
    : (BQ_?[a-zA-Z_$][a-zA-Z0-9_$]* BQ_? DOT_)? (BQ_?[a-zA-Z_$][a-zA-Z0-9_$]* BQ_?)
    ;
    
schemaName
    : ID
    ;
    
databaseName
    : ID
    ;
    
domainName
    : ID
    ;
    
tableName
    : ID
    ;
    
columnName
    : ID
    ;
    
sequenceName
    : ID
    ;
    
tablespaceName
    : ID
    ;
    
collationName
    : STRING_ | ID
    ;
    
indexName
    : ID
    ;
    
alias
    : ID
    ;
    
cteName
    : ID
    ;
    
parserName
    : ID
    ;
    
extensionName
    : ID
    ;
    
rowName
    : ID
    ;
    
opclass
    : ID
    ;
    
fileGroup
    : ID
    ;
    
groupName
    : ID
    ;
    
constraintName
    : ID
    ;
    
keyName
    : ID
    ;
    
typeName
    : ID
    ;
    
xmlSchemaCollection
    : ID
    ;
    
columnSetName
    : ID
    ;
    
directoryName
    : ID
    ;
    
triggerName
    : ID
    ;
    
routineName
    : ID
    ;
    
roleName
    : STRING_ | ID
    ;
    
partitionName
    : ID
    ;
    
rewriteRuleName
    : ID
    ;
    
ownerName
    : ID
    ;
    
userName
    : STRING_ | ID
    ;
    
serverName
    : ID
    ;
    
dataTypeLength
    : LP_ (NUMBER_ (COMMA_ NUMBER_)?)? RP_
    ;

primaryKey
    : PRIMARY? KEY
    ;
    
matchNone
    : 'Default does not match anything'
    ;
    
ids
    : ID (COMMA_ ID)*
    ;
    
idList
    : LP_ ids RP_
    ;
    
rangeClause
    : rangeItem (COMMA_ rangeItem)* | rangeItem OFFSET rangeItem
    ;
    
rangeItem
    : number | question
    ;
    
schemaNames
    : schemaName (COMMA_ schemaName)*
    ;
    
databaseNames
    : databaseName (COMMA_ databaseName)*
    ;
    
domainNames
    : domainName (COMMA_ domainName)*
    ;
    
tableList
    : LP_ tableNames RP_
    ;
    
tableNames
    : tableName (COMMA_ tableName)*
    ;
    
columnNamesWithParen
    : LP_ columnNames RP_
    ;
    
columnNames
    : columnName (COMMA_ columnName)*
    ;
    
columnList
    : LP_ columnNames RP_
    ;
    
sequenceNames
    : sequenceName (COMMA_ sequenceName)*
    ;
    
tablespaceNames
    : tablespaceName (COMMA_ tablespaceName)*
    ;
    
indexNames
    : indexName (COMMA_ indexName)*
    ;
    
indexList
    : LP_ indexNames RP_
    ;
    
typeNames
    : typeName (COMMA_ typeName)*
    ;
    
rowNames
    : rowName (COMMA_ rowName)*
    ;
    
roleNames
    : roleName (COMMA_ roleName)*
    ;
    
userNames
    : userName (COMMA_ userName)*
    ;
    
serverNames
    : serverName (COMMA_ serverName)*
    ;
    
bitExprs
    : bitExpr (COMMA_ bitExpr)*
    ;
    
exprs
    : expr (COMMA_ expr)*
    ;
    
exprsWithParen
    : LP_ exprs RP_
    ;
    
expr
    : expr AND expr
    | expr AND_ expr
    | expr XOR expr
    | LP_ expr RP_
    | NOT expr
    | NOT_ expr
    | expr OR expr
    | expr OR_ expr
    | booleanPrimary
    | exprRecursive
    ;
    
exprRecursive
    : matchNone
    ;
    
booleanPrimary
    : booleanPrimary IS NOT? (TRUE | FALSE | UNKNOWN |NULL)
    | booleanPrimary SAFE_EQ_ predicate
    | booleanPrimary comparisonOperator predicate
    | booleanPrimary comparisonOperator (ALL | ANY) subquery
    | predicate
    ;
    
comparisonOperator
    : EQ_
    | GTE_
    | GT_
    | LTE_
    | LT_
    | NEQ_
    ;
    
predicate
    : bitExpr NOT? IN subquery
    | bitExpr NOT? IN LP_ simpleExpr (COMMA_ simpleExpr)* RP_
    | bitExpr NOT? BETWEEN simpleExpr AND predicate
    | bitExpr SOUNDS LIKE simpleExpr
    | bitExpr NOT? LIKE simpleExpr (ESCAPE simpleExpr)*
    | bitExpr NOT? REGEXP simpleExpr
    | bitExpr
    ;
    
bitExpr
    : bitExpr VERTICAL_BAR_ bitExpr
    | bitExpr AMPERSAND_ bitExpr
    | bitExpr SIGNED_LEFT_SHIFT_ bitExpr
    | bitExpr SIGNED_RIGHT_SHIFT_ bitExpr
    | bitExpr PLUS_ bitExpr
    | bitExpr MINUS_ bitExpr
    | bitExpr ASTERISK_ bitExpr
    | bitExpr SLASH_ bitExpr
    | bitExpr MOD bitExpr
    | bitExpr MOD_ bitExpr
    | bitExpr CARET_ bitExpr
    | bitExpr PLUS_ intervalExpr
    | bitExpr MINUS_ intervalExpr
    | simpleExpr
    ;
    
simpleExpr
    : functionCall
    | liter
    | columnName
    | simpleExpr collateClause
    //| param_marker
    | variable
    | simpleExpr AND_ simpleExpr
    | PLUS_ simpleExpr
    | MINUS_ simpleExpr
    | TILDE_ simpleExpr
    | NOT_ simpleExpr
    | BINARY simpleExpr
    | exprsWithParen
    | ROW exprsWithParen
    | subquery
    | EXISTS subquery
    // | (identifier expr)
    //| match_expr
    | caseExpress
    | intervalExpr
    | privateExprOfDb
    ;
    
functionCall
    : ID LP_ distinct? (exprs | ASTERISK_)? RP_
    ;
    
distinct
    : DISTINCT
    ;
    
intervalExpr
    : matchNone
    ;
       
caseExpress
    : matchNone
    ; 
    
privateExprOfDb
    : matchNone
    ;
    
variable
    : matchNone
    ;
    
liter
    : question
    | number
    | TRUE
    | FALSE
    | NULL
    | LBE_ ID STRING_ RBE_
    | HEX_DIGIT_
    | string
    | ID STRING_ collateClause?
    | (DATE | TIME | TIMESTAMP) STRING_
    | ID? BIT_NUM_ collateClause?
    ;
    
question
    : QUESTION_
    ;
    
number
   : NUMBER_
   ;
   
string
    : STRING_
    ;
    
subquery
    : matchNone
    ;
    
collateClause
    : matchNone
    ;
    
orderByClause
    : ORDER BY orderByItem (COMMA_ orderByItem)*
    ;
    
orderByItem
    : (columnName | number | expr) (ASC | DESC)?
    ;
