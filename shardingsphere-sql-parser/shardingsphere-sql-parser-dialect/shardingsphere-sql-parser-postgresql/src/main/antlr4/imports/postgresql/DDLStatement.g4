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

grammar DDLStatement;

import Symbol, Keyword, PostgreSQLKeyword, Literals, BaseRule,DMLStatement;

createTable
    : CREATE createTableSpecification_ TABLE tableNotExistClause_ tableName
      createDefinitionClause
      (OF anyName (LP_ typedTableElementList RP_)?)?
      (PARTITION OF qualifiedName (LP_ typedTableElementList RP_)? partitionBoundSpec)?
      inheritClause_ partitionSpec? tableAccessMethodClause withOption? onCommitOption? tableSpace?
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

tableSpace
    : TABLESPACE name
    ;

onCommitOption
    : ON COMMIT (DROP | DELETE ROWS | PRESERVE ROWS)
    ;

withOption
    : WITH reloptions | WITHOUT OIDS
    ;

tableAccessMethodClause
    : (USING accessMethod)?
    ;

accessMethod
    : colId
    ;

createIndex
    : CREATE createIndexSpecification_ INDEX concurrentlyClause_ (indexNotExistClause_ indexName)? ON onlyClause_ tableName
      accessMethodClause? LP_ indexParams RP_ include? (WITH reloptions)? tableSpace? whereClause?
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
    : CREATE DATABASE name optWith_ createDatabaseSpecification_*
    ;

createView
    : CREATE (OR REPLACCE)? (TMP | TMPORARY)? (RECURSIVE)? VIEW name
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
    : DROP DATABASE (IF EXISTS)? name
    ;

createDatabaseSpecification_
    :  createdbOptName (EQ_)? (signedIconst | optBooleanOrString | DEFAULT)
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

optWith_
    : (WITH)?
    ;

alterTable
    : ALTER TABLE
    ( tableExistClause_ onlyClause_ tableNameClause alterDefinitionClause
    | ALL IN TABLESPACE name (OWNED BY roleList)? SET TABLESPACE name (NOWAIT)?)
    ;

alterIndex
    : ALTER INDEX indexExistClause_ indexName alterIndexDefinitionClause_
    ;

dropTable
    : DROP TABLE tableExistClause_ tableNames dropTableOpt?
    ;

dropTableOpt
    : CASCADE | RESTRICT
    ;

dropIndex
    : DROP INDEX concurrentlyClause_ indexExistClause_ indexNames dropIndexOpt?
    ;

dropIndexOpt
    : CASCADE | RESTRICT
    ;

truncateTable
    : TRUNCATE TABLE? onlyClause_ tableNamesClause restartSeqs? dropTableOpt?
    ;

restartSeqs
    : CONTINUE IDENTITY
    | RESTART IDENTITY
    ;

createTableSpecification_
    : ((GLOBAL | LOCAL)? (TEMPORARY | TEMP) | UNLOGGED)?
    ;

tableNotExistClause_
    : (IF NOT EXISTS)?
    ;

createDefinitionClause
    : LP_ (createDefinition (COMMA_ createDefinition)*)? RP_
    ;

createDefinition
    : columnDefinition | tableConstraint | LIKE tableName likeOption*
    ;

columnDefinition
    : columnName dataType collateClause_? columnConstraint*
    ;

columnConstraint
    : constraintClause? columnConstraintOption constraintOptionalParam
    ;

constraintClause
    : CONSTRAINT ignoredIdentifier_
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
    : (USING INDEX TABLESPACE ignoredIdentifier_)?
    | INCLUDE columnNames
    | WITH
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
    | EXCLUDE (USING ignoredIdentifier_)?
    | FOREIGN KEY columnNames REFERENCES tableName columnNames? (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)? (ON (DELETE | UPDATE) action)*
    ;

inheritClause_
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
    ;

partStrategy
    : identifier
    | unreservedWord
    ;

createIndexSpecification_
    : UNIQUE?
    ;

concurrentlyClause_
    : CONCURRENTLY?
    ;

indexNotExistClause_
    : (IF NOT EXISTS)?
    ;

onlyClause_
    : ONLY?
    ;

tableExistClause_
    : (IF EXISTS)?
    ;

asteriskClause_
    : ASTERISK_?
    ;

alterDefinitionClause
    : alterTableActions
    | renameColumnSpecification
    | renameConstraint
    | renameTableSpecification_
    | SET SCHEMA name
    | partitionCmd
    ;

partitionCmd
    : ATTACH PARTITION qualifiedName partitionBoundSpec
    | DETACH PARTITION qualifiedName
    ;

roleList
    : roleSpec (COMMA_ roleSpec)*
    ;

alterIndexDefinitionClause_
    : renameIndexSpecification | alterIndexDependsOnExtension | alterIndexSetTableSpace | alterTableCmds | indexPartitionCmd
    ;

indexPartitionCmd
    : ATTACH PARTITION qualifiedName
    ;

renameIndexSpecification
    : RENAME TO indexName
    ;

alterIndexDependsOnExtension
    : DEPENDS ON EXTENSION ignoredIdentifier_
    ;

alterIndexSetTableSpace
    : ALL IN TABLESPACE indexName (OWNED BY ignoredIdentifiers_)? SET TABLESPACE name (NOWAIT)?
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
    | ALTER CONSTRAINT ignoredIdentifier_ constraintOptionalParam
    | VALIDATE CONSTRAINT ignoredIdentifier_
    | DROP CONSTRAINT indexExistClause_ ignoredIdentifier_ (RESTRICT | CASCADE)?
    | (DISABLE | ENABLE) TRIGGER (ignoredIdentifier_ | ALL | USER)?
    | ENABLE (REPLICA | ALWAYS) TRIGGER ignoredIdentifier_
    | (DISABLE | ENABLE) RULE ignoredIdentifier_
    | ENABLE (REPLICA | ALWAYS) RULE ignoredIdentifier_
    | (DISABLE | ENABLE | (NO? FORCE)) ROW LEVEL SECURITY
    | CLUSTER ON indexName
    | SET WITHOUT CLUSTER
    | SET (WITH | WITHOUT) OIDS
    | SET TABLESPACE ignoredIdentifier_
    | SET (LOGGED | UNLOGGED)
    | SET LP_ storageParameterWithValue (COMMA_ storageParameterWithValue)* RP_
    | RESET LP_ storageParameter (COMMA_ storageParameter)* RP_
    | INHERIT tableName
    | NO INHERIT tableName
    | OF dataTypeName
    | NOT OF
    | OWNER TO (ignoredIdentifier_ | CURRENT_USER | SESSION_USER)
    | REPLICA IDENTITY (DEFAULT | (USING INDEX indexName) | FULL | NOTHING)
    ;

addColumnSpecification
    : ADD COLUMN? (IF NOT EXISTS)? columnDefinition
    ;

dropColumnSpecification
    : DROP COLUMN? columnExistClause_ columnName (RESTRICT | CASCADE)?
    ;

columnExistClause_
    : (IF EXISTS)?
    ;
    
modifyColumnSpecification
    : modifyColumn (SET DATA)? TYPE dataType collateClause_? (USING aExpr)?
    | modifyColumn SET DEFAULT aExpr
    | modifyColumn DROP DEFAULT
    | modifyColumn (SET | DROP) NOT NULL
    | modifyColumn ADD GENERATED (ALWAYS | (BY DEFAULT)) AS IDENTITY (LP_ sequenceOptions RP_)?
    | modifyColumn alterColumnSetOption alterColumnSetOption*
    | modifyColumn DROP IDENTITY columnExistClause_
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
    : (CONSTRAINT ignoredIdentifier_)? (UNIQUE | primaryKey) USING INDEX indexName constraintOptionalParam
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
    : RENAME CONSTRAINT ignoredIdentifier_ TO ignoredIdentifier_
    ;

renameTableSpecification_
    : RENAME TO identifier
    ;

indexExistClause_
    : (IF EXISTS)?
    ;

indexNames
    : indexName (COMMA_ indexName)*
    ;

alterDatabase
    : ALTER DATABASE databaseName alterDatabaseClause
    ;

alterDatabaseClause
    : (WITH)? createdbOptItems?
    | RENAME TO databaseName
    | OWNER TO roleSpec
    | SET TABLESPACE name
    | setResetClause
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
    : transactionModeItem ((COMMA_)? transactionModeItem)*
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

documentOrContent
    : DOCUMENT
    | CONTENT
    ;

createdbOptItems
    : createdbOptItem+
    ;

createdbOptItem
    : createdbOptName (EQ_)? signedIconst
    | createdbOptName (EQ_)? optBooleanOrString
    | createdbOptName (EQ_)? DEFAULT
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

alterTableCmds
    : alterTableCmd (COMMA_ alterTableCmd)*
    ;

alterTableCmd
    : ADD COLUMN? (IF NOT EXISTS)? columnDef
    | ALTER column? colId alterColumnDefault
    | ALTER column? colId DROP NOT NULL
    | ALTER column? colId SET NOT NULL
    | ALTER column? colId SET STATISTICS signedIconst
    | ALTER column? NUMBER_ SET STATISTICS signedIconst
    | ALTER column? colId SET reloptions
    | ALTER column? colId RESET reloptions
    | ALTER column? colId SET STORAGE colId
    | ALTER column? colId ADD GENERATED generatedWhen AS IDENTITY optParenthesizedSeqOptList
    | ALTER column? colId alterIdentityColumnOptionList
    | ALTER column? colId DROP IDENTITY
    | ALTER column? colId DROP IDENTITY IF EXISTS
    | DROP column? IF EXISTS colId dropBehavior?
    | DROP column? colId dropBehavior?
    | ALTER column? colId setData? TYPE typeName collateClause? alterUsing?
    | ALTER column? colId alterGenericOptions
    | ADD tableConstraint
    | ALTER CONSTRAINT name constraintAttributeSpec
    | VALIDATE CONSTRAINT name
    | DROP CONSTRAINT IF EXISTS name dropBehavior?
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
    | RESTART (WITH)? numericOnly
    | SET seqOptElem
    | SET GENERATED generatedWhen
    ;

alterColumnDefault
    : SET DEFAULT aExpr
    | DROP DEFAULT
    ;

column
    : COLUMN
    ;

alterOperator
    : ALTER OPERATOR alterOperatorClauses
    ;

alterOperatorClauses
    : operatorWithArgtypes SET SCHEMA name
    | operatorWithArgtypes SET LP_ operatorDefList RP_
    | operatorWithArgtypes OWNER TO roleSpec
    ;

operatorDefList
    : operatorDefElem (COMMA_ operatorDefElem)*
    ;

operatorDefElem
    : colLabel EQ_ (NONE | operatorDefArg)
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
    : ALTER DEFAULT PRIVILEGES (defACLOptionList)? defACLAction
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
    : SELECT (optColumnList)?
    | REFERENCES (optColumnList)?
    | CREATE (optColumnList)?
    | colId (optColumnList)?
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
    | anyName ADD tableConstraint
    | anyName DROP CONSTRAINT (IF EXISTS) name dropBehavior?
    | anyName VALIDATE CONSTRAINT name
    | anyName RENAME CONSTRAINT constraintName TO constraintName
    | anyName OWNER TO roleSpec
    | anyName RENAME TO anyName
    | anyName SET SCHEMA name
    | anyName alterColumnDefault
    ;

constraintName
    : colId
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
    : ALTER PROCEDURAL? LANGUAGE (colId RENAME TO colId | OWNER TO (ignoredIdentifier_ | CURRENT_USER | SESSION_USER))
    ;

alterLargeObject
    : ALTER LARGE OBJECT numericOnly OWNER TO (ignoredIdentifier_ | CURRENT_USER | SESSION_USER)
    ;

alterMaterializedView
    : ALTER MATERIALIZED VIEW alterMaterializedViewClauses
    ;

alterMaterializedViewClauses
    : (IF EXISTS)? qualifiedName alterTableCmds
    | qualifiedName DEPENDS ON EXTENSION name
    | (IF EXISTS)? qualifiedName RENAME (COLUMN)? columnName TO columnName
    | (IF EXISTS)? qualifiedName RENAME TO qualifiedName
    | (IF EXISTS)? qualifiedName SET SCHEMA schemaName
    | ALL IN TABLESPACE name (OWNED BY roleList) SET TABLESPACE name NOWAIT?
    ;
