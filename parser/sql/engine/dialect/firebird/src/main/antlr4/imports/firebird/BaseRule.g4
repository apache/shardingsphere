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

import Symbol, Keyword, FirebirdKeyword, Literals;

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
    : characterSetName? STRING_ collateClause?
    ;

numberLiterals
    : (PLUS_ | MINUS_)? NUMBER_
    ;

dateTimeLiterals
    : (DATE | TIME | TIMESTAMP) STRING_
    | LBE_ identifier STRING_ RBE_
    ;

hexadecimalLiterals
    : characterSetName? HEX_DIGIT_ collateClause?
    ;

bitValueLiterals
    : characterSetName? BIT_NUM_ collateClause?
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
    : ADA
    | C92 | CATALOG_NAME | CHARACTER_SET_CATALOG | CHARACTER_SET_NAME | CHARACTER_SET_SCHEMA
    | CLASS_ORIGIN | COBOL | COLLATION_CATALOG | COLLATION_NAME | COLLATION_SCHEMA
    | COLUMN_NAME | COMMAND_FUNCTION | COMMITTED | CONDITION_NUMBER | CONNECTION_NAME
    | CONSTRAINT_CATALOG | CONSTRAINT_NAME | CONSTRAINT_SCHEMA | CURSOR_NAME
    | DATA | DATETIME_INTERVAL_CODE | DATETIME_INTERVAL_PRECISION | DYNAMIC_FUNCTION
    | FORTRAN
    | LENGTH
    | MESSAGE_LENGTH | MESSAGE_OCTET_LENGTH | MESSAGE_TEXT | MORE92 | MUMPS
    | NAME | NULLABLE | NUMBER
    | PASCAL | PLI
    | REPEATABLE | RETURNED_LENGTH | RETURNED_OCTET_LENGTH | RETURNED_SQLSTATE | ROW_COUNT
    | SCALE | SCHEMA_NAME | SERIALIZABLE | SERVER_NAME | SUBCLASS_ORIGIN
    | TABLE_NAME | TYPE
    | UNCOMMITTED | UNNAMED | VALUE | FIRSTNAME | MIDDLENAME | LASTNAME
    ;

variable
    : (AT_? AT_)? (GLOBAL | LOCAL)? DOT_? identifier
    | DEFAULT
    ;

schemaName
    : identifier
    ;

savepointName
    : identifier
    ;

variableName
    : (owner DOT_)? name
    ;

domainName
    : identifier
    ;

packageName
    : identifier
    ;

tableName
    : (owner DOT_)? name
    ;

parameterName
    : identifier
    ;

collationName
    : identifier
    ;

attributeName
    : identifier
    ;

login
    : identifier
    ;

password
    : STRING_
    ;

roleName
    : identifier
    ;

columnName
    : (owner DOT_)? name
    ;

viewName
    : identifier
    | (owner DOT_)? identifier
    ;

functionName
    : identifier
    ;

triggerName
    : identifier
    ;

argumentName
    : identifier
    ;

owner
    : identifier
    ;

engineName
    : identifier
    ;

information
    : identifier
    ;

localVariableDeclarationName
    : identifier
    ;

baseSortName
    : identifier
    ;

constraintName
    : identifier
    ;

externalModuleName
    : identifier
    ;

cursorName
    : identifier
    ;

procedureName
    : identifier
    ;

name
    : identifier
    ;

columnNames
    : LP_? columnName (COMMA_ columnName)* RP_?
    ;

tableNames
    : LP_? tableName (COMMA_ tableName)* RP_?
    ;

characterSetName
    : IDENTIFIER_
    ;

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
    : OR
    ;

notOperator
    : NOT | NOT_
    ;

booleanPrimary
    : booleanPrimary IS NOT? (TRUE | FALSE | UNKNOWN | NULL)
    | booleanPrimary SAFE_EQ_ predicate
    | booleanPrimary comparisonOperator predicate
    | booleanPrimary comparisonOperator (ALL | SOME | ANY) subquery
    | predicate
    ;

comparisonOperator
    : EQ_ | GTE_ | GT_ | LTE_ | LT_ | NEQ_
    ;

predicate
    :
    | bitExpr NOT? IN subquery
    | bitExpr NOT? IN LP_ expr (COMMA_ expr)* RP_
    | bitExpr NOT? BETWEEN bitExpr AND predicate
    | bitExpr NOT? LIKE simpleExpr (ESCAPE simpleExpr)?
    | bitExpr NOT? STARTING WITH? bitExpr
    | bitExpr IS NOT? DISTINCT FROM bitExpr
    | bitExpr NOT? SIMILAR TO bitExpr (ESCAPE bitExpr)?
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
    | bitExpr PLUS_ intervalExpression
    | bitExpr MINUS_ intervalExpression
    | simpleExpr
    ;

simpleExpr
    : functionCall
    | parameterMarker
    | literals
    | simpleExpr CONCAT_ simpleExpr
    | columnName
    | simpleExpr COLLATE (STRING_ | identifier)
    | variable
    | (PLUS_ | MINUS_ | TILDE_ | NOT_) simpleExpr
    | LP_ expr (COMMA_ expr)* RP_
    | EXISTS? subquery
    | LBE_ identifier expr RBE_
    | matchExpression
    | caseExpression
    | intervalExpression
    ;

functionCall
    : aggregationFunction | specialFunction | regularFunction
    ;

aggregationFunction
    : aggregationFunctionName LP_ distinct? (expr (COMMA_ expr)* | ASTERISK_)? RP_ overClause?
    ;

aggregationFunctionName
    : MAX | MIN | SUM | COUNT | AVG
    ;

distinct
    : DISTINCT
    ;

specialFunction
    : castFunction
    | convertFunction
    | positionFunction
    | substringFunction
    | extractFunction
    | trimFunction
    | windowFunction
    | genIdFunction
    ;

castFunction
    : CAST LP_ (expr | NULL) AS dataType RP_
    ;

convertFunction
    : CONVERT LP_ expr USING identifier RP_
    ;

positionFunction
    : POSITION LP_ expr IN expr RP_
    ;

substringFunction
    : SUBSTRING LP_ expr FROM NUMBER_ (FOR NUMBER_)? RP_
    ;

extractFunction
    : EXTRACT LP_ identifier FROM expr RP_
    ;

trimFunction
    : TRIM LP_ ((LEADING | BOTH | TRAILING) expr? FROM)? expr RP_
    | TRIM LP_ (expr FROM)? expr RP_
    ;

genIdFunction
    : GEN_ID LP_ (variable | parameterMarker) COMMA_ expr RP_
    ;

regularFunction
    : completeRegularFunction
    | contextVariables
    ;

completeRegularFunction
    : regularFunctionName LP_ (expr (COMMA_ expr)* | ASTERISK_)? RP_
    ;

regularFunctionName
    : identifier | IF | INTERVAL
    | CHAR_LENGTH | CHARACTER_LENGTH | BIT_LENGTH | OCTET_LENGTH
    | UPPER | LOWER
    | NULLIF
    | COALESCE
    ;

matchExpression
    : literals MATCH UNIQUE? (PARTIAL | FULL)  subquery
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

intervalExpression
    : INTERVAL expr intervalUnit
    ;

intervalUnit
    : MICROSECOND | SECOND | MINUTE | HOUR | DAY | WEEK | MONTH | QUARTER | YEAR
    ;

subquery
    : 'Default does not match anything'
    ;

orderByClause
    : ORDER BY orderByItem (COMMA_ orderByItem)* limitClause?
    ;

orderByItem
    : (numberLiterals | expr) (ASC | DESC)?
    ;

limitClause
    : rowsClause | offsetDefinition
    ;

rowsClause
    : ROWS expr (TO expr)?
    ;

offsetDefinition
    : offsetClause
    | fetchClause
    | (offsetClause fetchClause)
    ;

offsetClause
    : OFFSET limitOffset (ROW | ROWS)
    ;

fetchClause
    : FETCH (FIRST | NEXT) limitRowCount (ROW | ROWS) ONLY
    ;

limitRowCount
    : numberLiterals | parameterMarker | bindLiterals
    ;

limitOffset
    : numberLiterals | parameterMarker | bindLiterals
    ;

optimizeClause
    : OPTIMIZE FOR (FIRST | ALL) ROWS
    ;

dataType
    : blobDataType
    | dataTypeName dataTypeLength? characterSet? collateClause?
    | dataTypeName LP_ STRING_ (COMMA_ STRING_)* RP_ characterSet? collateClause?
    | dataTypeName LBT_ (NUMBER_? COLON_ NUMBER | NUMBER_ (COMMA_ NUMBER_)*) RBT_
    ;

dataTypeName
    : CHARACTER | CHARACTER VARYING | CHAR VARYING | NATIONAL CHARACTER | NATIONAL CHARACTER VARYING | CHAR | VARCHAR | NCHAR
    | NATIONAL CHAR | NATIONAL CHAR VARYING | BIT | BIT VARYING | NUMERIC | DECIMAL | DEC | INTEGER | SMALLINT | BOOLEAN
    | FLOAT | REAL | DOUBLE PRECISION | DATE | TIME | TIMESTAMP | INTERVAL | TIME WITH TIME ZONE | TIMESTAMP WITH TIME ZONE | BLOB
    | identifier
    ;

blobDataType
    : BLOB blobSubTypeDefinition? blobSegmentSizeClause? characterSet?
    | BLOB (LP_ blobAbbreviatedAttributes RP_)?
    ;

blobSubTypeDefinition
    : SUB_TYPE blobSubType
    ;

blobSegmentSizeClause
    : SEGMENT SIZE NUMBER_
    ;

blobAbbreviatedAttributes
    : NUMBER_
    | NUMBER_ COMMA_ blobSubType
    | COMMA_ blobSubType
    ;

blobSubType
    : blobSubTypeName
    | blobSubTypeNumber
    ;

blobSubTypeName
    : BINARY | TEXT | BLR | ACL | RANGES | SUMMARY | FORMAT | TRANSACTION_DESCRIPTION | EXTERNAL_FILE_DESCRIPTION
    ;

blobSubTypeNumber
    : NUMBER_
    ;

dataTypeLength
    : LP_ NUMBER_ (COMMA_ NUMBER_)? RP_
    ;

characterSet
    : (CHARACTER | CHAR) SET EQ_? ignoredIdentifier
    ;

collateClause
    : COLLATE EQ_? (STRING_ | ignoredIdentifier)
    ;

ignoredIdentifier
    : identifier (DOT_ identifier)?
    ;

dropBehaviour
    : (CASCADE | RESTRICT)?
    ;

windowFunction
    : funcName = (ROW_NUMBER | RANK | DENSE_RANK) LP_ (expr (COMMA_ expr)* | ASTERISK_)? RP_ overClause?
    | funcName = (LEAD | LAG | FIRST_VALUE | LAST_VALUE | NTH_VALUE) LP_ (expr (COMMA_ expr)* | ASTERISK_)? RP_ overClause?
    ;

overClause
    : OVER LP_ (PARTITION BY expr (COMMA_ expr)*)? orderByClause? RP_
    ;

attributeCollation
    : SQ_ attributeCollationName EQ_ (STRING_ | NUMBER_) SQ_
    ;

attributeCollationName
    : DISABLE_COMPRESSIONS
    | DISABLE_EXPANSIONS
    | ICU_VERSION
    | LOCALE
    | MULTI_LEVEL
    | NUMERIC_SORT
    | SPECIALS_FIRST
    ;

defaultValue
    : (literals | NULL | contextVariables)
    ;

contextVariables
    : CURRENT_CONNECTION | CURRENT_DATE | CURRENT_ROLE
    | CURRENT_TIME | CURRENT_TIMESTAMP
    | LOCALTIME | LOCALTIMESTAMP
    | CURRENT_TRANSACTION | CURRENT_USER | USER
    | INSERTING | UPDATING | DELETING
    | NEW | NOW | OLD | ROW_COUNT
    | SQLCODE | GDSCODE | SQLSTATE
    | TODAY | TOMORROW | USER | YESTERDAY
    ;

announcementArgument
    : argumentName typeDescriptionArgument (NOT NULL)? collateClause?
    ;

announcementArgumentClause
    : announcementArgument (COMMA_ announcementArgument)*
    ;

typeDescriptionArgument
    : dataType
    | (TYPE OF)? domainName
    | TYPE OF COLUMN (tableName | viewName) DOT_ columnName
    ;


externalModule
    : EQ_ externalModuleName NOT_ functionName (NOT_ information)? EQ_
    ;

sortOrder
    : DOS850 | DB_DEU850 | DB_ESP850 | DB_FRA850 | DB_FRC850 | DB_ITA850 | DB_NLD850 | DB_PTB850 | DB_SVE850 | DB_UK850 | DB_US850
    | DOS852 | DB_CSY | DB_PLK | DB_SLO | PDOX_CSY | PDOX_HUN | PDOX_PLK | PDOX_SLO
    | DOS857 | DB_TRK
    | DOS858
    | DOS860 | DB_PTG860
    | DOS861 | PDOX_IS
    | DOS862
    | DOS863 | DB_FRC863
    | DOS864
    | DOS865 | DB_DAN865 | DB_NOR865 | PDOX_NORDAN4
    | DOS866
    | DOS869
    | EUCJ_0208
    | GB_2312
    | ISO8859_1 | DA_DA | DE_DE | DU_NL | EN_UK | EN_US | ES_ES | ES_ES_CI_AI | FI_FI | FR_CA | FR_FR | IS_IS | IT_IT | NO_NO | PT_PT | PT_BR | SV_SV
    | ISO8859_2 | CS_CZ | ISO_HUN | ISO_PLK
    | ISO8859_3
    | ISO8859_4
    | ISO8859_5
    | ISO8859_6
    | ISO8859_7
    | ISO8859_8
    | ISO8859_9
    | ISO8859_13 | LT_LT
    | KOI8R | KOI8R_RU
    | KOI8U | KOI8R_UA
    | KSC_5601 | KSC_DIC_TIONAR
    | NEXT | NXT_DEU | NXT_ESP | NXT_FRA | NXT_ITA | NXT_US
    | NONE
    | OCTETS
    | SJIS_0208
    | UNICODE_FSS
    | UTF8 | USC_BASIC | UNICODE
    | WIN1250 | BS_BA | PXW_CSY | PXW_HUN | PXW_HUNDC | PXW_PLK | PXW_SLOV | WIN_CZ | WIN_CZ_CI_AI
    | WIN1251 | WIN1251_UA | PXW_CYRL
    | WIN1252 | PXW_INTL | PXW_INTL850 | PXW_NORDAN4 | PXW_SPAN | PXW_SWEDFIN | WIN_PTBR
    | WIN1253 | PXW_GREEK
    | WIN1254 | PXW_TURK
    | WIN1255
    | WIN1256
    | WIN1257 | WIN1257_EE | WIN1257_LT | WIN1257_LV
    | WIN1258
    ;

attribute
    : attributeName EQ_ STRING_
    ;

attributeClause
    : attribute (COMMA_ attribute)*
    ;

bindLiterals
    : COLON_ identifier
    ;
