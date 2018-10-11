grammar PostgreStatement;

import PostgreKeyword, Keyword, PostgreBase, PostgreCreateIndex, PostgreAlterIndex, PostgreDropIndex, PostgreCreateTable, PostgreAlterTable, PostgreDropTable, PostgreTruncateTable;

execute
    : createIndex
    | alterIndex
    | dropIndex
    | createTable
    | alterTable
    | dropTable
    | truncateTable
    ;
 