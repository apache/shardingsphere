grammar OracleIndexBase;

import OracleKeyword, DataType, Keyword,OracleBase,BaseRule,Symbol;

createIndex:
    CREATE ( UNIQUE | BITMAP )? INDEX  indexName
    ON 
    ( clusterIndexClause
     | tableIndexClause
     | bitmapJoinIndexClause
    )
    UNUSABLE? 
    ;
    
clusterIndexClause:
    CLUSTER  clusterName 
    ;
    
tableIndexClause:
    tableName alias?
    LEFT_PAREN indexExprSort
    (COMMA indexExprSort)* RIGHT_PAREN 
    indexProperties?
    ;

indexExprSort:
    indexExpr ( ASC | DESC )?
    ;
    
indexExpr:
    columnName 
    | expr 
    ;
    
indexProperties:
    (globalPartitionedIndex
    | localPartitionedIndex
      | indexAttribute)+
    | (INDEXTYPE IS  domainIndexClause)
    ;
    
globalPartitionedIndex:
    GLOBAL PARTITION BY
       ( (RANGE columnList 
            LEFT_PAREN indexPartitioningClause (COMMA indexPartitioningClause)* RIGHT_PAREN)
       | (HASH columnList 
            ( individualHashPartitions
            | hashPartitionsByQuantity
            ))
       )
    ;
    
indexPartitioningClause:
    PARTITION partitionName?
    VALUES LESS THAN simpleExprsWithParen 
    segmentAttributesClause?
    ;

individualHashPartitions:
    LEFT_PAREN partitioningStorageClause
    (COMMA partitioningStorageClause)* RIGHT_PAREN 
    ;
    
partitioningStorageClause:
    PARTITION partitionName? partitioningStorage?
    ;
    
partitioningStorage:
    (TABLESPACE tablespaceName)
    | (OVERFLOW (TABLESPACE tablespaceName)?)
    | tableCompression
    | keyCompression
    | lobPartitioningStorage
    | (VARRAY varrayItemName STORE AS (SECUREFILE | BASICFILE)? LOB segName)
    ;
 
tableCompression:
    (COMPRESS(BASIC 
           | FOR ( OLTP
                 |((QUERY | ARCHIVE) ( LOW | HIGH )?) 
                 )
           )?)
    | NOCOMPRESS 
    ;

hashPartitionsByQuantity:
    PARTITIONS NUMBER
    ( STORE IN LEFT_PAREN tablespaceName ( COMMA tablespaceName )* RIGHT_PAREN )?
    ( tableCompression | keyCompression )?
    ( OVERFLOW STORE IN LEFT_PAREN tablespaceName ( COMMA tablespaceName )* RIGHT_PAREN )?
    ;

keyCompression:
    (COMPRESS NUMBER?)
    | NOCOMPRESS
    ;

lobPartitioningStorage:
    LOB LEFT_PAREN lobItems RIGHT_PAREN STORE AS (BASICFILE | SECUREFILE)?
    ((segName tablespaceClauseWithParen?)
    | tablespaceClauseWithParen
    )?
    ;

segmentAttributesClause:
     (physicalAttributesClause
    | (TABLESPACE tablespaceName)
    | loggingClause)+
    ;
    
tablespaceClauseWithParen:
    LEFT_PAREN tablespaceClause RIGHT_PAREN
    ;
    
tablespaceClause:
    TABLESPACE tablespaceName
    ;
    
physicalAttributesClause:
    storageClause
    |((PCTFREE 
      | PCTUSED 
      | INITRANS) NUMBER)
    ;

loggingClause:
    LOGGING 
    | NOLOGGING 
    | FILESYSTEM_LIKE_LOGGING
    ;

storageClause:
    STORAGE LEFT_PAREN storageOption(COMMA storageOption)* RIGHT_PAREN
    ;

storageOption:
    (INITIAL sizeClause)
    | (NEXT sizeClause)
    | (MINEXTENTS NUMBER)
    | (MAXEXTENTS ( NUMBER | UNLIMITED ))
    | maxsizeClause
    | (PCTINCREASE NUMBER)
    | (FREELISTS NUMBER)
    | (FREELIST GROUPS NUMBER)
    | (OPTIMAL (sizeClause | NULL )?)
    | (BUFFER_POOL ( KEEP | RECYCLE | DEFAULT ))
    | (FLASH_CACHE ( KEEP | NONE | DEFAULT ))
    | ENCRYPT
    ;
 
sizeClause:
    NUMBER ID?
    ;
    
maxsizeClause:
    MAXSIZE ( UNLIMITED | sizeClause )
    ;

localPartitionedIndex:
    LOCAL
    (storeInClause
    | onCompPartitionedTable
    | onPartitionedTable
    )?
    ;
    
storeInClause:
    STORE IN LEFT_PAREN tablespaceName( COMMA tablespaceName )* RIGHT_PAREN
    ;
    
onPartitionedTable:
    LEFT_PAREN
    (
    onRangePartitionedTable
    |onHashPartitionedTable
    |indexSubpartitionClause
    )
    RIGHT_PAREN
    ; 
    
onRangePartitionedTable:
    onRangePartitionedItem (COMMA onRangePartitionedItem)*
    ;
    
onRangePartitionedItem:
    PARTITION
    partitionName?
    ( segmentAttributesClause
      | keyCompression
    )*
    UNUSABLE?
    ;

onHashPartitionedTable:
    onHashPartitionedItem (COMMA onHashPartitionedItem)* 
    ;

onHashPartitionedItem:
    PARTITION partitionName? ( TABLESPACE tablespaceName )? keyCompression? UNUSABLE?
    ;
    
onCompPartitionedTable:
    storeInClause?
    LEFT_PAREN 
    onCompPartitionedItem (COMMA onCompPartitionedItem)*
    RIGHT_PAREN 
    ;
    
onCompPartitionedItem:
    PARTITION partitionName?
    ( segmentAttributesClause
       | keyCompression
    )*
    UNUSABLE? indexSubpartitionClause?
    ;

indexSubpartitionClause:
    storeInClause
    |(LEFT_PAREN indexSubpartitionItem(COMMA indexSubpartitionItem)* RIGHT_PAREN)
    ;
    
indexSubpartitionItem:
    SUBPARTITION partitionName?
    (TABLESPACE tablespaceName )? ( keyCompression )? UNUSABLE?
    ;
    
indexAttribute:
    physicalAttributesClause
    | loggingClause
    | ONLINE
    | (TABLESPACE ( tablespaceName | DEFAULT))
    | keyCompression
    | SORT 
    | NOSORT
    | REVERSE
    | VISIBLE 
    | INVISIBLE
    | parallelClause
    ;

parallelClause:
    NOPARALLEL 
    | (PARALLEL ( NUMBER )? )
    ;
    
domainIndexClause:
    indexTypeName
    localDomainIndexClause?
    parallelClause?
    odciParameter?
    ;
    
localDomainIndexClause:
    LOCAL
    ( 
      LEFT_PAREN 
          partionWithOdciParameter (COMMA  partionWithOdciParameter)*
       RIGHT_PAREN 
    )?
    ;

partionWithOdciParameter:
    PARTITION partitionName odciParameter?
    ;

odciParameter:
    PARAMETERS LEFT_PAREN STRING RIGHT_PAREN 
    ;

bitmapJoinIndexClause:
    tableName
    LEFT_PAREN 
    columnSortClause( COMMA columnSortClause)*
    RIGHT_PAREN 
    FROM tableAndAlias (COMMA tableAndAlias)*
    WHERE expr
    localPartitionedIndex? 
    ;