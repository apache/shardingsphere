grammar OracleAlterIndex;

import OracleKeyword, DataType, Keyword,OracleIndexBase,OracleBase,BaseRule,Symbol;

alterIndex:
	ALTER INDEX indexName
	( ( deallocateUnusedClause
	| allocateExtentClause
	| shrinkClause
	| parallelClause
	| physicalAttributesClause
	| loggingClause
	) *
	| rebuildClause
	| PARAMETERS LEFT_PAREN STRING RIGHT_PAREN 
	         RIGHT_PAREN 
	| COMPILE
	| ( ENABLE | DISABLE )
	| UNUSABLE
	| VISIBLE | INVISIBLE
	| RENAME TO indexName
	| COALESCE
	| ( MONITORING | NOMONITORING ) USAGE
	| UPDATE BLOCK REFERENCES
	| alterIndexPartitioning
	)
	;
	
deallocateUnusedClause:
	DEALLOCATE UNUSED ( KEEP sizeClause )?
	;

allocateExtentClause:
	ALLOCATE EXTENT
  	( LEFT_PAREN ( SIZE sizeClause
      | DATAFILE STRING
      | INSTANCE NUMBER
      ) *
    RIGHT_PAREN 
  	)?
	;
	
shrinkClause:
	SHRINK SPACE ( COMPACT )? ( CASCADE )?
	;

rebuildClause:
	REBUILD
	( ( PARTITION partitionName
	| SUBPARTITION partitionName
	)
	| ( REVERSE | NOREVERSE )
	)?
	( parallelClause
	| TABLESPACE tablespaceName
	| PARAMETERS LEFT_PAREN STRING RIGHT_PAREN 
	//| xmlindexParametersClause
	| ONLINE
	| physicalAttributesClause
	| keyCompression
	| loggingClause
	)*
	;

alterIndexPartitioning:
	 modifyIndexDefaultAttrs
	| addHashIndexPartition
	| modifyIndexPartition
	| renameIndexPartition
	| dropIndexPartition
	| splitIndexPartition
	| coalesceIndexPartition
	| modifyIndexSubpartition
	;
	
modifyIndexDefaultAttrs:
	MODIFY DEFAULT ATTRIBUTES
	   ( FOR PARTITION partitionName )?
	   ( physicalAttributesClause
	   | TABLESPACE ( tablespaceName | DEFAULT )
	   | loggingClause
	   )*
	;
	
addHashIndexPartition:
	ADD PARTITION
   	( partitionName )?
   	( TABLESPACE tablespaceName )?
   	( keyCompression )?
   	( parallelClause )?
	;
	
modifyIndexPartition:
	MODIFY PARTITION partitionName
	( ( deallocateUnusedClause
	  | allocateExtentClause
	  | physicalAttributesClause
	  | loggingClause
	  | keyCompression
	  )*
	| PARAMETERS LEFT_PAREN STRING RIGHT_PAREN 
	| COALESCE
	| UPDATE BLOCK REFERENCES
	| UNUSABLE
	)
	;
	
renameIndexPartition:
	RENAME
	  ( PARTITION partitionName | SUBPARTITION partitionName )
	TO 
	;
	
dropIndexPartition:
	DROP PARTITION 
	;
	
splitIndexPartition:
	SPLIT PARTITION partitionName
  	AT simpleExprsWithParen 
	( INTO LEFT_PAREN indexPartitionDescription COMMA 
	indexPartitionDescription
	RIGHT_PAREN 
	)?
	( parallelClause )?
	;

indexPartitionDescription:
	PARTITION
	(	 partitionName
   		( (segmentAttributesClause| keyCompression)+
   		| (PARAMETERS LEFT_PAREN STRING RIGHT_PAREN)
   		)?
   		UNUSABLE?
	)?
	;
	
coalesceIndexPartition:
	COALESCE PARTITION ( parallelClause )?
	;
	
modifyIndexSubpartition:
	MODIFY SUBPARTITION partitionName
	( UNUSABLE
	| allocateExtentClause
	| deallocateUnusedClause
	)
	;
