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

reserved_keyword
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

//literals
//    : stringLiterals
//    | numberLiterals
//    | hexadecimalLiterals
//    | bitValueLiterals
//    | booleanLiterals
//    | nullValueLiterals
//    ;

//stringLiterals
//    : unicodeEscapes_? STRING_ literalsType_? uescape_?
//    ;

numberLiterals
   : MINUS_? NUMBER_ literalsType_?
   ;

//hexadecimalLiterals
//    : HEX_DIGIT_
//    ;
//
//bitValueLiterals
//    : BIT_NUM_
//    ;
//
//booleanLiterals
//    : TRUE | FALSE
//    ;
//
//nullValueLiterals
//    : NULL
//    ;

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
//	| SKIP
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

type_func_name_keyword
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
    : '('? tableName (COMMA_ tableName)* ')'?
    ;

columnNames
    : '(' columnName (COMMA_ columnName)* ')'
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


cursor_name
    : name
	;

a_expr
    : c_expr
	| a_expr TYPE_CAST_ typeName
	| a_expr COLLATE anyName
	| a_expr AT TIME ZONE a_expr
	| '+' a_expr
	| '-' a_expr
	| a_expr '+' a_expr
	| a_expr '-' a_expr
	| a_expr '*' a_expr
	| a_expr '/' a_expr
	| a_expr '%' a_expr
	| a_expr '^' a_expr
	| a_expr comparisonOperator a_expr
    | '(' a_expr ')' opt_indirection
	| a_expr qual_Op a_expr
	| qual_Op a_expr
	| a_expr qual_Op
	| NOT a_expr
	| a_expr LIKE a_expr
	| a_expr LIKE a_expr ESCAPE a_expr
	| a_expr NOT LIKE a_expr
	| a_expr NOT LIKE a_expr ESCAPE a_expr
	| a_expr ILIKE a_expr
	| a_expr ILIKE a_expr ESCAPE a_expr
	| a_expr NOT ILIKE a_expr
	| a_expr NOT ILIKE a_expr ESCAPE a_expr
	| a_expr SIMILAR TO a_expr
	| a_expr SIMILAR TO a_expr ESCAPE a_expr
	| a_expr NOT SIMILAR TO a_expr
	| a_expr NOT SIMILAR TO a_expr ESCAPE a_expr
	| a_expr IS NULL
	| a_expr ISNULL
	| a_expr IS NOT NULL
	| a_expr NOTNULL
	| row OVERLAPS row
	| a_expr IS TRUE
	| a_expr IS NOT TRUE
	| a_expr IS FALSE
	| a_expr IS NOT FALSE
	| a_expr IS UNKNOWN
	| a_expr IS NOT UNKNOWN
	| a_expr IS DISTINCT FROM a_expr
	| a_expr IS NOT DISTINCT FROM a_expr
	| a_expr IS OF '(' type_list ')'
	| a_expr IS NOT OF '(' type_list ')'
	| a_expr BETWEEN ASYMMETRIC? b_expr AND a_expr
	| a_expr NOT BETWEEN ASYMMETRIC? b_expr AND a_expr
	| a_expr BETWEEN SYMMETRIC b_expr AND a_expr
	| a_expr NOT BETWEEN SYMMETRIC b_expr AND a_expr
	| a_expr IN in_expr
	| a_expr NOT IN in_expr
	| a_expr subquery_Op sub_type select_with_parens
	| a_expr subquery_Op sub_type '(' a_expr ')'
	| UNIQUE select_with_parens
	| a_expr IS DOCUMENT
	| a_expr IS NOT DOCUMENT
	| a_expr IS NORMALIZED
	| a_expr IS unicodeNormalForm NORMALIZED
	| a_expr IS NOT NORMALIZED
	| a_expr IS NOT unicodeNormalForm NORMALIZED
	| a_expr logicalOperator a_expr
	| DEFAULT
	;

b_expr
    : c_expr
	| b_expr TYPE_CAST_ typeName
	| '+' b_expr
	| '-' b_expr
	| b_expr qual_Op b_expr
	| qual_Op b_expr
	| b_expr qual_Op
	| b_expr IS DISTINCT FROM b_expr
	| b_expr IS NOT DISTINCT FROM b_expr
	| b_expr IS OF '(' type_list ')'
	| b_expr IS NOT OF '(' type_list ')'
	| b_expr IS DOCUMENT
	| b_expr IS NOT DOCUMENT
	;

c_expr
    : parameterMarker
    | columnref
	| aexprConst
	| PARAM indirection_el?
	| case_expr
	| func_expr
	| select_with_parens
	| select_with_parens indirection
	| EXISTS select_with_parens
	| ARRAY select_with_parens
	| ARRAY array_expr
	| explicit_row
	| implicit_row
	| GROUPING '(' exprList ')'
	;

indirection
    : indirection_el
	| indirection indirection_el
	;

opt_indirection
    : opt_indirection indirection_el |
	;

indirection_el
    : '.' attrName
	| '.' '*'
	| '[' a_expr ']'
	| '[' slice_bound? ':' slice_bound? ']'
	;

slice_bound
    : a_expr
	;

in_expr
    : select_with_parens | '(' exprList ')'
	;

case_expr
    : CASE case_arg? when_clause_list case_default? END
	;

when_clause_list
    : when_clause
	| when_clause_list when_clause
	;

when_clause
    : WHEN a_expr THEN a_expr
	;

case_default
    : ELSE a_expr
	;

case_arg
    : a_expr
	;


columnref
    : colId
	| colId indirection
	;

qual_Op
    : mathOperator
    | TILDE_TILDE_
    | NOT_TILDE_TILDE_
    | OPERATOR '(' any_operator ')'
	;

subquery_Op
    : all_Op
	| OPERATOR '(' any_operator ')'
	| LIKE
	| NOT LIKE
	| ILIKE
	| NOT ILIKE
	;

all_Op
    : mathOperator
	;

mathOperator
    : '+'
	| '-'
	| '*'
	| '/'
	| '%'
	| '^'
	| '<'
	| '>'
	| '='
	| LTE_
	| GTE_
	| NEQ_
	;


qual_all_Op
    : all_Op
	| OPERATOR '(' any_operator ')'
	;

asc_desc
    : ASC | DESC
	;

any_operator
    : all_Op | colId '.' any_operator
	;

frame_clause
    : RANGE frame_extent window_exclusion_clause?
	| ROWS frame_extent window_exclusion_clause?
	| GROUPS frame_extent window_exclusion_clause?
	;

frame_extent
    : frame_bound
	| BETWEEN frame_bound AND frame_bound
	;

frame_bound
    : UNBOUNDED PRECEDING
	| UNBOUNDED FOLLOWING
	| CURRENT ROW
	| a_expr PRECEDING
	| a_expr FOLLOWING
	;

window_exclusion_clause
    : EXCLUDE CURRENT ROW
	| EXCLUDE GROUP
	| EXCLUDE TIES
	| EXCLUDE NO OTHERS
	;

row
    : ROW '(' exprList ')'
	| ROW '(' ')'
	| '(' exprList ',' a_expr ')'
	;

explicit_row
    : ROW '(' exprList ')'
	| ROW '(' ')'
	;

implicit_row
    : '(' exprList ',' a_expr ')'
	;

sub_type
    : ANY | SOME | ALL
	;

array_expr
    : '[' exprList ']'
	| '[' array_expr_list ']'
	| '[' ']'
	;

array_expr_list
    : array_expr | array_expr_list ',' array_expr
	;

func_arg_list
    : func_arg_expr
	| func_arg_list ',' func_arg_expr
	;

param_name
    : typeFunctionName
	;

func_arg_expr
    : a_expr
	| param_name CQ_ a_expr
	| param_name GTE_ a_expr
	;

type_list
    : typeName
	| type_list ',' typeName
	;

funcApplication
    : funcName '(' ')'
    | funcName '(' func_arg_list sort_clause? ')'
    | funcName '(' VARIADIC func_arg_expr sort_clause? ')'
    | funcName '(' func_arg_list ',' VARIADIC func_arg_expr sort_clause? ')'
    | funcName '(' ALL func_arg_list sort_clause? ')'
    | funcName '(' DISTINCT func_arg_list sort_clause? ')'
    | funcName '(' '*' ')'
    ;

funcName
    : typeFunctionName | colId indirection
    ;

aexprConst
    : NUMBER_
	| STRING_
	| funcName STRING_
	| funcName '(' func_arg_list sort_clause? ')' STRING_
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
    : identifier | unreservedWord |type_func_name_keyword
    ;

functionTable
    : functionExprWindowless optOrdinality
    | ROWS FROM '(' rowsFromList ')' optOrdinality
    ;

xmlTable
    : XMLTABLE '(' c_expr xmlExistsArgument COLUMNS xmlTableColumnList ')'
	| XMLTABLE '(' XMLNAMESPACES '(' xmlNamespaceList ')' ',' c_expr xmlExistsArgument COLUMNS xmlTableColumnList ')'
	;

xmlTableColumnList
    : xmlTableColumnEl
	| xmlTableColumnList ',' xmlTableColumnEl
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
    : identifier b_expr
	| DEFAULT b_expr
	| NOT NULL
	| NULL
	;

xmlNamespaceList
    : xmlNamespaceEl
	| xmlNamespaceList ',' xmlNamespaceEl
	;

xmlNamespaceEl
    : b_expr AS identifier
	| DEFAULT b_expr
	;

func_expr
    : funcApplication within_group_clause? filter_clause? over_clause?
	| functionExprCommonSubexpr
	;

within_group_clause
    : WITHIN GROUP '(' sort_clause ')'
	;

filter_clause
    : FILTER '(' WHERE a_expr ')'
	;

functionExprWindowless
    : funcApplication | functionExprCommonSubexpr
    ;

optOrdinality
    : WITH ORDINALITY |
    ;

functionExprCommonSubexpr
    : COLLATION FOR '(' a_expr ')'
    | CURRENT_DATE
    | CURRENT_TIME
    | CURRENT_TIME '(' NUMBER_ ')'
    | CURRENT_TIMESTAMP
    | CURRENT_TIMESTAMP '(' NUMBER_ ')'
    | LOCALTIME
    | LOCALTIME '(' NUMBER_ ')'
    | LOCALTIMESTAMP
    | LOCALTIMESTAMP '(' NUMBER_ ')'
    | CURRENT_ROLE
    | CURRENT_USER
    | SESSION_USER
    | USER
    | CURRENT_CATALOG
    | CURRENT_SCHEMA
    | CAST '(' a_expr AS typeName ')'
    | EXTRACT '(' extractList? ')'
    | NORMALIZE '(' a_expr ')'
    | NORMALIZE '(' a_expr COMMA_ unicodeNormalForm ')'
    | OVERLAY '(' overlayList ')'
    | POSITION '(' positionList ')'
    | SUBSTRING '(' substrList ')'
    | TREAT '(' a_expr AS typeName ')'
    | TRIM '(' BOTH trimList ')'
    | TRIM '(' LEADING trimList ')'
    | TRIM '(' TRAILING trimList ')'
    | TRIM '(' trimList ')'
    | NULLIF '(' a_expr COMMA_ a_expr ')'
    | COALESCE '(' exprList ')'
    | GREATEST '(' exprList ')'
    | LEAST '(' exprList ')'
    | XMLCONCAT '(' exprList ')'
    | XMLELEMENT '(' NAME identifier ')'
    | XMLELEMENT '(' NAME identifier COMMA_ xmlAttributes ')'
    | XMLELEMENT '(' NAME identifier COMMA_ exprList ')'
    | XMLELEMENT '(' NAME identifier COMMA_ xmlAttributes COMMA_ exprList ')'
    | XMLEXISTS '(' c_expr xmlExistsArgument ')'
    | XMLFOREST '(' xmlAttributeList ')'
    | XMLPARSE '(' documentOrContent a_expr xmlWhitespaceOption ')'
    | XMLPI '(' NAME identifier ')'
    | XMLPI '(' NAME identifier COMMA_ a_expr ')'
    | XMLROOT '(' a_expr COMMA_ xmlRootVersion optXmlRootStandalone ')'
    | XMLSERIALIZE '(' documentOrContent a_expr AS simpleTypeName ')'
    ;

typeName
    : simpleTypeName optArrayBounds
	| SETOF simpleTypeName optArrayBounds
	| simpleTypeName ARRAY '[' NUMBER_ ']'
	| SETOF simpleTypeName ARRAY '[' NUMBER_ ']'
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
	| constInterval '(' NUMBER_ ')'
	;

exprList
    : a_expr
    | exprList ',' a_expr
    ;

extractList
    : extractArg FROM a_expr
	;

extractArg
    : identifier
	| YEAR
	| MONTH
	| DAY
	| HOUR
	| MINUTE
	| SECOND
	;

genericType
    :    typeFunctionName typeModifiers? | typeFunctionName attrs typeModifiers?
    ;

typeModifiers
    : '(' exprList ')'
    ;

numeric
    : INT | INTEGER | SMALLINT | BIGINT| REAL | FLOAT optFloat | DOUBLE PRECISION | DECIMAL typeModifiers? | DEC typeModifiers? | NUMERIC typeModifiers? | BOOLEAN
	;

constDatetime
    : TIMESTAMP '(' NUMBER_ ')' optTimezone
	| TIMESTAMP optTimezone
	| TIME '(' NUMBER_ ')' optTimezone
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
    : characterClause '(' NUMBER_ ')'
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
    : '(' NUMBER_ ')' |
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
    : BIT VARYING? '(' exprList ')'
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
    : optArrayBounds '[' ']'
    | optArrayBounds '[' NUMBER_ ']'
    |
    ;

intervalSecond
    : SECOND
	| SECOND '(' NUMBER_ ')'
	;

unicodeNormalForm
    : NFC | NFD | NFKC | NFKD
	;

trimList
    : a_expr FROM exprList
	| FROM exprList
	| exprList
	;

overlayList
    : a_expr overlayPlacing substrFrom substrFor
    | a_expr overlayPlacing substrFrom
    ;

overlayPlacing
    : PLACING a_expr
    ;

substrFrom
    : FROM a_expr
    ;

substrFor
    : FOR a_expr
    ;

positionList
    : b_expr IN b_expr |
	;

substrList
    : a_expr substrFrom substrFor
	| a_expr substrFor substrFrom
	| a_expr substrFrom
	| a_expr substrFor
	| exprList
	|
	;

xmlAttributes
    : XMLATTRIBUTES '(' xmlAttributeList ')'
	;

xmlAttributeList
    : xmlAttributeEl
	| xmlAttributeList ',' xmlAttributeEl
	;

xmlAttributeEl
    : a_expr AS identifier | a_expr
	;

xmlExistsArgument
    : PASSING c_expr
	| PASSING c_expr xmlPassingMech
	| PASSING xmlPassingMech c_expr
	| PASSING xmlPassingMech c_expr xmlPassingMech
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
    : VERSION a_expr
	| VERSION NO VALUE
	;

optXmlRootStandalone
    : ',' STANDALONE YES
	| ',' STANDALONE NO
	| ',' STANDALONE NO VALUE
	|
	;

rowsFromItem
    : functionExprWindowless optColumnDefList
	;

rowsFromList
    : rowsFromItem
	| rowsFromList ',' rowsFromItem
	;

optColumnDefList
    : AS '(' tableFuncElementList ')'
	;

tableFuncElementList
    : tableFuncElement
    | tableFuncElementList ',' tableFuncElement
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
    : AS colId '(' nameList ')'
	| AS colId
	| colId '(' nameList ')'
	| colId
	;

nameList
    : name | nameList ',' name
	;

funcAliasClause
    : aliasClause
	| AS '(' tableFuncElementList ')'
	| AS colId '(' tableFuncElementList ')'
	| colId '(' tableFuncElementList ')'
	;

tablesampleClause
    : TABLESAMPLE funcName '(' exprList ')' repeatableClause?
	;

repeatableClause
    : REPEATABLE '(' a_expr ')'
	;

all_or_distinct
    : ALL | DISTINCT
	;

sort_clause
    : ORDER BY sortby_list
	;

sortby_list
    : sortby (',' sortby)*
	;

sortby
    : a_expr USING qual_all_Op nulls_order?
	| a_expr asc_desc? nulls_order?
	;

nulls_order
    : NULLS FIRST
	| NULLS LAST
	;

distinct_clause
    : DISTINCT
	| DISTINCT ON '(' exprList ')'
	;

distinct
    : DISTINCT
    ;

over_clause
    : OVER window_specification
	| OVER colId
	;

window_specification
    : '(' window_name? partition_clause? sort_clause? frame_clause? ')'
	;

window_name
    : colId
	;

partition_clause
    : PARTITION BY exprList
	;

index_params
    : index_elem
	| index_params ',' index_elem
	;

index_elem_options
    : collate? opt_class asc_desc? nulls_order?
	| collate? anyName reloptions asc_desc? nulls_order?
	;

index_elem
    : colId index_elem_options
	| functionExprWindowless index_elem_options
	| '(' a_expr ')' index_elem_options
	;

collate
    : COLLATE anyName
	;

opt_class
    : anyName |
	;

reloptions
    : '(' reloption_list ')'
	;

opt_reloptions
    : WITH reloptions |
	;

reloption_list
    : reloption_elem
	| reloption_list ',' reloption_elem
	;

reloption_elem
    : alias '=' def_arg
	| alias
	| alias '.' alias '=' def_arg
	| alias '.' alias
	;

def_arg
    : func_type
	| reserved_keyword
	| qual_all_Op
	| NUMBER_
	| STRING_
	| NONE
	;

func_type
    : typeName
	| typeFunctionName attrs '%' TYPE
	| SETOF typeFunctionName attrs '%' TYPE
	;


//regularFunctionName_
//    : identifier | IF | CURRENT_TIMESTAMP | LOCALTIME | LOCALTIMESTAMP | INTERVAL
//    ;

select_with_parens
    : 'Default does not match anything'
    ;

dataType
    : dataTypeName dataTypeLength? characterSet_? collateClause_? | dataTypeName '(' STRING_ (COMMA_ STRING_)* ')' characterSet_? collateClause_?
    ;

dataTypeName
    : INT | INT2 | INT4 | INT8 | SMALLINT | INTEGER | BIGINT | DECIMAL | NUMERIC | REAL | FLOAT | FLOAT4 | FLOAT8 | DOUBLE PRECISION | SMALLSERIAL | SERIAL | BIGSERIAL
    | MONEY | VARCHAR | CHARACTER | CHAR | TEXT | NAME | BYTEA | TIMESTAMP | DATE | TIME | INTERVAL | BOOLEAN | ENUM | POINT
    | LINE | LSEG | BOX | PATH | POLYGON | CIRCLE | CIDR | INET | MACADDR | MACADDR8 | BIT | VARBIT | TSVECTOR | TSQUERY | UUID | XML
    | JSON | INT4RANGE | INT8RANGE | NUMRANGE | TSRANGE | TSTZRANGE | DATERANGE | ARRAY | identifier
    ;

dataTypeLength
    : '(' NUMBER_ (COMMA_ NUMBER_)? ')'
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
