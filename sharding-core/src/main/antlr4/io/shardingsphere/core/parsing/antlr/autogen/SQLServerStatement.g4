grammar SQLServerStatement;

import SQLServerKeyword, Keyword, SQLServerBase, Symbol, SQLServerCreateIndex, SQLServerAlterIndex
       , SQLServerDropIndex, SQLServerCreateTable, SQLServerAlterTable, SQLServerDropTable, SQLServerTruncateTable
       , SQLServerTCLStatement, SQLServerDCLStatement
       ;

execute
    : (createIndex
    | alterIndex
    | dropIndex
    | createTable
    | alterTable
    | dropTable
    | truncateTable
    | beginTransaction
    | setAutoCommit
    | commit
    | rollback
    | setTransaction
    | savepoint
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
    ) SEMI_?
    ;
