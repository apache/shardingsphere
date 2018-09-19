grammar OracleCreateTable;

import OracleKeyword, DataType, Keyword, OracleIndexBase,OracleTableBase,OracleBase,BaseRule,Symbol;

createTable:
    CREATE ( GLOBAL TEMPORARY )? TABLE  tableName
    ( relationalTable | objectTable | xmltypeTable )
    ;
    
relationalTable:
    ( LEFT_PAREN relationalProperties RIGHT_PAREN )
    ( ON COMMIT ( DELETE | PRESERVE ) ROWS )?
    physicalProperties?
    tableProperties
    ;
    
relationalProperties:
    relationalProperty (COMMA relationalProperty)*
    ;
    
relationalProperty:
     columnDefinition
    | virtualColumnDefinition
    | outOfLineConstraint
    | outOfLineRefConstraint
    | supplementalLoggingProps
    ;
    
tableProperties:
    columnProperties?
    tablePartitioningClauses?
    ( CACHE | NOCACHE )?
    ( RESULT_CACHE LEFT_PAREN MODE (DEFAULT | FORCE ) RIGHT_PAREN )? 
    parallelClause?
    ( ROWDEPENDENCIES | NOROWDEPENDENCIES )?
    ( enableDisableClause )*
    ( rowMovementClause )?
    ( flashbackArchiveClause )?
    ( AS subquery )?
    ;
    
tablePartitioningClauses:
     rangePartitions
    | listPartitions
    | hashPartitions
    | compositeRangePartitions
    | compositeListPartitions
    | compositeHashPartitions
    | referencePartitioning
    | systemPartitioning
    ;
    
rangePartitions:
    PARTITION BY RANGE columnList 
    (INTERVAL LEFT_PAREN expr RIGHT_PAREN (STORE IN LEFT_PAREN tablespaceName (COMMA tablespaceName)* RIGHT_PAREN )?)?
    LEFT_PAREN rangePartitionItem(COMMA rangePartitionItem)* RIGHT_PAREN
    ;
     
rangePartitionItem:
    PARTITION partitionName? rangeValuesClause tablePartitionDescription
    ;
    
listPartitions:
    PARTITION BY LIST LEFT_PAREN columnName RIGHT_PAREN 
    LEFT_PAREN listPartitionValue(COMMA listPartitionValue)* RIGHT_PAREN
    ;

listPartitionValue:
    PARTITION partitionName? listValuesClause tablePartitionDescription
    ;
    
hashPartitions:
    PARTITION BY HASH columnList 
    ( individualHashPartitions
    | hashPartitionsByQuantity
    )
    ;
    
compositeRangePartitions:
    PARTITION BY RANGE columnList 
    ( INTERVAL LEFT_PAREN expr RIGHT_PAREN ( STORE IN LEFT_PAREN tablespaceName ( COMMA tablespaceName)* RIGHT_PAREN )?)?
    (     
       subpartitionByRange
       |subpartitionByList
       |subpartitionByHash
    )
    LEFT_PAREN rangePartitionDesc ( COMMA rangePartitionDesc )* RIGHT_PAREN 
    ;
    
subpartitionByRange:
    SUBPARTITION BY RANGE columnList (subpartitionTemplate)?
    ;
    
subpartitionTemplate:
    SUBPARTITION TEMPLATE
    (subpartitionListItem
    |hashSubpartsByQuantity
    ) 
    ;
    
subpartitionListItem:
    LEFT_PAREN ( 
      (rangeSubpartitionDesc ( COMMA rangeSubpartitionDesc)*)
    | (listSubpartitionDesc ( COMMA listSubpartitionDesc)*)
    | (individualHashSubparts ( COMMA individualHashSubparts)*)
    )
    RIGHT_PAREN
    ;

subpartitionByList:
    SUBPARTITION BY LIST LEFT_PAREN columnName RIGHT_PAREN subpartitionTemplate?
    ;
    
subpartitionByHash:
    SUBPARTITION BY HASH columnList 
    ( SUBPARTITIONS NUMBER
           ( STORE IN LEFT_PAREN tablespaceName ( COMMA tablespaceName)* RIGHT_PAREN )?
       | subpartitionTemplate
    )?
    ;

compositeListPartitions:
    PARTITION BY LIST LEFT_PAREN columnName RIGHT_PAREN 
    ( 
        subpartitionByRange
      | subpartitionByList
      | subpartitionByHash
    )
    LEFT_PAREN listPartitionDesc ( COMMA listPartitionDesc)* RIGHT_PAREN 
    ;
    
listPartitionDesc:
    PARTITION partitionName
    listValuesClause
    tablePartitionDescription
    ( 
        LEFT_PAREN 
          rangeSubpartitionDesc ( COMMA rangeSubpartitionDesc)*
          | listSubpartitionDesc COMMA ( COMMA listSubpartitionDesc)*
          | individualHashSubparts ( COMMA individualHashSubparts)*
        RIGHT_PAREN 
       | hashSubpartsByQuantity
    )?
    ;

compositeHashPartitions:
    PARTITION BY HASH columnList 
    ( 
          subpartitionByRange
      | subpartitionByList
      | subpartitionByHash
    )
    (
          individualHashPartitions
      | hashPartitionsByQuantity
    )
    ;

referencePartitioning:
    PARTITION BY REFERENCE LEFT_PAREN constraintName RIGHT_PAREN 
    ( LEFT_PAREN referencePartitionDesc* RIGHT_PAREN )?
    ;
    
referencePartitionDesc:
    PARTITION partitionName? tablePartitionDescription RIGHT_PAREN 
    ;
    
systemPartitioning:
    PARTITION BY SYSTEM 
    ( (PARTITIONS NUMBER)
    | (referencePartitionDesc
       ( COMMA referencePartitionDesc *)?)
    )?
    ;

objectTable:
    OF
    typeName
    ( objectTableSubstitution )?
    ( LEFT_PAREN objectProperties RIGHT_PAREN )?
    ( ON COMMIT ( DELETE | PRESERVE ) ROWS )?
    ( oidClause )?
    ( oidIndexClause )?
    ( physicalProperties )?
    tableProperties
    ;
    
objectTableSubstitution:
    ( NOT )? SUBSTITUTABLE AT ALL 
    ;
    
oidClause:
    OBJECT IDENTIFIER IS
    ( SYSTEM GENERATED | PRIMARY KEY )
    ;
    
oidIndexClause:
    OIDINDEX ( indexName )?
    LEFT_PAREN ( physicalAttributesClause
    | TABLESPACE tablespaceName
    )*
    RIGHT_PAREN 
    ;
    
xmltypeTable:
    OF XMLTYPE
    ( LEFT_PAREN objectProperties RIGHT_PAREN )?
    ( XMLTYPE xmltypeStorage )?
    ( xmlschemaSpec )?
    ( xmltypeVirtualColumns )?
    ( ON COMMIT ( DELETE | PRESERVE ) ROWS )?
    ( oidClause )?
    ( oidIndexClause )?
    ( physicalProperties )?
    tableProperties 
    ;
    
xmltypeVirtualColumns:
    VIRTUAL COLUMNS 
    LEFT_PAREN 
        xmltypeVirtualColumn(COMMA xmltypeVirtualColumn)* 
    RIGHT_PAREN 
    ;
    
xmltypeVirtualColumn:
    columnName AS LEFT_PAREN expr RIGHT_PAREN
    ;