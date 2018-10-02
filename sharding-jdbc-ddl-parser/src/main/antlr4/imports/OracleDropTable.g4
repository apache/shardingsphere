grammar OracleDropTable;

import OracleKeyword, BaseRule;

dropTable:
    DROP TABLE tableName
    (CASCADE CONSTRAINTS)?  PURGE? 
    ;