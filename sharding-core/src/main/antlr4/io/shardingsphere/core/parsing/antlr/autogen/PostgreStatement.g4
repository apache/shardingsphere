grammar PostgreStatement;

import PostgreKeyword, Keyword, PostgreBase, PostgreCreateIndex, PostgreAlterIndex
       , PostgreDropIndex, PostgreCreateTable, PostgreAlterTable, PostgreDropTable, PostgreTruncateTable
       , PostgreTCLStatement
       ;

execute
    : createIndex
    | alterIndex
    | dropIndex
    | createTable
    | alterTable
    | dropTable
    | truncateTable
    | setTransaction
    | commit
    | rollback
    | savepoint
    | beginWork
    ;
 