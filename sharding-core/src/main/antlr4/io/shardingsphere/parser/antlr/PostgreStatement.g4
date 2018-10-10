grammar PostgreStatement;

import PostgreBase, PostgreCreateIndex, PostgreAlterIndex, PostgreDropIndex, PostgreCreateTable, PostgreAlterTable, PostgreDropTable, PostgreTruncateTable;

execute
    : createIndex
    | alterIndex
    | dropIndex
    | createTable
    | alterTable
    | dropTable
    | truncateTable
    ;
 