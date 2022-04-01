/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

grammar BaseRule;

import Symbol, Keyword, OracleKeyword, Literals;

parameterMarker
    : QUESTION_
    ;

literals
    : stringLiterals
    | numberLiterals
    | dateTimeLiterals
    | hexadecimalLiterals
    | bitValueLiterals
    | booleanLiterals
    | nullValueLiterals
    ;

stringLiterals
    : STRING_
    ;

numberLiterals
   : (PLUS_ | MINUS_)? (INTEGER_ | NUMBER_)
   ;

dateTimeLiterals
    : (DATE | TIME | TIMESTAMP) STRING_
    | LBE_ identifier STRING_ RBE_
    ;

hexadecimalLiterals
    : HEX_DIGIT_
    ;

bitValueLiterals
    : BIT_NUM_
    ;
    
booleanLiterals
    : TRUE | FALSE
    ;

nullValueLiterals
    : NULL
    ;

identifier
    : IDENTIFIER_ | unreservedWord
    ;

unreservedWord
    : TRUNCATE | FUNCTION | PROCEDURE | CASE | WHEN | CAST | TRIM | SUBSTRING
    | NATURAL | JOIN | FULL | INNER | OUTER | LEFT | RIGHT
    | CROSS | USING | IF | TRUE | FALSE | LIMIT | OFFSET
    | BEGIN | COMMIT | ROLLBACK | SAVEPOINT | BOOLEAN | DOUBLE | CHARACTER
    | ARRAY | INTERVAL | TIME | TIMESTAMP | LOCALTIME | LOCALTIMESTAMP | YEAR
    | QUARTER | MONTH | WEEK | DAY | HOUR | MINUTE | SECOND
    | MICROSECOND | MAX | MIN | SUM | COUNT | AVG | ENABLE
    | DISABLE | BINARY | ESCAPE | MOD | UNKNOWN | XOR | ALWAYS
    | CASCADE | GENERATED | PRIVILEGES | READ | WRITE | REFERENCES | TRANSACTION
    | ROLE | VISIBLE | INVISIBLE | EXECUTE | USE | DEBUG | UNDER
    | FLASHBACK | ARCHIVE | REFRESH | QUERY | REWRITE | KEEP | SEQUENCE
    | INHERIT | TRANSLATE | SQL | MERGE | AT | BITMAP | CACHE | CHECKPOINT
    | CONSTRAINTS | CYCLE | DBTIMEZONE | ENCRYPT | DECRYPT | DEFERRABLE
    | DEFERRED | EDITION | ELEMENT | END | EXCEPTIONS | FORCE | GLOBAL
    | IDENTITY | INITIALLY | INVALIDATE | JAVA | LEVELS | LOCAL | MAXVALUE
    | MINVALUE | NOMAXVALUE | NOMINVALUE | MINING | MODEL | NATIONAL | NEW
    | NOCACHE | NOCYCLE | NOORDER | NORELY | NOVALIDATE | ONLY | PRESERVE
    | PROFILE | REF | REKEY | RELY | REPLACE | SOURCE | SALT
    | SCOPE | SORT | SUBSTITUTABLE | TABLESPACE | TEMPORARY | TRANSLATION | TREAT
    | NO | TYPE | UNUSED | VALUE | VARYING | VIRTUAL | ZONE
    | ADVISOR | ADMINISTER | TUNING | MANAGE | MANAGEMENT | OBJECT
    | CONTEXT | EXEMPT | REDACTION | POLICY | DATABASE | SYSTEM
    | LINK | ANALYZE | DICTIONARY | DIMENSION | INDEXTYPE | EXTERNAL | JOB
    | CLASS | PROGRAM | SCHEDULER | LIBRARY | LOGMINING | MATERIALIZED | CUBE
    | MEASURE | FOLDER | BUILD | PROCESS | OPERATOR | OUTLINE | PLUGGABLE
    | CONTAINER | SEGMENT | RESTRICTED | COST | BACKUP | UNLIMITED
    | BECOME | CHANGE | NOTIFICATION | PRIVILEGE | PURGE | RESUMABLE
    | SYSGUID | SYSBACKUP | SYSDBA | SYSDG | SYSKM | SYSOPER | DBA_RECYCLEBIN |SCHEMA
    | DO | DEFINER | CURRENT_USER | CASCADED | CLOSE | OPEN | NEXT | NAME | NAMES
    | COLLATION | REAL | TYPE | FIRST | RANK | SAMPLE | SYSTIMESTAMP | INTERVAL | MINUTE | ANY 
    | LENGTH | SINGLE_C | capacityUnit
    ;

schemaName
    : identifier
    ;

tableName
    : (owner DOT_)? name
    ;

viewName
    : (owner DOT_)? name
    ;

materializedViewName
    : (owner DOT_)? name
    ;

columnName
    : (owner DOT_)? name
    ;

objectName
    : (owner DOT_)? name
    ;

clusterName
    : (owner DOT_)? name
    ;

indexName
    : (owner DOT_)? name
    ;

statisticsTypeName
    : (owner DOT_)? name
    ;

function
    : (owner DOT_)? name
    ;

packageName
    : (owner DOT_)? name
    ;

typeName
    : (owner DOT_)? name
    ;

indextypeName
    : (owner DOT_)? name
    ;

modelName
    : (owner DOT_)? name
    ;

operatorName
    : (owner DOT_)? name
    ;

dimensionName
    : (owner DOT_)? name
    ;

constraintName
    : identifier
    ;

savepointName
    : identifier
    ;

synonymName
    : identifier
    ;

owner
    : identifier
    ;

name
    : identifier
    ;

tablespaceName
    : identifier
    ;

tablespaceSetName
    : identifier
    ;

serviceName
    : identifier
    ;

ilmPolicyName
    : identifier
    ;

policyName
    : identifier
    ;

functionName
    : identifier
    ;

dbLink
    : identifier (DOT_ identifier)*
    ;

parameterValue
    : literals | identifier
    ;

directoryName
    : identifier
    ;

dispatcherName
    : stringLiterals
    ;

clientId
    : stringLiterals
    ;

opaqueFormatSpec
    : identifier
    ;

accessDriverType
    : identifier
    ;

varrayItem
    : identifier
    ;

nestedItem
    : identifier
    ;

storageTable
    : identifier
    ;

lobSegname
    : identifier
    ;

locationSpecifier
    : identifier
    ;

xmlSchemaURLName
    : identifier
    ;

elementName
    : identifier
    ;

subpartitionName
    : identifier
    ;

parameterName
    : identifier
    ;

editionName
    : identifier
    ;

containerName
    : identifier
    ;

partitionName
    : identifier
    ;

partitionSetName
    : identifier
    ;

partitionKeyValue
    : INTEGER_ | dateTimeLiterals
    ;

subpartitionKeyValue
    : INTEGER_ | dateTimeLiterals
    ;

zonemapName
    : identifier
    ;

flashbackArchiveName
    : identifier
    ;

roleName
    : identifier
    ;

username
    : identifier
    ;

password
    : identifier
    ;

logGroupName
    : identifier
    ;

columnNames
    : LP_? columnName (COMMA_ columnName)* RP_?
    ;

tableNames
    : LP_? tableName (COMMA_ tableName)* RP_?
    ;

oracleId
    : identifier | (STRING_ DOT_)* STRING_
    ;

collationName
    : STRING_ | IDENTIFIER_
    ;

columnCollationName
    : identifier
    ;

alias
    : identifier | STRING_
    ;

dataTypeLength
    : LP_ (INTEGER_ (COMMA_ INTEGER_)?)? RP_
    ;

primaryKey
    : PRIMARY? KEY
    ;

exprs
    : expr (COMMA_ expr)*
    ;

exprList
    : LP_ exprs RP_
    ;

// TODO comb expr
expr
    : expr andOperator expr
    | expr orOperator expr
    | notOperator expr
    | LP_ expr RP_
    | booleanPrimary
    ;

andOperator
    : AND | AND_
    ;

orOperator
    : OR | OR_
    ;

notOperator
    : NOT | NOT_
    ;

booleanPrimary
    : booleanPrimary IS NOT? (TRUE | FALSE | UNKNOWN | NULL)
    | booleanPrimary SAFE_EQ_ predicate
    | booleanPrimary comparisonOperator predicate
    | booleanPrimary comparisonOperator (ALL | ANY) subquery
    | predicate
    ;

comparisonOperator
    : EQ_ | GTE_ | GT_ | LTE_ | LT_ | NEQ_
    ;

predicate
    : bitExpr NOT? IN subquery
    | bitExpr NOT? IN LP_ expr (COMMA_ expr)* RP_
    | bitExpr NOT? IN LP_ expr (COMMA_ expr)* RP_ AND predicate
    | bitExpr NOT? BETWEEN bitExpr AND predicate
    | bitExpr NOT? LIKE simpleExpr (ESCAPE simpleExpr)?
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
    | bitExpr MOD_ bitExpr
    | bitExpr CARET_ bitExpr
    | simpleExpr
    ;

simpleExpr
    : functionCall
    | parameterMarker
    | literals
    | simpleExpr OR_ simpleExpr
    | (PLUS_ | MINUS_ | TILDE_ | NOT_ | BINARY) simpleExpr
    | ROW? LP_ expr (COMMA_ expr)* RP_
    | EXISTS? subquery
    | LBE_ identifier expr RBE_
    | caseExpression
    | columnName
    | privateExprOfDb
    ;

functionCall
    : aggregationFunction | specialFunction | regularFunction 
    ;

aggregationFunction
    : aggregationFunctionName LP_ (((DISTINCT | ALL)? expr) | ASTERISK_) RP_ (OVER LP_ analyticClause RP_)?
    ;

aggregationFunctionName
    : MAX | MIN | SUM | COUNT | AVG | GROUPING
    ;

analyticClause
    : queryPartitionClause? (orderByClause windowingClause?)?
    ;

queryPartitionClause
    : PARTITION BY (exprs | exprList)
    ;

windowingClause
    : (ROWS | RANGE) ((BETWEEN (UNBOUNDED PRECEDING | CURRENT ROW | expr (PRECEDING | FOLLOWING)) AND (UNBOUNDED FOLLOWING | CURRENT ROW | expr (PRECEDING | FOLLOWING)))
    | (UNBOUNDED PRECEDING | CURRENT ROW | expr PRECEDING))
    ;

specialFunction
    : castFunction  | charFunction
    ;

castFunction
    : CAST LP_ expr AS dataType RP_
    ;

charFunction
    : CHAR LP_ expr (COMMA_ expr)* (USING ignoredIdentifier)? RP_
    ;

regularFunction
    : regularFunctionName LP_ (expr (COMMA_ expr)* | ASTERISK_)? RP_
    ;

regularFunctionName
    : identifier | IF | LOCALTIME | LOCALTIMESTAMP | INTERVAL | DECODE
    ;

caseExpression
    : CASE simpleExpr? caseWhen+ caseElse? END
    ;

caseWhen
    : WHEN expr THEN expr
    ;

caseElse
    : ELSE expr
    ;

subquery
    : matchNone
    ;

orderByClause
    : ORDER SIBLINGS? BY orderByItem (COMMA_ orderByItem)*
    ;

orderByItem
    : (columnName | numberLiterals | expr) (ASC | DESC)? (NULLS FIRST | NULLS LAST)?
    ;

attributeName
    : oracleId
    ;

simpleExprs
    : simpleExpr (COMMA_ simpleExpr)*
    ;

lobItem
    : attributeName | columnName
    ;

lobItems
    : lobItem (COMMA_ lobItem)*
    ;

lobItemList
    : LP_ lobItems RP_
    ;

dataType
    : dataTypeName dataTypeLength? | specialDatatype | dataTypeName dataTypeLength? datetimeTypeSuffix
    ;

specialDatatype
    : dataTypeName (LP_ NUMBER_ CHAR RP_) | NATIONAL dataTypeName VARYING? LP_ NUMBER_ RP_ | dataTypeName LP_? columnName RP_?
    ;

dataTypeName
    : CHAR | NCHAR | RAW | VARCHAR | VARCHAR2 | NVARCHAR2 | LONG | LONG RAW | BLOB | CLOB | NCLOB | BINARY_FLOAT | BINARY_DOUBLE
    | BOOLEAN | PLS_INTEGER | BINARY_INTEGER | INTEGER | NUMBER | NATURAL | NATURALN | POSITIVE | POSITIVEN | SIGNTYPE
    | SIMPLE_INTEGER | BFILE | MLSLABEL | UROWID | DATE | TIMESTAMP | TIMESTAMP WITH TIME ZONE | TIMESTAMP WITH LOCAL TIME ZONE
    | INTERVAL DAY TO SECOND | INTERVAL YEAR TO MONTH | JSON | FLOAT | REAL | DOUBLE PRECISION | INT | SMALLINT
    | DECIMAL | NUMERIC | DEC | IDENTIFIER_ | XMLTYPE
    ;

datetimeTypeSuffix
    : (WITH LOCAL? TIME ZONE)? | TO MONTH | TO SECOND (LP_ NUMBER_ RP_)?
    ;

treatFunction
    : TREAT LP_ expr AS REF? dataTypeName RP_
    ;

privateExprOfDb
    : treatFunction | caseExpr | intervalExpression | objectAccessExpression | constructorExpr
    ;

caseExpr
    : CASE (simpleCaseExpr | searchedCaseExpr) elseClause? END
    ;

simpleCaseExpr
    : expr searchedCaseExpr+
    ;

searchedCaseExpr
    : WHEN expr THEN simpleExpr
    ;

elseClause
    : ELSE expr
    ;

intervalExpression
    : LP_ expr MINUS_ expr RP_ (DAY (LP_ NUMBER_ RP_)? TO SECOND (LP_ NUMBER_ RP_)? | YEAR (LP_ NUMBER_ RP_)? TO MONTH)
    ;

objectAccessExpression
    : (LP_ simpleExpr RP_ | treatFunction) DOT_ (attributeName (DOT_ attributeName)* (DOT_ functionCall)? | functionCall)
    ;

constructorExpr
    : NEW dataTypeName exprList
    ;

ignoredIdentifier
    : IDENTIFIER_
    ;

ignoredIdentifiers
    : ignoredIdentifier (COMMA_ ignoredIdentifier)*
    ;

matchNone
    : 'Default does not match anything'
    ;

hashSubpartitionQuantity
    : NUMBER
    ;

odciParameters
    : identifier
    ;

databaseName
    : identifier
    ;

locationName
    : STRING_
    ;

fileName
    : STRING_
    ;

asmFileName
    : STRING_
    ;

fileNumber
    : INTEGER_
    ;

instanceName
    : STRING_
    ;

logminerSessionName
    : identifier
    ;

tablespaceGroupName
    : identifier
    ;

copyName
    : identifier
    ;

mirrorName
    : identifier
    ;

uriString
    : identifier
    ;

qualifiedCredentialName
    : identifier
    ;

pdbName
    : identifier
    ;

diskgroupName
    : identifier
    ;

templateName
    : identifier
    ;

aliasName
    : identifier
    ;

domain
    : identifier
    ;

dateValue
    : dateTimeLiterals | stringLiterals | numberLiterals | expr
    ;

sessionId
    : numberLiterals
    ;

serialNumber
    : numberLiterals
    ;

instanceId
    : NUMBER_
    ;

sqlId
    : identifier
    ;

logFileName
    : stringLiterals
    ;

logFileGroupsArchivedLocationName
    : stringLiterals
    ;

asmVersion
    : stringLiterals
    ;

walletPassword
    : identifier
    ;

hsmAuthString
    : identifier
    ;

targetDbName
    : identifier
    ;

certificateId
    : identifier
    ;

categoryName
    : identifier
    ;

offset
    : numberLiterals | expr | nullValueLiterals
    ;

rowcount
    : numberLiterals | expr | nullValueLiterals
    ;

percent
    : numberLiterals | expr | nullValueLiterals
    ;

rollbackSegment
    : identifier
    ;

queryName
    : (owner DOT_)? name
    ;

cycleValue
    : STRING_
    ;

noCycleValue
    : STRING_
    ;

orderingColumn
    : columnName
    ;

subavName
    : (owner DOT_)? name
    ;

baseAvName
    : (owner DOT_)? name
    ;

measName
    : identifier
    ;

levelRef
    : identifier
    ;

offsetExpr
    : expr | numberLiterals
    ;

memberKeyExpr
    : identifier
    ;

depthExpression
    : identifier
    ;

unitName
    : (owner DOT_)? name
    ;

procedureName
    : identifier
    ;

cpuCost
    : INTEGER_
    ;

ioCost
    : INTEGER_
    ;

networkCost
    : INTEGER_
    ;

defaultSelectivity
    : INTEGER_
    ;

dataItem
    : variableName
    ;

variableName
    : identifier | stringLiterals
    ;

validTimeColumn
    : columnName
    ;

attrDim
    : identifier
    ;

hierarchyName
    : (owner DOT_)? name
    ;

analyticViewName
    : (owner DOT_)? name
    ;

samplePercent
    : numberLiterals
    ;

seedValue
    : numberLiterals
    ;

namespace
    : identifier
    ;

restorePoint
    : identifier
    ;

scnValue
    : literals
    ;

timestampValue
    : LP_? expr+ RP_?
    ;

scnTimestampExpr
    : scnValue | timestampValue
    ;

referenceModelName
    : identifier
    ;

mainModelName
    : identifier
    ;

measureColumn
    : columnName
    ;

dimensionColumn
    : columnName
    ;

pattern
    : stringLiterals
    ;

analyticFunctionName
    : identifier
    ;

condition
    : comparisonCondition
    | floatingPointCondition
    | condition (AND | OR) condition | NOT condition
    | modelCondition
    | multisetCondition
    | patternMatchingCondition
    | rangeCondition
    | nullCondition
    | xmlCondition
    | jsonCondition
    | LP_ condition RP_ | NOT condition | condition (AND | OR) condition
    | existsCondition
    | inCondition
    | isOfTypeCondition
    ;

comparisonCondition
    : simpleComparisonCondition | groupComparisonCondition
    ;

simpleComparisonCondition
    : (expr (EQ_ | NEQ_ | GT_ | LT_ | GTE_ | LTE_) expr)
    | (exprList (EQ_ | NEQ_) LP_ (expressionList | subquery) RP_)
    ;

expressionList
    : exprs | LP_ expr? (COMMA_ expr?)* RP_
    ;

groupComparisonCondition
    : (expr (EQ_ | NEQ_ | GT_ | LT_ | GTE_ | LTE_) (ANY | SOME | ALL) LP_ (expressionList | subquery) RP_)
    | (exprList (EQ_ | NEQ_) (ANY | SOME | ALL) LP_ ((expressionList (SQ_ expressionList)*) | subquery) RP_)
    ;

floatingPointCondition
    : expr IS NOT? (NAN | INFINITE)
    ;

logicalCondition
    : (condition (AND | OR) condition) | NOT condition
    ;

modelCondition
    : isAnyCondition | isPresentCondition
    ;

isAnyCondition
    : (dimensionColumn IS)? ANY
    ;

isPresentCondition
    : cellReference IS PRESENT
    ;

cellReference
    : identifier
    ;

multisetCondition
    : isASetCondition 
    | isEmptyCondition 
    | memberCondition 
    | submultisetCondition
    ;

isASetCondition
    : tableName IS NOT? A SET
    ;

isEmptyCondition
    : tableName IS NOT? EMPTY
    ;

memberCondition
    : expr NOT? MEMBER OF? tableName
    ;

submultisetCondition
    : tableName NOT? SUBMULTISET OF? tableName
    ;

patternMatchingCondition
    : likeCondition | regexpLikeCondition
    ;

likeCondition
    : searchValue NOT? (LIKE | LIKEC | LIKE2 | LIKE4) pattern (ESCAPE escapeChar)?
    ;

searchValue
    : identifier | stringLiterals
    ;

escapeChar
    : stringLiterals
    ;

regexpLikeCondition
    : REGEXP_LIKE LP_ searchValue COMMA_ pattern (COMMA_ matchParam)? RP_
    ;

matchParam
    : stringLiterals
    ;

rangeCondition
    : expr NOT? BETWEEN expr AND expr
    ;

nullCondition
    : expr IS NOT? NULL
    ;

xmlCondition
    : equalsPathCondition | underPathCondition
    ;

equalsPathCondition
    : EQUALS_PATH LP_ columnName COMMA_ pathString (COMMA_ correlationInteger)? RP_
    ;

pathString
    : stringLiterals
    ;

correlationInteger
    : INTEGER_
    ;

underPathCondition
    : UNDER_PATH LP_ columnName (COMMA_ levels)? COMMA_ pathString (COMMA_ correlationInteger)? RP_
    ;

level
    : identifier
    ;

levels
    : INTEGER_
    ;

jsonCondition
    : isJsonCondition | jsonExistsCondition | jsonTextcontainsCondition
    ;

isJsonCondition
    : expr IS NOT? JSON (FORMAT JSON)? (STRICT | LAX)? ((WITH | WITHOUT) UNIQUE KEYS)?
    ;

jsonEqualCondition
    : JSON_EQUAL LP_ expr COMMA_ expr RP_
    ;

jsonExistsCondition
    : JSON_EXISTS LP_ expr (FORMAT JSON)? COMMA_ jsonBasicPathExpr 
    jsonPassingClause? jsonExistsOnErrorClause? jsonExistsOnEmptyClause? RP_
    ;

jsonPassingClause
    : PASSING expr AS identifier (COMMA_ expr AS identifier)*
    ;

jsonExistsOnErrorClause
    : (ERROR | TRUE | FALSE) ON ERROR
    ;

jsonExistsOnEmptyClause
    : (ERROR | TRUE | FALSE) ON EMPTY
    ;

jsonTextcontainsCondition
    : JSON_TEXTCONTAINS LP_ columnName COMMA_ jsonBasicPathExpr COMMA_ stringLiterals RP_
    ;

jsonBasicPathExpr
    : jsonAbsolutePathExpr | jsonRelativePathExpr
    ;

jsonAbsolutePathExpr
    : DOLLAR_ jsonNonfunctionSteps? jsonFunctionStep?
    ;

jsonNonfunctionSteps
    : ((jsonObjectStep | jsonArrayStep | jsonDescendentStep) jsonFilterExpr?)+
    ;

jsonObjectStep
    : DOT_ASTERISK_ | DOT_ jsonFieldName
    ;

jsonFieldName
    : jsonString | (letter (letter | digit)*)
    ;

letter
    : identifier
    ;

digit
    : numberLiterals
    ;

jsonArrayStep
    : LBT_ (ASTERISK_ | INTEGER_ (TO INTEGER_)? (COMMA_ INTEGER_ (TO INTEGER_)?)*) RBT_
    ;

jsonDescendentStep
    : DOT_ DOT_ jsonFieldName
    ;

jsonFunctionStep
    : DOT_ jsonItemMethod LP_ RP_
    ;

jsonItemMethod
    : identifier
    ;

jsonFilterExpr
    : QUESTION_ LP_ jsonCond RP_
    ;

jsonCond
    : jsonCond OR_ jsonCond | jsonCond AND_ jsonCond | jsonNegation 
    | LP_ jsonCond RP_ | jsonComparison | jsonExistsCond 
    | jsonInCond | jsonLikeCond | jsonLikeRegexCond 
    | jsonEqRegexCond | jsonHasSubstringCond | jsonStartsWithCond
    ;

jsonDisjunction
    : jsonCond OR_ jsonCond
    ;

jsonConjunction
    : jsonCond AND_ jsonCond
    ;

jsonNegation
    : NOT_ LP_ jsonCond RP_
    ;

jsonExistsCond
    : EXISTS LP_ jsonRelativePathExpr RP_
    ;

jsonHasSubstringCond
    : jsonRelativePathExpr HAS SUBSTRING (jsonString | jsonVar)
    ;

jsonStartsWithCond
    : jsonRelativePathExpr STARTS WITH (jsonString | jsonVar)
    ;

jsonLikeCond
    : jsonRelativePathExpr LIKE (jsonString | jsonVar)
    ;

jsonLikeRegexCond
    : jsonRelativePathExpr LIKE_REGEX (jsonString | jsonVar)
    ;

jsonEqRegexCond
    : jsonRelativePathExpr EQ_REGEX (jsonString | jsonVar)
    ;

jsonInCond
    : jsonRelativePathExpr IN valueList
    ;

valueList
    : LP_ (jsonScalar | jsonVar) (COMMA_ (jsonScalar | jsonVar))* RP_
    ;

jsonComparison
    : (jsonRelativePathExpr jsonComparePred (jsonVar | jsonScalar))
    | ((jsonVar | jsonScalar) jsonComparePred jsonRelativePathExpr) 
    | (jsonScalar jsonComparePred jsonScalar)
    ;

jsonRelativePathExpr
    : AT_ jsonNonfunctionSteps? jsonFunctionStep?
    ;

jsonComparePred
    : DEQ_ | NEQ_ | LT_ | LTE_ | GTE_ | GT_
    ;

jsonVar
    : DOLLAR_ identifier
    ;

jsonScalar
    : jsonNumber | TRUE | FALSE | NULL | jsonString
    ;

jsonNumber
    : numberLiterals
    ;

jsonString
    : stringLiterals | identifier
    ;

compoundCondition
    : LP_ condition RP_ 
    | NOT condition 
    | condition (AND | OR) condition
    ;

existsCondition
    : EXISTS LP_ subquery RP_
    ;

inCondition
    : (expr NOT? IN LP_ (expressionList | subquery) RP_) 
    | (exprList NOT? IN LP_ ((expressionList (COMMA_ expressionList)*) | subquery) RP_)
    ;

isOfTypeCondition
    : expr IS NOT? OF TYPE? LP_ ONLY? typeName (COMMA_ ONLY? typeName)* RP_
    ;

databaseCharset
    : AL32UTF8
    ;

nationalCharset
    : AL16UTF16 | UTF8
    ;

filenamePattern
    : STRING_
    ;

connectString
    : STRING_
    ;

argument
    : identifier
    ;

dataSource
    : identifier
    ;

implementationType
    : (owner DOT_)? name
    ;

implementationPackage
    : (owner DOT_)? name
    ;

label
    : identifier
    ;

libName
    : identifier
    ;

externalDatatype
    : dataType
    ;
    
capacityUnit
    : ('K' | 'M' | 'G' | 'T' | 'P' | 'E')
    ;
