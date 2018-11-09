grammar OracleAlterIndex;

import OracleKeyword, Keyword, BaseRule;

alterIndex
    : ALTER INDEX indexName (RENAME TO indexName)?
    ;
