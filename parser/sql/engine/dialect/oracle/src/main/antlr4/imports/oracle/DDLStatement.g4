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

import DMLStatement, DCLStatement;

createView
    : CREATE (OR REPLACE)? (NO? FORCE)? (EDITIONING | EDITIONABLE EDITIONING? | NONEDITIONABLE)? VIEW viewName createSharingClause
    ( LP_ ((alias (VISIBLE | INVISIBLE)? inlineConstraint* | outOfLineConstraint) (COMMA_ (alias (VISIBLE | INVISIBLE)? inlineConstraint* | outOfLineConstraint))*) RP_
    | objectViewClause | xmlTypeViewClause)?
    ( DEFAULT COLLATION collationName)? (BEQUEATH (CURRENT_USER | DEFINER))? AS select subqueryRestrictionClause?
    ( CONTAINER_MAP | CONTAINERS_DEFAULT)?
    ;

createTable
    : CREATE createTableSpecification TABLE tableName createSharingClause createDefinitionClause memOptimizeClause createParentClause createQueueClause
    ;

createEdition
    : CREATE EDITION editionName (AS CHILD OF editionName)?
    ;

createIndex
    : CREATE createIndexSpecification INDEX indexName ON createIndexDefinitionClause usableSpecification? invalidationSpecification?
    ;

createOperator
    : CREATE OPERATOR operatorName bindDefinitionClause
    ;

bindDefinitionClause
    : BINDING bindDefinition (COMMA_ bindDefinition)*
    ;

bindDefinition
    : LP_ dataType (COMMA_ dataType)* RP_ returnDefinition
    ;

returnDefinition
    : RETURN returnTypeDef operatorBindClause? implementsOperator
    ;

implementsOperator
    : USING functionNameDef
    ;

operatorBindClause
    : withIndexContextDef COMMA_ scanContextDef primaryBindingDef?
    ;

primaryBindingDef
    : COMPUTE ANCILLARY DATA
    ;

scanContextDef
    : SCAN CONTEXT identifier
    ;

withIndexContextDef
   : WITH INDEX CONTEXT
   ;

returnTypeDef
    : dataType
    ;

functionNameDef
    : identifier (DOT_ functionName)?
    ;

createType
    : CREATE (OR REPLACE)? (EDITIONABLE | NONEDITIONABLE)? TYPE plsqlTypeSource
    ;

plsqlTypeSource
    : typeName (objectBaseTypeDef | objectSubTypeDef)
    ;

objectBaseTypeDef
    : (IS | AS) (objectTypeDef | varrayTypeSpec | nestedTableTypeSpec)
    ;

objectTypeDef
    : OBJECT LP_ dataTypeDefinition (COMMA_ dataTypeDefinition)* RP_ finalClause? instantiableClause? persistableClause?
    ;

objectViewClause
    : OF typeName (WITH OBJECT (IDENTIFIER | ID) (DEFAULT | LP_ attribute (COMMA_ attribute)* RP_) | UNDER (schemaName DOT_)? superview)
    ( LP_ outOfLineConstraint | attribute inlineConstraint* (COMMA_ outOfLineConstraint | attribute inlineConstraint*)* RP_)?
    ;

finalClause
    : NOT? FINAL
    ;

instantiableClause
    : NOT? INSTANTIABLE
    ;

persistableClause
    : NOT? PERSISTABLE
    ;

varrayTypeSpec
    : VARRAY (LP_ INTEGER_ RP_)? OF typeSpec
    ;

nestedTableTypeSpec
    : TABLE OF typeSpec
    ;

typeSpec
    : ((LP_ dataType RP_) | dataType) (NOT NULL)? persistableClause?
    ;

dataTypeDefinition
    : name dataType
    ;

objectSubTypeDef
    : UNDER typeName LP_ dataTypeDefinition (COMMA_ dataTypeDefinition)* RP_ finalClause? instantiableClause?
    ;

alterTable
    : ALTER TABLE tableName memOptimizeClause alterDefinitionClause enableDisableClauses?
    ;

alterIndex
    : ALTER INDEX indexName alterIndexInformationClause
    ;

alterTrigger
    : ALTER TRIGGER triggerName (
    | triggerCompileClause
    | ( ENABLE | DISABLE)
    | RENAME TO name
    | (EDITIONABLE | NONEDITIONABLE)
    )
    ;

triggerCompileClause
    : COMPILE (
    (DEBUG compilerParametersClause* (REUSE SETTINGS)?)
    | (compilerParametersClause+ (REUSE SETTINGS)?)
    | (REUSE SETTINGS)
    | (DEBUG REUSE SETTINGS)
    )?
    ;

compilerParametersClause
    : parameterName EQ_ parameterValue
    ;

dropContext
    : DROP CONTEXT contextName;

dropTable
    : DROP TABLE tableName (CASCADE CONSTRAINTS)? (PURGE)?
    ;

dropTableSpace
    : DROP TABLESPACE tablespaceName (INCLUDING CONTENTS ((AND | KEEP) DATAFILES)? (CASCADE CONSTRAINTS)? )?
    ;

dropPackage
    : DROP PACKAGE BODY? packageName
    ;

dropTrigger
    : DROP TRIGGER triggerName
    ;

dropIndex
    : DROP INDEX indexName ONLINE? FORCE? invalidationSpecification? (ON tableName)?
    ;

dropView
    : DROP VIEW viewName (CASCADE CONSTRAINTS)?
    ;

dropEdition
    : DROP EDITION editionName CASCADE?
    ;

dropOutline
    : DROP OUTLINE outlineName
    ;

dropCluster
    : DROP CLUSTER (schemaName DOT_)? clusterName (INCLUDING TABLES (CASCADE CONSTRAINTS)?)?
    ;

alterOutline
    : ALTER OUTLINE (PUBLIC | PRIVATE)? outlineName
    ( REBUILD
    | RENAME TO outlineName
    | CHANGE CATEGORY TO categoryName
    | (ENABLE | DISABLE)
    )+
    ;

truncateTable
    : TRUNCATE TABLE tableName materializedViewLogClause? dropReuseClause? CASCADE?
    ;

createTableSpecification
    : ((GLOBAL | PRIVATE) TEMPORARY | SHARDED | DUPLICATED | IMMUTABLE? BLOCKCHAIN | IMMUTABLE)?
    ;

tablespaceClauseWithParen
    : LP_ tablespaceClause RP_
    ;

tablespaceClause
    : TABLESPACE ignoredIdentifier
    ;

createSharingClause
    : (SHARING EQ_ (METADATA | DATA | EXTENDED DATA | NONE))?
    ;

createDefinitionClause
    : createRelationalTableClause | createObjectTableClause | createXMLTypeTableClause
    ;

createXMLTypeTableClause
    : OF? XMLTYPE
      (LP_ (objectProperties) RP_)?
      (XMLTYPE xmlTypeStorageClause)?
      (xmlSchemaSpecClause)?
      (xmlTypeVirtualColumnsClause)?
      (ON COMMIT (DELETE | PRESERVE) ROWS)?
      (oidClause)?
      (oidIndexClause)?
      (physicalProperties)?
      (tableProperties)?
    ;

xmlTypeStorageClause
    : STORE
      (AS ( OBJECT RELATIONAL | ((SECUREFILE | BASICFILE)? (CLOB | BINARY XML) (lobSegname (LP_ lobParameters RP_)? | (LP_ lobParameters RP_))?)))
      | (ALL VARRAYS AS (LOBS | TABLES ))
    ;

xmlSchemaSpecClause
    : (XMLSCHEMA xmlSchemaURLName)? ELEMENT (elementName | xmlSchemaURLName POUND_ elementName)?
      (STORE ALL VARRAYS AS (LOBS | TABLES))?
      ((ALLOW | DISALLOW) NONSCHEMA)?
      ((ALLOW | DISALLOW) ANYSCHEMA)?
    ;

xmlTypeVirtualColumnsClause
    : VIRTUAL COLUMNS LP_ (columnName AS LP_ expr RP_ (COMMA_ columnName AS LP_ expr RP_)+) RP_
    ;

xmlTypeViewClause
    : OF XMLTYPE xmlSchemaSpec? WITH OBJECT (IDENTIFIER | ID) (DEFAULT | LP_ expr (COMMA_ expr)* RP_)
    ;

xmlSchemaSpec
    : (XMLSCHEMA xmlSchemaURLName)? ELEMENT (elementName | xmlSchemaURLName POUND_ elementName)
    ( STORE ALL VARRAYS AS (LOBS | TABLES))? ((ALLOW | DISALLOW) NONSCHEMA)? ((ALLOW | DISALLOW) ANYSCHEMA)?
    ;

oidClause
    : OBJECT IDENTIFIER IS (SYSTEM GENERATED | PRIMARY KEY)
    ;

oidIndexClause
    : OIDINDEX indexName? LP_ (physicalAttributesClause | TABLESPACE tablespaceName)* RP_
    ;

createRelationalTableClause
    : (LP_ relationalProperties RP_)? immutableTableClauses? blockchainTableClauses? collationClause? commitClause? parallelClause? physicalProperties? tableProperties?
    ;

createParentClause
    : (PARENT tableName)?
    ;

createObjectTableClause
    : OF objectName objectTableSubstitution?
    (LP_ objectProperties RP_)? (ON COMMIT (DELETE | PRESERVE) ROWS)?
    oidClause? oidIndexClause? physicalProperties? tableProperties?
    ;

createQueueClause
    : (USAGE QUEUE)?
    ;

relationalProperties
    : relationalProperty (COMMA_ relationalProperty)*
    ;

immutableTableClauses
    : immutableTableNoDropClause? immutableTableNoDeleteClause?
    ;

immutableTableNoDropClause
    : NO DROP (UNTIL INTEGER_ DAYS IDLE)?
    ;

immutableTableNoDeleteClause
    : NO DELETE (LOCKED? | UNTIL INTEGER_ DAYS AFTER INSERT LOCKED?)
    ;

blockchainTableClauses
    : blockchainDropTableClause | blockchainRowRetentionClause | blockchainHashAndDataFormatClause
    ;

blockchainDropTableClause
    : NO DROP (UNTIL INTEGER_ DAYS IDLE)?
    ;

blockchainRowRetentionClause
    : NO DELETE (LOCKED? | UNTIL INTEGER_ DAYS AFTER INSERT LOCKED?)
    ;

blockchainHashAndDataFormatClause
    : HASHING USING 'sha2_512' VERSION V1
    ;

relationalProperty
    : columnDefinition | virtualColumnDefinition | outOfLineConstraint | outOfLineRefConstraint
    ;

columnDefinition
    : columnName REF? (dataType (COLLATE columnCollationName)?)? SORT? visibleClause (DEFAULT (ON NULL)? expr | identityClause)?
    ( ENCRYPT encryptionSpecification)? (inlineConstraint+ | inlineRefConstraint)?
    | REF LP_ columnName RP_ WITH ROWID
    | SCOPE FOR LP_ columnName RP_ IS identifier
    ;

visibleClause
    : (VISIBLE | INVISIBLE)?
    ;

identityClause
    : GENERATED (ALWAYS | BY DEFAULT (ON NULL)?) AS IDENTITY identifyOptions
    ;

identifyOptions
    : LP_? (identityOption+)? RP_?
    ;

identityOption
    : START WITH (INTEGER_ | LIMIT VALUE)
    | INCREMENT BY INTEGER_
    | MAXVALUE INTEGER_
    | NOMAXVALUE
    | MINVALUE INTEGER_
    | NOMINVALUE
    | CYCLE
    | NOCYCLE
    | CACHE INTEGER_
    | NOCACHE
    | ORDER
    | NOORDER
    ;

encryptionSpecification
    : (USING STRING_)? (IDENTIFIED BY (STRING_ | IDENTIFIER_))? (integrityAlgorithm? (NO? SALT)? | (NO? SALT)? integrityAlgorithm?)
    ;

inlineConstraint
    : (CONSTRAINT ignoredIdentifier)? (NOT? NULL | UNIQUE | primaryKey | referencesClause | CHECK LP_ expr RP_) constraintState?
    ;

referencesClause
    : REFERENCES tableName columnNames? (ON DELETE (CASCADE | SET NULL))?
    ;

constraintState
    : (NOT? DEFERRABLE (INITIALLY (DEFERRED | IMMEDIATE))? | INITIALLY (DEFERRED | IMMEDIATE) (NOT? DEFERRABLE)?)? (RELY | NORELY)? usingIndexClause? (ENABLE | DISABLE)? (VALIDATE | NOVALIDATE)? exceptionsClause?
    ;

exceptionsClause
    : EXCEPTIONS INTO tableName
    ;

usingIndexClause
    : USING INDEX (indexName | createIndexClause | indexAttributes)?
    ;

createIndexClause
    :  LP_ createIndex RP_
    ;

inlineRefConstraint
    : SCOPE IS tableName | WITH ROWID | (CONSTRAINT ignoredIdentifier)? referencesClause constraintState?
    ;

virtualColumnDefinition
    : columnName dataType? (GENERATED ALWAYS)? AS LP_ expr RP_ VIRTUAL? inlineConstraint*
    ;

outOfLineConstraint
    : (CONSTRAINT constraintName)?
    (UNIQUE columnNames
    | primaryKey columnNames
    | FOREIGN KEY columnNames referencesClause
    | CHECK LP_ expr RP_
    ) constraintState?
    ;

outOfLineRefConstraint
    : SCOPE FOR LP_ lobItem RP_ IS tableName
    | REF LP_ lobItem RP_ WITH ROWID
    | (CONSTRAINT constraintName)? FOREIGN KEY lobItemList referencesClause constraintState?
    ;

createIndexSpecification
    : (UNIQUE | BITMAP)?
    ;

clusterIndexClause
    : CLUSTER clusterName indexAttributes?
    ;

indexAttributes
    : (physicalAttributesClause | loggingClause | ONLINE | TABLESPACE (tablespaceName | DEFAULT) | indexCompression
    | (SORT | NOSORT) | REVERSE | (VISIBLE | INVISIBLE) | partialIndexClause | parallelClause)+
    ;

tableIndexClause
    : tableName alias? indexExpressions indexProperties?
    ;

indexExpressions
    : LP_? indexExpression (COMMA_ indexExpression)* RP_?
    ;

indexExpression
    : (columnName | expr) (ASC | DESC)?
    ;

indexProperties
    : (((globalPartitionedIndex | localPartitionedIndex) | indexAttributes)+ | INDEXTYPE IS (domainIndexClause | xmlIndexClause))?
    ;

globalPartitionedIndex
    : GLOBAL PARTITION BY (RANGE LP_ columnOrColumnList RP_ LP_ indexPartitioningClause RP_ | HASH LP_ columnOrColumnList RP_ (individualHashPartitions | hashPartitionsByQuantity))
    ;

indexPartitioningClause
    : PARTITION partitionName? VALUES LESS THAN LP_ literals (COMMA_ literals) RP_ segmentAttributesClause?
    ;

localPartitionedIndex
    : LOCAL (onRangePartitionedTable | onListPartitionedTable | onHashPartitionedTable | onCompPartitionedTable)?
    ;

onRangePartitionedTable
    : LP_ partitionedTable (COMMA_ partitionedTable)* RP_
    ;

onListPartitionedTable
    : LP_ partitionedTable (COMMA_ partitionedTable)* RP_
    ;

onHashPartitionedTable
    : (STORE IN LP_ tablespaceName (COMMA_ tablespaceName) RP_ | LP_ PARTITION partitionName? (TABLESPACE tablespaceName)? indexCompression? usableSpecification? RP_)
    ;

onCompPartitionedTable
    : (STORE IN LP_ tablespaceName (COMMA_ tablespaceName) RP_)? LP_ partitionedTable indexSubpartitionClause RP_
    ;
    
partitionedTable
    : PARTITION partitionName? (segmentAttributesClause | indexCompression)* usableSpecification?
    ;

domainIndexClause
    : indexTypeName localDomainIndexClause? parallelClause? (PARAMETERS LP_ SQ_ odciParameters SQ_ RP_)?
    ;

localDomainIndexClause
    : LOCAL (LP_ PARTITION partitionName? (PARAMETERS LP_ SQ_  SQ_ odciParameters RP_)? (COMMA_ PARTITION partitionName? (PARAMETERS LP_ SQ_  SQ_ odciParameters RP_)?)* RP_)?
    ;

xmlIndexClause
    : (XDB COMMA_)? XMLINDEX localXmlIndexClause? parallelClause? xmlIndexParametersClause?
    ;

localXmlIndexClause
    : LOCAL (LP_ PARTITION partitionName xmlIndexParametersClause? (COMMA_ PARTITION partitionName xmlIndexParametersClause?)* RP_)?
    ;

xmlIndexParametersClause
    : PARAMETERS LP_ SQ_ (xmlIndexParameters | PARAM identifier) SQ_ RP_
    ;

xmlIndexParameters
    : (xmlIndexParameterClause)* (TABLESPACE identifier)?
    ;

xmlIndexParameterClause
    : unstructuredClause | structuredClause | asyncClause
    ;

unstructuredClause
    : (PATHS (createIndexPathsClause | alterIndexPathsClause) | (pathTableClause | pikeyClause | pathIdClause | orderKeyClause | valueClause | dropPathTableClause) parallelClause?)
    ;

createIndexPathsClause
    : LP_ (INCLUDE LP_ xPathsList RP_ | EXCLUDE LP_ xPathsList RP_) namespaceMappingClause RP_
    ;

alterIndexPathsClause
    : LP_ (INDEX_ALL_PATHS | (INCLUDE | EXCLUDE) (ADD | REMOVE) LP_ xPathsList RP_ namespaceMappingClause?) RP_
    ;

namespaceMappingClause
    : NAMESPACE MAPPING LP_ namespace+ RP_
    ;

pathTableClause
    : PATH TABLE identifier? (LP_ segmentAttributesClause tableProperties RP_)?
    ;

pikeyClause
    : PIKEY (INDEX identifier? (LP_ indexAttributes RP_)?)?
    ;

pathIdClause
    : PATH ID (INDEX identifier? LP_ indexAttributes RP_)?
    ;

orderKeyClause
    : ORDER KEY (INDEX identifier? LP_ indexAttributes RP_)?
    ;

valueClause
    : VALUE (INDEX identifier? LP_ indexAttributes RP_)?
    ;

dropPathTableClause
    : DROP PATH TABLE
    ;

structuredClause
    : groupsClause | alterIndexGroupClause
    ;

asyncClause
    : ASYNC LP_ SYNC (ALWAYS | MANUAL | EVERY repeatInterval=STRING_ | ON COMMIT) (STALE LP_ (FALSE | TRUE) RP_)? RP_
    ;

groupsClause
    : ((GROUP identifier)? xmlIndexXmltableClause)+
    ;

xmlIndexXmltableClause
    : XMLTABLE identifier (LP_ segmentAttributesClause tableCompression? inmemoryTableClause? tableProperties RP_)?
    ( xmlNamespacesClause COMMA_)? xQueryString=STRING_ (PASSING identifier)? COLUMNS columnClause (COMMA_ columnClause)*
    ;

columnClause
    : columnName (FOR ORDINALITY | dataType PATH STRING_ VIRTUAL?)
    ;

alterIndexGroupClause
    : (NONBLOCKING? ADD_GROUP groupsClause | DROP_GROUP (GROUP identifier (COMMA_ identifier))?
    | NONBLOCKING? ADD_COLUMN addColumnOptions | DROP_COLUMN dropColumnOptions
    | NONBLOCKING ABORT | NONBLOCKING COMPLETE | MODIFY_COLUMN_TYPE modifyColumnTypeOptions)
    ;

addColumnOptions
    : (GROUP identifier)? XMLTABLE identifier (xmlNamespacesClause COMMA_)? COLUMNS columnClause (COMMA_ columnClause)*
    ;

dropColumnOptions
    : (GROUP identifier)? XMLTABLE identifier COLUMNS identifier (COMMA_ identifier)
    ;

modifyColumnTypeOptions
    : (GROUP identifier)? XMLTABLE identifier COLUMNS identifier identifier (COMMA_ identifier identifier)
    ;

bitmapJoinIndexClause
    : tableName columnSortsClause_ FROM tableAlias WHERE expr
    ;

columnSortsClause_
    : LP_? columnSortClause_ (COMMA_ columnSortClause_)* RP_?
    ;

columnSortClause_
    : (tableName | alias)? columnName (ASC | DESC)?
    ;

createIndexDefinitionClause
    : clusterIndexClause | tableIndexClause | bitmapJoinIndexClause
    ;

tableAlias
    : tableName alias? (COMMA_ tableName alias?)*
    ;

alterDefinitionClause
    : (alterTableProperties
    | constraintClauses
    | columnClauses
    | moveTableClause
    | alterTablePartitioning invalidationSpecification?
    | alterExternalTable)?
    ;

alterTableProperties
    : ((physicalAttributesClause
    | loggingClause
    | tableCompression
    | inmemoryTableClause
    | ilmClause
    | supplementalTableLogging
    | allocateExtentClause
    | deallocateUnusedClause
    | (CACHE | NOCACHE)
    | upgradeTableClause
    | recordsPerBlockClause
    | parallelClause
    | rowMovementClause
    | logicalReplicationClause
    | flashbackArchiveClause)+ | renameTableSpecification)? alterIotClauses? alterXMLSchemaClause?
    | shrinkClause
    | READ ONLY
    | READ WRITE
    | REKEY encryptionSpecification
    | DEFAULT COLLATION collationName
    | NO? ROW ARCHIVAL
    | ADD attributeClusteringClause
    | MODIFY CLUSTERING clusteringWhen? zonemapClause?
    | DROP CLUSTERING
    ;

renameTableSpecification
    : RENAME TO identifier
    ;

supplementalTableLogging
    : ADD SUPPLEMENTAL LOG (supplementalLogGrpClause | supplementalIdKeyClause) (COMMA_ SUPPLEMENTAL LOG (supplementalLogGrpClause | supplementalIdKeyClause))*
    | DROP SUPPLEMENTAL LOG (supplementalIdKeyClause | GROUP logGroupName) (COMMA_ SUPPLEMENTAL LOG (supplementalIdKeyClause | GROUP logGroupName))*
    ;

dropSynonym
    : DROP PUBLIC? SYNONYM (schemaName DOT_)? synonymName FORCE?
    ;

columnClauses
    : operateColumnClause+ | renameColumnClause | modifyCollectionRetrieval | modifylobStorageClause
    ;

operateColumnClause
    : addColumnSpecification | modifyColumnSpecification | dropColumnClause
    ;

addColumnSpecification
    : ADD (LP_ columnOrVirtualDefinitions RP_ | columnOrVirtualDefinitions) columnProperties?
    ;

columnOrVirtualDefinitions
    : columnOrVirtualDefinition (COMMA_ columnOrVirtualDefinition)*
    ;

columnOrVirtualDefinition
    : columnDefinition | virtualColumnDefinition
    ;

columnProperties
    : columnProperty+
    ;

columnProperty
    : objectTypeColProperties
    | xmlTypeColProperties
    | lobStorageClause
    ;

xmlTypeColProperties
    : XMLTYPE COLUMN? columnName xmlTypeStorageClause?
    ;

objectTypeColProperties
    : COLUMN columnName substitutableColumnClause
    ;

substitutableColumnClause
    : ELEMENT? IS OF TYPE? LP_ ONLY? dataTypeName RP_ | NOT? SUBSTITUTABLE AT ALL LEVELS
    ;

modifyColumnSpecification
    : MODIFY (LP_? modifyColProperties (COMMA_ modifyColProperties)* RP_? | modifyColSubstitutable)
    ;

modifyColProperties
    : columnName dataType? (DEFAULT expr)? (ENCRYPT encryptionSpecification | DECRYPT)? inlineConstraint*
    ;

modifyColSubstitutable
    : COLUMN columnName NOT? SUBSTITUTABLE AT ALL LEVELS FORCE?
    ;

dropColumnClause
    : SET UNUSED columnOrColumnList cascadeOrInvalidate* | dropColumnSpecification
    ;

dropColumnSpecification
    : DROP columnOrColumnList cascadeOrInvalidate* checkpointNumber?
    ;

columnOrColumnList
    : (COLUMN columnName) | columnNames
    ;

cascadeOrInvalidate
    : CASCADE CONSTRAINTS | INVALIDATE
    ;

checkpointNumber
    : CHECKPOINT NUMBER_
    ;

renameColumnClause
    : RENAME COLUMN columnName TO columnName
    ;

modifyCollectionRetrieval
    : MODIFY NESTED TABLE tableName RETURN AS (LOCATOR | VALUE)
    ;

moveTableClause
    : MOVE filterCondition? ONLINE? segmentAttributesClause? tableCompression? indexOrgTableClause? ((lobStorageClause | varrayColProperties)+)? parallelClause? allowDisallowClustering?
    ( UPDATE INDEXES (LP_ indexName (segmentAttributesClause | updateIndexPartition) RP_ (COMMA_ indexName (segmentAttributesClause | updateIndexPartition))*)?)?
    ;

constraintClauses
    : addConstraintSpecification | modifyConstraintClause | renameConstraintClause | dropConstraintClause+
    ;

addConstraintSpecification
    : ADD (LP_ outOfLineConstraint (COMMA_ outOfLineConstraint)* RP_ | outOfLineConstraint* | outOfLineRefConstraint)
    ;

modifyConstraintClause
    : MODIFY constraintOption constraintState CASCADE?
    ;

constraintWithName
    : CONSTRAINT constraintName
    ;

constraintOption
    : constraintWithName | constraintPrimaryOrUnique
    ;

constraintPrimaryOrUnique
    : primaryKey | UNIQUE columnNames
    ;

renameConstraintClause
    : RENAME constraintWithName TO ignoredIdentifier
    ;

dropConstraintClause
    : DROP
    (
    constraintPrimaryOrUnique CASCADE? ((KEEP | DROP) INDEX)? | (CONSTRAINT constraintName CASCADE?)
    )
    ;

alterExternalTable
    : (addColumnSpecification | modifyColumnSpecification | dropColumnSpecification)+
    ;

objectProperties
    : ((columnName | attributeName) (DEFAULT expr))
    | ((columnName | attributeName) (inlineConstraint* | inlineRefConstraint))
    | ((columnName | attributeName) (DEFAULT expr) (inlineConstraint* | inlineRefConstraint))
    | outOfLineConstraint
    | outOfLineRefConstraint
    | supplementalLoggingProps
    ;

alterIndexInformationClause
    : (deallocateUnusedClause
    | allocateExtentClause
    | shrinkClause
    | parallelClause
    | physicalAttributesClause
    | loggingClause
    | partialIndexClause)+
    | rebuildClause invalidationSpecification?
    | parallelClause
    | PARAMETERS LP_ odciParameters RP_
    | COMPILE
    | (ENABLE | DISABLE)
    | UNUSABLE ONLINE? invalidationSpecification?
    | (VISIBLE | INVISIBLE)
    | renameIndexClause
    | COALESCE CLEANUP? ONLY? parallelClause?
    | ((MONITORING | NOMONITORING) USAGE)
    | UPDATE BLOCK REFERENCES
    | alterIndexPartitioning
    ;

renameIndexClause
    : (RENAME TO indexName)?
    ;

alterIndexPartitioning
    : modifyIndexDefaultAttrs
    | addHashIndexPartition
    | modifyIndexPartition
    | renameIndexPartition
    | dropIndexPartition
    | splitIndexPartition
    | coalesceIndexPartition
    | modifyIndexsubPartition
    ;

modifyIndexDefaultAttrs
    : MODIFY DEFAULT ATTRIBUTES (FOR PARTITION partitionName)? (physicalAttributesClause | TABLESPACE (tablespaceName | DEFAULT) | loggingClause)+
    ;

addHashIndexPartition
    : ADD PARTITION partitionName? (TABLESPACE tablespaceName)? indexCompression? parallelClause?
    ;

modifyIndexPartition
    : MODIFY PARTITION partitionName
    ( (deallocateUnusedClause | allocateExtentClause | physicalAttributesClause | loggingClause | indexCompression)+
    | PARAMETERS LP_ odciParameters RP_
    | COALESCE (CLEANUP | ONLY | parallelClause)?
    | UPDATE BLOCK REFERENCES
    | UNUSABLE
    )
    ;

renameIndexPartition
    : RENAME (PARTITION partitionName | SUBPARTITION subpartitionName) TO newName
    ;

dropIndexPartition
    : DROP PARTITION partitionName
    ;

splitIndexPartition
    : SPLIT PARTITION partitionName AT LP_ literals (COMMA_ literals)* RP_ (INTO LP_ indexPartitionDescription COMMA_ indexPartitionDescription RP_)? (parallelClause)?
    ;

indexPartitionDescription
    : PARTITION (partitionName ((segmentAttributesClause | indexCompression)+ | PARAMETERS LP_ odciParameters RP_)? usableSpecification?)?
    ;

coalesceIndexPartition
    : COALESCE PARTITION parallelClause?
    ;

modifyIndexsubPartition
    : MODIFY SUBPARTITION subpartitionName (UNUSABLE | allocateExtentClause | deallocateUnusedClause)
    ;

objectTableSubstitution
    : NOT? SUBSTITUTABLE AT ALL LEVELS
    ;

memOptimizeClause
    : memOptimizeReadClause? memOptimizeWriteClause?
    ;

memOptimizeReadClause
    : (MEMOPTIMIZE FOR READ | NO MEMOPTIMIZE FOR READ)
    ;

memOptimizeWriteClause
    : (MEMOPTIMIZE FOR WRITE | NO MEMOPTIMIZE FOR WRITE)
    ;

enableDisableClauses
    : (enableDisableClause | enableDisableOthers)+
    ;

enableDisableClause
    : (ENABLE | DISABLE) (VALIDATE | NOVALIDATE)? ((UNIQUE columnName (COMMA_ columnName)*) | PRIMARY KEY | constraintWithName) usingIndexClause? exceptionsClause? CASCADE? ((KEEP | DROP) INDEX)?
    ;

enableDisableOthers
    : (ENABLE | DISABLE) (TABLE LOCK | ALL TRIGGERS | CONTAINER_MAP | CONTAINERS_DEFAULT)
    ;

rebuildClause
    : REBUILD (PARTITION partitionName | SUBPARTITION subpartitionName | REVERSE | NOREVERSE)?
    ( parallelClause
    | TABLESPACE tablespaceName
    | PARAMETERS LP_ odciParameters RP_
    | ONLINE
    | physicalAttributesClause
    | indexCompression
    | loggingClause
    | partialIndexClause)*
    ;

parallelClause
    : NOPARALLEL | PARALLEL (INTEGER_ | LP_ DEGREE INTEGER_ RP_)?
    ;

usableSpecification
    : (USABLE | UNUSABLE)
    ;

invalidationSpecification
    : (DEFERRED | IMMEDIATE) INVALIDATION
    ;

materializedViewLogClause
    : (PRESERVE | PURGE) MATERIALIZED VIEW LOG
    ;

dropReuseClause
    : (DROP (ALL)? | REUSE) STORAGE
    ;

collationClause
    : DEFAULT COLLATION collationName
    ;

createSynonym
    : CREATE (OR REPLACE)? (EDITIONABLE | NONEDITIONABLE)? (PUBLIC)? SYNONYM (schemaName DOT_)? synonymName (SHARING EQ_ (METADATA | NONE))? FOR objectName (AT_ dbLink)?
    ;

commitClause
    : (ON COMMIT (DROP | PRESERVE) DEFINITION)? (ON COMMIT (DELETE | PRESERVE) ROWS)?
    ;

physicalProperties
    : (deferredSegmentCreation? segmentAttributesClause tableCompression? inmemoryTableClause? ilmClause?
    | deferredSegmentCreation? (organizationClause | externalPartitionClause) | CLUSTER clusterName columns)
    ;

deferredSegmentCreation
    : SEGMENT CREATION (IMMEDIATE|DEFERRED)
    ;

segmentAttributesClause
    : ( physicalAttributesClause
    | (TABLESPACE tablespaceName | TABLESPACE SET tablespaceSetName)
    | tableCompression
    | loggingClause)+
    ;

physicalAttributesClause
    : (PCTFREE INTEGER_ | PCTUSED INTEGER_ | INITRANS INTEGER_ | MAXTRANS INTEGER_ | COMPUTE STATISTICS | storageClause)+
    ;

loggingClause
    : LOGGING | NOLOGGING |  FILESYSTEM_LIKE_LOGGING
    ;

partialIndexClause
    : INDEXING (PARTIAL | FULL)
    ;

storageClause
    : STORAGE LP_
    (INITIAL sizeClause
    | NEXT sizeClause
    | MINEXTENTS INTEGER_
    | MAXEXTENTS (INTEGER_ | UNLIMITED)
    | maxsizeClause
    | PCTINCREASE INTEGER_
    | FREELISTS INTEGER_
    | FREELIST GROUPS INTEGER_
    | OPTIMAL (sizeClause | NULL)?
    | BUFFER_POOL (KEEP | RECYCLE | DEFAULT)
    | FLASH_CACHE (KEEP | NONE | DEFAULT)
    | CELL_FLASH_CACHE (KEEP | NONE | DEFAULT)
    | ENCRYPT
    )+ RP_
    ;

tableCompression
    : COMPRESS
    | ROW STORE COMPRESS (BASIC | ADVANCED)?
    | COLUMN STORE COMPRESS (FOR (QUERY | ARCHIVE) (LOW | HIGH)?)? (NO? ROW LEVEL LOCKING)?
    | COMPRESS FOR OLTP
    | NOCOMPRESS
    ;

inmemoryTableClause
    : ((INMEMORY inmemoryAttributes?) | NO INMEMORY)
    | (inmemoryColumnClause)
    | ((INMEMORY inmemoryAttributes?) | NO INMEMORY) (inmemoryColumnClause)
    ;

inmemoryAttributes
    : inmemoryMemcompress? inmemoryPriority? inmemoryDistribute? inmemoryDuplicate?
    ;

inmemoryColumnClause
    : (INMEMORY inmemoryMemcompress? | NO INMEMORY) columnNames
    ;

inmemoryMemcompress
    : MEMCOMPRESS FOR ( DML | (QUERY | CAPACITY) (LOW | HIGH)? ) | NO MEMCOMPRESS
    ;

inmemoryPriority
    : PRIORITY (NONE | LOW | MEDIUM | HIGH | CRITICAL)
    ;

inmemoryDistribute
    : DISTRIBUTE (AUTO | BY (ROWID RANGE | PARTITION | SUBPARTITION))? (FOR SERVICE (DEFAULT | ALL | serviceName | NONE))?
    ;

inmemoryDuplicate
    : DUPLICATE | DUPLICATE ALL | NO DUPLICATE
    ;

ilmClause
    : ILM (ADD POLICY ilmPolicyClause
    | (DELETE | ENABLE | DISABLE) POLICY ilmPolicyName
    | (DELETE_ALL | ENABLE_ALL | DISABLE_ALL))
    ;

ilmPolicyClause
    : ilmCompressionPolicy | ilmTieringPolicy | ilmInmemoryPolicy
    ;

ilmCompressionPolicy
    : tableCompression (SEGMENT | GROUP) ( AFTER ilmTimePeriod OF ( NO ACCESS | NO MODIFICATION | CREATION ) | ON functionName)
    | (ROW STORE COMPRESS ADVANCED | COLUMN STORE COMPRESS FOR QUERY) ROW AFTER ilmTimePeriod OF NO MODIFICATION
    ;

ilmTimePeriod
    : NUMBER_ ((DAY | DAYS) | (MONTH | MONTHS) | (YEAR | YEARS))
    ;

ilmTieringPolicy
    : TIER TO tablespaceName (SEGMENT | GROUP)? (ON functionName)?
    | TIER TO tablespaceName READ ONLY (SEGMENT | GROUP)? (AFTER ilmTimePeriod OF (NO ACCESS | NO MODIFICATION | CREATION) | ON functionName)
    ;

ilmInmemoryPolicy
    : (SET INMEMORY inmemoryAttributes | MODIFY INMEMORY inmemoryMemcompress | NO INMEMORY) SEGMENT (AFTER ilmTimePeriod OF (NO ACCESS | NO MODIFICATION | CREATION) | ON functionName)
    ;

organizationClause
    : ORGANIZATION
    ( HEAP segmentAttributesClause? heapOrgTableClause
    | INDEX segmentAttributesClause? indexOrgTableClause
    | EXTERNAL externalTableClause)
    ;

heapOrgTableClause
    : tableCompression? inmemoryTableClause? ilmClause?
    ;

indexOrgTableClause
    : (mappingTableClause | PCTTHRESHOLD INTEGER_ | prefixCompression)* indexOrgOverflowClause?
    ;

externalTableClause
    : LP_ (TYPE accessDriverType)? (externalTableDataProps)? RP_ (REJECT LIMIT (NUMBER_ | UNLIMITED))? inmemoryTableClause?
    ;

externalTableDataProps
    : (DEFAULT DIRECTORY directoryName)? (ACCESS PARAMETERS ((opaqueFormatSpec delimSpec)? | USING CLOB subquery))? (LOCATION LP_ (directoryName | (directoryName COLON_)? locationSpecifier (COMMA_ (directoryName COLON_)? locationSpecifier)+) RP_)?
    ;

mappingTableClause
    : MAPPING TABLE | NOMAPPING
    ;

prefixCompression
    : COMPRESS NUMBER_? | NOCOMPRESS
    ;

indexOrgOverflowClause
    :  (INCLUDING columnName)? OVERFLOW segmentAttributesClause?
    ;

externalPartitionClause
    : EXTERNAL PARTITION ATTRIBUTES externalTableClause (REJECT LIMIT)?
    ;

clusterRelatedClause
    : CLUSTER clusterName columnNames
    ;

tableProperties
    : columnProperties? readOnlyClause? indexingClause? tablePartitioningClauses? attributeClusteringClause? (CACHE | NOCACHE)? parallelClause?
    ( RESULT_CACHE (LP_ MODE (DEFAULT | FORCE) RP_))? (ROWDEPENDENCIES | NOROWDEPENDENCIES)? enableDisableClause* rowMovementClause? logicalReplicationClause? flashbackArchiveClause?
    ( ROW ARCHIVAL)? (AS selectSubquery | FOR EXCHANGE WITH TABLE tableName)?
    ;

readOnlyClause
    : READ ONLY | READ WRITE
    ;

indexingClause
    : INDEXING (ON | OFF)
    ;

tablePartitioningClauses
    : rangePartitions
    | listPartitions
    | hashPartitions
    | compositeRangePartitions
    | compositeListPartitions
    | compositeHashPartitions
    | referencePartitioning
    | systemPartitioning
    | consistentHashPartitions
    | consistentHashWithSubpartitions
    | partitionsetClauses
    ;

rangePartitions
    : PARTITION BY RANGE columnNames
      (INTERVAL LP_ expr RP_ (STORE IN LP_ tablespaceName (COMMA_ tablespaceName)* RP_)?)?
      LP_ PARTITION partitionName? rangeValuesClause tablePartitionDescription (COMMA_ PARTITION partitionName? rangeValuesClause tablePartitionDescription externalPartSubpartDataProps?)* RP_
    ;

rangeValuesClause
    : VALUES LESS THAN LP_? (literals | MAXVALUE | toDateFunction) (COMMA_ (literals | MAXVALUE | toDateFunction))* RP_?
    ;

tablePartitionDescription
    : (INTERNAL | EXTERNAL)?
      deferredSegmentCreation?
      readOnlyClause?
      indexingClause?
      segmentAttributesClause?
      (tableCompression | prefixCompression)?
      inmemoryClause?
      ilmClause?
      (OVERFLOW segmentAttributesClause?)?
      (lobStorageClause | varrayColProperties | nestedTableColProperties)*
    ;

inmemoryClause
    : INMEMORY inmemoryAttributes
    | INMEMORY
    | NO INMEMORY
    ;

varrayColProperties
    : VARRAY varrayItem (substitutableColumnClause? varrayStorageClause | substitutableColumnClause)
    ;

nestedTableColProperties
    : NESTED TABLE (nestedItem | COLUMN_VALUE) substitutableColumnClause? (LOCAL | GLOBAL)? STORE AS storageTable
    ( LP_ (LP_ objectProperties RP_ | physicalProperties | columnProperties)+ RP_)? (RETURN AS? (LOCATOR | VALUE))?
    ;

lobStorageClause
    : LOB
    ( LP_ lobItem (COMMA_ lobItem)* RP_ STORE AS ((SECUREFILE | BASICFILE) | LP_ lobStorageParameters RP_)+
    | LP_ lobItem RP_ STORE AS ((SECUREFILE | BASICFILE) | lobSegname | LP_ lobStorageParameters RP_)+
    )
    ;

varrayStorageClause
    : STORE AS (SECUREFILE | BASICFILE)? LOB (lobSegname? LP_ lobStorageParameters RP_ | lobSegname)
    ;

lobStorageParameters
    : ((TABLESPACE tablespaceName | TABLESPACE SET tablespaceSetName) | lobParameters storageClause?)+ | storageClause
    ;

lobParameters
    : ( (ENABLE | DISABLE) STORAGE IN ROW
        | CHUNK INTEGER_
        | PCTVERSION INTEGER_
        | FREEPOOLS INTEGER_
        | lobRetentionClause
        | lobDeduplicateClause
        | lobCompressionClause
        | (ENCRYPT encryptionSpecification | DECRYPT)
        | (CACHE | NOCACHE | CACHE READS) loggingClause?
      )+
    ;

lobRetentionClause
    : RETENTION (MAX | MIN NUMBER_ | AUTO | NONE)?
    ;

lobDeduplicateClause
    : DEDUPLICATE | KEEP_DUPLICATES
    ;

lobCompressionClause
    : (COMPRESS (HIGH | MEDIUM | LOW)? | NOCOMPRESS)
    ;

externalPartSubpartDataProps
    : (DEFAULT DIRECTORY directoryName) (LOCATION LP_ (directoryName COLON_)? locationSpecifier (COMMA_ (directoryName COLON_)? locationSpecifier)* RP_)?
    ;

listPartitions
    : PARTITION BY LIST columnNames
      (AUTOMATIC (STORE IN LP_? tablespaceName (COMMA_ tablespaceName)* RP_?))?
      LP_ PARTITION partitionName? listValuesClause tablePartitionDescription (COMMA_ PARTITION partitionName? listValuesClause tablePartitionDescription externalPartSubpartDataProps?)* RP_
    ;

listValuesClause
    : VALUES LP_ (listValues | DEFAULT) RP_
    ;

listValues
    : (literals | NULL) (COMMA_ (literals | NULL))*
    | (LP_? ( (literals | NULL) (COMMA_ (literals | NULL))* ) RP_?) (COMMA_ LP_? ( (literals | NULL) (COMMA_ (literals | NULL))* ) RP_?)*
    ;

hashPartitions
    : PARTITION BY HASH columnNames (individualHashPartitions | hashPartitionsByQuantity)
    ;

hashPartitionsByQuantity
    : PARTITIONS INTEGER_ (STORE IN (tablespaceName (COMMA_ tablespaceName)*))? (tableCompression | indexCompression)? (OVERFLOW STORE IN (tablespaceName (COMMA_ tablespaceName)*))?
    ;

indexCompression
    : prefixCompression | advancedIndexCompression
    ;

advancedIndexCompression
    : COMPRESS ADVANCED (LOW | HIGH)? | NOCOMPRESS
    ;

individualHashPartitions
    : LP_? (PARTITION partitionName? readOnlyClause? indexingClause? partitioningStorageClause?) (COMMA_ PARTITION partitionName? readOnlyClause? indexingClause? partitioningStorageClause?)* RP_?
    ;

partitioningStorageClause
    : ((TABLESPACE tablespaceName | TABLESPACE SET tablespaceSetName)
    | OVERFLOW (TABLESPACE tablespaceName | TABLESPACE SET tablespaceSetName)?
    | tableCompression
    | indexCompression
    | inmemoryClause
    | ilmClause
    | lobPartitioningStorage
    | VARRAY varrayItem STORE AS (SECUREFILE | BASICFILE)? LOB lobSegname
    )*
    ;

lobPartitioningStorage
    :LOB LP_ lobItem RP_ STORE AS (BASICFILE | SECUREFILE)?
    (lobSegname (LP_ TABLESPACE tablespaceName | TABLESPACE SET tablespaceSetName RP_)?
    | LP_ TABLESPACE tablespaceName | TABLESPACE SET tablespaceSetName RP_
    )?
    ;

compositeRangePartitions
    : PARTITION BY RANGE columnNames
      (INTERVAL LP_ expr RP_ (STORE IN LP_? tablespaceName (COMMA_ tablespaceName)* RP_?)?)?
      (subpartitionByRange | subpartitionByList | subpartitionByHash)
      LP_? rangePartitionDesc (COMMA_ rangePartitionDesc)* RP_?
    ;

subpartitionByRange
    : SUBPARTITION BY RANGE columnNames subpartitionTemplate?
    ;

subpartitionByList
    : SUBPARTITION BY LIST columnNames subpartitionTemplate?
    ;

subpartitionByHash
    : SUBPARTITION BY HASH columnNames (SUBPARTITIONS INTEGER_ (STORE IN LP_ tablespaceName (COMMA_ tablespaceName)? RP_)? | subpartitionTemplate)?
    ;

subpartitionTemplate
    : SUBPARTITION TEMPLATE (LP_ (rangeSubpartitionDesc (COMMA_ rangeSubpartitionDesc)*
    | listSubpartitionDesc (COMMA_ listSubpartitionDesc)* | individualHashSubparts (COMMA_ individualHashSubparts)*) RP_) | hashSubpartitionQuantity
    ;

rangeSubpartitionDesc
    : SUBPARTITION subpartitionName? rangeValuesClause readOnlyClause? indexingClause? partitioningStorageClause? externalPartSubpartDataProps?
    ;

listSubpartitionDesc
    : SUBPARTITION subpartitionName? listValuesClause readOnlyClause? indexingClause? partitioningStorageClause? externalPartSubpartDataProps?
    ;

individualHashSubparts
    : SUBPARTITION subpartitionName? readOnlyClause? indexingClause? partitioningStorageClause?
    ;

hashSubpartitionQuantity
    : SUBPARTITIONS INTEGER_ (STORE IN LP_ tablespaceName (COMMA_ tablespaceName)* RP_)?
    ;

rangePartitionDesc
    : PARTITION partitionName? rangeValuesClause tablePartitionDescription (LP_ (rangeSubpartitionDesc (COMMA_ rangeSubpartitionDesc)*
    | listSubpartitionDesc (COMMA_ listSubpartitionDesc)* | individualHashSubparts (COMMA_ individualHashSubparts)*) RP_ | hashSubpartitionQuantity)?
    ;

compositeListPartitions
    : PARTITION BY LIST columnNames
      (AUTOMATIC (STORE IN LP_? tablespaceName (COMMA_ tablespaceName)* RP_?)?)?
      (subpartitionByRange | subpartitionByList | subpartitionByHash)
      LP_? listPartitionDesc (COMMA_ listPartitionDesc)* RP_?
    ;

listPartitionDesc
    : PARTITION partitionName? listValuesClause tablePartitionDescription (LP_ (rangeSubpartitionDesc (COMMA_ rangeSubpartitionDesc)*
    | listSubpartitionDesc (COMMA_ listSubpartitionDesc)* | individualHashSubparts (COMMA_ individualHashSubparts)*) RP_ | hashSubpartsByQuantity)?
    ;

compositeHashPartitions
    : PARTITION BY HASH columnNames (subpartitionByRange | subpartitionByList | subpartitionByHash) (individualHashPartitions | hashPartitionsByQuantity)
    ;

referencePartitioning
    :PARTITION BY REFERENCE LP_ constraint RP_ (LP_? referencePartitionDesc (COMMA_ referencePartitionDesc)* RP_?)?
    ;

referencePartitionDesc
    : PARTITION
    | PARTITION partitionName
    | PARTITION tablePartitionDescription
    | PARTITION partitionName tablePartitionDescription
    ;

constraint
    : inlineConstraint | outOfLineConstraint | inlineRefConstraint | outOfLineRefConstraint
    ;

systemPartitioning
    : PARTITION BY SYSTEM (PARTITIONS NUMBER_ | referencePartitionDesc (COMMA_ referencePartitionDesc)*)?
    | PARTITION BY SYSTEM
    ;

consistentHashPartitions
    : PARTITION BY CONSISTENT HASH columnNames (PARTITIONS AUTO)? TABLESPACE SET tablespaceSetName
    ;

consistentHashWithSubpartitions
    : PARTITION BY CONSISTENT HASH columnNames (subpartitionByRange | subpartitionByList | subpartitionByHash)  (PARTITIONS AUTO)?
    ;

partitionsetClauses
    : rangePartitionsetClause | listPartitionsetClause
    ;

rangePartitionsetClause
    : PARTITIONSET BY RANGE columnNames PARTITION BY CONSISTENT HASH columnNames
      (SUBPARTITION BY ((RANGE | HASH) columnNames | LIST LP_ columnName LP_) subpartitionTemplate?)?
      PARTITIONS AUTO LP_ rangePartitionsetDesc (COMMA_ rangePartitionsetDesc)* RP_
    ;

rangePartitionsetDesc
    : PARTITIONSET partitionSetName rangeValuesClause (TABLESPACE SET tablespaceSetName)? (lobStorageClause)? (SUBPARTITIONS STORE IN tablespaceSetName?)?
    ;

listPartitionsetClause
    : PARTITIONSET BY RANGE LP_ columnName RP_ PARTITION BY CONSISTENT HASH columnNames
      (SUBPARTITION BY ((RANGE | HASH) columnNames | LIST LP_ columnName LP_) subpartitionTemplate?)?
      PARTITIONS AUTO LP_ rangePartitionsetDesc (COMMA_ rangePartitionsetDesc)* RP_
    ;

attributeClusteringClause
    : CLUSTERING clusteringJoin? clusterClause clusteringWhen? zonemapClause?
    ;

clusteringJoin
    : tableName (JOIN tableName ON LP_ expr RP_)+
    ;

clusterClause
    : BY (LINEAR | INTERLEAVED)? ORDER clusteringColumns
    ;

createDirectory
    : CREATE (OR REPLACE)? DIRECTORY directoryName (SHARING EQ_ (METADATA | NONE))? AS pathString
    ;

clusteringColumns
    : LP_? clusteringColumnGroup (COMMA_ clusteringColumnGroup)* RP_?
    ;

clusteringColumnGroup
    : columnNames
    ;

clusteringWhen
    : ((YES | NO) ON LOAD)? ((YES | NO) ON DATA MOVEMENT)?
    ;

zonemapClause
    : (WITH MATERIALIZED ZONEMAP (LP_ zonemapName RP_)?) | (WITHOUT MATERIALIZED ZONEMAP)
    ;

rowMovementClause
    : (ENABLE | DISABLE) ROW MOVEMENT
    ;

logicalReplicationClause
    : (ENABLE | DISABLE) LOGICAL REPLICATION
    ;

flashbackArchiveClause
    : FLASHBACK ARCHIVE flashbackArchiveName? | NO FLASHBACK ARCHIVE
    ;

alterPackage
    : ALTER PACKAGE packageName (
    | packageCompileClause
    | (EDITIONABLE | NONEDITIONABLE)
    )
    ;

alterProfile
    : ALTER PROFILE profileName LIMIT (resourceParameters | passwordParameters)+ (CONTAINER EQ_ (CURRENT | ALL))?
    ;

resourceParameters
    : (SESSIONS_PER_USER
    | CPU_PER_SESSION
    | CPU_PER_CALL
    | CONNECT_TIME
    | IDLE_TIME
    | LOGICAL_READS_PER_SESSION
    | LOGICAL_READS_PER_CALL
    | COMPOSITE_LIMIT
    ) (INTEGER_ | UNLIMITED | DEFAULT)
    | PRIVATE_SGA (sizeClause | UNLIMITED | DEFAULT)
    ;

passwordParameters
    : (FAILED_LOGIN_ATTEMPTS
    | PASSWORD_LIFE_TIME
    | PASSWORD_REUSE_TIME
    | PASSWORD_REUSE_MAX
    | PASSWORD_LOCK_TIME
    | PASSWORD_GRACE_TIME
    | INACTIVE_ACCOUNT_TIME
    ) (expr | UNLIMITED | DEFAULT)
    | PASSWORD_VERIFY_FUNCTION (function | NULL | DEFAULT)
    | PASSWORD_ROLLOVER_TIME (expr | DEFAULT)
    ;

alterRollbackSegment
    : ALTER ROLLBACK SEGMENT rollbackSegmentName (ONLINE | OFFLINE | storageClause | SHRINK (TO sizeClause)?)
    ;

packageCompileClause
    : COMPILE DEBUG? (PACKAGE | SPECIFICATION | BODY)? (compilerParametersClause*)? (REUSE SETTINGS)?
    ;

alterSynonym
    : ALTER PUBLIC? SYNONYM (schemaName DOT_)? synonymName (COMPILE | EDITIONABLE | NONEDITIONABLE)
    ;

alterTablePartitioning
    : modifyTableDefaultAttrs
    | setSubpartitionTemplate
    | modifyTablePartition
    | modifyTableSubpartition
    | moveTablePartition
    | moveTableSubPartition
    | addTablePartition
    | coalesceTablePartition
    | dropTablePartition
    | renamePartitionSubpart
    | alterIntervalPartitioning
    ;

modifyTableDefaultAttrs
    : MODIFY DEFAULT ATTRIBUTES (FOR partitionExtendedName)? (DEFAULT DIRECTORY directoryName)? deferredSegmentCreation? readOnlyClause? indexingClause? segmentAttributesClause? alterOverflowClause?
    ( ((LOB LP_ lobItem RP_ | VARRAY varrayType) LP_ lobParameters RP_)+)?
    ;

setSubpartitionTemplate
    : SET SUBPARTITION TEMPLATE (LP_ (rangeSubpartitionDesc (COMMA_ rangeSubpartitionDesc)* | listSubpartitionDesc (COMMA_ listSubpartitionDesc)* | individualHashSubparts (COMMA_ individualHashSubparts)*)? RP_ | hashSubpartitionQuantity)
    ;

modifyTablePartition
    : modifyRangePartition
    | modifyHashPartition
    | modifyListPartition
    ;

modifyRangePartition
    : MODIFY partitionExtendedName (partitionAttributes
    | (addRangeSubpartition | addHashSubpartition | addListSubpartition)
    | coalesceTableSubpartition | alterMappingTableClauses | REBUILD? UNUSABLE LOCAL INDEXES
    | readOnlyClause | indexingClause)
    ;

modifyHashPartition
    : MODIFY partitionExtendedName (partitionAttributes | coalesceTableSubpartition
    | alterMappingTableClauses | REBUILD? UNUSABLE LOCAL INDEXES | readOnlyClause | indexingClause)
    ;

modifyListPartition
    : MODIFY partitionExtendedName (partitionAttributes
    | (ADD | DROP) VALUES LP_ listValues RP_
    | (addRangeSubpartition | addHashSubpartition | addListSubpartition)
    | coalesceTableSubpartition | REBUILD? UNUSABLE LOCAL INDEXES | readOnlyClause | indexingClause)
    ;

modifyTableSubpartition
    : MODIFY subpartitionExtendedName (allocateExtentClause
    | deallocateUnusedClause | shrinkClause | ((LOB lobItem | VARRAY varrayType) LP_ modifylobParameters RP_)+ | REBUILD? UNUSABLE LOCAL INDEXES
    | (ADD | DROP) VALUES LP_ listValues RP_ | readOnlyClause | indexingClause)
    ;

subpartitionExtendedName
    : SUBPARTITION (subpartitionName | FOR LP_ subpartitionKeyValue (COMMA_ subpartitionKeyValue)* RP_)
    ;

partitionExtendedName
    : PARTITION partitionName
    | PARTITION FOR LP_ partitionKeyValue (COMMA_ partitionKeyValue)* RP_
    ;

addRangeSubpartition
    : ADD rangeSubpartitionDesc (COMMA_ rangeSubpartitionDesc)* dependentTablesClause? updateIndexClauses?
    ;

dependentTablesClause
    : DEPENDENT TABLES LP_ tableName LP_ partitionSpec (COMMA_ partitionSpec)* RP_
    (COMMA_ tableName LP_ partitionSpec (COMMA_ partitionSpec)* RP_)* RP_
    ;

addHashSubpartition
    : ADD individualHashSubparts dependentTablesClause? updateIndexClauses? parallelClause?
    ;

addListSubpartition
    : ADD listSubpartitionDesc (COMMA_ listSubpartitionDesc)* dependentTablesClause? updateIndexClauses?
    ;

coalesceTableSubpartition
    : COALESCE SUBPARTITION subpartitionName? updateIndexClauses? parallelClause? allowDisallowClustering?
    ;

allowDisallowClustering
    : (ALLOW | DISALLOW) CLUSTERING
    ;

alterMappingTableClauses
    : MAPPING TABLE (allocateExtentClause | deallocateUnusedClause)
    ;

alterView
    : ALTER VIEW viewName (
    | ADD LP_? outOfLineConstraint RP_?
    | MODIFY CONSTRAINT constraintName (RELY | NORELY)
    | DROP (CONSTRAINT constraintName | PRIMARY KEY | UNIQUE columnNames)
    | COMPILE
    | READ (ONLY | WRITE)
    | (EDITIONABLE | NONEDITIONABLE)
    )
    ;

deallocateUnusedClause
    : DEALLOCATE UNUSED (KEEP sizeClause)?
    ;

allocateExtentClause
    : ALLOCATE EXTENT (LP_ (SIZE sizeClause | DATAFILE SQ_ fileName SQ_ | INSTANCE NUMBER_)* RP_)?
    ;

partitionSpec
    : PARTITION partitionName? tablePartitionDescription?
    ;

upgradeTableClause
    : UPGRADE (NOT? INCLUDING DATA)? columnProperties?
    ;

recordsPerBlockClause
    : (MINIMIZE | NOMINIMIZE) RECORDS_PER_BLOCK
    ;

partitionAttributes
    : (physicalAttributesClause | loggingClause | allocateExtentClause | deallocateUnusedClause | shrinkClause)*
      (OVERFLOW (physicalAttributesClause | loggingClause | allocateExtentClause | deallocateUnusedClause)*)?
      tableCompression? inmemoryClause?
    ;

shrinkClause
    : SHRINK SPACE COMPACT? CASCADE?
    ;

moveTablePartition
    : MOVE partitionExtendedName (MAPPING TABLE)? tablePartitionDescription? filterCondition? updateAllIndexesClause? parallelClause? allowDisallowClustering? ONLINE?
    ;

moveTableSubPartition
	: MOVE subpartitionExtendedName indexingClause? partitioningStorageClause? updateIndexClauses? filterCondition? parallelClause? allowDisallowClustering? ONLINE?
	;

filterCondition
    : INCLUDING ROWS whereClause
    ;

whereClause
    : WHERE expr
    ;

coalesceTablePartition
    : COALESCE PARTITION updateIndexClauses? parallelClause? allowDisallowClustering?
    ;

addTablePartition
    : ADD (PARTITION partitionName? addRangePartitionClause (COMMA_ PARTITION partitionName? addRangePartitionClause)*
    | PARTITION partitionName? addListPartitionClause (COMMA_ PARTITION partitionName? addListPartitionClause)*
    | PARTITION partitionName? addSystemPartitionClause (COMMA_ PARTITION partitionName? addSystemPartitionClause)* (BEFORE? (partitionName | NUMBER_)?)
    | PARTITION partitionName? addHashPartitionClause) dependentTablesClause?
    ;

addRangePartitionClause
    : rangeValuesClause tablePartitionDescription? externalPartSubpartDataProps?
    ((LP_? (rangeSubpartitionDesc (COMMA_ rangeSubpartitionDesc)* | listSubpartitionDesc (COMMA_ listSubpartitionDesc)* | individualHashSubparts (COMMA_ individualHashSubparts)*) RP_?)
    | hashSubpartsByQuantity)? updateIndexClauses?
    ;

addListPartitionClause
    : listValuesClause tablePartitionDescription? externalPartSubpartDataProps?
    ((LP_? (rangeSubpartitionDesc (COMMA_ rangeSubpartitionDesc)* | listSubpartitionDesc (COMMA_ listSubpartitionDesc)* | individualHashSubparts (COMMA_ individualHashSubparts)*) RP_?)
    | hashSubpartsByQuantity)? updateIndexClauses?
    ;

hashSubpartsByQuantity
    : SUBPARTITIONS NUMBER_ (STORE IN LP_ tablespaceName (COMMA_ tablespaceName)* RP_)?
    ;

addSystemPartitionClause
    : tablePartitionDescription? updateIndexClauses?
    ;

addHashPartitionClause
    : partitioningStorageClause updateIndexClauses? parallelClause? readOnlyClause? indexingClause?
    ;

dropTablePartition
    : DROP partitionExtendedNames (updateIndexClauses parallelClause?)?
    ;

renamePartitionSubpart
    : RENAME (partitionExtendedName | subpartitionExtendedName) TO newName
    ;

alterIntervalPartitioning
    : SET INTERVAL LP_ expr? RP_ | SET STORE IN LP_ tablespaceName (COMMA_ tablespaceName)* RP_
    ;

partitionExtendedNames
    : (PARTITION | PARTITIONS) (partitionName | partitionForClauses) (COMMA_ (partitionName | partitionForClauses))*
    ;

partitionForClauses
    : FOR LP_ partitionKeyValue (COMMA_ partitionKeyValue)* RP_
    ;

updateIndexClauses
    : updateGlobalIndexClause | updateAllIndexesClause
    ;

updateGlobalIndexClause
    : (UPDATE | INVALIDATE) GLOBAL INDEXES
    ;

updateAllIndexesClause
    : UPDATE INDEXES
    (LP_ indexName LP_ (updateIndexPartition | updateIndexSubpartition) RP_
    (COMMA_ indexName LP_ (updateIndexPartition | updateIndexSubpartition) RP_)* RP_)?
    ;

updateIndexPartition
    : indexPartitionDesc indexSubpartitionClause?
    (COMMA_ indexPartitionDesc indexSubpartitionClause?)*
    ;

indexPartitionDesc
    : PARTITION
    (partitionName
    ((segmentAttributesClause | indexCompression)+ | PARAMETERS LP_ SQ_ odciParameters SQ_ RP_ )?
    usableSpecification?
    )?
    ;

indexSubpartitionClause
    : STORE IN LP_ tablespaceName (COMMA_ tablespaceName)* RP_
    | LP_ SUBPARTITION subpartitionName? (TABLESPACE tablespaceName)? indexCompression? usableSpecification?
    (COMMA_ SUBPARTITION subpartitionName? (TABLESPACE tablespaceName)? indexCompression? usableSpecification?)* RP_
    ;

updateIndexSubpartition
    : SUBPARTITION subpartitionName? (TABLESPACE tablespaceName)?
    (COMMA_ SUBPARTITION subpartitionName? (TABLESPACE tablespaceName)?)*
    ;

supplementalLoggingProps
    : SUPPLEMENTAL LOG supplementalLogGrpClause|supplementalIdKeyClause
    ;

supplementalLogGrpClause
    : GROUP logGroupName LP_ columnName (NO LOG)? (COMMA_ columnName (NO LOG)?)* RP_ ALWAYS?
    ;

supplementalIdKeyClause
    : DATA LP_ (ALL | PRIMARY KEY | UNIQUE INDEX? | FOREIGN KEY) (COMMA_ (ALL | PRIMARY KEY | UNIQUE INDEX? | FOREIGN KEY))* RP_ COLUMNS
    ;

alterSession
    : ALTER SESSION alterSessionOption
    ;

alterSessionOption
    : adviseClause
    | closeDatabaseLinkClause
    | commitInProcedureClause
    | securiyClause
    | parallelExecutionClause
    | resumableClause
    | shardDdlClause
    | syncWithPrimaryClause
    | alterSessionSetClause
    ;

adviseClause
    : ADVISE (COMMIT | ROLLBACK | NOTHING)
    ;

closeDatabaseLinkClause
    : CLOSE DATABASE LINK dbLink
    ;

commitInProcedureClause
    : (ENABLE | DISABLE) COMMIT IN PROCEDURE
    ;

securiyClause
    : (ENABLE | DISABLE) GUARD
    ;

parallelExecutionClause
    : (ENABLE | DISABLE | FORCE) PARALLEL (DML | DDL | QUERY) (PARALLEL numberLiterals)?
    ;

resumableClause
    : enableResumableClause | disableResumableClause
    ;

enableResumableClause
    : ENABLE RESUMABLE (TIMEOUT numberLiterals)? (NAME stringLiterals)?
    ;

disableResumableClause
    : DISABLE RESUMABLE
    ;

shardDdlClause
    : (ENABLE | DISABLE) SHARD DDL
    ;

syncWithPrimaryClause
    : SYNC WITH PRIMARY
    ;

alterSessionSetClause
    : SET alterSessionSetClauseOption
    ;

alterSessionSetClauseOption
    : parameterClause
    | editionClause
    | containerClause
    | rowArchivalVisibilityClause
    | defaultCollationClause
    | eventsClause
    ;

parameterClause
    : (parameterName EQ_ parameterValue)+
    ;

editionClause
    : EDITION EQ_ editionName
    ;

containerClause
    : CONTAINER EQ_ containerName (SERVICE EQ_ serviceName)?
    ;

rowArchivalVisibilityClause
    : ROW ARCHIVAL VISIBILITY EQ_ (ACTIVE | ALL)
    ;

defaultCollationClause
    : DEFAULT_COLLATION EQ_ (collationName | NONE)
    ;

eventsClause
    : (EVENTS EQ_? STRING_)+
    ;

alterDatabaseDictionary
    : ALTER DATABASE DICTIONARY (
    | ENCRYPT CREDENTIALS
    | REKEY CREDENTIALS
    | DELETE CREDENTIALS KEY
    )
    ;

alterDatabase
    : ALTER databaseClauses
    ( startupClauses
    | recoveryClauses
    | databaseFileClauses
    | logfileClauses
    | controlfileClauses
    | standbyDatabaseClauses
    | defaultSettingsClauses
    | instanceClauses
    | securityClause
    | prepareClause
    | dropMirrorCopy
    | lostWriteProtection
    | cdbFleetClauses
    | propertyClause )
    ;

databaseClauses
    : DATABASE databaseName? | PLUGGABLE DATABASE pdbName?
    ;

startupClauses
    : MOUNT ((STANDBY | CLONE) DATABASE)?
    | OPEN ((READ WRITE)? (RESETLOGS | NORESETLOGS)? (UPGRADE | DOWNGRADE)? | READ ONLY)
    ;

recoveryClauses
    : generalRecovery | managedStandbyRecovery | BEGIN BACKUP | END BACKUP
    ;

generalRecovery
    : RECOVER (AUTOMATIC)? (FROM locationName)? (
      (fullDatabaseRecovery | partialDatabaseRecovery | LOGFILE fileName)
      ((TEST | ALLOW NUMBER_ CORRUPTION | parallelClause)+)?
    | CONTINUE DEFAULT?
    | CANCEL
    )
    ;

fullDatabaseRecovery
    : STANDBY? DATABASE?
    ((UNTIL (CANCEL | TIME dateValue | CHANGE NUMBER_ | CONSISTENT)
    | USING BACKUP CONTROLFILE
    | SNAPSHOT TIME dateValue
    )+)?
    ;

partialDatabaseRecovery
    : TABLESPACE tablespaceName (COMMA_ tablespaceName)*
    | DATAFILE (fileName | fileNumber) (COMMA_ (fileName | fileNumber))*
    ;

managedStandbyRecovery
    : RECOVER (MANAGED STANDBY DATABASE
    ((USING (ARCHIVED | CURRENT) LOGFILE | DISCONNECT (FROM SESSION)?
    | NODELAY
    | UNTIL CHANGE NUMBER_
    | UNTIL CONSISTENT | USING INSTANCES (ALL | NUMBER_) | parallelClause)+
    | FINISH | CANCEL)?
    | TO LOGICAL STANDBY (databaseName | KEEP IDENTITY))
    ;

databaseFileClauses
    : RENAME FILE fileName (COMMA_ fileName)* TO fileName
    | createDatafileClause
    | alterDatafileClause
    | alterTempfileClause
    | moveDatafileClause
    ;

createDatafileClause
    : CREATE DATAFILE (fileName | fileNumber) (COMMA_ (fileName | fileNumber))*
    ( AS (fileSpecifications | NEW))?
    ;

fileSpecifications
    : fileSpecification (COMMA_ fileSpecification)*
    ;

fileSpecification
    : datafileTempfileSpec | redoLogFileSpec
    ;

datafileTempfileSpec
    : (fileName | asmFileName )? (SIZE sizeClause)? REUSE? autoextendClause?
    ;

autoextendClause
    : AUTOEXTEND (OFF | ON (NEXT sizeClause)? maxsizeClause?)
    ;

redoLogFileSpec
    : ((fileName | asmFileName)
    | LP_ (fileName | asmFileName) (COMMA_ (fileName | asmFileName))* RP_)?
    (SIZE sizeClause)? (BLOCKSIZE sizeClause)? REUSE?
    ;

alterDatafileClause
    : DATAFILE (fileName | NUMBER_) (COMMA_ (fileName | NUMBER_))*
    (ONLINE | OFFLINE (FOR DROP)? | RESIZE sizeClause | autoextendClause | END BACKUP | ENCRYPT | DECRYPT)
    ;

alterTempfileClause
    : TEMPFILE (fileName | NUMBER_) (COMMA_ (fileName | NUMBER_))*
    (RESIZE sizeClause | autoextendClause | DROP (INCLUDING DATAFILES)? | ONLINE | OFFLINE)
    ;

logfileClauses
    : ((ARCHIVELOG MANUAL? | NOARCHIVELOG )
    | NO? FORCE LOGGING
    | SET STANDBY NOLOGGING FOR (DATA AVAILABILITY | LOAD PERFORMANCE)
    | RENAME FILE fileName (COMMA_ fileName)* TO fileName
    | CLEAR UNARCHIVED? LOGFILE logfileDescriptor (COMMA_ logfileDescriptor)* (UNRECOVERABLE DATAFILE)?
    | addLogfileClauses
    | dropLogfileClauses
    | switchLogfileClause
    | supplementalDbLogging)
    ;

logfileDescriptor
    : GROUP INTEGER_ | LP_ fileName (COMMA_ fileName)* RP_ | fileName
    ;

addLogfileClauses
    : ADD STANDBY? LOGFILE
    (((INSTANCE SQ_ instanceName SQ_)? | (THREAD INTEGER_)?)
    (GROUP INTEGER_)? redoLogFileSpec (COMMA_ (GROUP INTEGER_)? redoLogFileSpec)*
    | MEMBER fileName REUSE? (COMMA_ fileName REUSE?)* TO logfileDescriptor (COMMA_ logfileDescriptor)*)
    ;

controlfileClauses
    : CREATE ((LOGICAL | PHYSICAL)? STANDBY | FAR SYNC INSTANCE) CONTROLFILE AS fileName REUSE?
    | BACKUP CONTROLFILE TO (fileName REUSE? | traceFileClause)
    ;

traceFileClause
    : TRACE (AS fileName REUSE?)? (RESETLOGS | NORESETLOGS)?
    ;

dropLogfileClauses
    : DROP STANDBY? LOGFILE
    (logfileDescriptor (COMMA_ logfileDescriptor)*
    | MEMBER fileName (COMMA_ fileName)*)
    ;

switchLogfileClause
    : SWITCH ALL LOGFILES TO BLOCKSIZE NUMBER_
    ;

supplementalDbLogging
    : (ADD | DROP) SUPPLEMENTAL LOG
    ( DATA
    | supplementalIdKeyClause
    | supplementalPlsqlClause
    | supplementalSubsetReplicationClause)
    ;

supplementalPlsqlClause
    : DATA FOR PROCEDURAL REPLICATION
    ;

supplementalSubsetReplicationClause
    : DATA SUBSET DATABASE REPLICATION
    ;

standbyDatabaseClauses
    : ((activateStandbyDbClause
    | maximizeStandbyDbClause
    | registerLogfileClause
    | commitSwitchoverClause
    | startStandbyClause
    | stopStandbyClause
    | convertDatabaseClause) parallelClause?)
    | (switchoverClause | failoverClause)
    ;

activateStandbyDbClause
    : ACTIVATE (PHYSICAL | LOGICAL)? STANDBY DATABASE (FINISH APPLY)?
    ;

maximizeStandbyDbClause
    : SET STANDBY DATABASE TO MAXIMIZE (PROTECTION | AVAILABILITY | PERFORMANCE)
    ;

registerLogfileClause
    : REGISTER (OR REPLACE)? (PHYSICAL | LOGICAL)? LOGFILE fileSpecifications (FOR logminerSessionName)?
    ;

commitSwitchoverClause
    : (PREPARE | COMMIT) TO SWITCHOVER
    ( TO (((PHYSICAL | LOGICAL)? PRIMARY | PHYSICAL? STANDBY) ((WITH | WITHOUT) SESSION SHUTDOWN (WAIT | NOWAIT)?)?
    | LOGICAL STANDBY)
    | CANCEL
    )?
    ;

startStandbyClause
    : START LOGICAL STANDBY APPLY IMMEDIATE? NODELAY? (NEW PRIMARY dbLink | INITIAL scnValue? | (SKIP_SYMBOL FAILED TRANSACTION | FINISH))?
    ;

stopStandbyClause
    : (STOP | ABORT) LOGICAL STANDBY APPLY
    ;

switchoverClause
    : SWITCHOVER TO databaseName (VERIFY | FORCE)?
    ;

convertDatabaseClause
    : CONVERT TO (PHYSICAL | SNAPSHOT) STANDBY
    ;

failoverClause
    : FAILOVER TO databaseName FORCE?
    ;

defaultSettingsClauses
    : DEFAULT EDITION EQ_ editionName
    | SET DEFAULT bigOrSmallFiles TABLESPACE
    | DEFAULT TABLESPACE tablespaceName
    | DEFAULT LOCAL? TEMPORARY TABLESPACE (tablespaceName | tablespaceGroupName)
    | RENAME GLOBAL_NAME TO databaseName DOT_ domain (DOT_ domain)*
    | ENABLE BLOCK CHANGE TRACKING (USING FILE fileName REUSE?)?
    | DISABLE BLOCK CHANGE TRACKING
    | NO? FORCE FULL DATABASE CACHING
    | CONTAINERS DEFAULT TARGET EQ_ (LP_ containerName RP_ | NONE)
    | flashbackModeClause
    | undoModeClause
    | setTimeZoneClause
    ;

setTimeZoneClause
    : SET TIME_ZONE EQ_ ((PLUS_ | MINUS_) dateValue | timeZoneRegion)
    ;

timeZoneRegion
    : STRING_
    ;

flashbackModeClause
    : FLASHBACK (ON | OFF)
    ;

undoModeClause
    : LOCAL UNDO (ON | OFF)
    ;

moveDatafileClause
    : MOVE DATAFILE LP_ (fileName | asmFileName | fileNumber) RP_
    (TO LP_ (fileName | asmFileName) RP_ )? REUSE? KEEP?
    ;

instanceClauses
    : (ENABLE | DISABLE) INSTANCE instanceName
    ;

securityClause
    : GUARD (ALL | STANDBY | NONE)
    ;

prepareClause
    : PREPARE MIRROR COPY copyName (WITH (UNPROTECTED | MIRROR | HIGH) REDUNDANCY)?
    ;

dropMirrorCopy
    : DROP MIRROR COPY mirrorName
    ;

lostWriteProtection
    : (ENABLE | DISABLE | REMOVE | SUSPEND)? LOST WRITE PROTECTION
    ;

cdbFleetClauses
    : leadCdbClause | leadCdbUriClause
    ;

leadCdbClause
    : SET LEAD_CDB EQ_  (TRUE | FALSE)
    ;

leadCdbUriClause
    : SET LEAD_CDB_URI EQ_ uriString
    ;

propertyClause
    : PROPERTY (SET | REMOVE) DEFAULT_CREDENTIAL EQ_ qualifiedCredentialName
    ;

alterSystem
    : ALTER SYSTEM alterSystemOption
    ;

alterSystemOption
    : archiveLogClause
    | checkpointClause
    | checkDatafilesClause
    | distributedRecovClauses
    | flushClause
    | endSessionClauses
    | alterSystemSwitchLogfileClause
    | suspendResumeClause
    | quiesceClauses
    | rollingMigrationClauses
    | rollingPatchClauses
    | alterSystemSecurityClauses
    | affinityClauses
    | shutdownDispatcherClause
    | registerClause
    | setClause
    | resetClause
    | relocateClientClause
    | cancelSqlClause
    | flushPasswordfileMetadataCacheClause
    ;

archiveLogClause
    : ARCHIVE LOG instanceClause? (sequenceClause | changeClause | currentClause | groupClause | logfileClause | nextClause | allClause) toLocationClause?
    ;

checkpointClause
    : CHECKPOINT (GLOBAL | LOCAL)?
    ;

checkDatafilesClause
    : CHECK DATAFILES (GLOBAL | LOCAL)?
    ;

distributedRecovClauses
    : (ENABLE | DISABLE) DISTRIBUTED RECOVERY
    ;

flushClause
    : FLUSH flushClauseOption
    ;

endSessionClauses
    : (disconnectSessionClause | killSessionClause) (IMMEDIATE | NOREPLY)?
    ;

alterSystemSwitchLogfileClause
    : SWITCH LOGFILE
    ;

suspendResumeClause
    : SUSPEND | RESUME
    ;

quiesceClauses
    : QUIESCE RESTRICTED | UNQUIESCE
    ;

rollingMigrationClauses
    : startRollingMigrationClause | stopRollingMigrationClause
    ;

rollingPatchClauses
    : startRollingPatchClause | stopRollingPatchClause
    ;

alterSystemSecurityClauses
    : restrictedSessionClause | setEncryptionWalletOpenClause | setEncryptionWalletCloseClause | setEncryptionKeyClause
    ;

affinityClauses
    : enableAffinityClause | disableAffinityClause
    ;

shutdownDispatcherClause
    : SHUTDOWN IMMEDIATE? dispatcherName
    ;

registerClause
    : REGISTER
    ;

setClause
    : SET alterSystemSetClause+
    ;

resetClause
    : RESET alterSystemResetClause+
    ;

relocateClientClause
    : RELOCATE CLIENT clientId
    ;

cancelSqlClause
    : CANCEL SQL SQ_ sessionId serialNumber (AT_ instanceId)? sqlId? SQ_
    ;

flushPasswordfileMetadataCacheClause
    : FLUSH PASSWORDFILE_METADATA_CACHE
    ;

instanceClause
    : INSTANCE instanceName
    ;

sequenceClause
    : SEQUENCE INTEGER_
    ;

changeClause
    : CHANGE INTEGER_
    ;

currentClause
    : CURRENT NOSWITCH?
    ;

groupClause
    : GROUP INTEGER_
    ;

logfileClause
    : LOGFILE logFileName (USING BACKUP CONTROLFILE)?
    ;

nextClause
    : NEXT
    ;

allClause
    : ALL
    ;

toLocationClause
    : TO logFileGroupsArchivedLocationName
    ;

flushClauseOption
    : sharedPoolClause | globalContextClause | bufferCacheClause | flashCacheClause | redoToClause
    ;

disconnectSessionClause
    : DISCONNECT SESSION STRING_ POST_TRANSACTION?
    ;

killSessionClause
    : KILL SESSION STRING_
    ;

startRollingMigrationClause
    : START ROLLING MIGRATION TO asmVersion
    ;

stopRollingMigrationClause
    : STOP ROLLING MIGRATION
    ;

startRollingPatchClause
    : START ROLLING PATCH
    ;

stopRollingPatchClause
    : STOP ROLLING PATCH
    ;

restrictedSessionClause
    : (ENABLE | DISABLE) RESTRICTED SESSION
    ;

setEncryptionWalletOpenClause
    : SET ENCRYPTION WALLET OPEN IDENTIFIED BY (walletPassword | hsmAuthString)
    ;

setEncryptionWalletCloseClause
    : SET ENCRYPTION WALLET CLOSE (IDENTIFIED BY (walletPassword | hsmAuthString))?
    ;

setEncryptionKeyClause
    : SET ENCRYPTION KEY (identifiedByWalletPassword | identifiedByHsmAuthString)
    ;

enableAffinityClause
    : ENABLE AFFINITY tableName (SERVICE serviceName)?
    ;

disableAffinityClause
    : DISABLE AFFINITY tableName
    ;

alterSystemSetClause
    : setParameterClause | useStoredOutlinesClause | globalTopicEnabledClause | dbRecoveryFileDestSizeClause | eventsClause
    ;

alterSystemResetClause
    : parameterName scopeClause*
    ;

sharedPoolClause
    : SHARED_POOL
    ;

globalContextClause
    : GLOBAL CONTEXT
    ;

bufferCacheClause
    : BUFFER_CACHE
    ;

flashCacheClause
    : FLASH_CACHE
    ;

redoToClause
    : REDO TO targetDbName (NO? CONFIRM APPLY)?
    ;

identifiedByWalletPassword
    : certificateId? IDENTIFIED BY walletPassword
    ;

identifiedByHsmAuthString
    : IDENTIFIED BY hsmAuthString (MIGRATE USING walletPassword)?
    ;

setParameterClause
    : parameterName EQ_ parameterValue (COMMA_ parameterValue)* alterSystemCommentClause? DEFERRED? containerCurrentAllClause? scopeClause*
    ;

useStoredOutlinesClause
    : USE_STORED_OUTLINES EQ_ (TRUE | FALSE | categoryName)
    ;

globalTopicEnabledClause
    : GLOBAL_TOPIC_ENABLED EQ_ (TRUE | FALSE)
    ;

dbRecoveryFileDestSizeClause
    : DB_RECOVERY_FILE_DEST_SIZE EQ_ INTEGER_ capacityUnit?
    ;

alterSystemCommentClause
    : COMMENT EQ_ stringLiterals
    ;

containerCurrentAllClause
    : CONTAINER EQ_ (CURRENT | ALL)
    ;

scopeClause
    : SCOPE EQ_ (MEMORY | SPFILE | BOTH) | SID EQ_ (sessionId | SQ_ ASTERISK_ SQ_)
    ;

analyze
    : (ANALYZE ((TABLE tableName| INDEX indexName) partitionExtensionClause? | CLUSTER clusterName))
    (validationClauses | LIST CHAINED ROWS intoTableClause? | DELETE SYSTEM? STATISTICS)
    ;

partitionExtensionClause
    : PARTITION (LP_ partitionName RP_ | FOR LP_ partitionKeyValue (COMMA_ partitionKeyValue) RP_)
    | SUBPARTITION (LP_ subpartitionName RP_ | FOR LP_ subpartitionKeyValue (COMMA_ subpartitionKeyValue) RP_)
    ;

validationClauses
    : VALIDATE REF UPDATE (SET DANGLING TO NULL)?
    | VALIDATE STRUCTURE (CASCADE (FAST | COMPLETE? (OFFLINE | ONLINE) intoTableClause?)?)?
    ;

intoTableClause
    : INTO tableName
    ;

associateStatistics
    : ASSOCIATE STATISTICS WITH (columnAssociation | functionAssociation) storageTableClause?
    ;

columnAssociation
    : COLUMNS tableName DOT_ columnName (COMMA_ tableName DOT_ columnName)* usingStatisticsType
    ;

functionAssociation
    : (FUNCTIONS function (COMMA_ function)*
    | PACKAGES packageName (COMMA_ packageName)*
    | TYPES typeName (COMMA_ typeName)*
    | INDEXES indexName (COMMA_ indexName)*
    | INDEXTYPES indexTypeName (COMMA_ indexTypeName)*)
    (usingStatisticsType | defaultCostClause (COMMA_ defaultSelectivityClause)? | defaultSelectivityClause (COMMA_ defaultCostClause)?)
    ;

storageTableClause
    : WITH (SYSTEM | USER) MANAGED STORAGE TABLES
    ;

usingStatisticsType
    : USING (statisticsTypeName | NULL)
    ;

defaultCostClause
    : DEFAULT COST LP_ cpuCost COMMA_ ioCost COMMA_ networkCost RP_
    ;

defaultSelectivityClause
    : DEFAULT SELECTIVITY defaultSelectivity
    ;

disassociateStatistics
    : DISASSOCIATE STATISTICS FROM
    (COLUMNS tableName DOT_ columnName (COMMA_ tableName DOT_ columnName)*
    | FUNCTIONS function (COMMA_ function)*
    | PACKAGES packageName (COMMA_ packageName)*
    | TYPES typeName (COMMA_ typeName)*
    | INDEXES indexName (COMMA_ indexName)*
    | INDEXTYPES indexTypeName (COMMA_ indexTypeName)*) FORCE?
    ;

audit
    : auditTraditional | auditUnified
    ;

auditTraditional
    : AUDIT (auditOperationClause (auditingByClause | IN SESSION CURRENT)? | auditSchemaObjectClause | NETWORK | DIRECT_PATH LOAD auditingByClause?)
    ( BY (SESSION | ACCESS))? (WHENEVER NOT? SUCCESSFUL)? (CONTAINER EQ_ (CURRENT | ALL))?
    ;

auditingByClause
    : BY username (COMMA_ username)*
    ;

auditOperationClause
    : (sqlStatementShortcut | ALL | ALL STATEMENTS) (COMMA_ sqlStatementShortcut | ALL | ALL STATEMENTS)*
    | (systemPrivilege | ALL PRIVILEGES) (COMMA_ systemPrivilege | ALL PRIVILEGES)*
    ;

sqlStatementShortcut
    : ALTER SYSTEM | CLUSTER | CREATE CLUSTER | ALTER CLUSTER | DROP CLUSTER | TRUNCATE CLUSTER | CONTEXT | CREATE CONTEXT | DROP CONTEXT
    | DATABASE LINK | CREATE DATABASE LINK | ALTER DATABASE LINK | DROP DATABASE LINK | DIMENSION | CREATE DIMENSION | ALTER DIMENSION | DROP DIMENSION
    | DIRECTORY | CREATE DIRECTORY | DROP DIRECTORY | INDEX | CREATE INDEX | ALTER INDEX | ANALYZE INDEX | DROP INDEX
    | MATERIALIZED VIEW | CREATE MATERIALIZED VIEW | ALTER MATERIALIZED VIEW | DROP MATERIALIZED VIEW | NOT EXISTS | OUTLINE | CREATE OUTLINE | ALTER OUTLINE | DROP OUTLINE
    | PLUGGABLE DATABASE | CREATE PLUGGABLE DATABASE | ALTER PLUGGABLE DATABASE | DROP PLUGGABLE DATABASE
    | PROCEDURE | CREATE FUNCTION | CREATE LIBRARY | CREATE PACKAGE | CREATE PACKAGE BODY | CREATE PROCEDURE | DROP FUNCTION | DROP LIBRARY | DROP PACKAGE | DROP PROCEDURE
    | PROFILE | CREATE PROFILE | ALTER PROFILE | DROP PROFILE | PUBLIC DATABASE LINK | CREATE PUBLIC DATABASE LINK | ALTER PUBLIC DATABASE LINK | DROP PUBLIC DATABASE LINK
    | PUBLIC SYNONYM | CREATE PUBLIC SYNONYM | DROP PUBLIC SYNONYM | ROLE | CREATE ROLE | ALTER ROLE | DROP ROLE | SET ROLE
    | ROLLBACK SEGMENT | CREATE ROLLBACK SEGMENT | ALTER ROLLBACK SEGMENT | DROP ROLLBACK SEGMENT | SEQUENCE | CREATE SEQUENCE | DROP SEQUENCE | SESSION | SYNONYM | CREATE SYNONYM | DROP SYNONYM
    | SYSTEM AUDIT | SYSTEM GRANT | TABLE | CREATE TABLE | DROP TABLE | TRUNCATE TABLE | TABLESPACE | CREATE TABLESPACE | ALTER TABLESPACE | DROP TABLESPACE
    | TRIGGER | CREATE TRIGGER | ALTER TRIGGER | DROP TRIGGER | ALTER TABLE | TYPE | CREATE TYPE | CREATE TYPE BODY | ALTER TYPE | DROP TYPE | DROP TYPE BODY
    | USER | CREATE USER | ALTER USER | DROP USER | VIEW | CREATE VIEW | DROP VIEW
    | ALTER SEQUENCE | COMMENT TABLE | DELETE TABLE | EXECUTE DIRECTORY | EXECUTE PROCEDURE | GRANT DIRECTORY | GRANT PROCEDURE | GRANT SEQUENCE | GRANT TABLE | GRANT TYPE
    | INSERT TABLE | LOCK TABLE | READ DIRECTORY | SELECT SEQUENCE | SELECT TABLE | UPDATE TABLE | WRITE DIRECTORY
    ;

auditSchemaObjectClause
    : (sqlOperation (COMMA_ sqlOperation)* | ALL) auditingOnClause
    ;

auditingOnClause
    : ON (DEFAULT | objectName | DIRECTORY directoryName | MINING MODEL modelName | SQL TRANSLATION PROFILE profileName)
    ;

sqlOperation
    : ALTER | AUDIT | COMMENT | DELETE | FLASHBACK | GRANT | INDEX | INSERT | LOCK | RENAME | SELECT | UPDATE | EXECUTE | READ
    ;

auditUnified
    : AUDIT (auditPolicyClause | contextClause)
    ;

noAuditUnified
    : NOAUDIT (noAuditPolicyClause | contextClause)
    ;

auditPolicyClause
    : POLICY policyName (byUsersWithRoles | (BY | EXCEPT) username (COMMA_ username)*)? (WHENEVER NOT? SUCCESSFUL)?
    ;

noAuditPolicyClause
    : POLICY policyName (byUsersWithRoles | BY username (COMMA_ username)*)? (WHENEVER NOT? SUCCESSFUL)?
    ;

byUsersWithRoles
    : BY USERS WITH GRANTED ROLES roleName (COMMA_ roleName)*
    ;

contextClause
    : contextNamespaceAttributesClause (COMMA_ contextNamespaceAttributesClause)* (BY username (COMMA_ username)*)?
    ;

contextNamespaceAttributesClause
    : CONTEXT NAMESPACE namespace ATTRIBUTES attributeName (COMMA_ attributeName)*
    ;

comment
    : COMMENT ON (
    | AUDIT POLICY policyName
    | COLUMN (tableName | viewName | materializedViewName) DOT_ columnName
    | EDITION editionName
    | INDEXTYPE indexTypeName
    | MATERIALIZED VIEW materializedViewName
    | MINING MODEL modelName
    | OPERATOR operatorName
    | TABLE (tableName | viewName)
    ) IS STRING_
    ;

flashbackDatabase
    : FLASHBACK STANDBY? PLUGGABLE? DATABASE databaseName?
    ( TO (scnTimestampClause | restorePointClause)
    | TO BEFORE (scnTimestampClause | RESETLOGS))
    ;

scnTimestampClause
    : (SCN | TIMESTAMP) scnTimestampExpr
    ;

restorePointClause
    : RESTORE POINT restorePoint
    ;

flashbackTable
    : FLASHBACK TABLE tableName TO (
    (scnTimestampClause | restorePointClause) ((ENABLE | DISABLE) TRIGGERS)?
    | BEFORE DROP renameToTable? )
    ;

renameToTable
    : RENAME TO tableName
    ;

purge
    : PURGE (TABLE tableName
    | INDEX indexName
    | TABLESPACE tablespaceName (USER username)?
    | TABLESPACE SET tablespaceSetName (USER username)?
    | RECYCLEBIN
    | DBA_RECYCLEBIN)
    ;

rename
    : RENAME name TO name
    ;

createDatabase
    : CREATE DATABASE databaseName? createDatabaseClauses+
    ;

createDatabaseClauses
    : USER SYS IDENTIFIED BY password
    | USER SYSTEM IDENTIFIED BY password
    | CONTROLFILE REUSE
    | MAXDATAFILES INTEGER_
    | MAXINSTANCES INTEGER_
    | CHARACTER SET databaseCharset
    | NATIONAL CHARACTER SET nationalCharset
    | SET DEFAULT bigOrSmallFiles TABLESPACE
    | databaseLoggingClauses
    | tablespaceClauses
    | setTimeZoneClause
    | bigOrSmallFiles? USER_DATA TABLESPACE tablespaceName DATAFILE datafileTempfileSpec (COMMA_ datafileTempfileSpec)*
    | enablePluggableDatabase
    | databaseName USING MIRROR COPY mirrorName
    ;

databaseLoggingClauses
    : LOGFILE (GROUP INTEGER_)? fileSpecification (COMMA_ (GROUP INTEGER_)? fileSpecification)*
    | MAXLOGFILES INTEGER_
    | MAXLOGMEMBERS INTEGER_
    | MAXLOGHISTORY INTEGER_
    | (ARCHIVELOG | NOARCHIVELOG)
    | FORCE LOGGING
    | SET STANDBY NOLOGGING FOR (DATA AVAILABILITY | LOAD PERFORMANCE)
    ;

tablespaceClauses
    : EXTENT MANAGEMENT LOCAL
    | DATAFILE fileSpecifications
    | SYSAUX DATAFILE fileSpecifications
    | defaultTablespace
    | defaultTempTablespace
    | undoTablespace
    ;

defaultTablespace
    : DEFAULT TABLESPACE tablespaceName (DATAFILE datafileTempfileSpec)? extentManagementClause?
    ;

defaultTempTablespace
    : bigOrSmallFiles? DEFAULT
    (TEMPORARY TABLESPACE | LOCAL TEMPORARY TABLESPACE FOR (ALL | LEAF)) tablespaceName
    (TEMPFILE fileSpecifications)? extentManagementClause?
    ;

undoTablespace
    : bigOrSmallFiles? UNDO TABLESPACE tablespaceName (DATAFILE fileSpecifications)?
    ;

bigOrSmallFiles
    : BIGFILE | SMALLFILE
    ;

extentManagementClause
    : EXTENT MANAGEMENT LOCAL (AUTOALLOCATE | UNIFORM (SIZE sizeClause)?)?
    ;

enablePluggableDatabase
    : ENABLE PLUGGABLE DATABASE
    (SEED fileNameConvert? (SYSTEM tablespaceDatafileClauses)? (SYSAUX tablespaceDatafileClauses)?)? undoModeClause?
    ;

fileNameConvert
    : FILE_NAME_CONVERT EQ_ (LP_ replaceFileNamePattern (COMMA_ replaceFileNamePattern)* RP_| NONE)
    ;

replaceFileNamePattern
    : filenamePattern COMMA_ filenamePattern
    ;

tablespaceDatafileClauses
    : DATAFILES (SIZE sizeClause | autoextendClause)+
    ;

createDatabaseLink
    : CREATE SHARED? PUBLIC? DATABASE LINK dbLink
    (connectToClause | dbLinkAuthentication)* (USING connectString)?
    ;

alterDatabaseLink
    : ALTER SHARED? PUBLIC? DATABASE LINK dbLink (
    | CONNECT TO username IDENTIFIED BY password dbLinkAuthentication?
    | dbLinkAuthentication
    )
    ;

dropDatabaseLink
    : DROP PUBLIC? DATABASE LINK dbLink
    ;

connectToClause
    : CONNECT TO (CURRENT_USER | username IDENTIFIED BY password dbLinkAuthentication?)
    ;

dbLinkAuthentication
    : AUTHENTICATED BY username IDENTIFIED BY password
    ;

createDimension
    : CREATE DIMENSION dimensionName levelClause+ (hierarchyClause | attributeClause+ | extendedAttrbuteClause)+
    ;

levelClause
    : LEVEL level IS (columnName | LP_ columnName (COMMA_ columnName)* RP_) (SKIP_SYMBOL WHEN NULL)?
    ;

hierarchyClause
    : HIERARCHY hierarchyName LP_ level (CHILD OF level)+ dimensionJoinClause* RP_
    ;

dimensionJoinClause
    : JOIN KEY (columnName | LP_ columnName (COMMA_ columnName)* RP_) REFERENCES level
    ;

attributeClause
    : ATTRIBUTE level DETERMINES (columnName | LP_ columnName (COMMA_ columnName)* RP_)
    ;

extendedAttrbuteClause
    : ATTRIBUTE attributeName (LEVEL level DETERMINES (columnName | LP_ columnName (COMMA_ columnName)* RP_))+
    ;

alterDimension
    : ALTER DIMENSION dimensionName (alterDimensionAddClause* | alterDimensionDropClause* | COMPILE)
    ;

alterDimensionAddClause
    : ADD (levelClause | hierarchyClause | attributeClause | extendedAttrbuteClause)
    ;

alterDimensionDropClause
    : DROP (LEVEL level (RESTRICT | CASCADE)?
    | HIERARCHY hierarchyName
    | ATTRIBUTE attributeName (LEVEL level (COLUMN columnName (COMMA_ COLUMN columnName)*)?)?)
    ;

dropDimension
    : DROP DIMENSION dimensionName
    ;

dropDirectory
    : DROP DIRECTORY directoryName
    ;

dropType
    : DROP TYPE typeName (FORCE|VALIDATE)?
    ;

parameterDeclaration
    : parameterName (IN? dataType ((ASSIGNMENT_OPERATOR_ | DEFAULT) expr)? | IN? OUT NOCOPY? dataType)?
    ;

sharingClause
    : SHARING EQ_ (METADATA | NONE)
    ;

invokerRightsClause
    : AUTHID (CURRENT_USER | DEFINER)
    ;

accessibleByClause
    : ACCESSIBLE BY LP_ accessor (COMMA_ accessor)* RP_
    ;

accessor
    : unitKind unitName
    ;

unitKind
    : FUNCTION | PROCEDURE | PACKAGE | TRIGGER | TYPE
    ;

defaultCollationoOptionClause
    : DEFAULT COLLATION collationOption
    ;

collationOption
    : USING_NLS_COMP
    ;

deterministicClause
    : DETERMINISTIC
    ;

parallelEnableClause
    : PARALLEL_ENABLE (LP_ PARTITION argument BY (ANY
    | (HASH | RANGE) LP_ columnName (COMMA_ columnName)* RP_ streamingCluase?
    | VALUE LP_ columnName RP_) RP_)?
    ;

streamingCluase
    : (ORDER | CLUSTER) expr BY LP_ columnName (COMMA_ columnName)* RP_
    ;

resultCacheClause
    : RESULT_CACHE (RELIES_ON LP_ (dataSource (COMMA_ dataSource)*)? RP_)?
    ;

aggregateClause
    : AGGREGATE USING implementationType
    ;

pipelinedClause
    : PIPELINED ((USING implementationType)?
    | (ROW | TABLE) POLYMORPHIC (USING implementationPackage)?)
    ;

sqlMacroClause
    : SQL_MARCO
    ;

callSpec
    : javaDeclaration | cDeclaration
    ;

javaDeclaration
    : LANGUAGE JAVA NAME STRING_
    ;

cDeclaration
    : (LANGUAGE SINGLE_C | EXTERNAL)
    ((NAME name)? LIBRARY libName| LIBRARY libName (NAME name)?)
    (AGENT IN RP_ argument (COMMA_ argument)* LP_)?
    (WITH CONTEXT)?
    (PARAMETERS LP_ externalParameter (COMMA_ externalParameter)* RP_)?
    ;

externalParameter
    : (CONTEXT
    | SELF (TDO | property)?
    | (parameterName | RETURN) property? (BY REFERENCE)? externalDatatype)
    ;

property
    : (INDICATOR (STRUCT | TDO)? | LENGTH | DURATION | MAXLEN | CHARSETID | CHARSETFORM)
    ;

alterAnalyticView
    : ALTER ANALYTIC VIEW analyticViewName (RENAME TO analyticViewName | COMPILE)
    ;

alterAttributeDimension
    : ALTER ATTRIBUTE DIMENSION (schemaName DOT_)? attributeDimensionName (RENAME TO attributeDimensionName | COMPILE)
    ;

createSequence
    : CREATE SEQUENCE (schemaName DOT_)? sequenceName (SHARING EQ_ (METADATA | DATA | NONE))? createSequenceClause*
    ;

createSequenceClause
    : (INCREMENT BY | START WITH) INTEGER_
    | (MAXVALUE INTEGER_ | NOMAXVALUE)
    | (MINVALUE INTEGER_ | NOMINVALUE)
    | (CYCLE | NOCYCLE)
    | (CACHE INTEGER_ | NOCACHE)
    | (ORDER | NOORDER)
    | (KEEP | NOKEEP)
    | (SCALE (EXTEND | NOEXTEND) | NOSCALE)
    | (SHARD (EXTEND | NOEXTEND) | NOSHARD)
    | (SESSION | GLOBAL)
    ;

alterSequence
    : ALTER SEQUENCE (schemaName DOT_)? sequenceName alterSequenceClause+
    ;

alterSequenceClause
   : (INCREMENT BY | START WITH) INTEGER_
   | MAXVALUE INTEGER_
   | NOMAXVALUE
   | MINVALUE INTEGER_
   | NOMINVALUE
   | RESTART
   | CYCLE
   | NOCYCLE
   | CACHE INTEGER_
   | NOCACHE
   | ORDER
   | NOORDER
   | KEEP
   | NOKEEP
   | SCALE (EXTEND | NOEXTEND)
   | NOSCALE
   | SHARD (EXTEND | NOEXTEND)
   | NOSHARD
   | SESSION
   | GLOBAL
   ;

createContext
    : CREATE (OR REPLACE)? CONTEXT namespace USING (schemaName DOT_)? packageName sharingClause? (initializedClause | accessedClause)?
    ;

initializedClause
    : INITIALIZED (EXTERNALLY | GLOBALLY)
    ;

accessedClause
    : ACCESSED GLOBALLY
    ;

createSPFile
    : CREATE SPFILE (EQ_ spfileName)? FROM (PFILE (EQ_ pfileName)? (AS COPY)? | MEMORY)
    ;

createPFile
    : CREATE PFILE (EQ_ pfileName)? FROM (SPFILE (EQ_ spfileName)? (AS COPY)? | MEMORY)
    ;

createControlFile
    : CREATE CONTROLFILE REUSE? SET? DATABASE databaseName (logfileForControlClause | RESETLOGS | NORESETLOGS | DATAFILE fileSpecifications
    |( MAXLOGFILES INTEGER_
    | MAXLOGMEMBERS INTEGER_
    | MAXLOGHISTORY INTEGER_
    | MAXDATAFILES INTEGER_
    | MAXINSTANCES INTEGER_
    | ARCHIVELOG
    | NOARCHIVELOG
    | FORCE LOGGING
    | SET STANDBY NOLOGGING FOR (DATA AVAILABILITY | LOAD PERFORMANCE)))+
    characterSetClause?
    ;

resetLogsOrNot
   :  ( RESETLOGS | NORESETLOGS)? (DATAFILE fileSpecifications)?
   ;

logfileForControlClause
    : LOGFILE (GROUP INTEGER_)? fileSpecification (COMMA_ (GROUP INTEGER_)? fileSpecification)*
    ;

characterSetClause
    : CHARACTER SET characterSetName
    ;

createFlashbackArchive
   : CREATE FLASHBACK ARCHIVE DEFAULT? flashbackArchiveName tablespaceClause
     flashbackArchiveQuota? (NO? OPTIMIZE DATA)? flashbackArchiveRetention
   ;

flashbackArchiveQuota
    : QUOTA INTEGER_ quotaUnit
    ;

flashbackArchiveRetention
    : RETENTION INTEGER_ (YEAR | MONTH | DAY)
    ;

alterFlashbackArchive
    : ALTER FLASHBACK ARCHIVE flashbackArchiveName
    ( SET DEFAULT
    | (ADD | MODIFY) TABLESPACE tablespaceName flashbackArchiveQuota?
    | REMOVE TABLESPACE tablespaceName
    | MODIFY RETENTION? flashbackArchiveRetention
    | PURGE purgeClause
    | NO? OPTIMIZE DATA)
    ;

purgeClause
    : ALL
    | BEFORE (SCN expr | TIMESTAMP expr)
    ;

dropFlashbackArchive
    : DROP FLASHBACK ARCHIVE flashbackArchiveName
    ;

createDiskgroup
    : CREATE DISKGROUP diskgroupName ((HIGH | NORMAL | FLEX | EXTENDED (SITE siteName)? | EXTERNAL) REDUNDANCY)? diskClause+
    ( ATTRIBUTE attributeName EQ_ attributeValue (COMMA_ attributeName EQ_ attributeValue)*)?
    ;

diskClause
    : (QUORUM | REGULAR)? (FAILGROUP diskgroupName)? DISK qualifieDiskClause (COMMA_ qualifieDiskClause)*
    ;

qualifieDiskClause
    : searchString (NAME diskName)? (SIZE sizeClause)? (FORCE | NOFORCE)?
    ;

dropDiskgroup
    : DROP DISKGROUP diskgroupName contentsClause?
    ;

contentsClause
    : ((FORCE? INCLUDING) | EXCLUDING) CONTENTS
    ;

createRollbackSegment
    : CREATE PUBLIC? ROLLBACK SEGMENT rollbackSegment ((TABLESPACE tablespaceName) | storageClause)*
    ;

dropRollbackSegment
    : DROP ROLLBACK SEGMENT rollbackSegment
    ;

createLockdownProfile
    : CREATE LOCKDOWN PROFILE profileName (staticBaseProfile | dynamicBaseProfile)?
    ;

staticBaseProfile
    : FROM profileName
    ;

dynamicBaseProfile
    : INCLUDING profileName
    ;

dropLockdownProfile
    : DROP LOCKDOWN PROFILE profileName
    ;

createInmemoryJoinGroup
    : CREATE INMEMORY JOIN GROUP (schemaName DOT_)? joinGroupName
     LP_ tableColumnClause COMMA_ tableColumnClause (COMMA_ tableColumnClause)* RP_
    ;

tableColumnClause
    : (schemaName DOT_)? tableName LP_ columnName RP_
    ;

alterInmemoryJoinGroup
    : ALTER INMEMORY JOIN GROUP (schemaName DOT_)? joinGroupName (ADD | REMOVE) LP_ tableName LP_ columnName RP_ RP_
    ;

dropInmemoryJoinGroup
    : DROP INMEMORY JOIN GROUP (schemaName DOT_)? joinGroupName
    ;

createRestorePoint
    : CREATE CLEAN? RESTORE POINT restorePointName (FOR PLUGGABLE DATABASE pdbName)?
      (AS OF (TIMESTAMP | SCN) expr)?
      (PRESERVE | GUARANTEE FLASHBACK DATABASE)?
    ;

dropRestorePoint
    : DROP RESTORE POINT restorePointName (FOR PLUGGABLE DATABASE pdbName)?
    ;

dropOperator
    : DROP OPERATOR (schemaName DOT_)? operatorName FORCE?
    ;

alterLibrary
    : ALTER LIBRARY (schemaName DOT_)? libraryName (libraryCompileClause | EDITIONABLE | NONEDITIONABLE)
    ;

libraryCompileClause
    : COMPILE DEBUG? compilerParametersClause* (REUSE SETTINGS)?
    ;

alterMaterializedZonemap
    : ALTER MATERIALIZED ZONEMAP (schemaName DOT_)? zonemapName
    ( alterZonemapAttributes
    | zonemapRefreshClause
    | (ENABLE | DISABLE) PRUNING
    | COMPILE
    | REBUILD
    | UNUSABLE)
    ;

alterZonemapAttributes
    : (PCTFREE INTEGER_ | PCTUSED INTEGER_ | CACHE | NOCACHE)+
    ;

zonemapRefreshClause
    : REFRESH (FAST | COMPLETE | FORCE)?
      (ON (DEMAND | COMMIT | LOAD | DATA MOVEMENT | LOAD DATA MOVEMENT) )?
    ;

alterJava
   : ALTER JAVA (SOURCE | CLASS) objectName resolveClauses? (COMPILE | RESOLVE | invokerRightsClause)
   ;

resolveClauses
    : RESOLVER LP_ resolveClause+ RP_
    ;

resolveClause
    : LP_ matchString DOT_? (schemaName | MINUS_) RP_
    ;

alterAuditPolicy
    : ALTER AUDIT POLICY policyName
      ((ADD | DROP) subAuditClause)?
      (CONDITION (DROP | STRING_ EVALUATE PER (STATEMENT | SESSION | INSTANCE)))?
    ;

subAuditClause
    : privilegeAuditClause
    | actionAuditClause
    | roleAuditClause
    | ONLY TOPLEVEL
    | (privilegeAuditClause actionAuditClause)
    | (privilegeAuditClause roleAuditClause)
    | (privilegeAuditClause ONLY TOPLEVEL)
    | (actionAuditClause roleAuditClause)
    | (actionAuditClause ONLY TOPLEVEL)
    | (actionAuditClause roleAuditClause ONLY TOPLEVEL)
    | (privilegeAuditClause roleAuditClause ONLY TOPLEVEL)
    | (privilegeAuditClause actionAuditClause ONLY TOPLEVEL)
    | (privilegeAuditClause actionAuditClause roleAuditClause)
    | (privilegeAuditClause actionAuditClause roleAuditClause ONLY TOPLEVEL)
    ;

privilegeAuditClause
    : PRIVILEGES systemPrivilegeClause (COMMA_ systemPrivilegeClause)*
    ;

actionAuditClause
    : (standardActions | componentActions)*
    ;

standardActions
    : ACTIONS standardActionsClause standardActionsClause*
    ;

standardActionsClause
    : (objectAction ON (DIRECTORY directoryName | MINING MODEL objectName | objectName) | systemAction)
    ;

objectAction
    : ALL
    | ALTER
    | AUDIT
    | COMMENT
    | CREATE
    | DELETE
    | EXECUTE
    | FLASHBACK
    | GRANT
    | INDEX
    | INSERT
    | LOCK
    | READ
    | RENAME
    | SELECT
    | UPDATE
    | USE
    | WRITE
    ;

systemAction
    : ALL
    | ALTER EDITION
    | ALTER REWRITE EQUIVALENCE
    | ALTER SUMMARY
    | ALTER TRACING
    | CREATE BITMAPFILE
    | CREATE CONTROL FILE
    | CREATE DATABASE
    | CREATE SUMMARY
    | DECLARE REWRITE EQUIVALENCE
    | DROP BITMAPFILE
    | DROP DATABASE
    | DROP REWRITE EQUIVALENCE
    | DROP SUMMARY
    | FLASHBACK DATABASE
    | MERGE
    | SAVEPOINT
    | SET CONSTRAINTS
    | UNDROP OBJECT
    | UPDATE INDEXES
    | UPDATE JOIN INDEX
    | VALIDATE INDEX
    ;

componentActions
    : ACTIONS COMPONENT EQ_ (DATAPUMP | DIRECT_LOAD | OLS | XS) componentAction (COMMA_ componentAction)*
    | DV componentAction ON objectName (COMMA_ componentAction ON objectName)*
    ;

componentAction
    : ALL
    | dataDumpAction
    | directLoadAction
    | labelSecurityAction
    | securityAction
    | databaseVaultAction
    ;

dataDumpAction
    : EXPORT
    | IMPORT
    ;

directLoadAction
    : LOAD
    ;

labelSecurityAction
    : CREATE POLICY
    | ALTER POLICY
    | DROP POLICY
    | APPLY POLICY
    | REMOVE POLICY
    | SET AUTHORIZATION
    | PRIVILEGED ACTION
    | ENABLE POLICY
    | DISABLE POLICY
    | SUBSCRIBE OID
    | UNSUBSCRIBE OID
    | CREATE DATA LABEL
    | ALTER DATA LABEL
    | DROP DATA LABEL
    | CREATE LABEL COMPONENT
    | ALTER LABEL COMPONENTS
    | DROP LABEL COMPONENTS
    ;

securityAction
    : CREATE USER
    | UPDATE USER
    | DELETE USER
    | CREATE ROLE
    | UPDATE ROLE
    | DELETE ROLE
    | GRANT ROLE
    | REVOKE ROLE
    | ADD PROXY
    | REMOVE PROXY
    | SET USER PASSWORD
    | SET USER VERIFIER
    | CREATE ROLESET
    | UPDATE ROLESET
    | DELETE ROLESET
    | CREATE SECURITY CLASS
    | UPDATE SECURITY CLASS
    | DELETE SECURITY CLASS
    | CREATE NAMESPACE TEMPLATE
    | UPDATE NAMESPACE TEMPLATE
    | DELETE NAMESPACE TEMPLATE
    | CREATE ACL
    | UPDATE ACL
    | DELETE ACL
    | CREATE DATA SECURITY
    | UPDATE DATA SECURITY
    | DELETE DATA SECURITY
    | ENABLE DATA SECURITY
    | DISABLE DATA SECURITY
    | ADD GLOBAL CALLBACK
    | DELETE GLOBAL CALLBACK
    | ENABLE GLOBAL CALLBACK
    | ENABLE ROLE
    | DISABLE ROLE
    | SET COOKIE
    | SET INACTIVE TIMEOUT
    | CREATE SESSION
    | DESTROY SESSION
    | SWITCH USER
    | ASSIGN USER
    | CREATE SESSION NAMESPACE
    | DELETE SESSION NAMESPACE
    | CREATE NAMESPACE ATTRIBUTE
    | GET NAMESPACE ATTRIBUTE
    | SET NAMESPACE ATTRIBUTE
    | DELETE NAMESPACE ATTRIBUTE
    | SET USER PROFILE
    | GRANT SYSTEM PRIVILEGE
    | REVOKE SYSTEM PRIVILEGE
    ;

databaseVaultAction
    : REALM VIOLATION
    | REALM SUCCESS
    | REALM ACCESS
    | RULE SET FAILURE
    | RULE SET SUCCESS
    | RULE SET EVAL
    | FACTOR ERROR
    | FACTOR NULL
    | FACTOR VALIDATE ERROR
    | FACTOR VALIDATE FALSE
    | FACTOR TRUST LEVEL NULL
    | FACTOR TRUST LEVEL NEG
    | FACTOR ALL
    ;

roleAuditClause
    : ROLES roleName (COMMA_ roleName)*
    ;

alterCluster
    : ALTER CLUSTER clusterName
    (physicalAttributesClause
    | SIZE sizeClause
    | (MODIFY PARTITION partitionName)? allocateExtentClause
    | deallocateUnusedClause
    | (CACHE | NOCACHE))+ (parallelClause)?
    ;

alterOperator
    : ALTER OPERATOR operatorName (addBindingClause | dropBindingClause | COMPILE)
    ;

addBindingClause
    : ADD BINDING LP_ parameterType (COMMA_ parameterType)* RP_ RETURN (LP_ returnType RP_ | NUMBER) implementationClause? usingFunctionClause
    ;

implementationClause
    : (ANCILLARY TO primaryOperatorClause (COMMA_ primaryOperatorClause)*) | contextClauseWithOpeartor
    ;

primaryOperatorClause
    : operatorName LP_ parameterType (COMMA_ parameterType)* RP_
    ;

contextClauseWithOpeartor
    : withIndexClause? withColumnClause?
    ;

withIndexClause
    : WITH INDEX CONTEXT COMMA_ SCAN CONTEXT implementationType (COMPUTE ANCILLARY DATA)?
    ;

withColumnClause
    : WITH COLUMN CONTEXT
    ;

usingFunctionClause
    : USING (packageName DOT_ | typeName DOT_)? functionName
    ;

dropBindingClause
    : DROP BINDING LP_ parameterType (COMMA_ parameterType)* RP_ FORCE?
    ;

alterDiskgroup
    : ALTER DISKGROUP ((diskgroupName ((((addDiskClause | dropDiskClause) (COMMA_ (addDiskClause | dropDiskClause))* | resizeDiskClause) (rebalanceDiskgroupClause)?)
    | replaceDiskClause
    | renameDiskClause
    | diskOnlineClause
    | diskOfflineClause
    | rebalanceDiskgroupClause
    | checkDiskgroupClause
    | diskgroupTemplateClauses
    | diskgroupDirectoryClauses
    | diskgroupAliasClauses
    | diskgroupVolumeClauses
    | diskgroupAttributes
    | modifyDiskgroupFile
    | dropDiskgroupFileClause
    | convertRedundancyClause
    | usergroupClauses
    | userClauses
    | filePermissionsClause
    | fileOwnerClause
    | scrubClause
    | quotagroupClauses
    | filegroupClauses))
    | (((diskgroupName (COMMA_ diskgroupName)*) | ALL) (undropDiskClause | diskgroupAvailability | enableDisableVolume)))
    ;

addDiskClause
    : ADD ((SITE siteName)? (QUORUM | REGULAR)? (FAILGROUP failgroupName)? DISK qualifiedDiskClause (COMMA_ qualifiedDiskClause)*)+
    ;

qualifiedDiskClause
    : searchString (NAME diskName)? (SIZE sizeClause)? (FORCE | NOFORCE)?
    ;

dropDiskClause
    : DROP ((QUORUM | REGULAR)? DISK diskName (FORCE | NOFORCE)? (COMMA diskName (FORCE | NOFORCE)?)*
    | DISKS IN (QUORUM | REGULAR)? FAILGROUP failgroupName (FORCE | NOFORCE)? (COMMA_ failgroupName (FORCE | NOFORCE)?)*)
    ;

resizeDiskClause
    : RESIZE (ALL | DISKS IN FAILGROUP failgroupName) (SIZE sizeClause)?
    ;

rebalanceDiskgroupClause
    : REBALANCE ((((WITH withPhases) | (WITHOUT withoutPhases))? (POWER INTEGER_)? (WAIT | NOWAIT)?)
    | (MODIFY POWER (INTEGER_)?))?
    ;

withPhases
    : withPhase (COMMA_ withPhase)*
    ;

withPhase
    : RESTORE | BALANCE | PREPARE | COMPACT
    ;

withoutPhases
    : withoutPhase (COMMA_ withoutPhase)*
    ;

withoutPhase
    : BALANCE | PREPARE | COMPACT
    ;

replaceDiskClause
    : REPLACE DISK diskName WITH pathString (FORCE | NOFORCE)?
    (COMMA_ diskName WITH pathString (FORCE | NOFORCE)?)*
    (POWER INTEGER_)? (WAIT | NOWAIT)?
    ;

renameDiskClause
    : RENAME (DISK diskName TO diskName (COMMA_ diskName TO diskName)* | DISKS ALL)
    ;

diskOnlineClause
    : ONLINE (((QUORUM | REGULAR)? DISK diskName (COMMA_ diskName)*
    | DISKS IN (QUORUM | REGULAR)? FAILGROUP failgroupName (COMMA_ failgroupName)*)+
    | ALL) (POWER INTEGER_)? (WAIT | NOWAIT)?
    ;

diskOfflineClause
    : OFFLINE ((QUORUM | REGULAR)? DISK diskName (COMMA_ diskName)*
    | DISKS IN (QUORUM | REGULAR)? FAILGROUP failgroupName (COMMA_ failgroupName)*)+ (timeoutClause)?
    ;

timeoutClause
    : DROP AFTER INTEGER_ timeUnit
    ;

checkDiskgroupClause
    : CHECK ALL? (REPAIR | NOREPAIR)?
    ;

diskgroupTemplateClauses
    : (((ADD | MODIFY) TEMPLATE templateName qualifiedTemplateClause (COMMA_ templateName qualifiedTemplateClause)*)
    | (DROP TEMPLATE templateName (COMMA_ templateName)*))
    ;

qualifiedTemplateClause
    : (ATTRIBUTE | ATTRIBUTES) LP_ redundancyClause stripingClause diskRegionClause RP_
    ;

redundancyClause
    : (MIRROR | HIGH | UNPROTECTED | PARITY)?
    ;

stripingClause
    : (FINE | COARSE)?
    ;

diskRegionClause
    : (HOT | COLD)? (MIRRORHOT | MIRRORCOLD)?
    ;

diskgroupDirectoryClauses
    : (ADD DIRECTORY fileName (COMMA_ fileName)*
    | DROP DIRECTORY fileName (FORCE | NOFORCE)? (COMMA_ fileName (FORCE | NOFORCE)?)*
    | RENAME DIRECTORY directoryName TO directoryName (COMMA_ directoryName TO directoryName)*)
    ;

diskgroupAliasClauses
    : ((ADD ALIAS aliasName FOR fileName (COMMA_ aliasName FOR fileName)*)
    | (DROP ALIAS aliasName (COMMA_ aliasName)*)
    | (RENAME ALIAS aliasName TO aliasName (COMMA_ aliasName TO aliasName)*))
    ;

diskgroupVolumeClauses
    : (addVolumeClause
    | modifyVolumeClause
    | RESIZE VOLUME asmVolumeName SIZE sizeClause
    | DROP VOLUME asmVolumeName)
    ;

addVolumeClause
    : ADD VOLUME asmVolumeName SIZE sizeClause (redundancyClause)? (STRIPE_WIDTH INTEGER_ (K | M))? (STRIPE_COLUMNS INTEGER_)? (ATTRIBUTE (diskRegionClause))?
    ;

modifyVolumeClause
    : MODIFY VOLUME asmVolumeName (ATTRIBUTE (diskRegionClause))? (MOUNTPATH mountpathName)? (USAGE usageName)?
    ;

diskgroupAttributes
    : SET ATTRIBUTE attributeName EQ_ attributeValue
    ;

modifyDiskgroupFile
    : MODIFY FILE fileName ATTRIBUTE LP_ diskRegionClause RP_ (COMMA_ fileName ATTRIBUTE ( diskRegionClause ))*
    ;

dropDiskgroupFileClause
    : DROP FILE fileName (COMMA_ fileName)*
    ;

convertRedundancyClause
    : CONVERT REDUNDANCY TO FLEX
    ;

usergroupClauses
    : (ADD USERGROUP SQ_ usergroupName SQ_ WITH MEMBER SQ_ username SQ_ (COMMA_ SQ_ username SQ_)*
    | MODIFY USERGROUP usergroupName (ADD | DROP) MEMBER username (COMMA_ username)*
    | DROP USERGROUP SQ_ usergroupName SQ_)
    ;

userClauses
    : (ADD USER username (COMMA_ username)*
    | DROP USER SQ_ username SQ_ (COMMA_ SQ_ username SQ_)* (CASCADE)?
    | REPLACE USER SQ_ username SQ_ WITH SQ_ username SQ_ (COMMA_ SQ_ username SQ_ WITH SQ_ username SQ_)*)
    ;

filePermissionsClause
    : SET PERMISSION (OWNER | GROUP | OTHER) EQ_ (NONE | READ ONLY | READ WRITE) (COMMA_ (OWNER | GROUP | OTHER | ALL)
    EQ_ (NONE | READ ONLY | READ WRITE))* FOR FILE fileName (COMMA_ fileName)*
    ;

fileOwnerClause
    : SET OWNERSHIP (setOwnerClause (COMMA_ setOwnerClause)*) FOR FILE fileName (COMMA_ fileName)*
    ;

setOwnerClause
    : OWNER EQ_ username | GROUP EQ_ usergroupName
    ;

scrubClause
    : SCRUB (FILE asmFileName | DISK diskName)? (REPAIR | NOREPAIR)?
    (POWER (AUTO | LOW | HIGH | MAX))? (WAIT | NOWAIT)? (FORCE | NOFORCE)? (STOP)?
    ;

quotagroupClauses
    : (ADD QUOTAGROUP quotagroupName (setPropertyClause)?
    | MODIFY QUOTAGROUP quotagroupName setPropertyClause
    | MOVE FILEGROUP filegroupName TO quotagroupName
    | DROP QUOTAGROUP quotagroupName)
    ;

setPropertyClause
    : SET propertyName EQ_ propertyValue
    ;

quotagroupName
    : identifier
    ;

propertyName
    : QUOTA
    ;

propertyValue
    : sizeClause | UNLIMITED
    ;

filegroupName
    : identifier
    ;

filegroupClauses
    : (addFilegroupClause
    | modifyFilegroupClause
    | moveToFilegroupClause
    | dropFilegroupClause)
    ;

addFilegroupClause
    : ADD FILEGROUP filegroupName (DATABASE databaseName
    | CLUSTER clusterName
    | VOLUME asmVolumeName) (setFileTypePropertyclause)?
    ;

setFileTypePropertyclause
    :SET SQ_ (fileType DOT_)? propertyName SQ_ EQ_ SQ_ propertyValue SQ_
    ;

modifyFilegroupClause
    : MODIFY FILEGROUP filegroupName setFileTypePropertyclause
    ;

moveToFilegroupClause
    : MOVE FILE asmFileName TO FILEGROUP filegroupName
    ;

dropFilegroupClause
    : DROP FILEGROUP filegroupName (CASCADE)?
    ;

undropDiskClause
    : UNDROP DISKS
    ;

diskgroupAvailability
    : ((MOUNT (RESTRICTED | NORMAL)? (FORCE | NOFORCE)?) | (DISMOUNT (FORCE | NOFORCE)?))
    ;

enableDisableVolume
    : (ENABLE | DISABLE) VOLUME (asmVolumeName (COMMA_ asmVolumeName)* | ALL)
    ;

alterIndexType
    : ALTER INDEXTYPE indexTypeName ((addOrDropClause (COMMA_ addOrDropClause)* usingTypeClause?) | COMPILE) withLocalClause
    ;

addOrDropClause
    : (ADD | DROP) operatorName LP_ parameterType RP_
    ;

usingTypeClause
    : USING implementationType arrayDMLClause?
    ;

withLocalClause
    : (WITH LOCAL RANGE? PARTITION)? storageTableClause?
    ;

arrayDMLClause
    : (WITH | WITHOUT)? ARRAY DML arryDMLSubClause (COMMA_ arryDMLSubClause)*
    ;

arryDMLSubClause
    : LP_ typeName (COMMA_ varrayType)? RP_
    ;

createMaterializedView
    : CREATE MATERIALIZED VIEW materializedViewName (OF typeName )? materializedViewColumnClause? materializedViewPrebuiltClause materializedViewUsingClause? createMvRefresh? (FOR UPDATE)? ( (DISABLE | ENABLE) QUERY REWRITE )? AS selectSubquery
    ;

materializedViewColumnClause
    : ( LP_ (scopedTableRefConstraint | mvColumnAlias) (COMMA_ (scopedTableRefConstraint | mvColumnAlias))* RP_ )
    ;

materializedViewPrebuiltClause
    : ( ON PREBUILT TABLE ( (WITH | WITHOUT) REDUCED PRECISION)? | physicalProperties?  (CACHE | NOCACHE)? parallelClause? buildClause?)
    ;

materializedViewUsingClause
    : ( USING INDEX ( (physicalAttributesClause | TABLESPACE tablespaceName)+ )* | USING NO INDEX)
    ;

mvColumnAlias
    : (identifier | quotedString) (ENCRYPT encryptionSpecification)?
    ;

createMvRefresh
    : ( NEVER REFRESH | REFRESH createMvRefreshOptions+)
    ;

createMvRefreshOptions
    : ( (FAST | COMPLETE | FORCE) | ON (DEMAND | COMMIT | STATEMENT) | ((START WITH | NEXT) dateValue)+ | WITH (PRIMARY KEY | ROWID) | USING ( DEFAULT (MASTER | LOCAL)? ROLLBACK SEGMENT | (MASTER | LOCAL)? ROLLBACK SEGMENT rb_segment=REGULAR_ID ) | USING (ENFORCED | TRUSTED) CONSTRAINTS)
    ;

quotedString
    : variableName
    | CHAR_STRING
    | NATIONAL_CHAR_STRING_LIT
    ;

buildClause
    : BUILD (IMMEDIATE | DEFERRED)
    ;

createMaterializedViewLog
    : CREATE MATERIALIZED VIEW LOG ON tableName materializedViewLogAttribute? parallelClause? ( WITH ( ( COMMA_? ( OBJECT ID | PRIMARY KEY | ROWID | SEQUENCE | COMMIT SCN ) ) | (LP_ columnName ( COMMA_ columnName )* RP_ ) )* )? newViewValuesClause? mvLogPurgeClause? createMvRefresh? ((FOR UPDATE)? ( (DISABLE | ENABLE) QUERY REWRITE )? AS selectSubquery)?
    ;

materializedViewLogAttribute
    : ( ( physicalAttributesClause | TABLESPACE tablespaceName | loggingClause | (CACHE | NOCACHE))+ )
    ;

newViewValuesClause
    : (INCLUDING | EXCLUDING ) NEW VALUES
    ;

alterMaterializedView
    : ALTER MATERIALIZED VIEW materializedViewName materializedViewAttribute? alterIotClauses? (USING INDEX physicalAttributesClause)?
    ((MODIFY scopedTableRefConstraint) | alterMvRefresh)? evaluationEditionClause?
    ((ENABLE | DISABLE) ON QUERY COMPUTATION)? (alterQueryRewriteClause | COMPILE | CONSIDER FRESH)?
    ;

materializedViewAttribute
    : physicalAttributesClause
    | modifyMvColumnClause
    | tableCompression
    | inmemoryTableClause
    | lobStorageClause (COMMA_ lobStorageClause)*
    | modifylobStorageClause (COMMA_ modifylobStorageClause)*
    | alterTablePartitioning
    | parallelClause
    | loggingClause
    | allocateExtentClause
    | deallocateUnusedClause
    | shrinkClause
    | CACHE
    | NOCACHE
    ;

modifyMvColumnClause
    : MODIFY LP_ columnName ((ENCRYPT encryptionSpecification) | DECRYPT)? RP_
    ;

modifylobStorageClause
    : MODIFY LOB LP_ lobItem RP_ LP_ modifylobParameters+ RP_
    ;

modifylobParameters
    : storageClause
    | PCTVERSION INTEGER_
    | FREEPOOLS INTEGER_
    | REBUILD FREEPOOLS
    | lobRetentionClause
    | lobDeduplicateClause
    | lobCompressionClause
    | ENCRYPT encryptionSpecification
    | DECRYPT
    | CACHE
    | (NOCACHE | (CACHE READS)) loggingClause?
    | allocateExtentClause
    | shrinkClause
    | deallocateUnusedClause
    ;

alterIotClauses
    : indexOrgTableClause
    | alterOverflowClause
    | COALESCE
    ;

alterXMLSchemaClause
    : ALLOW (ANYSCHEMA | NONSCHEMA) | DISALLOW NONSCHEMA
    ;

alterOverflowClause
    : addOverflowClause | overflowClause
    ;

overflowClause
    : OVERFLOW (segmentAttributesClause | allocateExtentClause | shrinkClause | deallocateUnusedClause)+
    ;

addOverflowClause
    : ADD OVERFLOW segmentAttributesClause? (LP_ PARTITION segmentAttributesClause? (COMMA_ PARTITION segmentAttributesClause?)* RP_)?
    ;

scopedTableRefConstraint
    : SCOPE FOR LP_ (columnName | attributeName) RP_ IS (schemaName DOT_)? (tableName | alias)
    ;

alterMvRefresh
    : REFRESH ( ((FAST | COMPLETE | FORCE) (START WITH dateValue)? (NEXT dateValue)?)
    | ON DEMAND
    | ON COMMIT
    | START WITH dateValue
    | NEXT dateValue
    | WITH PRIMARY KEY
    | USING DEFAULT MASTER ROLLBACK SEGMENT
    | USING MASTER ROLLBACK SEGMENT rollbackSegment
    | USING ENFORCED CONSTRAINTS
    | USING TRUSTED CONSTRAINTS)
    ;

evaluationEditionClause
    : EVALUATE USING (CURRENT EDITION | EDITION editionName | NULL EDITION)
    ;

alterQueryRewriteClause
    : (ENABLE | DISABLE)? QUERY REWRITE unusableEditionsClause
    ;

unusableEditionsClause
    : unusableBefore? unusableBeginning?
    ;

unusableBefore
    : UNUSABLE BEFORE (CURRENT EDITION | EDITION editionName)
    ;

unusableBeginning
    : UNUSABLE BEGINNING WITH (CURRENT EDITION | EDITION editionName | NULL EDITION)
    ;

alterMaterializedViewLog
    : ALTER MATERIALIZED VIEW LOG FORCE? ON tableName
    ( physicalAttributesClause
    | addMvLogColumnClause
    | alterTablePartitioning
    | parallelClause
    | loggingClause
    | allocateExtentClause
    | shrinkClause
    | moveMvLogClause
    | CACHE
    | NOCACHE)? mvLogAugmentation? mvLogPurgeClause? forRefreshClause?
    ;

addMvLogColumnClause
    : ADD LP_ columnName RP_
    ;

moveMvLogClause
    : MOVE segmentAttributesClause parallelClause?
    ;

mvLogAugmentation
    : ADD addClause (COMMA_ addClause)* newValuesClause?
    ;

addClause
    : OBJECT ID columns?
    | PRIMARY KEY columns?
    | ROWID columns?
    | SEQUENCE columns?
    | columns
    ;

columns
    : LP_ columnName (COMMA_ columnName)* RP_
    ;

newValuesClause
    : (INCLUDING | EXCLUDING) NEW VALUES
    ;

mvLogPurgeClause
    : PURGE (IMMEDIATE (SYNCHRONOUS | ASYNCHRONOUS)?
    | START WITH dateValue nextOrRepeatClause?
    | (START WITH dateValue)? nextOrRepeatClause)
    ;

nextOrRepeatClause
    : NEXT dateValue | REPEAT intervalLiterals
    ;


forRefreshClause
    : FOR ((SYNCHRONOUS REFRESH USING stagingLogName) | (FAST REFRESH))
    ;

alterFunction
    : ALTER FUNCTION function (functionCompileClause | (EDITIONABLE | NONEDITIONABLE))
    ;

functionCompileClause
    : COMPILE DEBUG? compilerParametersClause* (REUSE SETTINGS)?
    ;

alterHierarchy
    : ALTER HIERARCHY hierarchyName (RENAME TO hierarchyName | COMPILE)
    ;

alterLockdownProfile
    : ALTER LOCKDOWN PROFILE profileName (lockdownFeatures | lockdownOptions | lockdownStatements)
    ;

lockdownFeatures
    : (DISABLE | ENABLE) FEATURE featureClauses
    ;

featureClauses
    : EQ_ LP_ featureName (COMMA_ featureName)* RP_
    | ALL (EXCEPT (EQ_ LP_ featureName (COMMA_ featureName)* RP_))?
    ;

lockdownOptions
    : (DISABLE | ENABLE) OPTION lockDownOptionClauses
    ;

lockDownOptionClauses
    : EQ_ LP_ optionName (COMMA_ optionName)* RP_
    | ALL (EXCEPT (EQ_ LP_ optionName (COMMA_ optionName)* RP_))?
    ;

lockdownStatements
    : (DISABLE | ENABLE) STATEMENT lockdownStatementsClauses
    ;

lockdownStatementsClauses
    : EQ_ LP_ sqlStatement (COMMA_ sqlStatement )* RP_
    | EQ_ LP_ sqlStatement RP_ statementClauses
    | ALL (EXCEPT (EQ_ LP_ sqlStatement (COMMA_ sqlStatement)* RP_))?
    ;

statementClauses
    : CLAUSE statementsSubClauses
    ;

statementsSubClauses
    : EQ_ LP_ clause (COMMA_ clause)* RP_
    | EQ_ LP_ clause RP_ clauseOptions
    | ALL (EXCEPT (EQ_ LP_ clause (COMMA_ clause)* RP_))?
    ;

clauseOptions
    : OPTION optionClauses
    ;

optionClauses
    : EQ_ LP_ clauseOptionOrPattern (COMMA_ clauseOptionOrPattern)* RP_
    | EQ_ LP_ clauseOption RP_ optionValues+
    | ALL (EXCEPT EQ_ LP_ clauseOptionOrPattern (COMMA_ clauseOptionOrPattern)* RP_)?
    ;

clauseOptionOrPattern
    : clauseOption | clauseOptionPattern
    ;

optionValues
    : VALUE EQ_ LP_ optionValue (COMMA_ optionValue)* RP_
    | MINVALUE EQ_ optionValue
    | MAXVALUE EQ_ optionValue
    ;

alterPluggableDatabase
    : ALTER databaseClause (pdbUnplugClause
    | pdbSettingsClauses
    | pdbDatafileClause
    | pdbRecoveryClauses
    | pdbChangeState
    | pdbChangeStateFromRoot
    | applicationClauses
    | snapshotClauses
    | prepareClause
    | dropMirrorCopy
    | lostWriteProtection)
    ;

databaseClause
    : DATABASE dbName?
    | PLUGGABLE DATABASE pdbName?
    ;

pdbUnplugClause
    : pdbName UNPLUG INTO fileName pdbUnplugEncrypt?
    ;

pdbUnplugEncrypt
    : ENCRYPT USING transportSecret
    ;

pdbSettingsClauses
    : pdbName? pdbSettingClause
    | CONTAINERS (DEFAULT TARGET EQ_ ((LP_ containerName RP_) | NONE) | HOST EQ_ hostName | PORT EQ_ NUMBER_)
    ;

pdbSettingClause
    : DEFAULT EDITION EQ_ editionName
    | SET DEFAULT (BIGFILE | SMALLFILE) TABLESPACE
    | DEFAULT TABLESPACE tablespaceName
    | DEFAULT TEMPORARY TABLESPACE (tablespaceName | tablespaceGroupName)
    | RENAME GLOBAL_NAME TO databaseName (DOT_ domain)+
    | setTimeZoneClause
    | databaseFileClauses
    | supplementalDbLogging
    | pdbStorageClause
    | pdbLoggingClauses
    | pdbRefreshModeClause
    | REFRESH pdbRefreshSwitchoverClause?
    | SET CONTAINER_MAP EQ_ mapObject
    ;

pdbStorageClause
    : STORAGE ((LP_ storageMaxSizeClauses+ RP_) | UNLIMITED)
    ;

storageMaxSizeClauses
    : (MAXSIZE | MAX_AUDIT_SIZE | MAX_DIAG_SIZE) (UNLIMITED | sizeClause)
    ;

pdbLoggingClauses
    : loggingClause | pdbForceLoggingClause
    ;

pdbForceLoggingClause
    : (ENABLE | DISABLE) FORCE (LOGGING | NOLOGGING)
    | SET STANDBY NOLOGGING FOR ((DATA AVAILABILITY) | (LOAD PERFORMANCE))
    ;

pdbRefreshModeClause
    : REFRESH MODE (MANUAL | (EVERY refreshInterval (MINUTES | HOURS)) | NONE )
    ;

pdbRefreshSwitchoverClause
    : FROM sourcePdbName AT_ dbLink SWITCHOVER
    ;

pdbDatafileClause
    : pdbName? DATAFILE (fileNameAndNumber | ALL)  (ONLINE | OFFLINE)
    ;

fileNameAndNumber
    : (fileName | fileNumber) (COMMA_ (fileName | fileNumber))*
    ;

pdbRecoveryClauses
    : pdbName? (pdbGeneralRecovery
    | BEGIN BACKUP
    | END BACKUP
    | ENABLE RECOVERY
    | DISABLE RECOVERY)
    ;

pdbGeneralRecovery
    : RECOVER AUTOMATIC? (FROM locationName)? (DATABASE
    | TABLESPACE tablespaceName (COMMA_ tablespaceName)*
    | DATAFILE fileNameAndNumber
    | LOGFILE fileName
    | CONTINUE DEFAULT?)?
    ;

pdbChangeState
    : pdbName? (pdbOpen | pdbClose | pdbSaveOrDiscardState)
    ;

pdbOpen
    : OPEN (((READ WRITE) | (READ ONLY))? RESTRICTED? FORCE?
    | (READ WRITE)? UPGRADE RESTRICTED?
    | RESETLOGS) instancesClause?
    ;

instancesClause
    : INSTANCES EQ_ (instanceNameClause | (ALL (EXCEPT instanceName)?))
    ;

instanceNameClause
    : LP_ instanceName (COMMA_ instanceName )* RP_
    ;

pdbClose
    : CLOSE ((IMMEDIATE? (instancesClause | relocateClause)?) | (ABORT? instancesClause?))
    ;

relocateClause
    : RELOCATE (TO instanceName)?
    | NORELOCATE
    ;

pdbSaveOrDiscardState
    : (SAVE | DISCARD) STATE instancesClause?
    ;

pdbChangeStateFromRoot
    : (pdbNameClause | (ALL (EXCEPT pdbNameClause)?)) (pdbOpen | pdbClose | pdbSaveOrDiscardState)
    ;

pdbNameClause
    : pdbName (COMMA_ pdbName)*
    ;

applicationClauses
    : APPLICATION ((appName appClause) | (ALL SYNC))
    ;

appClause
    : BEGIN INSTALL SQ_ appVersion SQ_ (COMMENT SQ_ commentValue SQ_)?
    | END INSTALL (SQ_ appVersion SQ_)?
    | BEGIN PATCH NUMBER_ (MINIMUM VERSION SQ_ appVersion SQ_)? (COMMENT SQ_ commentValue SQ_)?
    | END PATCH NUMBER_?
    | BEGIN UPGRADE (SQ_ startAppVersion SQ_)? TO SQ_ endAppVersion SQ_ (COMMENT SQ_ commentValue SQ_)?
    | END UPGRADE (TO SQ_ endAppVersion SQ_)?
    | BEGIN UNINSTALL
    | END UNINSTALL
    | SET PATCH NUMBER_
    | SET VERSION SQ_ appVersion SQ_
    | SET COMPATIBILITY VERSION ((SQ_ appVersion SQ_) | CURRENT)
    | SYNC TO ((SQ_ appVersion SQ_) | (PATCH patchNumber))
    | SYNC
    ;

snapshotClauses
    : pdbSnapshotClause
    | materializeClause
    | createSnapshotClause
    | dropSnapshotClause
    | setMaxPdbSnapshotsClause
    ;

pdbSnapshotClause
    : SNAPSHOT (MANUAL | (EVERY snapshotInterval (HOURS | MINUTES)) | NONE)
    ;

materializeClause
    : MATERIALIZE
    ;

createSnapshotClause
    : SNAPSHOT snapshotName
    ;

dropSnapshotClause
    : DROP SNAPSHOT snapshotName
    ;

setMaxPdbSnapshotsClause
    : SET maxPdbSnapshots EQ_ maxNumberOfSnapshots
    ;

dropIndexType
    : DROP INDEXTYPE indexTypeName FORCE?
    ;

dropProfile
    : DROP PROFILE profileName CASCADE?
    ;

dropPluggableDatabase
    : DROP PLUGGABLE DATABASE pdbName ((KEEP | INCLUDING) DATAFILES)?
    ;

dropSequence
    : DROP SEQUENCE (schemaName DOT_)? sequenceName
    ;

dropJava
     : DROP JAVA (SOURCE | CLASS | RESOURCE) objectName
     ;

dropLibrary
    : DROP LIBRARY libraryName
    ;

dropMaterializedView
    : DROP MATERIALIZED VIEW materializedViewName (PRESERVE TABLE)?
    ;

dropMaterializedViewLog
    : DROP MATERIALIZED VIEW LOG ON tableName
    ;

dropMaterializedZonemap
    : DROP MATERIALIZED ZONEMAP zonemapName
    ;

tablespaceEncryptionSpec
    : USING encryptAlgorithmName
    ;

tableCompressionTableSpace
    : COMPRESS
    | COMPRESS BASIC
    | COMPRESS (FOR (OLTP | ((QUERY | ARCHIVE) (LOW | HIGH)?)))
    | NOCOMPRESS
    ;

segmentManagementClause
    : SEGMENT SPACE MANAGEMENT (AUTO|MANUAL)
    ;

tablespaceGroupClause
    : TABLESPACE GROUP (tablespaceGroupName | SQ_ SQ_)
    ;

temporaryTablespaceClause
    : TEMPORARY TABLESPACE tablespaceName (TEMPFILE fileSpecification (COMMA_ fileSpecification)* )? tablespaceGroupClause? extentManagementClause?
    ;

tablespaceRetentionClause
    : RETENTION (GUARANTEE | NOGUARANTEE)
    ;

undoTablespaceClause
    : UNDO TABLESPACE tablespaceName (DATAFILE fileSpecification (COMMA_ fileSpecification)*)? extentManagementClause? tablespaceRetentionClause?
    ;

createTablespace
    : CREATE (BIGFILE|SMALLFILE)? (DATAFILE fileSpecifications)? (permanentTablespaceClause | temporaryTablespaceClause | undoTablespaceClause)
    ;

permanentTablespaceClause
    : TABLESPACE tablespaceName (
    (MINIMUM EXTEND sizeClause)
    | (BLOCKSIZE INTEGER_ capacityUnit?)
    | loggingClause
    | (FORCE LOGGING)
    | ENCRYPTION tablespaceEncryptionSpec
    | DEFAULT tableCompressionTableSpace? storageClause?
    | (ONLINE|OFFLINE)
    | extentManagementClause
    | segmentManagementClause
    | flashbackModeClause
    )
    ;

alterTablespace
    : ALTER TABLESPACE tablespaceName
    ( defaultTablespaceParams
    | MINIMUM EXTENT sizeClause
    | RESIZE sizeClause
    | COALESCE
    | SHRINK SPACE (KEEP sizeClause)?
    | RENAME TO newTablespaceName
    | (BEGIN | END) BACKUP
    | datafileTempfileClauses
    | tablespaceLoggingClauses
    | tablespaceGroupClause
    | tablespaceStateClauses
    | autoextendClause
    | flashbackModeClause
    | tablespaceRetentionClause
    | alterTablespaceEncryption
    | lostWriteProtection
    )
    ;

defaultTablespaceParams
    : DEFAULT defaultTableCompression? defaultIndexCompression? inmemoryClause? ilmClause? storageClause?
    ;

defaultTableCompression
    : TABLE (COMPRESS FOR OLTP | COMPRESS FOR QUERY (LOW | HIGH) | COMPRESS FOR ARCHIVE (LOW | HIGH) | NOCOMPRESS)
    ;

defaultIndexCompression
    : INDEX (COMPRESS ADVANCED (LOW | HIGH) | NOCOMPRESS)
    ;

datafileTempfileClauses
    : ADD (DATAFILE | TEMPFILE) (fileSpecification (COMMA_ fileSpecification)*)?
    | DROP (DATAFILE | TEMPFILE) (fileName | fileNumber)
    | SHRINK TEMPFILE (fileName | fileNumber) (KEEP sizeClause)?
    | RENAME DATAFILE fileName (COMMA_ fileName)* TO fileName (COMMA_ fileName)*
    | (DATAFILE | TEMPFILE) (ONLINE | OFFLINE)
    ;

tablespaceLoggingClauses
    : loggingClause | NO? FORCE LOGGING
    ;

tablespaceStateClauses
    : ONLINE | OFFLINE (NORMAL | TEMPORARY | IMMEDIATE)? | READ (ONLY | WRITE) | (PERMANENT | TEMPORARY)
    ;

tablespaceFileNameConvert
    : FILE_NAME_CONVERT EQ_ LP_ filenamePattern COMMA_ replacementFilenamePattern (COMMA_ filenamePattern COMMA_ replacementFilenamePattern)* RP_ KEEP?
    ;

alterTablespaceEncryption
    : ENCRYPTION(OFFLINE (tablespaceEncryptionSpec? ENCRYPT | DECRYPT)
    | ONLINE (tablespaceEncryptionSpec? (ENCRYPT | REKEY) | DECRYPT) tablespaceFileNameConvert?
    | FINISH (ENCRYPT | REKEY | DECRYPT) tablespaceFileNameConvert?)
    ;

dropFunction
    : DROP FUNCTION (schemaName DOT_)? function
    ;

compileTypeClause
    : COMPILE DEBUG? (SPECIFICATION|BODY)? compilerParametersClause? REUSE SETTINGS
    ;

inheritanceClauses
    : (NOT? (OVERRIDING | FINAL | INSTANTIABLE))+
    ;

procedureSpec
    : PROCEDURE procedureName LP_ (parameterValue typeName (COMMA_ parameterValue typeName)*) RP_ ((IS | AS) callSpec)?
    ;

returnClause
    : RETURN dataType ((IS | AS) callSpec)?
    ;

functionSpec
    : FUNCTION name LP_ (parameterValue dataType (COMMA_ parameterValue dataType)*) RP_ returnClause
    ;

subprogramSpec
    : (MEMBER | STATIC) (procedureSpec | functionSpec)
    ;

constructorSpec
    : FINAL? INSTANTIABLE? CONSTRUCTOR FUNCTION typeName
    (LP_ (SELF IN OUT dataType COMMA_)? parameterValue dataType (COMMA_ parameterValue dataType)* RP_)?
    RETURN SELF AS RESULT ((AS | IS) callSpec)?
    ;

mapOrderFunctionSpec
    : (MAP | ORDER) MEMBER functionSpec
    ;

restrictReferencesPragma
    : PRAGMA RESTRICT_REFERENCES
    LP_ (subprogramName | methodName | DEFAULT) COMMA_
    (RNDS | WNDS | RNPS | WNPS | TRUST)
    (COMMA_ (RNDS | WNDS | RNPS | WNPS | TRUST))* RP_
    ;

elementSpecification
    : inheritanceClauses? (subprogramSpec | constructorSpec | mapOrderFunctionSpec)+ (COMMA_ restrictReferencesPragma)?
    ;

replaceTypeClause
    : REPLACE invokerRightsClause? AS OBJECT LP_ (attributeName dataType (COMMA_ (elementSpecification | attributeName dataType))*) RP_
    ;

alterMethodSpec
    : (ADD | DROP) (mapOrderFunctionSpec | subprogramSpec) ((ADD | DROP) (mapOrderFunctionSpec | subprogramSpec))*
    ;

alterAttributeDefinition
    : (ADD | MODIFY) ATTRIBUTE ( attributeName dataType? | LP_ attributeName dataType (COMMA_ attributeName dataType)* RP_)
      | DROP ATTRIBUTE ( attributeName | LP_ attributeName (COMMA_ attributeName)* RP_)
    ;

alterCollectionClauses
    : MODIFY (LIMIT INTEGER_ | ELEMENT TYPE dataType)
    ;

dependentHandlingClause
    : INVALIDATE | CASCADE (NOT? INCLUDING TABLE DATA | CONVERT TO SUBSTITUTABLE)? (FORCE? exceptionsClause)?
    ;

alterType
    : ALTER TYPE typeName (compileTypeClause | replaceTypeClause | RESET
    | (alterMethodSpec | alterAttributeDefinition | alterCollectionClauses | NOT? (INSTANTIABLE | FINAL)) dependentHandlingClause?)
    ;

createCluster
    : CREATE CLUSTER (schemaName DOT_)? clusterName LP_ (columnName dataType (COLLATE columnCollationName)? SORT? (COMMA_ columnName dataType (COLLATE columnCollationName)? SORT?)*) RP_
    ( physicalAttributesClause | SIZE sizeClause | TABLESPACE tablespaceName | INDEX | (SINGLE TABLE)? HASHKEYS INTEGER_ (HASH IS expr)?)* parallelClause?
    ( NOROWDEPENDENCIES | ROWDEPENDENCIES)? (CACHE | NOCACHE)? clusterRangePartitions?
    ;

clusterRangePartitions
    : PARTITION BY RANGE columns LP_ (PARTITION partitionName? rangeValuesClause tablePartitionDescription) (COMMA_ (PARTITION partitionName? rangeValuesClause tablePartitionDescription))* RP_
    ;

createJava
    : CREATE (OR REPLACE)? (AND (RESOLVE | COMPILE))? NOFORCE? JAVA
    ((SOURCE | RESOURCE) NAMED (schemaName DOT_)? primaryName
    | CLASS (SCHEMA schemaName)?) invokerRightsClause? resolveClauses?
    (usingClause | AS sourceText)
    ;

usingClause
    : USING (fileType (LP_ directoryName COMMA_ serverFileName RP_ | subquery) | BQ_ keyForBlob BQ_)
    ;

fileType
    : BFILE
    | CLOB
    | BLOB
    ;

createLibrary
    : CREATE (OR REPLACE)? (EDITIONABLE | NONEDITIONABLE)? LIBRARY plsqlLibrarySource
    ;

plsqlLibrarySource
    : libraryName sharingClause? (IS | AS) (fullPathName | (fileName IN directoryObject)) agentClause
    ;

agentClause
    : (AGENT agentDblink)? (CREDENTIAL credentialName)?
    ;

switch
    : SWITCH switchClause TO COPY
    ;

switchClause
    : DATABASE
    | DATAFILE datafileSpecClause (COMMA_ datafileSpecClause)*
    | TABLESPACE SQ_? tablespaceName SQ_? (COMMA_ SQ_? tablespaceName SQ_?)*
    ;

datafileSpecClause
    : SQ_ fileName SQ_ | INTEGER_
    ;

createProfile
    : CREATE MANDATORY? PROFILE profileName LIMIT (resourceParameters | passwordParameters)+ (CONTAINER EQ_ (CURRENT | ALL))?
    ;

noAudit
    : noAuditTraditional | noAuditUnified
    ;

noAuditTraditional
    : NOAUDIT (auditOperationClause auditingByClause? |  auditSchemaObjectClause | NETWORK | DIRECT_PATH LOAD auditingByClause?)
    (WHENEVER NOT? SUCCESSFUL)? (CONTAINER EQ_ (CURRENT | ALL))?
    ;

dropDatabase
    : DROP DATABASE (INCLUDING BACKUPS)? NOPROMPT?
    ;

createOutline
    : CREATE (OR REPLACE)? (PUBLIC | PRIVATE)? OUTLINE outlineName?
    (FROM (PUBLIC | PRIVATE)? outlineName)? (FOR CATEGORY categoryName)? (ON (select | delete | update | insert | createTable))?
    ;
