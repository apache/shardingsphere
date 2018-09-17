grammar SQLServerAlterIndex;
import SQLServerKeyword, DataType, Keyword, SQLServerBase, BaseRule, Symbol;

alterIndex:
	alterIndexOp
	(alterRebuild
	| DISABLE
	| alterReorganize
	| alterSet
	| alterResume
	| PAUSE
	| ABORT)
	;

alterIndexOp:
	ALTER INDEX (indexName | ALL) ON tableOrViewName
	;

alterRebuild:
    REBUILD (((PARTITION EQ_OR_ASSIGN ALL)? withRebuildIndexOptions?)
    | ((PARTITION EQ_OR_ASSIGN NUMBER)? withSinglePartitionRebuildIndexOptions?))
    ;

withRebuildIndexOptions:
    WITH LEFT_PAREN rebuildIndexOption (COMMA rebuildIndexOption)* RIGHT_PAREN
    ;

rebuildIndexOptions:
    rebuildIndexOption (COMMA rebuildIndexOption)*
    ;

rebuildIndexOption:
    (PAD_INDEX EQ_OR_ASSIGN (ON | OFF))
    | (FILLFACTOR EQ_OR_ASSIGN NUMBER)
    | (SORT_IN_TEMPDB EQ_OR_ASSIGN (ON | OFF))
    | (IGNORE_DUP_KEY EQ_OR_ASSIGN (ON | OFF))
    | (STATISTICS_NORECOMPUTE EQ_OR_ASSIGN (ON | OFF))
    | (STATISTICS_INCREMENTAL EQ_OR_ASSIGN (ON | OFF))
    | (ONLINE EQ_OR_ASSIGN  (OFF | onLowPriorLockWait))
    | (RESUMABLE EQ_OR_ASSIGN (ON | OFF))
    | (MAX_DURATION EQ_OR_ASSIGN NUMBER (MINUTES)?)
    | (ALLOW_ROW_LOCKS EQ_OR_ASSIGN (ON | OFF))
    | (ALLOW_PAGE_LOCKS EQ_OR_ASSIGN (ON | OFF))
    | (MAXDOP EQ_OR_ASSIGN NUMBER)
    | (COMPRESSION_DELAY EQ_OR_ASSIGN (NUMBER (MINUTES)?))
    | (DATA_COMPRESSION EQ_OR_ASSIGN (NONE | ROW | PAGE | COLUMNSTORE | COLUMNSTORE_ARCHIVE) onPartitionClause?)
	;

withSinglePartitionRebuildIndexOptions:
    WITH LEFT_PAREN singlePartitionRebuildIndexOptions RIGHT_PAREN
    ;

singlePartitionRebuildIndexOptions:
    singlePartitionRebuildIndexOption (COMMA singlePartitionRebuildIndexOption)*
    ;

singlePartitionRebuildIndexOption:
    (SORT_IN_TEMPDB EQ_OR_ASSIGN (ON | OFF))
    | (MAXDOP EQ_OR_ASSIGN NUMBER)
    | (RESUMABLE EQ_OR_ASSIGN (ON | OFF))
    | (MAX_DURATION EQ_OR_ASSIGN NUMBER (MINUTES)?)
    | (DATA_COMPRESSION EQ_OR_ASSIGN (NONE | ROW | PAGE | COLUMNSTORE | COLUMNSTORE_ARCHIVE))
    | (ONLINE EQ_OR_ASSIGN (OFF | onLowPriorLockWait))
    ;

alterReorganize:
    REORGANIZE (PARTITION EQ_OR_ASSIGN NUMBER)? withReorganizeOption?
	;

withReorganizeOption:
    WITH LEFT_PAREN reorganizeOption RIGHT_PAREN
    ;

reorganizeOption:
    ((LOB_COMPACTION EQ_OR_ASSIGN (ON | OFF))
    | (COMPRESS_ALL_ROW_GROUPS EQ_OR_ASSIGN (ON | OFF)))
    ;

alterSet:
    SET LEFT_PAREN setIndexOptions RIGHT_PAREN
    ;

setIndexOptions:
    setIndexOption (COMMA setIndexOption)*
    ;

setIndexOption:
    (ALLOW_ROW_LOCKS EQ_OR_ASSIGN (ON | OFF))
    | (ALLOW_PAGE_LOCKS EQ_OR_ASSIGN (ON | OFF))
    | (IGNORE_DUP_KEY EQ_OR_ASSIGN (ON | OFF))
    | (STATISTICS_NORECOMPUTE EQ_OR_ASSIGN (ON | OFF))
    | (COMPRESSION_DELAY EQ_OR_ASSIGN (NUMBER (MINUTES)?))
    ;

alterResume:
    RESUME (WITH LEFT_PAREN resumableIndexOptions RIGHT_PAREN)?
    ;

resumableIndexOptions:
    resumableIndexOption (COMMA resumableIndexOption)*
    ;

resumableIndexOption:
    (MAXDOP EQ_OR_ASSIGN NUMBER)
    | (MAX_DURATION EQ_OR_ASSIGN NUMBER (MINUTES)?)
    | lowPriorityLockWait
    ;