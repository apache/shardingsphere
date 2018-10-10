grammar SQLServerDropIndex;

import SQLServerKeyword, Keyword, SQLServerBase, BaseRule;

dropIndex
    : DROP INDEX indexName ON tableName
    ;