grammar MySQLTruncateTable;

import MySQLKeyword, Keyword, BaseRule;

truncateTable
    : TRUNCATE TABLE? tableName
    ;
