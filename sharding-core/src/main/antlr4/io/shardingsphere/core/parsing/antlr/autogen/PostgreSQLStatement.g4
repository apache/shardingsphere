grammar PostgreSQLStatement;

import PostgreSQLKeyword, Keyword, PostgreSQLBase, PostgreSQLCreateIndex, PostgreSQLAlterIndex
       , PostgreSQLDropIndex, PostgreSQLCreateTable, PostgreSQLAlterTable, PostgreSQLDropTable, PostgreSQLTruncateTable
       , PostgreSQLTCLStatement, PostgreSQLDCLStatement
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
    | grantRole
    | revoke
    | revokeRole
    | createUser
    | alterUser
    | renameUser
    | alterUserSetConfig
    | alterUserResetConfig
    | dropUser
    | createRole
    | alterRole
    | renameRole
    | alterRoleSetConfig
    | alterRoleResetConfig
    ;
