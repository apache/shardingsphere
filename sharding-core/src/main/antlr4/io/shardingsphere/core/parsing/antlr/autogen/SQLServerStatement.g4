grammar SQLServerStatement;

import SQLServerKeyword, Keyword, SQLServerBase, SQLServerCreateIndex, SQLServerAlterIndex
       , SQLServerDropIndex, SQLServerCreateTable, SQLServerAlterTable, SQLServerDropTable, SQLServerTruncateTable
       , SQLServerTCLStatement
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
 