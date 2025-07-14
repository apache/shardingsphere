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

parser grammar DDLStatement;

import DMLStatement;

options {tokenVocab = ModeLexer;}

createTable
    : CREATE createTableSpecification TABLE ifNotExists? tableName
      (createDefinitionClause | (OF anyName (LP_ typedTableElementList RP_)?) | (PARTITION OF qualifiedName (LP_ typedTableElementList RP_)? partitionBoundSpec))
      inheritClause partitionSpec? tableAccessMethodClause? withOption? onCommitOption? tablespace?
      (AS select withData?)?
      (EXECUTE name executeParamClause withData?)?
    ;

executeParamClause
    : LP_ exprList RP_
    ;

partitionBoundSpec
    : FOR VALUES WITH LP_ hashPartbound RP_
    | FOR VALUES IN LP_ exprList RP_
    | FOR VALUES FROM LP_ exprList RP_ TO LP_ exprList RP_
    | DEFAULT
    ;

hashPartbound
    : hashPartboundElem (COMMA_ hashPartboundElem)*
    ;

hashPartboundElem
    : nonReservedWord NUMBER_
    ;

typedTableElementList
    : typedTableElement (COMMA_ typedTableElement)*
    ;

typedTableElement
    : columnOptions
    | tableConstraint
    ;

columnOptions
    : colId (WITH OPTIONS)? colQualList
    ;

colQualList
    : columnConstraint*
    ;

withData
    : WITH DATA | WITH NO DATA
    ;

tablespace
    : TABLESPACE name
    ;

onCommitOption
    : ON COMMIT (DROP | DELETE ROWS | PRESERVE ROWS)
    ;

withOption
    : WITH reloptions | WITHOUT OIDS
    ;

tableAccessMethodClause
    : USING accessMethod
    ;

accessMethod
    : identifier | unreservedWord | colNameKeyword
    ;

createIndex
    : CREATE createIndexSpecification INDEX concurrentlyClause (ifNotExists? indexName)? ON onlyClause tableName
      accessMethodClause? LP_ indexParams RP_ include? (WITH reloptions)? tablespace? whereClause?
    ;

include
    : INCLUDE LP_ indexIncludingParams RP_
    ;

indexIncludingParams
    : indexElem (COMMA_ indexElem)*
    ;

accessMethodClause
    : USING accessMethod
    ;

createDatabase
    : CREATE DATABASE name WITH? createDatabaseSpecification*
    ;

createView
    : CREATE (OR REPLACE)? (TEMP | TEMPORARY)? RECURSIVE? VIEW qualifiedName
      (LP_ (columnList (COMMA_ columnList)*)? RP_)?
      (WITH reloptions)?
      AS select
      (WITH (CASCADE | LOCAL)? CHECK OPTION)?
    ;

columnList
    : columnElem (COMMA_ columnElem)*
    ;

columnElem
    : colId
    ;

dropDatabase
    : DROP DATABASE ifExists? name
    ;

dropGroup
    : DROP GROUP ifExists? name (COMMA_ name)*
    ;

createDatabaseSpecification
    :  createdbOptName EQ_? (signedIconst | booleanOrString | DEFAULT)
    ;

createdbOptName
    : identifier
    | CONNECTION LIMIT
    | ENCODING
    | LOCATION
    | OWNER
    | TABLESPACE
    | TEMPLATE
    ;

alterTable
    : ALTER TABLE
    ( ifExists? onlyClause tableNameClause alterDefinitionClause
    | ALL IN TABLESPACE tableNameClause (OWNED BY roleList)? SET TABLESPACE name NOWAIT?)
    ;

alterIndex
    : ALTER INDEX (ifExists? | ALL IN TABLESPACE) qualifiedName alterIndexDefinitionClause
    ;

dropTable
    : DROP TABLE ifExists? tableNames dropTableOpt?
    ;

dropTableOpt
    : CASCADE | RESTRICT
    ;

dropIndex
    : DROP INDEX concurrentlyClause ifExists? qualifiedNameList dropIndexOpt?
    ;

dropIndexOpt
    : CASCADE | RESTRICT
    ;

truncateTable
    : TRUNCATE TABLE? onlyClause tableNamesClause restartSeqs? dropTableOpt?
    ;

restartSeqs
    : CONTINUE IDENTITY
    | RESTART IDENTITY
    ;

createTableSpecification
    : ((GLOBAL | LOCAL)? (TEMPORARY | TEMP) | UNLOGGED)?
    ;

createDefinitionClause
    : LP_ (createDefinition (COMMA_ createDefinition)*)? RP_
    ;

createDefinition
    : columnDefinition | tableConstraint | LIKE tableName likeOption*
    ;

columnDefinition
    : columnName dataType collateClause? columnConstraint*
    ;

columnConstraint
    : constraintClause? columnConstraintOption constraintOptionalParam
    ;

constraintClause
    : CONSTRAINT constraintName
    ;

columnConstraintOption
    : NOT? NULL
    | checkOption
    | DEFAULT defaultExpr
    | GENERATED ALWAYS AS LP_ aExpr RP_ STORED
    | GENERATED (ALWAYS | BY DEFAULT) AS IDENTITY (LP_ sequenceOptions RP_)?
    | UNIQUE indexParameters
    | primaryKey indexParameters
    | REFERENCES tableName columnNames? (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)? (ON (DELETE | UPDATE) action)*
    ;

checkOption
    : CHECK aExpr (NO INHERIT)?
    ;

defaultExpr
    : CURRENT_TIMESTAMP | aExpr
    ;

sequenceOptions
    : sequenceOption+
    ;

sequenceOption
    : START WITH? NUMBER_
    | INCREMENT BY? NUMBER_
    | MAXVALUE NUMBER_
    | NO MAXVALUE
    | MINVALUE NUMBER_
    | NO MINVALUE
    | CYCLE
    | NO CYCLE
    | CACHE NUMBER_
    | OWNED BY
    ;

indexParameters
    : (USING INDEX TABLESPACE ignoredIdentifier)?
    | INCLUDE columnNames
    | WITH definition
    ;

action
    : NO ACTION | RESTRICT | CASCADE | SET (NULL | DEFAULT)
    ;

constraintOptionalParam
    : (NOT? DEFERRABLE)? (INITIALLY (DEFERRED | IMMEDIATE))?
    ;

likeOption
    : (INCLUDING | EXCLUDING) (COMMENTS | CONSTRAINTS | DEFAULTS | IDENTITY | INDEXES | STATISTICS | STORAGE | ALL)
    ;

tableConstraint
    : constraintClause? tableConstraintOption constraintOptionalParam
    ;

tableConstraintOption
    : checkOption
    | UNIQUE columnNames indexParameters
    | primaryKey columnNames indexParameters
    | EXCLUDE (USING ignoredIdentifier)? LP_ exclusionConstraintList RP_ indexParameters exclusionWhereClause?
    | FOREIGN KEY columnNames REFERENCES tableName columnNames? (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)? (ON (DELETE | UPDATE) action)*
    ;

exclusionWhereClause
    : WHERE LP_ aExpr RP_
    ;

exclusionConstraintList
    : exclusionConstraintElem (COMMA_ exclusionConstraintElem)*
    ;

exclusionConstraintElem
    : indexElem WITH anyOperator
    | indexElem WITH OPERATOR LP_ anyOperator RP_
    ;

inheritClause
    : (INHERITS tableNames)?
    ;

partitionSpec
    : PARTITION BY partStrategy LP_ partParams RP_
    ;

partParams
    : partElem (COMMA_ partElem)*
    ;

partElem
    : colId (COLLATE anyName)?  anyName?
    | LP_ aExpr RP_ (COLLATE anyName)?  anyName?
    | funcExprWindowless (COLLATE anyName)?  anyName?
    ;

funcExprWindowless
    : funcApplication | functionExprCommonSubexpr
    ;

partStrategy
    : identifier
    | unreservedWord
    ;

createIndexSpecification
    : UNIQUE?
    ;

concurrentlyClause
    : CONCURRENTLY?
    ;

onlyClause
    : ONLY?
    ;

asteriskClause
    : ASTERISK_?
    ;

alterDefinitionClause
    : alterTableActions
    | renameColumnSpecification
    | renameConstraint
    | renameTableSpecification
    | SET SCHEMA name
    | partitionCmd
    ;

partitionCmd
    : ATTACH PARTITION qualifiedName partitionBoundSpec
    | DETACH PARTITION qualifiedName (CONCURRENTLY | FINALIZE)?
    ;

alterIndexDefinitionClause
    : renameIndexSpecification | alterIndexDependsOnExtension | alterIndexSetTableSpace | alterTableCmds | indexPartitionCmd
    ;

indexPartitionCmd
    : ATTACH PARTITION qualifiedName
    ;

renameIndexSpecification
    : RENAME TO indexName
    ;

alterIndexDependsOnExtension
    : DEPENDS ON EXTENSION ignoredIdentifier
    ;

alterIndexSetTableSpace
    : (OWNED BY ignoredIdentifiers)? SET TABLESPACE name (NOWAIT)?
    ;

tableNamesClause
    : tableNameClause (COMMA_ tableNameClause)*
    ;

tableNameClause
    : tableName ASTERISK_?
    ;

alterTableActions
    : alterTableAction (COMMA_ alterTableAction)*
    ;

alterTableAction
    : addColumnSpecification
    | dropColumnSpecification
    | modifyColumnSpecification
    | addConstraintSpecification
    | modifyConstraintSpecification
    | validateConstraintSpecification
    | dropConstraintSpecification
    | ALTER COLUMN? colId SET STATISTICS signedIconst
    | ALTER COLUMN? NUMBER_ SET STATISTICS signedIconst
    | ALTER COLUMN? colId SET reloptions
    | ALTER COLUMN? colId RESET reloptions
    | ALTER COLUMN? colId SET STORAGE colId
    | ALTER COLUMN? colId SET columnCompression
    | ALTER COLUMN? colId DROP EXPRESSION ifExists?
    | (DISABLE | ENABLE) TRIGGER (ignoredIdentifier | ALL | USER)?
    | ENABLE (REPLICA | ALWAYS) TRIGGER ignoredIdentifier
    | (DISABLE | ENABLE) RULE ignoredIdentifier
    | ENABLE (REPLICA | ALWAYS) RULE ignoredIdentifier
    | (DISABLE | ENABLE | (NO? FORCE)) ROW LEVEL SECURITY
    | CLUSTER ON indexName
    | SET (ACCESS METHOD name) (COMMA_ ACCESS METHOD name)*
    | SET WITHOUT CLUSTER
    | SET (WITH | WITHOUT) OIDS
    | SET STATISTICS signedIconst
    | SET TABLESPACE ignoredIdentifier
    | SET (LOGGED | UNLOGGED)
    | SET LP_ (storageParameterWithValue) (COMMA_ storageParameterWithValue)* RP_
    | SET LP_ (storageParameter) (COMMA_ storageParameter)* RP_
    | RESET LP_ storageParameterWithValue (COMMA_ storageParameterWithValue)* RP_
    | INHERIT tableName
    | NO INHERIT tableName
    | OF dataTypeName
    | NOT OF
    | OWNER TO (ignoredIdentifier | CURRENT_USER | SESSION_USER)
    | REPLICA IDENTITY (DEFAULT | (USING INDEX indexName) | FULL | NOTHING)
    ;

addColumnSpecification
    : ADD COLUMN? ifNotExists? columnDefinition
    ;

dropColumnSpecification
    : DROP COLUMN? ifExists? columnName (RESTRICT | CASCADE)?
    ;

modifyColumnSpecification
    : modifyColumn (SET DATA)? TYPE dataType collateClause? (USING aExpr)?
    | modifyColumn SET DEFAULT aExpr
    | modifyColumn DROP DEFAULT
    | modifyColumn (SET | DROP) NOT NULL
    | modifyColumn ADD GENERATED (ALWAYS | (BY DEFAULT)) AS IDENTITY (LP_ sequenceOptions RP_)?
    | modifyColumn alterColumnSetOption alterColumnSetOption*
    | modifyColumn DROP IDENTITY ifExists?
    | modifyColumn SET STATISTICS NUMBER_
    | modifyColumn SET LP_ attributeOptions RP_
    | modifyColumn RESET LP_ attributeOptions RP_
    | modifyColumn SET STORAGE (PLAIN | EXTERNAL | EXTENDED | MAIN)
    ;

modifyColumn
    : ALTER COLUMN? columnName
    ;

alterColumnSetOption
    : SET (GENERATED (ALWAYS | BY DEFAULT) | sequenceOption) | RESTART (WITH? NUMBER_)?
    ;

attributeOptions
    : attributeOption (COMMA_ attributeOption)*
    ;

attributeOption
    : IDENTIFIER_ EQ_ aExpr
    ;

addConstraintSpecification
    : ADD (tableConstraint (NOT VALID)? | tableConstraintUsingIndex)
    ;

tableConstraintUsingIndex
    : (CONSTRAINT constraintName)? (UNIQUE | primaryKey) USING INDEX indexName constraintOptionalParam
    ;

modifyConstraintSpecification
    : ALTER CONSTRAINT constraintName constraintOptionalParam
    ;

validateConstraintSpecification
    : VALIDATE CONSTRAINT constraintName
    ;

dropConstraintSpecification
    : DROP CONSTRAINT ifExists? constraintName (RESTRICT | CASCADE)?
    ;

storageParameterWithValue
    : storageParameter EQ_ aExpr
    ;

storageParameter
    : IDENTIFIER_
    ;

renameColumnSpecification
    : RENAME COLUMN? columnName TO columnName
    ;

renameConstraint
    : RENAME CONSTRAINT ignoredIdentifier TO ignoredIdentifier
    ;

renameTableSpecification
    : RENAME TO identifier
    ;

indexNames
    : indexName (COMMA_ indexName)*
    ;

alterDatabase
    : ALTER DATABASE databaseName alterDatabaseClause
    ;

alterDatabaseClause
    : WITH? createdbOptItems?
    | RENAME TO databaseName
    | OWNER TO roleSpec
    | SET TABLESPACE name
    | setResetClause
    ;

createdbOptItems
    : createdbOptItem+
    ;

createdbOptItem
    : createdbOptName EQ_? signedIconst
    | createdbOptName EQ_? booleanOrString
    | createdbOptName EQ_? DEFAULT
    ;

alterTableCmds
    : alterTableCmd (COMMA_ alterTableCmd)*
    ;

alterTableCmd
    : ADD COLUMN? ifNotExists? columnDef
    | ALTER COLUMN? colId alterColumnDefault
    | ALTER COLUMN? colId DROP NOT NULL
    | ALTER COLUMN? colId SET NOT NULL
    | ALTER COLUMN? colId SET STATISTICS signedIconst
    | ALTER COLUMN? NUMBER_ SET STATISTICS signedIconst
    | ALTER COLUMN? colId SET reloptions
    | ALTER COLUMN? colId RESET reloptions
    | ALTER COLUMN? colId SET STORAGE colId
    | ALTER COLUMN? colId SET columnCompression
    | ALTER COLUMN? colId ADD GENERATED generatedWhen AS IDENTITY parenthesizedSeqOptList?
    | ALTER COLUMN? colId alterIdentityColumnOptionList
    | ALTER COLUMN? colId DROP IDENTITY
    | ALTER COLUMN? colId DROP IDENTITY ifExists
    | DROP COLUMN? ifExists colId dropBehavior?
    | DROP COLUMN? colId dropBehavior?
    | ALTER COLUMN? colId setData? TYPE typeName collateClause? alterUsing?
    | ALTER COLUMN? colId alterGenericOptions
    | ADD tableConstraint (NOT VALID)?
    | ALTER CONSTRAINT name constraintAttributeSpec
    | VALIDATE CONSTRAINT name
    | DROP CONSTRAINT ifExists name dropBehavior?
    | DROP CONSTRAINT name dropBehavior?
    | SET WITHOUT OIDS
    | CLUSTER ON name
    | SET WITHOUT CLUSTER
    | SET LOGGED
    | SET UNLOGGED
    | ENABLE TRIGGER name
    | ENABLE ALWAYS TRIGGER name
    | ENABLE REPLICA TRIGGER name
    | ENABLE TRIGGER ALL
    | ENABLE TRIGGER USER
    | DISABLE TRIGGER name
    | DISABLE TRIGGER ALL
    | DISABLE TRIGGER USER
    | ENABLE RULE name
    | ENABLE ALWAYS RULE name
    | ENABLE REPLICA RULE name
    | DISABLE RULE name
    | INHERIT qualifiedName
    | NO INHERIT qualifiedName
    | OF anyName
    | NOT OF
    | OWNER TO roleSpec
    | SET TABLESPACE name
    | SET reloptions
    | RESET reloptions
    | REPLICA IDENTITY replicaIdentity
    | ENABLE ROW LEVEL SECURITY
    | DISABLE ROW LEVEL SECURITY
    | FORCE ROW LEVEL SECURITY
    | NO FORCE ROW LEVEL SECURITY
    | alterGenericOptions
    ;

columnCompression
    : COMPRESSION colId
    | COMPRESSION DEFAULT
    ;

constraintAttributeSpec
    : constraintAttributeElem*
    ;

constraintAttributeElem
    : NOT DEFERRABLE
    | DEFERRABLE
    | INITIALLY IMMEDIATE
    | INITIALLY DEFERRED
    | NOT VALID
    | NO INHERIT
    ;

alterGenericOptions
    : OPTIONS LP_ alterGenericOptionList RP_
    ;

alterGenericOptionList
    : alterGenericOptionElem (COMMA_ alterGenericOptionElem)*
    ;

alterGenericOptionElem
    : genericOptionElem
    | SET genericOptionElem
    | ADD genericOptionElem
    | DROP genericOptionName
    ;

genericOptionName
    : colLable
    ;

dropBehavior
    : CASCADE | RESTRICT
    ;

alterUsing
    : USING aExpr
    ;

setData
    : SET DATA
    ;

alterIdentityColumnOptionList
    : alterIdentityColumnOption+
    ;

alterIdentityColumnOption
    : RESTART
    | RESTART WITH? numericOnly
    | SET seqOptElem
    | SET GENERATED generatedWhen
    ;

alterColumnDefault
    : SET DEFAULT aExpr
    | DROP DEFAULT
    ;

alterOperator
    : ALTER OPERATOR alterOperatorClauses
    ;

alterOperatorClass
    : ALTER OPERATOR CLASS anyName USING name alterOperatorClassClauses
    ;

alterOperatorClassClauses
    : RENAME TO name | SET SCHEMA name | OWNER TO roleSpec
    ;

alterOperatorFamily
    : ALTER OPERATOR FAMILY anyName USING name alterOperatorFamilyClauses
    ;

alterOperatorFamilyClauses
    : (ADD | DROP) opclassItemList
    | alterOperatorClassClauses
    ;

opclassItemList
    : opclassItem (COMMA_ opclassItem)*
    ;

opclassItem
    : OPERATOR NUMBER_ anyOperator opclassPurpose? RECHECK?
    | OPERATOR NUMBER_ operatorWithArgtypes opclassPurpose? RECHECK?
    | FUNCTION NUMBER_ functionWithArgtypes
    | FUNCTION NUMBER_ LP_ typeList RP_ functionWithArgtypes
    | STORAGE typeName
    ;

opclassPurpose
    : FOR SEARCH | FOR ORDER BY anyName
    ;

alterOperatorClauses
    : operatorWithArgtypes SET SCHEMA schemaName
    | operatorWithArgtypes SET LP_ operatorDefList RP_
    | operatorWithArgtypes OWNER TO roleSpec
    ;

operatorDefList
    : operatorDefElem (COMMA_ operatorDefElem)*
    ;

operatorDefElem
    : (RESTRICT | JOIN) EQ_ (NONE | operatorDefArg)
    ;

operatorDefArg
    : funcType
    | reservedKeyword
    | qualAllOp
    | numericOnly
    | STRING_
    ;

operatorWithArgtypes
    : anyOperator operArgtypes
    ;

alterAggregate
    : ALTER AGGREGATE aggregateSignature alterAggregateDefinitionClause
    ;

aggregateSignature
    : funcName aggrArgs
    ;

aggrArgs
    : LP_ ASTERISK_ RP_
    | LP_ aggrArgsList RP_
    | LP_ ORDER BY aggrArgsList RP_
    | LP_ aggrArgsList ORDER BY aggrArgsList RP_
    ;

aggrArgsList
    : aggrArg (COMMA_ aggrArg)*
    ;

aggrArg
    : funcArg
    ;

alterAggregateDefinitionClause
    : RENAME TO name
    | OWNER TO  roleSpec
    | SET SCHEMA schemaName
    ;

alterCollation
    : ALTER COLLATION anyName alterCollationClause
    ;

alterCollationClause
    : REFRESH VERSION
    | RENAME TO name
    | OWNER TO roleSpec
    | SET SCHEMA schemaName
    ;

alterConversion
    : ALTER CONVERSION anyName alterConversionClause
    ;

alterConversionClause
    : RENAME TO name
    | OWNER TO roleSpec
    | SET SCHEMA schemaName
    ;

alterDefaultPrivileges
    : ALTER DEFAULT PRIVILEGES defACLOptionList? defACLAction
    ;

defACLAction
    : GRANT privileges ON defaclPrivilegeTarget TO granteeList grantGrantOption?
    | REVOKE privileges ON defaclPrivilegeTarget FROM granteeList dropBehavior?
    | REVOKE GRANT OPTION FOR privileges ON defaclPrivilegeTarget FROM granteeList dropBehavior?
    ;

grantGrantOption
    : WITH GRANT OPTION
    ;

granteeList
    : grantee (COMMA_ grantee)*
    ;

grantee
    : GROUP? roleSpec
    ;

defaclPrivilegeTarget
    :TABLES
    | FUNCTIONS
    | ROUTINES
    | SEQUENCES
    | TYPES
    | SCHEMAS
    ;

privileges
    : privilegeList
    | ALL
    | ALL PRIVILEGES
    | ALL LP_ columnList RP_
    | ALL PRIVILEGES LP_ columnList RP_
    ;

privilegeList
    : privilege (COMMA_ privilege)*
    ;

privilege
    : SELECT optColumnList?
    | REFERENCES optColumnList?
    | CREATE optColumnList?
    | colId optColumnList?
    ;

defACLOptionList
    : defACLOption+
    ;

defACLOption
    : IN SCHEMA schemaNameList
    | FOR (ROLE | USER) roleList
    ;

schemaNameList
    : nameList
    ;

alterDomain
    : ALTER DOMAIN alterDomainClause
    ;

alterDomainClause
    : anyName (SET | DROP) NOT NULL
    | anyName ADD tableConstraint (NOT VALID)?
    | anyName DROP CONSTRAINT ifExists? name dropBehavior?
    | anyName VALIDATE CONSTRAINT name
    | anyName RENAME CONSTRAINT constraintName TO constraintName
    | anyName OWNER TO roleSpec
    | anyName RENAME TO anyName
    | anyName SET SCHEMA name
    | anyName alterColumnDefault
    ;

alterEventTrigger
    : ALTER EVENT TRIGGER tiggerName alterEventTriggerClause
    ;

alterEventTriggerClause
    : DISABLE
    | ENABLE (REPLICA | ALWAYS)
    | OWNER TO roleSpec
    | RENAME TO tiggerName
    ;

tiggerName
    : colId
    ;

alterExtension
    : ALTER EXTENSION name alterExtensionClauses
    ;

alterExtensionClauses
    : UPDATE alterExtensionOptList
    | (ADD | DROP) ACCESS METHOD name
    | (ADD | DROP) AGGREGATE aggregateWithArgtypes
    | (ADD | DROP) CAST LP_ typeName AS typeName RP_
    | (ADD | DROP) COLLATION anyName
    | (ADD | DROP) CONVERSION anyName
    | (ADD | DROP) DOMAIN typeName
    | (ADD | DROP) FUNCTION functionWithArgtypes
    | (ADD | DROP) PROCEDURAL? LANGUAGE name
    | (ADD | DROP) OPERATOR operatorWithArgtypes
    | (ADD | DROP) OPERATOR (CLASS | FAMILY) anyName USING accessMethod
    | (ADD | DROP) PROCEDURE functionWithArgtypes
    | (ADD | DROP) ROUTINE functionWithArgtypes
    | (ADD | DROP) SCHEMA name
    | (ADD | DROP) EVENT TRIGGER name
    | (ADD | DROP) TABLE anyName
    | (ADD | DROP) TEXT SEARCH PARSER anyName
    | (ADD | DROP) TEXT SEARCH DICTIONARY anyName
    | (ADD | DROP) TEXT SEARCH TEMPLATE anyName
    | (ADD | DROP) TEXT SEARCH CONFIGURATION anyName
    | (ADD | DROP) SEQUENCE anyName
    | (ADD | DROP) VIEW anyName
    | (ADD | DROP) MATERIALIZED VIEW anyName
    | (ADD | DROP) FOREIGN TABLE anyName
    | (ADD | DROP) FOREIGN DATA WRAPPER name
    | (ADD | DROP) SERVER name
    | (ADD | DROP) TRANSFORM FOR typeName LANGUAGE name
    | (ADD | DROP) TYPE typeName
    | SET SCHEMA name
    ;

functionWithArgtypes
    : funcName funcArgs
    | typeFuncNameKeyword
    | colId
    | colId indirection
    ;

funcArgs
    : LP_ funcArgsList RP_
    | LP_ RP_
    ;

aggregateWithArgtypes
    : funcName aggrArgs
    ;

alterExtensionOptList
    : alterExtensionOptItem*
    ;

alterExtensionOptItem
    : TO (nonReservedWord | STRING_)
    ;

alterForeignDataWrapper
    : ALTER FOREIGN DATA WRAPPER colId alterForeignDataWrapperClauses
    ;

alterForeignDataWrapperClauses
    : fdwOptions? alterGenericOptions
    | fdwOptions
    | RENAME TO name
    | OWNER TO roleSpec
    ;

genericOptionElem
    : genericOptionName genericOptionArg
    ;

genericOptionArg
    : aexprConst
    ;

fdwOptions
    : fdwOption+
    ;

fdwOption
    : HANDLER handlerName
    | NO HANDLER
    | VALIDATOR handlerName
    | NO VALIDATOR
    ;

handlerName
    : anyName
    ;

alterGroup
    : ALTER GROUP alterGroupClauses
    ;

alterGroupClauses
    : roleSpec (ADD|DROP) USER roleList
    | roleSpec RENAME TO roleSpec
    ;

alterLanguage
    : ALTER PROCEDURAL? LANGUAGE colId (RENAME TO colId | OWNER TO (ignoredIdentifier | CURRENT_USER | SESSION_USER))
    ;

alterLargeObject
    : ALTER LARGE OBJECT numericOnly OWNER TO (ignoredIdentifier | CURRENT_USER | SESSION_USER)
    ;

alterMaterializedView
    : ALTER MATERIALIZED VIEW alterMaterializedViewClauses
    ;

alterMaterializedViewClauses
    : ifExists? qualifiedName alterTableCmds
    | qualifiedName DEPENDS ON EXTENSION name
    | ifExists? qualifiedName RENAME COLUMN? columnName TO columnName
    | ifExists? qualifiedName RENAME TO qualifiedName
    | ifExists? qualifiedName SET SCHEMA schemaName
    | ALL IN TABLESPACE name (OWNED BY roleList) SET TABLESPACE name NOWAIT?
    ;

executeStmt
    : EXECUTE name executeParamClause?
    ;

createMaterializedView
    : CREATE UNLOGGED? MATERIALIZED VIEW ifNotExists? createMvTarget AS select (WITH DATA | WITH NO DATA)?
    ;

createMvTarget
    : qualifiedName optColumnList? tableAccessMethodClause? (WITH reloptions)? (TABLESPACE name)?
    ;

alterPolicy
    : ALTER POLICY name ON tableName alterPolicyClauses
    ;

alterPolicyClauses
    : (TO roleList)? (USING LP_ aExpr RP_)? (WITH CHECK LP_ aExpr RP_)?
    | RENAME TO name
    ;

refreshMatViewStmt
    : REFRESH MATERIALIZED VIEW CONCURRENTLY? qualifiedName (WITH DATA | WITH NO DATA)?
    ;

alterProcedure
    : ALTER PROCEDURE functionWithArgtypes alterProcedureClauses
    ;

alterProcedureClauses
    : alterfuncOptList RESTRICT?
    | RENAME TO name
    | NO? DEPENDS ON EXTENSION name
    | SET SCHEMA name
    | OWNER TO roleSpec
    ;

alterfuncOptList
    : commonFuncOptItem+
    ;

alterFunction
    : ALTER FUNCTION functionWithArgtypes alterFunctionClauses
    ;

alterFunctionClauses
    : alterfuncOptList RESTRICT?
    | RENAME TO name
    | NO? DEPENDS ON EXTENSION name
    | SET SCHEMA name
    | OWNER TO roleSpec
    ;

alterPublication
    : ALTER PUBLICATION name
    ( RENAME TO name
    | OWNER TO roleSpec
    | SET definition
    | (ADD | SET | DROP) TABLE relationExprList)
    ;

alterRoutine
    : ALTER ROUTINE functionWithArgtypes alterProcedureClauses
    ;

alterRule
    : ALTER RULE name ON qualifiedName RENAME TO name
    ;

alterSequence
    : ALTER SEQUENCE ifExists? qualifiedName alterSequenceClauses
    ;

alterSequenceClauses
    : OWNER TO roleSpec | seqOptList | RENAME TO name | SET SCHEMA name
    ;

alterServer
    : ALTER SERVER name
    ( foreignServerVersion alterGenericOptions
    | foreignServerVersion
    | alterGenericOptions
    | RENAME TO name
    | OWNER TO roleSpec)
    ;

foreignServerVersion
    : VERSION (STRING_ | NULL)
    ;

alterStatistics
    : ALTER STATISTICS
    ( ifExists? anyName SET STATISTICS signedIconst
    | anyName RENAME TO name
    | anyName SET SCHEMA name
    | anyName OWNER TO roleSpec )
    ;

alterSubscription
    : ALTER SUBSCRIPTION name
    ( RENAME TO name
    | OWNER TO roleSpec
    | SET definition
    | CONNECTION STRING_
    | REFRESH PUBLICATION (WITH definition)?
    | SET PUBLICATION publicationNameList (WITH definition)?
    | ADD PUBLICATION publicationNameList (WITH definition)?
    | DROP PUBLICATION publicationNameList (WITH definition)?
    | (ENABLE | DISABLE))
    ;

publicationNameList
    : publicationNameItem (COMMA_ publicationNameItem)*
    ;

publicationNameItem
    : colLabel
    ;

alterSystem
    : ALTER SYSTEM (SET genericSet | RESET genericReset)
    ;

alterTablespace
    : ALTER TABLESPACE name
    ( SET|RESET reloptions
    | RENAME TO name
    | OWNER TO roleSpec)
    ;

alterTextSearchConfiguration
    : ALTER TEXT SEARCH CONFIGURATION anyName alterTextSearchConfigurationClauses
    ;

alterTextSearchConfigurationClauses
    : RENAME TO name
    | SET SCHEMA name
    | OWNER TO roleSpec
    | (ADD | ALTER) MAPPING FOR nameList WITH? anyNameList
    | ALTER MAPPING (FOR nameList)? REPLACE anyName WITH anyName
    | DROP MAPPING ifExists? FOR nameList
    ;

anyNameList
    : anyName (COMMA_ anyName)*
    ;

alterTextSearchDictionary
    : ALTER TEXT SEARCH DICTIONARY anyName
    ( RENAME TO name
    | SET SCHEMA name
    | OWNER TO roleSpec
    | definition)
    ;

alterTextSearchParser
    : ALTER TEXT SEARCH PARSER anyName (RENAME TO name | SET SCHEMA name)
    ;

alterTextSearchTemplate
    : ALTER TEXT SEARCH TEMPLATE anyName (RENAME TO name | SET SCHEMA name)
    ;

alterTrigger
    : ALTER TRIGGER name ON qualifiedName (RENAME TO name | NO? DEPENDS ON EXTENSION name)
    ;

alterType
    : ALTER TYPE anyName alterTypeClauses
    ;

alterTypeClauses
    : alterTypeCmds
    | ADD VALUE ifNotExists? STRING_ ((BEFORE | AFTER) STRING_)?
    | RENAME VALUE STRING_ TO STRING_
    | RENAME TO name
    | RENAME ATTRIBUTE name TO name dropBehavior?
    | SET SCHEMA name
    | SET LP_ typeDefList RP_
    | OWNER TO roleSpec
    ;

alterTypeCmds
    : alterTypeCmd (COMMA_ alterTypeCmd)?
    ;

alterTypeCmd
    : ADD ATTRIBUTE tableFuncElement dropBehavior?
    | DROP ATTRIBUTE ifExists? colId dropBehavior?
    | ALTER ATTRIBUTE colId setData? TYPE typeName collateClause? dropBehavior?
    ;

typeDefList
    : typeDefElem (COMMA_ typeDefElem)*
    ;

typeDefElem
    : (RECEIVE | SEND | TYPMOD_IN | TYPMOD_OUT | ANALYZE | SUBSCRIPT | STORAGE) EQ_ (NONE | typeDefArg)
    ;

typeDefArg
    : funcType
    | reservedKeyword
    | qualAllOp
    | numericOnly
    | STRING_
    ;

alterUserMapping
    : ALTER USER MAPPING FOR authIdent SERVER name alterGenericOptions
    ;

authIdent
    : roleSpec | USER
    ;

alterView
    : ALTER VIEW ifExists? qualifiedName alterViewClauses
    ;

alterViewClauses
    : alterTableCmds #alterViewCmds
    | RENAME TO name #alterRenameView
    | RENAME COLUMN? name TO name #alterRenameColumn
    | SET SCHEMA name #alterSetSchema
    ;

close
    : CLOSE (cursorName | ALL)
    ;

cluster
    : CLUSTER clusterVerboseSpecification? tableName? clusterIndexSpecification?
    ;

clusterVerboseSpecification
    : VERBOSE | clusterVerboseOptionList
    ;

clusterIndexSpecification
    : USING indexName
    ;

clusterVerboseOptionList
    : LP_ clusterVerboseOption (COMMA_ clusterVerboseOption)* RP_
    ;

clusterVerboseOption
    : VERBOSE booleanValue?
    ;

comment
    : COMMENT ON commentClauses
    ;

commentClauses
    : objectTypeAnyName anyName IS commentText
    | COLUMN anyName IS commentText
    | objectTypeName name IS commentText
    | TYPE typeName IS commentText
    | DOMAIN typeName IS commentText
    | AGGREGATE aggregateWithArgtypes IS commentText
    | FUNCTION functionWithArgtypes IS commentText
    | OPERATOR operatorWithArgtypes IS commentText
    | CONSTRAINT name ON DOMAIN anyName IS commentText
    | objectTypeNameOnAnyName name ON tableName IS commentText
    | PROCEDURE functionWithArgtypes IS commentText
    | ROUTINE functionWithArgtypes IS commentText
    | TRANSFORM FOR typeName LANGUAGE name IS commentText
    | OPERATOR CLASS anyName USING name IS commentText
    | OPERATOR FAMILY anyName USING name IS commentText
    | LARGE OBJECT numericOnly IS commentText
    | CAST LP_ typeName AS typeName RP_ IS commentText
    ;

objectTypeNameOnAnyName
    : POLICY | RULE	| TRIGGER | CONSTRAINT
    ;

objectTypeName
    : dropTypeName
    | DATABASE
    | ROLE
    | SUBSCRIPTION
    | TABLESPACE
    ;

dropTypeName
    : ACCESS METHOD
    | EVENT TRIGGER
    | EXTENSION
    | FOREIGN DATA WRAPPER
    | PROCEDURAL? LANGUAGE
    | PUBLICATION
    | SCHEMA
    | SERVER
    ;

objectTypeAnyName
    : TABLE
    | SEQUENCE
    | VIEW
    | MATERIALIZED VIEW
    | INDEX
    | FOREIGN TABLE
    | COLLATION
    | CONVERSION
    | STATISTICS
    | TEXT SEARCH PARSER
    | TEXT SEARCH DICTIONARY
    | TEXT SEARCH TEMPLATE
    | TEXT SEARCH CONFIGURATION
    ;

commentText
    : STRING_ | NULL
    ;

createAccessMethod
    : CREATE ACCESS METHOD name TYPE (INDEX|TABLE) HANDLER handlerName
    ;

createAggregate
    : CREATE (OR REPLACE)? AGGREGATE funcName (aggrArgs definition | oldAggrDefinition)
    ;

oldAggrDefinition
    : LP_ oldAggrList RP_
    ;

oldAggrList
    : oldAggrElem (COMMA_ oldAggrElem)*
    ;

oldAggrElem
    : identifier EQ_ defArg
    ;

createCast
    : CREATE CAST LP_ typeName AS typeName RP_
    ( WITH FUNCTION functionWithArgtypes castContext?
    | WITHOUT FUNCTION castContext?
    | WITH INOUT castContext?)
    ;

castContext
    : AS IMPLICIT | AS ASSIGNMENT
    ;

createCollation
    : CREATE COLLATION ifNotExists? (anyName definition | anyName FROM anyName)
    ;

createConversion
    : CREATE DEFAULT? CONVERSION anyName FOR STRING_ TO STRING_ FROM anyName
    ;

createDomain
    : CREATE DOMAIN anyName AS? typeName colQualList
    ;

createEventTrigger
    : CREATE EVENT TRIGGER name ON colLabel (WHEN eventTriggerWhenList)? EXECUTE (FUNCTION | PROCEDURE) funcName LP_ RP_
    ;

eventTriggerWhenList
    : eventTriggerWhenItem (AND eventTriggerWhenItem)*
    ;

eventTriggerWhenItem
    : colId IN LP_ eventTriggerValueList RP_
    ;

eventTriggerValueList
    : STRING_ (COMMA_ STRING_)*
    ;

createExtension
    : CREATE EXTENSION ifNotExists? name WITH? createExtensionOptList
    ;

createExtensionOptList
    : createExtensionOptItem*
    ;

createExtensionOptItem
    : SCHEMA name
    | VERSION nonReservedWordOrSconst
    | FROM nonReservedWordOrSconst
    | CASCADE
    ;

createForeignDataWrapper
    : CREATE FOREIGN DATA WRAPPER name fdwOptions? createGenericOptions?
    ;

createForeignTable
    : CREATE FOREIGN TABLE createForeignTableClauses
    ;

createForeignTableClauses
    : ifNotExists? qualifiedName LP_ tableElementList? RP_
      (INHERITS LP_ qualifiedNameList RP_)? SERVER name createGenericOptions?
    | ifNotExists? qualifiedName PARTITION OF qualifiedName (LP_ typedTableElementList RP_)? partitionBoundSpec
      SERVER name createGenericOptions?
    ;

tableElementList
    : tableElement (COMMA_ tableElement)*
    ;

tableElement
    : columnDef	| tableLikeClause | tableConstraint
    ;

tableLikeClause
    : LIKE qualifiedName tableLikeOptionList
    ;

tableLikeOptionList
    : tableLikeOptionList (INCLUDING | EXCLUDING) tableLikeOption |
    ;

tableLikeOption
    : COMMENTS
    | CONSTRAINTS
    | DEFAULTS
    | IDENTITY
    | GENERATED
    | INDEXES
    | STATISTICS
    | STORAGE
    | ALL
    ;

createFunction
    : CREATE (OR REPLACE)? FUNCTION funcName funcArgsWithDefaults
    ( RETURNS funcReturn createfuncOptList
    | RETURNS TABLE LP_ tableFuncColumnList RP_ createfuncOptList
    | createfuncOptList)
    ;

tableFuncColumnList
    : tableFuncColumn (COMMA_ tableFuncColumn)*
    ;

tableFuncColumn
    : paramName funcType
    ;

createfuncOptList
    : createfuncOptItem+
    ;

createfuncOptItem
    : AS funcAs
    | LANGUAGE nonReservedWordOrSconst
    | TRANSFORM transformTypeList
    | WINDOW
    | commonFuncOptItem
    ;

transformTypeList
    : FOR TYPE typeName (COMMA_ FOR TYPE typeName)
    ;

funcAs
    : funcDefinition (COMMA_ funcDefinition)?
    ;

funcDefinition
    : identifier | aexprConst
    ;

funcReturn
    : funcType
    ;

funcArgsWithDefaults
    : LP_ funcArgsWithDefaultsList? RP_
    ;

funcArgsWithDefaultsList
    : funcArgWithDefault (COMMA_ funcArgWithDefault)*
    ;

funcArgWithDefault
    : funcArg
    | funcArg DEFAULT aExpr
    | funcArg EQ_ aExpr
    ;

createLanguage
    : CREATE (OR REPLACE)? TRUSTED? PROCEDURAL? LANGUAGE name
    ( HANDLER handlerName (INLINE handlerName)? validatorClause?
    | LP_ transformElementList RP_)?
    ;

transformElementList
    : FROM SQL WITH FUNCTION functionWithArgtypes COMMA_ (TO | FROM) SQL WITH FUNCTION functionWithArgtypes
    | (TO | FROM) SQL WITH FUNCTION functionWithArgtypes
    ;

validatorClause
    : VALIDATOR handlerName	| NO VALIDATOR
    ;

createPolicy
    : CREATE POLICY name ON qualifiedName (AS identifier)?
      (FOR rowSecurityCmd)? (TO roleList)?
      (USING LP_ aExpr RP_)? (WITH CHECK LP_ aExpr RP_)?
    ;

createProcedure
    : CREATE (OR REPLACE)? PROCEDURE funcName funcArgsWithDefaults createfuncOptList
    ;

createPublication
    : CREATE PUBLICATION name publicationForTables?	(WITH definition)?
    ;

publicationForTables
    : FOR TABLE relationExprList | FOR ALL TABLES
    ;

createRule
    : CREATE (OR REPLACE)? RULE name AS ON event TO qualifiedName (WHERE aExpr)?
      DO (INSTEAD | ALSO)? ruleActionList
    ;

ruleActionList
    : NOTHING
    | ruleActionStmt
    | LP_ ruleActionMulti RP_
    ;

ruleActionStmt
    : select
    | insert
    | update
    | delete
    | notifyStmt
    ;

ruleActionMulti
    : ruleActionStmt? (SEMI_ ruleActionStmt?)*
    ;

createTrigger
    : CREATE TRIGGER name triggerActionTime triggerEvents ON qualifiedName triggerReferencing? triggerForSpec? triggerWhen? EXECUTE (FUNCTION | PROCEDURE) funcName LP_ triggerFuncArgs? RP_
    | CREATE CONSTRAINT TRIGGER (FROM qualifiedName)? constraintAttributeSpec FOR EACH ROW triggerWhen EXECUTE (FUNCTION | PROCEDURE) funcName LP_ triggerFuncArgs RP_
    ;

triggerEvents
    : triggerOneEvent (OR triggerOneEvent)*
    ;

triggerOneEvent
    : INSERT
    | DELETE
    | UPDATE
    | UPDATE OF columnList
    | TRUNCATE
    ;

triggerActionTime
    : BEFORE | AFTER | INSTEAD OF
    ;

triggerFuncArgs
    : triggerFuncArg (COMMA_ triggerFuncArg)*
    ;

triggerFuncArg
    : NUMBER_ | STRING_ | colLabel
    ;

triggerWhen
    : WHEN LP_ aExpr RP_
    ;

triggerForSpec
    : FOR EACH? (ROW | STATEMENT)
    ;

triggerReferencing
    : REFERENCING triggerTransitions
    ;

triggerTransitions
    : triggerTransition+
    ;

triggerTransition
    : transitionOldOrNew transitionRowOrTable AS? transitionRelName
    ;

transitionRelName
    : colId
    ;

transitionRowOrTable
    : TABLE | ROW
    ;

transitionOldOrNew
    : OLD | NEW
    ;

createSequence
    : CREATE tempOption? SEQUENCE ifNotExists? qualifiedName seqOptList?
    ;

tempOption
    : ((LOCAL | GLOBAL)? (TEMPORARY | TEMP)) | UNLOGGED
    ;

createServer
    : CREATE SERVER ifNotExists? name (TYPE STRING_)? foreignServerVersion? FOREIGN DATA WRAPPER name createGenericOptions
    ;

createStatistics
    : CREATE STATISTICS ifNotExists? anyName optNameList ON exprList FROM fromList
    ;

createSubscription
    : CREATE SUBSCRIPTION name CONNECTION STRING_ PUBLICATION publicationNameList (WITH definition)?
    ;

createTablespace
    : CREATE TABLESPACE name (OWNER roleSpec)? LOCATION STRING_ (WITH reloptions)?
    ;

createTextSearch
    : CREATE TEXT SEARCH (CONFIGURATION | DICTIONARY | PARSER | TEMPLATE) anyName definition
    ;

createTransform
    : CREATE (OR REPLACE)? TRANSFORM FOR typeName LANGUAGE name LP_ transformElementList RP_
    ;

createType
    : CREATE TYPE anyName createTypeClauses
    ;

createTypeClauses
    : definition?
    | AS LP_ tableFuncElementList? RP_
    | AS ENUM LP_ enumValList? RP_
    | AS RANGE definition
    ;

enumValList
    : STRING_ (COMMA_ STRING_)*
    ;

createUserMapping
    : CREATE USER MAPPING ifNotExists? FOR authIdent SERVER name createGenericOptions
    ;

discard
    : DISCARD (ALL | PLANS | SEQUENCES | TEMPORARY | TEMP)
    ;

dropAccessMethod
    : DROP ACCESS METHOD ifExists? name dropBehavior?
    ;

dropAggregate
    : DROP AGGREGATE ifExists? aggregateWithArgtypesList dropBehavior?
    ;

aggregateWithArgtypesList
    : aggregateWithArgtypes (COMMA_ aggregateWithArgtypes)*
    ;

dropCast
    : DROP CAST ifExists? LP_ typeName AS typeName RP_ dropBehavior?
    ;

dropCollation
    : DROP COLLATION ifExists? name dropBehavior?
    ;

dropConversion
    : DROP CONVERSION ifExists? name dropBehavior?
    ;

dropDomain
    : DROP DOMAIN ifExists? nameList dropBehavior?
    ;

dropEventTrigger
    : DROP EVENT TRIGGER ifExists? nameList dropBehavior?
    ;

dropExtension
    : DROP EXTENSION ifExists? nameList dropBehavior?
    ;

dropForeignDataWrapper
    : DROP FOREIGN DATA WRAPPER ifExists? nameList dropBehavior?
    ;

dropForeignTable
    : DROP FOREIGN TABLE ifExists? tableName (COMMA_ tableName)* dropBehavior?
    ;

dropFunction
    : DROP FUNCTION ifExists? functionWithArgtypesList dropBehavior?
    ;

functionWithArgtypesList
    : functionWithArgtypes (COMMA_ functionWithArgtypes)*
    ;

dropLanguage
    : DROP PROCEDURAL? LANGUAGE ifExists? name dropBehavior?
    ;

dropMaterializedView
    : DROP MATERIALIZED VIEW ifExists? anyNameList dropBehavior?
    ;

dropOperator
    : DROP OPERATOR ifExists? operatorWithArgtypesList dropBehavior?
    ;

operatorWithArgtypesList
    : operatorWithArgtypes (COMMA_ operatorWithArgtypes)*
    ;

dropOperatorClass
    : DROP OPERATOR CLASS ifExists? anyName USING name dropBehavior?
    ;

dropOperatorFamily
    : DROP OPERATOR FAMILY ifExists? anyName USING name dropBehavior?
    ;

dropOwned
    : DROP OWNED BY roleList dropBehavior?
    ;

dropPolicy
    : DROP POLICY ifExists? name ON tableName dropBehavior?
    ;

dropProcedure
    : DROP PROCEDURE ifExists? functionWithArgtypesList dropBehavior?
    ;

dropPublication
    : DROP PUBLICATION ifExists? anyNameList dropBehavior?
    ;

dropRoutine
    : DROP ROUTINE ifExists? functionWithArgtypesList dropBehavior?
    ;

dropRule
    : DROP RULE ifExists? name ON tableName dropBehavior?
    ;

dropSequence
    : DROP SEQUENCE ifExists? qualifiedNameList dropBehavior?
    ;

dropServer
    : DROP SERVER ifExists? qualifiedNameList dropBehavior?
    ;

dropStatistics
    : DROP STATISTICS ifExists? qualifiedNameList dropBehavior?
    ;

dropSubscription
    : DROP SUBSCRIPTION ifExists? qualifiedName dropBehavior?
    ;

dropTablespace
    : DROP TABLESPACE ifExists? qualifiedName
    ;

dropTextSearch
    : DROP TEXT SEARCH (CONFIGURATION | DICTIONARY | PARSER | TEMPLATE) ifExists? qualifiedName dropBehavior?
    ;

dropTransform
    : DROP TRANSFORM ifExists? FOR typeName LANGUAGE name dropBehavior?
    ;

dropTrigger
    : DROP TRIGGER ifExists? qualifiedName ON tableName dropBehavior?
    ;

dropType
    : DROP TYPE ifExists? anyNameList dropBehavior?
    ;

dropUserMapping
    : DROP USER MAPPING ifExists? FOR authIdent SERVER name
    ;

dropView
    : DROP VIEW ifExists? qualifiedNameList dropBehavior?
    ;

importForeignSchema
    : IMPORT FOREIGN SCHEMA name importQualification? FROM SERVER name INTO name createGenericOptions?
    ;

importQualification
    : importQualificationType LP_ relationExprList RP_
    ;

importQualificationType
    : LIMIT TO | EXCEPT
    ;

declare
    : DECLARE cursorName cursorOption CURSOR ((WITH | WITHOUT) HOLD)? FOR select
    ;

cursorOption
    : BINARY? (ASENSITIVE | INSENSITIVE)? (NO? SCROLL)?
    ;

open
    : OPEN cursorName (usingValueClause | usingSqlDescriptorClause)?
    ;

usingValueClause
    : USING value (COMMA_ value)*
    ;

value
    : aexprConst | hostVariable
    ;

usingSqlDescriptorClause
    : USING SQL DESCRIPTOR descriptorName
    ;

descriptorName
    : identifier | hostVariable
    ;

move
    : MOVE direction? (FROM | IN)? cursorName
    ;

fetch
    : FETCH direction? (FROM | IN)? cursorName
    ;

listen
    : LISTEN channelName
    ;

unlisten
    : UNLISTEN (channelName | ASTERISK_)
    ;

notifyStmt
    : NOTIFY colId (COMMA_ STRING_)?
    ;

direction
    : NEXT #next
    | PRIOR #prior
    | FIRST #first
    | LAST #last
    | ABSOLUTE signedIconst #absoluteCount
    | RELATIVE signedIconst #relativeCount
    | signedIconst #count
    | ALL #all
    | FORWARD #forward
    | FORWARD signedIconst #forwardCount
    | FORWARD ALL #forwardAll
    | BACKWARD #backward
    | BACKWARD signedIconst #backwardCount
    | BACKWARD ALL #backwardAll
    ;

prepare
    : PREPARE name prepTypeClause? AS preparableStmt
    ;

reindex
    : REINDEX reIndexClauses
    ;

reIndexClauses
    : reindexTargetType CONCURRENTLY? qualifiedName
    | reindexTargetMultitable CONCURRENTLY? name
    | LP_ reindexOptionList RP_ reindexTargetType CONCURRENTLY? qualifiedName
    | LP_ reindexOptionList RP_ reindexTargetMultitable CONCURRENTLY? name
    ;

reindexOptionList
    : reindexOptionElem (COMMA_ reindexOptionElem)*
    ;

reindexOptionElem
    : VERBOSE | CONCURRENTLY | TABLESPACE
    ;

reindexTargetMultitable
    : SCHEMA | SYSTEM | DATABASE
    ;

reindexTargetType
    : INDEX | TABLE
    ;

deallocate
    : DEALLOCATE PREPARE? (name | ALL)
    ;

prepTypeClause
    : LP_ typeList RP_
    ;

refreshMaterializedView
    : REFRESH MATERIALIZED VIEW CONCURRENTLY? qualifiedName withData?
    ;

alterForeignTable
    : ALTER FOREIGN TABLE ifExists? relationExpr alterForeignTableClauses
    ;

alterForeignTableClauses
    : RENAME TO name
    | RENAME COLUMN? name TO name
    | alterTableCmds
    | SET SCHEMA name
    ;

createOperator
    : CREATE OPERATOR anyOperator definition
    ;

createOperatorClass
    : CREATE OPERATOR CLASS anyName DEFAULT? FOR TYPE typeName USING name (FAMILY anyName)? AS opclassItemList
    ;

createOperatorFamily
    : CREATE OPERATOR FAMILY anyName USING name
    ;

createSchema
    : CREATE SCHEMA ifNotExists? createSchemaClauses
    ;

createSchemaClauses
    : colId? AUTHORIZATION roleSpec schemaEltList
    | colId schemaEltList
    ;

schemaEltList
    : schemaStmt*
    ;

schemaStmt
    : createTable | createIndex | createSequence | createTrigger | grant | createView
    ;

securityLabelStmt
    : SECURITY LABEL (FOR nonReservedWordOrSconst)? ON securityLabelClausces IS securityLabel
    ;

securityLabel
    : STRING_ | NULL
    ;

securityLabelClausces
    : objectTypeAnyName anyName
    | COLUMN anyName
    | (TYPE | DOMAIN) typeName
    | (AGGREGATE | FUNCTION) aggregateWithArgtypes
    | LARGE OBJECT numericOnly
    | (PROCEDURE | ROUTINE) functionWithArgtypes
    ;

grant
    : GRANT (privilegeClause | roleClause)
    ;

privilegeClause
    : privilegeTypes ON onObjectClause (FROM | TO) granteeList (WITH GRANT OPTION)?
    ;

roleClause
    : privilegeList (FROM | TO) roleList (WITH ADMIN OPTION)? (GRANTED BY roleSpec)?
    ;

privilegeTypes
    : privilegeType columnNames? (COMMA_ privilegeType columnNames?)*
    ;

onObjectClause
    : DATABASE nameList
    | SCHEMA nameList
    | DOMAIN anyNameList
    | FUNCTION functionWithArgtypesList
    | PROCEDURE functionWithArgtypesList
    | ROUTINE functionWithArgtypesList
    | LANGUAGE nameList
    | LARGE OBJECT numericOnlyList
    | TABLESPACE nameList
    | TYPE anyNameList
    | SEQUENCE qualifiedNameList
    | TABLE? privilegeLevel
    | FOREIGN DATA WRAPPER nameList
    | FOREIGN SERVER nameList
    | ALL TABLES IN SCHEMA nameList
    | ALL SEQUENCES IN SCHEMA nameList
    | ALL FUNCTIONS IN SCHEMA nameList
    | ALL PROCEDURES IN SCHEMA nameList
    | ALL ROUTINES IN SCHEMA nameList
    ;

numericOnlyList
    : numericOnly (COMMA_ numericOnly)*
    ;

privilegeLevel
    : ASTERISK_ | ASTERISK_ DOT_ASTERISK_ | identifier DOT_ASTERISK_ | tableNames | schemaName DOT_ routineName
    ;

routineName
    : identifier
    ;

privilegeType
    : SELECT
    | INSERT
    | UPDATE
    | DELETE
    | TRUNCATE
    | REFERENCES
    | TRIGGER
    | CREATE
    | CONNECT
    | TEMPORARY
    | TEMP
    | EXECUTE
    | USAGE
    | ALL PRIVILEGES?
    ;

alterSchema
    : ALTER SCHEMA name (RENAME TO name | OWNER TO roleSpec)
    ;

dropSchema
    : DROP SCHEMA ifExists? nameList dropBehavior?
    ;
