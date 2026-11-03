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

parser grammar BaseRule;

options {
    tokenVocab = ModeLexer;
}

parameterMarker
    : QUESTION_ literalsType?
    | DOLLAR_ NUMBER_ {$NUMBER_.text.matches("[0-9]+") }?
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
   : MINUS_? NUMBER_ literalsType?
   ;

literalsType
    : TYPE_CAST_ IDENTIFIER_
    ;

identifier
    : UNICODE_ESCAPE? IDENTIFIER_ uescape? | unreservedWord | jsonFunctionKeyword
    ;

uescape
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
    | ATOMIC
    | ATTRIBUTE
    | BACKWARD
    | BEFORE
    | BEGIN
    | BY
    | BYTEA
    | BOX
    | SMALLSERIAL
    | SERIAL
    | BIGSERIAL
    | CIDR
    | DATE
    | FLOAT4
    | FLOAT8
    | FORMAT
    | INET
    | INT2
    | INT4
    | INT8
    | LINE
    | MACADDR
    | MACADDR8
    | MICROSECOND
    | TSQUERY
    | TSVECTOR
    | VARBIT
    | CACHE
    | CALL
    | CALLED
    | CASCADE
    | CASCADED
    | CATALOG
    | CHAIN
    | CHARACTERISTICS
    | CHECKPOINT
    | CIDR
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
    | CONNECT
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
    | CIRCLE
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
    | EXTENDED
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
    | INET
    | INHERIT
    | INHERITS
    | INLINE
    | INPUT
    | INSENSITIVE
    | INSERT
    | INSTEAD
    | INVOKER
    | INTERVAL
    | ISOLATION
    | KEY
    | LABEL
    | LANGUAGE
    | LARGE
    | LAST
    | LEAKPROOF
    | LEVEL
    | LINE
    | LISTEN
    | LOAD
    | LOCAL
    | LOCATION
    | LOCK
    | LOCKED
    | LOGGED
    | LOGIN
    | LSEG
    | MACADDR
    | MACADDR8
    | MAIN
    | MAINTAIN
    | MAPPING
    | MATCH
    | MATCHED
    | MATERIALIZED
    | MAXVALUE
    | MERGE
    | METHOD
    | MINUTE
    | MINVALUE
    | MODE
    | MONTH
    | MOVE
    | MOD
    | NAME
    | NAMES
    | NATIONAL
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
    | OBJECTS
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
    | PATH
    | PLAIN
    | PLANS
    | POLICY
    | POINT
    | POLYGON
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
    | QUARTER
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
    | RETURN
    | RESTART
    | RESTRICT
    | RETURN
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
    | SKIP_SYMBOL
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
    | TSQUERY
    | TSVECTOR
    | TYPE
    | TYPES
    | TIME
    | TIMESTAMP
    | UESCAPE
    | UNBOUNDED
    | UNCOMMITTED
    | UNENCRYPTED
    | UNKNOWN
    | UNLISTEN
    | UNLOGGED
    | UNTIL
    | UPDATE
    | USAGE
    | VACUUM
    | VALID
    | VALIDATE
    | VALIDATOR
    | VALUE
    | VARBIT
    | VARYING
    | VERSION
    | VIEW
    | VIEWS
    | VOLATILE
    | WEEK
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
    | JSON
    | ABSENT
    | ATOMIC
    | BREADTH
    | CONDITIONAL
    | DEPTH
    | EMPTY
    | ENFORCED
    | ERROR
    | IGNORE
    | INDENT
    | KEEP
    | KEYS
    | LOG_VERBOSITY
    | NESTED
    | OMIT
    | ON_ERROR
    | PERIOD
    | QUOTES
    | REJECT_LIMIT
    | SCALAR
    | SILENT
    | SOURCE
    | STOP
    | STRING
    | TARGET
    | UNCONDITIONAL
    | UTF8
    | VIRTUAL
    | PARAM
    | TABLE
    | CONSTRAINT
    | COLLATION
    | USAGE
    | SOURCE
    | TARGET
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
    : (owner DOT_)? identifier
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

constraintName
    : identifier
    ;

alias
    : identifier
    ;

primaryKey
    : PRIMARY? KEY
    ;

andOperator
    : AND
    ;

orOperator
    : OR | OR_
    ;

comparisonOperator
    : EQ_ | GTE_ | GT_ | LTE_ | LT_ | NEQ_
    ;

patternMatchingOperator
    : LIKE
    | NOT LIKE
    | ILIKE
    | NOT ILIKE
    | SIMILAR TO
    | NOT SIMILAR TO
    | patternMatchingSymbol
    ;

patternMatchingSymbol
    : TILDE_
    | TILDE_TILDE_
    | NOT_TILDE_TILDE_
    | ILIKE_
    | NOT_ILIKE_
    | REGEX_NOT_MATCH_
    | REGEX_MATCH_CASE_INSENSITIVE_
    | REGEX_NOT_MATCH_CASE_INSENSITIVE_
    ;

cursorName
    : name | hostVariable
    ;

aExpr
    : cExpr
    | aExpr TYPE_CAST_ typeName
    | aExpr COLLATE anyName
    | aExpr AT TIME ZONE aExpr
    | aExpr AT LOCAL
    | PLUS_ aExpr
    | MINUS_ aExpr
    | aExpr PLUS_ aExpr
    | aExpr MINUS_ aExpr
    | aExpr ASTERISK_ aExpr
    | aExpr SLASH_ aExpr
    | aExpr MOD_ aExpr
    | aExpr CARET_ aExpr
    | TILDE_ aExpr
    | {QUESTION_ != _input.LA(1)}? qualOp aExpr
    | aExpr qualOp aExpr
    | aExpr qualOp
    | aExpr comparisonOperator aExpr
    | NOT aExpr
    | aExpr patternMatchingOperator aExpr ESCAPE aExpr
    | aExpr patternMatchingOperator aExpr
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
    | aExpr IS NOT? JSON jsonValueType? jsonUniqueKeys?
    | aExpr IS NORMALIZED
    | aExpr IS unicodeNormalForm NORMALIZED
    | aExpr IS NOT NORMALIZED
    | aExpr IS NOT unicodeNormalForm NORMALIZED
    | aExpr andOperator aExpr
    | aExpr orOperator aExpr
    | DEFAULT
    ;

bExpr
    : cExpr
    | bExpr TYPE_CAST_ typeName
    | PLUS_ bExpr
    | MINUS_ bExpr
    | bExpr CARET_ bExpr
    | bExpr (ASTERISK_ | SLASH_ | MOD_) bExpr
    | bExpr (PLUS_ | MINUS_) bExpr
    | TILDE_ bExpr
    | {QUESTION_ != _input.LA(1)}? qualOp bExpr
    | bExpr qualOp bExpr
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
    | DOT_ASTERISK_
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
    : whenClause+
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
    : jsonOperator
    | geometricOperator
    | patternMatchingSymbol
    | AND_
    | AMPERSAND_
    | VERTICAL_BAR_
    | CUSTOM_OPERATOR_
    | NOT_
    | AT_
    | DEQ_
    | SAFE_EQ_
    | BQ_
    | OPERATOR LP_ anyOperator RP_
    ;

subqueryOp
    : allOp
    | OPERATOR LP_ anyOperator RP_
    | LIKE
    | NOT LIKE
    | ILIKE
    | NOT ILIKE
    | TILDE_
    | REGEX_NOT_MATCH_
    | REGEX_MATCH_CASE_INSENSITIVE_
    | REGEX_NOT_MATCH_CASE_INSENSITIVE_
    ;

allOp
    : op | mathOperator
    ;

op
    : (AND_
    | OR_
    | NOT_
    | TILDE_
    | VERTICAL_BAR_
    | AMPERSAND_
    | SIGNED_LEFT_SHIFT_
    | SIGNED_RIGHT_SHIFT_
    | CARET_
    | MOD_
    | COLON_
    | PLUS_
    | MINUS_
    | ASTERISK_
    | SLASH_
    | BACKSLASH_
    | DOT_
    | DOT_ASTERISK_
    | SAFE_EQ_
    | DEQ_
    | EQ_
    | CQ_
    | NEQ_
    | GT_
    | GTE_
    | LT_
    | LTE_
    | POUND_
    | LP_
    | RP_
    | LBE_
    | RBE_
    | LBT_
    | RBT_
    | COMMA_
    | DQ_
    | SQ_
    | BQ_
    | QUESTION_
    | DOLLAR_
    | AT_
    | SEMI_
    | TILDE_TILDE_
    | NOT_TILDE_TILDE_
    | TYPE_CAST_
    | ILIKE_
    | NOT_ILIKE_
    | REGEX_NOT_MATCH_
    | REGEX_MATCH_CASE_INSENSITIVE_
    | REGEX_NOT_MATCH_CASE_INSENSITIVE_
    | UNICODE_ESCAPE
    | JSON_EXTRACT_
    | JSON_EXTRACT_TEXT_
    | JSON_PATH_EXTRACT_
    | JSON_PATH_EXTRACT_TEXT_
    | JSONB_CONTAIN_RIGHT_
    | JSONB_CONTAIN_LEFT_
    | JSONB_CONTAIN_ALL_TOP_KEY_
    | JSONB_PATH_DELETE_
    | JSONB_PATH_CONTAIN_ANY_VALUE_
    | JSONB_PATH_PREDICATE_CHECK_
    | GEOMETRIC_LENGTH_
    | GEOMETRIC_DISTANCE_
    | GEOMETRIC_EXTEND_RIGHT_
    | GEOMETRIC_EXTEND_LEFT_
    | GEOMETRIC_STRICT_BELOW_
    | GEOMETRIC_STRICT_ABOVE_
    | GEOMETRIC_EXTEND_ABOVE_
    | GEOMETRIC_EXTEND_BELOW_
    | GEOMETRIC_BELOW_
    | GEOMETRIC_ABOVE_
    | GEOMETRIC_INTERSECT_
    | GEOMETRIC_PERPENDICULAR_
    | GEOMETRIC_SAME_AS_ )+
    | CUSTOM_OPERATOR_
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

jsonOperator
    : JSON_EXTRACT_ # jsonExtract
    | JSON_EXTRACT_TEXT_ # jsonExtractText
    | JSON_PATH_EXTRACT_ # jsonPathExtract
    | JSON_PATH_EXTRACT_TEXT_ # jsonPathExtractText
    | JSONB_CONTAIN_RIGHT_ # jsonbContainRight
    | JSONB_CONTAIN_LEFT_ # jsonbContainLeft
    | QUESTION_ # jsonbContainTopKey
    | QUESTION_ VERTICAL_BAR_ # jsonbContainAnyTopKey
    | JSONB_CONTAIN_ALL_TOP_KEY_ # jsonbContainAllTopKey
    | OR_ # jsonbConcat
    | MINUS_ # jsonbDelete
    | JSONB_PATH_DELETE_ # jsonbPathDelete
    | JSONB_PATH_CONTAIN_ANY_VALUE_ # jsonbPathContainAnyValue
    | JSONB_PATH_PREDICATE_CHECK_ # jsonbPathPredicateCheck
    ;

geometricOperator
    : GEOMETRIC_LENGTH_
    | GEOMETRIC_DISTANCE_
    | GEOMETRIC_EXTEND_RIGHT_
    | GEOMETRIC_EXTEND_LEFT_
    | GEOMETRIC_STRICT_BELOW_
    | GEOMETRIC_STRICT_ABOVE_
    | GEOMETRIC_EXTEND_ABOVE_
    | GEOMETRIC_EXTEND_BELOW_
    | GEOMETRIC_BELOW_
    | GEOMETRIC_ABOVE_
    | GEOMETRIC_INTERSECT_
    | GEOMETRIC_PERPENDICULAR_
    | GEOMETRIC_SAME_AS_
    | QUESTION_ MINUS_
    | QUESTION_ OR_
    | POUND_
    | SIGNED_LEFT_SHIFT_
    | SIGNED_RIGHT_SHIFT_
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
    : (RANGE|ROWS|GROUPS) frameExtent windowExclusionClause?
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
    : arrayExpr (COMMA_ arrayExpr)*
    ;

funcArgList
    : funcArgExpr (COMMA_ funcArgExpr)*
    ;

paramName
    : typeFunctionName
    ;

funcArgExpr
    : aExpr
    | paramName CQ_ aExpr
    | paramName NAMED_ARGUMENT_ aExpr
    ;

typeList
    : typeName (COMMA_ typeName)*
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
    : (owner DOT_)? typeFunctionName
    | colId indirection
    ;

aexprConst
    : numberConst
    | STRING_
    | UNICODE_ESCAPE STRING_ uescape?
    | BIT_NUM_
    | HEX_DIGIT_
    | BEGIN_DOLLAR_STRING_CONSTANT DOLLAR_TEXT* END_DOLLAR_STRING_CONSTANT
    | INTERVAL STRING_ optInterval
    | INTERVAL LP_ NUMBER_ RP_ STRING_ optInterval
    | funcName STRING_
    | funcName LP_ funcArgList sortClause? RP_ STRING_
    | constTypeName STRING_
    | TRUE
    | FALSE
    | NULL
    ;

numberConst
    : (PLUS_ | MINUS_)? (NUMBER_ | HEX_NUMBER_ | BINARY_NUMBER_ | OCTAL_NUMBER_)
    ;

qualifiedName
    : colId | colId indirection
    ;

colId
    : identifier | colNameKeyword
    ;

channelName
    : identifier
    ;

typeFunctionName
    : UNICODE_ESCAPE? IDENTIFIER_ uescape?
    | unreservedWord
    | typeFuncNameKeyword
    | INT4RANGE
    | INT8RANGE
    | NUMRANGE
    | TSRANGE
    | TSTZRANGE
    | DATERANGE
    ;

functionTable
    : functionExprWindowless ordinality?
    | ROWS FROM LP_ rowsFromList RP_ ordinality?
    ;

xmlTable
    : XMLTABLE LP_ cExpr xmlExistsArgument COLUMNS xmlTableColumnList RP_
    | XMLTABLE LP_ XMLNAMESPACES LP_ xmlNamespaceList RP_ COMMA_ cExpr xmlExistsArgument COLUMNS xmlTableColumnList RP_
    ;

xmlTableColumnList
    : xmlTableColumnEl (COMMA_ xmlTableColumnEl)*
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
    : xmlNamespaceEl (COMMA_ xmlNamespaceEl)*
    ;

xmlNamespaceEl
    : bExpr AS identifier
    | DEFAULT bExpr
    ;

funcExpr
    : jsonAggregateFunction filterClause? overClause?
    | funcApplication withinGroupClause? filterClause? overClause?
    | functionExprCommonSubexpr
    ;

withinGroupClause
    : WITHIN GROUP LP_ sortClause RP_
    ;

filterClause
    : FILTER LP_ WHERE aExpr RP_
    ;

functionExprWindowless
    : funcApplication | functionExprCommonSubexpr | jsonAggregateFunction
    ;

ordinality
    : WITH ORDINALITY
    ;

functionExprCommonSubexpr
    : jsonFunction
    | COLLATION FOR LP_ aExpr RP_
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
    | XMLROOT LP_ aExpr COMMA_ xmlRootVersion xmlRootStandalone? RP_
    | XMLSERIALIZE LP_ documentOrContent aExpr AS simpleTypeName (NO? INDENT)? RP_
    ;

jsonValueType
    : VALUE | SCALAR | ARRAY | OBJECT
    ;

jsonFormat
    : FORMAT JSON (ENCODING UTF8)?
    ;

jsonValueExpr
    : aExpr jsonFormat?
    ;

jsonNullClause
    : (NULL | ABSENT) ON NULL
    ;

jsonUniqueKeys
    : (WITH | WITHOUT) UNIQUE KEYS?
    ;

jsonReturningClause
    : RETURNING typeName jsonFormat?
    ;

jsonNameAndValue
    : cExpr VALUE jsonValueExpr
    | aExpr COLON_ jsonValueExpr
    ;

jsonFunction
    : JSON_OBJECT LP_ funcArgList RP_
    | JSON_OBJECT LP_ jsonNameAndValue (COMMA_ jsonNameAndValue)* jsonNullClause? jsonUniqueKeys? jsonReturningClause? RP_
    | JSON_OBJECT LP_ jsonReturningClause? RP_
    | JSON_ARRAY LP_ jsonValueExpr (COMMA_ jsonValueExpr)* jsonNullClause? jsonReturningClause? RP_
    | JSON_ARRAY LP_ selectNoParens jsonFormat? jsonReturningClause? RP_
    | JSON_ARRAY LP_ jsonReturningClause? RP_
    | JSON LP_ jsonValueExpr jsonUniqueKeys? RP_
    | JSON_SCALAR LP_ aExpr RP_
    | JSON_SERIALIZE LP_ jsonValueExpr jsonReturningClause? RP_
    | JSON_EXISTS LP_ jsonValueExpr COMMA_ aExpr jsonPassingClause? jsonExistsErrorClause? RP_
    | JSON_VALUE LP_ jsonValueExpr COMMA_ aExpr jsonPassingClause? (RETURNING typeName)? jsonValueEmptyClause? jsonValueErrorClause? RP_
    | JSON_QUERY LP_ jsonValueExpr COMMA_ aExpr jsonPassingClause? jsonReturningClause? jsonWrapperClause? jsonQuotesClause? jsonQueryEmptyClause? jsonQueryErrorClause? RP_
    ;

jsonAggregateFunction
    : JSON_ARRAYAGG LP_ jsonValueExpr sortClause? jsonNullClause? jsonReturningClause? RP_
    | JSON_OBJECTAGG LP_ jsonNameAndValue jsonNullClause? jsonUniqueKeys? jsonReturningClause? RP_
    ;

jsonPassingClause
    : PASSING jsonPassingArgument (COMMA_ jsonPassingArgument)*
    ;

jsonPassingArgument
    : aExpr AS colId
    ;

jsonWrapperClause
    : WITHOUT ARRAY? WRAPPER
    | WITH (CONDITIONAL | UNCONDITIONAL)? ARRAY? WRAPPER
    ;

jsonQuotesClause
    : (KEEP | OMIT) QUOTES (ON SCALAR STRING)?
    ;

jsonValueBehavior
    : ERROR | NULL | DEFAULT aExpr
    ;

jsonQueryBehavior
    : jsonValueBehavior | EMPTY (ARRAY | OBJECT)?
    ;

jsonValueEmptyClause
    : jsonValueBehavior ON EMPTY
    ;

jsonValueErrorClause
    : jsonValueBehavior ON ERROR
    ;

jsonQueryEmptyClause
    : jsonQueryBehavior ON EMPTY
    ;

jsonQueryErrorClause
    : jsonQueryBehavior ON ERROR
    ;

jsonExistsErrorClause
    : (ERROR | TRUE | FALSE | UNKNOWN) ON ERROR
    ;

jsonTable
    : JSON_TABLE LP_ jsonValueExpr COMMA_ aExpr (AS colId)? jsonPassingClause? COLUMNS LP_ jsonTableColumn (COMMA_ jsonTableColumn)* RP_ jsonTableErrorClause? RP_
    ;

jsonTableColumn
    : colId FOR ORDINALITY
    | colId typeName jsonFormat? (PATH jsonTablePath)? jsonWrapperClause? jsonQuotesClause? jsonQueryEmptyClause? jsonQueryErrorClause?
    | colId typeName EXISTS (PATH jsonTablePath)? jsonExistsErrorClause?
    | NESTED PATH? jsonTablePath (AS colId)? COLUMNS LP_ jsonTableColumn (COMMA_ jsonTableColumn)* RP_
    ;

jsonTablePath
    : STRING_
    | UNICODE_ESCAPE STRING_ uescape?
    | BEGIN_DOLLAR_STRING_CONSTANT DOLLAR_TEXT* END_DOLLAR_STRING_CONSTANT
    ;

jsonTableErrorClause
    : (ERROR | EMPTY ARRAY?) ON ERROR
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

constTypeName
    : numeric
    | bit
    | character
    | constDatetime
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
    | QUARTER
    | MONTH
    | WEEK
    | DAY
    | HOUR
    | MINUTE
    | SECOND
    | identifier
    ;

genericType
    : typeFunctionName typeModifiers? | typeFunctionName attrs typeModifiers?
    ;

typeModifiers
    : LP_ exprList RP_
    ;

numeric
    : INT | INTEGER | SMALLINT | BIGINT| REAL | FLOAT optFloat | DOUBLE PRECISION | DECIMAL typeModifiers? | DEC typeModifiers? | NUMERIC typeModifiers? | BOOLEAN | FLOAT8 | FLOAT4 | INT2 | INT4 | INT8
    ;

constDatetime
    : TIMESTAMP LP_ NUMBER_ RP_ timezone?
    | TIMESTAMP timezone?
    | TIME LP_ NUMBER_ RP_ timezone?
    | TIME timezone?
    | DATE
    ;

timezone
    : WITH TIME ZONE
    | WITHOUT TIME ZONE
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
    | colNameKeyword
    | typeFuncNameKeyword
    | reservedKeyword
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
    | aExpr SIMILAR aExpr ESCAPE aExpr
    | exprList
    |
    ;

xmlAttributes
    : XMLATTRIBUTES LP_ xmlAttributeList RP_
    ;

xmlAttributeList
    : xmlAttributeEl (COMMA_ xmlAttributeEl)*
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

xmlRootStandalone
    : COMMA_ STANDALONE YES
    | COMMA_ STANDALONE NO
    | COMMA_ STANDALONE NO VALUE
    ;

rowsFromItem
    : functionExprWindowless columnDefList?
    ;

rowsFromList
    : rowsFromItem (COMMA_ rowsFromItem)*
    ;

columnDefList
    : AS LP_ tableFuncElementList RP_
    ;

tableFuncElementList
    : tableFuncElement (COMMA_ tableFuncElement)*
    ;

tableFuncElement
    : colId typeName collateClause?
    ;

collateClause
    : COLLATE EQ_? anyName
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
    : indexElem (COMMA_ indexElem)*
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

reloptionList
    : reloptionElem (COMMA_ reloptionElem)*
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
    | numericOnly
    | STRING_
    | NONE
    | funcName (LP_ funcArgsList RP_ | LP_ RP_)
    ;

funcType
    : typeName
    | typeFunctionName attrs MOD_ TYPE
    | SETOF typeFunctionName attrs MOD_ TYPE
    ;

selectWithParens
    : DEFAULT_DOES_NOT_MATCH_ANYTHING
    ;

dataType
    : dataTypeName dataTypeLength? characterSet? collateClause? | dataTypeName LP_ STRING_ (COMMA_ STRING_)* RP_ characterSet? collateClause?
    ;

dataTypeName
    : INT | INT2 | INT4 | INT8 | SMALLINT | INTEGER | BIGINT | DECIMAL | NUMERIC | REAL | FLOAT | FLOAT4 | FLOAT8 | DOUBLE PRECISION | SMALLSERIAL | SERIAL | BIGSERIAL
    | VARCHAR | CHARACTER | CHAR | TEXT | NAME | BYTEA | TIMESTAMP | DATE | TIME | INTERVAL | BOOLEAN | ENUM | POINT
    | LINE | LSEG | BOX | PATH | POLYGON | CIRCLE | CIDR | INET | MACADDR | MACADDR8 | BIT | VARBIT | TSVECTOR | TSQUERY | XML
    | JSON | INT4RANGE | INT8RANGE | NUMRANGE | TSRANGE | TSTZRANGE | DATERANGE | ARRAY | identifier | constDatetime | typeName
    ;

dataTypeLength
    : LP_ NUMBER_ (COMMA_ NUMBER_)? RP_
    ;

characterSet
    : (CHARACTER | CHAR) SET EQ_? ignoredIdentifier
    ;

ignoredIdentifier
    : identifier (DOT_ identifier)?
    ;

ignoredIdentifiers
    : ignoredIdentifier (COMMA_ ignoredIdentifier)*
    ;

signedIconst
    : NUMBER_
    | PLUS_ NUMBER_
    | MINUS_ NUMBER_
    ;

booleanOrString
    : TRUE
    | FALSE
    | ON
    | nonReservedWord
    | STRING_
    ;

nonReservedWord
    : identifier
    | unreservedWord
    | colNameKeyword
    | typeFuncNameKeyword
    ;

jsonFunctionKeyword
    : JSON_ARRAY
    | JSON_ARRAYAGG
    | JSON_EXISTS
    | JSON_OBJECT
    | JSON_OBJECTAGG
    | JSON_QUERY
    | JSON_SCALAR
    | JSON_SERIALIZE
    | JSON_TABLE
    | JSON_VALUE
    ;

colNameKeyword
    : BETWEEN
    | BIGINT
    | BIT
    | BOOLEAN
    | CHAR
    | CHARACTER
    | COALESCE
    | DEC
    | DECIMAL
    | EXISTS
    | EXTRACT
    | FLOAT
    | GREATEST
    | GROUPING
    | INOUT
    | INT
    | INTEGER
    | INTERVAL
    | jsonFunctionKeyword
    | LEAST
    | NATIONAL
    | NCHAR
    | NONE
    | NULLIF
    | NUMERIC
    | OUT
    | OVERLAY
    | POSITION
    | PRECISION
    | REAL
    | ROW
    | SETOF
    | SMALLINT
    | SUBSTRING
    | TIME
    | TIMESTAMP
    | TREAT
    | TRIM
    | VALUES
    | VARCHAR
    | XMLATTRIBUTES
    | XMLCONCAT
    | XMLELEMENT
    | XMLEXISTS
    | XMLFOREST
    | XMLNAMESPACES
    | XMLPARSE
    | XMLPI
    | XMLROOT
    | XMLSERIALIZE
    | XMLTABLE
    ;

databaseName
    : colId
    ;

roleSpec
    : identifier
    | nonReservedWord
    | CURRENT_USER
    | SESSION_USER
    | CURRENT_ROLE
    | PUBLIC
    ;

varName
    : colId
    | varName DOT_ colId
    ;

varList
    : varValue (COMMA_ varValue)*
    ;

varValue
    : booleanOrString | numericOnly
    ;

zoneValue
    : STRING_
    | identifier
    | INTERVAL STRING_ optInterval
    | INTERVAL LP_ NUMBER_ RP_ STRING_
    | numericOnly
    | DEFAULT
    | LOCAL
    ;

numericOnly
    : NUMBER_
    | PLUS_ NUMBER_
    | MINUS_ NUMBER_
    ;

isoLevel
    : READ UNCOMMITTED
    | READ COMMITTED
    | REPEATABLE READ
    | SERIALIZABLE
    ;

columnDef
    : colId typeName createGenericOptions? colQualList
    ;

colQualList
    : colConstraint*
    ;

colConstraint
    : CONSTRAINT name colConstraintElem
    | colConstraintElem
    | constraintAttr
    | COLLATE anyName
    ;

constraintAttr
    : DEFERRABLE
    | NOT DEFERRABLE
    | INITIALLY DEFERRED
    | INITIALLY IMMEDIATE
    ;

colConstraintElem
    : NOT NULL
    | NULL
    | UNIQUE (WITH definition)? consTableSpace
    | PRIMARY KEY (WITH definition)? consTableSpace
    | CHECK LP_ aExpr RP_ noInherit?
    | DEFAULT bExpr
    | GENERATED generatedWhen AS IDENTITY parenthesizedSeqOptList?
    | GENERATED generatedWhen AS LP_ aExpr RP_ STORED
    | REFERENCES qualifiedName optColumnList? keyMatch? keyActions?
    ;

parenthesizedSeqOptList
    : LP_ seqOptList RP_
    ;

seqOptList
    : seqOptElem+
    ;

seqOptElem
    : AS simpleTypeName
    | CACHE numericOnly
    | NO? CYCLE
    | INCREMENT BY? numericOnly
    | (MAXVALUE | MINVALUE) numericOnly
    | NO (MAXVALUE | MINVALUE)
    | OWNED BY (anyName | NONE)
    | SEQUENCE NAME anyName
    | START WITH? numericOnly
    | RESTART
    | RESTART WITH? numericOnly
    ;

optColumnList
    : LP_ columnList RP_
    ;

columnElem
    : colId
    ;

columnList
    : columnElem (COMMA_ columnElem)*
    ;

generatedWhen
    : ALWAYS
    | BY DEFAULT
    ;

noInherit
    : NO INHERIT
    ;

consTableSpace
    : USING INDEX TABLESPACE name
    ;

definition
    : LP_ defList RP_
    ;

defList
    : defElem (COMMA_ defElem)*
    ;

defElem
    : (colLabel EQ_ defArg) | colLabel
    ;

colLabel
    : identifier
    | unreservedWord
    | colNameKeyword
    | typeFuncNameKeyword
    | reservedKeyword
    ;

keyActions
    : keyUpdate
    | keyDelete
    | keyUpdate keyDelete
    | keyDelete keyUpdate
    ;

keyDelete
    : ON DELETE keyAction
    ;

keyUpdate
    : ON UPDATE keyAction
    ;

keyAction
    : NO ACTION
    | RESTRICT
    | CASCADE
    | SET NULL
    | SET DEFAULT
    ;

keyMatch
    : MATCH FULL | MATCH PARTIAL | MATCH SIMPLE
    ;

createGenericOptions
    : OPTIONS LP_ genericOptionList RP_
    ;

genericOptionList
    : genericOptionElem (COMMA_ genericOptionElem)*
    ;

genericOptionElem
    : genericOptionName genericOptionArg
    ;

genericOptionArg
    : STRING_
    ;

genericOptionName
    : colLable
    ;

replicaIdentity
    : NOTHING
    | FULL
    | DEFAULT
    | USING INDEX name
    ;

operArgtypes
    : LP_ (typeName | NONE) COMMA_ typeName RP_
    ;

funcArg
    : argClass paramName funcType
    | paramName argClass funcType
    | paramName funcType
    | argClass funcType
    | funcType
    ;

argClass
    : IN
    | OUT
    | INOUT
    | IN OUT
    | VARIADIC
    ;

funcArgsList
    : funcArg (COMMA_ funcArg)*
    ;

nonReservedWordOrSconst
    : nonReservedWord
    | STRING_
    ;

fileName
    : STRING_
    ;

roleList
    : roleSpec (COMMA_ roleSpec)*
    ;

setResetClause
    : SET setRest
    | variableResetStmt
    ;

setRest
    : TRANSACTION transactionModeList
    | SESSION CHARACTERISTICS AS TRANSACTION transactionModeList
    | setRestMore
    ;

transactionModeList
    : transactionModeItem (COMMA_? transactionModeItem)*
    ;

transactionModeItem
    : ISOLATION LEVEL isoLevel
    | READ ONLY
    | READ WRITE
    | DEFERRABLE
    | NOT DEFERRABLE
    ;

setRestMore
    : genericSet
    | varName FROM CURRENT
    | TIME ZONE zoneValue
    | CATALOG STRING_
    | SCHEMA STRING_
    | NAMES encoding?
    | ROLE nonReservedWord | STRING_
    | SESSION AUTHORIZATION nonReservedWord | STRING_
    | SESSION AUTHORIZATION DEFAULT
    | XML OPTION documentOrContent
    | TRANSACTION SNAPSHOT STRING_
    ;

encoding
    : STRING_
    | DEFAULT
    ;

genericSet
    : varName (EQ_|TO) (varList | DEFAULT)
    ;

variableResetStmt
    : RESET resetRest
    ;

resetRest
    : genericReset
    | TIME ZONE
    | TRANSACTION ISOLATION LEVEL
    | SESSION AUTHORIZATION
    ;

genericReset
    : varName
    | ALL
    ;

relationExprList
    : relationExpr (COMMA_ relationExpr)*
    ;

relationExpr
    : qualifiedName (ASTERISK_)?
    | ONLY LP_? qualifiedName RP_?
    ;

commonFuncOptItem
    : CALLED ON NULL INPUT
    | RETURNS NULL ON NULL INPUT
    | STRICT
    | IMMUTABLE
    | STABLE
    | VOLATILE
    | EXTERNAL SECURITY DEFINER
    | EXTERNAL SECURITY INVOKER
    | SECURITY DEFINER
    | SECURITY INVOKER
    | LEAKPROOF
    | NOT LEAKPROOF
    | COST numericOnly
    | ROWS numericOnly
    | SUPPORT anyName
    | functionSetResetClause
    | PARALLEL colId
    ;

functionSetResetClause
    : SET setRestMore
    | variableResetStmt
    ;

rowSecurityCmd
    : ALL | SELECT | INSERT | UPDATE | DELETE
    ;

event
    : SELECT | UPDATE | DELETE | INSERT
    ;

typeNameList
    : typeName (COMMA_ typeName)*
    ;

ifNotExists
    : IF NOT EXISTS
    ;

ifExists
    : IF EXISTS
    ;

booleanValue
    : TRUE | ON | FALSE | OFF | NUMBER_
    ;

hostVariable
    : (COLON_)? identifier
    ;
