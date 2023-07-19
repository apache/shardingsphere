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

import BaseRule, DCLStatement;

createTable
    : CREATE createTableSpecification TABLE tableName createSharingClause createDefinitionClause createMemOptimizeClause createParentClause
    ;

createEdition
    : CREATE EDITION editionName (AS CHILD OF editionName)?
    ;

createIndex
    : CREATE createIndexSpecification INDEX indexName ON createIndexDefinitionClause usableSpecification? invalidationSpecification?
    ;

alterTable
    : ALTER TABLE tableName memOptimizeClause alterDefinitionClause enableDisableClauses
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
    : COMPILE DEBUG? (compilerParametersClause*)? (REUSE SETTINGS)?
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
    : DROP INDEX indexName ONLINE? FORCE? ((DEFERRED|IMMEDIATE) INVALIDATION)?
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
    : ((GLOBAL | PRIVATE) TEMPORARY | SHARDED | DUPLICATED)?
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

oidClause
    : OBJECT IDENTIFIER IS (SYSTEM GENERATED | PRIMARY KEY)
    ;

oidIndexClause
    : OIDINDEX indexName? LP_ (physicalAttributesClause | TABLESPACE tablespaceName)* RP_
    ;

createRelationalTableClause
    : (LP_ relationalProperties RP_)? collationClause? commitClause? physicalProperties? tableProperties?
    ;
    
createMemOptimizeClause
    : (MEMOPTIMIZE FOR READ)? (MEMOPTIMIZE FOR WRITE)? 
    ;    

createParentClause
    : (PARENT tableName)?
    ;

createObjectTableClause
    : OF objectName objectTableSubstitution? 
    (LP_ objectProperties RP_)? (ON COMMIT (DELETE | PRESERVE) ROWS)?
    oidClause? oidIndexClause? physicalProperties? tableProperties?
    ;

relationalProperties
    : relationalProperty (COMMA_ relationalProperty)*
    ;

relationalProperty
    : columnDefinition | virtualColumnDefinition | outOfLineConstraint | outOfLineRefConstraint
    ;

columnDefinition
    : columnName dataType SORT? visibleClause (defaultNullClause expr | identityClause)? (ENCRYPT encryptionSpecification)? (inlineConstraint+ | inlineRefConstraint)?
    ;

visibleClause
    : (VISIBLE | INVISIBLE)?
    ;

defaultNullClause
    : DEFAULT (ON NULL)?
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
    : (USING STRING_)? (IDENTIFIED BY STRING_)? STRING_? (NO? SALT)?
    ;

inlineConstraint
    : (CONSTRAINT ignoredIdentifier)? (NOT? NULL | UNIQUE | primaryKey | referencesClause | CHECK LP_ expr RP_) constraintState?
    ;

referencesClause
    : REFERENCES tableName columnNames? (ON DELETE (CASCADE | SET NULL))?
    ;

constraintState
    : notDeferrable 
    | initiallyClause 
    | RELY | NORELY 
    | usingIndexClause 
    | ENABLE | DISABLE 
    | VALIDATE | NOVALIDATE 
    | exceptionsClause
    ;

notDeferrable
    : NOT? DEFERRABLE
    ;

initiallyClause
    : INITIALLY (IMMEDIATE | DEFERRED)
    ;

exceptionsClause
    : EXCEPTIONS INTO tableName
    ;

usingIndexClause
    : USING INDEX (indexName | createIndexClause)?
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
    : (ONLINE | (SORT|NOSORT) | REVERSE | (VISIBLE | INVISIBLE))
    ;

tableIndexClause
    : tableName alias? indexExpressions
    ;

indexExpressions
    : LP_? indexExpression (COMMA_ indexExpression)* RP_?
    ;

indexExpression
    : (columnName | expr) (ASC | DESC)?
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
    | columnClauses
    | constraintClauses
    | alterTablePartitioning ((DEFERRED| IMMEDIATE) INVALIDATION)?
    | alterExternalTable)?
    ;

alterTableProperties
    : renameTableSpecification | REKEY encryptionSpecification
    ;

renameTableSpecification
    : RENAME TO identifier
    ;

dropSynonym
    : DROP PUBLIC? SYNONYM (schemaName DOT_)? synonymName FORCE?
    ;

columnClauses
    : operateColumnClause+ | renameColumnClause
    ;

operateColumnClause
    : addColumnSpecification | modifyColumnSpecification | dropColumnClause
    ;

addColumnSpecification
    : ADD LP_ columnOrVirtualDefinitions RP_ columnProperties?
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

constraintClauses
    : addConstraintSpecification | modifyConstraintClause | renameConstraintClause | dropConstraintClause+
    ;

addConstraintSpecification
    : ADD (outOfLineConstraint+ | outOfLineRefConstraint)
    ;

modifyConstraintClause
    : MODIFY constraintOption constraintState+ CASCADE?
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
    : ((columnName | attributeName) (DEFAULT expr)? (inlineConstraint* | inlineRefConstraint)?)
    | outOfLineConstraint
    | outOfLineRefConstraint
    | supplementalLoggingProps
    ;

alterIndexInformationClause
    : rebuildClause ((DEFERRED|IMMEDIATE) | INVALIDATION)?
    | parallelClause
    | COMPILE
    | (ENABLE | DISABLE)
    | UNUSABLE ONLINE? ((DEFERRED|IMMEDIATE)|INVALIDATION)?
    | (VISIBLE | INVISIBLE)
    | renameIndexClause
    | COALESCE CLEANUP? ONLY? parallelClause?
    | ((MONITORING | NOMONITORING) USAGE)
    | UPDATE BLOCK REFERENCES
    ;

renameIndexClause
    : (RENAME TO indexName)?
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
    : (enableDisableClause | enableDisableOthers)?
    ;

enableDisableClause
    : (ENABLE | DISABLE) (VALIDATE |NO VALIDATE)? ((UNIQUE columnName (COMMA_ columnName)*) | PRIMARY KEY | constraintWithName) usingIndexClause? exceptionsClause? CASCADE? ((KEEP | DROP) INDEX)?
    ;

enableDisableOthers
    : (ENABLE | DISABLE) (TABLE LOCK | ALL TRIGGERS | CONTAINER_MAP | CONTAINERS_DEFAULT)
    ;

rebuildClause
    : REBUILD parallelClause?
    ;

parallelClause
    : NOPARALLEL | PARALLEL NUMBER_?
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
    : (ON COMMIT (DROP | PRESERVE) ROWS)? (ON COMMIT (DELETE | PRESERVE) ROWS)?
    ;

physicalProperties
    : deferredSegmentCreation? segmentAttributesClause? tableCompression? inmemoryTableClause? ilmClause?
    | deferredSegmentCreation? (organizationClause?|externalPartitionClause?)
    | clusterClause
    ;

deferredSegmentCreation
    : SEGMENT CREATION (IMMEDIATE|DEFERRED)
    ;

segmentAttributesClause
    : ( physicalAttributesClause
    | (TABLESPACE tablespaceName | TABLESPACE SET tablespaceSetName)
    | loggingClause)+
    ;

physicalAttributesClause
    : (PCTFREE INTEGER_ | PCTUSED INTEGER_ | INITRANS INTEGER_ | storageClause)+
    ;

loggingClause
    : LOGGING | NOLOGGING |  FILESYSTEM_LIKE_LOGGING
    ;

storageClause
    : STORAGE LP_
    (INITIAL sizeClause
    | NEXT sizeClause
    | MINEXTENTS NUMBER_
    | MAXEXTENTS (NUMBER_ | UNLIMITED)
    | maxsizeClause
    | PCTINCREASE NUMBER_
    | FREELISTS NUMBER_
    | FREELIST GROUPS NUMBER_
    | OPTIMAL (sizeClause | NULL)?
    | BUFFER_POOL (KEEP | RECYCLE | DEFAULT)
    | FLASH_CACHE (KEEP | NONE | DEFAULT)
    | CELL_FLASH_CACHE (KEEP | NONE | DEFAULT)
    | ENCRYPT
    )+ RP_
    ;

sizeClause
    : INTEGER_ capacityUnit?
    ;

maxsizeClause
    : MAXSIZE (UNLIMITED | sizeClause)
    ;

tableCompression
    : COMPRESS
    | ROW STORE COMPRESS (BASIC | ADVANCED)?
    | COLUMN STORE COMPRESS (FOR (QUERY | ARCHIVE) (LOW | HIGH)?)? (NO? ROW LEVEL LOCKING)?
    | NOCOMPRESS
    ;

inmemoryTableClause
    : ((INMEMORY inmemoryAttributes?) | NO INMEMORY)? (inmemoryColumnClause)?
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
    : (mappingTableClause | PCTTHRESHOLD NUMBER_ | prefixCompression)* indexOrgOverflowClause?
    ;

externalTableClause
    : LP_ (TYPE accessDriverType)? (externalTableDataProps)? RP_ (REJECT LIMIT (NUMBER_ | UNLIMITED))? inmemoryTableClause?
    ;

externalTableDataProps
    : (DEFAULT DIRECTORY directoryName)? (ACCESS PARAMETERS ((opaqueFormatSpec) | USING CLOB subquery))? (LOCATION LP_ (directoryName COLON_)? locationSpecifier (COMMA_ (directoryName COLON_)? locationSpecifier)+ RP_)?
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
    :columnProperties?
     readOnlyClause?
     indexingClause?
     tablePartitioningClauses?
     attributeClusteringClause?
     (CACHE | NOCACHE)?
     ( RESULT_CACHE ( MODE (DEFAULT | FORCE) ) )?
     parallelClause?
     (ROWDEPENDENCIES | NOROWDEPENDENCIES)?
     enableDisableClause*
     rowMovementClause?
     flashbackArchiveClause?
     (ROW ARCHIVAL)?
     (AS subquery | FOR EXCHANGE WITH TABLE tableName)?
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
    : VALUES LESS THAN LP_? (numberLiterals | MAXVALUE) (COMMA_ (numberLiterals | MAXVALUE))* RP_?
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
    : INMEMORY inmemoryAttributes? | NO INMEMORY
    ;

varrayColProperties
    : VARRAY varrayItem (substitutableColumnClause? varrayStorageClause | substitutableColumnClause)
    ;

nestedTableColProperties
    : NESTED TABLE 
    (nestedItem | COLUMN_VALUE) substitutableColumnClause? (LOCAL | GLOBAL)? STORE AS storageTable 
    LP_ (LP_ objectProperties RP_ | physicalProperties | columnProperties) RP_ 
    (RETURN AS? (LOCATOR | VALUE))?
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
        | CHUNK NUMBER_
        | PCTVERSION NUMBER_
        | FREEPOOLS NUMBER_
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
    : VALUES ( listValues | DEFAULT )
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
    : SUBPARTITION BY HASH columnNames (SUBPARTITIONS NUMBER_ (STORE IN LP_ tablespaceName (COMMA_ tablespaceName)? RP_)? | subpartitionTemplate)?
    ;

subpartitionTemplate
    : SUBPARTITION TEMPLATE
    (LP_? rangeSubpartitionDesc (COMMA_ rangeSubpartitionDesc)* | listSubpartitionDesc (COMMA_ listSubpartitionDesc)* | individualHashSubparts (COMMA_ individualHashSubparts)* RP_?)
    | hashSubpartitionQuantity
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

rangePartitionDesc
    : PARTITION partitionName? rangeValuesClause tablePartitionDescription
    ((LP_? rangeSubpartitionDesc (COMMA_ rangeSubpartitionDesc)* | listSubpartitionDesc (COMMA_ listSubpartitionDesc)* | individualHashSubparts (COMMA_ individualHashSubparts)* RP_?)
    | hashSubpartitionQuantity)?
    ;

compositeListPartitions
    : PARTITION BY LIST columnNames 
      (AUTOMATIC (STORE IN LP_? tablespaceName (COMMA_ tablespaceName)* RP_?)?)?
      (subpartitionByRange | subpartitionByList | subpartitionByHash) 
      LP_? listPartitionDesc (COMMA_ listPartitionDesc)* RP_?
    ;

listPartitionDesc
    : PARTITIONSET partitionSetName listValuesClause (TABLESPACE SET tablespaceSetName)? lobStorageClause? (SUBPARTITIONS STORE IN LP_? tablespaceSetName (COMMA_ tablespaceSetName)* RP_?)?
    ;

compositeHashPartitions
    : PARTITION BY HASH columnNames (subpartitionByRange | subpartitionByList | subpartitionByHash) (individualHashPartitions | hashPartitionsByQuantity)
    ;

referencePartitioning
    :PARTITION BY REFERENCE LP_ constraint RP_ (LP_? referencePartitionDesc (COMMA_ referencePartitionDesc)* RP_?)?
    ;

referencePartitionDesc
    : PARTITION partitionName? tablePartitionDescription?
    ;

constraint
    : inlineConstraint | outOfLineConstraint | inlineRefConstraint | outOfLineRefConstraint
    ;

systemPartitioning
    : PARTITION BY SYSTEM (PARTITIONS NUMBER_ | referencePartitionDesc (COMMA_ referencePartitionDesc)*)?
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

flashbackArchiveClause
    : FLASHBACK ARCHIVE flashbackArchiveName? | NO FLASHBACK ARCHIVE
    ;

alterPackage
    : ALTER PACKAGE packageName (
    | packageCompileClause
    | (EDITIONABLE | NONEDITIONABLE)
    )
    ;

packageCompileClause
    : COMPILE DEBUG? (PACKAGE | SPECIFICATION | BODY)? (compilerParametersClause*)? (REUSE SETTINGS)?
    ;

alterSynonym
    : ALTER PUBLIC? SYNONYM (schemaName DOT_)? synonymName (COMPILE | EDITIONABLE | NONEDITIONABLE)
    ;

alterTablePartitioning
    : modifyTablePartition
    | moveTablePartition
    | addTablePartition
    | coalesceTablePartition
    | dropTablePartition
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
    : COALESCE SUBPARTITION subpartitionName updateIndexClauses? parallelClause? allowDisallowClustering?
    ;

allowDisallowClustering
    : (ALLOW | DISALLOW) CLUSTERING
    ;

alterMappingTableClauses
    : MAPPING TABLE (allocateExtentClause | deallocateUnusedClause)
    ;

alterView
    : ALTER VIEW viewName (
    | ADD outOfLineConstraint
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
    : ADD ((PARTITION partitionName? addRangePartitionClause (COMMA_ PARTITION partitionName? addRangePartitionClause)*)
        |  (PARTITION partitionName? addListPartitionClause (COMMA_ PARTITION partitionName? addListPartitionClause)*)
        |  (PARTITION partitionName? addSystemPartitionClause (COMMA_ PARTITION partitionName? addSystemPartitionClause)*)
        (BEFORE (partitionName | NUMBER_))?
        |  (PARTITION partitionName? addHashPartitionClause)
        ) dependentTablesClause?
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
    : DISCONNECT SESSION SQ_ INTEGER_ COMMA_ INTEGER_ SQ_ POST_TRANSACTION?
    ;

killSessionClause
    : KILL SESSION SQ_ INTEGER_ COMMA_ INTEGER_ (COMMA_ AT_ INTEGER_)? SQ_
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
    : setParameterClause | useStoredOutlinesClause | globalTopicEnabledClause
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

alterSystemCommentClause
    : COMMENT EQ_ stringLiterals
    ;

containerCurrentAllClause
    : CONTAINER EQ_ (CURRENT | ALL)
    ;

scopeClause
    : SCOPE EQ_ (MEMORY | SPFILE | BOTH) | SID EQ_ (SQ_ sessionId SQ_ | SQ_ ASTERISK_ SQ_)
    ;

analyze
    : (ANALYZE ((TABLE tableName| INDEX indexName) partitionExtensionClause? | CLUSTER clusterName))
    (validationClauses | LIST CHAINED ROWS intoClause? | DELETE SYSTEM? STATISTICS)
    ;

partitionExtensionClause
    : PARTITION (LP_ partitionName RP_ | FOR LP_ partitionKeyValue (COMMA_ partitionKeyValue) RP_)
    | SUBPARTITION (LP_ subpartitionName RP_ | FOR LP_ subpartitionKeyValue (COMMA_ subpartitionKeyValue) RP_)
    ;

validationClauses
    : VALIDATE REF UPDATE (SET DANGLING TO NULL)?
    | VALIDATE STRUCTURE (CASCADE (FAST | COMPLETE (OFFLINE | ONLINE) intoClause?))?
    ;

intoClause
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
    : AUDIT (auditPolicyClause | contextClause)
    ;

noAudit
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

createFunction
    : CREATE (OR REPLACE)? (EDITIONABLE | NONEDITIONABLE)? FUNCTION plsqlFunctionSource
    ;

plsqlFunctionSource
    : function (LP_ parameterDeclaration (COMMA_ parameterDeclaration)* RP_)? RETURN dataType
    sharingClause? (invokerRightsClause
    | accessibleByClause 
    | defaultCollationoOptionClause
    | deterministicClause
    | parallelEnableClause
    | resultCacheClause
    | aggregateClause
    | pipelinedClause
    | sqlMacroClause)* 
    (IS | AS) callSpec
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
    : CREATE SEQUENCE (schemaName DOT_)? sequenceName (SHARING EQ_ (METADATA | DATA | NONE))? createSequenceClause+
    ;

createSequenceClause
    : (INCREMENT BY | START WITH) INTEGER_
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
    | KEEP
    | NOKEEP
    | SCALE (EXTEND | NOEXTEND)
    | NOSCALE
    | SHARD (EXTEND | NOEXTEND)
    | NOSHARD
    | SESSION
    | GLOBAL
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
    : CREATE CONTROLFILE REUSE? SET? DATABASE databaseName logfileForControlClause? resetLogsOrNot
    ( MAXLOGFILES INTEGER_
    | MAXLOGMEMBERS INTEGER_
    | MAXLOGHISTORY INTEGER_
    | MAXDATAFILES INTEGER_
    | MAXINSTANCES INTEGER_
    | ARCHIVELOG
    | NOARCHIVELOG
    | FORCE LOGGING
    | SET STANDBY NOLOGGING FOR (DATA AVAILABILITY | LOAD PERFORMANCE)
    )*
    characterSetClause?
    ;

resetLogsOrNot
   :  ( RESETLOGS | NORESETLOGS) (DATAFILE fileSpecifications)?
   ;

logfileForControlClause
    : LOGFILE (GROUP INTEGER_)? fileSpecification (COMMA_ (GROUP INTEGER_)? fileSpecification)+
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
    | MODIFY RETENTION flashbackArchiveRetention
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
    : CREATE DISKGROUP diskgroupName ((HIGH | NORMAL | FLEX | EXTENDED (SITE siteName)? | EXTERNAL) REDUNDANCY)? diskClause+ attribute?
    ;

diskClause
    : (QUORUM | REGULAR)? (FAILGROUP diskgroupName)? DISK qualifieDiskClause (COMMA_ qualifieDiskClause)*
    ;

qualifieDiskClause
    : searchString (NAME diskName)? (SIZE sizeClause)? (FORCE | NOFORCE)?
    ;

attribute
    : ATTRIBUTE attributeNameAndValue (COMMA_ attributeNameAndValue)*
    ;

attributeNameAndValue
    : SQ_ attributeName SQ_ EQ_ SQ_ attributeValue SQ_
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
   : ALTER JAVA (SOURCE | CLASS) objectName resolveClauses (COMPILE | RESOLVE | invokerRightsClause)
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
      (CONDITION (DROP | SQ_ condition SQ_ EVALUATE PER (STATEMENT | SESSION | INSTANCE)))?
    ;

subAuditClause
    : (privilegeAuditClause)? (actionAuditClause)? (roleAuditClause)? (ONLY TOPLEVEL)?
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
    : ADD BINDING LP_ parameterType (COMMA_ parameterType)* RP_
      RETURN LP_ returnType RP_ implementationClause? usingFunctionClause
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
    : RESIZE ALL (SIZE sizeClause)?
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
    : DROP AFTER INTEGER_ (M | H)
    ;

checkDiskgroupClause
    : CHECK (REPAIR | NOREPAIR)?
    ;

diskgroupTemplateClauses
    : (((ADD | MODIFY) TEMPLATE templateName qualifiedTemplateClause (COMMA_ templateName qualifiedTemplateClause)*)
    | (DROP TEMPLATE templateName (COMMA_ templateName)*))
    ;

qualifiedTemplateClause
    : ATTRIBUTE LP_ redundancyClause stripingClause diskRegionClause RP_
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
    : SET ATTRIBUTE attributeNameAndValue
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
    | MODIFY USERGROUP SQ_ usergroupName SQ_ (ADD | DROP) MEMBER SQ_ username SQ_ (COMMA_ SQ_ username SQ_)*
    | DROP USERGROUP SQ_ usergroupName SQ_)
    ;

userClauses
    : (ADD USER SQ_ username SQ_ (COMMA_ SQ_ username SQ_)*
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
    :OWNER EQ_ SQ_ username SQ_ | GROUP EQ_ SQ_ usergroupName SQ_
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

alterOverflowClause
    : addOverflowClause | overflowClause
    ;

overflowClause
    : OVERFLOW (segmentAttributesClause | allocateExtentClause | shrinkClause | deallocateUnusedClause)+
    ;

addOverflowClause
    : ADD OVERFLOW segmentAttributesClause? LP_ PARTITION segmentAttributesClause? (COMMA_ PARTITION segmentAttributesClause?)* RP_
    ;

scopedTableRefConstraint
    : SCOPE FOR LP_ (columnName | attributeName) RP_ IS (schemaName DOT_)? (tableName | alias)
    ;

alterMvRefresh
    : REFRESH (FAST
    | COMPLETE
    | FORCE
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
    : PURGE IMMEDIATE (SYNCHRONOUS | ASYNCHRONOUS)?
    | START WITH dateValue nextOrRepeatClause?
    | (START WITH dateValue)? nextOrRepeatClause
    ;

nextOrRepeatClause
    : NEXT dateValue | REPEAT INTERVAL intervalExpression
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
    | CONTAINERS containersClause
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

containersClause
    : DEFAULT TARGET EQ_ ((LP_ containerName RP_) | NONE)
    | HOST EQ_ hostName
    | PORT EQ_ NUMBER_
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

createTablespace
    : CREATE (BIGFILE|SMALLFILE)? permanentTablespaceClause
    ;

permanentTablespaceClause
    : TABLESPACE tablespaceName (ONLINE|OFFLINE)
    ;

dropFunction
    : DROP FUNCTION (schemaName DOT_)? function
    ;
