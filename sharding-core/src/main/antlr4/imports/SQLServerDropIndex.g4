grammar SQLServerDropIndex;

import SQLServerKeyword, Keyword, SQLServerBase, BaseRule;

dropIndex
    : DROP INDEX (IF EXISTS)? indexName ON tableName
    ;
