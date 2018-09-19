grammar OracleTruncateTable;

import OracleKeyword, DataType, Keyword,BaseRule,Symbol;

truncateTable:
    TRUNCATE TABLE  tableName
    ((PRESERVE | PURGE) MATERIALIZED VIEW LOG)?
    ((DROP  ALL ? | REUSE) STORAGE)? 
    ;