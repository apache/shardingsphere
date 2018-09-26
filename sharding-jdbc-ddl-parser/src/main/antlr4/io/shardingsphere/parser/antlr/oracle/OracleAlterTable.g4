grammar OracleAlterTable;

import OracleKeyword, DataType, Keyword,OracleIndexBase, OracleTableBase,OracleBase,BaseRule,Symbol;

alterTable:
    ALTER TABLE tableName
     ( alterTableProperties
     | columnClauses
     | constraintClauses
     | alterTablePartitioning
     | alterExternalTable
     | moveTableClause
    )?
    ( enableDisableClause
    |((ENABLE | DISABLE) ( (TABLE LOCK) | (ALL TRIGGERS))) 
    )* 
    ;
    
alterTableProperties:
    alterIotOrXml
    | shrinkClause 
    | (READ (ONLY|WRITE))  
    | (REKEY encryptionSpec )
    ;

alterIotOrXml:
    (
      alterIotOrXmlRepeatHeader+
      |renameTable
    )*
    alterIotClauses? alterXmlschemaClause?
    ;

renameTable:
    RENAME TO tableName
    ;

alterIotOrXmlRepeatHeader:
    physicalAttributesClause
    | loggingClause
    | tableCompression
    | supplementalTableLogging
    | allocateExtentClause
    | deallocateUnusedClause
    | ( CACHE | NOCACHE )
    | (RESULT_CACHE LEFT_PAREN MODE (DEFAULT | FORCE) RIGHT_PAREN )
    | upgradeTableClause
    | recordsPerBlockClause
    | parallelClause
    | rowMovementClause
    | flashbackArchiveClause
    ;    
     
supplementalTableLogging:
    addSupplementalLogClause
    | dropSupplementalLogClause
    ;

addSupplementalLogClause:
    ADD addSupplementalLogItem (COMMA addSupplementalLogItem)*
    ;
    
addSupplementalLogItem:
    SUPPLEMENTAL LOG
    (supplementalLogGrpClause | supplementalIdKeyClause)
    ;

dropSupplementalLogClause:
    DROP addSupplementalLogItem (COMMA addSupplementalLogItem)*
    ;
    
dropSupplementalLogItem:
    SUPPLEMENTAL LOG
    (supplementalIdKeyClause | (GROUP groupName))
      ;

allocateExtentClause:
    ALLOCATE EXTENT
      ( LEFT_PAREN ( SIZE sizeClause
      | DATAFILE 'filename'
      | INSTANCE NUMBER
      ) *
    RIGHT_PAREN 
      )?
    ;

deallocateUnusedClause:
    DEALLOCATE UNUSED ( KEEP sizeClause )?
    ;
    
upgradeTableClause:
    UPGRADE ( NOT? INCLUDING DATA )?
       columnProperties?
    ;

recordsPerBlockClause:
    MINIMIZE 
    | NOMINIMIZE
    ;

alterIotClauses:
     indexOrgTableClause
    | alterOverflowClause
    | alterMappingTableClauses
    | COALESCE
    ;

alterOverflowClause:
    addOverflowClause
    | overflowClause
    ;

addOverflowClause:
    ADD OVERFLOW segmentAttributesClause?
    partitionSegmentAttributesWithParen?
    ;

partitionSegmentAttributesWithParen:
    LEFT_PAREN 
        partitionSegmentAttributes (COMMA partitionSegmentAttributes)*
    RIGHT_PAREN 
    ;
     
partitionSegmentAttributes:
    PARTITION segmentAttributesClause?
    ;

overflowClause:
    OVERFLOW
    (
     segmentAttributesClause
     | allocateExtentClause
     | shrinkClause
     | deallocateUnusedClause
    )+
    ;
    
shrinkClause:
    SHRINK SPACE COMPACT? CASCADE?
    ;
    
alterMappingTableClauses:
    MAPPING TABLE
    ( allocateExtentClause
    | deallocateUnusedClause
    )
    ;
    
alterXmlschemaClause:
    (ALLOW ANYSCHEMA)
    | (ALLOW NONSCHEMA)
    | (DISALLOW NONSCHEMA)
    ;
  
columnClauses:
    opColumnClause+
    | renameColumn
    | modifyCollectionRetrieval+
    | modifyLobStorageClause+
    | alterVarrayColProperties+
    ;

opColumnClause:
    addColumn
    | modifyColumn
    | dropColumnClause
    ;
    
addColumn:
    ADD
    columnOrVirtualDefinitions 
    columnProperties?
    outOfLinePartStorages?
    ;

columnOrVirtualDefinitions:
    LEFT_PAREN 
        columnOrVirtualDefinition
        (COMMA columnOrVirtualDefinition)* 
    RIGHT_PAREN
    |columnOrVirtualDefinition
    ;
    
columnOrVirtualDefinition:
    columnDefinition 
    | virtualColumnDefinition
    ;
    
outOfLinePartStorages:
    outOfLinePartStorage ( COMMA outOfLinePartStorage)*
    ;
    
outOfLinePartStorage:
    PARTITION partitionName
    outOfLinePartBody+
    (LEFT_PAREN 
    	SUBPARTITION partitionName outOfLinePartBody+
      RIGHT_PAREN 
    )?    
    ;
    
outOfLinePartBody:
    nestedTableColProperties 
    | lobStorageClause 
    | varrayColProperties
    ;
    
modifyColumn:
    MODIFY 
    ( 
        LEFT_PAREN modifyColProperties (COMMA modifyColProperties)* RIGHT_PAREN
       | modifyColSubstitutable
    )
    ;
    
modifyColProperties:
    columnName 
    ( dataType )?
    ( DEFAULT expr )?
    ( ( ENCRYPT encryptionSpec ) | DECRYPT )?
    inlineConstraint * 
    ( lobStorageClause )?
    ( alterXmlschemaClause )?
    ;
    
modifyColSubstitutable:
    COLUMN columnName
    ( NOT )? SUBSTITUTABLE AT ALL LEVELS
    ( FORCE )?
    ;
    
dropColumnClause:
    ( SET UNUSED columnOrColumnList cascadeOrInvalidate*)
    | dropColumn
    | (DROP ( (UNUSED COLUMNS)| (COLUMNS CONTINUE) )checkpointNumber?)
    ;

dropColumn:
    DROP columnOrColumnList cascadeOrInvalidate* checkpointNumber?
	;
	
columnOrColumnList:
    (COLUMN columnName)
    | (LEFT_PAREN columnName ( COMMA columnName )* RIGHT_PAREN) 
    ;

cascadeOrInvalidate:
    (CASCADE CONSTRAINTS )
    | INVALIDATE
    ;

checkpointNumber:
    CHECKPOINT NUMBER
    ;
    
renameColumn:
    RENAME COLUMN columnName TO columnName
    ;
    
modifyCollectionRetrieval:
    MODIFY NESTED TABLE varrayItemName
    RETURN AS ( LOCATOR | VALUE )
    ;
    
modifyLobStorageClause:
    MODIFY LOB LEFT_PAREN lobItems RIGHT_PAREN 
       LEFT_PAREN modifyLobParameters RIGHT_PAREN 
    ;
    
modifyLobParameters:
    ( storageClause
    |lobCommonParameter
    )+
    ;

alterVarrayColProperties:
    MODIFY VARRAY varrayItemName
    LEFT_PAREN modifyLobParameters RIGHT_PAREN 
    ;
    
constraintClauses:
    addConstraintClause
    | modifyConstraintClause
    | renameConstraintClause
    | dropConstraintClause+
    ;
    
addConstraintClause:
    ADD 
    ( outOfLineConstraint+
     |outOfLineRefConstraint
    )
    ;

modifyConstraintClause:
    MODIFY constraintOption constraintState CASCADE?
    ;

constraintWithName:
    CONSTRAINT constraintName
    ;    
    
constraintOption:
     constraintWithName
     | constraintPrimaryOrUnique
     ;
 
constraintPrimaryOrUnique:
      primaryKey
     | (UNIQUE columnList)
     ;
        
renameConstraintClause:
   RENAME constraintWithName TO constraintName
   ;
   
dropConstraintClause:
    DROP
    (
        (constraintPrimaryOrUnique CASCADE? (( KEEP | DROP) INDEX)?)
       | (CONSTRAINT constraintName ( CASCADE )?)
    ) 
    ;

alterTablePartitioning:
     modifyTableDefaultAttrs
    | alterIntervalPartitioning
    | setSubpartitionTemplate
    | modifyTablePartition
    | modifyTableSubpartition
    | moveTablePartition
    | moveTableSubpartition
    | addTablePartition
    | coalesceTablePartition
    | coalesceTableSubpartition
    | dropTablePartition
    | dropTableSubpartition
    | renamePartitionSubpart
    | truncatePartitionSubpart
    | splitTablePartition
    | splitTableSubpartition
    | mergeTablePartitions
    | mergeTableSubpartitions
    | exchangePartitionSubpart
    ;
    
modifyTableDefaultAttrs:
    MODIFY DEFAULT ATTRIBUTES
    (FOR partitionExtendedName)?
    deferredSegmentCreation?
    segmentAttributesClause?
    tableCompression?
    ( PCTTHRESHOLD NUMBER )?
    ( keyCompression )?
    ( alterOverflowClause )?
    modifyTableLobOrArray*
    ;

modifyTableLobOrArray:
    ((LOB lobItemList )| (VARRAY varrayItemName)) LEFT_PAREN lobParameters RIGHT_PAREN
    ;
    
partitionExtendedName:
    PARTITION 
    (
        partitionName
        |(FOR LEFT_PAREN partitionKeyValue ( COMMA partitionKeyValue)* RIGHT_PAREN)
    )
    ;
    
alterIntervalPartitioning:
    SET
    (
       INTERVAL LEFT_PAREN (expr)? RIGHT_PAREN
       |(STORE IN LEFT_PAREN tablespaceName (COMMA tablespaceName)* RIGHT_PAREN )
    )
    ;
    
setSubpartitionTemplate:
    SET SUBPARTITION TEMPLATE
       (LEFT_PAREN subpartitionDescs? RIGHT_PAREN )
       | hashSubpartitionQuantity
    ;

subpartitionDescs:
    rangeSubpartitionDescs
    |listSubpartitionDescs
    |individualHashSubpartses
    ;

rangeSubpartitionDescs:
    rangeSubpartitionDesc ( COMMA rangeSubpartitionDesc)*
    ;

listSubpartitionDescs:
    listSubpartitionDesc ( COMMA listSubpartitionDesc)*
    ;    

individualHashSubpartses:
    individualHashSubparts ( COMMA individualHashSubparts)*
    ;    

hashSubpartitionQuantity:
    matchNone
    ;
    
modifyTablePartition:
     modifyRangePartition
    | modifyHashPartition
    | modifyListPartition
    ;
    
modifyRangePartition:
    MODIFY partitionExtendedName
       (modifyPartitionCommonOp
       | alterMappingTableClause
       )
    ;
    
modifyPartitionCommonOp:
    partitionAttributes
    | addRangeSubpartition
    | addHashSubpartition
    | addListSubpartition
    | (COALESCE SUBPARTITION updateIndexClauses? parallelClause?)
    | (REBUILD? UNUSABLE LOCAL INDEXES)
    ;
    
alterMappingTableClause:
    matchNone
    ;
    
partitionAttributes:
    (commonPartitionAttributes| shrinkClause)*
    ( OVERFLOW commonPartitionAttributes*)?
    tableCompression?
    modifyTableLobOrArray*
    ;

addRangeSubpartition:
    ADD rangeSubpartitionDesc dependentTablesClause? ( updateIndexClauses )? 
    ;

commonPartitionAttributes:
     physicalAttributesClause
    | loggingClause
    | allocateExtentClause
    | deallocateUnusedClause
    ;
      
dependentTablesClause:
    DEPENDENT TABLES
    LEFT_PAREN 
        tableNameAndPartitionSpecs (COMMA tableNameAndPartitionSpecs)*
    RIGHT_PAREN 
    ;
    
tableNameAndPartitionSpecs:
    tableName LEFT_PAREN partitionSpecs RIGHT_PAREN
    ;
    
partitionSpecs:
    partitionSpec (COMMA partitionSpec)*
    ;
    
partitionSpec:
    PARTITION partitionName? tablePartitionDescription
    ;
    
tablePartitionDescription:
    deferredSegmentCreation?
    segmentAttributesClause?
    (tableCompression | keyCompression)?
    ( OVERFLOW segmentAttributesClause? )?
    ( lobStorageClause
      | varrayColProperties
      | nestedTableColProperties
     )*
    ;
    
updateIndexClauses:
    updateGlobalIndexClause
    | updateAllIndexesClause
    ;
    
updateGlobalIndexClause:
    ( UPDATE | INVALIDATE ) GLOBAL 
    ;
    
updateAllIndexesClause:
    UPDATE INDEXES
       ( LEFT_PAREN 
        indexNameWithUpdateIndexPartition ( COMMA indexNameWithUpdateIndexPartition)*
      RIGHT_PAREN 
       )?
    ;

indexNameWithUpdateIndexPartition:
    indexName 
    LEFT_PAREN
    ( 
        updateIndexPartition
        | updateIndexSubpartition
    )
    RIGHT_PAREN 
    ;
    
updateIndexPartition:
    indexOrSubpartition(COMMA indexOrSubpartition)*
    ;

indexOrSubpartition:
    indexPartitionDescription indexSubpartitionClause?
    ;
    
indexPartitionDescription:
    PARTITION
    (     partitionName
        (segmentAttributesClause| keyCompression)+
        (PARAMETERS LEFT_PAREN STRING RIGHT_PAREN)
       ?
       UNUSABLE?
    )?
    ;
    
updateIndexSubpartition:
    subpartionWithTablespace (COMMA subpartionWithTablespace)*
    ;
    
subpartionWithTablespace:
    SUBPARTITION partitionName? (TABLESPACE tablespaceName )?
       ;
       
addHashSubpartition:
    ADD 
    individualHashSubparts
   dependentTablesClause?
   ( updateIndexClauses )?
   ( parallelClause )?
    ;
    
addListSubpartition:
    ADD listSubpartitionDesc dependentTablesClause? ( updateIndexClauses )?
    ;
    
modifyHashPartition:
    MODIFY partitionExtendedName
    ( partitionAttributes
      | alterMappingTableClause
      | (( REBUILD )? UNUSABLE LOCAL INDEXES)
    )
    ;
    
modifyListPartition:
    MODIFY partitionExtendedName
    ( modifyPartitionCommonOp
    | addOrDropPartition
    )
    ;

addOrDropPartition:
    ( ADD | DROP ) VALUES LEFT_PAREN simpleExpr( COMMA simpleExpr)* RIGHT_PAREN
    ;

modifyTableSubpartition:
    MODIFY subpartitionExtendedName
    ( allocateExtentClause
    | deallocateUnusedCluse
    | shrinkClause
    | ( ( LOB lobItems | VARRAY varrayItemName ) LEFT_PAREN modifyLobParameters RIGHT_PAREN )*
    | (( REBUILD )? UNUSABLE LOCAL INDEXES)
    | addOrDropPartition
    )
    ;
    
subpartitionExtendedName:
    (SUBPARTITION partitionName    )
    | (SUBPARTITION FOR LEFT_PAREN partitionKeyValue ( COMMA partitionKeyValue)* RIGHT_PAREN )
    ;

deallocateUnusedCluse:
    matchNone;
    
moveTablePartition:
    MOVE partitionExtendedName
    ( MAPPING TABLE )?
    tablePartitionDescription
    ( updateIndexClauses )?
    ( parallelClause )?
    ;
    
moveTableSubpartition:
    MOVE subpartitionExtendedName ( partitioningStorageClause )?
     ( updateIndexClauses )? ( parallelClause )?
    ;
    
addTablePartition:
    ADD PARTITION partitionName?
    ( addRangePartitionClause
    | addHashPartitionClause
    | addListPartitionClause
    ) 
    dependentTablesClause?
    ;
    
addRangePartitionClause:
    rangeValuesClause
    tablePartitionDescription
    subpartsDescsOrByQuantity?
    ( updateIndexClauses )?
    ;
    
subpartsDescsOrByQuantity:
    (LEFT_PAREN subpartitionDescs RIGHT_PAREN)
    | hashSubpartsByQuantity
    ;
    
hashSubpartsByQuantity:
    SUBPARTITIONS NUMBER (STORE IN LEFT_PAREN tablespaceName ( COMMA tablespaceName)* RIGHT_PAREN )?
    ;
    
addHashPartitionClause:
    partitioningStorageClause
    ( updateIndexClauses )?
    ( parallelClause )?
    ;
    
addListPartitionClause:
    listValuesClause
    tablePartitionDescription
    subpartsDescsOrByQuantity?
    ( updateIndexClauses )?
    ;
    
coalesceTablePartition:
    COALESCE PARTITION ( updateIndexClauses )? ( parallelClause )?
    ;
    
coalesceTableSubpartition:
    COALESCE SUBPARTITION partitionName (updateIndexClauses)? (parallelClause)?
    ;
    
dropTablePartition:
    DROP partitionExtendedName
       ( updateIndexClauses ( parallelClause )? )?
    ;
    
dropTableSubpartition:
    DROP subpartitionExtendedName
       ( updateIndexClauses ( parallelClause )? )?
    ;
    
renamePartitionSubpart:
    RENAME ( partitionExtendedName| subpartitionExtendedName) TO
    ;
    
truncatePartitionSubpart:
    TRUNCATE 
    ( partitionExtendedName
     | subpartitionExtendedName
    )
       (((DROP ( ALL )?) | REUSE ) STORAGE )?
       (updateIndexClauses ( parallelClause )?)?
    ;
    
splitTablePartition:
    SPLIT partitionExtendedName
     (
        ( AT simpleExprsWithParen ( INTO LEFT_PAREN rangePartitionDescs RIGHT_PAREN )?)
    	 |
       (VALUES simpleExprsWithParen  ( INTO LEFT_PAREN listPartitionDescs RIGHT_PAREN )?)
      ) 
      ( splitNestedTablePart )?
    dependentTablesClause?
    ( updateIndexClauses )?
    ( parallelClause )?
    ;

partitionWithNames:
    partitionWithName (COMMA partitionWithName)*
    ;
    
partitionWithName:
    PARTITION partitionName
    ;
    
rangePartitionDescs:
    rangePartitionDesc COMMA rangePartitionDesc
    ;
    
listPartitionDescs:
    listPartitionDesc COMMA listPartitionDesc
    ;
    

rangePartitionDesc:
    PARTITION partitionName?
    rangeValuesClause?
    tablePartitionDescription
    subpartsDescsOrByQuantity?
    ;
    
listPartitionDesc:
    PARTITION partitionName?
    listValuesClause?
    tablePartitionDescription
     subpartsDescsOrByQuantity?
    ;
    
splitNestedTablePart:
    NESTED TABLE columnName INTO
    LEFT_PAREN partitionSegmentAttribute COMMA 
    partitionSegmentAttribute (splitNestedTablePart)?
    RIGHT_PAREN 
    ( splitNestedTablePart )?
    ;
    
partitionSegmentAttribute:
    PARTITION partitionName (segmentAttributesClause)?
    ;
    
splitTableSubpartition:
    SPLIT subpartitionExtendedName
    (    
       (AT simpleExprsWithParen ( INTO LEFT_PAREN (rangeSubpartitionDescs) RIGHT_PAREN )?)
       |(VALUES simpleExprsWithParen  ( INTO LEFT_PAREN listSubpartitionDescs RIGHT_PAREN )?)
     ) 
    dependentTablesClause?
    ( updateIndexClauses )?
    ( parallelClause )?
    ;
    
mergeTablePartitions:
    MERGE PARTITIONS
    partitionNameOrForKeyValue
    COMMA 
    partitionNameOrForKeyValue
    ( INTO partitionSpec )?
    dependentTablesClause?
    ( updateIndexClauses )?
    ( parallelClause )?
    ;

partitionNameOrForKeyValue:
    partitionName 
    | ( FOR LEFT_PAREN partitionKeyValue ( COMMA partitionKeyValue )* RIGHT_PAREN )
    ;

partitionKeyValue:
    simpleExpr
    ;
    
mergeTableSubpartitions:
    MERGE SUBPARTITIONS
    partitionNameOrForKeyValue
    COMMA 
    partitionNameOrForKeyValue
    ( INTO 
        (rangeSubpartitionDesc 
          | listSubpartitionDesc
        )
     )?
    dependentTablesClause?
    ( updateIndexClauses )?
    ( parallelClause )?
    ;
    
exchangePartitionSubpart:
    EXCHANGE 
    (
         partitionExtendedName
        | subpartitionExtendedName
    )
    WITH TABLE  tableName
    ( ( INCLUDING | EXCLUDING ) INDEXES )?
    ( ( WITH | WITHOUT ) VALIDATION )?
    ( exceptionsClause )?
    ( updateIndexClauses ( parallelClause )? )?
    ;

alterExternalTable:
    ( addColumn
    | modifyColumn
    | dropColumn
    | parallelClause
    | externalDataProperties
    | (REJECT LIMIT ( NUMBER | UNLIMITED ))
    | (PROJECT COLUMN ( ALL | REFERENCED ))
    )+
    ;
    
externalDataProperties:
    matchNone
    ;
    
moveTableClause:
    MOVE ( ONLINE )?
    segmentAttributesClause?
    tableCompression?
    ( indexOrgTableClause )?
    ( lobStorageClause | varrayColProperties )*
    ( parallelClause )?
    ;


