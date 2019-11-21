grammar SQLServerAlterIndex;

import SQLServerKeyword, Keyword, SQLServerBase, BaseRule;

alterIndex
    : ALTER INDEX (indexName | ALL) ON tableName
    ;
