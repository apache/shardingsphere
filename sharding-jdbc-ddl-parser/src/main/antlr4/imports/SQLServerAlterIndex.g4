grammar SQLServerAlterIndex;

import SQLServerKeyword, SQLServerBase, BaseRule;

alterIndex:
    ALTER INDEX (indexName | ALL) ON tableName
    ;

