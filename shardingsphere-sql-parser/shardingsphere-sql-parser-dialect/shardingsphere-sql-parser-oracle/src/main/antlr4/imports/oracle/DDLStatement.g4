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

import Symbol, Keyword, OracleKeyword, Literals, BaseRule;

createTable
    : CREATE createTableSpecification TABLE tableName createSharingClause createDefinitionClause createMemOptimizeClause createParentClause
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

dropTable
    : DROP TABLE tableName (CASCADE CONSTRAINTS)? (PURGE)?
    ;
 
dropIndex
    : DROP INDEX indexName ONLINE? FORCE? ((DEFERRED|IMMEDIATE) INVALIDATION)?
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

domainIndexClause
    : indexTypeName
    ;

createSharingClause
    : (SHARING EQ_ (METADATA | DATA | EXTENDED DATA | NONE))?
    ;

createDefinitionClause
    : createRelationalTableClause | createObjectTableClause
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
    : OF objectName objectTableSubstitution? (LP_ objectProperties RP_)? (ON COMMIT (DELETE | PRESERVE) ROWS)?
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
    : START WITH (NUMBER_ | LIMIT VALUE)
    | INCREMENT BY NUMBER_
    | MAXVALUE NUMBER_
    | NOMAXVALUE
    | MINVALUE NUMBER_
    | NOMINVALUE
    | CYCLE
    | NOCYCLE
    | CACHE NUMBER_
    | NOCACHE
    | ORDER
    | NOORDER
    ;

encryptionSpecification
    : (USING STRING_)? (IDENTIFIED BY STRING_)? STRING_? (NO? SALT)?
    ;

inlineConstraint
    : (CONSTRAINT ignoredIdentifier)? (NOT? NULL | UNIQUE | primaryKey | referencesClause | CHECK LP_ expr RP_) constraintState*
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
    : SCOPE IS tableName | WITH ROWID | (CONSTRAINT ignoredIdentifier)? referencesClause constraintState*
    ;

virtualColumnDefinition
    : columnName dataType? (GENERATED ALWAYS)? AS LP_ expr RP_ VIRTUAL? inlineConstraint*
    ;

outOfLineConstraint
    : (CONSTRAINT ignoredIdentifier)?
    (UNIQUE columnNames
    | primaryKey columnNames 
    | FOREIGN KEY columnNames referencesClause
    | CHECK LP_ expr RP_
    ) constraintState*
    ;

outOfLineRefConstraint
    : SCOPE FOR LP_ lobItem RP_ IS tableName
    | REF LP_ lobItem RP_ WITH ROWID
    | (CONSTRAINT ignoredIdentifier)? FOREIGN KEY lobItemList referencesClause constraintState*
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
    : LP_ indexExpression (COMMA_ indexExpression)* RP_
    ;

indexExpression
    : (columnName | expr) (ASC | DESC)?
    ;

bitmapJoinIndexClause
    : tableName columnSortsClause_ FROM tableAlias WHERE expr
    ;

columnSortsClause_
    : LP_ columnSortClause_ (COMMA_ columnSortClause_)* RP_
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
    : (alterTableProperties | columnClauses | constraintClauses | alterExternalTable)?
    ;

alterTableProperties
    : renameTableSpecification | REKEY encryptionSpecification
    ;

renameTableSpecification
    : RENAME TO identifier
    ;

columnClauses
    : operateColumnClause+ | renameColumnClause
    ;

operateColumnClause
    : addColumnSpecification | modifyColumnSpecification | dropColumnClause
    ;

addColumnSpecification
    : ADD columnOrVirtualDefinitions columnProperties?
    ;

columnOrVirtualDefinitions
    : LP_ columnOrVirtualDefinition (COMMA_ columnOrVirtualDefinition)* RP_ | columnOrVirtualDefinition
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
    : COLUMN columnName | LP_ columnName (COMMA_ columnName)* RP_
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
    : CONSTRAINT ignoredIdentifier
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
    constraintPrimaryOrUnique CASCADE? ((KEEP | DROP) INDEX)? | (CONSTRAINT ignoredIdentifier CASCADE?)
    ) 
    ;

alterExternalTable
    : (addColumnSpecification | modifyColumnSpecification | dropColumnSpecification)+
    ;

objectProperties
    : objectProperty (COMMA_ objectProperty)*
    ;

objectProperty
    : (columnName | attributeName) (DEFAULT expr)? (inlineConstraint* | inlineRefConstraint?) | outOfLineConstraint | outOfLineRefConstraint
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
    : PARALLEL
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

commitClause
    : (ON COMMIT (DROP | PRESERVE) ROWS)? (ON COMMIT (DELETE | PRESERVE) ROWS)?
    ;

physicalProperties
    : deferredSegmentCreation? segmentAttributesClause tableCompression? inmemoryTableClause? ilmClause?
    | deferredSegmentCreation? (organizationClause?|externalPartitionClause?)
    | clusterClause
    ;

deferredSegmentCreation
    : SEGMENT CREATION (IMMEDIATE|DEFERRED)
    ;

segmentAttributesClause
    : physicalAttributesClause
    | (TABLESPACE tablespaceName | TABLESPACE SET tablespaceSetName)
    | loggingClause
    ;

physicalAttributesClause
    : (PCTFREE NUMBER_ | PCTUSED NUMBER_ | INITRANS NUMBER_ | storageClause)*
    ;

loggingClause
    : LOGGING | NOLOGGING |  FILESYSTEM_LIKE_LOGGING
    ;

storageClause
    : STORAGE
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
    )+
    ;

sizeClause
    : NUMBER_ (K | M | G | T | P | E)?
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
    : (INMEMORY inmemoryMemcompress? | NO INMEMORY) LP_ columnName (DOT columnName)* RP_
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
    : NUMBER ((DAY | DAYS) | (MONTH | MONTHS) | (YEAR | YEARS))
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
    : (mappingTableClause | PCTTHRESHOLD NUMBER | prefixCompression)* indexOrgOverflowClause?
    ;

externalTableClause
    : LP_ (TYPE accessDriverType)? (externalTableDataProps)? RP_ (REJECT LIMIT (NUMBER | UNLIMITED))? inmemoryTableClause?
    ;

externalTableDataProps
    : (DEFAULT DIRECTORY directoryName)? (ACCESS PARAMETERS ((opaqueFormatSpec) | USING CLOB subquery))? (LOCATION LP_ (directoryName COLON_)? 'location_specifier' (COMMA_ (directoryName COLON_)? 'location_specifier')+ RP_)?
    ;

mappingTableClause
    : MAPPING TABLE | NOMAPPING
    ;

prefixCompression
    : COMPRESS NUMBER? | NOCOMPRESS
    ;

indexOrgOverflowClause
    :  (INCLUDING columnName)? OVERFLOW segmentAttributesClause?
    ;

externalPartitionClause
    : EXTERNAL PARTITION ATTRIBUTES externalTableClause (REJECT LIMIT)?
    ;

clusterRelatedClause
    : CLUSTER clusterName LP_ (columnName (COMMA_ columnName)*) RP_
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
    : PARTITION BY RANGE LP_ columnName (COMMA columnName)* RP_
      (INTERVAL LP_ expr RP_ (STORE IN LP_ tablespaceName (COMMA tablespaceName)* RP_)?)?
      LP_ PARTITION partition? rangeValuesClause tablePartitionDescription (COMMA PARTITION partition? rangeValuesClause tablePartitionDescription externalPartSubpartDataProps?)* RP_
    ;

rangeValuesClause
    : VALUES LESS THAN LP_ (numberLiterals | MAXVALUE) (COMMA (numberLiterals | MAXVALUE))*RP_
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
      (LOBStorageClause | varrayColProperties | nestedTableColProperties)*
    ;

inmemoryClause
    : INMEMORY inmemoryAttributes? | NO INMEMORY
    ;

varrayColProperties
    : VARRAY varrayItem (substitutableColumnClause? varrayStorageClause | substitutableColumnClause)
    ;

nestedTableColProperties
    : 
    ;

varrayStorageClause
    : 
    ;

externalPartSubpartDataProps
    :
    ;

listPartitions
    : PARTITION BY LIST LP_ columnName (COMMA columnName)* RP_
      (AUTOMATIC (STORE IN LP_ tablespaceName (COMMA tablespaceName)* RP_))?
      LP_ PARTITION partition? listValuesClause tablePartitionDescription (COMMA PARTITION partition? listValuesClause tablePartitionDescription externalPartSubpartDataProps?)* RP_
    ;

hashPartitions
    : PARTITION BY HASH LP_ columnName (COMMA columnName)* RP_ (individualHashPartitions | hashPartitionsByQuantity)
    ;

compositeRangePartitions
    : PARTITION BY RANGE LP_ columnName (COMMA columnName)* RP_ 
      (INTERVAL LP_ expr RP_ (STORE IN LP_ tablespaceName (COMMA tablespaceName)* RP_)?)?
      (subpartitionByRange | subpartitionByList | subpartitionByHash) 
      LP_ rangePartitionDesc (COMMA rangePartitionDesc)* RP_
    ;

compositeListPartitions
    : PARTITION BY LIST LP_ columnName (COMMA columnName)* RP_ 
      (AUTOMATIC (STORE IN LP_ tablespaceName (COMMA tablespaceName)* RP_)?)?
      (subpartitionByRange | subpartitionByList | subpartitionByHash) 
      LP_ listPartitionDesc (COMMA listPartitionDesc)* RP_
    ;

compositeHashPartitions
    : PARTITION BY HASH LP_ columnName (COMMA columnName)* RP_ (subpartitionByRange | subpartitionByList | subpartitionByHash) (individualHashPartitions | hashPartitionsByQuantity)
    ;

referencePartitioning
    :PARTITION BY REFERENCE LP_ constraint RP_ (LP_ referencePartitionDesc (COMMA referencePartitionDesc)* RP_)?
    ;

systemPartitioning
    : PARTITION BY SYSTEM (PARTITIONS NUMBER | referencePartitionDesc (COMMA referencePartitionDesc)*)?
    ;

consistentHashPartitions
    : PARTITION BY CONSISTENT HASH LP_ columnName (COMMA columnName)* RP_ (PARTITIONS AUTO)? TABLESPACE SET tablespaceSetName
    ;

consistentHashWithSubpartitions
    : PARTITION BY CONSISTENT HASH LP_ columnName (COMMA columnName)* RP_ (subpartitionByRange | subpartitionByList | subpartitionByHash)  (PARTITIONS AUTO)?
    ;

partitionsetClauses
    : rangePartitionsetClause | listPartitionsetClause
    ;

attributeClusteringClause
    : CLUSTERING clusteringJoin? clusterClause clusteringWhen? zonemapClause?
    ;

clusteringJoin
    :
    ;

clusterClause
    :
    ;

clusteringWhen
    :
    ;

zonemapClause
    :
    ;

rowMovementClause
    : (ENABLE | DISABLE) ROW MOVEMENT
    ;

flashbackArchiveClause
    : FLASHBACK ARCHIVE flashbackArchive? | NO FLASHBACK ARCHIVE
    ;

flashbackArchive
    :
    ;
