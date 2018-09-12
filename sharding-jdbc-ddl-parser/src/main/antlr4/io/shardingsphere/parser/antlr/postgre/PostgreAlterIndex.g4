grammar PostgreAlterIndex;
import PostgreKeyword, DataType, Keyword, PostgreBase,BaseRule,Symbol;

alterIndex:
    (alterIndexName(renameIndex | setTableSpace | setStorageParameter | resetStorageParameter))
    | alterIndexDependsOnExtension
    | alterIndexSetTableSpace
    ;

alterIndexName:
    ALTER INDEX (IF EXISTS)? indexName
    ;

renameIndex:
    RENAME TO indexName
    ;

setTableSpace:
    SET TABLESPACE tablespaceName
    ;

setStorageParameter:
    SET storageParametersWithParen
    ;

resetStorageParameter:
    RESET storageParametersWithParen
    ;

alterIndexDependsOnExtension:
    ALTER INDEX indexName DEPENDS ON EXTENSION extensionName
    ;

alterIndexSetTableSpace:
    ALTER INDEX ALL IN TABLESPACE indexName (OWNED BY rowNames)?
    SET TABLESPACE tablespaceName (NOWAIT)?
    ;