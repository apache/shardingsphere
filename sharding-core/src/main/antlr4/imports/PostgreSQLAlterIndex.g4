grammar PostgreSQLAlterIndex;

import PostgreSQLKeyword, Keyword, PostgreSQLBase, BaseRule;

alterIndex
    : alterIndexName renameIndex | alterIndexDependsOnExtension | alterIndexSetTableSpace
    ;
    
alterIndexName
    : ALTER INDEX (IF EXISTS)? indexName
    ;
    
renameIndex
    : RENAME TO indexName
    ;
    
alterIndexDependsOnExtension
    : ALTER INDEX indexName DEPENDS ON EXTENSION extensionName
    ;
    
alterIndexSetTableSpace
    : ALTER INDEX ALL IN TABLESPACE indexName (OWNED BY rowNames)?
    ;
