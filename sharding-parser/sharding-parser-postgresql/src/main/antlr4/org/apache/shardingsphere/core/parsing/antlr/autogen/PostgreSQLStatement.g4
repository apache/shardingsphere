grammar PostgreSQLStatement;

import PostgreSQLKeyword, Keyword, Symbol, PostgreSQLBase, PostgreSQLDDLStatement, PostgreSQLTCLStatement, PostgreSQLDCLStatement, PostgreSQLDALStatement;

execute
    : (createIndex
    | alterIndex
    | dropIndex
    | createTable
    | alterTable
    | dropTable
    | truncateTable
    | setTransaction
    | beginTransaction
    | commit
    | rollback
    | savepoint
    | grant
    | revoke
    | createUser
    | dropUser
    | alterUser
    | createRole
    | dropRole
    | alterRole
    | show
    | setParam
    | resetParam
    ) SEMI_?
    ;
