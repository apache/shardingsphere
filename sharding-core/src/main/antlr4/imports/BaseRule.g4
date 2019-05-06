grammar BaseRule;

import DataType, Keyword, Symbol;

ID: 
    (BQ_?[a-zA-Z_$][a-zA-Z0-9_$]* BQ_? DOT)? (BQ_?[a-zA-Z_$][a-zA-Z0-9_$]* BQ_?)
    ;
    
BLOCK_COMMENT
    : SLASH ASTERISK .*? ASTERISK SLASH -> channel(HIDDEN)
    ;
    
SL_COMMENT
    : MINUS MINUS ~[\r\n]* -> channel(HIDDEN)
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
    : STRING
    | ID
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
    : STRING | ID
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
    : STRING | ID
    ;
    
serverName
    : ID
    ;
    
dataTypeLength
    : LP_ (NUMBER (COMMA NUMBER)?)? RP_
    ;

primaryKey
    : PRIMARY? KEY
    ;
    
matchNone
    : 'Default does not match anything'
    ;
    
ids
    : ID (COMMA ID)*
    ;
    
idList
    : LP_ ids RP_
    ;
    
rangeClause
    : rangeItem (COMMA rangeItem)* | rangeItem OFFSET rangeItem
    ;
    
rangeItem
    : number | question
    ;
    
schemaNames
    : schemaName (COMMA schemaName)*
    ;
    
databaseNames
    : databaseName (COMMA databaseName)*
    ;
    
domainNames
    : domainName (COMMA domainName)*
    ;
    
tableList
    : LP_ tableNames RP_
    ;
    
tableNames
    : tableName (COMMA tableName)*
    ;
    
columnNamesWithParen
    : LP_ columnNames RP_
    ;
    
columnNames
    : columnName (COMMA columnName)*
    ;
    
columnList
    : LP_ columnNames RP_
    ;
    
sequenceNames
    : sequenceName (COMMA sequenceName)*
    ;
    
tablespaceNames
    : tablespaceName (COMMA tablespaceName)*
    ;
    
indexNames
    : indexName (COMMA indexName)*
    ;
    
indexList
    : LP_ indexNames RP_
    ;
    
typeNames
    : typeName (COMMA typeName)*
    ;
    
rowNames
    : rowName (COMMA rowName)*
    ;
    
roleNames
    : roleName (COMMA roleName)*
    ;
    
userNames
    : userName (COMMA userName)*
    ;
    
serverNames
    : serverName (COMMA serverName)*
    ;
    
bitExprs:
    bitExpr (COMMA bitExpr)*
    ;
    
exprs
    : expr (COMMA expr)*
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
    | booleanPrimary SAFE_EQ predicate
    | booleanPrimary comparisonOperator predicate
    | booleanPrimary comparisonOperator (ALL | ANY) subquery
    | predicate
    ;
    
comparisonOperator
    : EQ_
    | GTE
    | GT
    | LTE
    | LT
    | NEQ_
    | NEQ
    ;
    
predicate
    : bitExpr NOT? IN subquery
    | bitExpr NOT? IN LP_ simpleExpr (COMMA simpleExpr)* RP_
    | bitExpr NOT? BETWEEN simpleExpr AND predicate
    | bitExpr SOUNDS LIKE simpleExpr
    | bitExpr NOT? LIKE simpleExpr (ESCAPE simpleExpr)*
    | bitExpr NOT? REGEXP simpleExpr
    | bitExpr
    ;
    
bitExpr
    : bitExpr BIT_INCLUSIVE_OR bitExpr
    | bitExpr BIT_AND bitExpr
    | bitExpr SIGNED_LEFT_SHIFT bitExpr
    | bitExpr SIGNED_RIGHT_SHIFT bitExpr
    | bitExpr PLUS bitExpr
    | bitExpr MINUS bitExpr
    | bitExpr ASTERISK bitExpr
    | bitExpr SLASH bitExpr
    | bitExpr MOD bitExpr
    | bitExpr MOD_ bitExpr
    | bitExpr BIT_EXCLUSIVE_OR bitExpr
    | bitExpr PLUS intervalExpr
    | bitExpr MINUS intervalExpr
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
    | PLUS simpleExpr
    | MINUS simpleExpr
    | UNARY_BIT_COMPLEMENT simpleExpr
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
    : ID LP_ distinct? (exprs | ASTERISK)? RP_
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
    | LBE_ ID STRING RBE_
    | HEX_DIGIT
    | string
    | ID STRING collateClause?
    | (DATE | TIME | TIMESTAMP) STRING
    | ID? BIT_NUM collateClause?
    ;
    
question
    : QUESTION
    ;
    
number
   : NUMBER
   ;
   
string
    : STRING
    ;
    
subquery
    : matchNone
    ;
    
collateClause
    : matchNone
    ;
    
orderByClause
    : ORDER BY orderByItem (COMMA orderByItem)*
    ;
    
orderByItem
    : (columnName | number | expr) (ASC | DESC)?
    ;
