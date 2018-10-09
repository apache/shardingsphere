grammar SQLServerStatement;

import SQLServerCreateIndex, SQLServerAlterIndex, SQLServerDropIndex, SQLServerCreateTable, SQLServerAlterTable, SQLServerDropTable, SQLServerTruncateTable;

execute
    : createIndex
    | alterIndex
    | dropIndex
    | createTable
    | alterTable
    | dropTable
    | truncateTable
    ;
 