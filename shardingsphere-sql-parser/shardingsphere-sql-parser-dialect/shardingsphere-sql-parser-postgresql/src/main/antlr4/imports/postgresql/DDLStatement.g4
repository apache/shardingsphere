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

import Symbol, Keyword, PostgreSQLKeyword, Literals, BaseRule;

createTable
    : CREATE createTableSpecification_ TABLE tableNotExistClause_ tableName createDefinitionClause inheritClause_
    ;

createIndex
    : CREATE createIndexSpecification_ INDEX concurrentlyClause_ (indexNotExistClause_ indexName)? ON onlyClause_ tableName 
    ;

alterTable
    : ALTER TABLE tableExistClause_ onlyClause_ tableNameClause alterDefinitionClause
    ;

alterIndex
    : ALTER INDEX indexExistClause_ indexName alterIndexDefinitionClause_
    ;

dropTable
    : DROP TABLE tableExistClause_ tableNames
    ;

dropIndex
    : DROP INDEX concurrentlyClause_ indexExistClause_ indexNames
    ;
    
truncateTable
    : TRUNCATE TABLE? onlyClause_ tableNamesClause
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
    : alterTableActions | renameColumnSpecification | renameConstraint | renameTableSpecification_
    ;

alterIndexDefinitionClause_
    : renameIndexSpecification | alterIndexDependsOnExtension | alterIndexSetTableSpace
    ;

renameIndexSpecification
    : RENAME TO indexName
    ;

alterIndexDependsOnExtension
    : ALTER INDEX indexName DEPENDS ON EXTENSION ignoredIdentifier_
    ;

alterIndexSetTableSpace
    : ALTER INDEX ALL IN TABLESPACE indexName (OWNED BY ignoredIdentifiers_)?
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

alterAggregate
    : ALTER AGGREGATE aggregateName LP_ aggregateSignature RP_ alterAggregateDefinitionClause
    ;

aggregateSignature
    : ASTERISK_
    | aggregateArgsList
    | (aggregateArgsList)? ORDER BY aggregateArgsList
    ;

alterAggregateDefinitionClause
    : RENAME TO aggregateName
    | OWNER TO  (owner | CURRENT_USER | SESSION_USER)
    | SET SCHEMA schemaName
    ;

aggregateArgsList
    : aggregateArgs (COMMA_ aggregateArgs)*
    ;

aggregateArgs
    : argMode? argName? argType
    ;

argType
    : dataTypeName
    ;

argName
    : name
    ;

argMode
    : IN
    | VARIADIC
    ;

alterCollation
    : ALTER COLLATION anyName alterCollationClause
    ;

alterCollationClause
    : REFRESH VERSION
    | RENAME TO anyName
    | OWNER TO (ignoredIdentifier_ | CURRENT_USER | SESSION_USER)
    | SET SCHEMA schemaName
    ;

alterConversion
    : ALTER CONVERSION anyName alterConversionClause
    ;

alterConversionClause
    : RENAME TO anyName
    | OWNER TO (ignoredIdentifier_ | CURRENT_USER | SESSION_USER)
    | SET SCHEMA schemaName
    ;

alterDatabase
    : ALTER DATABASE databaseName alterDatabaseClause
    ;

alterDatabaseClause
    : alterDatabaseConnectionOptions
    | RENAME TO databaseName
    | OWNER TO (ignoredIdentifier_ | CURRENT_USER | SESSION_USER)
    | SET TABLESPACE name
    | SET varName (TO | EQ_) (varList | DEFAULT)
    | SET varName FROM CURRENT
    | RESET varName
    | RESET ALL
    ;

alterDatabaseConnectionOptions
    : (WITH)? dbOptionList
    ;

dbOptionList
    : dbOptionItem
    | dbOptionList dbOptionItem
    ;

dbOptionItem
    : ALLOW_CONNECTIONS allowConnections
    | CONNECTION LIMIT connlimit
    | IS_TEMPLATE istemplate
    ;

istemplate
    : optBoolean
    ;

connlimit
    : INT_
    ;

allowConnections
    : optBoolean
    ;

alterDefaultPrivileges
    : ALTER DEFAULT PRIVILEGES (defACLOptionList)? defACLAction
    ;

defACLAction
    : GRANT privileges ON defaclPrivilegeTarget TO granteeList optGrantGrantOption?
    | REVOKE privileges ON defaclPrivilegeTarget FROM granteeList optDropBehavior?
    | REVOKE GRANT OPTION FOR privileges ON defaclPrivilegeTarget FROM granteeList optDropBehavior?
    ;

optDropBehavior
    : CASCADE
    | RESTRICT
    ;

optGrantGrantOption
    : (WITH GRANT OPTION)?
    ;

granteeList
    : grantee
    | granteeList COMMA_ grantee
    ;

grantee
    : GROUP? roleSpec
    ;

defaclPrivilegeTarget
    :TABLES
    | FUNCTIONS
    | ROUTINES
    | SEQUENCES
    | TYPES_P
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
    : privilege
    | privilegeList COMMA_ privilege
    ;

privilege
    : SELECT (optColumnList)?
    | REFERENCES (optColumnList)?
    | CREATE (optColumnList)?
    | colId (optColumnList)?
    ;

optColumnList
    : LP_ columnList RP_
    ;

defACLOptionList
    : defACLOptionList defACLOption
    | defACLOption
    ;

defACLOption
    : IN SCHEMA schemaNameList
    | FOR (ROLE | USER) roleList
    ;

roleList
    : roleSpec
    | roleList COMMA_ roleSpec
    ;

schemaNameList
    : nameList
    ;
//TODO
alterDomain
    : ALTER DOMAIN anyName alterDomainClause
    ;

alterDomainClause
    : (SET DEFAULT aExpr | DROP DEFAULT)
    | (SET | DROP) NOT NULL
    | ADD domainConstraint (NOT VALID)?
    | DROP CONSTRAINT (IF EXISTS)? constraintName (RESTRICT | CASCADE)?
    | RENAME CONSTRAINT constraintName TO constraintName
    | VALIDATE CONSTRAINT constraintName
    | OWNER TO (ignoredIdentifier_ | CURRENT_USER | SESSION_USER)
    | RENAME TO anyName
    | SET SCHEMA name
    ;

domainConstraint
    : CONSTRAINT colId constraintElem
    | constraintElem
    ;

constraintElem
    : CHECK LP_ aExpr RP_ constraintAttributeSpec?
    | UNIQUE LP_ columnList RP_ opt_c_include? opt_definition? optConsTableSpace? constraintAttributeSpec?
    | UNIQUE existingIndex constraintAttributeSpec?
    | PRIMARY KEY LP_ columnList RP_ opt_c_include? opt_definition? optConsTableSpace? constraintAttributeSpec?
    | PRIMARY KEY existingIndex constraintAttributeSpec?
//    | EXCLUDE access_method_clause? LP_ exclusionConstraintList RP_ opt_c_include? opt_definition? optConsTableSpace?  exclusionWhereClause constraintAttributeSpec?
//    | FOREIGN KEY LP_ columnList RP_ REFERENCES qualified_name opt_column_list key_match key_actions ConstraintAttributeSpec
    ;

exclusionConstraintList
    :
    ;

access_method_clause
    : USING accessMethod
    ;

existingIndex
    : USING INDEX indexName
    ;

optConsTableSpace
    : USING INDEX TABLESPACE name
    ;

opt_definition
    : WITH definition
    ;

definition
    : LP_ def_list RP_
    ;

def_list
    : def_elem
    | def_list COMMA_ def_elem
    ;

def_elem
    : colLable EQ_ defArg
    | colLable
    ;

opt_c_include
    : INCLUDE LP_ columnList RP_
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

constraintName
    : colId
    ;

alterEventTrigger
    : ALTER EVENT TRIGGER tiggerName alterEventTriggerClause
    ;

alterEventTriggerClause
    : DISABLE
    | ENABLE (REPLICA | ALWAYS)
    | OWNER TO (ignoredIdentifier_ | CURRENT_USER | SESSION_USER)
    | RENAME TO tiggerName
    ;

tiggerName
    : colId
    ;

//TODO
alterExtension
    : ALTER EXTENSION colId
    ;

alterForeignDataWrapper
    : ALTER FOREIGN DATA WRAPPER colId alterForeignDataWrapperClauses
    ;

alterForeignDataWrapperClauses
    : opt_fdw_options alter_generic_options
    | fdw_options
    ;

alter_generic_options
    : OPTIONS LP_ alter_generic_option_list RP_
    ;

alter_generic_option_list
    : alter_generic_option_elem
    | alter_generic_option_list COMMA_ alter_generic_option_elem
    ;

alter_generic_option_elem
    : generic_option_elem
    | SET generic_option_elem
    | ADD generic_option_elem
    | DROP generic_option_name
    ;

generic_option_name
    : colLable
    ;

generic_option_elem
    : generic_option_name generic_option_arg
    ;

generic_option_arg
    : aexprConst
    ;

fdw_options
    : fdw_option+
    ;

fdw_option
    : HANDLER handler_name
    | NO HANDLER
    | VALIDATOR handler_name
    | NO VALIDATOR
    ;

handler_name
    : anyName
    ;

opt_fdw_options
    : fdw_options?
    ;

alterForeignTable
    : ALTER FOREIGN TABLE (IF EXISTS)? alterForeignTableClauses
    ;

alterForeignTableClauses
    : (ONLY)? tableNameClause alterForeignTableActions
    | (ONLY)? tableNameClause RENAME COLUMN? columnName TO columnName
    | tableName RENAME TO tableName
    | SET SCHEMA schemaName
    ;

alterForeignTableActions
    : alterTableActions
    | OPTIONS LP_ alter_generic_option_list RP_
    ;

alterFunction
    : ALTER FUNCTION function_with_argtypes alterFunctionClauses
    ;

alterFunctionClauses
    : altefunc_opt_list opt_restrict
    | RENAME TO funcName
    | DEPENDS ON EXTENSION colId
    | SET SCHEMA schemaName
    | OWNER TO roleSpec
    ;

opt_restrict
    : RESTRICT?
    ;

altefunc_opt_list
    : common_func_opt_item
    | altefunc_opt_list common_func_opt_item
    ;

common_func_opt_item
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
    : SET set_rest_more
    | variableResetStmt
    ;

variableResetStmt
    : RESET reset_rest
    ;

reset_rest
    : generic_reset
    | TIME ZONE
    | TRANSACTION ISOLATION LEVEL
    | SESSION AUTHORIZATION
    ;

generic_reset
    : varName
    | ALL
    ;


set_rest_more
    : generic_set
    | varName FROM CURRENT
    | TIME ZONE zoneValue
    | CATALOG STRING_
    | SCHEMA STRING_
    | NAMES opt_encoding?
    | ROLE nonReservedWord | STRING_
    | SESSION AUTHORIZATION nonReservedWord | STRING_
    | SESSION AUTHORIZATION DEFAULT
    | XML OPTION document_or_content
    | TRANSACTION SNAPSHOT STRING_
    ;

document_or_content
    : DOCUMENT
    | CONTENT
    ;

opt_encoding
    : STRING_
    | DEFAULT
    ;

generic_set
    : varName (EQ_|TO) (varList | DEFAULT)
    ;

function_with_argtypes
    : funcName funcArgs
    | typeFuncNameKeyword
    | colId
    | colId indirection
    ;

funcArgs
    : LP_ funcArgsList RP_
    | LP_ RP_
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
    : (IF EXISTS)? qualifiedName alter_table_cmds
    | qualifiedName DEPENDS ON EXTENSION name
    | (IF EXISTS)? qualifiedName RENAME (COLUMN)? columnName TO columnName
    | (IF EXISTS)? qualifiedName RENAME TO qualifiedName
    | (IF EXISTS)? qualifiedName SET SCHEMA schemaName
    | ALL IN TABLESPACE name (OWNED BY roleList) SET TABLESPACE name NOWAIT?
    ;

alter_table_cmds
    : alter_table_cmd
    | alter_table_cmds COMMA_ alter_table_cmd
    ;

alter_table_cmd
    : ADD COLUMN? (IF NOT EXIST)? columnDef
    | ALTER opt_column ColId alter_column_default
    | ALTER opt_column ColId DROP NOT NULL
    | ALTER opt_column ColId SET NOT NULL
    | ALTER opt_column ColId SET STATISTICS SignedIconst
    | ALTER opt_column Iconst SET STATISTICS SignedIconst
    | ALTER opt_column ColId SET reloptions
    | ALTER opt_column ColId RESET reloptions
    | ALTER opt_column ColId SET STORAGE ColId
    | ALTER opt_column ColId ADD_P GENERATED generated_when AS IDENTITY_P OptParenthesizedSeqOptList
    | ALTER opt_column ColId alter_identity_column_option_list
    | ALTER opt_column ColId DROP IDENTITY_P
    | ALTER opt_column ColId DROP IDENTITY_P IF EXISTS
    | DROP opt_column IF_P EXISTS ColId opt_drop_behavior
    | DROP opt_column ColId opt_drop_behavior
    | ALTER opt_column ColId opt_set_data TYPE Typename opt_collate_clause alter_using
    | ALTER opt_column ColId alter_generic_options
    | ADD_P TableConstraint
    | ALTER CONSTRAINT name ConstraintAttributeSpec
    | VALIDATE CONSTRAINT name
    | DROP CONSTRAINT IF EXISTS name opt_drop_behavior
    | DROP CONSTRAINT name opt_drop_behavior
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
    | REPLICA IDENTITY replica_identity
    | ENABLE_P ROW LEVEL SECURITY
    | DISABLE_P ROW LEVEL SECURITY
    | FORCE ROW LEVEL SECURITY
    | NO FORCE ROW LEVEL SECURITY
    | alter_generic_options
    ;

