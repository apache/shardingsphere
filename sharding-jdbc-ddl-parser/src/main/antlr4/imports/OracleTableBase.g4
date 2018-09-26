grammar OracleTableBase;

import OracleKeyword,Keyword,Symbol,OracleBase,BaseRule,DataType;

columnDefinition:
    columnName dataType ( SORT )?
    ( DEFAULT expr )?
    ( ENCRYPT encryptionSpec )?
    ( 
       inlineConstraint+ 
      | inlineRefConstraint
    )?
    ;
    
virtualColumnDefinition:
    columnName dataType? (GENERATED ALWAYS)? AS LEFT_PAREN expr RIGHT_PAREN 
    (VIRTUAL)?
    inlineConstraint*
    ;

inlineConstraint:
    ( CONSTRAINT constraintName )?
    ( (NOT? NULL)
    | UNIQUE
    | primaryKey
    | referencesClause
    | (CHECK LEFT_PAREN expr RIGHT_PAREN)
    )
    constraintState?
    ;
    
referencesClause:
    REFERENCES  objectName columnList?
      (ON DELETE ( CASCADE | (SET NULL)) )?
    ;
    
constraintState:
    (notDeferrable
    |initiallyClause
    |( RELY | NORELY )
    |( usingIndexClause )
    |( ENABLE | DISABLE )
    |( VALIDATE | NOVALIDATE )
    |exceptionsClause
    )+
    ;

notDeferrable:
    NOT? DEFERRABLE
    ;
    
initiallyClause:
    INITIALLY ( IMMEDIATE | DEFERRED )
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

usingIndexClause:
    USING INDEX
    (  indexName
    | (LEFT_PAREN createIndex RIGHT_PAREN) 
    | indexProperties
    )?
    ;
    
createIndex:
    matchNone
    ;

indexProperties:
    matchNone
    ;
    
segmentAttributesClause:
     (physicalAttributesClause
    | (TABLESPACE tablespaceName)
    | loggingClause)+
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

tablespaceClauseWithParen:
    LEFT_PAREN tablespaceClause RIGHT_PAREN
    ;
    
tablespaceClause:
    TABLESPACE tablespaceName
    ;

hashPartitionsByQuantity:
    PARTITIONS NUMBER
    ( STORE IN LEFT_PAREN tablespaceName ( COMMA tablespaceName )* RIGHT_PAREN )?
    ( tableCompression | keyCompression )?
    ( OVERFLOW STORE IN LEFT_PAREN tablespaceName ( COMMA tablespaceName )* RIGHT_PAREN )?
    ;


exceptionsClause:
    EXCEPTIONS INTO  
    ;
    
inlineRefConstraint:
     (SCOPE  IS  tableName)
    | (WITH ROWID)
    | (( CONSTRAINT constraintName )?
      referencesClause
      constraintState?)
    ;

outOfLineConstraint:
      (CONSTRAINT constraintName )?
    ( (UNIQUE columnList )
    | (primaryKey columnList) 
    | (FOREIGN KEY columnList referencesClause)
    | (CHECK LEFT_PAREN expr RIGHT_PAREN )
    ) 
    constraintState?
    ;
    
outOfLineRefConstraint:
    ( SCOPE FOR LEFT_PAREN lobItem RIGHT_PAREN 
        IS  tableName)
    | (REF LEFT_PAREN lobItem RIGHT_PAREN WITH ROWID)
    | ((CONSTRAINT constraintName)? FOREIGN KEY
        lobItemList referencesClause
        constraintState?)
    ;
    
supplementalLoggingProps:
    SUPPLEMENTAL LOG 
    (supplementalLogGrpClause
    | supplementalIdKeyClause
    )
    ;

supplementalLogGrpClause:
    GROUP groupName
    LEFT_PAREN columnName (NO LOG)?
    ( COMMA columnName (NO LOG)? )* RIGHT_PAREN 
    ( ALWAYS )?
    ;

supplementalIdKeyClause:
    DATA
     LEFT_PAREN supplementalIdKey
        (COMMA supplementalIdKey)*
     RIGHT_PAREN 
    ;
 
supplementalIdKey:
    ALL | primaryKey | UNIQUE | FOREIGN KEY
    ;

physicalProperties:
    ( (deferredSegmentCreation? segmentAttributesClause tableCompression?)
    | (deferredSegmentCreation? ORGANIZATION
      ( (HEAP ( segmentAttributesClause )? tableCompression?)
      | (INDEX ( segmentAttributesClause )? indexOrgTableClause)
      | (EXTERNAL externalTableClause)
      ))
    | (CLUSTER clusterName columnList )
    )
    ;
    
deferredSegmentCreation:
    SEGMENT CREATION ( IMMEDIATE | DEFERRED )
    ;
    
indexOrgTableClause:
     ( mappingTableClause
      | PCTTHRESHOLD NUMBER
      | keyCompression
      |indexOrgOverflowClause
    )+
    ;
    
mappingTableClause:
    (MAPPING TABLE) 
    | NOMAPPING
    ;
    
indexOrgOverflowClause:
      ( INCLUDING columnName )
    |OVERFLOW 
    |segmentAttributesClause 
    ;
    
externalTableClause:
    matchNone
    ;

varrayColProperties:
    VARRAY varrayItemName 
    ( (substitutableColumnClause? varrayStorageClause)
    | substitutableColumnClause
    )
    ;

substitutableColumnClause:
    ( ELEMENT? IS OF TYPE? LEFT_PAREN ONLY? typeName RIGHT_PAREN) 
    | (NOT? SUBSTITUTABLE AT ALL LEVELS)
    ;

varrayStorageClause:
    STORE AS (SECUREFILE | BASICFILE)? LOB
    ( (segName? LEFT_PAREN lobStorageParameters RIGHT_PAREN)
    | segName
    )
    ;
    
lobStorageParameters:
     ((TABLESPACE tablespaceName)
      | (lobParameters storageClause?)
    | storageClause
    )+ 
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

lobParameters:
    lobParameter+
    ;

lobParameter:
    ((ENABLE | DISABLE) STORAGE IN ROW)
    |lobCommonParameter
    ;
    
lobCommonParameter:
    (CHUNK NUMBER)
    | (PCTVERSION NUMBER)
    | (FREEPOOLS NUMBER)
    | lobRetentionClause
    | lobDeduplicateClause
    | lobCompressionClause
    | ((ENCRYPT encryptionSpec) | DECRYPT )
    | (( CACHE | NOCACHE | (CACHE READS))loggingClause?) 
    ;
    
encryptionSpec:
    ( USING STRING )?
    ( IDENTIFIED BY STRING )?
     ( STRING )?
     ( ( NO )? SALT )?
    ;

lobRetentionClause:
    RETENTION ( MAX | MIN NUMBER | AUTO | NONE )?
    ;
    
lobDeduplicateClause:
    ( DEDUPLICATE  
    | KEEP_DUPLICATES
    )
    ;
    
lobCompressionClause:
    ( COMPRESS (HIGH | MEDIUM | LOW )?)
    | NOCOMPRESS
    ;
    
lobStorageClause:
    LOB
    ( lobItemList 
         STORE AS ( SECUREFILE 
              BASICFILE
                      | (LEFT_PAREN lobStorageParameters RIGHT_PAREN) 
                  )+
        )
    | (LEFT_PAREN lobItem RIGHT_PAREN 
         STORE AS  (SECUREFILE 
              BASICFILE
                      | segName 
                      | (LEFT_PAREN lobStorageParameters RIGHT_PAREN)
                      )+
    )
    ;    
    
lobPartitionStorage:
    PARTITION partitionName
    ( lobStorageClause | varrayColProperties )*
      ( LEFT_PAREN SUBPARTITION partitionName
         ( lobPartitioningStorage | varrayColProperties )*
        RIGHT_PAREN 
    )?
    ;
    
xmltypeColumnProperties:
    XMLTYPE ( COLUMN )? columnName
       ( xmltypeStorage )?
    ( xmlschemaSpec )?
    ;
    
xmltypeStorage:
    STORE
    storeAsClause
    |( ALL VARRAYS AS ( LOBS | TABLES ))
    ;

storeAsClause:
    AS
    (OBJECT RELATIONAL)
    |(SECUREFILE | BASICFILE)? (CLOB | BINARY XML)
      ((segName lobStorageParametersWithParen?)
         |lobStorageParametersWithParen 
      )?
    ;

lobStorageParametersWithParen:
    LEFT_PAREN lobStorageParameters RIGHT_PAREN 
    ;
    
xmlschemaSpec:
    ( XMLSCHEMA xmlschemaUrl )?
    ELEMENT ( elementName | xmlschemaUrl POUND_ elementName )
    ( ( ALLOW | DISALLOW ) NONSCHEMA )?
    ( ( ALLOW | DISALLOW ) ANYSCHEMA )?
    ;

xmlschemaUrl:
    STRING;

flashbackArchiveClause:
    (FLASHBACK ARCHIVE (archiveName)?)
    | (NO FLASHBACK) 
    ;
    
rangeValuesClause:
    VALUES LESS THAN
    LEFT_PAREN  
       simpleExpr(COMMA simpleExpr)*
    RIGHT_PAREN 
    ;

tablePartitionDescription:
    deferredSegmentCreation?
    segmentAttributesClause?
    ( tableCompression | keyCompression )?
    (OVERFLOW ( segmentAttributesClause )?)?
    ( 
       lobStorageClause
      | varrayColProperties
      | nestedTableColProperties
    )*
    ;

nestedTableColProperties:
    NESTED TABLE
    (columnName | COLUMN_VALUE)
    substitutableColumnClause?
    ( LOCAL | GLOBAL )?
    STORE AS tableName
    ( LEFT_PAREN 
    	( 
          LEFT_PAREN objectProperties RIGHT_PAREN
        | physicalProperties
        | columnProperties 
        )+
      RIGHT_PAREN 
    )?
    (RETURN AS? (LOCATOR | VALUE))?
    ;

objectProperties:
    objectProperty (COMMA objectProperty)*
    ;

objectProperty:
    (( ( columnName | attributeName )
    (DEFAULT expr)?
    (inlineConstraint*  | inlineRefConstraint?))
    | ( outOfLineConstraint
      | outOfLineRefConstraint
      | supplementalLoggingProps
      )
    )
    ;

columnProperties:
    columnProperty+
    ;

columnProperty:
    objectTypeColProperties
    | nestedTableColProperties
    | (( varrayColProperties | lobStorageClause )
        ( LEFT_PAREN lobPartitionStorage ( COMMA lobPartitionStorage )* RIGHT_PAREN )?)
    | xmltypeColumnProperties
    ;
    
objectTypeColProperties:
    COLUMN columnName substitutableColumnClause
    ;
    
rangePartitionDesc:
    PARTITION partitionName
    rangeValuesClause
    tablePartitionDescription
    (  
       LEFT_PAREN  
       (rangeSubpartitionDescs
           | listSubpartitionDescs
           | individualHashSubpartses
        )
        RIGHT_PAREN
      | hashSubpartsByQuantity 
    )?
    ;

rangeSubpartitionDescs:
    rangeSubpartitionDesc ( COMMA rangeSubpartitionDesc)* 
    ;

rangeSubpartitionDesc:
    SUBPARTITION partitionName 
    rangeValuesClause?
    partitioningStorageClause?
    ;

listSubpartitionDescs:
    listSubpartitionDesc ( COMMA listSubpartitionDesc)* 
    ;

listSubpartitionDesc:
    SUBPARTITION partitionName
    listValuesClause?
    partitioningStorageClause?
    ;
    
individualHashSubpartses:
    individualHashSubparts ( COMMA individualHashSubparts)* 
    ;
    
listValuesClause:
    VALUES 
    LEFT_PAREN
    listValues
    RIGHT_PAREN 
    ;

listValues:
    (listValueItem (COMMA listValueItem)*)
    |DEFAULT
    ;
    
listValueItem:
    liter 
    | NULL
    ;

individualHashSubparts:
    SUBPARTITION partitionName partitioningStorageClause?
    ;
    
hashSubpartsByQuantity:
    SUBPARTITIONS NUMBER (STORE IN LEFT_PAREN tablespaceName ( COMMA tablespaceName)* RIGHT_PAREN )?
    ;

enableDisableClause:
    ( ENABLE | DISABLE )
    ( VALIDATE | NOVALIDATE )?
    ( UNIQUE columnList 
    | primaryKey
    | CONSTRAINT constraintName
    )
    ( usingIndexClause )?
    ( exceptionsClause )?
    ( CASCADE )?
    ( ( KEEP | DROP ) INDEX )?
    ;

rowMovementClause:
    ( ENABLE | DISABLE ) ROW 
    ;