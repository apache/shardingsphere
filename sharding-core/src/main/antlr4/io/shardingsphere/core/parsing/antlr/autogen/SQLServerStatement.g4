grammar SQLServerStatement;

import SQLServerKeyword, Keyword, SQLServerBase, SQLServerCreateIndex, SQLServerAlterIndex
       , SQLServerDropIndex, SQLServerCreateTable, SQLServerAlterTable, SQLServerDropTable, SQLServerTruncateTable
       , SQLServerTCLStatement, SQLServerDCLStatement
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
    | grant
    | revoke
    | deny
    | createUser
    | alterUser
    | dropUser
    | createLogin
    | alterLogin
    | dropLogin
    | createRole
    | alterRole
    | dropRole
    ;
