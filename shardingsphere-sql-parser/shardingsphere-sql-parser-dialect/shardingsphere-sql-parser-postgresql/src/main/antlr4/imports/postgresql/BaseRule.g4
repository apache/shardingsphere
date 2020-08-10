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

import Keyword, PostgreSQLKeyword, Symbol, Literals;

parameterMarker
    : QUESTION_ literalsType_?
    ;

reservedKeyword
    : ALL
    | ANALYSE
    | ANALYZE
    | AND
    | ANY
    | ARRAY
    | AS
    | ASC
    | ASYMMETRIC
    | BOTH
    | CASE
    | CAST
    | CHECK
    | COLLATE
    | COLUMN
    | CONSTRAINT
    | CREATE
    | CURRENT_CATALOG
    | CURRENT_DATE
    | CURRENT_ROLE
    | CURRENT_TIME
    | CURRENT_TIMESTAMP
    | CURRENT_USER
    | DEFAULT
    | DEFERRABLE
    | DESC
    | DISTINCT
    | DO
    | ELSE
    | END
    | EXCEPT
    | FALSE
    | FETCH
    | FOR
    | FOREIGN
    | FROM
    | GRANT
    | GROUP
    | HAVING
    | IN
    | INITIALLY
    | INTERSECT
    | INTO
    | LATERAL
    | LEADING
    | LIMIT
    | LOCALTIME
    | LOCALTIMESTAMP
    | NOT
    | NULL
    | OFFSET
    | ON
    | ONLY
    | OR
    | ORDER
    | PLACING
    | PRIMARY
    | REFERENCES
    | RETURNING
    | SELECT
    | SESSION_USER
    | SOME
    | SYMMETRIC
    | TABLE
    | THEN
    | TO
    | TRAILING
    | TRUE
    | UNION
    | UNIQUE
    | USER
    | USING
    | VARIADIC
    | WHEN
    | WHERE
    | WINDOW
    | WITH
    ;

numberLiterals
   : MINUS_? NUMBER_ literalsType_?
   ;

literalsType_
    : TYPE_CAST_ IDENTIFIER_
    ;

identifier
    : unicodeEscapes_? IDENTIFIER_ uescape_? |  unreservedWord 
    ;

unicodeEscapes_
    : ('U' | 'u') AMPERSAND_
    ;

uescape_
    : UESCAPE STRING_
    ;
    
unreservedWord
    : ABORT
    | ABSOLUTE
    | ACCESS
    | ACTION
    | ADD
    | ADMIN
    | AFTER
    | AGGREGATE
    | ALSO
    | ALTER
    | ALWAYS
    | ASSERTION
    | ASSIGNMENT
    | AT
    | ATTACH
    | ATTRIBUTE
    | BACKWARD
    | BEFORE
    | BEGIN
    | BY
    | CACHE
    | CALL
    | CALLED
    | CASCADE
    | CASCADED
    | CATALOG
    | CHAIN
    | CHARACTERISTICS
    | CHECKPOINT
    | CLASS
    | CLOSE
    | CLUSTER
    | COLUMNS
    | COMMENT
    | COMMENTS
    | COMMIT
    | COMMITTED
    | CONFIGURATION
    | CONFLICT
    | CONNECTION
    | CONSTRAINTS
    | CONTENT
    | CONTINUE
    | CONVERSION
    | COPY
    | COST
    | CSV
    | CUBE
    | CURRENT
    | CURSOR
    | CYCLE
    | DATA
    | DATABASE
    | DAY
    | DEALLOCATE
    | DECLARE
    | DEFAULTS
    | DEFERRED
    | DEFINER
    | DELETE
    | DELIMITER
    | DELIMITERS
    | DEPENDS
    | DETACH
    | DICTIONARY
    | DISABLE
    | DISCARD
    | DOCUMENT
    | DOMAIN
    | DOUBLE
    | DROP
    | EACH
    | ENABLE
    | ENCODING
    | ENCRYPTED
    | ENUM
    | ESCAPE
    | EVENT
    | EXCLUDE
    | EXCLUDING
    | EXCLUSIVE
    | EXECUTE
    | EXPLAIN
    | EXPRESSION
    | EXTENSION
    | EXTERNAL
    | FAMILY
    | FILTER
    | FIRST
    | FOLLOWING
    | FORCE
    | FORWARD
    | FUNCTION
    | FUNCTIONS
    | GENERATED
    | GLOBAL
    | GRANTED
    | GROUPS
    | HANDLER
    | HEADER
    | HOLD
    | HOUR
    | IDENTITY
    | IF
    | IMMEDIATE
    | IMMUTABLE
    | IMPLICIT
    | IMPORT
    | INCLUDE
    | INCLUDING
    | INCREMENT
    | INDEX
    | INDEXES
    | INHERIT
    | INHERITS
    | INLINE
    | INPUT
    | INSENSITIVE
    | INSERT
    | INSTEAD
    | INVOKER
    | ISOLATION
    | KEY
    | LABEL
    | LANGUAGE
    | LARGE
    | LAST
    | LEAKPROOF
    | LEVEL
    | LISTEN
    | LOAD
    | LOCAL
    | LOCATION
    | LOCK
    | LOCKED
    | LOGGED
    | MAPPING
    | MATCH
    | MATERIALIZED
    | MAXVALUE
    | METHOD
    | MINUTE
    | MINVALUE
    | MODE
    | MONTH
    | MOVE
    | NAME
    | NAMES
    | NEW
    | NEXT
    | NFC
    | NFD
    | NFKC
    | NFKD
    | NO
    | NORMALIZED
    | NOTHING
    | NOTIFY
    | NOWAIT
    | NULLS
    | OBJECT
    | OF
    | OFF
    | OIDS
    | OLD
    | OPERATOR
    | OPTION
    | OPTIONS
    | ORDINALITY
    | OTHERS
    | OVER
    | OVERRIDING
    | OWNED
    | OWNER
    | PARALLEL
    | PARSER
    | PARTIAL
    | PARTITION
    | PASSING
    | PASSWORD
    | PLANS
    | POLICY
    | PRECEDING
    | PREPARE
    | PREPARED
    | PRESERVE
    | PRIOR
    | PRIVILEGES
    | PROCEDURAL
    | PROCEDURE
    | PROCEDURES
    | PROGRAM
    | PUBLICATION
    | QUOTE
    | RANGE
    | READ
    | REASSIGN
    | RECHECK
    | RECURSIVE
    | REF
    | REFERENCING
    | REFRESH
    | REINDEX
    | RELATIVE
    | RELEASE
    | RENAME
    | REPEATABLE
    | REPLACE
    | REPLICA
    | RESET
    | RESTART
    | RESTRICT
    | RETURNS
    | REVOKE
    | ROLE
    | ROLLBACK
    | ROLLUP
    | ROUTINE
    | ROUTINES
    | ROWS
    | RULE
    | SAVEPOINT
    | SCHEMA
    | SCHEMAS
    | SCROLL
    | SEARCH
    | SECOND
    | SECURITY
    | SEQUENCE
    | SEQUENCES
    | SERIALIZABLE
    | SERVER
    | SESSION
    | SET
    | SETS
    | SHARE
    | SHOW
    | SIMPLE
//    | SKIP
    | SNAPSHOT
    | SQL
    | STABLE
    | STANDALONE
    | START
    | STATEMENT
    | STATISTICS
    | STDIN
    | STDOUT
    | STORAGE
    | STORED
    | STRICT
    | STRIP
    | SUBSCRIPTION
    | SUPPORT
    | SYSID
    | SYSTEM
    | TABLES
    | TABLESPACE
    | TEMP
    | TEMPLATE
    | TEMPORARY
    | TEXT
    | TIES
    | TRANSACTION
    | TRANSFORM
    | TRIGGER
    | TRUNCATE
    | TRUSTED
    | TYPE
    | TYPES
    | UESCAPE
    | UNBOUNDED
    | UNCOMMITTED
    | UNENCRYPTED
    | UNKNOWN
    | UNLISTEN
    | UNLOGGED
    | UNTIL
    | UPDATE
    | VACUUM
    | VALID
    | VALIDATE
    | VALIDATOR
    | VALUE
    | VARYING
    | VERSION
    | VIEW
    | VIEWS
    | VOLATILE
    | WHITESPACE
    | WITHIN
    | WITHOUT
    | WORK
    | WRAPPER
    | WRITE
    | XML
    | YEAR
    | YES
    | ZONE
    ;

typeFuncNameKeyword
    : AUTHORIZATION
    | BINARY
    | COLLATION
    | CONCURRENTLY
    | CROSS
    | CURRENT_SCHEMA
    | FREEZE
    | FULL
    | ILIKE
    | INNER
    | IS
    | ISNULL
    | JOIN
    | LEFT
    | LIKE
    | NATURAL
    | NOTNULL
    | OUTER
    | OVERLAPS
    | RIGHT
    | SIMILAR
    | TABLESAMPLE
    | VERBOSE
    ;

schemaName
    : identifier
    ;

tableName
    : (owner DOT_)? name
    ;

columnName
    : (owner DOT_)? name
    ;

owner
    : identifier
    ;

name
    : identifier
    ;

tableNames
    : LP_? tableName (COMMA_ tableName)* RP_?
    ;

columnNames
    : LP_ columnName (COMMA_ columnName)* RP_
    ;

collationName
    : STRING_ | identifier
    ;

indexName
    : identifier
    ;

alias
    : identifier
    ;

primaryKey
    : PRIMARY? KEY
    ;

logicalOperator
    : OR | OR_ | AND | AND_
    ;

comparisonOperator
    : EQ_ | GTE_ | GT_ | LTE_ | LT_ | NEQ_
    ;


cursorName
    : name
    ;

aExpr
    : cExpr
    | aExpr TYPE_CAST_ typeName
    | aExpr COLLATE anyName
    | aExpr AT TIME ZONE aExpr
    | PLUS_ aExpr
    | MINUS_ aExpr
    | aExpr PLUS_ aExpr
    | aExpr MINUS_ aExpr
    | aExpr ASTERISK_ aExpr
    | aExpr SLASH_ aExpr
    | aExpr MOD_ aExpr
    | aExpr CARET_ aExpr
    | aExpr comparisonOperator aExpr
    | aExpr qualOp aExpr
    | qualOp aExpr
    | aExpr qualOp
    | NOT aExpr
    | aExpr LIKE aExpr
    | aExpr LIKE aExpr ESCAPE aExpr
    | aExpr NOT LIKE aExpr
    | aExpr NOT LIKE aExpr ESCAPE aExpr
    | aExpr ILIKE aExpr
    | aExpr ILIKE aExpr ESCAPE aExpr
    | aExpr NOT ILIKE aExpr
    | aExpr NOT ILIKE aExpr ESCAPE aExpr
    | aExpr SIMILAR TO aExpr
    | aExpr SIMILAR TO aExpr ESCAPE aExpr
    | aExpr NOT SIMILAR TO aExpr
    | aExpr NOT SIMILAR TO aExpr ESCAPE aExpr
    | aExpr IS NULL
    | aExpr ISNULL
    | aExpr IS NOT NULL
    | aExpr NOTNULL
    | row OVERLAPS row
    | aExpr IS TRUE
    | aExpr IS NOT TRUE
    | aExpr IS FALSE
    | aExpr IS NOT FALSE
    | aExpr IS UNKNOWN
    | aExpr IS NOT UNKNOWN
    | aExpr IS DISTINCT FROM aExpr
    | aExpr IS NOT DISTINCT FROM aExpr
    | aExpr IS OF LP_ typeList RP_
    | aExpr IS NOT OF LP_ typeList RP_
    | aExpr BETWEEN ASYMMETRIC? bExpr AND aExpr
    | aExpr NOT BETWEEN ASYMMETRIC? bExpr AND aExpr
    | aExpr BETWEEN SYMMETRIC bExpr AND aExpr
    | aExpr NOT BETWEEN SYMMETRIC bExpr AND aExpr
    | aExpr IN inExpr
    | aExpr NOT IN inExpr
    | aExpr subqueryOp subType selectWithParens
    | aExpr subqueryOp subType LP_ aExpr RP_
    | UNIQUE selectWithParens
    | aExpr IS DOCUMENT
    | aExpr IS NOT DOCUMENT
    | aExpr IS NORMALIZED
    | aExpr IS unicodeNormalForm NORMALIZED
    | aExpr IS NOT NORMALIZED
    | aExpr IS NOT unicodeNormalForm NORMALIZED
    | aExpr logicalOperator aExpr
    | DEFAULT
    ;

bExpr
    : cExpr
    | bExpr TYPE_CAST_ typeName
    | PLUS_ bExpr
    | MINUS_ bExpr
    | bExpr qualOp bExpr
    | qualOp bExpr
    | bExpr qualOp
    | bExpr IS DISTINCT FROM bExpr
    | bExpr IS NOT DISTINCT FROM bExpr
    | bExpr IS OF LP_ typeList RP_
    | bExpr IS NOT OF LP_ typeList RP_
    | bExpr IS DOCUMENT
    | bExpr IS NOT DOCUMENT
    ;

cExpr
    : parameterMarker
    | columnref
    | aexprConst
    | PARAM indirectionEl?
    | LP_ aExpr RP_ optIndirection
    | caseExpr
    | funcExpr
    | selectWithParens
    | selectWithParens indirection
    | EXISTS selectWithParens
    | ARRAY selectWithParens
    | ARRAY arrayExpr
    | explicitRow
    | implicitRow
    | GROUPING LP_ exprList RP_
    ;

indirection
    : indirectionEl
    | indirection indirectionEl
    ;

optIndirection
    : optIndirection indirectionEl |
    ;

indirectionEl
    : DOT_ attrName
    | DOT_ ASTERISK_
    | LBT_ aExpr RBT_
    | LBT_ sliceBound? COLON_ sliceBound? RBT_
    ;

sliceBound
    : aExpr
    ;

inExpr
    : selectWithParens | LP_ exprList RP_
    ;

caseExpr
    : CASE caseArg? whenClauseList caseDefault? END
    ;

whenClauseList
    : whenClause
    | whenClauseList whenClause
    ;

whenClause
    : WHEN aExpr THEN aExpr
    ;

caseDefault
    : ELSE aExpr
    ;

caseArg
    : aExpr
    ;

columnref
    : colId
    | colId indirection
    ;

qualOp
    : mathOperator
    | TILDE_TILDE_
    | NOT_TILDE_TILDE_
    | OPERATOR LP_ anyOperator RP_
    ;

subqueryOp
    : allOp
    | OPERATOR LP_ anyOperator RP_
    | LIKE
    | NOT LIKE
    | ILIKE
    | NOT ILIKE
    ;

allOp
    : mathOperator
    ;

mathOperator
    : PLUS_
    | MINUS_
    | ASTERISK_
    | SLASH_
    | MOD_
    | CARET_
    | LT_
    | GT_
    | EQ_
    | LTE_
    | GTE_
    | NEQ_
    ;

qualAllOp
    : allOp
    | OPERATOR LP_ anyOperator RP_
    ;

ascDesc
    : ASC | DESC
    ;

anyOperator
    : allOp | colId DOT_ anyOperator
    ;

frameClause
    : RANGE frameExtent windowExclusionClause?
    | ROWS frameExtent windowExclusionClause?
    | GROUPS frameExtent windowExclusionClause?
    ;

frameExtent
    : frameBound
    | BETWEEN frameBound AND frameBound
    ;

frameBound
    : UNBOUNDED PRECEDING
    | UNBOUNDED FOLLOWING
    | CURRENT ROW
    | aExpr PRECEDING
    | aExpr FOLLOWING
    ;

windowExclusionClause
    : EXCLUDE CURRENT ROW
    | EXCLUDE GROUP
    | EXCLUDE TIES
    | EXCLUDE NO OTHERS
    ;

row
    : ROW LP_ exprList RP_
    | ROW LP_ RP_
    | LP_ exprList COMMA_ aExpr RP_
    ;

explicitRow
    : ROW LP_ exprList RP_
    | ROW LP_ RP_
    ;

implicitRow
    : LP_ exprList COMMA_ aExpr RP_
	;

subType
    : ANY | SOME | ALL
    ;

arrayExpr
    : LBT_ exprList RBT_
    | LBT_ arrayExprList RBT_
    | LBT_ RBT_
    ;

arrayExprList
    : arrayExpr | arrayExprList COMMA_ arrayExpr
    ;

funcArgList
    : funcArgExpr
    | funcArgList COMMA_ funcArgExpr
    ;

paramName
    : typeFunctionName
;

funcArgExpr
    : aExpr
    | paramName CQ_ aExpr
    | paramName GTE_ aExpr
    ;

typeList
    : typeName
    | typeList COMMA_ typeName
    ;

funcApplication
    : funcName LP_ RP_
    | funcName LP_ funcArgList sortClause? RP_
    | funcName LP_ VARIADIC funcArgExpr sortClause? RP_
    | funcName LP_ funcArgList COMMA_ VARIADIC funcArgExpr sortClause? RP_
    | funcName LP_ ALL funcArgList sortClause? RP_
    | funcName LP_ DISTINCT funcArgList sortClause? RP_
    | funcName LP_ ASTERISK_ RP_
    ;

funcName
    : typeFunctionName | colId indirection
    ;

aexprConst
    : NUMBER_
    | STRING_
    | funcName STRING_
    | funcName LP_ funcArgList sortClause? RP_ STRING_
    | TRUE
    | FALSE
    | NULL
    ;

qualifiedName
    : colId | colId indirection
    ;

colId
    : identifier
    ;

typeFunctionName
    : identifier | unreservedWord | typeFuncNameKeyword
    ;

functionTable
    : functionExprWindowless optOrdinality
    | ROWS FROM LP_ rowsFromList RP_ optOrdinality
    ;

xmlTable
    : XMLTABLE LP_ cExpr xmlExistsArgument COLUMNS xmlTableColumnList RP_
    | XMLTABLE LP_ XMLNAMESPACES LP_ xmlNamespaceList RP_ COMMA_ cExpr xmlExistsArgument COLUMNS xmlTableColumnList RP_
    ;

xmlTableColumnList
    : xmlTableColumnEl
    | xmlTableColumnList COMMA_ xmlTableColumnEl
    ;

xmlTableColumnEl
    : colId typeName
    | colId typeName xmlTableColumnOptionList
    | colId FOR ORDINALITY
    ;

xmlTableColumnOptionList
    : xmlTableColumnOptionEl
    | xmlTableColumnOptionList xmlTableColumnOptionEl
    ;

xmlTableColumnOptionEl
    : identifier bExpr
    | DEFAULT bExpr
    | NOT NULL
    | NULL
    ;

xmlNamespaceList
    : xmlNamespaceEl
    | xmlNamespaceList COMMA_ xmlNamespaceEl
    ;

xmlNamespaceEl
    : bExpr AS identifier
    | DEFAULT bExpr
    ;

funcExpr
    : funcApplication withinGroupClause? filterClause? overClause?
    | functionExprCommonSubexpr
    ;

withinGroupClause
    : WITHIN GROUP LP_ sortClause RP_
    ;

filterClause
    : FILTER LP_ WHERE aExpr RP_
    ;

functionExprWindowless
    : funcApplication | functionExprCommonSubexpr
    ;

optOrdinality
    : WITH ORDINALITY |
    ;

functionExprCommonSubexpr
    : COLLATION FOR LP_ aExpr RP_
    | CURRENT_DATE
    | CURRENT_TIME
    | CURRENT_TIME LP_ NUMBER_ RP_
    | CURRENT_TIMESTAMP
    | CURRENT_TIMESTAMP LP_ NUMBER_ RP_
    | LOCALTIME
    | LOCALTIME LP_ NUMBER_ RP_
    | LOCALTIMESTAMP
    | LOCALTIMESTAMP LP_ NUMBER_ RP_
    | CURRENT_ROLE
    | CURRENT_USER
    | SESSION_USER
    | USER
    | CURRENT_CATALOG
    | CURRENT_SCHEMA
    | CAST LP_ aExpr AS typeName RP_
    | EXTRACT LP_ extractList? RP_
    | NORMALIZE LP_ aExpr RP_
    | NORMALIZE LP_ aExpr COMMA_ unicodeNormalForm RP_
    | OVERLAY LP_ overlayList RP_
    | POSITION LP_ positionList RP_
    | SUBSTRING LP_ substrList RP_
    | TREAT LP_ aExpr AS typeName RP_
    | TRIM LP_ BOTH trimList RP_
    | TRIM LP_ LEADING trimList RP_
    | TRIM LP_ TRAILING trimList RP_
    | TRIM LP_ trimList RP_
    | NULLIF LP_ aExpr COMMA_ aExpr RP_
    | COALESCE LP_ exprList RP_
    | GREATEST LP_ exprList RP_
    | LEAST LP_ exprList RP_
    | XMLCONCAT LP_ exprList RP_
    | XMLELEMENT LP_ NAME identifier RP_
    | XMLELEMENT LP_ NAME identifier COMMA_ xmlAttributes RP_
    | XMLELEMENT LP_ NAME identifier COMMA_ exprList RP_
    | XMLELEMENT LP_ NAME identifier COMMA_ xmlAttributes COMMA_ exprList RP_
    | XMLEXISTS LP_ cExpr xmlExistsArgument RP_
    | XMLFOREST LP_ xmlAttributeList RP_
    | XMLPARSE LP_ documentOrContent aExpr xmlWhitespaceOption RP_
    | XMLPI LP_ NAME identifier RP_
    | XMLPI LP_ NAME identifier COMMA_ aExpr RP_
    | XMLROOT LP_ aExpr COMMA_ xmlRootVersion optXmlRootStandalone RP_
    | XMLSERIALIZE LP_ documentOrContent aExpr AS simpleTypeName RP_
    ;

typeName
    : simpleTypeName optArrayBounds
    | SETOF simpleTypeName optArrayBounds
    | simpleTypeName ARRAY LBT_ NUMBER_ RBT_
    | SETOF simpleTypeName ARRAY LBT_ NUMBER_ RBT_
    | simpleTypeName ARRAY
    | SETOF simpleTypeName ARRAY
    ;

simpleTypeName
	: genericType
	| numeric
	| bit
	| character
	| constDatetime
	| constInterval optInterval
	| constInterval LP_ NUMBER_ RP_
	;

exprList
    : aExpr
    | exprList COMMA_ aExpr
    ;

extractList
    : extractArg FROM aExpr
    ;

extractArg
    : YEAR
    | MONTH
    | DAY
    | HOUR
    | MINUTE
    | SECOND
    | identifier
    ;

genericType
    :    typeFunctionName typeModifiers? | typeFunctionName attrs typeModifiers?
    ;

typeModifiers
    : LP_ exprList RP_
    ;

numeric
    : INT | INTEGER | SMALLINT | BIGINT| REAL | FLOAT optFloat | DOUBLE PRECISION | DECIMAL typeModifiers? | DEC typeModifiers? | NUMERIC typeModifiers? | BOOLEAN
	;

constDatetime
    : TIMESTAMP LP_ NUMBER_ RP_ optTimezone
    | TIMESTAMP optTimezone
    | TIME LP_ NUMBER_ RP_ optTimezone
    | TIME optTimezone
    ;

optTimezone
    : WITH TIME ZONE
    | WITHOUT TIME ZONE
    |
    ;

character
    : characterWithLength | characterWithoutLength
    ;

characterWithLength
    : characterClause LP_ NUMBER_ RP_
    ;

characterWithoutLength
    : characterClause
    ;

characterClause
    : CHARACTER VARYING?
    | CHAR VARYING?
    | VARCHAR
    | NATIONAL CHARACTER VARYING?
    | NATIONAL CHAR VARYING?
    | NCHAR VARYING?
	;

optFloat
    : LP_ NUMBER_ RP_ |
    ;

attrs
    : DOT_ attrName | attrs DOT_ attrName
    ;

attrName
    : colLable
    ;

colLable
    : identifier
    ;

bit
    : bitWithLength | bitWithoutLength
    ;

bitWithLength
    : BIT VARYING? LP_ exprList RP_
    ;

bitWithoutLength
    : BIT VARYING?
    ;

constInterval
    : INTERVAL
    ;

optInterval
    : YEAR
    | MONTH
    | DAY
    | HOUR
    | MINUTE
    | intervalSecond
    | YEAR TO MONTH
    | DAY TO HOUR
    | DAY TO MINUTE
    | DAY TO intervalSecond
    | HOUR TO MINUTE
    | HOUR TO intervalSecond
    | MINUTE TO intervalSecond
    |
    ;

optArrayBounds
    : optArrayBounds LBT_ RBT_
    | optArrayBounds LBT_ NUMBER_ RBT_
    |
    ;

intervalSecond
    : SECOND
    | SECOND LP_ NUMBER_ RP_
    ;

unicodeNormalForm
    : NFC | NFD | NFKC | NFKD
    ;

trimList
    : aExpr FROM exprList
    | FROM exprList
    | exprList
    ;

overlayList
    : aExpr overlayPlacing substrFrom substrFor
    | aExpr overlayPlacing substrFrom
    ;

overlayPlacing
    : PLACING aExpr
    ;

substrFrom
    : FROM aExpr
    ;

substrFor
    : FOR aExpr
    ;

positionList
    : bExpr IN bExpr |
    ;

substrList
    : aExpr substrFrom substrFor
    | aExpr substrFor substrFrom
    | aExpr substrFrom
    | aExpr substrFor
    | exprList
    |
    ;

xmlAttributes
    : XMLATTRIBUTES LP_ xmlAttributeList RP_
    ;

xmlAttributeList
    : xmlAttributeEl
    | xmlAttributeList COMMA_ xmlAttributeEl
    ;

xmlAttributeEl
    : aExpr AS identifier | aExpr
    ;

xmlExistsArgument
    : PASSING cExpr
    | PASSING cExpr xmlPassingMech
    | PASSING xmlPassingMech cExpr
    | PASSING xmlPassingMech cExpr xmlPassingMech
    ;

xmlPassingMech
    : BY REF | BY VALUE
    ;

documentOrContent
    : DOCUMENT | CONTENT
    ;

xmlWhitespaceOption
    : PRESERVE WHITESPACE | STRIP WHITESPACE |
    ;

xmlRootVersion
    : VERSION aExpr
    | VERSION NO VALUE
    ;

optXmlRootStandalone
    : COMMA_ STANDALONE YES
    | COMMA_ STANDALONE NO
    | COMMA_ STANDALONE NO VALUE
    |
    ;

rowsFromItem
    : functionExprWindowless optColumnDefList
    ;

rowsFromList
    : rowsFromItem
	| rowsFromList COMMA_ rowsFromItem
    ;

optColumnDefList
    : AS LP_ tableFuncElementList RP_
    ;

tableFuncElementList
    : tableFuncElement
    | tableFuncElementList COMMA_ tableFuncElement
    ;

tableFuncElement
    :  typeName optCollateClause
    ;

optCollateClause
    : COLLATE anyName
    ;

anyName
    : colId | colId attrs
    ;

aliasClause
    : AS colId LP_ nameList RP_
    | AS colId
    | colId LP_ nameList RP_
    | colId
    ;

nameList
    : name | nameList COMMA_ name
    ;

funcAliasClause
    : aliasClause
    | AS LP_ tableFuncElementList RP_
    | AS colId LP_ tableFuncElementList RP_
    | colId LP_ tableFuncElementList RP_
    ;

tablesampleClause
    : TABLESAMPLE funcName LP_ exprList RP_ repeatableClause?
    ;

repeatableClause
    : REPEATABLE LP_ aExpr RP_
    ;

allOrDistinct
    : ALL | DISTINCT
    ;

sortClause
    : ORDER BY sortbyList
    ;

sortbyList
    : sortby (COMMA_ sortby)*
    ;

sortby
    : aExpr USING qualAllOp nullsOrder?
    | aExpr ascDesc? nullsOrder?
    ;

nullsOrder
    : NULLS FIRST
    | NULLS LAST
    ;

distinctClause
    : DISTINCT
    | DISTINCT ON LP_ exprList RP_
    ;

distinct
    : DISTINCT
    ;

overClause
    : OVER windowSpecification
    | OVER colId
    ;

windowSpecification
    : LP_ windowName? partitionClause? sortClause? frameClause? RP_
    ;

windowName
    : colId
    ;

partitionClause
    : PARTITION BY exprList
    ;

indexParams
    : indexElem
    | indexParams COMMA_ indexElem
    ;

indexElemOptions
    : collate? optClass ascDesc? nullsOrder?
    | collate? anyName reloptions ascDesc? nullsOrder?
    ;

indexElem
    : colId indexElemOptions
    | functionExprWindowless indexElemOptions
    | LP_ aExpr RP_ indexElemOptions
    ;

collate
    : COLLATE anyName
    ;

optClass
    : anyName |
    ;

reloptions
    : LP_ reloptionList RP_
    ;

optReloptions
    : WITH reloptions |
    ;

reloptionList
    : reloptionElem
    | reloptionList COMMA_ reloptionElem
	;

reloptionElem
    : alias EQ_ defArg
    | alias
    | alias DOT_ alias EQ_ defArg
    | alias DOT_ alias
	;

defArg
    : funcType
    | reservedKeyword
    | qualAllOp
    | NUMBER_
    | STRING_
    | NONE
    ;

funcType
    : typeName
    | typeFunctionName attrs MOD_ TYPE
    | SETOF typeFunctionName attrs MOD_ TYPE
    ;

selectWithParens
    : 'Default does not match anything'
    ;

dataType
    : dataTypeName dataTypeLength? characterSet_? collateClause_? | dataTypeName LP_ STRING_ (COMMA_ STRING_)* RP_ characterSet_? collateClause_?
    ;

dataTypeName
    : INT | INT2 | INT4 | INT8 | SMALLINT | INTEGER | BIGINT | DECIMAL | NUMERIC | REAL | FLOAT | FLOAT4 | FLOAT8 | DOUBLE PRECISION | SMALLSERIAL | SERIAL | BIGSERIAL
    | MONEY | VARCHAR | CHARACTER | CHAR | TEXT | NAME | BYTEA | TIMESTAMP | DATE | TIME | INTERVAL | BOOLEAN | ENUM | POINT
    | LINE | LSEG | BOX | PATH | POLYGON | CIRCLE | CIDR | INET | MACADDR | MACADDR8 | BIT | VARBIT | TSVECTOR | TSQUERY | UUID | XML
    | JSON | INT4RANGE | INT8RANGE | NUMRANGE | TSRANGE | TSTZRANGE | DATERANGE | ARRAY | identifier
    ;

dataTypeLength
    : LP_ NUMBER_ (COMMA_ NUMBER_)? RP_
    ;

characterSet_
    : (CHARACTER | CHAR) SET EQ_? ignoredIdentifier_
    ;

collateClause_
    : COLLATE EQ_? (STRING_ | ignoredIdentifier_)
    ;

ignoredIdentifier_
    : identifier (DOT_ identifier)?
    ;

ignoredIdentifiers_
    : ignoredIdentifier_ (COMMA_ ignoredIdentifier_)*
    ;
